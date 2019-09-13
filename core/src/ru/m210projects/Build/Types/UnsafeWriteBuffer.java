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

public abstract class UnsafeWriteBuffer extends UnsafeBuffer {

	public UnsafeBuffer put(byte x) {
        unsafe.putByte(getAddress(nextIndex(1)), x);
        return this;
    }

    public UnsafeBuffer put(int i, byte x) {
        unsafe.putByte(getAddress(i), x);
        return this;
    }
    
    public UnsafeBuffer wrap(byte[] src) {
    	unsafe.copyMemory(src, BYTE_ARRAY_BASE_OFFSET, null, address, src.length);
    	return rewind();
    }
    
    public UnsafeBuffer putBytes(byte[] src, int offset, int length) {
        unsafe.copyMemory(src, BYTE_ARRAY_BASE_OFFSET + offset, null, getAddress(nextIndex(length)), length);
        return this;
    }
    
    public UnsafeBuffer putShort(short x) {
		unsafe.putShort(getAddress(nextIndex((1 << 1))), x);
        return this;
    }

    public UnsafeBuffer putShort(int i, short x) {
    	unsafe.putShort(getAddress(i), x);
        return this;
    }

	public UnsafeBuffer putInt(int value) {
		unsafe.putInt(getAddress(nextIndex((1 << 2))), value);
		return this;
	}
	
	public UnsafeBuffer putInt(int i, int value) {
		unsafe.putInt(getAddress(i), value);
        return this;
    }

}
