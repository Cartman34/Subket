package com.sowapps.subket.peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;

// TODO: If server and connected to client, open new connection  

public class SubketPeer implements Runnable {
	
	public static final short CLIENT	= 1;
	public static final short SERVER	= 2;
	
	public static final short BUFFER_SIZE	= 1024;
	public static final short defaultPort	= 2013;

	public final static byte SIGNAL_OK	= 1;
	public final static byte SIGNAL_NOT	= 0;
	
	/**
	 * The application key
	 */
	protected int appKey;
	
	/**
	 * The hub host
	 */
	protected String subketHost;
	
	/**
	 * The hub port
	 */
	protected short subketPort;
	
	/**
	 * The hub peer type
	 */
	protected short peerType;
	
	/**
	 * The active socket connection to hub
	 */
	protected Socket subket;
	
	/**
	 * The input stream from hub
	 */
	protected InputStream inputStream;
	
	/**
	 * The output stream to hub
	 */
	protected OutputStream outputStream;
	
	/**
	 * The response callback class
	 */
	protected Class<? extends SubketResponse> respClass;
	
	/**
	 * The response callback object
	 */
	protected SubketResponse respObject;
	
	/**
	 * Asynchrone state
	 */
	protected boolean asynchrone;
	
	/**
	 * Connected state
	 */
	protected boolean connected = false;
	
//	public final static byte[] SIGNAL_OK = new byte[]{0,0,0,1};
	
	/**
	 * Constructor
	 * 
	 * @param appKey
	 * @param subketHost
	 * @param subketPort
	 * @param isServer
	 * @param respClass
	 * @param respObject
	 */
	public SubketPeer(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass, SubketResponse respObject) {
		this.appKey		= appKey;
		this.subketHost	= subketHost;
		this.subketPort	= subketPort;
		this.peerType	= isServer ? SERVER : CLIENT;
		this.respClass	= respClass;
		this.respObject	= respObject;
		setAsynchrone();
	}
	
