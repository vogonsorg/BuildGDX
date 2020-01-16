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

import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static ru.m210projects.Build.Strhandler.toLowerCase;

public class FileResource implements Resource {
	
	private static byte[] readbuf = new byte[1024];
	
	public static enum Mode { Read, Write }

	private RandomAccessFile raf;
	private Mode mode;
	private String ext;
	
	protected FileResource open(File file, Mode mode)
	{
		this.mode = mode;
		try {
			switch(mode)
			{
				case Read:
					raf = new RandomAccessFile(file, "r");
					handle(file);
					return this;
				case Write:
					raf = new RandomAccessFile(file, "rw");
					raf.setLength(0);
					handle(file);
					return this;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		} 
		
		return null;
	}
	
	private void handle(File file)
	{
		String filename = toLowerCase(file.getName());
		ext = filename.substring(filename.lastIndexOf('.') + 1);
	}
	
	public String getPath()
	{
		if(isClosed()) return null;
		
		try {
			Field path = raf.getClass().getDeclaredField("path");
			path.setAccessible(true);
			return (String) path.get(raf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Mode getMode()
	{
		return mode;
	}
	
	@Override
	public boolean isClosed()
	{
		return raf == null;
	}

	@Override
	public void close() {
		if(isClosed()) return;
		
		try {
			raf.close();
			raf = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int seek(long offset, Whence whence) {
		int var = -1;
		if(isClosed()) return var;

		try {
			if(whence == Whence.Set) {
				if(offset < 0) return -1;
				raf.getChannel().position(offset);			
			} else if(whence == Whence.Current) {
				raf.getChannel().position(raf.getChannel().position() + offset);		
			} else if(whence == Whence.End) {
				raf.getChannel().position(raf.getChannel().size() + offset);			
			}
			
			var = (int) raf.getChannel().position();
		} catch (Exception e) {
			e.printStackTrace();
	    } 
		
		return var;
	}

	@Override
	public int read(byte[] buf, int offset, int len) {
		int var = -1;
		if(isClosed()) return var;
		
		try {
			var = raf.read(buf, offset, len);
		} catch (EOFException e) {
	    	return -1;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		return var;
	}
	
	@Override
	public int read(byte[] buf, int len) {
		int var = -1;
		if(isClosed()) return var;
		
		try {
			var = raf.read(buf, 0, len);
		} catch (EOFException e) {
	    	return -1;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		return var;
	}
	
	@Override
	public int read(byte[] buf) {
		return read(buf, 0, buf.length);
	}
	
	@Override
	public int read(ByteBuffer bb, int offset, int len) {
		try {
			int var = -1;
			bb.position(offset);
			int p = 0;
			while(len > 0)
			{
				if((var = raf.read(readbuf, 0, Math.min(len, readbuf.length))) == -1)
					return p;
				bb.put(readbuf, 0, var);
				len -= var;
				p += var;
			}
			return len;
		} catch (EOFException e) {
	    	return -1;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
	}

	@Override
	public String readString(int len)
	{
		byte[] data;
		if(len < readbuf.length)
			data = readbuf;
		else data = new byte[len];
		if(read(data, 0, len) != len)
			return null;
		
		return new String(data, 0, len);
	}
	
	@Override
	public Integer readInt()
	{
		Integer var = null;
		if(isClosed()) return var;
		
		try {
			if(raf.read(readbuf, 0, 4) == 4)
				return ( (readbuf[3] & 0xFF) << 24 ) + ( (readbuf[2] & 0xFF) << 16 ) + ( (readbuf[1] & 0xFF) << 8 ) + ( readbuf[0] & 0xFF );
			
		} catch (EOFException e) {
	    	return null;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		return var;
	}
	
	@Override
	public Short readShort()
	{
		Short var = null;
		if(isClosed()) return var;
		
		try {
			if(raf.read(readbuf, 0, 2) == 2)
				return (short) ( ( (readbuf[1] & 0xFF) << 8 ) + ( readbuf[0] & 0xFF ) );
		} catch (EOFException e) {
	    	return null;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		return var;
	}
	
	@Override
	public Byte readByte()
	{
		if(isClosed()) return null;
		
		try {
			return raf.readByte();
		} catch (EOFException e) {
	    	return null;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
	}

	protected ByteBuffer readBuffer(int len)
	{
		ByteBuffer out = null;
		if(isClosed()) return null;
		
		try {
			FileChannel ch = raf.getChannel();
			long pos = ch.position();
			out = ch.map(FileChannel.MapMode.READ_ONLY, pos, len);
			ch.position(pos + len);
		} catch (EOFException e) {
	    	return null;
	    } catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		
		return out;
	}
	
	public int writeBytes(Object array) {
		int len = 0;
		if(array instanceof byte[]) 
			len = ((byte[])array).length;
		else if(array instanceof ByteBuffer) 
			len = ((ByteBuffer) array).capacity();
		else if(array instanceof short[]) 
			len = ((short[])array).length;
		else if(array instanceof int[]) 
			len = ((int[])array).length;
		else if(array instanceof char[])
			len = ((char[]) array).length;
		else if(array instanceof String) 
			len = ((String)array).getBytes().length;

		if(len != 0)
			return writeBytes(array, len);
		
		return -1;
	}
	
	public int writeBytes(Object array, int len) {
		int var = -1;
		if(isClosed() || getMode() != Mode.Write) return var;
		
		try {
			byte[] data = null;
			if(array instanceof byte[])
				data = (byte[])array;
			else if(array instanceof char[]) {
				data = new byte[len];
				char[] src = (char[]) array;
				for(int i = 0; i < Math.min(len, src.length); i++) 
					data[i] = (byte) src[i];
			}
			else if(array instanceof ByteBuffer) {
				ByteBuffer buf = (ByteBuffer) array;
				buf.rewind();
				if(!buf.isDirect()) 
					data = buf.array();
				else {
					data = new byte[Math.min(len, buf.capacity())];
					buf.get(data);
				}
			}
			else if(array instanceof short[]) {
				var = 0;
				short[] shortArr = (short[])array;
				len = Math.min(len, shortArr.length);
				for(int i = 0; i < len; i++) 
					var += writeShort(shortArr[i]);
			}
			else if(array instanceof int[]) {
				var = 0;
				int[] intArr = (int[])array;
				len = Math.min(len, intArr.length);
				for(int i = 0; i < len; i++) 
					var += writeInt(intArr[i]);
			}
			else if(array instanceof String) {
				data = ((String)array).getBytes();
			}

			if(data != null) {
				len = Math.min(len, data.length);
				raf.write(data, 0, len);
				var = len;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't write to file \r\n" + e.getMessage());
	    } 
		return var;
	}

	public int writeByte(int value)
	{
		int var = -1;
		if(isClosed() || getMode() != Mode.Write) return var;
		
		readbuf[0] = (byte) value;
		
		try {  
			raf.write(readbuf, 0, 1);
			var = 1;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to file \r\n" + e.getMessage());
	    } 
		
		return var;
	}
	
	public int writeShort(int value)
	{
		int var = -1;
		if(isClosed() || getMode() != Mode.Write) return var;
		
		readbuf[0] = (byte) ( ( value >>> 0 ) & 0xFF );
		readbuf[1] = (byte) ( ( value >>> 8 ) & 0xFF );
		
		try {  
			raf.write(readbuf, 0, 2);
			var = 2;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to file \r\n" + e.getMessage());
	    } 
		
		return var;
	}
	
	public int writeInt(int value)
	{
		int var = -1;
		if(isClosed() || getMode() != Mode.Write) return var;
		
		readbuf[0] = (byte) ( ( value >>> 0 ) & 0xFF );
		readbuf[1] = (byte) ( ( value >>> 8 ) & 0xFF );
		readbuf[2] = (byte) ( ( value >>> 16 ) & 0xFF );
		readbuf[3] = (byte) ( ( value >>> 24 ) & 0xFF );
		
		try {  
			raf.write(readbuf, 0, 4);
			var = 4;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to file \r\n" + e.getMessage());
	    } 
		
		return var;
	}

	@Override
	public int size() {
		int var = -1;
		if(isClosed()) return var;
		
		try {
			var = (int) raf.length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return var;
	}

	@Override
	public int position() {
		int var = -1;
		if(isClosed()) return var;

		try {
			var = (int) raf.getChannel().position();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return var;
	}

	@Override
	public ResourceData getData() {
		ResourceData out = null;
		if(isClosed()) return null;
		
		try {
			out = new ResourceData(raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length()));
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read file \r\n" + e.getMessage());
	    }
		
		return out;
	}
	
	@Override
	public byte[] getBytes() {
		int size = this.size();
		if(size > 0) {
			byte[] data = new byte[size];
			if(this.read(data) != -1)
				return data;
		}
		return null;
	}

	@Override
	public Group getParent() {
		return null;
	}

	@Override
	public String getExtension() {
		return ext;
	}
}
