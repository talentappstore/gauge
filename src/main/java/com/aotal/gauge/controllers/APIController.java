package com.aotal.gauge.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

//import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.jpa.Assessment;
import com.aotal.gauge.jpa.AssessmentRepository;
import com.aotal.gauge.pojos.AppStatus;
import com.aotal.gauge.pojos.Tenant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class APIController {

	private static final Logger logger = LoggerFactory.getLogger(APIController.class);

	public final static String APP = "gauge";
	public final static String SECRET = "KR9xElELRHS7mlrdpmJug-pytvxVT3NySlVuP2JY";
	public final static String APPBASE = "https://" + APP + ".communityapps.talentappstore.com";

	@Autowired
	private AssessmentRepository assessmentRepo;

	@Autowired
	private AccountRepository accountRepo;
	
	@Autowired
	RestTemplate restTemplate;

	/**
	 * helper method to make API calls with the tazzy-secret header as required when making API calls on TAS, and optionally a request body
	 * @return
	 */
	static HttpEntity<String> entityWithSecret(String body, MediaType contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", SECRET);  // attaching the "tazzy-secret" request header
		headers.setContentType(contentType);
		return new HttpEntity<String>(body, headers);
	}

	static HttpEntity entityWithSecret() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", SECRET);  // attaching the "tazzy-secret" request header
		return new HttpEntity(headers);
	}


	/////////////////////////////////////////////////////
	// API Helpers

	// call tenant API GET /assessments/byID/{id} to fetch the assessment detail 
	private ObjectNode fetchAssessment(String tenant, long id) throws JsonParseException, JsonMappingException, IOException {
		logger.info("calling GET /assessments/byID/{id} for tenant " + tenant + " and id " + id);
		String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/assessments/byID/" + id;
		String assessmentDetail = restTemplate.exchange(url, HttpMethod.GET, entityWithSecret(), String.class).getBody(); 
		logger.info("assessment details is: " + assessmentDetail);
		ObjectNode assessment = new ObjectMapper().readValue(new StringReader(assessmentDetail), ObjectNode.class);
		return assessment;
	}

	// call tenant API GET /assessments/byID/{id} to fetch the assessment detail 
	private ObjectNode fetchView(String tenant, String key) throws JsonParseException, JsonMappingException, IOException {
		logger.info("calling GET /applications/views/byKey/{key} for tenant " + tenant + " and key " + key);
		String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/applications/views/byKey/" + key;
		String viewDetail = restTemplate.exchange(url, HttpMethod.GET, entityWithSecret(), String.class).getBody(); 
		logger.info("view detail is: " + viewDetail);
		ObjectNode view = new ObjectMapper().readValue(new StringReader(viewDetail), ObjectNode.class);
		return view;
	}

	

	/////////////////////////////////////////////////////
	// Tenant APIs

	// respond with details of our app, e.g. its landing page (when user clicks "open" on the app in the storefront) 
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/appStatus", method = RequestMethod.GET)
	public AppStatus appStatus(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in GET /appStatus for tenant " + tenant);

		if (! secret.equals(SECRET)) {
				logger.error("mismatching keys: code has " + SECRET + ", incoming is " + secret);
		}
		
		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		// tell the user that setup is needed if they have no credits left
		Account account = accountRepo.findByTenant(tenant);
/*		
		String ret = 			
				"{" +
						"  \"landingPage\": \"" + APPBASE + "/t/" + tenant + "/account\"," +
						"  \"setupRequired\": " + (account.getCreditsRemaining() == 0 ? "true": "false") +
						"}";
*/
;
		
		return new AppStatus(
				APPBASE + "/t/" + tenant + "/account",
				null,
				account.getCreditsRemaining() == 0);
	}


	// Respond with a full list of details of all of our assessment types. In this case, we only have one.
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessmentTypes/forApp", method = RequestMethod.GET)
	public String getAssessmentTypes(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in GET /assessmentTypes/forApp for tenant " + tenant);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		String imageUrl = APPBASE + "/img/gauge.png";

		String ret = 			
				"		[" +
						"		  {" +
						"		    \"key\": \"gauge\"," +
						"		    \"userTitle\": \"Quick addition test\"," +
						"		    \"daysToExpire\": 365," +
						"		    \"isPassFail\": false," +
						"		    \"canReuse\": true," +
						"		    \"userDescription\": \"Gauge the candidate's ability to add random pairs of numbers together, under time pressure.\"," +
						"		    \"appCommunicatesDirectlyToCandidate\": false," +
						"		    \"image\": \"" + imageUrl + "\"" + 
						"		  }" +
						"		]		";

		return ret;
	}

	// this endpoint gets hit whenever one of our assessments is created, or updated, by a user (or automatically)
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessments/byID/{id}/tenantDeltaPings", method = RequestMethod.POST)
	public String deltaPings(@PathVariable String tenant,
			@PathVariable long id,
			@RequestHeader("tazzy-secret") String secret) throws JsonParseException, JsonMappingException, IOException {

		
		logger.info("in POST /assessments/byID/{id}/tenantDeltaPings for tenant " + tenant + " and id " + id);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		// call GET /assessments/byID/{id} to fetch the assessment detail (which includes the view key) as an ObjectNode
		ObjectNode assessment = fetchAssessment(tenant, id); 

		//	switch, based on the assessment's status
		String status = assessment.get("status").asText();
		if (status.equals("Started")) {
			
			// since we never set our assessments to Error, we know this is a new assessment, not a restarted one. Grab some details
			// from it, and store in our database
			String viewKey = assessment.get("view").asText();
			String givenName = assessment.get("givenName") != null ? assessment.get("givenName").asText() : "";
			String familyName = assessment.get("familyName") != null ? assessment.get("familyName").asText() : "";
			String email = assessment.get("email") != null ? assessment.get("email").asText() : "";

			// in our case, we also want more details - specifically the candidate's phone number - we grab it from the view
			ObjectNode view = fetchView(tenant, viewKey);
			// extract phone number from the view
			String phoneNumber = "0064-9-3660348"; // view.get("candidate").get("person").get("givenName").asText(); 

			// store the assessment details, along with some random addition problems for the candidate 
			Random randy = new Random();
			Assessment newOne = new Assessment(tenant, id, "In progress",
					givenName,
					familyName,
					phoneNumber,
					randy.nextInt(100), // random number between 0 and 99
					randy.nextInt(100),
					randy.nextInt(100),
					randy.nextInt(100),
					randy.nextInt(100),
					randy.nextInt(100),
					randy.nextInt(100),
					randy.nextInt(100));
			assessmentRepo.save(newOne);
			
			String candidateUrl = APPBASE + "/quiz/" + newOne.getKey();

			String reqBody = 
					"	        {" +
							"       	  \"status\": \"In progress\"," +
							"       	  \"interactionUris\": {" +
							"       	    \"candidateInteractionUri\": \"" + candidateUrl + "\"," +
							"       	    \"userInteractionUri\": null," +
							"       	    \"userAttentionRequired\": false" +
							"       	  }" +
							"       	}";

			// now PATCH the assessment to have the candidate URL. For our assessment type, this will cause an email to be sent to the candidate, with
			// this link in it.
			String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/assessments/byID/" + id + "/appDetails";
			logger.info("calling PATCH " + url + " with request body: " + reqBody);
			RestTemplate restTemplatePatcher = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<Void> response = restTemplatePatcher.exchange(url, HttpMethod.PATCH,
					entityWithSecret(reqBody, new MediaType("application", "merge-patch+json")), Void.class);
		} else {
			logger.error("unhandled status " + status);
		}

		return "done!";
	}

	
	/////////////////////////////////////////////////////
	// Core-in APIs

	// purely for example, we don't do anything here (we don't even have a database that might hold customer details)
	@RequestMapping(value = "/tas/core/tenants", method = RequestMethod.POST)
	public void createTenant(@RequestBody Tenant tenant, @RequestHeader("tazzy-secret") String secret) {

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		logger.info("inserting account for tenant " + tenant);
		
		// create an account in our database, with 0 credits
		Account account = new Account(tenant.shortCode, 0,  0);
		accountRepo.save(account);
	}

	// purely for example, we don't do anything here (we don't even have a database that might hold customer details)
	@RequestMapping(value = "/tas/core/tenants/{tenant}", method = RequestMethod.DELETE)
	public void deleteTenant(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in DELETE /tenants/{tenant} for tenant " + tenant);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		logger.info("deleting account for tenant " + tenant);

		// delete the account and all linked assessments in our database, with 0 credits
		Account account = accountRepo.findByTenant(tenant);
		// TODO: delete assessments as well 
		accountRepo.delete(account);
	}


}

