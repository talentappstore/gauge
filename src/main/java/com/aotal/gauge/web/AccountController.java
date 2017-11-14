package com.aotal.gauge.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.aotal.gauge.UnauthorizedException;
import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.pojos.SamlDetail;
import com.aotal.gauge.pojos.Tenant;

/**
 * Web traffic to do with the account page
 * 
 * @author abraae
 *
 */
@Controller
public class AccountController {

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountRepository repo;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private Environment env;
	@Autowired
	private String appBase;

    // CRITICAL: for every endpoint in this controller, check that the incoming tazzy-secret matches our secret key
	@ModelAttribute
	private void verify(@RequestHeader("tazzy-secret") String secret) {
		if (! secret.equals(env.getProperty("tas.secret"))) throw new UnauthorizedException();
	}
	
	// call core APIs to fetch tenant and signed in user details, and attach them to the model
	private void populateModel(Account account, String samlKey, Model model) {
		model.addAttribute("account", account);
		// get tenant details and attach
		{
			String url = "https://" + env.getProperty("tas.app") + ".tazzy.io/core/tenants/" + account.getTenant();
			logger.info("calling GET " + url);
			Tenant tenantObject = restTemplate.exchange(url, HttpMethod.GET, null, Tenant.class).getBody(); 
			model.addAttribute("tenant", tenantObject);
		}
		
		{
			String url = "https://" + env.getProperty("tas.app") + ".tazzy.io/core/tenants/" + account.getTenant()
					+ "/saml/assertions/byKey/" + samlKey + "/json";
			logger.info("calling GET " + url);
			SamlDetail sam = restTemplate.exchange(url, HttpMethod.GET, null, SamlDetail.class).getBody();
			model.addAttribute("samlDetail", sam);
		}
	}
	
	// when user views their account
    @GetMapping("/t/{tenant}/account")
	public String getAccountDetail(Model model, @PathVariable String tenant,
			@RequestHeader("tazzy-saml") String tazzySaml) { // , @RequestHeader("Authorization") String authorization) {
    	logger.info("tazzy-saml: " + tazzySaml);
		// add loads of data to the model
		Account account = repo.findByTenant(tenant);
		populateModel(account, tazzySaml, model);
		return "account";
//		return "materializeAccount";
	}

    // when candidate clicks to request 1 credits
    @RequestMapping(value = "/t/{tenant}/account", params = "1credit", method = RequestMethod.POST)
	public String credit1Account(Model model, @PathVariable String tenant,
			@RequestHeader("tazzy-saml") String tazzySaml) { // , @RequestHeader("Authorization") String authorization) {
    	return creditAccount(model, tenant, tazzySaml, 1);
	}

    // when candidate clicks to request 5 credits
    @RequestMapping(value = "/t/{tenant}/account", params = "5credits", method = RequestMethod.POST)
	public String credit5Account(Model model, @PathVariable String tenant,
			@RequestHeader("tazzy-saml") String tazzySaml) { // , @RequestHeader("Authorization") String authorization) {
    	return creditAccount(model, tenant, tazzySaml, 5);
	}

	public String creditAccount(Model model, String tenant, String tazzySaml, int numCredits) {
		// update credits on database
		Account account = repo.findByTenant(tenant);
		account.setCreditsRemaining(account.getCreditsRemaining() + numCredits);
		repo.save(account);
		// load up model and redisplay
		populateModel(account, tazzySaml, model);
		return "account";
	}
    
    
}
