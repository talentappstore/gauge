package com.aotal.gauge.endpoints;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interceptor that:
 * - adds tazzy-secret requestheader (with our secret key) to all outgoing API calls
 * - logs all outgoing call details
 * 
 * @author abraae
 *
 */
public class SecretKeyInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(SecretKeyInterceptor.class);
	
	private Environment env;
	
	public SecretKeyInterceptor(Environment env) {
		this.env = env;
	}
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		// attach secret key
		HttpHeaders headers = request.getHeaders();
		headers.add("tazzy-secret", env.getProperty("tas.secret"));
		
		// log outgoing request details
		String log = "< " + request.getMethod() + " " + request.getURI();
		if (body.length > 0) {
			ObjectMapper mapper = new ObjectMapper();
			String bodyString = new String(body);
			Object json = mapper.readValue(bodyString, Object.class);
			log += "\nRequest:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);			
		}
		logger.info(log);

		return execution.execute(request, body);
	}
}
