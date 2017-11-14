package com.aotal.gauge;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;

@SpringBootApplication
public class GaugeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaugeApplication.class, args);
	}

    @Autowired
    private Environment env;
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory()); // special incantation needed for PATCH
		rt.setInterceptors(Collections.singletonList(new SecretKeyInterceptor(env)));  // always attach the secret key as a request header
		return rt;
	}

	// build up url string of our app, sitting behind tazzy, for use when generating links
	@Bean
	public String appBase() {
		return "https://" + env.getProperty("tas.app") + ".communityapps.talentappstore.com";
	}
	
}
