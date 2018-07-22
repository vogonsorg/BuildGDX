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
// Music part of this file has been modified from 
// Nathan Sweet's LibGDX OpenALMusic by Alexander Makarov-[M210]

package ru.m210projects.Build.desktop.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.EFX10.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC10;

import ru.m210projects.Build.Audio.Sound;
import ru.m210projects.Build.Audio.Source;
import ru.m210projects.Build.Audio.BMusic.Music;
import ru.m210projects.Build.OnSceenDisplay.Console;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
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
	
	private String name = "OpenAL Soft";
	protected List<Source> loopedSource = new ArrayList<Source>();
	
	private int alEffectSlot = -1;
	private int alEffect = -1;
	private boolean alReverbEnable = false;
	private float alReverbDelay = 0.0f;
	private float alDefReverbDelay;
	private ALMusicDrv mus;
	
	public ALSoundDrv()
	{
		LwjglNativesLoader.load();
		LwjglApplicationConfiguration.disableAudio = true; //Disable Gdx Audio
		
		this.mus = new ALMusicDrv(this);
	}

	@Override
	public boolean init(int system, int maxChannels) {
		try {
			AL.create();
			noDevice = false;
			
			alDistanceModel( AL_NONE );
			orientation.put(deforientation).flip();
			sourceManager = new SourceManager(maxChannels);
			buffers = BufferUtils.newIntBuffer(maxChannels);
			alGenBuffers(buffers);

			resetListener();
			this.system = system;

			name = ALC10.alcGetString(AL.getDevice(), ALC10.ALC_DEVICE_SPECIFIER);

			Console.Println(name + " initialized", OSDTEXT_GOLD);
			Console.Println("\twith max voices: " + maxChannels, OSDTEXT_GOLD);
			Console.Println("\tOpenAL version: " + alGetString(AL_VERSION), OSDTEXT_GOLD); 	
	
			if (!ALC10.alcIsExtensionPresent(AL.getDevice(), ALC_EXT_EFX_NAME)) 
				Console.Println("No ALC_EXT_EFX supported by driver.", OSDTEXT_RED);
			else {
				alEffectSlot = alGenAuxiliaryEffectSlots();
				alEffect = alGenEffects();
				
				alEffecti(alEffect, AL_EFFECT_TYPE, AL_EFFECT_REVERB);
				alDefReverbDelay = alGetEffectf(alEffect, AL_REVERB_DECAY_TIME);
	
				if(alGetError() == AL_NO_ERROR)
					Console.Println("ALC_EXT_EFX enabled.");	
				else Console.Println("ALC_EXT_EFX error!", OSDTEXT_RED);	
			}

			//AL_NUM_RESAMPLERS_SOFT
//			float ridx = alGetInteger(SOFTSourceResampler.AL_DEFAULT_RESAMPLER_SOFT);

			int error = alGetError();
			if(error != AL_NO_ERROR) 
				Console.Println("OpenAL Init Error " + error, OSDTEXT_RED);
			
			mus.init();
			
			loopedSource.clear();
			return true;
		} catch (Exception ex) {
			Console.Println("Unable to initialize OpenAL! - " + ex.getLocalizedMessage(), OSDTEXT_RED);
			noDevice = true;
			return false;
		}
	}
	
	@Override
	public boolean isInited() {
		return !noDevice;
	}
	
	@Override
	public void dispose() {
		if (noDevice || !AL.isCreated()) return;
		sourceManager.dispose();
		alDeleteBuffers(buffers);
		mus.dispose();
		alDeleteEffects(alEffect);
		alDeleteAuxiliaryEffectSlots(alEffectSlot);

		sourceManager = null;
		buffers = null;
		AL.destroy();
		while (AL.isCreated()) {
			try {
				Thread.sleep(10);
				} catch (InterruptedException e) {
			}
		}
		noDevice = true;
		loopedSource.clear();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setListener(int x, int y, int z, int ang) {
		if (noDevice) return;
		if(system != MONO) 
		{
			alListener3f(AL_POSITION, x, y, z);
			double angle = (ang * 2 * Math.PI) / 2048;
			orientation.put(0, (float)Math.cos(angle));
			orientation.put(1, 0);
			orientation.put(2, (float)Math.sin(angle));
			orientation.rewind();
			alListener ( AL_ORIENTATION, orientation );
		} else alListener (AL_POSITION, NULLVECTOR);
	}

	@Override
	public void resetListener() {
		if (noDevice) return;
		orientation.put(deforientation).flip();
		alListener(AL_ORIENTATION, orientation);
		alListener(AL_VELOCITY, NULLVECTOR);
		alListener(AL_POSITION, NULLVECTOR);
	}
	
	@Override
	public float getVolume() {
		return soundVolume;
	}
	
	@Override
	public void setVolume(float vol) {
		if (noDevice) return;
		this.soundVolume = Math.min(Math.max(vol, 0.0f), 1.0f);
	}
	
	@Override
	public Source newSound(ByteBuffer data, int rate, int bits, int priority) {
		Source source = sourceManager.obtainSource(priority);
		if (source == null) return null;

		if(loopedSource.size() > 0 && source.loopInfo.looped)
			loopedSource.remove(source);
		source.loopInfo.clear();
		
		int sourceId = source.sourceId;
		alSourcei(sourceId, AL_LOOPING, AL_FALSE);
		
		setSourceReverb(sourceId, alReverbEnable, alReverbDelay);

		source.setVolume(0.0f);

		int format = toALFormat(0, bits);
		source.format = format;
		source.rate = rate;
		source.data = data;
		
		int bufferID = buffers.get(source.bufferId);
		alBufferData(bufferID, format, data, rate);
		alSourcei(sourceId, AL_BUFFER,   bufferID );
		
		return source;
	}

	@Override
	public void stopAllSounds() {
		if (noDevice) return;
		sourceManager.stopAllSounds();
		loopedSource.clear();
	}
	
	@Override
	public int getReverb() {
		return alReverbEnable?1:0;
	}

	@Override
	public float getReverbDelay() {
		return alReverbDelay;
	}

	@Override
	public void setReverb(int enable) {
		if(!isInited()) return;
		alReverbEnable = (enable == 1);
		alReverbDelay = alDefReverbDelay;

		Iterator<Source> it = sourceManager.iterator();
	    while(it.hasNext()) {
	    	Source s = (Source)it.next();
	    	setSourceReverb(s.sourceId, alReverbEnable, alReverbDelay);
	    }
	}

	@Override
	public void setReverbDelay(float delay) {
		if(!isInited()) return;
		alReverbDelay = delay;
		
		Iterator<Source> it = sourceManager.iterator();
	    while(it.hasNext()) {
	    	Source s = (Source)it.next();
	    	setSourceReverb(s.sourceId, alReverbEnable, alReverbDelay);
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
	
	protected void setSourceReverb(int sourceId, boolean enable, float delay)
	{
		if(alEffect == -1)
			return;
		
		if(enable)
		{
			 alEffectf(alEffect, AL_REVERB_DECAY_TIME, alReverbDelay);
			 alAuxiliaryEffectSloti(alEffectSlot, AL_EFFECTSLOT_EFFECT, alEffect);
		     alSource3i(sourceId, AL_AUXILIARY_SEND_FILTER, alEffectSlot, 0, AL_FILTER_NULL);
		} 
		else
		{
			alAuxiliaryEffectSloti(alEffectSlot, AL_EFFECTSLOT_EFFECT, AL_EFFECT_NULL);
			alSource3i(sourceId, AL_AUXILIARY_SEND_FILTER, AL_EFFECTSLOT_NULL, 0, AL_FILTER_NULL);
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
				int sourceId = alGenSources();
				if (alGetError() != AL_NO_ERROR) break;
				allSources[i] = new ALSource(ALSoundDrv.this, i, sourceId);

				// set default values for AL sources
			    alSourcef (sourceId, AL_GAIN, 0.0f);
			    alSourcef (sourceId, AL_PITCH, 1.0f);
			    alSourcei (sourceId, AL_SOURCE_RELATIVE,  AL_FALSE);
			    alSource  (sourceId, AL_VELOCITY, NULLVECTOR);
			    alSourcei (sourceId, AL_LOOPING, AL_FALSE);
			    
			    add(allSources[i]);
			}
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
				alSourceStop(sourceId);
				alSourcei(sourceId, AL_BUFFER, 0);
				alSourcef(sourceId, AL_GAIN, 0);
				alSourcef(sourceId, AL_PITCH, 1);
				alSource3f(sourceId, AL_POSITION, 0, 0, 0);
				alSourcei (sourceId, AL_SOURCE_RELATIVE,  AL_FALSE);
				add(source);
				
				return source;
			} else 
				return null;
		}


		public int stopSound(Source source) {
			if(source.flags == Source.Locked) 
				return -1;
			
			alSourceStop(source.sourceId);
			alSourcei(source.sourceId, AL_BUFFER, 0);
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
				alDeleteSources(allSources[i].sourceId);
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

		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error " + error, OSDTEXT_RED);

		Iterator<Source> i = loopedSource.iterator();
		while (i.hasNext()) {
			Source s = i.next();
			if(!s.isPlaying() && s.loopInfo.looped) {
				int bufferID = alSourceUnqueueBuffers(s.sourceId);
				alBufferData(bufferID, s.loopInfo.format, s.loopInfo.getData(), s.loopInfo.sampleRate);
			   	alSourceQueueBuffers(s.sourceId, bufferID);
			   	alSourcei(s.sourceId, AL_LOOPING, AL_TRUE);
				alSourcePlay(s.sourceId);
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
		return sourceManager.element().priority < priority || !sourceManager.element().isPlaying();
	}
}



