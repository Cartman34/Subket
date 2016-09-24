package com.sowapps.subket.demo;

import java.io.Serializable;

/**
 * Test object that will be sent through the subket hub 
 * 
 * @author Florent HAZARD
 *
 */
public class TestObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private int a;
	public String b;
	
	/**
	 * Constructor
	 */
	public TestObject() {
		a = 245;
		b = "This is ok !";
	}
	
	@Override
	public String toString() {
		return a+" / "+b;
	}
}
