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

package ru.m210projects.Build.Pattern.CommonMenus;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.GLFrame;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSlider;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;

public abstract class MenuColorCorr extends BuildMenu {

	public abstract MenuTitle getTitle(BuildGame app, String text);
	
	public MenuColorCorr(final BuildGame app, int posx, int posy, int width, int menuHeight, BuildFont style, BuildFont reset) {
		addItem(getTitle(app, "Color correction"), false);
		
		final BuildConfig cfg = app.cfg;

		final MenuSlider mGamma = new MenuSlider(app.slider, "GAMMA:", style, posx, posy += menuHeight, width, (int) ((1 - cfg.gamma) * 4096), 0, 4096, 64,
		new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSlider slider = (MenuSlider) pItem;

				float gamma = slider.value / 4096.0f;
				if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(1 - gamma, cfg.brightness, cfg.contrast))
					cfg.gamma = (1 - gamma);
				else 
					slider.value = (int) ((1 - cfg.gamma) * 4096);
			}
		}, true);
		mGamma.digitalMax = 4096;
		
		final MenuSlider mBrightness = new MenuSlider(app.slider, "Brightness:", style, posx, posy += menuHeight, width,(int) (cfg.brightness * 4096), -4096, 4096, 64,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						float brightness = slider.value / 4096.0f;
						if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(cfg.gamma, brightness, cfg.contrast))
							cfg.brightness = brightness;
						else 
							slider.value = (int) (cfg.brightness * 4096);
					}
				}, true);
		mBrightness.digitalMax = 4096;
		
		final MenuSlider mContrast = new MenuSlider(app.slider, "Contrast:", style, posx, posy += menuHeight, width, (int) (cfg.contrast * 4096), 0, 8192, 64,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						float contrast = slider.value / 4096.0f;
						if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(cfg.gamma, cfg.brightness, contrast))
							cfg.contrast = contrast;
						else 
							slider.value = (int) (cfg.contrast * 4096);
					}
				}, true);
		mContrast.digitalMax = 4096;

		MenuButton mDefault = new MenuButton("Set to default", reset, 0, posy += 2 * menuHeight, 320, 1, 0, null, -1, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				cfg.gamma = 1.0f;
				cfg.brightness = 0.0f;
				cfg.contrast = 1.0f;
				
				mGamma.value = (int) ((1 - cfg.gamma) * 4096);
				mBrightness.value = (int) (cfg.brightness * 4096);
				mContrast.value = (int) (cfg.contrast * 4096);
				((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(cfg.gamma, cfg.brightness, cfg.contrast);
			}
		}, -1);
		
		addItem(mGamma, true);
		addItem(mBrightness, false);
		addItem(mContrast, false);
		addItem(mDefault, false);
	}
}
