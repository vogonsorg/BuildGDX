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

import com.badlogic.gdx.ScreenAdapter;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildConfig.MenuKeys;

public abstract class SkippableAdapter extends ScreenAdapter {
	
	protected BuildGame game;
	protected Engine engine;
	
	public SkippableAdapter(BuildGame game)
	{
		this.game = game;
		this.engine = game.pEngine;
	}
	
	
	protected Runnable skipCallback;
	protected boolean escSkip;

	public SkippableAdapter setSkipping(Runnable skipCallback) {
		this.skipCallback = skipCallback;
		return this;
	}
	
	public SkippableAdapter escSkipping(boolean escSkip) {
		this.escSkip = escSkip;
		return this;
	}
	
	public abstract void process(float delta);

	public abstract void skip();
	
	@Override
	public final void render(float delta) {
		engine.clearview(0);
		engine.sampletimer();

		if(!skippingHandler()) 
			process(delta);

		engine.nextpage();
	}
	
	private boolean skippingHandler() {
		if((escSkip && (game.pInput.ctrlGetInputKey(MenuKeys.Menu_Open, true) 
				|| game.pInput.ctrlPadStatusOnce(MenuKeys.Menu_Open))) 
				|| (!escSkip && game.pInput.ctrlKeyPressed())) {
			
			skip();
			if(skipCallback != null) {
				skipCallback.run();
				skipCallback = null;
			}
			game.pInput.ctrlResetKeyStatus();
			return true;
		}
		
		return false;
	}
}
