package com.aotal.gauge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class CandidateQuizController {


	private static final Logger logger = LoggerFactory.getLogger(CandidateQuizController.class);

	@Autowired
	private AssessmentRepository repo;
	
    // when candidate views the quiz
    @GetMapping("/quiz/{key}")
	public String getQuiz(Model model, @PathVariable long key) {

		Assessment ass = repo.findByKey(key);
		model.addAttribute("assessment", ass);

		QuizForm qf = new QuizForm(key, ass.getQ1a(), ass.getQ1b(), ass.getQ2a(), ass.getQ2b(), ass.getQ3a(), ass.getQ3b(), ass.getQ4a(), ass.getQ4b());
       model.addAttribute("quizForm", qf);

		return "showNewQuiz";
	}


    // when candidate clicks submit
	@PostMapping("/quiz/{key}")
	public String postQuiz(Model model, @PathVariable long key, @ModelAttribute QuizForm q) {
		
		// retrieve details for the asessment
		Assessment ass = repo.findByKey(key);
		
		// score candidate from 0-4 on # correct answers
		int score = 0;
		if (ass.getQ1a() + ass.getQ1b() == q.getA1()) score++;
		if (ass.getQ2a() + ass.getQ2b() == q.getA2()) score++;
		if (ass.getQ3a() + ass.getQ3b() == q.getA3()) score++;
		if (ass.getQ4a() + ass.getQ4b() == q.getA4()) score++;
		
		logger.info("candidate score is " + score);
		return "showQuiz";
	}
	
}
