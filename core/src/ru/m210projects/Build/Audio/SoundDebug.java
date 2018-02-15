package ru.m210projects.Build.Audio;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import java.nio.ByteBuffer;

import ru.m210projects.Build.OnSceenDisplay.Console;

public class SoundDebug implements Sound {

	@Override
	public boolean init(int system, int maxChannels) {
		Console.Println(getName() + " initialized", OSDTEXT_GOLD);
		return true;
	}

	@Override
	public void restart() {
		System.out.println("restart()");
	}

	@Override
	public void dispose() {
		System.out.println("dispose()");
	}

	@Override
	public void setVolume(float vol) {
		System.out.println("setVolume()");
	}

	@Override
	public void endLooping(Source source) {
		System.out.println("endLooping()");
	}

	@Override
	public Source playRaw(ByteBuffer data, int length, int rate, int bits,
			int pitchoffset, int volume, int priority) {
		System.out.println("playRaw() " + volume + " " + priority);
		return null;
	}

	@Override
	public Source playLoopedRaw(ByteBuffer data, int length, int loopstart,
			int loopend, int rate, int bits, int pitchoffset, int vol, int priority) {
		System.out.println("playLoopedRaw() " + vol + " " + priority);
		return null;
	}

	@Override
	public int stopSound(Source source) {
		System.out.println("stopSound()");
		return 0;
	}

	@Override
	public void stopAllSounds() {
		System.out.println("stopAllSounds()");
	}

	@Override
	public boolean isLooping(Source source) {
		System.out.println("isLooping()");
		return false;
	}

	@Override
	public boolean isPlaying(Source source) {
		System.out.println("isPlaying()");
		return false;
	}

	@Override
	public void setListener(int x, int y, int z, int ang) {
//		System.out.println("setListener()");
	}

	@Override
	public void setSourcePos(Source source, int x, int y, int z) {
		System.out.println("setSourcePos()");
	}

	@Override
	public void setSourceVolume(Source source, int vol) {
		System.out.println("setSourceVolume()");
	}

	@Override
	public void setSourcePitch(Source source, float pitch) {
		System.out.println("setSourcePitch()");
	}

	@Override
	public void setGlobal(Source source, int num) {
		System.out.println("setGlobal()");
	}

	@Override
	public void setPriority(Source source, int priority) {
		System.out.println("setPriority()");
	}

	@Override
	public boolean isActive(Source source) {
		System.out.println("isActive()");
		return false;
	}

	@Override
	public void setSystem(int system) {
		System.out.println("setSystem()");
	}

	@Override
	public String getName() {
		return "Debug sound";
	}

	@Override
	public void resetListener() {
		System.out.println("resetListener()");
	}

	@Override
	public boolean isInited() {
//		System.out.println("isInited()");
		return true;
	}
	
	@Override
	public void update() {
		
	}

	@Override
	public boolean newMusic(String file) {
		System.out.println("newMusic()");
		return false;
	}

	@Override
	public void playMusic() {
		System.out.println("playMusic()");
	}

	@Override
	public void stopMusic() {
		System.out.println("stopMusic()");
	}

	@Override
	public void pauseMusic() {
		System.out.println("pauseMusic()");
	}

	@Override
	public void volumeMusic(int vol) {
		System.out.println("volumeMusic()");
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
