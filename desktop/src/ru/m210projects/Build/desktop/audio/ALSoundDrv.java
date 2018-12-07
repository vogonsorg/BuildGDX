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
//

package ru.m210projects.Build.desktop.audio;

import static ru.m210projects.Build.Audio.ALAudio.AL_BUFFER;
import static ru.m210projects.Build.Audio.ALAudio.AL_FALSE;
import static ru.m210projects.Build.Audio.ALAudio.AL_GAIN;
import static ru.m210projects.Build.Audio.ALAudio.AL_LOOPING;
import static ru.m210projects.Build.Audio.ALAudio.AL_NO_ERROR;
import static ru.m210projects.Build.Audio.ALAudio.AL_PITCH;
import static ru.m210projects.Build.Audio.ALAudio.AL_POSITION;
import static ru.m210projects.Build.Audio.ALAudio.AL_SOURCE_RELATIVE;
import static ru.m210projects.Build.Audio.ALAudio.AL_TRUE;
import static ru.m210projects.Build.Audio.ALAudio.AL_NONE;
import static ru.m210projects.Build.Audio.ALAudio.AL_ORIENTATION;
import static ru.m210projects.Build.Audio.ALAudio.AL_VELOCITY;
import static ru.m210projects.Build.Audio.ALAudio.AL_FORMAT_STEREO16;
import static ru.m210projects.Build.Audio.ALAudio.AL_FORMAT_MONO16;
import static ru.m210projects.Build.Audio.ALAudio.AL_FORMAT_STEREO8;
import static ru.m210projects.Build.Audio.ALAudio.AL_FORMAT_MONO8;

import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.m210projects.Build.Audio.ALAudio;
import ru.m210projects.Build.Audio.Sound;
import ru.m210projects.Build.Audio.Source;
import ru.m210projects.Build.Audio.BMusic.Music;
import ru.m210projects.Build.OnSceenDisplay.Console;
import com.badlogic.gdx.utils.BufferUtils;

public class ALSoundDrv implements Sound {

	protected boolean noDevice = true;
	protected static final int MONO = 0;
	private final static FloatBuffer NULLVECTOR = BufferUtils.newFloatBuffer(3);
	private final static FloatBuffer orientation = (FloatBuffer)BufferUtils.newFloatBuffer(6);
	private final float[] deforientation = new float[] {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};

	protected int system;
	protected IntBuffer buffers;

	protected SourceManager sourceManager;
	private float soundVolume = 0.5f;
	
	private String name = "OpenAL";
	protected List<Source> loopedSource = new ArrayList<Source>();
	
	private boolean alReverbEnable = false;
	private float alReverbDelay = 0.0f;
	
	private int alCurrentSoftResampler;

	private ALMusicDrv mus;
	private ALAudio al;
	
	public ALSoundDrv(ALAudio al)
	{
		this.al = al;
		this.mus = new ALMusicDrv(this);
	}
	
	public ALAudio getALAudio()
	{
		return al;
	}

	@Override
	public boolean init(int system, int maxChannels, int softResampler) {
		if(al == null) {
			noDevice = true;
			return false;
		}

		noDevice = false;
		
		al.alDistanceModel( AL_NONE );
		orientation.put(deforientation).flip();
		sourceManager = new SourceManager(maxChannels);
		buffers = BufferUtils.newIntBuffer(maxChannels);
		al.alGenBuffers(buffers);

		resetListener();
		this.system = system;

		Console.Println(al.getName() + " initialized", OSDTEXT_GOLD);
		Console.Println("\twith max voices: " + sourceManager.getSourcesNum(), OSDTEXT_GOLD);
		Console.Println("\tOpenAL version: " + al.getVersion(), OSDTEXT_GOLD); 	

		if(al.alIsEFXSupport())
			Console.Println("ALC_EXT_EFX enabled.");	
		else Console.Println("ALC_EXT_EFX not support!");	 
		
		if(al.alIsSoftResamplerSupport()) {
			Console.Println("AL_SOFT_Source_Resampler enabled. Using resampler: " + al.alGetSoftResamplerName(softResampler));	
			setSoftResampler(softResampler);
		}
		else Console.Println("AL_SOFT_Source_Resampler not support!");	 

		int error = al.alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Init Error " + error, OSDTEXT_RED);
		
		mus.init();
		
		loopedSource.clear();
		return true;
	}
	
	@Override
	public boolean isInited() {
		return !noDevice;
	}
	
	@Override
	public void destroy() {
		uninit();
		if(al != null)
			al.dispose();
	}

