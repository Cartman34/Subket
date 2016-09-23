package subket.demo;

import subket.pair.SubketPair;

public class SubketClient {

	public static void main(String[] args) {
		System.out.println("Running SubketClient");
		SubketPair pair = new SubketPair(1, "127.0.0.1", SubketPair.defaultPort, false, SubketCallback.class);
		try {
			new Thread(pair).start();
		} catch (Exception e) {
			System.out.println("Error while running Subket Client");
			e.printStackTrace();
		}
	}
}
