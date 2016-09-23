package subket.demo;

import subket.pair.SubketResponse;

public class SubketCallback extends SubketResponse {

	@Override
	public void run() {
		System.out.println("[DEMO] Recv "+getData().length+" bytes ==>");
		System.out.println(new String(getData()));
		System.out.println("[DEMO] End of reception");
	}

}
