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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class TestController implements BController {

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
