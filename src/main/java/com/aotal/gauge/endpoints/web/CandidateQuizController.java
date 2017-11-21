package com.aotal.gauge.endpoints.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.aotal.gauge.endpoints.TASController;
import com.aotal.gauge.endpoints.UnauthorizedException;
import com.aotal.gauge.endpoints.api.TenantAPIController;
import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.jpa.Assessment;
import com.aotal.gauge.jpa.AssessmentRepository;

/**
 * Web traffic to do with a single assessment/quiz
 *  
 * @author abraae
 *
 */
@Controller
public class CandidateQuizController extends TASController {

	private static final Logger logger = LoggerFactory.getLogger(CandidateQuizController.class);

    // when candidate views the quiz
    @GetMapping("/quiz/{key}")
	public ModelAndView getQuiz(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByAccessKey(key);

		// show quiz page, or redirect to results page if candidate has already completed the quiz
		if (ass.status.equals("In progress")) {
			model.addAttribute("assessment", ass);
			QuizForm qf = new QuizForm(key, ass.q1a, ass.q1b, ass.q2a, ass.q2b, ass.q3a, ass.q3b, ass.q4a, ass.q4b);
			model.addAttribute("quizForm", qf);
			return new ModelAndView("showQuiz");
			
		} else {
			String redirectUrl = inBase + "/quizResult/" + ass.accessKey;
	        return new ModelAndView("redirect:" + redirectUrl);
		}
	}

    // when candidate clicks submit
	@PostMapping("/quiz/{key}")
	public ModelAndView postQuiz(Model model, @PathVariable long key, @ModelAttribute QuizForm q) {
		
		// don't allow the assessment to be completed twice
		Assessment local = assessmentRepo.findByAccessKey(key);
		if (local.status.equals("In progress")) {
		
			// score candidate from 0-100% on # correct answers
			int score = 0;
			if (local.q1a + local.q1b == q.a1) score++;
			if (local.q2a + local.q2b == q.a2) score++;
			if (local.q3a + local.q3b == q.a3) score++;
			if (local.q4a + local.q4b == q.a4) score++;
			score *= 25;
			logger.info("candidate score is " + score);
			
			// update our local copy of the assessment
			local.status = "Complete";
			local.score = score;
			assessmentRepo.save(local);
			patchRemoteAssessment(local);		// patch the remote assessment (i.e. in the hub) via API to match

			// reduce credits by 1 (race conditions not dealt with !)
			Account account = accountRepo.findByTenant(local.tenant);
			account.creditsRemaining--;
			accountRepo.save(account);
		}
		
		String redirectUrl = inBase + "/quizResult/" + local.accessKey;
        return new ModelAndView("redirect:" + redirectUrl);
	}

    // when candidate views their result, after completing the quiz
    @GetMapping("/quizResult/{key}")
	public String getQuizResult(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByAccessKey(key);
		model.addAttribute("score", ass.score);

		return "showResultToCandidate";
	}

    // when user sees candidate's quiz result
    @GetMapping("/t/{tenant}/quizResultUser/{key}")
	public String getQuizResultUser(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByAccessKey(key);
		model.addAttribute("score", ass.score);

		return "showResultToUser";
	}

	@GetMapping("/t/{tenant}/somethingsWrong/{key}")
	public String showError(Model model, @PathVariable long key) {
	
		return "somethingsWrong";
	}

	@GetMapping("/t/{tenant}/underway/{key}")
	public String underway(Model model, @PathVariable long key) {
	
		return "underway";
	}
	
	
}
