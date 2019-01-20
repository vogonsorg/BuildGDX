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

package ru.m210projects.Build.Types;

public abstract class BConfig {
	
	public String path;
	public String soundBank = "";
	public boolean startup = true;
	public boolean autoloadFolder = true;
	public boolean userfolder = false;
	public boolean checkVersion = true;
	
	public int fullscreen = 0;
	public int ScreenWidth = 640;
	public int ScreenHeight = 400;
	public boolean gVSync = false;
	public boolean borderless = false;
	public int fpslimit = 0;
	
	public int snddrv = 1;
	public int middrv = 1;
	public String midiSynth = "None";
	
	public float gamma = 1;
	public float brightness = 0;
	public float contrast = 1;
	public float gFpsScale = 1.0f;
	
	public abstract void saveConfig(String path);
}
