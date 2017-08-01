package com.aotal.gauge;

import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebController {

  private static final Logger logger = LoggerFactory.getLogger(WebController.class);

  private RestTemplate restTemplate = new RestTemplate();

  // holy security alert Batman - passing details directly within the URL like this allows an attacker candidate to forge
  // a url to access any assessment! Add a signature at least, or use a database, in anything but a demo like this.
  @RequestMapping("/tenants/{tenant}/quiz")
  public String showQuiz(Model model, @PathVariable String tenant, @RequestParam int assessment,
		  @RequestParam int num1,
		  @RequestParam int num2
//		  @RequestParam String givenName,
//		  @RequestParam String familyName,
//		  @RequestParam String email
		  ) {
	  model.addAttribute("tenant", tenant);
	  model.addAttribute("assessment", assessment);
	  model.addAttribute("num1", num1);
	  model.addAttribute("num2", num2);
//	  model.addAttribute("givenName", givenName);
//	  model.addAttribute("familyName", familyName);
//	  model.addAttribute("email", email);
	  
	  return "showQuiz";
  }

  
}

