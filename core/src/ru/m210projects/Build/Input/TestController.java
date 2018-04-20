package ru.m210projects.Build.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class TestController implements Controller {

	@Override
	public boolean getButton(int butName) {
		if(butName == 0 && Gdx.input.isKeyPressed(Keys.K))
			return true;
		if(butName == 1 && Gdx.input.isKeyPressed(Keys.L))
			return true;
		return false;
	}

	@Override
	public float getAxis(int value) {
		if(value == 0) {
			if(Gdx.input.isKeyPressed(Keys.LEFT_BRACKET))
				return 1.0f;
			
			if(Gdx.input.isKeyPressed(Keys.RIGHT_BRACKET))
				return -1.0f;
		}
		
		return 0;
	}

	@Override
	public PovDirection getPov(int paramInt) {
		return null;
	}

	@Override
	public boolean getSliderX(int paramInt) {
		return false;
	}

	@Override
	public boolean getSliderY(int paramInt) {
		return false;
	}

	@Override
	public Vector3 getAccelerometer(int paramInt) {
		return null;
	}

	@Override
	public void setAccelerometerSensitivity(float paramFloat) {}

	@Override
	public String getName() {
		return "Test controller";
	}

	@Override
	public int getControlCount(ControlType type) {
		switch(type)
		{
			case button:
				return 10;
			case axis:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public void addListener(ControllerListener paramControllerListener) {}

	@Override
	public void removeListener(ControllerListener paramControllerListener) {}
}
