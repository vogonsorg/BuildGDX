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

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import ru.m210projects.Build.Architecture.BuildGDX;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;

public class GPManager {
	
	public final static int MAXBUTTONS = 64;
	public final static int MAXPOV = 4;
	public final static int MAXAXIS = 12;

	private Array<Gamepad> gamepads;
	private float deadZone = 0.01f;
	
//	boolean TestGamepad = false;
	
	public GPManager()
	{
		try {
			gamepads = new Array<Gamepad>();
			Array<Controller> controllers = null;
			
			if(BuildGDX.app.getFrameType() != FrameType.Software)
				controllers = Controllers.getControllers();
			
			if(controllers != null && controllers.size > 0) {
				for(int i = 0; i < controllers.size; i++) {
					gamepads.add(new Gamepad(i));
				}
			}
		} catch (Exception e) { }
		
//		if(TestGamepad)
//			gamepads.add(new Gamepad(new TestController()));
	}
	
	public int getControllers()
	{
		return gamepads.size;
	}

	public boolean isValidDevice(int deviceIndex)
	{
		return gamepads.size > 0 && deviceIndex >= 0 && deviceIndex < gamepads.size;
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
	
	public boolean getButton(int deviceIndex, int buttonCode)
	{
		return gamepads.get(deviceIndex).getButton(buttonCode);
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
	
	public boolean buttonStatusOnce(int deviceIndex, int buttonCode)
	{
		return gamepads.get(deviceIndex).buttonStatusOnce(buttonCode);
	}
	
	public boolean buttonPressed(int deviceIndex, int buttonCode)
	{
		return gamepads.get(deviceIndex).buttonPressed(buttonCode);
	}
	
	public boolean buttonStatus(int deviceIndex, int buttonCode)
	{
		return gamepads.get(deviceIndex).buttonStatus(buttonCode);
	}
	
	public float getAxisValue(int aCode) {
		float value = 0.0f;
		for(int i = 0; i < gamepads.size; i++) {
			if((value = gamepads.get(i).getAxisValue(aCode, deadZone)) != 0.0f)
				return value;
		}
		return 0.0f;
	}

	public Vector2 getStickValue(int deviceIndex, int aCode1, int aCode2)
	{
		// TODO
		// how come we are looping through an array in getAxisValue while it's single player ?
		// there should a parameter indicating which player pad is desired
		return gamepads.get(deviceIndex).getStickValue(aCode1, aCode2, deadZone);
	}
}
