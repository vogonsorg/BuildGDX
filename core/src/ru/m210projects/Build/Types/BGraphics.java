package ru.m210projects.Build.Types;

import com.badlogic.gdx.Graphics;

public interface BGraphics extends Graphics {
	
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast);
	
	public void setMaxFramerate(int fps);
	
	public void setDefaultDisplayConfiguration();

}
