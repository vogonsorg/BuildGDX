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

import static ru.m210projects.Build.FileHandle.Compat.Bcheck;
import static ru.m210projects.Build.FileHandle.Compat.Bclose;
import static ru.m210projects.Build.FileHandle.Compat.Bopen;
import static ru.m210projects.Build.FileHandle.Compat.Bwrite;
import static ru.m210projects.Build.FileHandle.Compat.toLowerCase;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.Tools.IniFile;

public abstract class BuildConfig extends IniFile {

	public boolean isInited;
	
	public interface KeyType {
		
		public int getNum();
		
		public KeyType setNum(int num);
		
		public String getName();
		
	}
	
	public KeyType[] joymap = {
		MenuKeys.Menu_Open.setJoyNum(0),
		MenuKeys.Menu_Enter.setJoyNum(1),
		MenuKeys.Menu_Up.setJoyNum(2),
		MenuKeys.Menu_Down.setJoyNum(3),
		MenuKeys.Menu_Left.setJoyNum(4),
		MenuKeys.Menu_Right.setJoyNum(5),
	};
	
	public enum MenuKeys implements KeyType { 
		Menu_Open,
		Menu_Enter,
		Menu_Up,
		Menu_Down,
		Menu_Left,
		Menu_Right;
	
		private int num = -1;
		private int joynum = -1;

		public int getNum() { return num; }
		
		public String getName() { return name(); }
		
		public KeyType setNum(int num) { this.num = num; return this; }
		
		public int getJoyNum() { return joynum; }
		
		public KeyType setJoyNum(int num) { this.joynum = num; return this; }
	}
	
	public enum GameKeys implements KeyType { 
		
		Move_Forward,
		Move_Backward,
		Turn_Left,
		Turn_Right,
		Turn_Around,
		Strafe,
		Strafe_Left,
		Strafe_Right,
		Jump,
		Crouch,
		Run,
		Open,
		Weapon_Fire,
		Next_Weapon,
		Previous_Weapon,
		Look_Up,
		Look_Down,
		Map_Toggle,
		Enlarge_Screen,
		Shrink_Screen,
		Send_Message,
		Mouse_Aiming,
		Show_Console;
		
		private int num = -1;

		public int getNum() { return num; }
		
		public String getName() { return name(); }
		
		public KeyType setNum(int num) { this.num = num; return this; }
	}

	public String path;
	public String cfgPath;
	public File soundBank;
	public boolean startup = true;
	public boolean autoloadFolder = true;
	public boolean userfolder = false;
	public boolean checkVersion = true;
	
	public int fullscreen = 0;
	public int ScreenWidth = 640;
	public int ScreenHeight = 400;
	public boolean gVSync = false;
	public int fpslimit = 0;
	public boolean borderless = false;
	
	public int snddrv = 1;
	public int middrv = 0;
	public int midiSynth = 0;
	
	public float gamma = 1;
	public float brightness = 0;
	public float contrast = 1;
	public float gFpsScale = 1.0f;

	public KeyType[] keymap;
	public int[] primarykeys;
	public int[] secondkeys;
	public int[] mousekeys;
	public int[] gpadkeys;

	public  int gJoyMoveAxis = 0; //Stick1Y
	public  int gJoyStrafeAxis = 1; //Stick1X
	public  int gJoyLookAxis = 2; //Stick2Y
	public  int gJoyTurnAxis = 3; //Stick2X
	public  int gJoyTurnSpeed = 65536;
	public  int gJoyLookSpeed = 65536;
	public  int gJoyDeadZone = 6144;
	public  boolean gJoyInvert = false;
	public  int gJoyDevice = -1;
	public int[] gJoyMenukeys = new int[joymap.length];

	public boolean useMouse = true;
	public boolean menuMouse = true;
	public int gSensitivity = 69632;
	public int gMouseTurnSpeed = 65536;
	public int gMouseLookSpeed = 65536;
	public int gMouseMoveSpeed = 65536;
	public int gMouseStrafeSpeed = 131072;
	public int gMouseCursor = 0;
	public int gMouseCursorSize = 65536;
	public boolean gMouseAim = true;
	public boolean gInvertmouse = false;
	
