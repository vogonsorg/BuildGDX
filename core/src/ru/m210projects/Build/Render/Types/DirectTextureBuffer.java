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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectTextureBuffer implements TextureBuffer {

	private ByteBuffer bb;
	public DirectTextureBuffer(int size)
	{
		this.bb = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public byte get(int i) {
		return bb.get(i);
	}

	@Override
	public int getInt(int i) {
		return bb.getInt(i);
	}

	@Override
	public void putBytes(byte[] src, int srcPos, int length) {
		bb.put(src, srcPos, length);
	}

	@Override
	public void put(int offset, byte value) {
		 bb.put(offset, value);
	}

	@Override
	public void putInt(int offset, int value) {
		 bb.putInt(offset, value);
	}

	@Override
	public void fill(int offset, int length, byte value) {
		for (int i = 0; i < length; i++)
			bb.put(offset + i, value);
	}

	@Override
	public void clear() {
		bb.clear();
	}

	@Override
	public ByteBuffer getBuffer() {
		bb.rewind();
		return bb;
	}
}
