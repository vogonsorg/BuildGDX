package ru.m210projects.Build.Pattern.MenuItems;

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

import static ru.m210projects.Build.Gameutils.*;

import static ru.m210projects.Build.Input.Keymap.MOUSE_LBUTTON;
import static ru.m210projects.Build.Input.Keymap.MOUSE_RBUTTON;
import static ru.m210projects.Build.Input.Keymap.MOUSE_WHELLDN;
import static ru.m210projects.Build.Input.Keymap.MOUSE_WHELLUP;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Pattern.BuildConfig.*;
import static ru.m210projects.Build.Pragmas.*;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Pattern.BuildControls;
import ru.m210projects.Build.Pattern.BuildFont;

public abstract class MenuHandler {
	
	public int mCount = 0;
	public BuildMenu[] mMenuHistory;
	public boolean gShowMenu;

	protected boolean mUseMouse;
	
	private float keycount = 0;
	private final float hitTime = 0.5f; //kTimerRate;
	private final float changeTime = 0.05f;

	public enum MenuOpt {
		NONE, //0
		ANY, //1
		UP, //2
		DW, //3
		LEFT, //4
		RIGHT, //5
		ENTER, //6
		ESC, //7
		SPACE, //8
		BSPACE, //9 backspace
		DELETE, //10
		LMB, //11
		PGUP, //12
		PGDW, //13
		HOME, //14
		END, //15
		MWUP, //16 mouse wheel up
		MWDW, //17 mouse wheel down
		RMB, //18
		
		Open, //0x8000
		Close //0x8001
	}
	
	public MenuHandler()
	{
		mMenuHistory = new BuildMenu[10];
	}

	//item == m_pMenu.m_pItems[m_pMenu.m_nFocus] for get focused shade
	public abstract int getShade(MenuItem item);
	
	public abstract int getPal(BuildFont font, MenuItem item);
	
	public abstract void mPostDraw(MenuItem item);

	public abstract void mDrawMouse(int x, int y);
	
	public abstract void mSound(MenuItem item, MenuOpt opt);

	public MenuOpt mUpdateMouse(BuildControls input) {
		if(Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0)
			mUseMouse = true;

		if(!mUseMouse)
			return null;

		int mx = BClipRange(Gdx.input.getX(), 0, xdim);
		int my = BClipRange(Gdx.input.getY(), 0, ydim);

		if(!input.ctrlMenuMouse())
			return null;
		
		BuildMenu pMenu = mMenuHistory[0];
		
		if(mCount > 1) {
			//Back button XXX
//			int bzoom = (int) divscale(16384, gViewX1Scaled, 16);
//			int size = mulscale(tilesizy[40], bzoom, 16);
//			int bx = 0;
//			int by = ydim - size;
//			if(mx >= bx && mx < bx + size)
//				if(my >= by && my < by + size)
//					if(input.ctrlKeyStatusOnce(MOUSE_LBUTTON)) {
//						mMenuBack();
//						return null;
//					}
		}
		
		MenuOpt opt = null;

		//Sliders
		if(pMenu.m_nFocus != -1 && pMenu.mCheckMouseFlag(pMenu.m_nFocus)) {
			if(pMenu.m_pItems[pMenu.m_nFocus] instanceof MenuSlider
					|| pMenu.m_pItems[pMenu.m_nFocus] instanceof MenuScroller
				|| pMenu.m_pItems[pMenu.m_nFocus] instanceof MenuFileBrowser
				|| (pMenu.m_pItems[pMenu.m_nFocus] instanceof MenuKeyboardList 
						&& ((MenuKeyboardList)pMenu.m_pItems[pMenu.m_nFocus]).l_set == 0))
			{
				if(input.ctrlKeyStatus(MOUSE_LBUTTON)) 
					opt = MenuOpt.LMB;
			} 
			else if(input.ctrlKeyStatusOnce(MOUSE_LBUTTON)) 
				opt = MenuOpt.LMB;
		}
		
		if(input.ctrlKeyStatusOnce(MOUSE_RBUTTON)) 
			opt = MenuOpt.RMB;
		if(input.ctrlKeyStatusOnce(MOUSE_WHELLUP)) 
			opt = MenuOpt.MWUP;
		if(input.ctrlKeyStatusOnce(MOUSE_WHELLDN)) 
			opt = MenuOpt.MWDW;

		short focus = mCheckButton(pMenu, input, mx, my);
		if(focus != -1 && pMenu.mCheckItemsFlags(focus) && mUseMouse)
			pMenu.m_nFocus = focus;

		return opt;
	}
	
	private short mCheckButton(BuildMenu pMenu, BuildControls input, int x, int y)
	{
		int oxdim = xdim;
		int xdim = (4 * ydim) / 3;
		int normxofs = x - oxdim / 2;
		int touchX = scale(normxofs, 320, xdim) + 320 / 2;
		int touchY = (int) mulscale(y, divscale(200, ydim, 16), 16);

		for(short i = 0; i < pMenu.m_pItems.length; i++)
		{
			if(pMenu.mCheckMouseFlag(i) && 
					pMenu.m_pItems[i].mouseAction(touchX, touchY) && !input.ctrlKeyStatus(MOUSE_LBUTTON))
				return i;
		}
		return -1;
	}
	
