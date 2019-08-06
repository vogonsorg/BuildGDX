// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.desktop.software;

import java.lang.reflect.Method;

public class LwjglMouse extends AWTMouse {

	private Method moveMouse;
	private Method grabMouse;
	private Method ungrabMouse;

	public LwjglMouse(JDisplay display) throws Exception {
		super(display);
		this.robot = null;

		moveMouse = display.getImpl().getClass().getDeclaredMethod("setCursorPosition", int.class, int.class);
		moveMouse.setAccessible(true);
		
		grabMouse = display.getImpl().getClass().getDeclaredMethod("setupCursorClipping", long.class);
		grabMouse.setAccessible(true);
		
		ungrabMouse = display.getImpl().getClass().getDeclaredMethod("resetCursorClipping");
		ungrabMouse.setAccessible(true);
	}

	@Override
	public void showCursor (boolean visible) {
		super.showCursor(visible);
		try {
			if (!visible && catched && display.isActive()) {
				grabMouse.invoke(display.getImpl(), display.getHwnd());
			} else {
				ungrabMouse.invoke(display.getImpl());
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void setCursorPosition(int x, int y)
	{
		try {
			this.x = x;
			this.y = y;

			moveMouse.invoke(display.getImpl(), x, y - 1);
		} catch (Exception e) { e.printStackTrace(); }
	}
}
