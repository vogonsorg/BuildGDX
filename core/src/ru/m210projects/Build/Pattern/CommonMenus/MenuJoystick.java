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

import ru.m210projects.Build.Input.GPManager;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildConfig.GameKeys;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuJoyList;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSlider;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuText;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;

public abstract class MenuJoystick extends BuildMenu {
	
	public MenuConteiner mJoyDevices;
	public MenuButton mJoyKey;
	public MenuConteiner mJoyTurn;
	public MenuConteiner mJoyLook;
	public MenuConteiner mJoyStrafe;
	public MenuConteiner mJoyMove;
	public MenuSlider mDeadZone;
	public MenuSlider mLookSpeed;
	public MenuSlider mTurnSpeed;
	public MenuSwitch mInvert;
	public MenuJoyList mList;
	
	
	public BuildMenu joyButtons;

	public abstract MenuTitle getTitle(BuildGame app, String text);

	public abstract String keyNames(int keycode);
	
	public MenuJoystick(final BuildGame app, int posx, int posy, int width, int menuHeight, int separatorHeight, BuildFont list, BuildFont buttons, BuildFont conteiner, BuildFont helpFont, int menupal, int list_len, int list_pal_left, int list_pal_right, int help_pal)
	{
		addItem(getTitle(app, "Joystick setup"), false);
		
		final BuildConfig cfg = app.pCfg;
		
		joyButtons = getJoyButtonsMenu(this, app, width, list, helpFont, posx, posy, list_len, list_pal_left, list_pal_right, menupal, help_pal);
		
		mJoyDevices = new MenuConteiner("Device:", buttons, conteiner, posx, posy, width, null, 0,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						cfg.gJoyDevice = item.num - 1;
					}
				}) {
			@Override
			public void open() {
				int controllers = app.pInput.ctrlGetControllers();
				if (this.list == null) {
					if (controllers > 0) {
						this.list = new char[controllers + 1][];
						this.list[0] = "Disabled".toCharArray();
						for (int i = 0; i < controllers; i++) {
							this.list[i + 1] = app.pInput.ctrlGetControllerName(i).toCharArray();
						}
					} else {
						this.list = new char[][]{"No joystick devices found".toCharArray()};
					}
				}

				// handles unplugged device(s) between runs
				int min = 0;
				int max = Math.max(0, controllers);
				int val = cfg.gJoyDevice + 1;
				this.num = val < min ? min : (val > max ? max : val);
			}
		};

		mJoyKey = new MenuButton("Configure buttons", buttons, posx, posy += separatorHeight, width, 1, 0, joyButtons, -1,
				null, 0) {
			@Override
			public void draw(MenuHandler handler) {
				super.draw(handler);
				mCheckEnableItem(app.pInput.ctrlGetControllers() > 0 && app.pInput.ctrlIsValidDevice(cfg.gJoyDevice));
			}
		};
		
		final char[][] StickName = { "Stick1_Y".toCharArray(), "Stick1_X".toCharArray(), "Stick2_Y".toCharArray(),
				"Stick2_X".toCharArray(), };

		mJoyTurn = new MenuConteiner("Turn axis:", buttons, posx, posy += separatorHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				cfg.gJoyTurnAxis = item.num;
			}
		}) {
			@Override
			public void open() {
				num = cfg.gJoyTurnAxis;
			}
		};
		mJoyTurn.list = StickName;

		mJoyLook = new MenuConteiner("Look axis:", buttons, posx, posy += menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				cfg.gJoyLookAxis = item.num;
			}
		}) {
			@Override
			public void open() {
				num = cfg.gJoyLookAxis;
			}
		};
		mJoyLook.list = StickName;

		mJoyStrafe = new MenuConteiner("Strafe axis:", buttons, posx, posy += menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				cfg.gJoyStrafeAxis = item.num;
			}
		}) {
			@Override
			public void open() {
				num = cfg.gJoyStrafeAxis;
			}
		};
		mJoyStrafe.list = StickName;

		mJoyMove = new MenuConteiner("Move axis:", buttons, posx, posy += menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				cfg.gJoyMoveAxis = item.num;
			}
		}) {
			@Override
			public void open() {
				num = cfg.gJoyMoveAxis;
			}
		};
		mJoyMove.list = StickName;

		posy += 5;
		mDeadZone = new MenuSlider(app.pSlider, "Dead zone:", buttons, posx, posy += menuHeight, width, cfg.gJoyDeadZone, 0, 0x8000,
				2048, new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						cfg.gJoyDeadZone = slider.value;
						app.pInput.ctrlSetDeadZone(cfg.gJoyDeadZone / 65536f);
					}
				}, true);
		mDeadZone.digitalMax = 65536.0f;

		mLookSpeed = new MenuSlider(app.pSlider, "Look speed:", buttons, posx, posy += menuHeight, width, cfg.gJoyLookSpeed, 0,
				0x28000, 4096, new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						cfg.gJoyLookSpeed = slider.value;
					}
				}, true);
		mLookSpeed.digitalMax = 65536.0f;

		mTurnSpeed = new MenuSlider(app.pSlider, "Turn speed:", buttons, posx, posy += menuHeight, width, cfg.gJoyTurnSpeed, 0,
				0x28000, 4096, new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						cfg.gJoyTurnSpeed = slider.value;
					}
				}, true);
		mTurnSpeed.digitalMax = 65536.0f;

		mInvert = new MenuSwitch("Invert look axis:", buttons, posx, posy += separatorHeight, width, cfg.gJoyInvert, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				cfg.gJoyInvert = sw.value;
			}
		}, "Yes", "No");
		
		addItem(mJoyDevices, true);
		addItem(mJoyKey, false);
		addItem(mJoyTurn, false);
		addItem(mJoyLook, false);
		addItem(mJoyStrafe, false);
		addItem(mJoyMove, false);
		addItem(mDeadZone, false);
		addItem(mLookSpeed, false);
		addItem(mTurnSpeed, false);
		addItem(mInvert, false);
	}

	public BuildMenu getJoyButtonsMenu(MenuJoystick parent, final BuildGame app, int width, BuildFont style, BuildFont helpFont, int posx, int posy, int list_len, int list_pal_left, int list_pal_right, int joymenupal, int help_pal)
	{
		BuildMenu menu = new BuildMenu();
		
		menu.addItem(parent.getTitle(app, "Config. buttons"), false);
		
		final BuildConfig cfg = app.pCfg;
		final GPManager gpmanager = app.pInput.ctrlGetGamepadManager();

		MenuProc callback = new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuJoyList item = (MenuJoyList) pItem;
				if (item.l_set == 0) {
					item.l_pressedId = null;
					item.l_set = 1;
				} else if (item.l_set == 1) {
					switch (item.l_pressedId) {
					case UP:
					case DW:
					case LEFT:
					case RIGHT:
					case ENTER:
					case ESC:
						item.l_set = 0;
						if(!gpmanager.isValidDevice(cfg.gJoyDevice)) break;
						for (int kb = 0; kb < gpmanager.getButtonCount(cfg.gJoyDevice); kb++) {
							if (gpmanager.buttonPressed(cfg.gJoyDevice, kb)) {
								if(item.l_nFocus < cfg.joymap.length)
									cfg.setButton(cfg.joymap[item.l_nFocus], kb);
								else cfg.setButton(cfg.keymap[item.l_nFocus - cfg.joymap.length], kb);
							}
						}
						break;
					default:
						if(!gpmanager.isValidDevice(cfg.gJoyDevice)) break;
						for (int kb = 0; kb < gpmanager.getButtonCount(cfg.gJoyDevice); kb++) {
							if (gpmanager.getButton(cfg.gJoyDevice, kb)) {
								if(item.l_nFocus < cfg.joymap.length)
									cfg.setButton(cfg.joymap[item.l_nFocus], kb);
								else cfg.setButton(cfg.keymap[item.l_nFocus - cfg.joymap.length], kb);
								item.l_set = 0;
							}
						}
						break;
					}
				}
				app.pInput.ctrlResetKeyStatus();
				if (item.l_nFocus == GameKeys.Show_Console.getNum()) {
					Console.setCaptureKey(cfg.gpadkeys[item.l_nFocus], 3);
				}
			}
		};

		mList = new MenuJoyList(app, joymenupal, style, posx, posy, width, list_len, list_pal_left, list_pal_right, callback) {
			@Override
			public String getKeyName(int keycode) {
				return keyNames(keycode);
			}
		};
		
		
		posy += mList.mFontOffset() * list_len;

		MenuText mText = new MenuText("UP/DOWN = Select action", helpFont, 160, posy += 2 * mList.mFontOffset(), 1);
		mText.pal = help_pal;
		MenuText mText2 = new MenuText("Enter = modify  Delete = clear", helpFont, 160, posy += mList.mFontOffset(), 1);
		mText2.pal = help_pal;
		
		menu.addItem(mList, true);
		menu.addItem(mText, false);
		menu.addItem(mText2, false);
		
		return menu;
	}

}