	@Override
	public String getName() {
		if(noDevice) return name;
		return name + al.getVersion();
	}

	@Override
	public void setListener(int x, int y, int z, int ang) {
		if (noDevice) return;
		if(system != MONO) 
		{
			al.alListener3f(AL_POSITION, x, y, z);
			double angle = (ang * 2 * Math.PI) / 2048;
			orientation.put(0, (float)Math.cos(angle));
			orientation.put(1, 0);
			orientation.put(2, (float)Math.sin(angle));
			orientation.rewind();
			al.alListener ( AL_ORIENTATION, orientation );
		} else al.alListener (AL_POSITION, NULLVECTOR);
		
		int error = al.alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error setListener " + error, OSDTEXT_RED);
	}

	@Override
	public void resetListener() {
		if (noDevice) return;
		orientation.put(deforientation).flip();
		al.alListener(AL_ORIENTATION, orientation);
		al.alListener(AL_VELOCITY, NULLVECTOR);
		al.alListener(AL_POSITION, NULLVECTOR);
	}
	
	@Override
	public float getVolume() {
		return soundVolume;
	}
	
	@Override
	public void setVolume(float vol) {
		this.soundVolume = Math.min(Math.max(vol, 0.0f), 1.0f);
	}
	
	@Override
	public Source newSound(ByteBuffer data, int rate, int bits, int priority) {
		if(noDevice) return null;
		
		Source source = sourceManager.obtainSource(priority);
		if (source == null) return null;

		if(loopedSource.size() > 0 && source.loopInfo.looped)
			loopedSource.remove(source);
		source.loopInfo.clear();
		
		int sourceId = source.sourceId;
		al.alSourcei(sourceId, AL_LOOPING, AL_FALSE);
		
		al.setSourceReverb(sourceId, alReverbEnable, alReverbDelay);
		al.setSourceSoftResampler(sourceId, alCurrentSoftResampler);

		source.setVolume(0.0f);

		int format = toALFormat(0, bits);
		if(format == -1) {
			Console.Println("OpenAL Error wrong bits: " + bits, OSDTEXT_RED);
			source.dispose();
			return null;
		}
		
		source.format = format;
		source.rate = rate;
		source.data = data;

		int bufferID = buffers.get(source.bufferId);
		al.alBufferData(bufferID, format, data, rate);
		al.alSourcei(sourceId, AL_BUFFER,   bufferID );
		
		int error = al.alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error newSound " + error, OSDTEXT_RED);
		
		return source;
	}

	@Override
	public void stopAllSounds() {
		if (noDevice) return;
		sourceManager.stopAllSounds();
		loopedSource.clear();
	}
	
	@Override
	public float getReverb() {
		return alReverbDelay;
	}

	@Override
	public void setReverb(boolean enable, float delay) {
		if(noDevice) return;
		alReverbEnable = enable;
		if(enable) 
			alReverbDelay = BClipRange(delay, 0.0f, 10.0f);
		else alReverbDelay = 0;

		Iterator<Source> it = sourceManager.iterator();
	    while(it.hasNext()) {
	    	Source s = (Source)it.next();
	    	al.setSourceReverb(s.sourceId, alReverbEnable, alReverbDelay);
	    }
	}
	
	@Override
	public String getSoftResamplerName(int num) {
		if(noDevice) return "Not support";
		return al.alGetSoftResamplerName(num);
	}

	@Override
	public int getCurrentSoftResampler() {
		return alCurrentSoftResampler;
	}
	
	@Override
	public int getNumResamplers() {
		if(noDevice) return 1;
		return al.alGetNumResamplers();
	}

	@Override
	public void setSoftResampler(int num) {
		if(noDevice) return;
		
		alCurrentSoftResampler = num;
		Iterator<Source> it = sourceManager.iterator();
	    while(it.hasNext()) {
	    	Source s = (Source)it.next();
	    	al.setSourceSoftResampler(s.sourceId, alCurrentSoftResampler);
	    }
	}

	protected int toALFormat(int channels, int bits)
	{
		boolean stereo = (channels > 1);

	   	switch (bits) {
	    	case 16:
                if (stereo)
                	return AL_FORMAT_STEREO16;
                else
                	return AL_FORMAT_MONO16;
	        case 8:
                if (stereo)
                	return AL_FORMAT_STEREO8;
                else
                	return AL_FORMAT_MONO8;
	        default:
	        	return -1;
		}
	}
	
	protected class SourceManager extends java.util.PriorityQueue<Source>
	{
		private static final long serialVersionUID = 1L;
		private Source[] allSources;
		
