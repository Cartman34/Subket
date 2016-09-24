package com.sowapps.subket.demo;

import java.util.Scanner;

import com.sowapps.subket.peer.SubketPeer;

/**
 * Subket peer server implementation for standard demo
 * 
 * @author Florent HAZARD
 *
 */
public class SubketServer {
	
	public static void main(String[] args) {
		System.out.println("Running SubketServer");
		SubketPeer pair = new SubketPeer(1, "127.0.0.1", SubketPeer.defaultPort, true, SubketCallback.class);
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
