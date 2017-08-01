package com.aotal.gauge;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class APIController {

	private static final Logger logger = LoggerFactory.getLogger(APIController.class);

	public final static String APP = "gauge";
	public final static String SECRET = "NKhBcvL7OGFaIkRf4G-mGPgzr0RMDOGYO0EWjbY_";
	public final static String APPBASE = "https://" + APP + ".communityapps.talentappstore.com";


	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * helper method to make API calls with the tazzy-secret header as required when making API calls on TAS, and optionally a request body
	 * @return
	 */
	private HttpEntity<String> entityWithSecret(String body, MediaType contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", SECRET);  // attaching the "tazzy-secret" request header
		headers.setContentType(contentType);
		return new HttpEntity<String>(body, headers);
	}

	private HttpEntity entityWithSecret() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", SECRET);  // attaching the "tazzy-secret" request header
		return new HttpEntity(headers);
	}

	
	/////////////////////////////////////////////////////
	// Tenant APIs

	// respond with details of our app, e.g. its landing page (when user clicks "open" on the app in the storefront) 
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/appStatus", method = RequestMethod.GET)
	public String appStatus(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in GET /appStatus for tenant " + tenant);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		String ret = 			
				"{" +
						"  \"landingPage\": \"" + APPBASE + "/tenants/" + tenant + "\"," +
						"  \"setupRequired\": false" +
						"}";

		return ret;
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
						"		    \"userTitle\": \"Math test\"," +
						"		    \"daysToExpire\": 365," +
						"		    \"isPassFail\": true," +
						"		    \"canReuse\": true," +
						"		    \"userDescription\": \"Gauge the candidate's ability to answer a basic arithmetic question.\"," +
						"		    \"appCommunicatesDirectlyToCandidate\": false," +
						"		    \"image\": \"" + imageUrl + "\"" + 
						"		  }" +
						"		]		";

		return ret;
	}

	// this endpoint gets hit whenever one of our assessments is created, or updated, by a user (or automatically)
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessments/byID/{id}/tenantDeltaPings", method = RequestMethod.POST)
	public String deltaPings(@PathVariable String tenant, @PathVariable long id, @RequestHeader("tazzy-secret") String secret) throws JsonParseException, JsonMappingException, IOException {

		logger.info("in POST /assessments/byID/{id}/tenantDeltaPings for tenant " + tenant + " and id " + id);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret

		// call GET /assessments/byID/{id} to fetch the assessment detail (which includes the view key)
		String assessmentDetail;
		{
			logger.info("calling GET /assessments/byID/{id} for tenant " + tenant + " and id " + id);
			String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/assessments/byID/" + id;
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entityWithSecret(), String.class);
			assessmentDetail = response.getBody();
			logger.info("assessment details is: " + assessmentDetail);
		}

		// prepare to dig into the assessment body. We're using dynamic json handling, not POJOs, so this is a little verbose (Java!)
		ObjectNode assessment;
		{
			Reader reader = new StringReader(assessmentDetail);
			ObjectMapper objectMapper = new ObjectMapper();
			assessment = objectMapper.readValue(reader, ObjectNode.class);
		}

		// pull the view key out from the assessment detail
		int viewKey = assessment.get("view").asInt();

		// now call GET /applications/views/byKey/{key} to grab the view itself via another API call
		String givenName;
		String familyName;
		String email;
		{
			logger.info("calling GET /applications/views/byKey/{key} for tenant " + tenant + " and key " + viewKey);
			String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/applications/views/byKey/{key}";
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entityWithSecret(), String.class, viewKey);
			String viewDetail = response.getBody();
			logger.info("view detail is: " + viewDetail);

			// prepare to dig into the view body
			ObjectNode view;
			{
				Reader reader = new StringReader(viewDetail);
				ObjectMapper objectMapper = new ObjectMapper();
				view = objectMapper.readValue(reader, ObjectNode.class);
			}

			// extract basic details from the json. For more complex stuff - phone numbers, user defined fields, etc. - we might have to parse the vcard, look at items, etc.
			givenName =		view.get("candidate").get("person").get("givenName").asText(); 
			familyName =	view.get("candidate").get("person").get("familyName").asText(); 
			email =			""; // view.get("candidate").get("person").get("email").asText();  TODO: reinstate once bug fixed
		}

		//	switch, based on the assessment's status
		String status = assessment.get("status").asText();
		if (status.equals("Started")) {
			// if the assessment is new, then we want to set up the candidate's quiz, point them to it, and set status to "In progress"
			{
				// this demo code has terrible security fails - e.g., we burn details into the url to avoid the need for a db, we do bad init of the Random object, etc.
				Random randy = new Random();
				String candidateUrl = APPBASE + "/tenants/" + tenant + "/quiz"
		        		+ "?assessment=" + id
		        		+ "&num1=" + randy.nextInt(10) // random number between 0 and 9
		        		+ "&num2=" + randy.nextInt(10) // random number between 0 and 9
		        		
				// These arguments have been omitted until https://github.com/talentappstore/assessmenthub/issues/85 is fixed 
				//		        		+ "&givenName=" + URLEncoder.encode(givenName, "UTF-8")
				//		        		+ "&familyName=" + URLEncoder.encode(familyName, "UTF-8")
				//		        		+ "&email=" + URLEncoder.encode(email, "UTF-8")
				;

				String reqBody = 
						"	        {" +
								"       	  \"status\": \"In progress\"," +
								"       	  \"interactionUris\": {" +
								"       	    \"candidateInteractionUri\": \"" + candidateUrl + "\"," +
								"       	    \"userInteractionUri\": null," +
								"       	    \"userAttentionRequired\": false" +
								"       	  }" +
								"       	}";

				// now PATCH the assessment to have the candidate URL. This will cause an email to be sent to the candidate, with this link in it, leading to our web page.
				// Many apps will handle their own email communication with the candidate (assuming the tenant has decided to give them access to the email).
				String url = "https://" + APP + ".tazzy.io/t/" + tenant + "/devs/tas/assessments/byID/" + id + "/appDetails";
				logger.info("calling PATCH " + url + " with request body: " + reqBody);
				RestTemplate restTemplatePatcher = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
				ResponseEntity<Void> response = restTemplatePatcher.exchange(url, HttpMethod.PATCH,
						entityWithSecret(reqBody, new MediaType("application", "merge-patch+json")), Void.class);
			}
		} else {
			logger.info("unhandled status " + status);
		}

		return "done!";
	}

	
	/////////////////////////////////////////////////////
	// Core-in APIs

	// purely for example, we don't do anything here (we don't even have a database that might hold customer details)
	@RequestMapping(value = "/tas/core/tenants", method = RequestMethod.POST)
	public void createTenant(@RequestBody String body, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in POST /tenants with payload " + body );

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret
	}

	// purely for example, we don't do anything here (we don't even have a database that might hold customer details)
	@RequestMapping(value = "/tas/core/tenants/{tenant}", method = RequestMethod.DELETE)
	public void deleteTenant(@PathVariable String tenant, @RequestHeader("tazzy-secret") String secret) {

		logger.info("in DELETE /tenants/{tenant} for tenant " + tenant);

		if (! secret.equals(SECRET)) throw new UnauthorizedException(); // check incoming tazzy-secret
	}


}

