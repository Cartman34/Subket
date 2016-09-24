package com.sowapps.subket.peer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public abstract class SubketResponse implements Runnable {
	
	protected byte[] data;

	protected SubketResponse() {
	}

	protected SubketResponse(byte[] data) {
		setData(data);
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
//	protected ObjectInputStream ois;
	public Object unserialize() throws IOException, ClassNotFoundException {
//		ois	= new ObjectInputStream(new ByteArrayInputStream(getData()));
//		ois.
//		return ois.readObject();
	    return new ObjectInputStream(new ByteArrayInputStream(getData())).readObject();
	}
	
}
