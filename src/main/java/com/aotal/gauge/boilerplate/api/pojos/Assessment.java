package com.aotal.gauge.boilerplate.api.pojos;

public class Assessment {
	public String status;
	public String image;
	public URISet interactionUris;
	
	public static class URISet {
		public String candidateInteractionUri;
		public String userInteractionUri;
		public boolean userAttentionRequired;
	}
	
	public String view;
}

