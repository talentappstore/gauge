package com.aotal.gauge.jpa;

import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.UniqueConstraint;

/**
 * Our local copy of an assessment, hopefully mostly in sync with the remote master held by the assessment hub
 * 
 * @author abraae
 *
 */
@Entity
public class Assessment {

	static final Random randy = new Random();
    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long key;
    private String tenant;
    private Long assessmentID;
    private String status;
    private String givenName;
    private String familyName;
    private String phoneNumber;
    
    // the operands of each of the 4 equations the candidate will be shown
    int q1a;
    int q1b;
    int q2a;
    int q2b;
    int q3a;
    int q3b;
    int q4a;
    int q4b;

    // score
    int score;
    
    public Long getKey() {
		return key;
	}


	public int getQ1a() {
		return q1a;
	}


	public int getQ1b() {
		return q1b;
	}


	public int getQ2a() {
		return q2a;
	}


	public int getQ2b() {
		return q2b;
	}


	public int getQ3a() {
		return q3a;
	}


	public int getQ3b() {
		return q3b;
	}


	public int getQ4a() {
		return q4a;
	}


	public int getQ4b() {
		return q4b;
	}


	public int getScore() {
		return score;
	}


	public void setScore(int score) {
		this.score = score;
	}

	

	public String getTenant() {
		return tenant;
	}


	public void setTenant(String tenant) {
		this.tenant = tenant;
	}


	public Long getAssessmentID() {
		return assessmentID;
	}


	public void setAssessmentID(Long assessmentID) {
		this.assessmentID = assessmentID;
	}

	

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	protected Assessment() {}


	public Assessment(String tenant, Long assessmentID, String status, String givenName, String familyName, String phoneNumber,
			int q1a, int q1b, int q2a, int q2b, int q3a, int q3b, int q4a, int q4b) {
		super();
		this.key = randy.nextLong();
		this.tenant = tenant;
		this.assessmentID = assessmentID;
		this.status = status;
		this.givenName = givenName;
		this.familyName = familyName;
		this.phoneNumber = phoneNumber;
		this.q1a = q1a;
		this.q1b = q1b;
		this.q2a = q2a;
		this.q2b = q2b;
		this.q3a = q3a;
		this.q3b = q3b;
		this.q4a = q4a;
		this.q4b = q4b;
	}


	@Override
    public String toString() {
        return String.format(
                "Assessment[tenant=%s, id=%d, status='%s', for %s %s]",
                tenant, assessmentID, status, givenName, familyName);
    }

}