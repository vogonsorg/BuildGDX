//This file is part of BuildGDX.
//Copyright (C) 2017-2020  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.android;

import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.media.MediaPlayer;
import ru.m210projects.Build.Audio.Music;
import ru.m210projects.Build.Audio.Sound;
import ru.m210projects.Build.Audio.Source;
import ru.m210projects.Build.Audio.SourceCallback;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class AndoidSoundAPI23 implements Sound {
	
	private String name = "Android Sound Module";
	private boolean inited;
	private MediaPlayer mediaPlayer;
	private float soundVolume = 0.5f;
	protected SourceManager sourceManager;

	protected List<Source> loopedSource = new ArrayList<Source>();
	
	@Override
	public boolean init(SystemType system, int maxChannels, int softResampler) {
		
		if(maxChannels < 1) maxChannels = 1;
		
		this.sourceManager = new SourceManager(maxChannels);
		this.mediaPlayer = new MediaPlayer();
		inited = true;
		
		Console.Println(getName() + " initialized", OSDTEXT_GOLD);
		Console.Println("\twith max voices: " + sourceManager.getSourcesNum(), OSDTEXT_GOLD);
	
		Console.Println("ALC_EXT_EFX not supported!");	 
		Console.Println("AL_SOFT_Source_Resampler not supported!");	 

//		getDigitalMusic().init();
		
		loopedSource.clear();

		return true;
	}

	@Override
	public void uninit() {
		/* nothing */ }

	@Override
	public boolean isInited() {
		return inited;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Music getDigitalMusic() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getSoftResamplerName(int num) {
		return "Not supported";
	}

	@Override
	public int getNumResamplers() {
		return 1;
	}

	@Override
	public float getVolume() {
		return soundVolume;
	}

	@Override
	public void setVolume(float volume) {
		soundVolume = volume;
		mediaPlayer.setVolume(volume, volume);
	}
	
	@Override
	public void update() {
//		mus.update();

		if(loopedSource.size() > 0) {
			Iterator<Source> i = loopedSource.iterator();
			while (i.hasNext()) {
				Source s = i.next();
				if(!s.isPlaying() && s.loopInfo.looped) {
//					int bufferID = al.alSourceUnqueueBuffers(s.sourceId);
//					al.alBufferData(bufferID, s.loopInfo.format, s.loopInfo.getData(), s.loopInfo.sampleRate);
//					al.alSourceQueueBuffers(s.sourceId, bufferID);
//					al.alSourcei(s.sourceId, AL_LOOPING, AL_TRUE);
//					al.alSourcePlay(s.sourceId);
					i.remove();
				} 
			}
		}

		sourceManager.update();
	}

	@Override
	public void dispose() {
		/* nothing */ }
	
	@Override
	public float getReverb() {
		return 0;
	}

	@Override
	public void setReverb(boolean enable, float delay) {
		/* nothing */ }

	@Override
	public void setListener(int x, int y, int z, int ang) {
	/* nothing */ }

	@Override
	public void resetListener() {
	/* nothing */ }

	@Override
	public int getCurrentSoftResampler() {
		return 0;
	}

	@Override
	public void setSoftResampler(int num) {
	/* nothing */ }

	@Override
	public void stopAllSounds() {
		sourceManager.stopAllSounds();
		loopedSource.clear();
	}

	@Override
	public boolean isAvailable(int priority) {
		return sourceManager.element().priority < priority || !sourceManager.element().isPlaying();
	}

	@Override
	public Source newSound(ByteBuffer data, int rate, int bits, int channels, int priority) {
		if(data == null) return null;
		
		Source source = sourceManager.obtainSource(priority);
		if (source == null) return null;

		if(loopedSource.size() > 0 && source.loopInfo.looped)
			loopedSource.remove(source);
		source.loopInfo.clear();
		
		int sourceId = source.sourceId;
//		al.alSourcei(sourceId, AL_LOOPING, AL_FALSE);
//		
//		al.setSourceReverb(sourceId, alReverbEnable, alReverbDelay);
//		al.setSourceSoftResampler(sourceId, alCurrentSoftResampler);

		source.setVolume(0.0f);
//		source.format = format;
		source.rate = rate;
		source.data = data;

//		int bufferID = buffers.get(source.bufferId);
//		al.alBufferData(bufferID, format, data, rate);
//		al.alSourcei(sourceId, AL_BUFFER,   bufferID );

		return source;
	}

	protected class SourceManager extends java.util.PriorityQueue<Source>
	{
		private static final long serialVersionUID = 1L;
		private Source[] allSources;
		
		public SourceManager(int maxSources) {
			allSources = new Source[maxSources];

			clear();
			for (int i = 0; i < maxSources; i++) {
				allSources[i] = new AndroidSource(AndoidSoundAPI23.this, i);
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
				if(source.callback != null) {
					source.callback.run(source.channel);
					source.callback = null;
				}

				add(source);
			}
		}
		
		protected void update()
		{
			for(int i = 0; i < allSources.length; i++) {
				if(allSources[i] != null && !allSources[i].free && !allSources[i].isPlaying() && allSources[i].flags != Source.Locked)
					freeSource(allSources[i]);
			}
		}
		
		protected Source obtainSource(int priority)
		{
			if(element().priority < priority || !element().isPlaying())
			{
				Source source = remove();
				int sourceId = source.sourceId;
				source.priority = priority;
				source.free = false;
				if(source.callback != null) {
					source.callback.run(source.channel);
					source.callback = null;
				}
				
//				al.alSourceStop(sourceId);
//				al.alSourcei(sourceId, AL_BUFFER, 0);
//				al.alSourcef(sourceId, AL_GAIN, 0.0f);
//				al.alSourcef(sourceId, AL_PITCH, 1.0f);
//				al.alSource3f(sourceId, AL_POSITION, 0, 0, 0);
//				al.alSourcei (sourceId, AL_SOURCE_RELATIVE,  AL_FALSE);
//				al.alSourcei (sourceId, AL_LOOPING, AL_FALSE);
				 
				add(source);
				
				return source;
			} else 
				return null;
		}

		public int stopSound(Source source) {
			if(source.flags == Source.Locked) 
				return -1;
			
//			al.alSourceStop(source.sourceId);
//			al.alSourcei(source.sourceId, AL_BUFFER, 0);
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
		}
	}
	
	protected class AndroidSource extends Source {

		public AndroidSource(AndoidSoundAPI23 drv, int i) {
			super(i, i, 0);
		}

		@Override
		public void play(float volume) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resume() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int dispose() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLooping(boolean loop, int loopstart, int loopend) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPosition(float x, float y, float z) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setVolume(float volume) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPitch(float pitch) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setGlobal(int num) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPriority(int priority) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isActive() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isLooping() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setCallback(SourceCallback callback, int num) {
			// TODO Auto-generated method stub
			
		}
	}

}
