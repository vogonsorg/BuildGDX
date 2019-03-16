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

import static ru.m210projects.Build.Engine.pow2long;
import static ru.m210projects.Build.Engine.usehightile;
import static ru.m210projects.Build.Engine.usemodels;
import static ru.m210projects.Build.Engine.usevoxels;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;
import ru.m210projects.Build.Render.GLInfo;

public abstract class MenuVideoSetup extends BuildMenu {
	
	public MenuButton mVideoMode;
	public MenuButton mColorMode;
	public MenuConteiner sFilter;		
	public MenuConteiner sAnisotropy;
	public MenuSwitch sWidescreen;
	public MenuConteiner mMenuFPS;
	public MenuSwitch sVSync;
	public  MenuSwitch UseVoxels;
	public MenuSwitch UseModels;
	public MenuSwitch Usehrp;
	
	public abstract MenuTitle getTitle(BuildGame app, String text);
	
	public abstract MenuColorCorr getColorCorrectionMenu(BuildGame app);
	
	public abstract MenuVideoMode getVideoModeMenu(BuildGame app);
	
	public MenuVideoSetup(final BuildGame app, int posx, int posy, int width, int menuHeight, BuildFont style, BuildFont conteiner, BuildFont reset)
	{
		addItem(getTitle(app, "Video setup"), false);
		
		final BuildConfig cfg = app.pCfg;

		mVideoMode = new MenuButton("Video mode", style, 0, posy += menuHeight, 320, 1, 0, getVideoModeMenu(app), -1, null, 0);

		mColorMode = new MenuButton("Color correction", style, posx, posy += menuHeight, width, 1, 0, getColorCorrectionMenu(app), -1, null, 0);

		sFilter = new MenuConteiner("Texture mode:", style, conteiner, posx, posy += 2 * menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				int filter = item.num;
				if (filter == 2)
					filter = 5;

				Console.Set("r_texturemode", filter);
				app.pEngine.render.gltexapplyprops();
			}
		}) {
			@Override
			public void open() {
				if (this.list == null) {
					this.list = new char[3][];
					this.list[0] = "Classic".toCharArray();
					this.list[1] = "Bilinear".toCharArray();
					this.list[2] = "Trilinear".toCharArray();
				}

				int filter = Console.Geti("r_texturemode");
				if (filter == 5)
					filter = 2;
				num = filter;
			}
		};
		
		sAnisotropy = new MenuConteiner("Anisotropy: ", style, conteiner, posx, posy += menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				app.pEngine.setanisotropy(cfg, pow2long[item.num]);
			}
		}) {
			@Override
			public void open() {
				if (this.list == null) {
					this.list = new char[calcAnisotropy((int) GLInfo.maxanisotropy) + 1][];
					this.list[0] = "None".toCharArray();
					for (int i = 1; i < list.length; i++)
						this.list[i] = (Integer.toString(pow2long[i]) + "x").toCharArray();
				}
				if (cfg.glanisotropy > GLInfo.maxanisotropy)
					app.pEngine.setanisotropy(cfg, (int) GLInfo.maxanisotropy);
				num = calcAnisotropy(cfg.glanisotropy);
			}
		};

		sWidescreen = new MenuSwitch("Widescreen:", style, posx, posy += menuHeight, width, cfg.widescreen == 1,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSwitch sw = (MenuSwitch) pItem;
						app.pEngine.setwidescreen(cfg, sw.value);
					}
				}, null, null);
		
		mMenuFPS = new MenuConteiner("Framerate limit:", style, conteiner, posx, posy += menuHeight, width, null, 0,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						
						int fps = 0;
						switch(item.num) {
							case 1: fps = 30; break;
							case 2: fps = 60; break;
							case 3: fps = 120; break;
							case 4: fps = 144; break;
						}
						cfg.fpslimit = fps;

						BuildGdx.app.setMaxFramerate(fps);
					}
				}) {
			@Override
			public void open() {
				if (this.list == null) {
					this.list = new char[5][];
					this.list[0] = "None".toCharArray();
					this.list[1] = "30 fps".toCharArray();
					this.list[2] = "60 fps".toCharArray();
					this.list[3] = "120 fps".toCharArray();
					this.list[4] = "144 fps".toCharArray();
				}
				
				num = cfg.checkFps(cfg.fpslimit);
			}
		};

		sVSync = new MenuSwitch("VSync:", style, posx, posy += menuHeight, width, cfg.gVSync, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				cfg.gVSync = sw.value;
				try { // crash if hires textures loaded
					Gdx.graphics.setVSync(cfg.gVSync);
				} catch (Exception e) {}
			}
		}, null, null);
		posy += 5;
		UseVoxels = new MenuSwitch("Voxels:", style, posx, posy += menuHeight, width, usevoxels, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				usevoxels = sw.value;
			}
		}, null, null) {
			@Override
			public void open() {
				value = usevoxels;
			}
		};
		UseModels = new MenuSwitch("3d models:", style, posx, posy += menuHeight, width, usemodels, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				usemodels = sw.value;
			}
		}, null, null) {
			@Override
			public void open() {
				value = usemodels;
			}
		};
		Usehrp = new MenuSwitch("True color textures:", style, posx, posy += menuHeight, width, usehightile, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				usehightile = sw.value;
				app.pEngine.getrender().gltexinvalidateall(1);
			}
		}, null, null) {
			@Override
			public void open() {
				value = usehightile;
			}
		};

		addItem(mVideoMode, true);
		addItem(mColorMode, false);
		addItem(sFilter, false);
		addItem(sAnisotropy, false);
		addItem(sWidescreen, false);
		addItem(mMenuFPS, false);
		addItem(sVSync, false);
		addItem(UseVoxels, false);
		addItem(UseModels, false);
		addItem(Usehrp, false);
	}
	
	protected int calcAnisotropy(int anisotropy) {
		int anisotropysize = 0;
		for (int s = anisotropy; s > 1; s >>= 1)
			anisotropysize++;
		return anisotropysize;
	}

}
