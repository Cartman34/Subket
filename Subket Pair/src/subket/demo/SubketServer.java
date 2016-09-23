package subket.demo;

import java.util.Scanner;

import subket.pair.SubketPair;

public class SubketServer {

	public static void main(String[] args) {
		System.out.println("Running SubketServer");
		SubketPair pair = new SubketPair(1, "127.0.0.1", SubketPair.defaultPort, true, SubketCallback.class);
		try {
			new Thread(pair).start();
			System.out.println("[Server] Pair Started");
			String data = "We are entering a test session !";
			while( !pair.isConnected() ) {
				Thread.sleep(100);
			}
			System.out.println("Sending some data");
			pair.send(data.getBytes());
			System.out.println("Enter some text:");
			Scanner sc	= new Scanner(System.in);
//			String s	= "";
			data		= "";
			while( !data.equals("exit") ) {
				data	= sc.nextLine();
				pair.send(data.getBytes());
			}
			pair.send("exit");
			sc.close();
		} catch (Exception e) {
			System.out.println("Error while running Subket Server");
			e.printStackTrace();
		}
	}
}
