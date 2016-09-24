package com.sowapps.subket.hub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Connect the client and the server, transmit all incoming data to the other
 * 
 * @author Florent HAZARD
 *
 */
public class SubketProxy implements Runnable {
	
	public final static byte SIGNAL_OK	= 1;
	public final static byte SIGNAL_NOT	= 0;
	
	public final static int BUFFER_SIZE	= 4092;//Formerly 1024, 4092 is the max buffer size for Java (or system)
	
	/**
	 * The application
	 */
	protected SubketApplication app;
	
	// Proxy don't need to know about which one is server or client but it's useful for debugs
	
	/**
	 * The connected client
	 */
	protected Socket client;
	
	/**
	 * The connected server
	 */
	protected Socket server;
	
	/**
	 * The client thread to communicate to
	 */
	protected Thread clientThread;
	
	/**
	 * The server thread to communicate to
	 */
	protected Thread serverThread;
	
	/**
	 * Closed state
	 */
	protected boolean closed;
	
	/**
	 * Constructor
	 * 
	 * @param app
	 * @param client
	 * @param server
	 * @throws Exception
	 */
	public SubketProxy(SubketApplication app, Socket client, Socket server) throws Exception {
		if( client.isClosed() ) {
			throw new Exception("client closed");
		}
		if( server.isClosed() ) {
			throw new Exception("server closed");
		}
		System.out.println("Creating proxy");
		this.closed	= false;
		this.app	= app;
		this.client	= client;
		this.server	= server;
		log("Detaching the proxy from the thread");
		// Once started, all lost connection cause Proxy terminating 
		new Thread(this).start();
		log("Detached proxy");
	}
	
	/**
	 * Log this report
	 * 
	 * @param s
	 */
	public void log(String s) {
		System.out.println("[Proxy-"+Thread.currentThread().getId()+"] "+s);
	}

	// When socket are closed, threads terminates
//	public synchronized void terminate() {
//		close();
//		if( !Thread.currentThread().equals(clientThread) ) {
//			clientThread.interrupt();
//		} else {
//			serverThread.interrupt();
//		}
//	}
	
	/**
	 * Close the current proxy by closing the 2 sockets
	 * 
	 * @return
	 */
	public synchronized boolean close() {
		if( isClosed() ) {
			return false;// Avoid infinite loop
		}
		closed = true;
		if( !client.isClosed() ) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if( !server.isClosed() ) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		app.disconnect(this);
		return true;
	}
	
	/**
	 * Check the proxy is closed
	 * 
	 * @return
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Send one byte to other
	 * 
	 * @param socket
	 * @param data
	 * @throws IOException
	 */
	private void sendTo(Socket socket, byte data) throws IOException {
		sendTo(socket, new byte[]{data});
	}
//	private void sendTo(Socket socket, int data) throws IOException {
//		sendTo(socket, ByteBuffer.allocate(4).putInt(data).array());
//	}
	
	/**
	 * Send a byte array to other
	 * 
	 * @param socket
	 * @param data
	 * @throws IOException
	 */
	private void sendTo(Socket socket, byte[] data) throws IOException {
		log("[SendTo] Writing "+data.length+" bytes...");
		socket.getOutputStream().write(data);
	}
	
	/**
	 * Transmit all data from input to output
	 * 
	 * @param input
	 * @param output
	 * @param direction The direction, only for logs
	 * @throws IOException
	 */
	private void transmit(Socket input, Socket output, String direction) throws IOException {
		byte[] buffer	= new byte[BUFFER_SIZE];
		InputStream inputStream		= input.getInputStream();
		OutputStream outputStream	= output.getOutputStream();
	    int byteGot;
	    while( (byteGot = inputStream.read(buffer)) > -1 ) {
    		outputStream.write(buffer, 0, byteGot);
			log("["+direction+"] Wrote "+byteGot+" bytes of data");
	    }
	}

	@Override
	public void run() {
		try {
			sendTo(client, SIGNAL_OK);
			sendTo(server, SIGNAL_OK);
		} catch (SocketException e) {
			log("Connection closed sending OK signal");
		} catch (Exception e) {
			e.printStackTrace();
			close();
			return;
		}
		
		// Send data from CLIENT to SERVER
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					clientThread = Thread.currentThread();
					transmit(client, server, "Client->Server");
				} catch (SocketException e) {
					log("Connection closed in CLIENT->SERVER Transmitter");
				} catch (Exception e) {
					e.printStackTrace();
				}
				close();
			}
		}).start();
		
		// Send data from SERVER to CLIENT
		try {
			serverThread = Thread.currentThread();
			transmit(server, client, "Server->Client");
		} catch (SocketException e) {
			log("Connection closed in SERVER->CLIENT Transmitter");
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
	}
}