	public void mKeyHandler(BuildControls input, float delta) {

		BuildMenu pMenu = mMenuHistory[0];

		if(pMenu != null) {
			MenuOpt opt = MenuOpt.ANY;

			if(input.ctrlKeyStatusOnce(Keys.UP) || input.ctrlPadStatusOnce(GameKeys.Move_Forward))
				opt = MenuOpt.UP;
			if(input.ctrlKeyStatusOnce(Keys.DOWN) || input.ctrlPadStatusOnce(GameKeys.Move_Backward)) 
				opt = MenuOpt.DW;
			if(input.ctrlKeyStatusOnce(Keys.LEFT) || input.ctrlPadStatusOnce(GameKeys.Turn_Left)) 
				opt = MenuOpt.LEFT;
			if(input.ctrlKeyStatusOnce(Keys.RIGHT) || input.ctrlPadStatusOnce(GameKeys.Turn_Right)) 
				opt = MenuOpt.RIGHT;
			if(input.ctrlKeyStatusOnce(Keys.ENTER) || input.ctrlPadStatusOnce(GameKeys.Open)) 
				opt = MenuOpt.ENTER;
			if(input.ctrlGetInputKey(MenuKeys.Menu_Open, true)) 
				opt = MenuOpt.ESC;
			if(input.ctrlKeyStatusOnce(Keys.SPACE)) 
				opt = MenuOpt.SPACE;
			if(input.ctrlKeyStatusOnce(Keys.BACKSPACE)) 
				opt = MenuOpt.BSPACE;
			if(input.ctrlKeyStatusOnce(Keys.FORWARD_DEL)) 
				opt = MenuOpt.DELETE;
			if(input.ctrlKeyStatusOnce(Keys.PAGE_UP)) 
				opt = MenuOpt.PGUP;
			if(input.ctrlKeyStatusOnce(Keys.PAGE_DOWN)) 
				opt = MenuOpt.PGDW;
			if(input.ctrlKeyStatusOnce(Keys.HOME)) 
				opt = MenuOpt.HOME;
			if(input.ctrlKeyStatusOnce(Keys.END)) 
				opt = MenuOpt.END;

			if(opt != MenuOpt.ANY) mUseMouse = false;
			MenuOpt mopt = mUpdateMouse(input);
			if(mopt != null) opt = mopt;
			
			if(pMenu.mLoadRes(this, opt)) 
				mMenuBack();

			if(!BuildGdx.input.isTouched() && input.ctrlKeyPressed()
					&& !input.ctrlKeyPressed(Keys.ENTER) 
					&& !input.ctrlKeyPressed(Keys.ESCAPE)) {

				keycount += delta;
				if(keycount >= hitTime) {
					if(keycount >= (hitTime + changeTime)) {
						input.ctrlResetInput();
						keycount = hitTime;
					}
				}
			} else keycount = 0;
		}
	}
	
	public void mOpen(BuildMenu pMenu, int nItem)
	{
		if(pMenu == null || mCount == 8) return;

		mMenuHistory[0] = pMenu;
		mMenuHistory[++mCount] = pMenu;
	  
		pMenu.open(this, nItem);
		gShowMenu = true;
		
		BuildGdx.input.setCursorCatched(false);
	}
	
	public void mClose()
	{
		Arrays.fill(mMenuHistory, null);
		mCount = 0;
		
		gShowMenu = false;
    	BuildGdx.input.setCursorCatched(true);
    	BuildGdx.input.setCursorPosition(xdim / 2, ydim / 2);
	}
	
	public void mMenuBack() {
		if(mCount > 0) {
			if(mMenuHistory[0] != null)
				mMenuHistory[0].mLoadRes(this, MenuOpt.Close);
			mCount = BClipLow(mCount - 1, 0);
			if(mCount > 0) {
				mMenuHistory[0] = mMenuHistory[mCount];
			} else {
				mClose();
			}
		}
	}
	
	public void mMenuBack(MenuOpt opt) {
		if(mCount > 0) {
			if(mMenuHistory[0] != null)
				mMenuHistory[0].mLoadRes(this, MenuOpt.Close);
			mCount = BClipLow(mCount - 1, 0);
			if(mCount > 0) {
				mMenuHistory[0] = mMenuHistory[mCount];
				mMenuHistory[0].mLoadRes(this, opt);
			} else {
				mClose();
			}
		}
	}

	public BuildMenu getCurrentMenu()
	{
		return mMenuHistory[0];
	}
	
	public boolean isOpened(BuildMenu pMenu)
	{
		return getCurrentMenu() == pMenu;
	}

	public void mDrawMenu() {
		if(mMenuHistory[0] != null) 
			mMenuHistory[0].mDraw(this);
		
		if(mUseMouse)
			mDrawMouse(BuildGdx.input.getX(), BuildGdx.input.getY());
	}
}
