package com.aotal.gauge;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Interceptor that automatically adds tazzy-secret requestheader (with our secret key) to all outgoing API calls
 * 
 * @author abraae
 *
 */
public class SecretKeyInterceptor implements ClientHttpRequestInterceptor {

	private Environment env;
	
	public SecretKeyInterceptor(Environment env) {
		this.env = env;
	}
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		HttpHeaders headers = request.getHeaders();
		headers.add("tazzy-secret", env.getProperty("tas.secret"));
		
		return execution.execute(request, body);
	}
}
