package com.aotal.gauge.jpa;

import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

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
    @NotNull public Long accessKey;
    @NotNull public  String tenant;
    @NotNull public  Long assessmentID;
    @NotNull public  String status;
    @NotNull public  String view;
    
    // the following may be null in the event of the view (setup by the customer) not containing them - but in this case the
    // status would be Error. A diligent coder would slap a database constraint on to enforce this (also that the score is present when status is Complete)
    public  String givenName;
    public  String familyName;
    public  String email;
    public  String phoneNumber;
    
    // the operands of each of the 4 equations the candidate will be shown
    public int q1a;
    public int q1b;
    public int q2a;
    public int q2b;
    public int q3a;
    public int q3b;
    public int q4a;
    public int q4b;

    // score
    public Integer score;
    
	protected Assessment() {}

	// see if we have all the mandatory fields from the view that we need
	public boolean viewFieldsOK() {
		return givenName != null && familyName != null && email != null && phoneNumber != null;
	}
	
	// create a local Assessment, with all non-null fields 
	public Assessment(String tenant, Long assessmentID, String view,
			int q1a, int q1b, int q2a, int q2b, int q3a, int q3b, int q4a, int q4b) {
		
		super();
		
		this.accessKey = randy.nextLong();
		this.tenant = tenant;
		this.assessmentID = assessmentID;
		this.view = view;
		
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