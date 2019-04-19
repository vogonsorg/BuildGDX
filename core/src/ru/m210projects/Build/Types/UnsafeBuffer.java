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
import java.nio.ByteOrder;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public class UnsafeBuffer {

	private Unsafe unsafe;
	private ByteBuffer bb;
	private int position;
	private long address;
	private int limit;
	private int capacity;

	public UnsafeBuffer(int capacity) {
		unsafe = getTheUnsafe();
		bb = ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
		address = ((DirectBuffer) bb).address();
		this.limit = capacity;
		this.capacity = capacity;
	}

	public ByteBuffer getBuffer() { 
		rewind();
		return bb;
	}
	
	public byte get() {
        return unsafe.getByte(ix(nextIndex(1)));
    }

    public byte get(int i) {
        return unsafe.getByte(ix(checkIndex(i, 1)));
    }
    
    public UnsafeBuffer get(byte[] dst, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }
	
	public UnsafeBuffer put(byte x) {
        unsafe.putByte(ix(nextIndex(1)), x);
        return this;
    }

    public UnsafeBuffer put(int i, byte x) {
        unsafe.putByte(ix(checkIndex(i, 1)), x);
        return this;
    }
    
    public UnsafeBuffer put(byte[] src, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i < end; i++)
        	this.put(src[i]);
        return this;
    }

	public short getShort() {
        return unsafe.getShort(ix(nextIndex((1 << 1))));
    }

    public short getShort(int i) {
        return unsafe.getShort(ix(checkIndex(i, (1 << 1))));
    }
    
	public UnsafeBuffer putShort(short x) {
		unsafe.putShort(ix(nextIndex((1 << 1))), x);
        return this;
    }

    public UnsafeBuffer putShort(int i, short x) {
    	unsafe.putShort(ix(checkIndex(i, (1 << 1))), x);
        return this;
    }
	
	public int getInt() {
		return unsafe.getInt(ix(nextIndex((1 << 2))));
	}

	public int getInt(int value) {
		return unsafe.getInt(ix(checkIndex(value, (1 << 2))));
	}

	public UnsafeBuffer putInt(int value) {
		unsafe.putInt(ix(nextIndex((1 << 2))), value);
		return this;
	}
	
	public UnsafeBuffer putInt(int i, int value) {
		unsafe.putInt(ix(checkIndex(i, (1 << 2))), value);
        return this;
    }

	private long ix(int i) {
		return address + (i << 0);
	}

	private int nextIndex(int nb) {
//        if (limit - position < nb)
//            throw new BufferUnderflowException();
		int p = position;
		position += nb;
		return p;
	}

	private int checkIndex(int i, int nb) {
//		if ((i < 0) || (nb > limit - i))
//			throw new IndexOutOfBoundsException();
		return i;
	}
	
	public int capacity() {
		return capacity;
	}

	public int position() {
		return position;
	}

	public UnsafeBuffer rewind() {
		bb.rewind();
		position = 0;
		return this;
	}

	public UnsafeBuffer flip() {
		bb.rewind();
		bb.limit(position);
		limit = position;
		position = 0;
		return this;
	}

	public UnsafeBuffer clear() {
		bb.clear();
		position = 0;
		limit = capacity;
		return this;
	}

	public int remaining() {
		return limit - position;
	}

	public boolean hasRemaining() {
		return position < limit;
	}

	private Unsafe getTheUnsafe() {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
