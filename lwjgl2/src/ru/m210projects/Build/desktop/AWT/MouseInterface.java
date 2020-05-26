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


package ru.m210projects.Build.desktop.AWT;

import com.badlogic.gdx.InputProcessor;

public interface MouseInterface {

	public void reset();

	public int getX();

	public int getY();
	
	public int getDeltaX();

	public int getDeltaY();

	public boolean isTouched();
	
	public boolean justTouched();
	
	public boolean isButtonPressed(int button);

	public boolean isButtonJustPressed(int button);
	
	public int getDWheel();

	public long processEvents(InputProcessor processor);

	public void setCursorCatched(boolean catched);

	public boolean isCursorCatched();

	public void setCursorPosition(int x, int y);

	public void showCursor(boolean b);

	public boolean isInsideWindow();
	
	public void setWindowHandle();

}