package subket.hub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SubketProxy implements Runnable {
	
	public final static byte SIGNAL_OK	= 1;
	public final static byte SIGNAL_NOT	= 0;
	
	public final static int BUFFER_SIZE	= 4092;//Formerly 1024, 4092 is the max buffer size for Java (or system)
	
	protected SubketApplication app;
	
	// Proxy don't about which one is server or client but it's useful for debugs
	protected Socket client;
	protected Socket server;
		
	protected Thread clientThread;
	protected Thread serverThread;
	
	protected boolean closed;
	
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
	
	public boolean isClosed() {
		return closed;
	}
	
	private void sendTo(Socket socket, byte data) throws IOException {
		sendTo(socket, new byte[]{data});
	}
//	private void sendTo(Socket socket, int data) throws IOException {
//		sendTo(socket, ByteBuffer.allocate(4).putInt(data).array());
//	}
	private void sendTo(Socket socket, byte[] data) throws IOException {
		log("[SendTo] Writing "+data.length+" bytes...");
		socket.getOutputStream().write(data);
	}
	
	private void transmit(Socket input, Socket output, String direction) throws IOException {
		byte[] buffer	= new byte[BUFFER_SIZE];
		InputStream inputStream		= input.getInputStream();
		OutputStream outputStream	= output.getOutputStream();
	    int byteGot;
	    while( (byteGot = inputStream.read(buffer)) > -1 ) {
//	    	if( byteGot > 0 ) {
    		outputStream.write(buffer, 0, byteGot);
			log("["+direction+"] Wrote "+byteGot+" bytes of data");
//	    	}
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
		
		// Sends data from CLIENT to SERVER
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
		
		// Sends data from SERVER to CLIENT
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
