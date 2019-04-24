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
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildConfig.MenuKeys;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;

public abstract class MenuAdapter extends ScreenAdapter {
	
	protected BuildGame game;
	protected MenuHandler menu;
	protected Engine engine;
	protected BuildConfig cfg;
	protected BuildMenu mainMenu;

	public abstract void draw(float delta);
	
	public void process(float delta) { }
	
	public MenuAdapter(final BuildGame game, BuildMenu mainMenu)
	{
		this.game = game;
		this.menu = game.pMenu;
		this.engine = game.pEngine;
		this.cfg = game.pCfg;
		this.mainMenu = mainMenu;
	}
	
	@Override
	public void render(float delta) {
		engine.clearview(0);
		engine.sampletimer();
		
		draw(delta);

		engine.handleevents();
		
		if (menu.gShowMenu) {
			menu.mKeyHandler(game.pInput, delta);
			menu.mDrawMenu();
		} else {
			if (game.pInput.ctrlGetInputKey(MenuKeys.Menu_Toggle, true))
				menu.mOpen(mainMenu, -1);
		}

		process(delta);

		if (cfg.gShowFPS)
			engine.printfps(cfg.gFpsScale);

		engine.nextpage();
	}
}