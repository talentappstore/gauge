package com.aotal.gauge.endpoints.web;

/**
 * Backing object to hold form field values
 * 
 * @author abraae
 *
 */
public class QuizForm {

    private Long key;
    
    // the operands of each of the 4 equations the candidate will be shown
    public int q1a;
    public int q1b;
    public int q2a;
    public int q2b;
    public int q3a;
    public int q3b;
    public int q4a;
    public int q4b;

    // candidate answers
    public int a1;
    public int a2;
    public int a3;
    public int a4;

	public QuizForm(Long key, int q1a, int q1b, int q2a, int q2b, int q3a, int q3b, int q4a, int q4b) {
		super();
		this.key = key;
		this.q1a = q1a;
		this.q1b = q1b;
		this.q2a = q2a;
		this.q2b = q2b;
		this.q3a = q3a;
		this.q3b = q3b;
		this.q4a = q4a;
		this.q4b = q4b;
	}

	
}
