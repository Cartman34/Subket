package com.sowapps.subket.peer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Abstract subket response to use as callback
 * 
 * @author Florent HAZARD
 *
 */
public abstract class SubketResponse implements Runnable {
	
	protected byte[] data;
	
	/**
	 * Constructor
	 */
	protected SubketResponse() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param data The response data
	 */
	protected SubketResponse(byte[] data) {
		setData(data);
	}
	
	/**
	 * Set the data
	 * 
	 * @param data The response data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Get the data
	 * 
	 * @return The response data
	 */
	public byte[] getData() {
		return this.data;
	}
	
	/**
	 * Unserialize data as object 
	 * 
	 * @return The data as object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object unserialize() throws IOException, ClassNotFoundException {
	    return new ObjectInputStream(new ByteArrayInputStream(getData())).readObject();
	}
	
}
