package com.sowapps.subket.demo;

import com.sowapps.subket.peer.SubketPeer;

/**
 * Subket peer client implementation for standard demo
 * 
 * @author Florent HAZARD
 *
 */
public class SubketClient {
	
	public static void main(String[] args) {
		System.out.println("Running SubketClient");
		SubketPeer pair = new SubketPeer(1, "127.0.0.1", SubketPeer.defaultPort, false, SubketCallback.class);
		try {
			new Thread(pair).start();
		} catch (Exception e) {
			System.out.println("Error while running Subket Client");
			e.printStackTrace();
		}
	}
}
