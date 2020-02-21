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

import sun.misc.Unsafe;

public class AndroidTextureBuffer extends FastTextureBuffer {

	public AndroidTextureBuffer(int size) throws Exception {
		super(size);
	}
	
	@Override
	protected void init() throws Exception
	{
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		
		this.unsafe = (Unsafe) theUnsafe.get(null);
		this.BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
		Class<?> buffer = bb.getClass().getSuperclass().getSuperclass().getSuperclass();
		
		Field f = buffer.getDeclaredField("effectiveDirectAddress");
		f.setAccessible(true);
		this.address = (long) f.get(bb);
	}
	
	@Override
	public byte get(int i) {
		return (byte) (getInt(i) & 0xFF);
	}
	
	@Override
	public void put(int offset, byte value) {
		bb.put(offset, value);
//		unsafe.put(null, getAddress(offset), value);
	}
	
	@Override
	public void putBytes(byte[] src, int srcPos, int length) {
		bb.put(src, srcPos, length);
	}

	@Override
	public void fill(int offset, int length, byte value) {
		for(int i = offset; i < offset + length; i++)
			put(i, value);
	}
}
