package com.aotal.gauge.endpoints.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.aotal.gauge.GaugeApplication;
import com.aotal.gauge.boilerplate.TASController;
import com.aotal.gauge.boilerplate.UnauthorizedException;
import com.aotal.gauge.boilerplate.api.pojos.AppStatus;
import com.aotal.gauge.boilerplate.api.pojos.Tenant;
import com.aotal.gauge.endpoints.GaugeController;
import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.jpa.Assessment;
import com.aotal.gauge.jpa.AssessmentRepository;
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
public class TenantAPIController extends GaugeController {

	private static final Logger logger = LoggerFactory.getLogger(TenantAPIController.class);

	static void logH2Message() {
		logger.error("======================================");
		logger.error(" no account found for tenant - has the database been cleared down?");
		logger.error("======================================");
	}
	
	// respond with details of our app, e.g. its landing page (when user clicks "open" on the app in the storefront) 
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/appStatus", method = RequestMethod.GET)
	public AppStatus appStatus(@PathVariable String tenant) throws JsonParseException, JsonMappingException, IOException {

		Account account = accountRepo.findByTenant(tenant);
		if (account == null)
			logH2Message();

		AppStatus response = new AppStatus();
		response.landingPage = inBase + "/t/" + tenant + "/account";
		response.settingsPage = inBase + "/t/" + tenant + "/account";
		response.setupRequired = account.creditsRemaining <= 0;    // tell the user that setup is needed if they have no credits left

		return response;
	}

