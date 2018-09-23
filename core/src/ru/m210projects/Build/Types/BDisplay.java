package ru.m210projects.Build.Types;

import com.badlogic.gdx.Graphics.BufferFormat;
import com.badlogic.gdx.Graphics.DisplayMode;

public interface BDisplay {
	
	public enum DisplayType {
		Software, GL
	}
	
	public DisplayType getType();
	
	public int getWidth();
	
	public int getHeight();

	public void setTitle (String title);
	
	public void setUndecorated (boolean undecorated);
	
	public void setResizable (boolean resizable);
	
	public void sync(int frameRate);

	public void setVSync (boolean vsync);
	
	public boolean isFullscreen ();
	
	public int getX();
	
	public int getY();
	
	public void update();
	
	public void destroy();
	
	public void process();
	
	public boolean isCloseRequested();
	
	public boolean isActive();
	
	public boolean wasResized();
	
	public BufferFormat getBufferFormat();
	
	public boolean supportsExtension (String extension);
	
	public boolean setWindowedMode (int width, int height);
	
	public boolean setFullscreenMode (DisplayMode displayMode);
	
	public DisplayMode[] getDisplayModes();
	
	public DisplayMode getDisplayMode();
	
	public boolean isDirty();
	
	public void resize(int width, int height);
	
}
