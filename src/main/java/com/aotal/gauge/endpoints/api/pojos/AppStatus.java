package com.aotal.gauge.endpoints.api.pojos;

public class AppStatus {

	public String landingPage;
	public String settingsPage;
	public boolean setupRequired;

	public AppStatus() {
		// TODO Auto-generated constructor stub
	}

	public AppStatus(String landingPage, String settingsPage, boolean setupRequired) {
		super();
		this.landingPage = landingPage;
		this.settingsPage = settingsPage;
		this.setupRequired = setupRequired;
	}

	
}
