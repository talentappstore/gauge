package com.aotal.gauge.endpoints.api;

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
 * Serve up core out APIs
 * 
 * @author abraae
 *
 */
@RestController
public class CoreOutAPIController extends GaugeController {

	private static final Logger logger = LoggerFactory.getLogger(CoreOutAPIController.class);

	// create account in local db
	@RequestMapping(value = "/tas/core/tenants", method = RequestMethod.POST)
	public void createTenant(@RequestBody Tenant tenant) {

		// create an account in our database, with 0 credits
		logger.info("inserting account for tenant " + tenant);
		accountRepo.save(new Account(tenant.shortCode, 0,  0));
	}

	// delete account & associated assessments from local db
	@RequestMapping(value = "/tas/core/tenants/{tenant}", method = RequestMethod.DELETE)
	public void deleteTenant(@PathVariable String tenant) {

		// delete the account and all linked assessments in our database
		logger.info("deleting account for tenant " + tenant);
		Account account = accountRepo.findByTenant(tenant);
		if (account == null)
			TenantAPIController.logH2Message();
		
		// TODO: delete assessments as well 
		accountRepo.delete(account);
	}

}

