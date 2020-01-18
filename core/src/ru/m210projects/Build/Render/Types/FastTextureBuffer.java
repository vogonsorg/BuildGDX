// This file is part of BuildGDX.
// Copyright (C) 2017-2020  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Render.Types;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public class FastTextureBuffer implements TextureBuffer {

	protected ByteBuffer bb;
	protected Unsafe unsafe;
	protected long BYTE_ARRAY_BASE_OFFSET;
	
	protected long address;
	protected int position;

	public FastTextureBuffer(int size) throws Exception 
	{
		this.bb = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
		
		init();
	}
	
	protected void init() throws Exception
	{
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		
		this.unsafe = (Unsafe) theUnsafe.get(null);
		this.BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
		this.address = ((DirectBuffer) bb).address();
		this.position = 0;
	}
	
	@Override
	public byte get(int i) {
		return unsafe.getByte(null, getAddress(i));
	}

	@Override
	public int getInt(int i) {
		return unsafe.getInt(null, getAddress(i));
	}
	
	@Override
	public void putBytes(byte[] src, int srcPos, int length) {
		unsafe.copyMemory(src, BYTE_ARRAY_BASE_OFFSET + srcPos, null, getAddress(nextIndex(length)), length);
	}

	@Override
	public void put(int offset, byte value) {
		unsafe.putByte(null, getAddress(offset), value);
	}

	@Override
	public void putInt(int offset, int value) {
		unsafe.putInt(null, getAddress(offset), value);
	}

	@Override
	public void fill(int offset, int length, byte value) {
		unsafe.setMemory(getAddress(offset), length, value);
	}

	@Override
	public void clear() {
		position = 0;
		bb.clear();
	}

	@Override
	public ByteBuffer getBuffer() {
		position = 0;
		bb.rewind();
		return bb;
	}

	protected long getAddress(int offset) {
		return address + offset;
	}
	
	protected int nextIndex(int nb) {
		int p = position;
		position += nb;
		return p;
	}
}
