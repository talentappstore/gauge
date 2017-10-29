package com.aotal.gauge.jpa;

import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.UniqueConstraint;

@Entity
public class Account {

    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
    private String tenant;
    private int creditsRemaining;
    private int creditsUsed;
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public int getCreditsRemaining() {
		return creditsRemaining;
	}
	public void setCreditsRemaining(int creditsRemaining) {
		this.creditsRemaining = creditsRemaining;
	}
	public int getCreditsUsed() {
		return creditsUsed;
	}
	public void setCreditsUsed(int creditsUsed) {
		this.creditsUsed = creditsUsed;
	}
	
	public Account(String tenant, int creditsRemaining, int creditsUsed) {
		super();
		this.tenant = tenant;
		this.creditsRemaining = creditsRemaining;
		this.creditsUsed = creditsUsed;
	}

	public Account() {
		super();
	}
	
	@Override
    public String toString() {
        return String.format(
                "Assessment[tenant=%s, creditsRemaining=%d, creditsUsed='%s', for %s %s]",
                tenant, creditsRemaining, creditsUsed);
    }

}