	// Respond with a full list of details of all of our assessment types. In this case, we only have one.
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessmentTypes/forApp", method = RequestMethod.GET)
	public String getAssessmentTypes(@PathVariable String tenant) throws JsonParseException, JsonMappingException, IOException {

		String imageUrl = inBase + "/img/gauge.png";
		// a little nasty, but can't be bothered creating POJOs since we only have a single, unchanging assessment type 
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

	// load the local Assessment object with fields from the view (givenName etc.)
	private void loadViewFields(Assessment local) throws JsonParseException, JsonMappingException, IOException { //  throws JsonParseException, JsonMappingException, IOException {

		// we also want the candidate's name, email and phone number - make API call to get it from the view.
		// Since the view is so deeply nested, we can't be bothered creating POJOs, so use jackson's dynamic approach instead
		ObjectNode view;
		{
			String url = outBase + "/t/" + local.tenant + "/devs/tas/applications/views/byKey/" + local.view;
			String viewDetail = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody(); 
			view = new ObjectMapper().readValue(new StringReader(viewDetail), ObjectNode.class);
		}
		
		/*
		 The example view that comes back from Tiny ATS looks like the below, but the actual view that comes back
		 in production will depend on what fields each specific customer dragged across into their view. Its important for assessment types to explain
		 to the customer in their description what fields they require, so the customer can set up the view correctly, and to give good error messages
		 when the view does not hold the data the app requires.
		 
		 {
			"id": 864,
			"bucketMovements": [],
			"candidate": {
				"id": 1,
				"person": {
					"givenName": "1",
					"familyName": "1",
					"email": "abraae@gmail.com",
					"vcard": ["vcard", [
						["version", {}, "text", "4.0"],
						["prodid", {}, "text", "ez-vcard 0.9.11"],
						["email", {}, "text", "abraae@gmail.com"],
						["n", {}, "text", ["1", "1", "", "", ""]],
						["tel", {}, "text", "0414999999"]
					]]
				},
				"items": [],
				"categories": []
			},
			"job": {
				"id": 1,
				"code": "1",
				"title": "1",
				"categories": [],
				"items": []
			},
			"items": []
		}
		 
		*/ 

		// the view is under the control of the customer, so its very possible they will screw up and not provide the data we expect.
		// In that case, set the assessment to Error and give them a sturdy error message so they can go back and edit the view.
		// TODO more detailed error handling to tell the user exactly which field was missing
		try {
			local.givenName = view.get("candidate").get("person").get("givenName").asText();
			local.familyName = view.get("candidate").get("person").get("familyName").asText();
			local.email = view.get("candidate").get("person").get("email").asText();
	
			// navigate the bizarro world of vcards and grab the first phone number TODO handle > 1 phone number, picking the best one
			Iterator<JsonNode> it = view.get("candidate").get("person").get("vcard").elements();
			it.next(); // skip the silly "vcard" text string
			Iterator<JsonNode> it2 = it.next().elements();
			while (it2.hasNext() ) {
				// see if this group's 1st element is labelled "tel", and if it is, grab the 4th element which will be the number in E164 format
				Iterator<JsonNode> it3 = it2.next().elements();
				if (it3.next().asText().equals("tel")) {
					it3.next();
					it3.next();
					local.phoneNumber = it3.next().asText();
					break; // there could be more phone numbers, depending on the ATS, but we just grab the first and bail 
				}
			}
		} catch (NoSuchElementException		 // thrown if we iterate too far through elements()
				| NullPointerException e) {	 // thrown if e.g. there is no candidate object
			logger.error("exception thrown while extracting givenName, familyName, email, phoneNumber from view");  // swallow the exception and return with whatever we managed to get
		}
	}

	// This endpoint gets hit whenever one of our assessments is created, or updated, by a user (or automatically).
	// Its probably the most complex code in the app, kind of a state machine driven from the state of the remote assessment and its view (as
	// fetched via API) and our local version stored in our database
	// TODO handle customer cancelling the assessment
	@RequestMapping(value = "/t/{tenant}/tas/devs/tas/assessments/byID/{id}/tenantDeltaPings", method = RequestMethod.POST)
	public String deltaPings(@PathVariable String tenant,
			@PathVariable long id) throws JsonParseException, JsonMappingException, IOException {

		Account account = accountRepo.findByTenant(tenant); 		// get account details from local db
		if (account == null)
			logH2Message();

		// get assessment details via API call to GET /assessments/byID/{id}
		com.aotal.gauge.boilerplate.api.pojos.Assessment remote = restTemplate.exchange(outBase + "/t/" + tenant + "/devs/tas/assessments/byID/" + id,
					HttpMethod.GET, null, com.aotal.gauge.boilerplate.api.pojos.Assessment.class).getBody(); 
		 
		//	switch, based on the assessment's status
		if (remote.status.equals("Started")) {

			// either (a) this is a new assessment, or (b) the user is restarting an existing one after clearing the error condition (insufficient credits)
			Assessment local = assessmentRepo.findByAssessmentID(id);
			
			if (local == null) { 	// must be a new one, create a local copy, with all non-null assessment details (except for status), along with some random addition problems for the candidate 
				Random randy = new Random();
				local = new Assessment(tenant, id, remote.view,
						randy.nextInt(100), // random number between 0 and 99
						randy.nextInt(100),
						randy.nextInt(100),
						randy.nextInt(100),
						randy.nextInt(100),
						randy.nextInt(100),
						randy.nextInt(100),
						randy.nextInt(100));

				loadViewFields(local);	// now attach as many fields as possible from the view

				// now we have enough data to work out what the status should be
				if (account.creditsRemaining <= 0 || ! local.viewFieldsOK()) {
					local.status = "Error";
					assessmentRepo.save(local);
					patchRemoteAssessment(local);
				} else  { // no error conditions
					local.status = "In progress";
					assessmentRepo.save(local);
					patchRemoteAssessment(local);
				}
				
			} else { // its an existing one - so presumably our local copy must be in Error, and the assessment has been recently restarted by the customer  
				
				if (local.status.equals("Error")) {
					// see if all error conditions have now been cleared
					if (account.creditsRemaining <= 0)
						logger.info("user tried to restart an Error-ed assessment, but there are not enough credits");  // remain in Error
					else {
						if (local.viewFieldsOK())
							// these must have already been OK, so we can restart the assessment
							local.status = "In progress";
						else {
							// when we last fetched the view, some fields were missing - but perhaps the customer has now fixed the view?
							loadViewFields(local);	// API call to load view fields again
							if (local.viewFieldsOK())
								local.status = "In progress";		// sweet, things are now all go
							else
								logger.info("user tried to restart an Error-ed assessment, but there are missing view fields");  // remain in Error
								;  // some view fields still missing - remain in Error 
						}
					}
					assessmentRepo.save(local);
					patchRemoteAssessment(local);
					
				} else
					logger.error("user restarted an assessment, but strangely, our local copy was not in Error state");
			}
			
		} else
			logger.error("unhandled status " + remote.status);

		return "done!";
	}
	
}

