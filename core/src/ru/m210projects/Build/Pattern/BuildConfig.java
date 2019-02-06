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

import ru.m210projects.Build.Types.BConfig;

public class BuildConfig extends BConfig {

	public String[] keynames;
	public int[] primarykeys;
	public int[] secondkeys;
	public int[] mousekeys;
	public int[] gpadkeys;
	public int gJoyDeadZone;
	public int gJoyDevice;
	public int gJoyLookSpeed;
	public int gJoyTurnSpeed;
	public boolean gJoyInvert;
	public int gJoyTurnAxis;
	public int gJoyLookAxis;
	public int gJoyStrafeAxis;
	public int gJoyMoveAxis;
	
	public boolean useMouse;
	public boolean menuMouse;
	public int gSensitivity;
	public int gMouseTurnSpeed;
	public int gMouseMoveSpeed;
	public int gMouseStrafeSpeed;
	public boolean gMouseAim;
	public int gMouseLookSpeed;
	public boolean gInvertmouse;
	
	public static final int AXISLEFT = 0;
	public static final int AXISRIGHT = 1;
	public static final int AXISUP = 2;
	public static final int AXISDOWN = 3;
	public int[] mouseaxis = new int[4];
	
	public float soundVolume;
	public float musicVolume;
	public boolean noSound;
	public boolean muteMusic;
	public int resampler_num;
	public int maxvoices;
	public int musicType;
	
	public  int anisotropy = 0;
	public int widescreen;
	public boolean gShowFPS;
	
	
	
	public static final int Show_Console = 0;
	public static final int Menu_open = 1;
	public static final int Move_Forward = 2;
	public static final int Move_Backward = 3;
	public static final int Turn_Left = 4;
	public static final int Turn_Right = 5;
	public static final int Turn_Around = 6;
	public static final int Open = 7;
	
	public BuildConfig(String[] keynames)
	{
		this.keynames = keynames;
		this.primarykeys = new int[keynames.length];
		this.secondkeys = new int[keynames.length];
		this.mousekeys = new int[keynames.length];
		this.gpadkeys = new int[keynames.length];
	}

	@Override
	public void saveConfig(String path) {
		
	}

	public void setKey(int index, int keyId) {
		if(primarykeys[index] == 0 && secondkeys[index] == 0)
			primarykeys[index] = keyId;
		else if(primarykeys[index] != 0 && secondkeys[index] == 0 ) {
			if(keyId != primarykeys[index]) { 
				secondkeys[index] = primarykeys[index];
				primarykeys[index] = keyId;
			} else secondkeys[index] = 0;
		} else
		{
			if(keyId == primarykeys[index] || keyId == secondkeys[index]) {
				primarykeys[index] = keyId;
				secondkeys[index] = 0;
			} else {
				secondkeys[index] = primarykeys[index];
				primarykeys[index] = keyId;
			}
		}
		
		for(int i = 0; i < primarykeys.length; i++)
		{
			if(i != index && keyId == primarykeys[i]) {
				if(primarykeys[i] != 0 && secondkeys[i] != 0 ) {
					primarykeys[i] = secondkeys[i];
					secondkeys[i] = 0;
				} else primarykeys[i] = 0;
			}
		}
		
		for(int i = 0; i < secondkeys.length; i++)
		{
			if(i != index && keyId == secondkeys[i]) {
				secondkeys[i] = 0;
			}
		}
	}

	public void setButton(int index, int button) {
	
	}

	public int checkFps(int fpslimit) {
		
		return 0;
	}

}
