package ru.m210projects.Build.Input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

public class GPManager {
	
	public final static int MAXBUTTONS = 128;

//	private Array<Controller> controllers;
	private Array<Gamepad> gamepads;
	
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
			gamepads.get(i).buttonHandler();
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

}
