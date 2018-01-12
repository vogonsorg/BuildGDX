package ru.m210projects.Build.Audio;

public interface Music {
	public void open(String name);
	public int open(byte[] data);
	public void close();
	public void stop();
	public void play(boolean looping);
	public void pause();
	public void resume();
	public void setVolume(int volume);
	public void dispose();
	public boolean init();
	public boolean isInited();
	public boolean isPlaying();
	public String getName();
	
}
