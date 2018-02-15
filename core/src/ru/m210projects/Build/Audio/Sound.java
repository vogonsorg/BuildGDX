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
