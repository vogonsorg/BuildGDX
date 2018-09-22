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

import ru.m210projects.Build.Audio.BMusic.Music;

public interface Sound {
	//Driver
	public boolean init(int system, int kMaxSFXChannels, int softResampler);
	public void uninit();
	public boolean isInited();
	public void destroy();
	public String getName();
	public Music getDigitalMusic();
	
	//EFX
	public float getReverb();
	public void setReverb(boolean enable, float delay);
	public void setListener(int x, int y, int z, int ang);
	public void resetListener();
	
	///Soft resampler
	public String getSoftResamplerName(int num);
	public int getSoftResampler();
	public void setSoftResampler(int num);
	
	//Source handler
	public float getVolume();
	public void setVolume(float vol);
	public void stopAllSounds();
	public boolean isAvailable(int priority);
	public Source newSound(ByteBuffer data, int rate, int bits, int priority);
	public void update();
}
