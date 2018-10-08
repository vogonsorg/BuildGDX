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

import static ru.m210projects.Build.Engine.getInput;
import static ru.m210projects.Build.Input.Keymap.ANYKEY;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import ru.m210projects.Build.OnSceenDisplay.Console;

import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;

public class Gamepad {

	private final PovDirection[] directions = {
		PovDirection.north,
		PovDirection.south,
		PovDirection.west,
		PovDirection.east,
		PovDirection.northWest, //up left
		PovDirection.northEast, //up rigth
		PovDirection.southWest, //down left
		PovDirection.southEast, //down rigth
	};
	
	private Controller controller;
	protected String controllerName;
	protected boolean[] buttonStatus;
	protected boolean[] hitButton;
	protected int buttonsNum;
	protected int axisNum;
	protected int povNum;
	protected int allButtonsCount;
	protected boolean buttonPressed = false;

	public Gamepad(Controller controller)
	{
		buttonsNum = controller.getControlCount(ControlType.button);
		axisNum = controller.getControlCount(ControlType.axis);
		povNum = controller.getControlCount(ControlType.pov);
		
		controllerName = controller.getName();
		Console.Println("Found controller: \"" + controllerName + "\" [buttons: " + buttonsNum + " axises: " + axisNum + " povs: " + povNum + "]", OSDTEXT_YELLOW);
		allButtonsCount = buttonsNum + povNum * 4 + 2;
		buttonStatus = new boolean[allButtonsCount];
		hitButton = new boolean[allButtonsCount];
		this.controller = controller;
	}
	
	public boolean buttonPressed()
	{
		return buttonPressed;
	}
	
	public boolean getButton(int buttonCode)
	{
		if(buttonCode >= 0 && buttonCode < allButtonsCount)
			return buttonStatus[buttonCode];
		
		return false;
	}
	
	public void resetButtonStatus()
	{
		Arrays.fill(buttonStatus, false);
	}

	public boolean buttonPressed(int buttonCode)
	{
		if(buttonCode >= 0 && buttonCode < allButtonsCount)
			return hitButton[buttonCode];
		
		return false;
	}
	
	public boolean buttonStatusOnce(int buttonCode)
	{
		if(buttonCode >= 0 && buttonCode < allButtonsCount && buttonStatus[buttonCode]) {
			buttonStatus[buttonCode] = false;
			return true;
		}
		return false;
	}
	
	public boolean buttonStatus(int buttonCode)
	{
		if(buttonCode >= 0 && buttonCode < allButtonsCount && buttonStatus[buttonCode]) 
			return true;

		return false;
	}
	
	public int getButtonCount()
	{
		return allButtonsCount;
	}
	
	public int getAxisCount()
	{
		return axisNum;
	}
	
	public int getPovCount()
	{
		return povNum;
	}

	public float getAxisValue(int aCode, float deadZone) {
		float value = 0.0f;
		if(Math.abs(value = controller.getAxis(aCode)) >= deadZone) return value;
		
		return 0.0f;
	}

	private Vector2 stickOut = new Vector2();
	public Vector2 getStickValue(int aCode1, int aCode2, float deadZone, float smoothing)
	{
		float lx = controller.getAxis(aCode1);
		float ly = controller.getAxis(aCode2);
		float mag = (float) Math.sqrt(lx*lx + ly*ly);
		float nlx = lx / mag;
		float nly = ly / mag;
		float nlm = 0.0f;
		if (mag > deadZone)
		{
			if (mag > 1.0f)
				mag = 1.0f;

			mag -= deadZone;
			nlm = mag / (1.0f - deadZone);
			float x1 = nlx * nlm;
			float y1 = nly * nlm;
			float x2 = Math.signum(x1) * (float) Math.pow(Math.abs(x1), smoothing);
			float y2 = Math.signum(y1) * (float) Math.pow(Math.abs(y1), smoothing);
			//return new Vector2(x2, y2); //I don't like memory leaks
			stickOut.set(x2, y2);
			return stickOut;
		}
		else
		{
			mag = 0.0f;
			nlm = 0.0f;
			//return new Vector2(0.0f, 0.0f); //I don't like memory leaks
			stickOut.set(0.0f, 0.0f);
			return stickOut;
		}
	}

	public String getName() {
		return controllerName;
	}
	
	private void TriggerHandler()
	{
		float value = controller.getAxis(4);
		int num = buttonsNum + (4 * povNum);
		
		if(value >= 0.9f) {
			buttonPressed = true;
			if(!hitButton[num]) {
				getInput().setKey(ANYKEY, 1);
				buttonStatus[num] = true;
				hitButton[num] = true;
			}
		} else {
			buttonStatus[num] = false;
			hitButton[num] = false;
		}
		
		if(value <= -0.9f) {
			buttonPressed = true;
			if(!hitButton[num + 1]) {
				getInput().setKey(ANYKEY, 1);
				buttonStatus[num + 1] = true;
				hitButton[num + 1] = true;
			}
		} else {
			buttonStatus[num + 1] = false;
			hitButton[num + 1] = false;
		}
	}

	private void DPADHandler()
	{
		for(int i = 0; i < povNum; i++)
		{
			PovDirection dir = controller.getPov(i);
			if (dir != null && dir != PovDirection.center) 
			{
				int num = buttonsNum + (4 * i);
				for(int d = 0; d < 4; d++) 
				{
					if(dir == directions[d])
					{
						buttonPressed = true;
						if(!hitButton[num + d]) {
							getInput().setKey(ANYKEY, 1);
							buttonStatus[num + d] = true;
							hitButton[num + d] = true;
						}
					} else {
						buttonStatus[num + d] = false;
						hitButton[num + d] = false;
					}
				}

				for(int d = 0; d < 4; d++) 
				{
					int fbut = num + d / 2; //up down
					int sbut = (num + 2) + d % 2; //left right
					if(dir == directions[d + 4])
					{
						getInput().setKey(ANYKEY, 1);
						buttonStatus[fbut] = true;
						hitButton[fbut] = true;
						
						buttonStatus[sbut] = true;
						hitButton[sbut] = true;
					}
				}
			} else {
				for(int b = 0; b < 4; b++) {
					int num = buttonsNum + (4 * i) + b;
					buttonStatus[num] = false;
					hitButton[num] = false;
				}
			}
		}
	}

	public void ButtonHandler() {
		buttonPressed = false;
		DPADHandler();
		TriggerHandler();
		for(int i = 0; i < buttonsNum; i++)
		{
			if (controller.getButton(i)) {
				buttonPressed = true;
				if (!hitButton[i]) {
					getInput().setKey(ANYKEY, 1);
					buttonStatus[i] = true;
					hitButton[i] = true;
				}
			} else {
				buttonStatus[i] = false;
				hitButton[i] = false;
			}
		}
	}
}
