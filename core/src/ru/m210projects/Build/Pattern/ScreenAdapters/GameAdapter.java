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

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.GLFrame;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildEngine;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildNet;
import ru.m210projects.Build.Pattern.BuildGame.NetMode;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Settings.BuildConfig;

public abstract class GameAdapter extends ScreenAdapter {
	
	protected BuildGame game;
	protected BuildNet pNet;
	protected MenuHandler pMenu;
	protected BuildEngine pEngine;
	protected BuildConfig pCfg;
	protected Runnable gScreenCapture;
	protected LoadingAdapter load;
	public byte[] captBuffer;

	public GameAdapter(final BuildGame game, LoadingAdapter load)
	{
		this.game = game;
		this.pNet = game.pNet;
		this.pMenu = game.pMenu;
		this.pEngine = game.pEngine;
		this.pCfg = game.pCfg;
		this.load = load;
	}
	
	public void PreFrame(BuildNet net) { /* nothing */ }
	
	public void PostFrame(BuildNet net) { /* nothing */ }

	public abstract void ProcessFrame(BuildNet net);
	
	/** 
	 * Don't use DrawWorld() for save game!
	 */
	public abstract void DrawWorld(float smooth);
	
	public abstract void DrawHud(float smooth);
	
	public abstract void KeyHandler();
	
	public abstract void sndHandlePause(boolean pause);
	
	protected abstract boolean prepareboard(String map);

	public GameAdapter setTitle(String title)
	{
		load.setTitle(title);
		return this;
	}
	
	public GameAdapter loadboard(final String map, final Runnable prestart)
	{
		pNet.ready2send = false;
		game.changeScreen(load);
		load.init(new Runnable() {
			@Override
			public void run() {
				if(prepareboard(map)) {
					if(prestart != null)
						prestart.run();
					startboard(startboard);
				}
			}
		});
		
		return this;
	}
	
	private Runnable startboard = new Runnable() {
		@Override
		public void run() {
			pNet.WaitForAllPlayers(0);
			System.gc();
			
			pNet.ResetTimers();
			game.pInput.resetMousePos();
			pNet.ready2send = true;
			game.changeScreen(GameAdapter.this);
			
			pEngine.faketimerhandler();
		}
	};

	protected void startboard(Runnable startboard) {
		startboard.run();
	}
	
	@Override
	public void show() {
		pMenu.mClose();
	}
	
	@Override
	public void render(float delta) {
		KeyHandler();

		if (numplayers > 1) {
			pEngine.faketimerhandler();
			
			pNet.GetPackets();
			while (pNet.gPredictTail < pNet.gNetFifoHead[myconnectindex] && !game.gPaused) 
				pNet.UpdatePrediction(pNet.gFifoInput[pNet.gPredictTail & kFifoMask][myconnectindex]);
		} else pNet.bufferJitter = 0;
		
		PreFrame(pNet);

		int i;
		while (pNet.gNetFifoHead[myconnectindex] - pNet.gNetFifoTail > pNet.bufferJitter && !game.gExit) {
			for (i = connecthead; i >= 0; i = connectpoint2[i])
				if (pNet.gNetFifoTail == pNet.gNetFifoHead[i]) break;
			if (i >= 0) break;
			
			pEngine.faketimerhandler(); //game timer sync
			ProcessFrame(pNet);
		}
		
		pNet.CheckSync();
		
		float smoothratio = 65536;
		if (!game.gPaused && (game.nNetMode != NetMode.Single || !pMenu.gShowMenu && !Console.IsShown())) {
			smoothratio = pEngine.getsmoothratio();
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
		
		DrawHud(smoothratio);
		game.pInt.restoreinterpolations();
		
		if(pMenu.gShowMenu)
			pMenu.mDrawMenu();
		
		PostFrame(pNet);

		if (pCfg.gShowFPS)
			pEngine.printfps(pCfg.gFpsScale);

		pEngine.sampletimer();
		pEngine.nextpage();
	}
	
	public void capture(final int width, final int height) {
		gScreenCapture = new Runnable() {
			@Override
			public void run() {
				captBuffer = pEngine.screencapture(width, height);
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
				pNet.ototalclock = totalclock;
			}
			sndHandlePause(game.gPaused);
		}
		game.updateColorCorrection();
	}

}
