package com.sowapps.subket.hub;

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Declare a specific application to the subket, each application has its own client and server peers.
 * 
 * @author Florent HAZARD
 *
 */
public class SubketApplication {
	
	/**
	 * List of waiting clients
	 */
	protected LinkedList<Socket> waitingClients;
	
	/**
	 * List of waiting servers
	 */
	protected LinkedList<Socket> waitingServers;
	
	/**
	 * List of current proxies
	 */
	protected ArrayList<SubketProxy> proxies;
	
	/**
	 * Constructor
	 */
	public SubketApplication() {
		waitingClients	 = new LinkedList<Socket>();
		waitingServers	 = new LinkedList<Socket>();
		proxies			 = new ArrayList<SubketProxy>();
	}
	
	/**
	 * Connect socket to the application, client to server, server to client and if pear available, the socket waits for a new one.
	 * It creates a new proxy to communicate between peers.
	 * 
	 * @param connecting
	 * @param isServer
	 * @throws Exception
	 */
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
	
	/**
	 * Connect a client to the application
	 * 
	 * @param client
	 * @throws Exception
	 */
	public void connectClient(Socket client) throws Exception {
		connect(client, false);
	}
	
	/**
	 * Connect a server to the application
	 * 
	 * @param server
	 * @throws Exception
	 */
	public void connectServer(Socket server) throws Exception {
		connect(server, true);
	}
	
	/**
	 * Disconnect the given proxy
	 * 
	 * @param proxy
	 */
	public void disconnect(SubketProxy proxy) {
		if( !proxies.contains(proxy) ) {
			return;// Avoid infinite loop
		}
		System.out.println("Disconnecting Proxy from Application");
		proxies.remove(proxy);// Before closing proxy
		proxy.close();//If closed, do nothing
	}
	
	/**
	 * Test if the given socket is available
	 * 
	 * @param socket
	 * @return
	 * @throws Exception
	 */
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
}
