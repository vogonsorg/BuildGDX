package ru.m210projects.Build.Input;

public interface IGamepad {
	
	public boolean isKeyPressed (int buttonCode);
	public float axisMoved(int axisCode);
	public String getName();

}
