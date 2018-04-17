package ru.m210projects.Build.Input;

import static ru.m210projects.Build.Engine.getInput;
import static ru.m210projects.Build.Input.Keymap.ANYKEY;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;

import java.util.Arrays;

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

	private String controllerName;
	private boolean[] buttonStatus;
	private boolean[] hitButton;
	private Controller controller;
	private int buttonsNum;
	private int axisNum;
	private int povNum;
	private int allButtonsCount;

	public Gamepad(Controller controller)
	{
		buttonsNum = controller.getControlCount(ControlType.button);
		axisNum = controller.getControlCount(ControlType.axis);
		povNum = controller.getControlCount(ControlType.pov);
		
		controllerName = controller.getName();
		Console.Println("Found controller: " + controllerName + " buttons: " + buttonsNum + " axises: " + axisNum + " povs: " + povNum, OSDTEXT_YELLOW);
		allButtonsCount = buttonsNum + povNum * 4 + 2;
		buttonStatus = new boolean[allButtonsCount];
		hitButton = new boolean[allButtonsCount];
		this.controller = controller;
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
		return hitButton[buttonCode];
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

	public String getName() {
		return controllerName;
	}
	
	private void TriggerHandler()
	{
		float value = controller.getAxis(4);
		int num = buttonsNum + (4 * povNum);
		
		if(value >= 0.9f) {
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
		DPADHandler();
		TriggerHandler();
		for(int i = 0; i < buttonsNum; i++)
		{
			if (controller.getButton(i)) {
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
