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

package ru.m210projects.Build.Pattern.ScreenAdapters;

import static ru.m210projects.Build.Engine.fullscreen;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.FileHandle.Cache1D.initgroupfile;
import static ru.m210projects.Build.Net.Mmulti.uninitmultiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildMessage.MessageType;
import ru.m210projects.Build.Audio.BuildAudio.Driver;
import ru.m210projects.Build.Input.GPManager;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildEngine;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildConfig.GameKeys;
import ru.m210projects.Build.Pattern.BuildFactory;

public class InitScreen extends ScreenAdapter {
	
	private int frames;
	private BuildEngine engine;
	
	private BuildFactory factory;
	
	private Thread thread;

	@Override
	public void show()
	{
		frames = 0;
		Console.fullscreen(true);
	}
	
	@Override
	public void hide () {
		Console.fullscreen(false);
	}

	public InitScreen(final BuildGame game)
	{
		factory = game.getFactory();
		
		Console.SetLogFile(game.appname + ".log");
		
		Console.Println("BUILD engine by Ken Silverman (http://www.advsys.net/ken) \r\n"
				+ game.appname + " " + game.sversion + " by [M210®] (http://m210.duke4.net)");

		
		Console.Println("Current date " + game.date.getLaunchDate());
		
		String osver = System.getProperty("os.version");
		String jrever = System.getProperty("java.version");

		Console.Println("Running on " + game.OS + " (version " + osver + ")");
		Console.Println("\t with JRE version: " + jrever + "\r\n");
		
		Console.Println("Initializing resource archives");

		for(int i = 0; i < factory.resources.length; i++) {
			try {
				initgroupfile(factory.resources[i]);
			} catch (Exception e) { 
				BuildGdx.message.show("Init error!", "Resource initialization error!", MessageType.Info);
				Gdx.app.exit();
				return;
			}
		}

		try {
			Console.Println("Initializing Build 3D engine");
			this.engine = game.pEngine = factory.engine();
		} catch (Exception e) { 
			BuildGdx.message.show("Build Engine Initialization Error!", "There was a problem initialising the Build engine: \r\n" + e.getMessage(), MessageType.Info);
			Gdx.app.exit();
			return;
		}
		
		Console.setFunction(factory.console());

		engine.loadpics("tiles000.art");
		
		BuildConfig cfg = game.pCfg;
		game.pFonts = factory.fonts();
		
		engine.setrendermode(factory.renderer());
		if(!engine.setgamemode(cfg.fullscreen, cfg.ScreenWidth, cfg.ScreenHeight))
			cfg.fullscreen = 0;
		fullscreen = cfg.fullscreen;
		
		cfg.checkFps(cfg.fpslimit);
		engine.setanisotropy(cfg, cfg.glanisotropy);
		engine.setwidescreen(cfg, cfg.widescreen != 0);
		Console.Set("r_texturemode", cfg.glfilter);
		
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BuildConfig cfg = game.pCfg;
					if(!cfg.isInited) 
						cfg.isInited = cfg.InitConfig(!cfg.isExist());
					
					game.pInput = factory.input(new GPManager());
					game.pMenu = factory.menus();
					game.pNet = factory.net();
					game.pSlider = factory.slider();

					uninitmultiplayer();
					
					BuildGdx.audio.setDriver(Driver.Sound, cfg.snddrv);
					BuildGdx.audio.setDriver(Driver.Music, cfg.middrv);
					
					int consolekey = GameKeys.Show_Console.getNum();
					
					Console.setCaptureKey(cfg.primarykeys[consolekey], 0);
					Console.setCaptureKey(cfg.secondkeys[consolekey], 1);
					Console.setCaptureKey(cfg.mousekeys[consolekey], 2);
					Console.setCaptureKey(cfg.gpadkeys[consolekey], 3);

					game.init();
				} catch (Exception e) {
					game.ThrowError("InitScreen error", e);
				}
			}
		});
		thread.start();
	}

	public void dispose()
	{
		try { 
			if(thread != null)
				thread.join();
		} catch (InterruptedException e) { }
	}

	@Override
	public void render(float delta) {
		engine.clearview(0);
		
		engine.rotatesprite(0, 0, 65536, 0, factory.getInitTile(), -128, 0, 10 | 16, 0, 0, xdim - 1, ydim - 1);

		if(frames > 3)
		{
//			BuildConfig cfg = game.cfg;
//			game.input = factory.input(new GPManager());
//			game.menu = factory.menus();
//			game.net = factory.net();
//
//			uninitmultiplayer();
//			
//			BuildGdx.audio.setDriver(Driver.Sound, cfg.snddrv);
//			BuildGdx.audio.setDriver(Driver.Music, cfg.middrv);

//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						game.init();
//					} catch (Exception e) {
//						game.ThrowError("InitScreen error", e);
//					}
//				}
//			}).start();
		}

		engine.nextpage();
		frames++;
	}
}
