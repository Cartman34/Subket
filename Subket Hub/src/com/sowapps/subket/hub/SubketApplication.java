package com.sowapps.subket.hub;

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class SubketApplication {

	protected LinkedList<Socket> waitingClients;
	protected LinkedList<Socket> waitingServers;
	protected ArrayList<SubketProxy> proxies;
	
	public SubketApplication() {
		waitingClients	 = new LinkedList<Socket>();
		waitingServers	 = new LinkedList<Socket>();
		proxies			 = new ArrayList<SubketProxy>();
	}
	
	public void disconnect(SubketProxy proxy) {
		if( !proxies.contains(proxy) ) {
			return;// Avoid infinite loop
		}
		System.out.println("Disconnecting Proxy from Application");
		proxies.remove(proxy);// Before closing proxy
		proxy.close();//If closed, do nothing
	}
	
	public boolean testSocket(Socket socket) throws Exception {
		if( socket.isClosed() ) { return false; }
		try {
			socket.getInputStream().read();
		} catch( Exception e ) {
//			e.printStackTrace();
			socket.close();
			return false;
		}
		return true;
	}
	
	public void connect(Socket connecting, boolean isServer) throws Exception {
		if( connecting.isClosed() || !testSocket(connecting) ) {
			throw new Exception("Connecting is closed");
		}
		String connectingStr	= isServer ? "server" : "client";
		String otherStr			= isServer ? "client" : "server";
		LinkedList<Socket> waitingConnecting	= isServer ? waitingServers : waitingClients;
		LinkedList<Socket> waitingOthers		= isServer ? waitingClients : waitingServers; 
		// If one server found, we use it
		Socket other;
		while( (other = waitingOthers.poll()) != null ) {
			if( testSocket(other) ) {
				System.out.println("[SubketApp] Connecting to an existing "+otherStr);
				proxies.add(new SubketProxy(this, isServer ? other : connecting, isServer ? connecting : other));
				return;
			}
			System.out.println("[SubketApp] One available "+otherStr+" is now disconnected");
		}
		// We used the waiting byte, so with signal NOT OK
		connecting.getOutputStream().write(SubketProxy.SIGNAL_NOT);
		// Adding this client to the waiting ones
		System.out.println("[SubketApp] Add to waiting "+connectingStr+"s");
		waitingConnecting.add(connecting);
	}
	
	public void connectClient(Socket client) throws Exception {
		connect(client, false);
//		if( client.isClosed() || !testSocket(client) ) {
//			throw new Exception("client closed");
//		}
//		// If one server found, we use it
//		Socket server;
//		while( (server = waitingServers.poll()) != null ) {
//			if( testSocket(server) ) {
//				System.out.println("[SubketApp] Connecting to an existing server");
//				proxies.add(new SubketProxy(this, client, server));
//				return;
//			}
//			System.out.println("[SubketApp] One available server is now disconnected");
//		}
//		// We used the waiting byte, so with signal NOT OK
//		client.getOutputStream().write(SubketProxy.SIGNAL_NOT);
//		// Adding this client to the waiting ones
//		System.out.println("[SubketApp] Add to waiting clients");
//		waitingClients.add(client);
	}
	
	public void connectServer(Socket server) throws Exception {
		connect(server, true);
//		if( server.isClosed() ) {
//			throw new Exception("server closed");
//		}
//		// If one client found, we use it
//		Socket client;
//		while( (client = waitingClients.poll()) != null ) {
//			if( testSocket(client) ) {
//				System.out.println("[SubketApp] Connecting to an existing client");
//				proxies.add(new SubketProxy(this, client, server));
//				return;
//			}
//			System.out.println("[SubketApp] One available client is now disconnected");
//		}
//		// Adding this server to the waiting ones
//		System.out.println("[SubketApp] Add to waiting servers");
//		waitingServers.add(server);
	}
}
