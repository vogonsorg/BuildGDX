package ru.m210projects.Build.Types;

import com.badlogic.gdx.Graphics;

import ru.m210projects.Build.Types.BDisplay.DisplayType;

public interface BGraphics extends Graphics {
	
	public void setMaxFramerate(int fps);
	
	public void setDisplayType(DisplayType type);
	
	public DisplayType getDisplayType();
	
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast);

	public void setDefaultDisplayConfiguration();
	
	public byte[] getFrame();
	
	public void changepalette(byte[] palette);
}
