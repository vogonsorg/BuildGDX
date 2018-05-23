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

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import java.nio.ByteBuffer;

import ru.m210projects.Build.OnSceenDisplay.Console;

public class DummySound implements Sound {

	@Override
	public boolean init(int system, int maxChannels) {
		Console.Println(getName() + " initialized", OSDTEXT_GOLD);
		return true;
	}

	@Override
	public void restart() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setVolume(float vol) {
	}

	@Override
	public void endLooping(Source source) {
	}

	@Override
	public Source playRaw(ByteBuffer data, int length, int rate, int bits,
			int pitchoffset, int volume, int priority) {
		return null;
	}

	@Override
	public Source playLoopedRaw(ByteBuffer data, int length, int loopstart,
			int loopend, int rate, int bits, int pitchoffset, int vol, int priority) {
		return null;
	}

	@Override
	public int stopSound(Source source) {
		return 0;
	}

	@Override
	public void stopAllSounds() {
	}

	@Override
	public boolean isLooping(Source source) {
		return false;
	}

	@Override
	public boolean isPlaying(Source source) {
		return false;
	}

	@Override
	public void setListener(int x, int y, int z, int ang) {
	}

	@Override
	public void setSourcePos(Source source, int x, int y, int z) {
	}

	@Override
	public void setSourceVolume(Source source, int vol) {
	}

	@Override
	public void setSourcePitch(Source source, float pitch) {
	}

	@Override
	public void setGlobal(Source source, int num) {
	}

	@Override
	public void setPriority(Source source, int priority) {
	}

	@Override
	public boolean isActive(Source source) {
		return false;
	}

	@Override
	public void setSystem(int system) {
	}

	@Override
	public String getName() {
		return "Dummy sound";
	}

	@Override
	public void resetListener() {
	}

	@Override
	public boolean isInited() {
		return true;
	}
	
	@Override
	public void update() {
		
	}

	@Override
	public boolean newMusic(String file) {
		return false;
	}

	@Override
	public void playMusic() {
		
	}

	@Override
	public void stopMusic() {
		
	}

	@Override
	public void pauseMusic() {
		
	}

	@Override
	public void volumeMusic(int vol) {
		
	}

	@Override
	public boolean isPlaying() {
		return false;
	}
	
	@Override
	public int getReverb() {
		return 0;
	}

	@Override
	public float getReverbDelay() {
		return 0;
	}

	@Override
	public void setReverb(int enable) {

	}

	@Override
	public void setReverbDelay(float delay) {

	}
}
