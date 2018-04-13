package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

public abstract interface Controller {

	public abstract boolean getButton(int paramInt);
	  
	public abstract float getAxis(int paramInt);
	  
	public abstract PovDirection getPov(int paramInt);
	  
	public abstract boolean getSliderX(int paramInt);
	  
	public abstract boolean getSliderY(int paramInt);
	  
	public abstract Vector3 getAccelerometer(int paramInt);
	  
	public abstract void setAccelerometerSensitivity(float paramFloat);
	  
	public abstract String getName();
	
	public abstract int getControlCount(ControlType type);
	  
	public abstract void addListener(ControllerListener paramControllerListener);
	  
	public abstract void removeListener(ControllerListener paramControllerListener);
}
