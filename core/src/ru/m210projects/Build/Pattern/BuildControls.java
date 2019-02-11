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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Input.GPManager;
import ru.m210projects.Build.Input.KeyInput;
import ru.m210projects.Build.Pattern.BuildNet.NetInput;

public abstract class BuildControls {
	
	public int oldPosX;
	public int oldPosY;
	public boolean[] maxisstatus;
	public Vector2 mouseMove;
	public Vector2 stick1;
	public Vector2 stick2;
	
	protected GPManager gpmanager;
	protected BuildConfig cfg;
	
	public enum JoyStick { Turning, Moving };
	
	public BuildControls(BuildConfig cfg, GPManager gpmanager)
	{
		this.cfg = cfg;
		this.gpmanager = gpmanager;
		this.gpmanager.setDeadZone(cfg.gJoyDeadZone / 65536f);
		this.maxisstatus = new boolean[cfg.keymap.length];
		this.mouseMove = new Vector2();
		this.stick1 = new Vector2();
		this.stick2 = new Vector2();
	}
	
	public void resetMousePos()
	{
		BuildGdx.input.setCursorPosition(xdim / 2, ydim / 2);
		oldPosX = BuildGdx.input.getX();
		oldPosY = BuildGdx.input.getY();
	}
	
	public void ctrlMouseHandler()
	{
		mouseMove.set(0, 0);
		Arrays.fill(maxisstatus, false);
		
		if (cfg.useMouse) {
			
			int dx = Gdx.input.getX() - oldPosX;
			int dy = Gdx.input.getY() - oldPosY;
			
			if(dx != 0) {
				if(dx > 0)
				{
					if(cfg.mouseaxis[AXISRIGHT] != -1) {
						maxisstatus[cfg.mouseaxis[AXISRIGHT]] = true;
					}
				} else {
					if(cfg.mouseaxis[AXISLEFT] != -1) {
						maxisstatus[cfg.mouseaxis[AXISLEFT]] = true;
					}
				}
			}
			
			if(dy != 0) {
				if(dy > 0)
				{
					if(cfg.mouseaxis[AXISDOWN] != -1) {
						maxisstatus[cfg.mouseaxis[AXISDOWN]] = true;
					}
				} else {
					if(cfg.mouseaxis[AXISUP] != -1) {	
						maxisstatus[cfg.mouseaxis[AXISUP]] = true;
					}
				}
			}

			float sensscale = cfg.gSensitivity / 65536.0f;

			float xscale = sensscale * 4;
			float yscale = sensscale / 4;

			mouseMove.set(dx * xscale, dy * yscale);

			resetMousePos();
		}
	}
	
	public float ctrlGetMouseMove()
	{
		return mouseMove.y * cfg.gMouseMoveSpeed / 65536f;
	}
	
	public float ctrlGetMouseLook(boolean invert)
	{
		if(invert) 
			return -mouseMove.y * cfg.gMouseLookSpeed / 65536f;
		return mouseMove.y * cfg.gMouseLookSpeed / 65536f;
	}
	
	public float ctrlGetMouseTurn()
	{
		return mouseMove.x * cfg.gMouseTurnSpeed / 65536f;
	}
	
	public float ctrlGetMouseStrafe()
	{
		return mouseMove.x * cfg.gMouseStrafeSpeed / 2097152f;
	}

	public void ctrlJoyHandler() {
		stick1.set(0, 0);
		stick2.set(0, 0);
		if(gpmanager.isValidDevice(cfg.gJoyDevice)) {
			stick1.set(gpmanager.getStickValue(cfg.gJoyDevice, cfg.gJoyTurnAxis, cfg.gJoyLookAxis));
			stick2.set(gpmanager.getStickValue(cfg.gJoyDevice, cfg.gJoyStrafeAxis, cfg.gJoyMoveAxis));
        }
	}
	
	public Vector2 ctrlGetStick(JoyStick stick) {
		
		if(stick == JoyStick.Turning)
			return stick1;
		
		return stick2;
	}

	public boolean ctrlPadStatusOnce(KeyType buttonCode)
	{
		return gpmanager.isValidDevice(cfg.gJoyDevice) && gpmanager.buttonStatusOnce(cfg.gJoyDevice, cfg.gpadkeys[buttonCode.getNum()]);
	}
	
	public boolean ctrlPadStatus(KeyType buttonCode)
	{
		return gpmanager.isValidDevice(cfg.gJoyDevice) && gpmanager.buttonStatus(cfg.gJoyDevice, cfg.gpadkeys[buttonCode.getNum()]);
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
	
	public boolean ctrlGetInputKey(KeyType keyName, boolean once) {
		final KeyInput input = getInput();
		final int key1 = cfg.primarykeys[keyName.getNum()];
		final int key2 = cfg.secondkeys[keyName.getNum()];
		final int keyM = cfg.mousekeys[keyName.getNum()];
		
		if (once) {
			return input.keyStatusOnce(key1)
					|| input.keyStatusOnce(key2)
					|| input.keyStatusOnce(keyM)
					|| ctrlAxisStatusOnce(keyName.getNum())
					|| /* keyName > Turn_Right && */ctrlPadStatusOnce(keyName);
		} else {
			return input.keyStatus(key1)
					|| input.keyStatus(key2)
					|| input.keyStatus(keyM)
					|| ctrlAxisStatus(keyName.getNum())
					|| /* keyName > Turn_Right && */ctrlPadStatus(keyName);
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
