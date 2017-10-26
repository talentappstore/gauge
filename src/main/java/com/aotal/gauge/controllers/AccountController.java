package com.aotal.gauge.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.jpa.Assessment;
import com.aotal.gauge.jpa.AssessmentRepository;
import com.aotal.gauge.pojos.Tenant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Controller
public class AccountController {


	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountRepository repo;
	
	@Autowired
	private RestTemplate rest;

	// when user views their account
    @GetMapping("/t/{tenant}/account")
	public String getAccount(Model model, @PathVariable String tenant) {

		Account account = repo.findByTenant(tenant);
		model.addAttribute("used", account.getCreditsUsed());
		model.addAttribute("remaining", account.getCreditsRemaining());

		// call core API to fetch tenant details, and attach to model as well
		logger.info("calling GET /tenants/{} for tenant " + tenant);
		String url = "https://" + APIController.APP + ".tazzy.io/core/tenants/" + tenant;
		Tenant tenantObject = rest.exchange(url, HttpMethod.GET, APIController.entityWithSecret(), Tenant.class).getBody(); 
		model.addAttribute("tenant", tenant);
		
		return "account";
	}

    // when candidate clicks to request credits
    @RequestMapping(value = "/t/{tenant}/account", params = "1credit", method = RequestMethod.POST)
	public String credit1Account(Model model, @PathVariable String tenant) {
		
		// update credits on database
		Account account = repo.findByTenant(tenant);
		account.setCreditsRemaining(account.getCreditsRemaining() + 1);
		repo.save(account);

		return "account";
	}

    // when candidate clicks to request credits
    @RequestMapping(value = "/t/{tenant}/account", params = "5credits", method = RequestMethod.POST)
	public String credit5Account(Model model, @PathVariable String tenant) {
		
		// update credits on database
		Account account = repo.findByTenant(tenant);
		account.setCreditsRemaining(account.getCreditsRemaining() + 5);
		repo.save(account);

		return "account";
	}
	
}
