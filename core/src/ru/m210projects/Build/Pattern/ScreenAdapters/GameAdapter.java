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

import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Net.Mmulti.*;
import static ru.m210projects.Build.Pattern.BuildNet.*;

import com.badlogic.gdx.ScreenAdapter;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.GLFrame;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildNet;
import ru.m210projects.Build.Pattern.BuildGame.NetMode;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;

public abstract class GameAdapter extends ScreenAdapter {
	
	protected BuildGame game;
	protected BuildNet net;
	protected MenuHandler menu;
	protected Engine engine;
	protected BuildConfig cfg;
	protected Runnable gScreenCapture;
	protected LoadingAdapter load;
	public byte[] captBuffer;

	public GameAdapter(final BuildGame game, LoadingAdapter load)
	{
		this.game = game;
		this.net = game.pNet;
		this.menu = game.pMenu;
		this.engine = game.pEngine;
		this.cfg = game.pCfg;
		this.load = load;
	}
	
	public void PreFrame(BuildNet net) { /* nothing */ }
	
	public void PostFrame(BuildNet net) { /* nothing */ }

	public abstract void ProcessFrame(BuildNet net);
	
	/** 
	 * Don't use DrawWorld() for save game!
	 */
	public abstract void DrawWorld(float smooth);
	
	public abstract void DrawHud();
	
	public abstract void KeyHandler();
	
	public abstract void sndHandlePause(boolean pause);
	
	protected abstract boolean prepareboard(String map);
	
	public void loadboard(final String map, final Runnable prestart)
	{
		net.ready2send = false;
		game.changeScreen(load);
		load.init(new Runnable() {
			@Override
			public void run() {
				if(prepareboard(map)) {
					if(prestart != null)
						prestart.run();
					startboard();
				}
			}
		});
	}

	protected void startboard() 
	{
		net.WaitForAllPlayers(0);
		System.gc();
		
		net.ResetTimers();
		net.ready2send = true;
		game.changeScreen(this);
		
		engine.faketimerhandler();
	}
	
	@Override
	public void show() {
		menu.mClose();
	}
	
	@Override
	public void render(float delta) {
		KeyHandler();

		if (numplayers > 1) {
			engine.faketimerhandler();
			
			net.GetPackets();
			while (net.gPredictTail < net.gNetFifoHead[myconnectindex] && !game.gPaused) 
				net.UpdatePrediction(net.gFifoInput[net.gPredictTail & kFifoMask][myconnectindex]); 

		} else net.bufferJitter = 0;
		
		PreFrame(net);

		int i;
		while (net.gNetFifoHead[myconnectindex] - net.gNetFifoTail > net.bufferJitter && !game.gExit) {
			for (i = connecthead; i >= 0; i = connectpoint2[i])
				if (net.gNetFifoTail == net.gNetFifoHead[i]) break;
			if (i >= 0) break;
			game.pInt.updateinterpolations();
			ProcessFrame(net);
		}
		
		net.CheckSync();
		
		float smoothratio = 65536;
		if (!game.gPaused && (!menu.gShowMenu || game.nNetMode != NetMode.Single) && !Console.IsShown()) {
			smoothratio = engine.getsmoothratio();
			if (smoothratio < 0 || smoothratio > 0x10000) {
//				System.err.println("Interpolation error " + smoothratio);
				smoothratio = BClipRange(smoothratio, 0, 0x10000);
			}
		}

		game.pInt.dointerpolations(smoothratio);
		DrawWorld(smoothratio); //smooth sprites

		if (gScreenCapture != null) {
			gScreenCapture.run();
			gScreenCapture = null;
		}
		
		DrawHud();
		game.pInt.restoreinterpolations();
		
		if(menu.gShowMenu)
			menu.mDrawMenu();
		
		PostFrame(net);

		if (cfg.gShowFPS)
			engine.printfps(cfg.gFpsScale);

		engine.sampletimer();
		engine.nextpage();
	}
	
	public void capture(final int width, final int height) {
		gScreenCapture = new Runnable() {
			@Override
			public void run() {
				captBuffer = engine.screencapture(width, height);
			}
		};
	}
	
	@Override
	public void pause () {
		if (game.nNetMode == NetMode.Single && numplayers < 2) {
			game.gPaused = true;
			sndHandlePause(game.gPaused);
		}

		if (BuildGdx.app.getFrameType() == FrameType.GL)
			((GLFrame) BuildGdx.app.getFrame()).setDefaultDisplayConfiguration();
	}

	@Override
	public void resume () {
		if (game.nNetMode == NetMode.Single && numplayers < 2) {
			{
				game.gPaused = false;
				net.ototalclock = totalclock;
			}
			sndHandlePause(game.gPaused);
		}
		game.updateColorCorrection();
	}

}