		public SourceManager(int maxSources) {
			allSources = new Source[maxSources];

			clear();
			for (int i = 0; i < maxSources; i++) {
				int sourceId = al.alGenSources();
				if (al.alGetError() != AL_NO_ERROR) break;
				allSources[i] = new ALSource(ALSoundDrv.this, i, sourceId);

				// set default values for AL sources
			    al.alSourcef (sourceId, AL_GAIN, 0.0f);
			    al.alSourcef (sourceId, AL_PITCH, 1.0f);
			    al.alSourcei (sourceId, AL_SOURCE_RELATIVE,  AL_FALSE);
			    al.alSource  (sourceId, AL_VELOCITY, NULLVECTOR);
			    al.alSourcei (sourceId, AL_LOOPING, AL_FALSE);
			    
			    add(allSources[i]);
			}
		}
		
		public int getSourcesNum()
		{
			return this.size();
		}

		public void freeSource(Source source)
		{
			if(remove(source)) {
				source.free = true;
				source.priority = 0;
				source.flags = 0;
				source.loopInfo.clear();
				source.format = 0;
				source.rate = 0;
				source.data = null;
				
				add(source);
			}
		}
		
		public Source obtainSource(int priority)
		{
			for(int i = 0; i < allSources.length; i++) {
				if(allSources[i] != null && !allSources[i].free && !allSources[i].isPlaying() && allSources[i].flags != Source.Locked)
					freeSource(allSources[i]);
			}
			
//			System.out.println("obtainSource()");
//			Iterator<Source> it = this.iterator();
//		    while(it.hasNext()) {
//		      Source obj = (Source)it.next();
//		      System.out.println(obj.sourceId + " " + obj.free + " " + obj.priority + " " + obj.flags);
//		    }
//		    System.out.println();

			if(element().priority < priority || !element().isPlaying())
			{
				Source source = remove();
				int sourceId = source.sourceId;
				source.priority = priority;
				source.free = false;
				al.alSourceStop(sourceId);
				al.alSourcei(sourceId, AL_BUFFER, 0);
				al.alSourcef(sourceId, AL_GAIN, 0.0f);
				al.alSourcef(sourceId, AL_PITCH, 1.0f);
				al.alSource3f(sourceId, AL_POSITION, 0, 0, 0);
				al.alSourcei (sourceId, AL_SOURCE_RELATIVE,  AL_FALSE);
				add(source);
				
				return source;
			} else 
				return null;
		}

		public int stopSound(Source source) {
			if(source.flags == Source.Locked) 
				return -1;
			
			al.alSourceStop(source.sourceId);
			al.alSourcei(source.sourceId, AL_BUFFER, 0);
			freeSource(source);
			return source.sourceId;
		}

		public void stopAllSounds() {
			for (int i = 0, n = allSources.length; i < n; i++) {
				Source source = allSources[i];
				if(source == null) break; //max sources
				stopSound(source);
			}
		}
		
		public void dispose() {
			for (int i = 0, n = allSources.length; i < n; i++) {
				if(allSources[i] == null) break; //max sources
				allSources[i].dispose();
				al.alDeleteSources(allSources[i].sourceId);
				allSources[i].sourceId = -1;
				allSources[i].bufferId = -1;
				allSources[i] = null;
			}
		}
	}
	
	@Override
	public void update() {
		if(noDevice) return;
		
		mus.update();

		int error = al.alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error " + error, OSDTEXT_RED);

		Iterator<Source> i = loopedSource.iterator();
		while (i.hasNext()) {
			Source s = i.next();
			if(!s.isPlaying() && s.loopInfo.looped) {
				int bufferID = al.alSourceUnqueueBuffers(s.sourceId);
				al.alBufferData(bufferID, s.loopInfo.format, s.loopInfo.getData(), s.loopInfo.sampleRate);
				al.alSourceQueueBuffers(s.sourceId, bufferID);
				al.alSourcei(s.sourceId, AL_LOOPING, AL_TRUE);
				al.alSourcePlay(s.sourceId);
				i.remove();
			} 
		}
	}

	@Override
	public Music getDigitalMusic() {
		return mus;
	}

	@Override
	public boolean isAvailable(int priority) {
		if(noDevice) return false;
		return sourceManager.element().priority < priority || !sourceManager.element().isPlaying();
	}

	@Override
	public void uninit() {
		if(noDevice) return;
		
		sourceManager.dispose();
		al.alDeleteBuffers(buffers);
		mus.dispose();
		sourceManager = null;
		buffers = null;
		noDevice = true;
		loopedSource.clear();
	}
}



