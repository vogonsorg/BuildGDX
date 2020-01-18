package ru.m210projects.Build.android;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaDataSource;

@SuppressLint("NewApi")
public class ByteDataSource extends MediaDataSource {

	private final Object data;
	private final int size;

	public ByteDataSource(Object data) throws Exception {
		if(data instanceof byte[])
			this.size = ((byte[]) data).length;
		else if(data instanceof ByteBuffer)
			this.size = ((ByteBuffer) data).limit();
		else throw new Exception("Unsupported array");
		
		this.data = data;
	}

	@Override
	public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
		if (position >= getSize())
			return -1;

		int len = (int) Math.min(getSize(), size);
		if(data instanceof byte[])
			System.arraycopy(data, (int) position, buffer, offset, len);
		else if(data instanceof ByteBuffer) 
			((ByteBuffer) data).get(buffer, offset, len);
		return len;
	}

	@Override
	public long getSize() throws IOException {
		return size;
	}

	@Override
	public void close() throws IOException {
		/* nothing */ }

}
