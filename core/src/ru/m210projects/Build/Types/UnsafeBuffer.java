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
import java.nio.ByteBuffer;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public abstract class UnsafeBuffer {

	protected static Unsafe unsafe;
	protected static long BYTE_ARRAY_BASE_OFFSET;
	
	protected int position;
	protected long address;

	static {
		unsafe = getTheUnsafe();
		BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
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
    	unsafe.copyMemory(null, getAddress(nextIndex(length)), dst, BYTE_ARRAY_BASE_OFFSET + offset, length);
        return this;
    }

    protected void setAddress(ByteBuffer bb)
    {
    	this.address = ((DirectBuffer) bb).address();
    }
    
    protected void dispose(ByteBuffer bb)
    {
    	((DirectBuffer) bb).cleaner().clean();
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
