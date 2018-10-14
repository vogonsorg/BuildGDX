// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

import ru.m210projects.Build.Types.BGraphics;
import ru.m210projects.Build.Types.BDisplay.DisplayType;

public class GPManager {
	
	public final static int MAXBUTTONS = 64;
	public final static int MAXPOV = 4;
	public final static int MAXAXIS = 12;

	private Array<Gamepad> gamepads;
	private float deadZone = 0.01f;
	
	boolean TestGamepad = false;
	
	public GPManager()
	{
		try {
			gamepads = new Array<Gamepad>();
			Array<Controller> controllers = null;
			if(((BGraphics) Gdx.graphics).getDisplayType() != DisplayType.Software)
				controllers = Controllers.getControllers();
			
			if(controllers != null && controllers.size > 0) {
				for(int i = 0; i < controllers.size; i++) {
					gamepads.add(new Gamepad(controllers.get(i)));
				}
			}
		} catch (Exception e) { }
		
		if(TestGamepad)
			gamepads.add(new Gamepad(new TestController()));
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
	
	public boolean buttonPressed()
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).buttonPressed())
				return true;
		}
		return false;
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
	
	public void resetButtonStatus()
	{
		for(int i = 0; i < gamepads.size; i++) {
			gamepads.get(i).resetButtonStatus();	
		}
	}
	
	public boolean buttonStatusOnce(int buttonCode)
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).buttonStatusOnce(buttonCode))
				return true;
		}
		return false;
	}
	
	public boolean buttonPressed(int buttonCode)
	{
		for(int i = 0; i < gamepads.size; i++) {
			if(gamepads.get(i).buttonPressed(buttonCode))
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