	public static final int AXISLEFT = 0;
	public static final int AXISRIGHT = 1;
	public static final int AXISUP = 2;
	public static final int AXISDOWN = 3;
	public int[] mouseaxis = new int[4];
	
	public float soundVolume = 1.00f;
	public float musicVolume = 1.00f;
	public boolean noSound = false;
	public boolean muteMusic = false;
	public int resampler_num = 0;
	public int maxvoices = 32;
	public int musicType = 0;
	
	public  int glanisotropy = 0;
	public int widescreen = 1;
	public int glfilter = 0;
	public boolean gShowFPS = true;

	public BuildConfig(String path, String name) {
		super();	

		this.cfgPath = path;
		this.keymap = getKeyMap();
		this.name = toLowerCase(name);
		byte[] data = null;
		
		try {
			RandomAccessFile raf = new RandomAccessFile(path + name, "r");
			data = new byte[(int)raf.length()];
			raf.read(data);
			init(data);
			raf.close();
		} catch(FileNotFoundException e) { 
			Console.Println("File not found: " + path + name, OSDTEXT_YELLOW);
		} catch (IOException e) {
			Console.Println("Read file error: " + e.getMessage(), OSDTEXT_YELLOW);
		}

		this.primarykeys = new int[keymap.length];
		this.secondkeys = new int[keymap.length];
		this.mousekeys = new int[keymap.length];
		this.gpadkeys = new int[keymap.length];
		
		Arrays.fill(mouseaxis, -1);
		Arrays.fill(gpadkeys, -1);
		Arrays.fill(gJoyMenukeys, -1);
		
		LoadMain();
	}
	
	public boolean isExist()
	{
		return data != null;
	}
	
	public void LoadMain()
	{
		if(data != null)
		{
			if(set("Main")) {
				startup = GetKeyInt("Startup") == 1;
				int check = GetKeyInt("CheckNewVersion");
				if(check != -1) checkVersion = (check == 1);
				int afolder = GetKeyInt("AutoloadFolder");
				if(afolder != -1) autoloadFolder = (afolder == 1);
				userfolder = GetKeyInt("UseHomeFolder") == 1;
				String respath = GetKeyString("Path");
				if(respath != null && !respath.isEmpty())
					this.path = respath;
				String bank = GetKeyString("SoundBank");
				if(bank != null && !bank.isEmpty()) {
					File fbank = new File(bank);
					if(fbank.exists())
						this.soundBank = fbank;
				}

				int value = GetKeyInt("Fullscreen");
				if(value != -1) fullscreen = value;
				value = GetKeyInt("ScreenWidth");
				if(value != -1 && value >= 640) ScreenWidth = value;
				value = GetKeyInt("ScreenHeight");
				if(value != -1 && value >= 400) ScreenHeight = value;
				borderless = GetKeyInt("BorderlessMode") == 1;
				gVSync = GetKeyInt("VSync") == 1;
				value = GetKeyInt("FPSLimit");
				if(value != -1) fpslimit = value;
				value = GetKeyInt("GLFilterMode");
				if(value != -1) glfilter = value;
				value = GetKeyInt("GLAnisotropy");
				if(value != -1) glanisotropy = value;
				value = GetKeyInt("WideScreen");
				if(value != -1) widescreen = value;
				noSound = GetKeyInt("NoSound") == 1;
				muteMusic = GetKeyInt("NoMusic") == 1;
				int snd = GetKeyInt("SoundDriver");
				if(snd != -1) snddrv = snd;
				int mid = GetKeyInt("MidiDriver");
				if(mid != -1) middrv = mid;
				int mids = GetKeyInt("MidiSynth");
				if(mids != -1) midiSynth = mids;
				int type = GetKeyInt("MusicType");
				if(type != -1) musicType = type;
			}
		}
		
		isInited = false;
	}
	
	public void saveConfig(String path)
	{
		if(!isInited) 
			return;
		File file = Bcheck(cfgPath+name, "R");
		if(file != null) 
			file.delete();
		int fil = Bopen(cfgPath+name, "RW");
		SaveMain(fil, path);
		SaveConfig(fil);
		Bclose(fil);
	}
	
