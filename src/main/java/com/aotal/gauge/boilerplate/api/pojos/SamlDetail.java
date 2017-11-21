package com.aotal.gauge.boilerplate.api.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SamlDetail {

@JsonProperty("tas.personal.email")
public String email;

public String nameID;

public String entityID;

@JsonProperty("tas.personal.givenName")
public String givenName;

@JsonProperty("tas.personal.familyName")
public String familyName;

@JsonProperty("tas.personal.image")
public String image;

//"tas.roles":["recruiter","admin"]


}
