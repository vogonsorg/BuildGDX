//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.desktop.audio;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_PAUSED;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_SOURCE_RELATIVE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import ru.m210projects.Build.Audio.Source;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class ALSource extends Source {

	private ALSoundDrv drv;
	public ALSource(ALSoundDrv drv, int bufferId, int sourceId) {
		super(bufferId, sourceId, 0);
		this.drv = drv;
	}

	@Override
	public int dispose() {
		drv.sourceManager.stopSound(this);
		return sourceId;
	}

	@Override
	public boolean isLooping() {
		return alGetSourcei(sourceId, AL_LOOPING) == AL_TRUE;
	}

	@Override
	public boolean isPlaying() {
		return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	@Override
	public void setPosition(float x, float y, float z) {
		alSource3f(sourceId, AL_POSITION, x, y, z);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setPosition " + error + ", values: ["+ x + ", " + y + ", " + z + "]", OSDTEXT_RED);
	}

	@Override
	public void setVolume(float volume) {
		volume *= drv.getVolume();
		volume = Math.min(Math.max(volume, 0.0f), 1.0f);
		alSourcef(sourceId, AL_GAIN, volume);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setVolume " + error + ", value is " + volume, OSDTEXT_RED);
	}

	@Override
	public void setPitch(float pitch) {
		pitch = Math.min(Math.max(pitch, 0.0f), 1.0f);
		alSourcef(sourceId, AL_PITCH, pitch);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setPitch " + error + ", value is " + pitch, OSDTEXT_RED);
	}

	@Override
	public void setGlobal(int num) {
		alSourcei(sourceId, AL_SOURCE_RELATIVE,  num);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setGlobal " + error + ", value is " + num, OSDTEXT_RED);
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public boolean isActive() {
		return isPlaying() && priority != 0 && !free;
	}

	@Override
	public void setLooping(boolean loop, int loopstart, int loopend) {
		if(!loop)
		{
			alSourcei(sourceId, AL_LOOPING, AL_FALSE);
			loopInfo.clear();
		} else {
			int start = 0, end = data.capacity();
			if(loopstart >= 0 && loopstart < data.capacity()) 
				start = loopstart;
			if(loopend < data.capacity()) 
				end = loopend;
			
			int bufferID = drv.buffers.get(bufferId);
			if(start > 0) {
				alSourcei(sourceId, AL_BUFFER, 0);
				alSourcei(sourceId, AL_LOOPING, AL_FALSE);
				alSourceQueueBuffers(sourceId, bufferID);
				drv.loopedSource.add(this);
				loopInfo.set(data, start, end, format, rate);
			} else {
				if(end > 0) data.limit(end);
				alSourcei(sourceId, AL_LOOPING, AL_TRUE);
				alSourcei(sourceId, AL_BUFFER,   bufferID );
			}
		}
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setLooping " + error, OSDTEXT_RED);
	}

	@Override
	public void play(float volume) {
		setVolume(volume);
		alSourcePlay(sourceId);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error play " + error, OSDTEXT_RED);
	}

	@Override
	public void stop() {
		alSourceStop(sourceId);
	}

	@Override
	public void pause() {
		alSourcePause(sourceId);
	}

	@Override
	public void resume() {
		if(alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED)
			alSourcePlay(sourceId);
	}
}
