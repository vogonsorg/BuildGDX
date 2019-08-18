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

public class DataResource extends GroupResource {

	public DataResource(Group parent, String filename, int fileid, byte[] data) {
		super(parent);
		
		this.handleName(filename);
		this.fileid = fileid;
		if(data != null) {
			buffer = new ResourceData(data);
			buffer.rewind();
			this.size = data.length;
		}
	}
	
	@Override
	public void flush() { /* nothing */ }

	@Override
	public void close() { synchronized(parent) { buffer.rewind(); } }

	@Override
	public int seek(long offset, Whence whence) {
		synchronized(parent) {
			switch (whence)
	        {
	        	case Set: buffer.position((int) offset); break;
	        	case Current: buffer.position(buffer.position() + (int)offset); break;
	        	case End: buffer.position(size + (int) offset);  break;
	        }
	        return position();
		}
	}

	@Override
	public int read(byte[] buf, int len) {
		synchronized(parent) {
			if(position() >= size) 
				return -1;
			
			len = Math.min(len, size - position());
			buffer.get(buf, 0, len);
			return len;
		}
	}
	
	@Override
	public int read(byte[] buf) {
		synchronized(parent) {
			return read(buf, buf.length);
		}
	}
	
	@Override
	public Byte readByte() {
		synchronized(parent) {
			return buffer.get();
		}
	}
	
	@Override
	public Short readShort() {
		synchronized(parent) {
			return buffer.getShort();
		}
	}

	@Override
	public Integer readInt() {
		synchronized(parent) {
			return buffer.getInt();
		}
	}

	@Override
	public String readString(int len)
	{
		synchronized(parent) {
			byte[] data = new byte[len];
			if(read(data) != len)
				return null;
			
			return new String(data);
		}
	}
	
	@Override
	public int position() {
		synchronized(parent) {
			return buffer.position();
		}
	}

	@Override
	public ResourceData getData() {
		synchronized(parent) {
			buffer.rewind();
			return buffer;
		}
	}

	@Override
	public byte[] getBytes() {
		synchronized(parent) {
			byte[] data = new byte[buffer.capacity()];
			buffer.rewind();
			buffer.get(data);
			return data;
		}
	}

	@Override
	public boolean isClosed() {
		synchronized(parent) {
			return buffer == null;
		}
	}

}
