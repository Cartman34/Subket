package com.sowapps.subket.hub;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Subket {
	
	public static final int defaultPort = 2013;

	public static void main(String[] args) {
		Subket subket = new Subket(defaultPort);
		try {
			subket.run();
		} catch (Exception e) {
			System.out.println("Error while running Subket");
			e.printStackTrace();
		}
	}
	
	private int port;
	private ServerSocket adminSocket;
	private static HashMap<Integer, SubketApplication> apps;
	
	public static final int CLIENT = 1;
	public static final int SERVER = 2;
	
	public Subket(int port) {
		this.port = port;
		apps = new HashMap<Integer, SubketApplication>();
	}
	
	public void run() throws Exception {
		if( adminSocket != null ) {
			throw new Exception("Already running");
		}

		try {
			adminSocket = new ServerSocket(port);
		} catch (Exception e) {
		    System.out.println("Could not listen on port: "+port);
		    System.out.println(e.getMessage());
		    return;
		}
		while(true) {
		    Socket pairSocket = null;
			try {
				System.out.println("[Subket] Waiting for next Pair");
				pairSocket = adminSocket.accept();
				System.out.println("[Subket] Accepted connection from "+pairSocket.getInetAddress());
			    InputStream is = pairSocket.getInputStream();
			    byte[] appKeyBytes = new byte[4];
			    if( is.read(appKeyBytes) < 0 ) {
			    	throw new Exception("Unable to read App Key");
			    }
			    int appKey = ByteBuffer.wrap(appKeyBytes).getInt();
			    short pairTypeByte = (short) is.read();
			    if( pairTypeByte < 0 ) {
			    	throw new Exception("Unable to read Pair Type");
			    }
			    if( pairTypeByte != CLIENT && pairTypeByte != SERVER ) {
			    	throw new Exception("Unknown Pair Type");
			    }
			    SubketApplication app = apps.get(appKey);
			    if( app == null ) {
			    	app = new SubketApplication();
			    	apps.put(appKey, app);
			    }
			    System.out.println("[Subket] Connecting Pair for app key "+appKey);
			    if( pairTypeByte == CLIENT ) {
			    	app.connectClient(pairSocket);
			    } else {
			    	app.connectServer(pairSocket);
			    }
			    
			} catch (Exception e) {
			    System.out.println("Failed to create new pair");
			    System.out.println(e.getMessage());
			    // Close connection to client if an error occurred
			    if( pairSocket != null && !pairSocket.isClosed() ) {// && clientSocket.isConnected()
			    	pairSocket.close();
			    }
			    return;
			}
		}
	}

}