	public void SaveMain(int fil, String path)
	{
		saveString(fil, "[Main]\r\n");
		String line =  "; Always show configuration options on startup\r\n" +
				";   0 - No\r\n" +
				";   1 - Yes\r\n";
		saveString(fil, line);
		saveBoolean(fil, "Startup", startup);
		saveBoolean(fil, "CheckNewVersion", checkVersion);
		saveBoolean(fil, "AutoloadFolder", autoloadFolder);
		saveBoolean(fil, "UseHomeFolder", userfolder);
		saveString(fil, "Path = ");
			byte[] buf = path.getBytes(); //without this path can be distorted
			Bwrite(fil, buf, buf.length);
			
		if(soundBank != null)	
			saveString(fil, "\r\nSoundBank = " + soundBank.getAbsolutePath() + "\r\n");
		else saveString(fil, "\r\nSoundBank = \r\n");
		saveInteger(fil, "Fullscreen", fullscreen);
		saveInteger(fil, "ScreenWidth", ScreenWidth);
		saveInteger(fil, "ScreenHeight", ScreenHeight);
		saveBoolean(fil, "BorderlessMode", borderless);
		saveBoolean(fil, "VSync", gVSync);
		saveInteger(fil, "FPSLimit", fpslimit);
		if(Console.IsInited())
			saveInteger(fil, "GLFilterMode", Console.Geti("r_texturemode"));
		else saveInteger(fil, "GLFilterMode", glfilter);
		saveInteger(fil, "GLAnisotropy", glanisotropy);
		saveInteger(fil, "WideScreen", widescreen);
		
		saveBoolean(fil, "NoSound", noSound);
		saveBoolean(fil, "NoMusic", muteMusic);
		saveInteger(fil, "SoundDriver", snddrv);
		
		saveInteger(fil, "MidiDriver", middrv);
		saveInteger(fil, "MidiSynth", midiSynth);
		saveInteger(fil, "MusicType", musicType);
		saveString(fil, ";\r\n;\r\n");
	}

	public abstract void SaveConfig(int fil);
	
	public abstract boolean InitConfig(boolean isDefault);
	
	public void InitKeymap()
	{
		for(int i = 0; i < keymap.length; i++)
			keymap[i].setNum(i);
	}
	
	public abstract KeyType[] getKeyMap();
	
	public void setKey(int index, int keyId)
	{
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

	public void setButton(KeyType key, int button) {
		if(key instanceof MenuKeys) {
			MenuKeys mk = (MenuKeys) key;
			gJoyMenukeys[mk.getJoyNum()] = button;
		}
		else gpadkeys[key.getNum()] = button;
	}

	public int checkFps(int fpslimit) {
		int num = -1;
		switch(fpslimit) {
			case 0: num = 0; break;
			case 30: num = 1; break;
			case 60: num = 2; break;
			case 120: num = 3; break;
			case 144: num = 4; break;
		}
		
		if(num < 0 || num >= 5) {
			num = 0;
			fpslimit = 0;
			BuildGdx.app.setMaxFramerate(0);
		}
		
		return num;
	}

	public int getKeyIndex(String keyname)
	{
		keyname = keyname.replaceAll("[^a-zA-Z_-]", "");
		for(int i = 0; i < keymap.length; i++)
		{
			if(keyname.equals(keymap[i].getName()))
				return i;
		}	
		return -1;
	}
	
	public void saveBoolean(int fil, String name, boolean var)
	{
		String line =  name + " = " + (var?1:0) +"\r\n";
		Bwrite(fil, line.toCharArray(), line.length());
	}
	
	public void saveInteger(int fil, String name, int var)
	{
		String line =  name + " = " + var +"\r\n";
		Bwrite(fil, line.toCharArray(), line.length());
	}
	
	public void saveString(int fil, String text)
	{
		Bwrite(fil, text.toCharArray(), text.length());
	}
	
	public void saveString(int fil, String name, String var)
	{
		String line =  name + " = " + var +"\r\n";
		Bwrite(fil, line.toCharArray(), line.length());
	}
}
