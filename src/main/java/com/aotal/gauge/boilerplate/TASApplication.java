package com.aotal.gauge.boilerplate;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class TASApplication {

	private static final Logger logger = LoggerFactory.getLogger(TASApplication.class);
	
    @Autowired
    private Environment env;
	
	@Bean
	public RestTemplate restTemplate() {
		HttpComponentsClientHttpRequestFactory hchrf = new HttpComponentsClientHttpRequestFactory();  // special incantation needed for Spring + PATCH :(

		// we want the option to use the charles proxy, so we can examine details about outgoing API calls (i.e. tenant and core-in API calls).
		// In theory it should be possible to just add these JVM params:
		//  -Dhttp.proxyPort=8888 -Dhttp.proxyHost=127.0.0.1
		// but in my eclipse, this fails with IOErrors, and it is order dependant on whether the port of host comes first. That's some bad shit. But at
		// least, configuring proxy settings at this level seems to work. wtf eclipse.
		String proxyHost = env.getProperty("tas.proxyhost");
		String proxyPortString = env.getProperty("tas.proxyport");
		Integer proxyPort = proxyPortString != null ? new Integer(proxyPortString) : null;
		if (proxyHost != null && proxyPort != null) {
			logger.info("proxying is enabled: outgoing requests are being proxied through " + proxyHost + ":" + proxyPort);
			
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			CloseableHttpClient httpClient = HttpClients.custom().setProxy(proxy).build();
			hchrf.setHttpClient(httpClient);
		}
		
		RestTemplate rt = new RestTemplate(hchrf); 
		// always attach the secret key as a request header
		rt.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body,
					ClientHttpRequestExecution execution) throws IOException {
				HttpHeaders headers = request.getHeaders();
				headers.add("tazzy-secret", env.getProperty("tas.secret"));
				return execution.execute(request, body);
			}
		}));

		return rt;
	}
	
	// build up url base (i.e. pages within our app, sitting behind tazzy), for use when generating links
	@Bean
	public String inBase() {
		return "https://" + env.getProperty("tas.app") + ".communityapps.talentappstore.com";
	}

	// build up url base for outgoing TAS API calls
	// TODO move to properties file, needs tweaking when using the proxy (cbf getting their certs working) 
	@Bean
	public String outBase() {
		return "http://" + env.getProperty("tas.app") + ".tazzy.io";
	}

}
