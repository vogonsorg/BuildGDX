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

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildMessage.MessageType;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.SliderDrawable;
import ru.m210projects.Build.Pattern.ScreenAdapters.InitScreen;
import ru.m210projects.Build.Pattern.Tools.Interpolation;
import ru.m210projects.Build.Pattern.Tools.SaveManager;
import ru.m210projects.Build.Script.DefScript;

import static ru.m210projects.Build.FileHandle.Compat.FilePath;
import static ru.m210projects.Build.OnSceenDisplay.Console.CloseLogFile;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public abstract class BuildGame extends Game {
	
	/*
	 * Вместо callback можно использовать пустые методы в классах, как в MenuSound или abstarct методы (переделать остальные)
	 * MenuVideo отключены настройки anisotropy
	 * MenuScreen handler если меню выключено
	 * MenuList, ResolutionList - getShade
	 */

	public final String appname;
	public final String sversion;
	public final char[] version;
	public final boolean release;
	public final String OS = System.getProperty("os.name");
	public final Date date;
	
	public BuildEngine engine;
	public BuildControls input;
	public BuildConfig cfg;
	public MenuHandler menu;
	public FontHandler fonts;
	public BuildNet net;
	public Interpolation gInt;
	public SaveManager savemgr;
	public SliderDrawable slider;
	
	public boolean gExit = false;
	public boolean gPaused = false;
	
	public final DefScript baseDef;
	public DefScript currentDef;
	
	private Screen gCurrScreen;
	private Screen gPrevScreen;
	
	public enum NetMode { Single, Multiplayer };
	public NetMode nNetMode;

	public BuildGame(BuildConfig cfg, String appname, String sversion, boolean release)
	{
		this.appname = appname;
		this.sversion = sversion;
		this.release = release;
		this.version = sversion.toCharArray();
		this.cfg = cfg;
		this.date = new Date("MMM dd, yyyy HH:mm:ss");
		this.baseDef = new DefScript(false);
		this.gInt = new Interpolation();
		this.savemgr = new SaveManager();
	}

	@Override
	public final void create() {
		setScreen(new InitScreen(this));
	}
	
	public abstract BuildFactory getFactory();

	public abstract void init() throws Exception;
	
	@Override
	public void dispose() {
		if(getScreen() instanceof InitScreen)
			((InitScreen) getScreen()).dispose();
		
		if(engine != null)
			engine.uninit();

		cfg.saveConfig(FilePath);
		System.out.println("disposed");
	}
	
	public BuildFont getFont(int i)
	{
		return fonts.getFont(i);
	}
	
	@Override
	public void render() {
		try {
			if(!gExit)
				super.render();
			else Gdx.app.exit();
		} catch (Throwable e) {
			e.printStackTrace();
			dispose();
			System.exit(1);
		}
	}
	
	public void changeScreen(Screen screen)
	{
		gPrevScreen = gCurrScreen;
		gCurrScreen = screen;
		setScreen(screen);
	}

	public boolean isCurrentScreen(Screen screen)
	{
		return gCurrScreen == screen;
	}
	
	public void setPrevScreen()
	{
		gCurrScreen = gPrevScreen;
		setScreen(gPrevScreen);
	}
	
	public String getScrName()
	{
		if(gCurrScreen != null)
			return gCurrScreen.getClass().getSimpleName();
		
		if(Gdx.app != null)
			return "Create frame";
		
		return "Init frame";
	}
	
	public void setDefs(DefScript script)
	{
		if(currentDef != script) {
			currentDef = script;
			engine.setDefs(script);
		}
	}
	
	protected String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append("\t" + element.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}
	
	public void ThrowError(String msg, Exception ex) {
		String stack = stackTraceToString(ex);
		Console.LogPrint(msg + ": " + stack);
		System.err.println(msg + ": " + stack);
		CloseLogFile();

		try {
			if (BuildGdx.message.show(msg, stack, MessageType.Crash))
			{
//				saveToFTP(); XXX
			}
		} catch (Exception e) {	
		} finally {
			Gdx.app.exit();
		}
	}
	
	public void ThrowError(String msg) {
		
	}
	
}
