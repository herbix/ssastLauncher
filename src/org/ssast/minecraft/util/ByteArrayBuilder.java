package org.ssast.minecraft.util;

public class ByteArrayBuilder {
	byte[] buffer = new byte[1];
	int capacity = 1;
	int size = 0;
	
	public void append(byte[] buff, int off, int len) {
		if(buff.length - off < len)
			throw new IllegalArgumentException();
		if(len + size > capacity) {
			while(len + size > capacity)
				capacity *= 2;
			byte[] old = buffer;
			buffer = new byte[capacity];
			for(int i=0; i<size; i++)
				buffer[i] = old[i];
		}
		for(int j=off; j<off+len; size++, j++) {
			buffer[size] = buff[j];
		}
	}
	
	public byte[] toArray() {
		byte[] r = new byte[size];
		for(int i=0; i<size; i++)
			r[i] = buffer[i];
		return r;
	}
	
	public int size() {
		return size;
	}
}
