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

import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Render.VideoMode.strvmodes;
import static ru.m210projects.Build.Render.VideoMode.validmodes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuList;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuResolutionList;
import ru.m210projects.Build.Pattern.MenuItems.MenuScroller;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;
import ru.m210projects.Build.Render.VideoMode;

public abstract class MenuVideoMode extends BuildMenu {
	
	protected VideoMode choosedMode;
	protected VideoMode currentMode;
	protected boolean isFullscreen;

	public abstract MenuTitle getTitle(BuildGame app, String text);
	
	public abstract void setMode(BuildConfig cfg);
	
	public BuildMenu getResolutionListMenu(final MenuVideoMode parent, final BuildGame app, int posx, int posy, int width, int nListItems, BuildFont style, int nListBackground) {
		BuildMenu menu = new BuildMenu();
		
		menu.addItem(parent.getTitle(app, "Resolution"), false);

		List<char[]> list = new ArrayList<char[]>();
		if (strvmodes != null) {
			for (int i = 0; i < strvmodes.length; i++)
				list.add(strvmodes[i].toCharArray());
		}

		MenuProc callback = new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuList item = (MenuList) pItem;
				if (item.l_nFocus == -1)
					return;

				currentMode = choosedMode = validmodes.get(item.l_nFocus);
				setMode(app.pCfg);
				parent.mLoadRes(app.pMenu, MenuOpt.Open);
				app.pMenu.mMenuBack();
			}
		};

		MenuList mSlot = new MenuResolutionList(app.pEngine, list, style, posx, posy, width, 1, null, callback, nListItems, nListBackground);

		MenuScroller slider = new MenuScroller(app.pSlider, mSlot, width + posx - app.pSlider.getScrollerWidth());
		
		menu.addItem(mSlot, true);
		menu.addItem(slider, false);
		
		return menu;
	}
	
	public MenuVideoMode(final BuildGame app, int posx, int posy, int width, int itemHeight, BuildFont style, BuildFont conteiner, BuildFont apply, int nListItems, int nListWidth, int nBackground) {
		
		addItem(getTitle(app, "Video mode"), false);
		
		final BuildConfig cfg = app.pCfg;
		MenuProc callback = new MenuProc() {
			public void run(MenuHandler handler, MenuItem pItem) {
				cfg.fullscreen = isFullscreen ? 1 : 0;
				currentMode = choosedMode;
				setMode(cfg);
			}
		};
		
		final BuildMenu mResList = getResolutionListMenu(this, app, posx + (width - nListWidth) / 2, posy + 2 * style.nHeight, nListWidth, nListItems, style, nBackground);
		
		MenuConteiner mResolution = new MenuConteiner("Resolution: ", style, conteiner, posx,
				posy += itemHeight, width, strvmodes, 0, new MenuProc() {
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						choosedMode = validmodes.get(item.num);
					}
				}) {
			
			@Override
			public boolean callback(MenuHandler handler, MenuOpt opt) {
				switch(opt)
				{
				case LEFT:
				case MWDW:
					if(num > 0) num--;
					else num = 0;
					if(callback != null)
						callback.run(handler, this);
					return false;
				case RIGHT:
				case MWUP:
					if(num < list.length - 1) num++;
					else num = list.length - 1;
					if(callback != null)
						callback.run(handler, this);
					return false;
				case ENTER:
				case LMB:
					handler.mOpen(mResList, -1);
					return false;
				default:
					return m_pMenu.mNavigation(opt);
				}
			}
			
			@Override
			public void open() {
				num = -1;
				for (int m = 0; m < validmodes.size(); m++) {
					if ((validmodes.get(m).xdim == xdim)
							&& (validmodes.get(m).ydim == ydim)) {
						num = m;
						break;
					}
				}
				
				if (num != -1) {
					currentMode = validmodes.get(num);
					choosedMode = currentMode;
				} else {
					currentMode = new VideoMode(Gdx.graphics.getDisplayMode());
				}
			}

			public void draw(MenuHandler handler) {
				int px = x, py = y;
				
				char[] key = null;
				if (num != -1 && list != null)
					key = list[num];
				else
					key = new String(cfg.ScreenWidth + " x " + cfg.ScreenHeight + " 32bpp").toCharArray();	//XXX	

				int pal = handler.getPal(font, this);
				int shade = handler.getShade(this);
				font.drawText(px, py, text, shade, pal, TextAlign.Left, 0, false);
				
				if(key == null) return;

				listFont.drawText(x + width - 1 - listFont.getWidth(key), py, key, shade, pal, TextAlign.Left, 0, false);
				
				handler.mPostDraw(this);
			}
		};
		
		MenuConteiner mRenderer = new MenuConteiner("Renderer: ", style, conteiner, posx,
				posy += itemHeight, width, new String[] { "Polymost" }, 0, null) {
			@Override
			public void draw(MenuHandler handler) {
				super.draw(handler);
				mCheckEnableItem(false);
			}
			
			@Override
			public void mCheckEnableItem(boolean nEnable) {
				if (nEnable) 
					flags = 3 | 4;
				else flags = 3;
			}
		};

		MenuSwitch mFullscreen = new MenuSwitch("Fullscreen:", style, posx,
				posy += itemHeight, width, cfg.fullscreen == 1, new MenuProc() {
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSwitch sw = (MenuSwitch) pItem;
						isFullscreen = sw.value;
					}
				}, null, null) {
			
			public void open() {
				value = isFullscreen = (cfg.fullscreen == 1);
			}
		};
		
		MenuButton mApplyChanges = new MenuButton("Apply changes", apply, 0, posy += 2 * itemHeight, 320, 1, 0, null, -1, callback, 0) {
			@Override
			public void draw(MenuHandler handler) {
				super.draw(handler);
				mCheckEnableItem(choosedMode != null && (choosedMode != currentMode || isFullscreen != (cfg.fullscreen == 1)));
			}
			
			@Override
			public void mCheckEnableItem(boolean nEnable) {
				if (nEnable) 
					flags = 3 | 4;
				else flags = 3;
			}
		};

		addItem(mResolution, true);
		addItem(mRenderer, false);
		addItem(mFullscreen, false);
		addItem(mApplyChanges, false);
	}
}
