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
// Music part of this file has been modified from 
// Nathan Sweet's LibGDX OpenALMusic by Alexander Makarov-[M210]

package ru.m210projects.Build.desktop.audio;

import static org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
import static org.lwjgl.openal.AL10.AL_BUFFERS_QUEUED;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_INVALID_VALUE;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alGetSourcef;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceUnqueueBuffers;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.AL11.AL_SEC_OFFSET;
import static ru.m210projects.Build.FileHandle.Cache1D.kExist;
import static ru.m210projects.Build.FileHandle.Cache1D.kGetBytes;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.backends.lwjgl.audio.OggInputStream;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.StreamUtils;

import ru.m210projects.Build.Audio.Source;
import ru.m210projects.Build.Audio.BMusic.Music;
import ru.m210projects.Build.Audio.BMusic.MusicSource;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.desktop.audio.ALSoundDrv.SourceManager;

public class ALMusicDrv implements Music {

	private MusicSource music;
	private float musicVolume = 1.0f;
	private boolean inited;
	
	protected IntBuffer musicBuffers;
	protected final int musicBufferCount = 3;
	
	private ALSoundDrv drv;
	public ALMusicDrv(ALSoundDrv drv) {
		this.drv = drv;
	}

	@Override
	public MusicSource newMusic(byte[] data) {
		if(drv.noDevice || data == null) return null;
		if(music != null) {
			if(music.isPlaying())
				music.stop();
			music = null;
		}
		
		try {
			music = new ALMusicSource(new Ogg.Music(drv.sourceManager, musicBuffers, data));
		} catch (Throwable e) {
			Console.Println("Can't load ogg file", OSDTEXT_RED);
			return null;
		}
		
		setVolume(musicVolume);
		return music;
	}

	@Override
	public MusicSource newMusic(String name) {
		if(!kExist(name, 0)) return null;
		return newMusic(kGetBytes(name, 0));
	}

	@Override
	public String getName() {
		return "OpenAL Music";
	}
	
	@Override
	public void dispose() {
		alDeleteBuffers(musicBuffers); 
	}

	@Override
	public boolean init() {
		if(!drv.isInited()) return false;
		inited = false;
		musicBuffers = BufferUtils.newIntBuffer(musicBufferCount);
		alGenBuffers(musicBuffers);
		
		if (alGetError() != AL_NO_ERROR) {
			Console.Println("OpenAL Music: Unabe to allocate audio buffers.", OSDTEXT_RED);
			return false;
		}
		
		inited = true;
		return true;
	}

	@Override
	public boolean isInited() {
		return drv.isInited() && inited;
	}

	@Override
	public void update() {
		if(music != null)
			music.update();
	}

	@Override
	public void setVolume(float volume) {
		musicVolume = volume;
		if(music != null)
			((ALMusicSource) music).setVolume(volume);
	}
}





/** @author Nathan Sweet */
abstract class OpenALMusic {
	
	static private final int bufferSize = 4096 * 10;
	static private final int bytesPerSample = 2;
	static private final byte[] tempBytes = new byte[bufferSize];
	static private final ByteBuffer tempBuffer = BufferUtils.newByteBuffer(bufferSize);

	private SourceManager sourceManager;
	private Source source = null;
	private int format, sampleRate;
	private boolean isLooping, isPlaying;
	private float renderedSeconds, secondsPerBuffer;
	protected byte[] data;
	private float musicVolume;
	private IntBuffer musicBuffers;
	
	public OpenALMusic (SourceManager sourceManager, IntBuffer ALbuffers, byte[] data) {
		this.sourceManager = sourceManager;
		this.data = data;
		this.musicBuffers = ALbuffers;
	}

	protected void setup (int channels, int sampleRate) {
		this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
		this.sampleRate = sampleRate;
		secondsPerBuffer = (float)bufferSize / bytesPerSample / channels / sampleRate;
	}

