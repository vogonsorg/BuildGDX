package ru.m210projects.Build.Input;

import static ru.m210projects.Build.Input.GPManager.MAXBUTTONS;
import com.badlogic.gdx.controllers.Controller;

public class Gamepad {

	private String controllerName;
	private boolean[] buttonStatus;
	private boolean[] hitButton;
	private Controller controller;
	private int buttonsNum;
	
	public Gamepad(Controller controller)
	{
		buttonsNum = MAXBUTTONS;
		buttonStatus = new boolean[buttonsNum];
		hitButton = new boolean[buttonsNum];
		this.controller = controller;
		controllerName = controller.getName();
	}
	
	public boolean getButton(int buttonCode)
	{
		if(buttonCode > 0 && buttonCode < buttonsNum)
			return buttonStatus[buttonCode];
		
		return false;
	}

	public boolean buttonStatusOnce(int buttonCode)
	{
		if(buttonCode > 0 && buttonCode < buttonsNum && buttonStatus[buttonCode]) {
			buttonStatus[buttonCode] = false;
			return true;
		}
		return false;
	}
	
	public boolean buttonStatus(int buttonCode)
	{
		if(buttonCode > 0 && buttonCode < buttonsNum && buttonStatus[buttonCode]) 
			return true;

		return false;
	}

	public float getAxisValue(int aCode, float deadZone) {
		float value = 0.0f;
		
		if(Math.abs(value = controller.getAxis(aCode)) >= deadZone) return value;
		
		return 0.0f;
	}

	public String getName() {
		return controllerName;
	}

	public void buttonHandler() {
		for(int i = 0; i < buttonsNum; i++)
		{
			if (controller.getButton(i)) {
				if (!hitButton[i]) {
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
