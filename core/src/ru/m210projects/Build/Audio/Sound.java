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

import java.nio.ByteBuffer;

public interface Sound {
	public boolean init(int system, int kMaxSFXChannels);
	public boolean isInited();
	public void restart();
	public void dispose();
	public void setVolume(float vol);
	public void endLooping( Source source );
	public Source playRaw( ByteBuffer data, int length, int rate, int bits, int pitchoffset, int volume, int priority );
	public Source playLoopedRaw( ByteBuffer data, int length, int loopstart, int loopend, int rate, int bits, int pitchoffset, int vol, int priority );
	public int stopSound( Source source );
	public void stopAllSounds();
	public boolean isLooping(Source source);
	public boolean isPlaying(Source source);
	public void setListener(int x, int y, int z, int ang);
	public void setSourcePos(Source source, int x, int y, int z);
	public void setSourceVolume( Source source, int vol );
	public void setSourcePitch( Source source, float pitch );
	public void setGlobal(Source source, int num);
	public void setPriority(Source source, int priority);
	public boolean isActive(Source source);
	public void resetListener();
	public void setSystem(int system);
	public String getName();
	
	public int getReverb();
	public float getReverbDelay();
	public void setReverb(int enable);
	public void setReverbDelay(float delay);
	
	public void update();
	public boolean newMusic(String file);
	public void playMusic();
	public void stopMusic();
	public void pauseMusic();
	public boolean isPlaying();
	public void volumeMusic(int vol);
}
