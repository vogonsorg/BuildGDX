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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sun.nio.ch.DirectBuffer;

public class UnsafeDirectBuffer extends UnsafeWriteBuffer {

	private ByteBuffer bb;
	private int limit;
	private int capacity;

	public UnsafeDirectBuffer(int capacity) {
		this.bb = ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
		this.address = ((DirectBuffer) bb).address();
		
		this.capacity = capacity;
		this.limit = capacity;
	}
	
	public UnsafeDirectBuffer(byte[] data) {
		this.bb = ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
		this.address = ((DirectBuffer) bb).address();
		unsafe.copyMemory(data, BYTE_ARRAY_BASE_OFFSET, null, address, data.length);
		
		this.capacity = bb.capacity();
		this.limit = capacity;
	}

	public ByteBuffer getBuffer() { 
		rewind();
		return bb;
	}

	public int capacity() {
		return capacity;
	}
	
	@Override
	public UnsafeDirectBuffer rewind() {
		bb.rewind();
		position = 0;
		return this;
	}

	public UnsafeDirectBuffer flip() {
		bb.rewind();
		bb.limit(position);
		limit = position;
		position = 0;
		return this;
	}

	public UnsafeDirectBuffer clear() {
		bb.clear();
		position = 0;
		limit = capacity;
		
		return this;
	}
	
	public UnsafeDirectBuffer fill(int len, byte value)
	{
		unsafe.setMemory(address, len, value);
		return this;
	}

	public int remaining() {
		return limit - position;
	}

	public boolean hasRemaining() {
		return position < limit;
	}
}
