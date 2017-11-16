package com.aotal.gauge.endpoints.api.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SamlDetail {

@JsonProperty("tas.personal.email")
private String email;

private String nameID;

private String entityID;

@JsonProperty("tas.personal.givenName")
private String givenName;

@JsonProperty("tas.personal.familyName")
private String familyName;

@JsonProperty("tas.personal.image")
private String image;

//"tas.roles":["recruiter","admin"]

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getNameID() {
	return nameID;
}

public void setNameID(String nameID) {
	this.nameID = nameID;
}

public String getEntityID() {
	return entityID;
}

public void setEntityID(String entityID) {
	this.entityID = entityID;
}

public String getGivenName() {
	return givenName;
}

public void setGivenName(String givenName) {
	this.givenName = givenName;
}

public String getFamilyName() {
	return familyName;
}

public void setFamilyName(String familyName) {
	this.familyName = familyName;
}

public String getImage() {
	return image;
}

public void setImage(String image) {
	this.image = image;
}


}
