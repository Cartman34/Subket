package subket.demo;

import subket.pair.SubketPair;
import subket.pair.SubketResponse;

//COULD NOT WORK DUE TO THE NON-GENERICITY OF run()
public class SubketObjectClient extends SubketPair {

	public SubketObjectClient(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		super(appKey, subketHost, subketPort, isServer, respClass);
	}

	public static void main(String[] args) {
		System.out.println("Running SubketObjectClient");
		SubketPair pair = new SubketObjectClient(1, "127.0.0.1", SubketPair.defaultPort, false, SubketObjectCallback.class);
		try {
			new Thread(pair).start();
		} catch (Exception e) {
			System.out.println("Error while running Subket Client");
			e.printStackTrace();
		}
	}
}
