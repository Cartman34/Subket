package com.sowapps.subket.demo;

import java.io.Serializable;

public class TestObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private int a;
	public String b;
	
	public TestObject() {
		a = 245;
		b = "This is ok !";
	}
	
	@Override
	public String toString() {
		return a+" / "+b;
	}
}
