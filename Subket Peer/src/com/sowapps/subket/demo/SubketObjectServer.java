package com.sowapps.subket.demo;

import com.sowapps.subket.peer.SubketPeer;
import com.sowapps.subket.peer.SubketResponse;

/**
 * Subket peer server implementation for object demo
 * 
 * @author Florent HAZARD
 *
 */
public class SubketObjectServer extends SubketPeer {

	public SubketObjectServer(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		super(appKey, subketHost, subketPort, isServer, respClass);
	}
	
//	@Override
//	public InputStream convertInputStream(InputStream s) throws Exception {
//		return new ObjectInputStream(s);
//	}

	public static void main(String[] args) {
		System.out.println("Running SubketObjectServer");
		SubketPeer pair = new SubketObjectServer(1, "127.0.0.1", SubketPeer.defaultPort, true, SubketObjectCallback.class);
		try {
			new Thread(pair).start();
			System.out.println("[Server] Pair Started");
			while( !pair.isConnected() ) {
				Thread.sleep(100);
			}
			TestObject data = new TestObject();
//			System.out.println("Sending some data");
			pair.send(data);
		} catch (Exception e) {
			System.out.println("Error while running Subket Server");
			e.printStackTrace();
		}
	}
}
