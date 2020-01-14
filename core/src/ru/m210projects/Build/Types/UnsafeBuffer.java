// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Application.ApplicationType;

import ru.m210projects.Build.Architecture.BuildGdx;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public abstract class UnsafeBuffer {

	protected static Unsafe unsafe;
	protected static long BYTE_ARRAY_BASE_OFFSET;
	protected static int JAVA_VERSION;
	
	protected int position;
	protected long address;

	static {
		unsafe = getTheUnsafe();
		BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
		
		JAVA_VERSION = getVersion();
	}
	
	private static int getVersion()
	{
		String version = System.getProperty("java.version");
	    if(version.startsWith("1.")) {
	        version = version.substring(2, 3);
	    } else {
	        int dot = version.indexOf(".");
	        if(dot != -1) { version = version.substring(0, dot); }
	    } return Integer.parseInt(version);
	}
	
	private static Unsafe getTheUnsafe() {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			return null;
		}
	}
	
	public byte get() {
        return get(nextIndex(1));
    }

    public byte get(int i) {
        return unsafe.getByte(getAddress(i));
    }
    
    public short getShort() {
        return getShort(nextIndex((1 << 1)));
    }

    public short getShort(int i) {
        return unsafe.getShort(getAddress(i));
    }
	
	public int getInt() {
		return getInt(nextIndex((1 << 2)));
	}

	public int getInt(int i) {
		return unsafe.getInt(getAddress(i));
	}
	
	public float getFloat(int i) {
		return unsafe.getFloat(getAddress(i));
	}
	
	public float getFloat() {
		return getFloat(nextIndex((1 << 2)));
	}
	
	public long getLong(int i) {
		return unsafe.getLong(getAddress(i));
	}
	
	public long getLong() {
		return getLong(nextIndex((1 << 3)));
	}
	
	public UnsafeBuffer get(byte[] dst) {
		return get(dst, 0, dst.length);
	}
    
    public UnsafeBuffer get(byte[] dst, int offset, int length) {
    	if(BuildGdx.app.getType() != ApplicationType.Android)
    		unsafe.copyMemory(null, getAddress(nextIndex(length)), dst, BYTE_ARRAY_BASE_OFFSET + offset, length);
    	else { //no such method copyMemory
    		for(int i = 0; i < length; i++)
    			dst[offset + i] = unsafe.getByte(getAddress(nextIndex(1)));
    	}
        return this;
    }

    protected void setAddress(ByteBuffer bb)
    {
    	this.address = ((DirectBuffer) bb).address();
    }
    
    protected void dispose(ByteBuffer bb)
    {
    	try {
	    	if(JAVA_VERSION < 9) {
	    		Object cleaner = ((DirectBuffer) bb).cleaner();
	    		Method invokeCleaner = cleaner.getClass().getDeclaredMethod("clean");
	    		invokeCleaner.setAccessible(true);
	    		invokeCleaner.invoke(cleaner);
	    	} else {
	    		Method invokeCleaner = unsafe.getClass().getMethod("invokeCleaner", ByteBuffer.class);
		    	invokeCleaner.invoke(unsafe, bb);
	    	}
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    }
	
	protected long getAddress(int offset) {
		return address + offset;
	}

	protected int nextIndex(int nb) {
		int p = position;
		position += nb;
		return p;
	}

	public int position() {
		return position;
	}
	
	public UnsafeBuffer position(int newPosition) {
		position = newPosition;
		return this;
	}
	
	public abstract UnsafeBuffer rewind();
}
