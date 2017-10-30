package com.aotal.gauge.api;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * helper methods e.g. to add the tazzy secret key as a request header when making API requests
 * @author abraae
 *
 */
public class Helpers {

	// build up url string of our app, sitting behind tazzy, for use when generating links
	public static String getAppBase(Environment env) {
		return "https://" + env.getProperty("tas.app") + ".communityapps.talentappstore.com";
	}

	public static HttpEntity<String> entityWithSecret(Environment env, String body, MediaType contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", env.getProperty("tas.secret"));  // attaching the "tazzy-secret" request header
		headers.setContentType(contentType);
		return new HttpEntity<String>(body, headers);
	}

	public static HttpEntity entityWithSecret(Environment env) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("tazzy-secret", env.getProperty("tas.secret"));  // attaching the "tazzy-secret" request header
		return new HttpEntity(headers);
	}


}
