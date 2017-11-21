package com.aotal.gauge.endpoints;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import com.aotal.gauge.endpoints.api.CoreOutAPIController;
import com.aotal.gauge.jpa.AccountRepository;
import com.aotal.gauge.jpa.Assessment;
import com.aotal.gauge.jpa.AssessmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Supertype for our TAS endpoint controllers that:
 * - checks that the incoming tazzy-secret key matches our own secret key
 * - injects commonly used beans
 * - logs basic details of incoming API call (not the request though - oh no, Spring) 
 * - provides commonly used helpers for its subtypes 
 * 
 * @author abraae
 *
 */
public class TASController {

	@Autowired
	protected AssessmentRepository assessmentRepo;
	@Autowired
	protected AccountRepository accountRepo;
	@Autowired
	protected RestTemplate restTemplate;
	@Autowired
	protected Environment env;
	@Autowired
	protected String inBase;
	@Autowired
	protected String outBase;

	private static final Logger logger = LoggerFactory.getLogger(TASController.class);
	
    // CRITICAL: for every endpoint in this controller, check that the incoming tazzy-secret matches our secret key 
	// TODO: move to a filter instead
	@ModelAttribute
	private void verify(@RequestHeader("tazzy-secret") String secret) {
		if (! secret.equals(env.getProperty("tas.secret"))) throw new UnauthorizedException();
	}

	// for every endpoint in this controller, log incoming API call details
	// TODO: move to a filter instead
	@ModelAttribute
	private void logIncomingCall(HttpServletRequest request) throws IOException {
	    logger.info("> " + request.getMethod() + " " + request.getRequestURL().toString()
	    										+ (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));
	}

	// PATCH the remote assessment to reflect our new local reality
	protected void patchRemoteAssessment(Assessment local) {

		// depending on the assessment's new status, we may/may not offer links to candidate, user, and may/may not show the score image 
		String candidateUrl = null;
		String imageUrl = null;
		String userUrl = null;
		if (local.status.equals("In progress")) {
			candidateUrl = inBase + "/quiz/" + local.accessKey;
			userUrl = inBase + "/t/" + local.tenant + "/underway/" + local.accessKey;
			
		} else if (local.status.equals("Error")) {
			userUrl = inBase + "/t/" + local.tenant + "/somethingsWrong/" + local.accessKey;
			
		} else if (local.status.equals("Complete")) {
			candidateUrl = inBase + "/quiz/" + local.accessKey; // candidate can still see their own results
			userUrl = inBase + "/t/" + local.tenant + "/quizResultUser/" + local.accessKey;
			imageUrl = env.getProperty("imageServer") + "/scoreWithIcon.png?score=" + local.score + "&label=GA";
		}
 
		com.aotal.gauge.endpoints.api.pojos.Assessment remote = new com.aotal.gauge.endpoints.api.pojos.Assessment();
		remote.status = local.status;
		if (imageUrl != null)
			remote.image = imageUrl;
		{
			com.aotal.gauge.endpoints.api.pojos.Assessment.URISet uriSet = new com.aotal.gauge.endpoints.api.pojos.Assessment.URISet();
			uriSet.candidateInteractionUri = candidateUrl;
			uriSet.userInteractionUri = userUrl;
			uriSet.userAttentionRequired = false; // this assessment never needs user input (except on Error - in which case the hub will show the red triangle itself)
			remote.interactionUris = uriSet;
		}
		remote.view = local.view; // carry across (TODO - should be ignored anyway by the PATCH ?)

		// now PATCH the assessment. If we've changed the candidate url, this will cause an email to be sent to the candidate
		String url = outBase + "/t/" + local.tenant + "/devs/tas/assessments/byID/" + local.assessmentID + "/appDetails";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "merge-patch+json"));
		HttpEntity<com.aotal.gauge.endpoints.api.pojos.Assessment> entity = new HttpEntity<com.aotal.gauge.endpoints.api.pojos.Assessment>(remote, headers);
		ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
	}
	
}
