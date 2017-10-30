package com.aotal.gauge;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.aotal.gauge.jpa.Account;
import com.aotal.gauge.jpa.AccountRepository;

@SpringBootApplication
public class GaugeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaugeApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
//	    return new RestTemplate();
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory()); // special incantation needed for PATCH

	}
	

}
