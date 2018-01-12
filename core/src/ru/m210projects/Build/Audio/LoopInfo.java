package ru.m210projects.Build.Audio;

import java.nio.ByteBuffer;

public class LoopInfo {
	public boolean looped;
	public ByteBuffer data;
	public int sampleRate;
	public int format;
	public int start;
	public int end;
	
	public void set(ByteBuffer data, int start, int end, int format, int sampleRate)
	{
		this.start = start;
		this.end = end;
		this.data = data;
		this.format = format;
		this.sampleRate = sampleRate;
		this.looped = true;
	}
	
	public ByteBuffer getData()
	{
		data.position(start);
		data.limit(end);
		return data;
	}
	
	public void clear()
	{
		start = end = sampleRate = format = 0;
		data = null;
		looped = false;
	}
}
