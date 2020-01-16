//This file is part of BuildGDX.
//Copyright (C) 2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.FileHandle;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.m210projects.Build.Types.UnsafeBuffer;

public interface Resource extends Closeable {

	public class ResourceData extends UnsafeBuffer {

		private ByteBuffer bb;
		public ResourceData(ByteBuffer bb)
		{
			this.bb = bb.order(ByteOrder.LITTLE_ENDIAN);
			this.setAddress(bb);
		}
		
		public ResourceData(byte[] data)
		{
			this.bb = ByteBuffer.allocateDirect(data.length);
			this.bb.order(ByteOrder.LITTLE_ENDIAN);
			this.bb.put(data).rewind();
			this.setAddress(bb);
		}

		public String getString(int len)
		{
			if(remaining() < len) return null;
			
			byte[] buf = new byte[len];
			get(buf);
			return new String(buf);
		}

	    public int capacity() {
			return bb.capacity();
		}
		
		public ResourceData rewind() {
			bb.rewind();
			position = 0;
			return this;
		}

		public ResourceData flip() {
			bb.rewind();
			bb.limit(position);
			position = 0;
			return this;
		}

		public ResourceData clear() {
			bb.clear();
			position = 0;
			return this;
		}
		
		public void dispose()
		{
			dispose(bb);
		}
		
		public void limit(int newLimit)
		{
			bb.limit(newLimit);
		}

		public int remaining() {
			return bb.limit() - position;
		}

		public boolean hasRemaining() {
			return position < bb.limit();
		}
	}

	public static enum Whence { Set, Current, End };

	public String getExtension();
	
	public Group getParent();
	
	public void close();
	
	public boolean isClosed();
	
	public int seek(long offset, Whence whence);
	
	public int read(byte[] buf, int len);
	
	public int read(byte[] buf, int offset, int len);
	
	public int read(byte[] buf);
	
	public int read(ByteBuffer bb, int offset, int len);
	
	public String readString(int len);
	
	public Integer readInt();
	
	public Short readShort();
	
	public Byte readByte();
	
	public int size();
	
	public int position();
	
	public ResourceData getData();
	
	public byte[] getBytes();

}
