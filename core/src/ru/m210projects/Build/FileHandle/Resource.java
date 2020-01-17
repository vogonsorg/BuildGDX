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

public interface Resource extends Closeable {

	public interface IResourceData {
		
		public byte get();

	    public byte get(int i);
	    
	    public short getShort();

	    public short getShort(int i);
		
		public int getInt();

		public int getInt(int i);
		
		public float getFloat(int i);
		
		public float getFloat();
		
		public long getLong(int i);
		
		public long getLong();
		
		public int position();
		
		public void position(int newPosition);
		
		public void get(byte[] dst);
	    
	    public void get(byte[] dst, int offset, int length);

		public String getString(int len);

	    public int capacity();
		
		public void rewind();

		public void flip();

		public void clear();
		
		public void dispose();
		
		public void limit(int newLimit);

		public int remaining();

		public boolean hasRemaining();
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
	
	public Boolean readBoolean();
	
	public Long readLong();
	
	public Float readFloat();
	
	public int size();
	
	public int position();
	
	public int remaining();
	
	public boolean hasRemaining();
	
//	public IResourceData getData();
	
	public byte[] getBytes();

}
