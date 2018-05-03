package ru.m210projects.Build.desktop;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.EFX10.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;
import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.io.ByteArrayInputStream;
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
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.desktop.DesktopSound.SourceManager;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.backends.openal.OggInputStream;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.StreamUtils;

public class DesktopSound implements Sound {

	private boolean noDevice = true;
	private final int MONO = 0;
	private final static FloatBuffer NULLVECTOR = BufferUtils.newFloatBuffer(3);
	private final static FloatBuffer orientation = (FloatBuffer)BufferUtils.newFloatBuffer(6);
	private final float[] deforientation = new float[] {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
	
	private final float ref_distance = 2.0f;
	private int system;
	private int maxChannels;

	private IntBuffer buffers;
	private IntBuffer musicBuffers;
	private final int musicBufferCount = 3;
	private SourceManager sourceManager;
	private float soundVolume = 0.5f;
	private float musicVolume = 1.0f;
	private String name = "OpenAL";
	private List<Source> loopedSource = new ArrayList<Source>();
	
	private int alEffectSlot = -1;
	private int alEffect = -1;
	private boolean alReverbEnable = false;
	private float alReverbDelay = 0.0f;
	private float alDefReverbDelay;
	

	public DesktopSound()
	{
		LwjglNativesLoader.load();
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
			
			musicBuffers = BufferUtils.newIntBuffer(musicBufferCount);
			alGenBuffers(musicBuffers);
			
			if (alGetError() != AL_NO_ERROR) 
				Console.Println("Unabe to allocate audio buffers.", OSDTEXT_RED);
	
			resetListener();
			this.system = system;
			this.maxChannels = maxChannels;
	
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
	
			loopedSource.clear();
			return true;
		} catch (Exception ex) {
			Console.Println("Unable to initialize OpenAL! - " + ex.getLocalizedMessage(), OSDTEXT_RED);
			noDevice = true;
			return false;
		}
	}
	
	private void setSourceReverb(int sourceId, boolean enable, float delay)
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
	
	@Override
	public Source playRaw(ByteBuffer data, int length, int sampleRate, int sampleBits, int pitchoffset, int vol, int priority) {
		if (noDevice) return null;
		Source source = sourceManager.obtainSource(priority);
		if (source == null) return null;

		if(loopedSource.size() > 0 && source.loopInfo.looped)
			loopedSource.remove(source);
		source.loopInfo.clear();
		
		int sourceId = source.sourceId;
		alSourcei(sourceId, AL_LOOPING, AL_FALSE);
		
		setSourceReverb(sourceId, alReverbEnable, alReverbDelay);

		setSourceVolume(source, vol);
		int bufferID = buffers.get(source.bufferId);
		alBufferData(bufferID, toALFormat(0, sampleBits), data, sampleRate);
		alSourcei(sourceId, AL_BUFFER,   bufferID );
		alSourcePlay(sourceId);

		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("playRaw " + error, OSDTEXT_RED);

		return source;
	}
	
	@Override
	public Source playLoopedRaw(ByteBuffer data, int length, int loopstart,
			int loopend, int sampleRate, int sampleBits, int pitchoffset, int vol, int priority) {
	
		if (noDevice) return null;
		Source source = sourceManager.obtainSource(priority);
		if (source == null) return null;
		int sourceId = source.sourceId;
		
		if(loopedSource.size() > 0 && source.loopInfo.looped)
			loopedSource.remove(source);
		source.loopInfo.clear();
		
		int start = 0, end = data.capacity();
		if(loopstart >= 0 && loopstart < data.capacity()) 
			start = loopstart;
		if(loopend < data.capacity()) 
			end = loopend;
		
		setSourceReverb(sourceId, alReverbEnable, alReverbDelay);
		
		float volume = vol / 255.0f;
		if(volume > 1.0f) volume = 1.0f;
		setSourceVolume(source, vol);
		int bufferID = buffers.get(source.bufferId);
		alBufferData(bufferID, toALFormat(0, sampleBits), data, sampleRate);
		if(start > 0) {
			alSourcei(sourceId, AL_LOOPING, AL_FALSE);
			alSourceQueueBuffers(source.sourceId, bufferID);
			loopedSource.add(source);
			source.loopInfo.set(data, start, end, toALFormat(0, sampleBits), sampleRate);
		} else {
			if(end > 0) data.limit(end);
			alSourcei(sourceId, AL_LOOPING, AL_TRUE);
			alSourcei(sourceId, AL_BUFFER,   bufferID );
		}
		alSourcePlay(sourceId);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("playLoopeRaw " + error, OSDTEXT_RED);
		return source;
	}

