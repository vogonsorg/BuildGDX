package ru.m210projects.Build.Types;

public class Palette {
	
	private byte[] bytes;
	private int[] values;
	
	public Palette() {
		bytes = new byte[768];
		values = new int[256];
	}
	
	public void update(byte[] palette, boolean buildPalette)
	{
		System.arraycopy(palette, 0, bytes, 0, palette.length);
		if(buildPalette)
			for(int i = 0; i < palette.length; i++) 
				bytes[i] <<= 2;
		
		int p = 0;
		for(int i = 0; i < palette.length / 3; i++) 
			values[i] = (bytes[p++] & 0xFF) | ( (bytes[p++] & 0xFF) << 8 ) | ( (bytes[p++] & 0xFF) << 16 ) | (255 << 24);
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	public int getRed(int index)
	{
		return (values[index] & 0x000000FF);
	}
	
	public int getGreen(int index)
	{
		return (values[index] & 0x0000FF00) >> 8;
	}
	
	public int getBlue(int index)
	{
		return (values[index] & 0x00FF0000) >> 16;
	}
	
	public int getRGB(int index)
	{
		return values[index] & 0x00FFFFFF;
	}
	
	public int getRGBA(int index, byte alphaMask)
	{
		return getRGB(index) | (alphaMask << 24);
	}
	
	public int getRGBA(int index)
	{
		return values[index];
	}
}
