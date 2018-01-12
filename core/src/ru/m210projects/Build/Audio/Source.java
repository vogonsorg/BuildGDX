package ru.m210projects.Build.Audio;

public class Source implements Comparable<Source> {
	
	public static final int Locked = 1;
	
	public int bufferId;
	public int sourceId;
	public int priority;
	public boolean free;
	public int type;
	public int flags;
	public int channel;
	
	public LoopInfo loopInfo;
	
	public Source(int bufferId, int sourceId, int priority)
	{
		this.bufferId = bufferId;
		this.sourceId = sourceId;
		this.priority = priority;
		this.free = true;
		this.channel = -1;
		loopInfo = new LoopInfo();
	}

	@Override
	public int compareTo(Source source) {
		if((source.flags & Source.Locked) != 0) return -1;
		if(source.free) return 1;
		return (this.priority - source.priority);
	}
}
