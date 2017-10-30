package com.aotal.gauge.api;

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
import org.springframework.core.env.Environment;
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

/**
 * Serve up incoming tenant API calls
 * 
 * @author abraae
 *
 */
@RestController
public class TenantAPIController {

	private static final Logger logger = LoggerFactory.getLogger(TenantAPIController.class);

	@Autowired
	private AssessmentRepository assessmentRepo;
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	RestTemplate restTemplate;
    @Autowired
    private Environment env;
   
	static void logH2Message() {
		logger.error("======================================");
		logger.error(" no account found for tenant - has the h2 database been restarted?");
		logger.error("======================================");
	}

	
	/////////////////////////////////////////////////////
	// API Helpers

	// call tenant API GET /assessments/byID/{id} to fetch the assessment detail 
	private ObjectNode fetchAssessment(String tenant, long id) throws JsonParseException, JsonMappingException, IOException {
		logger.info("calling GET /assessments/byID/{id} for tenant " + tenant + " and id " + id);
		String url = "https://" + env.getProperty("tas.app") + ".tazzy.io/t/" + tenant + "/devs/tas/assessments/byID/" + id;
		String assessmentDetail = restTemplate.exchange(url, HttpMethod.GET, Helpers.entityWithSecret(env), String.class).getBody(); 
		logger.info("assessment details is: " + assessmentDetail);
		ObjectNode assessment = new ObjectMapper().readValue(new StringReader(assessmentDetail), ObjectNode.class);
		return assessment;
	}

	// call tenant API GET /assessments/byID/{id} to fetch the assessment detail 
	private ObjectNode fetchView(String tenant, String key) throws JsonParseException, JsonMappingException, IOException {
		logger.info("calling GET /applications/views/byKey/{key} for tenant " + tenant + " and key " + key);
		String url = "https://" + env.getProperty("tas.app") + ".tazzy.io/t/" + tenant + "/devs/tas/applications/views/byKey/" + key;
		String viewDetail = restTemplate.exchange(url, HttpMethod.GET, Helpers.entityWithSecret(env), String.class).getBody(); 
		logger.info("view detail is: " + viewDetail);
		ObjectNode view = new ObjectMapper().readValue(new StringReader(viewDetail), ObjectNode.class);
		return view;
	}

	
	/////////////////////////////////////////////////////
	// Tenant APIs

	// respond with details of our app, e.g. its landing page (when user clicks "open" on the app in the storefront) 
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/appStatus", method = RequestMethod.GET)
	public String appStatus(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		if (! secret.equals(env.getProperty("tas.secret"))) throw new UnauthorizedException(); // check incoming tazzy-secret

		// tell the user that setup is needed if they have no credits left
		Account account = accountRepo.findByTenant(tenant);
		if (account == null)
			logH2Message();

		String response = "{" +
				"  \"landingPage\": \"" + Helpers.getAppBase(env) + "/tenant/" + tenant + "/account\"," +
				"  \"settingsPage\": \"" + Helpers.getAppBase(env) + "/tenant/" + tenant + "/account\"," +
				"  \"setupRequired\": " + (account.getCreditsRemaining() == 0 ? "true": "false") +
				"}";
		
/*
		AppStatus response = new AppStatus(
				Helpers.getAppBase(env) + "/t/" + tenant + "/account",
				null,
				account.getCreditsRemaining() == 0);
*/
		logger.info("in GET /appStatus for tenant " + tenant + ", response is " + response);
		return response;
	}

	// Respond with a full list of details of all of our assessment types. In this case, we only have one.
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessmentTypes/forApp", method = RequestMethod.GET)
	public String getAssessmentTypes(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in GET /assessmentTypes/forApp for tenant " + tenant);
		if (! secret.equals(env.getProperty("tas.secret"))) throw new UnauthorizedException(); // check incoming tazzy-secret

		String imageUrl = Helpers.getAppBase(env) + "/img/gauge.png";
		String ret = 			
				"		[" +
						"		  {" +
						"		    \"key\": \"mathrace\"," +
						"		    \"userTitle\": \"Addition test\"," +
						"		    \"daysToExpire\": 365," +
						"		    \"isPassFail\": false," +
						"		    \"canReuse\": false," +
						"		    \"userDescription\": \"Gauge the candidate's ability to add random pairs of numbers together, under time pressure.\"," +
						"		    \"candidateDescription\": \"Prove your arithmetic skills by adding random pairs of numbers together! Go as fast as you can.\"," +
						"		    \"appCommunicatesDirectlyToCandidate\": false," +
						"		    \"image\": \"" + imageUrl + "\"" + 
						"		  }" +
						"		]		";

		return ret;
	}

