package com.sowapps.subket.demo;

import com.sowapps.subket.peer.SubketResponse;

/**
 * Subket callback implementation for object demo
 * 
 * @author Florent HAZARD
 *
 */
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
