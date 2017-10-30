package com.aotal.gauge.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.aotal.gauge.api.Helpers;
import com.aotal.gauge.api.TenantAPIController;
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
public class CandidateQuizController {

	private static final Logger logger = LoggerFactory.getLogger(CandidateQuizController.class);

	@Autowired
	private AssessmentRepository assessmentRepo;
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private Environment env;
	
    // when candidate views the quiz
    @GetMapping("/quiz/{key}")
	public ModelAndView getQuiz(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByKey(key);

		// show quiz page, or redirect to results page if candidate has already completed the quiz
		if (ass.getStatus().equals("In progress")) {
			model.addAttribute("assessment", ass);
			QuizForm qf = new QuizForm(key, ass.getQ1a(), ass.getQ1b(), ass.getQ2a(), ass.getQ2b(), ass.getQ3a(), ass.getQ3b(), ass.getQ4a(), ass.getQ4b());
			model.addAttribute("quizForm", qf);
			return new ModelAndView("showQuiz");
			
		} else {
			String redirectUrl = Helpers.getAppBase(env) + "/quizResult/" + ass.getKey();
	        return new ModelAndView("redirect:" + redirectUrl);
		}
	}

    // when candidate clicks submit
	@PostMapping("/quiz/{key}")
	public ModelAndView postQuiz(Model model, @PathVariable long key, @ModelAttribute QuizForm q) {
		
		// retrieve from db
		Assessment ass = assessmentRepo.findByKey(key);

		// don't allow the assessment to be completed twice
		if (ass.getStatus().equals("In progress")) {
		
			// score candidate from 0-100% on # correct answers
			int score = 0;
			if (ass.getQ1a() + ass.getQ1b() == q.getA1()) score++;
			if (ass.getQ2a() + ass.getQ2b() == q.getA2()) score++;
			if (ass.getQ3a() + ass.getQ3b() == q.getA3()) score++;
			if (ass.getQ4a() + ass.getQ4b() == q.getA4()) score++;
			score *= 25;
			logger.info("candidate score is " + score);
			
			// update our local copy of the assessment
			ass.setStatus("Complete");
			ass.setScore(score);
			assessmentRepo.save(ass);

			// reduce credits by 1 (race conditions not dealt with !)
			Account account = accountRepo.findByTenant(ass.getTenant());
					
			// patch the master assessment (i.e. in the hub) via API, to be "Complete"
			TenantAPIController.patchAssessment(env, ass, "Complete", restTemplate);
		}
		
		String redirectUrl = Helpers.getAppBase(env) + "/quizResult/" + ass.getKey();
        return new ModelAndView("redirect:" + redirectUrl);
	}

    // when candidate views their result, after completing the quiz
    @GetMapping("/quizResult/{key}")
	public String getQuizResult(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByKey(key);
		model.addAttribute("score", ass.getScore());

		return "showResultToCandidate";
	}

    // when user sees candidate's quiz result
    @GetMapping("/tenant/{tenant}/quizResultUser/{key}")
	public String getQuizResultUser(Model model, @PathVariable long key) {

		Assessment ass = assessmentRepo.findByKey(key);
		model.addAttribute("score", ass.getScore());

		return "showResultToUser";
	}

	@GetMapping("/tenant/{tenant}/notEnoughCredits/{key}")
	public String showError(Model model, @PathVariable long key) {
	
		return "notEnoughCredits";
	}

	
}
