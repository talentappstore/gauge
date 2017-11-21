package com.aotal.gauge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.aotal.gauge.boilerplate.TASApplication;

@SpringBootApplication
public class GaugeApplication extends TASApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaugeApplication.class, args);
	}

	private static final Logger logger = LoggerFactory.getLogger(GaugeApplication.class);
}
