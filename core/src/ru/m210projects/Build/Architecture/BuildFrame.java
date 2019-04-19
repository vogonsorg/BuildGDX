package ru.m210projects.Build.Architecture;

public interface BuildFrame {
	
	public enum FrameType {
		Software, GL
	}

	public BuildInput getInput();
	
	public BuildGraphics getGraphics();
	
	public FrameType getType();
	
	public boolean isActive();
	
	public boolean isReady();
	
	public void setMaxFramerate(int fps);
	
	public int getX();
	
	public int getY();

}
