package ru.m210projects.Build.Types;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

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
}
