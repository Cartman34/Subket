package com.sowapps.subket.demo;

import com.sowapps.subket.pair.SubketResponse;

public class SubketObjectCallback extends SubketResponse {

	@Override
	public void run() {
		try {
			TestObject to = (TestObject) unserialize();
			System.out.println("Received TestObject: "+to);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
