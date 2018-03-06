package ru.m210projects.Build.Input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

public class Gamepad implements IGamepad {

	private String controllerName;
	private Array<Controller> controllers;
	
	public Gamepad()
	{
		controllers = Controllers.getControllers();
		if(controllers.size > 0)
			controllerName = controllers.get(0).getName();
	}
	
	@Override
	public boolean isButtonPressed(int buttonCode) {
		
		for(int i = 0; i < controllers.size; i++)
		{
			Controller c = controllers.get(i);
			if(c.getButton(buttonCode)) return true;
		}
		
		return false;
	}

	@Override
	public float getAxisValue(int aCode) {
		float value = 0.0f;
		for(int i = 0; i < controllers.size; i++)
		{
			Controller c = controllers.get(i);
			if(Math.abs(value = c.getAxis(aCode)) >= 0.01f) return value;
		}
		
		return 0.0f;
	}

	@Override
	public String getName() {
		return controllerName;
	}
}
