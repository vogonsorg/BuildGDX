package ru.m210projects.Build.Input;

public interface IGamepad {
	
	public boolean isButtonPressed (int buttonCode);
	public float getAxisValue(int axisCode);
	public String getName();

}