	@Override
	public void dispose() {
		if (noDevice || !AL.isCreated()) return;
		sourceManager.dispose();
		alDeleteBuffers(buffers);
		alDeleteBuffers(musicBuffers); 
		
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
	public void setVolume(float vol) {
		if (noDevice) return;
		
		this.soundVolume = vol;
//		alListenerf(AL_GAIN, vol);
	}

	@Override
	public void endLooping(Source source) {
		if (noDevice) return;
		alSourcei(source.sourceId, AL_LOOPING, AL_FALSE);
	}
	
	@Override
	public void setSourceVolume( Source source, int vol ) {
		if (noDevice) return;
		if(vol > 255) vol = 255;
		float volume = (vol * soundVolume) / 255.0f;
		alSourcef(source.sourceId, AL_GAIN, volume);
	}

	@Override
	public void setSourcePitch(Source source, float pitch) {
		if (noDevice) return;
		if(pitch < 0) pitch = 0;
		alSourcef(source.sourceId, AL_PITCH, pitch);
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("setPitch " + error + " " + pitch, OSDTEXT_RED);
	}
	
	@Override
	public void setGlobal(Source source, int num) {
		if (noDevice) return;
		alSourcei (source.sourceId, AL_SOURCE_RELATIVE,  num);
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("setGlobal " + error + " " + num, OSDTEXT_RED);
	}

	@Override
	public int stopSound(Source source) {
		if (noDevice) return -1;
		
		if(loopedSource.size() > 0 && source.loopInfo.looped) {
			source.loopInfo.clear();
			loopedSource.remove(source);
		}

		return sourceManager.stopSound(source);
	}

	@Override
	public void stopAllSounds() {
		if (noDevice) return;
		sourceManager.stopAllSounds();
		loopedSource.clear();
	}
	
	@Override
	public boolean isLooping(Source source)
	{
		if (noDevice) return false;
		return alGetSourcei(source.sourceId, AL_LOOPING) == AL_TRUE;
	}
	
	@Override
	public boolean isPlaying(Source source)
	{
		if (noDevice) return false;
		return alGetSourcei(source.sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	@Override
	public void setSourcePos(Source source, int x, int y, int z) {
		if (noDevice) return;
		if(system != MONO) {
			alSource3f(source.sourceId, AL_POSITION, x / ref_distance, y / ref_distance, z / ref_distance);
		} else alSource3f(source.sourceId, AL_POSITION, 0, 0, 0);
	}
	
	@Override
	public void setPriority(Source source, int priority) {
		if (noDevice) return;
		source.priority = priority;
	}

	@Override
	public void setListener(int x, int y, int z, int ang) {
		if (noDevice) return;
		if(system != MONO) 
		{
			alListener3f(AL_POSITION, x / ref_distance, y / ref_distance, z / ref_distance);
			double angle = (ang * 2 * Math.PI) / 2048;
			orientation.put(0, (float)Math.cos(angle));
			orientation.put(1, 0);
			orientation.put(2, (float)Math.sin(angle));
			orientation.rewind();
			alListener ( AL_ORIENTATION, orientation );
		} else alListener (AL_POSITION, NULLVECTOR);
	}

	@Override
	public void setSystem(int system) {
		if (noDevice) return;
		this.system = system;
	}

	@Override
	public void restart() {
		System.out.println("Sound system restarting...");
		dispose();
		init(system, maxChannels);
	}

	@Override
	public boolean isActive(Source source) {
		if (noDevice) return false;
		return isPlaying(source) && source.priority != 0 && !source.free;
	}

	@Override
	public String getName() {
		return name;
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
	public boolean isInited() {
		return !noDevice;
	}
	
	public int toALFormat(int channels, int bits)
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
	
	public class SourceManager extends java.util.PriorityQueue<Source>
	{
		private static final long serialVersionUID = 1L;
		private Source[] allSources;
		public SourceManager(int maxSources) {
			allSources = new Source[maxSources];

			clear();
			for (int i = 0; i < maxSources; i++) {
				int sourceId = alGenSources();
				if (alGetError() != AL_NO_ERROR) break;
				allSources[i] = new Source(i, sourceId, 0);

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
				source.type = 0;
				source.flags = 0;
				source.channel = -1;
				source.loopInfo.clear();
				add(source);
			}
		}
		
		public Source obtainSource(int priority)
		{
			for(int i = 0; i < allSources.length; i++) {
				if(!allSources[i].free && !isPlaying(allSources[i]) && allSources[i].flags != Source.Locked)
					freeSource(allSources[i]);
			}
			
//			Iterator<Source> it = this.iterator();
//		    while(it.hasNext()) {
//		      Source obj = (Source)it.next();
//		      System.out.println(obj.sourceId + " " + obj.free + " " + obj.priority + " " + obj.flags);
//		    }
//		    System.out.println();

			if(element().priority < priority || !isPlaying(element()))
			{
				Source source = remove();
				int sourceId = source.sourceId;
				source.priority = priority;
				source.free = false;
				source.type = 0;
				source.flags = 0;
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
				stopSound(allSources[i]);
				alDeleteSources(allSources[i].sourceId);
				allSources[i] = null;
			}
		}
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
	
	
	
	
	//Digital music handler

	
	private OpenALMusic music;

	@Override
	public void update() {
		if(noDevice) return;
		
		int error = alGetError();
		if(error != AL_NO_ERROR) 
			Console.Println("OpenAL Error " + error, OSDTEXT_RED);
		
		if(music != null)
			music.update();

		Iterator<Source> i = loopedSource.iterator();
		while (i.hasNext()) {
			Source s = i.next();
			if(!isPlaying(s) && s.loopInfo.looped) {
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
	public boolean newMusic(String file) {
		if(noDevice) return false;
		if(music != null) {
			if(music.isPlaying())
				music.stop();
			music = null;
		}
		
		if(!kExist(file, 0)) return false;

		try {
			music = new Ogg.Music(sourceManager, kGetBytes(file, 0));
		} catch (Throwable e) {
			Console.Println("Can't load ogg file: " + file, OSDTEXT_RED);
			return false;
		}
		
		music.setVolume(musicVolume);
		music.setLooping(true);
		return true;
	}

	@Override
	public void playMusic() {
		if(music == null) return;
		music.play(musicBuffers);
	}

	@Override
	public void stopMusic() {
		if(music == null) return;
		music.stop();
	}

	@Override
	public void pauseMusic() {
		if(music == null) return;
		music.pause();
	}

	@Override
	public void volumeMusic(int vol) {
		System.out.println("Change music volume " + (vol / 255.0f));
		musicVolume = vol / 255.0f;
		if(music != null)
			music.setVolume(musicVolume);
	}

	@Override
	public boolean isPlaying() {
		if(music == null) return false;
		return music.isPlaying();
	}
}

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

	public OpenALMusic (SourceManager sourceManager, byte[] data) {
		this.sourceManager = sourceManager;
		this.data = data;
		
	}

	protected void setup (int channels, int sampleRate) {
		this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
		this.sampleRate = sampleRate;
		secondsPerBuffer = (float)bufferSize / bytesPerSample / channels / sampleRate;
	}

	public void play(IntBuffer buffers) {
		if (source == null) {
			source = sourceManager.obtainSource(Integer.MAX_VALUE);
			if (source == null) return;
			alSourcei(source.sourceId, AL_LOOPING, AL_FALSE);
			setVolume(musicVolume);
			source.flags |= Source.Locked;
			for (int i = 0; i < buffers.capacity(); i++) {
				int bufferID = buffers.get(i);
				if (!fill(bufferID)) break;
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
			else
				end = true;
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
		public Music(SourceManager sourceManager, byte[] data) {
			super(sourceManager, data);
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


