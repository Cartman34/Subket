package subket.pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;

// TODO: If server and connected to client, open new connection  

public class SubketPair implements Runnable {
	
	public static final short CLIENT	= 1;
	public static final short SERVER	= 2;
	
	public static final short BUFFER_SIZE	= 1024;
	public static final short defaultPort	= 2013;

	public final static byte SIGNAL_OK	= 1;
	public final static byte SIGNAL_NOT	= 0;

	protected int appKey;
	protected String subketHost;
	protected short subketPort;
	protected short pairType;
	protected Socket subket;
	protected InputStream inputStream;
	protected OutputStream outputStream;
	protected Thread runningThread;
	protected Class<? extends SubketResponse> respClass;
	protected SubketResponse respObject;
	protected boolean asynchrone;
	protected boolean connected = false;
	
//	public final static byte[] SIGNAL_OK = new byte[]{0,0,0,1};

	public SubketPair(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass, SubketResponse respObject) {
		this.appKey		= appKey;
		this.subketHost	= subketHost;
		this.subketPort	= subketPort;
		this.pairType	= isServer ? SERVER : CLIENT;
		this.respClass	= respClass;
		this.respObject	= respObject;
		setAsynchrone();
	}
	public SubketPair(int appKey, String subketHost, short subketPort, boolean isServer, Class<? extends SubketResponse> respClass) {
		this(appKey, subketHost, subketPort, isServer, respClass, null);
	}
	public SubketPair(int appKey, String subketHost, short subketPort, boolean isServer, SubketResponse respObject) {
		this(appKey, subketHost, subketPort, isServer, null, respObject);
	}
	
	public void setSynchrone() {
		this.asynchrone	= false;
	}
	public void setAsynchrone() {
		this.asynchrone	= true;
	}
	public boolean isAsynchrone() {
		return this.asynchrone;
	}
	public boolean isRunning() {
		return outputStream != null;
	}
	public boolean isConnected() {
		return connected;
	}
	
	public OutputStream convertOutputStream(OutputStream s) throws Exception {
		return s;
	}
	public InputStream convertInputStream(InputStream s) throws Exception {
		return s;
	}
	
	public void setOutputStream(OutputStream os) throws Exception {
		if( !isRunning() ) {
			throw new Exception("Pair is not running");
		}
		if( os == null ) {
			throw new Exception("Null value");
		}
		outputStream = os;
	}

	public synchronized void sendRaw(byte[] packet) throws Exception {
		if( !isRunning() ) {
			throw new Exception("Pair is not running");
		}
		outputStream.write(packet);
		outputStream.flush();
//		outputStream.write(0);
//		outputStream.flush();
	}
	public synchronized void send(byte[] data) throws Exception {
		log("Sending "+data.length+" bytes (+ 4 bytes for length value).");
		sendRaw(ByteBuffer.allocate(data.length+4).putInt(data.length).put(data).array());
	}
	public synchronized void send(Serializable data) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		new ObjectOutputStream(bos).writeObject(data);
		send(bos.toByteArray());
	}
	public static synchronized void log(String s) {
		System.out.println("[Pair] "+s);
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
			sendRaw(ByteBuffer.allocate(5).putInt(appKey).put((byte) pairType).array());
			
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
			System.out.println("Error running SubketPair listener");
			e.printStackTrace();
		}
		close();
		log("Ending running SubketPair Thread");
	}
	
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
