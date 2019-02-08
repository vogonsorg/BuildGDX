package ru.m210projects.Build.Pattern.MenuItems;

import static ru.m210projects.Build.Gameutils.*;
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

import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Pattern.BuildConfig.*;

import java.util.Arrays;

import com.badlogic.gdx.Input.Keys;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Pattern.BuildControls;
import ru.m210projects.Build.Pattern.BuildFont;

public abstract class MenuHandler {
	
	public int mCount = 0;
	public final int kMaxGameMenus;
	public BuildMenu[] mMenuHistory;
	public BuildMenu[] mMenus;
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
		
		Open,
		Close
	}
	
	public MenuHandler(int nMaxMenus)
	{
		this.kMaxGameMenus = nMaxMenus;
		mMenus = new BuildMenu[kMaxGameMenus];
		mMenuHistory = new BuildMenu[10];
	}

	//item == m_pMenu.m_pItems[m_pMenu.m_nFocus] for get focused shade
	public abstract int getShade(MenuItem item);
	
	public abstract int getPal(BuildFont font, MenuItem item);
	
	public abstract MenuOpt mUpdateMouse();
	
	public abstract void mDrawMouse(int x, int y);
	
	public abstract int mDrawSlider(int x, int y, int nPos, int len, boolean focus);
	
	public void mKeyHandler(BuildControls input, float delta) {

		BuildMenu pMenu = mMenuHistory[0];

		if(pMenu != null) {
			MenuOpt opt = MenuOpt.ANY;

			if(input.ctrlKeyStatusOnce(Keys.UP) || input.ctrlPadStatusOnce(Move_Forward))
				opt = MenuOpt.UP;
			if(input.ctrlKeyStatusOnce(Keys.DOWN) || input.ctrlPadStatusOnce(Move_Backward)) 
				opt = MenuOpt.DW;
			if(input.ctrlKeyStatusOnce(Keys.LEFT) || input.ctrlPadStatusOnce(Turn_Left)) 
				opt = MenuOpt.LEFT;
			if(input.ctrlKeyStatusOnce(Keys.RIGHT) || input.ctrlPadStatusOnce(Turn_Right)) 
				opt = MenuOpt.RIGHT;
			if(input.ctrlKeyStatusOnce(Keys.ENTER) || input.ctrlPadStatusOnce(Open)) 
				opt = MenuOpt.ENTER;
			if(input.ctrlGetInputKey(Menu_open, true)) 
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
				opt = MenuOpt.DW;
			if(input.ctrlKeyStatusOnce(Keys.HOME)) 
				opt = MenuOpt.HOME;
			if(input.ctrlKeyStatusOnce(Keys.END)) 
				opt = MenuOpt.END;

			if(opt != MenuOpt.ANY) mUseMouse = false;
			MenuOpt mopt = mUpdateMouse();
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

	public boolean isOpened(BuildMenu pMenu)
	{
		return mMenuHistory[0] == pMenu;
	}

	public void mDrawMenu() {
		if(mMenuHistory[0] != null) 
			mMenuHistory[0].mDraw(this);
		
		if(mUseMouse)
			mDrawMouse(BuildGdx.input.getX(), BuildGdx.input.getY());
	}
}
