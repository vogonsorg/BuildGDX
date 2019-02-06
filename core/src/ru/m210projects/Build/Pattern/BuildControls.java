//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Pattern;

import static ru.m210projects.Build.Engine.getInput;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;

import static ru.m210projects.Build.Pattern.BuildConfig.*;

import java.util.Arrays;

import com.badlogic.gdx.Input.Keys;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Input.GPManager;
import ru.m210projects.Build.Input.KeyInput;
import ru.m210projects.Build.Pattern.BuildNet.NetInput;

public abstract class BuildControls {
	
	public int oldPosX;
	public int oldPosY;
	public boolean[] maxisstatus;
	
	protected GPManager gpmanager;
	protected BuildConfig cfg;
	
	public BuildControls(BuildConfig cfg, GPManager gpmanager)
	{
		this.cfg = cfg;
		this.gpmanager = gpmanager;
		this.gpmanager.setDeadZone(cfg.gJoyDeadZone / 65536f);
		maxisstatus = new boolean[cfg.keynames.length];
		
		cfg.setKey(cfg.Menu_open, Keys.ESCAPE);
	}
	
	public void resetMousePos()
	{
		BuildGdx.input.setCursorPosition(xdim / 2, ydim / 2);
		oldPosX = BuildGdx.input.getX();
		oldPosY = BuildGdx.input.getY();
	}
	
	public boolean ctrlPadStatusOnce(int buttonCode)
	{
		return gpmanager.isValidDevice(cfg.gJoyDevice) && gpmanager.buttonStatusOnce(cfg.gJoyDevice, cfg.gpadkeys[buttonCode]);
	}
	
	public boolean ctrlPadStatus(int buttonCode)
	{
		return gpmanager.isValidDevice(cfg.gJoyDevice) && gpmanager.buttonStatus(cfg.gJoyDevice, cfg.gpadkeys[buttonCode]);
	}

	public boolean ctrlAxisStatusOnce(int keyId)
	{
		if(keyId >= 0 && maxisstatus[keyId]) {
			maxisstatus[keyId] = false;
			return true;
		}
		return false;
	}
	
	public boolean ctrlAxisStatus(int keyId)
	{
		if(keyId >= 0 && maxisstatus[keyId])
			return true;

		return false;
	}
	
	public boolean ctrlKeyStatusOnce(int keyId)
	{
		return getInput().keyStatusOnce(keyId);
	}
	
	public boolean ctrlKeyStatus(int keyId)
	{
		return getInput().keyStatus(keyId);
	}
	
	public boolean ctrlKeyPressed() {
		return getInput().keyPressed();
	}
	
	public boolean ctrlKeyPressed(int keyId)
	{
		return getInput().keyPressed(keyId);
	}
	
	public boolean ctrlGetInputKey(int keyName, boolean once) {
		final KeyInput input = getInput();
		final int key1 = cfg.primarykeys[keyName];
		final int key2 = cfg.secondkeys[keyName];
		final int keyM = cfg.mousekeys[keyName];
		final int keyG = cfg.gpadkeys[keyName];

		if (once) {
			return input.keyStatusOnce(key1)
					|| input.keyStatusOnce(key2)
					|| input.keyStatusOnce(keyM)
					|| ctrlAxisStatusOnce(keyName)
					|| keyName > Turn_Right && ctrlPadStatusOnce(keyG);
		} else {
			return input.keyStatus(key1)
					|| input.keyStatus(key2)
					|| input.keyStatus(keyM)
					|| ctrlAxisStatus(keyName)
					|| keyName > Turn_Right && ctrlPadStatus(keyG);
		}
	}
	
	public void ctrlResetKeyStatus() {
		getInput().resetKeyStatus();
		gpmanager.resetButtonStatus();
	}
	
	public void ctrlResetInput() {
		ctrlResetKeyStatus();
		Arrays.fill(getInput().hitkey, false);
	}
	
	public abstract void ctrlGetInput(NetInput input);

}
