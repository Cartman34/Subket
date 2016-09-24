package com.sowapps.subket.demo;

import com.sowapps.subket.peer.SubketResponse;

/**
 * Subket callback implementation for standard demo
 * 
 * @author Florent HAZARD
 *
 */
public class SubketCallback extends SubketResponse {

	@Override
	public void run() {
		System.out.println("[DEMO] Recv "+getData().length+" bytes ==>");
		System.out.println(new String(getData()));
		System.out.println("[DEMO] End of reception");
	}

}
