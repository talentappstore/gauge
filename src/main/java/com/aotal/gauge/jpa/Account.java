package com.aotal.gauge.jpa;

import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.UniqueConstraint;

/**
 * The details about this tenant - e.g.. how many credits do they have remaining
 * 
 * @author abraae
 *
 */
@Entity
public class Account {

    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
    public String tenant;
    public int creditsRemaining;
    public int creditsUsed;
	
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