	// create a local copy, in our database, of an assessment we learned about through an API call
	private Assessment createNewAssessment(String tenant, long assessmentID, ObjectNode apiAssessment, String status) throws JsonParseException, JsonMappingException, IOException {
		String viewKey = apiAssessment.get("view").asText();
		String givenName = apiAssessment.get("givenName") != null ? apiAssessment.get("givenName").asText() : "";
		String familyName = apiAssessment.get("familyName") != null ? apiAssessment.get("familyName").asText() : "";
		String email = apiAssessment.get("email") != null ? apiAssessment.get("email").asText() : "";

		// in our case, we also want more details - specifically the candidate's phone number - we grab it from the view
		ObjectNode view = fetchView(tenant, viewKey);
		// extract phone number from the view
		String phoneNumber = "0064-9-3660348"; // view.get("candidate").get("person").get("givenName").asText(); 

		// store the assessment details, along with some random addition problems for the candidate 
		Random randy = new Random();
		Assessment newOne = new Assessment(tenant, assessmentID, status,
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
		
		return newOne;
	}

	// PATCH the assessment to reflect our new local reality
	public static void patchAssessment(Environment env, Assessment assessment, String status, RestTemplate restTemplate) {

		// depending on the assessment's new status, we may/may not offer links to candidate, user, and may/may not show the score image 
		String candidateUrl = null;
		String imageUrl = null;
		String userUrl = null;
		if (status.equals("In progress")) {
			candidateUrl = Helpers.getAppBase(env) + "/quiz/" + assessment.getKey();
			
		} else if (status.equals("Error")) {
			userUrl = Helpers.getAppBase(env) + "/tenant/" + assessment.getTenant() + "/notEnoughCredits/" + assessment.getKey();
			
		} else if (status.equals("Complete")) {
			candidateUrl = Helpers.getAppBase(env) + "/quiz/" + assessment.getKey(); // candidate can still see their own results
			userUrl = Helpers.getAppBase(env) + "/tenant/" + assessment.getTenant() + "/quizResultUser/" + assessment.getKey();
			imageUrl = "https://16c4b5fa.ngrok.io" + "/scoreWithIcon.png?score=" + assessment.getScore() + "&label=GA";
		}
 
		String reqBody = 
						"        {" +
						"      	  \"status\": \"" + status + "\"," +
						"         \"image\": "
										+ (imageUrl != null ? ("\"" + imageUrl + "\"") : "null") + "," +
						"      	  \"interactionUris\": {" +
						"      	    \"candidateInteractionUri\": "
										+ (candidateUrl != null ? ("\"" + candidateUrl + "\"") : "null") + "," +
						"      	    \"userInteractionUri\": "
										+ (userUrl != null ? ("\"" + userUrl + "\"") : "null") + "," +
						"      	    \"userAttentionRequired\": "
										+ (status.equals("Error") ? "true" : "false") +
						"      	  }" +
						"      	}";

		// now PATCH the assessment. If we've changed the candidate url, this will cause an email to be sent to the candidate
		String url = "https://" + env.getProperty("tas.app") + ".tazzy.io/t/" + assessment.getTenant() + "/devs/tas/assessments/byID/" + assessment.getAssessmentID() + "/appDetails";
		logger.info("calling PATCH " + url + " with request body: " + reqBody);
//		RestTemplate restTemplatePatcher = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH,
				Helpers.entityWithSecret(env, reqBody, new MediaType("application", "merge-patch+json")), Void.class);
	}

	
	// this endpoint gets hit whenever one of our assessments is created, or updated, by a user (or automatically)
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessments/byID/{id}/tenantDeltaPings", method = RequestMethod.POST)
	public String deltaPings(@PathVariable String tenant,
			@PathVariable long id,
			@RequestHeader("tazzy-secret") String secret) throws JsonParseException, JsonMappingException, IOException {

		logger.info("in POST /assessments/byID/{id}/tenantDeltaPings for tenant " + tenant + " and id " + id);
		if (! secret.equals(env.getProperty("tas.secret"))) throw new UnauthorizedException(); // check incoming tazzy-secret

		// get details from local db
		Account account = accountRepo.findByTenant(tenant);
		if (account == null)
			logH2Message();

		// get assessment details via API call to GET /assessments/byID/{id}
		ObjectNode apiAssessment = fetchAssessment(tenant, id); 

		//	switch, based on the assessment's status
		String status = apiAssessment.get("status").asText();
		if (status.equals("Started")) {

			// either (a) this is a new assessment, or (b) the user is restarting an existing one after clearing the error condition (insufficient credits)
			Assessment ass = assessmentRepo.findByAssessmentID(id);
			if (ass == null) { 	// must be a new one
				if (account.getCreditsRemaining() > 0) {
					ass = createNewAssessment(tenant, id, apiAssessment, "In progress");
					patchAssessment(env, ass, "In progress", restTemplate);
				} else  { // still no credits, set it back to error
					ass = createNewAssessment(tenant, id, apiAssessment, "Error");
					patchAssessment(env, ass, "Error", restTemplate);
				}
				
			} else { // its an existing one
				if (ass.getStatus().equals("Error")) {
					if (account.getCreditsRemaining() > 0) {
						// if we have enough credits, we can restart the assessment
						ass.setStatus("In progress");
						assessmentRepo.save(ass);
						patchAssessment(env, ass, "In progress", restTemplate);
					} else {
						logger.info("user tried to restart an Error-ed assessment, but there are still not enough credits");
						ass.setStatus("Error");
						assessmentRepo.save(ass);
						patchAssessment(env, ass, "Error", restTemplate);
						return "error";
					}
				}
			}
		} else
			logger.error("unhandled status " + status);

		return "done!";
	}


}

