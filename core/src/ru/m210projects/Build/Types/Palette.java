package ru.m210projects.Build.Types;

public class Palette {
	public int r;
	public int g;
	public int b;
	public int f;
	
	public Palette() { }
	
	public Palette(int r, int g, int b, int f)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.f = f;
		
	}
	public int[] array;
	
	public int[] toArray() {
		if(array == null)
			array = new int[4];
		array[0] = r;
		array[1] = g;
		array[2] = b;
		array[3] = f;
		
		return array;
	}
}