	/**
	 * Constructor
	 * 
	 * @param appKey
	 * @param subketHost
	 * @param subketPort
	 * @param isServer
	 * @param respClass
	 */
	public SubketPeer(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		this(appKey, subketHost, subketPort, isServer, respClass, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param appKey
	 * @param subketHost
	 * @param subketPort
	 * @param isServer
	 * @param respObject
	 */
	public SubketPeer(int appKey, String subketHost, short subketPort, boolean isServer, SubketResponse respObject) {
		this(appKey, subketHost, subketPort, isServer, null, respObject);
	}
	
	/**
	 * Set peer synchrone
	 */
	public void setSynchrone() {
		this.asynchrone	= false;
	}
	
	/**
	 * Set peer asynchrone
	 */
	public void setAsynchrone() {
		this.asynchrone	= true;
	}
	
	/**
	 * Is asynchrone ?
	 * 
	 * @return
	 */
	public boolean isAsynchrone() {
		return this.asynchrone;
	}
	
	/**
	 * Is this peer running ?
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return outputStream != null;
	}
	
	/**
	 * Is this peer connected ?
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Convert output stream
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public OutputStream convertOutputStream(OutputStream s) throws Exception {
		return s;
	}
	
	/**
	 * Convert input stream
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public InputStream convertInputStream(InputStream s) throws Exception {
		return s;
	}
	
	/**
	 * Set output stream
	 * 
	 * @param os
	 * @throws Exception
	 */
	public void setOutputStream(OutputStream os) throws Exception {
		if( !isRunning() ) {
			throw new Exception("Peer is not running");
		}
		if( os == null ) {
			throw new Exception("Null value");
		}
		outputStream = os;
	}
	
	/**
	 * Send packet to the output stream with no process
	 * 
	 * @param packet
	 * @throws Exception
	 */
	public synchronized void sendRaw(byte[] packet) throws Exception {
		if( !isRunning() ) {
			throw new Exception("Peer is not running");
		}
		outputStream.write(packet);
		outputStream.flush();
//		outputStream.write(0);
//		outputStream.flush();
	}
	
	/**
	 * Send packet to the output stream prepended by length
	 * 
	 * @param packet
	 * @throws Exception
	 */
	public synchronized void send(byte[] data) throws Exception {
		log("Sending "+data.length+" bytes (+ 4 bytes for length value).");
		sendRaw(ByteBuffer.allocate(data.length+4).putInt(data.length).put(data).array());
	}
	
	/**
	 * Send Serializable object to the output stream
	 * @param data
	 * @throws Exception
	 */
	public synchronized void send(Serializable data) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		new ObjectOutputStream(bos).writeObject(data);
		send(bos.toByteArray());
	}
	
	/**
	 * Log this report
	 * 
	 * @param s
	 */
	public static synchronized void log(String s) {
		System.out.println("[Peer] "+s);
	}

	@Override
	public void run() {
		try {
			log("Trying to connect to Subket "+subketHost+":"+subketPort);
			subket = new Socket(subketHost, subketPort);
		} catch (Exception e) {
			log("Error while connecting to subket");
			return;
		}
		try {
			log("Running");
			outputStream	= convertOutputStream(subket.getOutputStream());
			// Send authentication to hub
			sendRaw(ByteBuffer.allocate(5).putInt(appKey).put((byte) peerType).array());
			
			inputStream		= convertInputStream(subket.getInputStream());
			
			// Blocking until hub signal
			log("Waiting for linked signal from subket...");
			byte[] packet		= new byte[1];
			do {
				// Ask if OK ?
				outputStream.write(SIGNAL_OK);
				// Wait for response
				inputStream.read(packet);
				// If not ok, we ask again
			} while( packet[0] != SIGNAL_OK );
			System.out.println("\tConnected.");
			connected = true;
			
			// Waits for inputs from hub
			byte[] lengthBuffer	= new byte[4];
			byte[] data			= new byte[0];
//			packet				= new byte[0];
		    int dataLength, dataRead, packetRead;
		    
		    while( inputStream.read(lengthBuffer) > -1 ) {
		    	// Reading data length
		    	dataLength = ByteBuffer.wrap(lengthBuffer).getInt();
		    	log("Receiving "+dataLength+" bytes, prepared to get bytes and to build response");
		    	dataRead	= 0;
		    	data	= new byte[dataLength];
		    	packet	= new byte[4092];// A buffer size (for my config)
		    	// Read data from the buffer
		    	while( dataRead < dataLength ) {
//		    		System.out.println("Waiting for packet !");
		    		packetRead	= inputStream.read(packet);
//		    		data
		    		System.arraycopy(packet, 0, data, dataRead, packetRead);
		    		dataRead	+= packetRead;
		    	}
		    	packet	= new byte[0];
		    	packetRead	= 0;
		    	/*
		    	byte[] c = new byte[a.length + b.length];
				System.arraycopy(a, 0, c, 0, a.length);
				System.arraycopy(b, 0, c, a.length, b.length);
				*/
		    	log("Read "+dataRead+" bytes");
		    	
		    	// Returns response as new instance of class respClass
		    	if( respClass != null ) {
			    	SubketResponse resp = respClass.newInstance();
			    	resp.setData(data);
			    	log("calling response class");
			    	if( isAsynchrone() ) {
			    		new Thread(resp).start();
			    	} else {
			    		resp.run();
			    	}
			    }
		    	
		    	// Returns response as direct call of run() from respClass
//	    		SubketResponse resp = respClass.newInstance();
		    	if( respObject != null ) {
			    	respObject.setData(data);
			    	log("calling response object");
			    	if( isAsynchrone() ) {
			    		new Thread(respObject).start();
			    	} else {
			    		respObject.run();
			    	}
			    }
		    	
		    	data = new byte[0];
		    }
		    log("Connection closed");
			
		} catch (Exception e) {
			System.out.println("Error running SubketPeer listener");
			e.printStackTrace();
		}
		close();
		log("Ending running SubketPeer Thread");
	}
	
	/**
	 * Close this proxy by closing the socket and resetting the output stream
	 */
	public synchronized void close() {
		if( !subket.isClosed() ) {
			try {
				subket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if( outputStream != null ) {
			outputStream = null;
		}
	}
}