	public void play() {
		if (source == null) {
			source = sourceManager.obtainSource(Integer.MAX_VALUE);
			if (source == null) return;
			alSourcei(source.sourceId, AL_LOOPING, AL_FALSE);
			setVolume(musicVolume);
			source.flags |= Source.Locked;
			for (int i = 0; i < musicBuffers.capacity(); i++) {
				int bufferID = musicBuffers.get(i);
				if (!fill(bufferID)) break;
				renderedSeconds = 0;
				alSourceQueueBuffers(source.sourceId, bufferID);
			}
			if (alGetError() != AL_NO_ERROR) {
				stop();
				return;
			}
		}
		alSourcePlay(source.sourceId);
		isPlaying = true;
	}

	public void stop() {
		if (source == null) return;
		reset();
		source.flags &= ~Source.Locked;
		sourceManager.stopSound(source);
		source = null;
		renderedSeconds = 0;
		isPlaying = false;
	}

	public void pause() {
		if (source != null) alSourcePause(source.sourceId);
		isPlaying = false;
	}

	public boolean isPlaying() {
		if (source == null) return false;
		return isPlaying;
	}

	public void setLooping(boolean isLooping) {
		this.isLooping = isLooping;
	}

	public boolean isLooping() {
		return isLooping;
	}

	public void setVolume(float volume) {
		this.musicVolume = volume;
		if (source != null) 
			alSourcef(source.sourceId, AL_GAIN, volume);
	}

	public float getVolume() {
		return this.musicVolume;
	}

	public float getPosition() {
		if (source == null) return 0;
		return renderedSeconds + alGetSourcef(source.sourceId, AL_SEC_OFFSET);
	}
	
	public int getChannels() {
		return format == AL_FORMAT_STEREO16 ? 2 : 1;
	}

	public int getRate() {
		return sampleRate;
	}

	public void update() {
		if (source == null) return;
		boolean end = false;
		int buffers = alGetSourcei(source.sourceId, AL_BUFFERS_PROCESSED);
		while (buffers-- > 0) {
			int bufferID = alSourceUnqueueBuffers(source.sourceId);
			if (bufferID == AL_INVALID_VALUE) break;
			renderedSeconds += secondsPerBuffer;
			if (end) continue;
			if (fill(bufferID)) 
				alSourceQueueBuffers(source.sourceId, bufferID);
			else end = true;
		}
		if (end && alGetSourcei(source.sourceId, AL_BUFFERS_QUEUED) == 0) {
			stop();
		}
		
		// A buffer underflow will cause the source to stop.
		if (isPlaying && alGetSourcei(source.sourceId, AL_SOURCE_STATE) != AL_PLAYING) alSourcePlay(source.sourceId);
	}

	private boolean fill (int bufferID) {
		tempBuffer.clear();
		int length = read(tempBytes);
		if (length <= 0) {
			if (isLooping) {
				reset();
				renderedSeconds = 0;
				length = read(tempBytes);
				if (length <= 0) return false;
			} else
				return false;
		}
		tempBuffer.put(tempBytes, 0, length).flip();
		alBufferData(bufferID, format, tempBuffer, sampleRate);
		return true;
	}
	
	public abstract int read(byte[] buffer);
	
	public abstract void reset ();
}

class Ogg {
	static public class Music extends OpenALMusic {
		private OggInputStream input;
		public Music(SourceManager sourceManager, IntBuffer ALbuffers, byte[] data) {
			super(sourceManager, ALbuffers, data);
			input = new OggInputStream(new ByteArrayInputStream(data, 0, data.length));
			setup(input.getChannels(), input.getSampleRate());
		}

		public int read(byte[] buffer) {
			if (input == null) {
				input = new OggInputStream(new ByteArrayInputStream(data, 0, data.length));
				setup(input.getChannels(), input.getSampleRate());
			}
			return input.read(buffer);
		}

		public void reset() {
			StreamUtils.closeQuietly(input);
			input = null;
		}
	}
}
