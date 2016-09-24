package com.sowapps.subket.demo;

import com.sowapps.subket.peer.SubketPeer;
import com.sowapps.subket.peer.SubketResponse;

/**
 * Subket peer client implementation for object demo
 * 
 * @author Florent HAZARD
 *
 */
public class SubketObjectClient extends SubketPeer {

	public SubketObjectClient(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		super(appKey, subketHost, subketPort, isServer, respClass);
	}

	public static void main(String[] args) {
		System.out.println("Running SubketObjectClient");
		SubketPeer pair = new SubketObjectClient(2, "127.0.0.1", SubketPeer.defaultPort, false, SubketObjectCallback.class);
		try {
			new Thread(pair).start();
		} catch (Exception e) {
			System.out.println("Error while running Subket Client");
			e.printStackTrace();
		}
	}
}
