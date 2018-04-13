package ru.m210projects.Build.Input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

public class GPManager {
	
	public final static int MAXBUTTONS = 64;
	public final static int MAXPOV = 4;
	public final static int MAXAXIS = 12;

	private Array<Gamepad> gamepads;
	private float deadZone = 0.01f;
	
	public GPManager()
	{
		Array<Controller> controllers = Controllers.getControllers();
		gamepads = new Array<Gamepad>();
		if(controllers.size > 0) {
			for(int i = 0; i < controllers.size; i++)
				gamepads.add(new Gamepad(controllers.get(i)));
		}
	}
	
	public int getControllers()
	{
		return gamepads.size;
	}
	
	public String getControllerName(int num)
	{
		return gamepads.get(num).getName();
	}
	
	public void setDeadZone(float value)
	{
		this.deadZone = value;
	}
	
	public int getButtonCount(int num)
	{
		if(getControllers() > 0)
			return gamepads.get(num).getButtonCount();
		return 0;
	}
	
	public boolean getButton(int buttonCode)
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).getButton(buttonCode))
				return true;
		}
		return false;
	}
	
	public void handler()
	{
		for(int i = 0; i < gamepads.size; i++)
			gamepads.get(i).ButtonHandler();
	}
	
	public boolean buttonStatusOnce(int buttonCode)
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).buttonStatusOnce(buttonCode))
				return true;
		}
		return false;
	}
	
	public boolean buttonStatus(int buttonCode)
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).buttonStatus(buttonCode))
				return true;
		}
		return false;
	}
	
	public float getAxisValue(int aCode) {
		float value = 0.0f;
		for(int i = 0; i < gamepads.size; i++) {
			if((value = gamepads.get(i).getAxisValue(aCode, deadZone)) != 0.0f)
				return value;
		}
		return 0.0f;
	}
}
