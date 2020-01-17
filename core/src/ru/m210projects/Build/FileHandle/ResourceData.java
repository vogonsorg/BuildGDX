package ru.m210projects.Build.FileHandle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.m210projects.Build.FileHandle.Resource.IResourceData;

public class ResourceData implements IResourceData {

	private ByteBuffer bb;
	public ResourceData(ByteBuffer bb)
	{
		this.bb = bb.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public ResourceData(byte[] data)
	{
		this.bb = ByteBuffer.allocateDirect(data.length);
		this.bb.order(ByteOrder.LITTLE_ENDIAN);
		this.bb.put(data).rewind();
	}

	@Override
    public int capacity() {
		return bb.capacity();
	}
	
	@Override
	public void rewind() {
		bb.rewind();
	}

	@Override
	public void flip() {
		bb.flip();
	}

	@Override
	public void clear() {
		bb.clear();
	}
	
	@Override
	public void dispose()
	{
		bb.clear();
	}
	
	@Override
	public void limit(int newLimit)
	{
		bb.limit(newLimit);
	}

	@Override
	public int remaining() {
		return bb.remaining();
	}

	@Override
	public boolean hasRemaining() {
		return bb.hasRemaining();
	}
	
	private final int nextGetIndex(int nb) {                 
        int p = bb.position();
        bb.position(p + nb);
        return p;
    }
	
	@Override
	public byte get() {
		return get(nextGetIndex(1));
	}

	@Override
	public byte get(int i) {
		return bb.get(i);
	}

	@Override
	public String getString(int len)
	{
		if(remaining() < len) return null;
		
		byte[] buf = new byte[len];
		get(buf, 0, len);
		return new String(buf);
	}
	
	@Override
	public short getShort() {
		return getShort(nextGetIndex(2));
	}

	@Override
	public short getShort(int i) {
		return bb.getShort(i);
	}

	@Override
	public int getInt() {
		return getInt(nextGetIndex(4));
	}

	@Override
	public int getInt(int i) {
		return bb.getInt(i);
	}

	@Override
	public float getFloat(int i) {
		return bb.getFloat(i);
	}

	@Override
	public float getFloat() {
		return getFloat(nextGetIndex(4));
	}

	@Override
	public long getLong(int i) {
		return bb.getLong(i);
	}

	@Override
	public long getLong() {
		return getLong(nextGetIndex(8));
	}

	@Override
	public int position() {
		return bb.position();
	}

	@Override
	public void position(int newPosition) {
		bb.position(newPosition);
	}

	@Override
	public void get(byte[] dst) {
		get(dst, 0, dst.length);
	}

	@Override
	public void get(byte[] dst, int offset, int length) {
		bb.get(dst, offset, length);
	}
}
