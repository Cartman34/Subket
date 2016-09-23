package com.sowapps.subket.demo;

import com.sowapps.subket.pair.SubketPair;
import com.sowapps.subket.pair.SubketResponse;

// COULD NOT WORK DUE TO THE NON-GENERICITY OF run()
public class SubketObjectServer extends SubketPair {

	public SubketObjectServer(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		super(appKey, subketHost, subketPort, isServer, respClass);
	}
	
//	@Override
//	public InputStream convertInputStream(InputStream s) throws Exception {
//		return new ObjectInputStream(s);
//	}

	public static void main(String[] args) {
		System.out.println("Running SubketObjectServer");
		SubketPair pair = new SubketObjectServer(1, "127.0.0.1", SubketPair.defaultPort, true, SubketObjectCallback.class);
		try {
			new Thread(pair).start();
			System.out.println("[Server] Pair Started");
			TestObject data = new TestObject();
			while( !pair.isConnected() ) {
				Thread.sleep(100);
			}
//			System.out.println("Sending some data");
			pair.send(data);
		} catch (Exception e) {
			System.out.println("Error while running Subket Server");
			e.printStackTrace();
		}
	}
}
