// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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
