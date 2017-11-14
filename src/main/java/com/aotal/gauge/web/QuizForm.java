package com.aotal.gauge.web;

/**
 * Backing object to hold form field values
 * 
 * @author abraae
 *
 */
public class QuizForm {

    private Long key;
    
    // the operands of each of the 4 equations the candidate will be shown
    int q1a;
    int q1b;
    int q2a;
    int q2b;
    int q3a;
    int q3b;
    int q4a;
    int q4b;

    // candidate answers
    int a1;
    int a2;
    int a3;
    int a4;


	public int getQ1a() {
		return q1a;
	}

	public void setQ1a(int q1a) {
		this.q1a = q1a;
	}

	public int getQ1b() {
		return q1b;
	}

	public void setQ1b(int q1b) {
		this.q1b = q1b;
	}

	public int getQ2a() {
		return q2a;
	}

	public void setQ2a(int q2a) {
		this.q2a = q2a;
	}

	public int getQ2b() {
		return q2b;
	}

	public void setQ2b(int q2b) {
		this.q2b = q2b;
	}

	public int getQ3a() {
		return q3a;
	}

	public void setQ3a(int q3a) {
		this.q3a = q3a;
	}

	public int getQ3b() {
		return q3b;
	}

	public void setQ3b(int q3b) {
		this.q3b = q3b;
	}

	public int getQ4a() {
		return q4a;
	}

	public void setQ4a(int q4a) {
		this.q4a = q4a;
	}

	public int getQ4b() {
		return q4b;
	}

	public void setQ4b(int q4b) {
		this.q4b = q4b;
	}

	public int getA1() {
		return a1;
	}

	public void setA1(int a1) {
		this.a1 = a1;
	}

	public int getA2() {
		return a2;
	}

	public void setA2(int a2) {
		this.a2 = a2;
	}

	public int getA3() {
		return a3;
	}

	public void setA3(int a3) {
		this.a3 = a3;
	}

	public int getA4() {
		return a4;
	}

	public void setA4(int a4) {
		this.a4 = a4;
	}

	public QuizForm() {
		// TODO Auto-generated constructor stub
	}

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
