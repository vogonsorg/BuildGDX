// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build;

import java.util.Arrays;

import ru.m210projects.Build.Architecture.BuildGdx;

public class TouchControl {
	public final int ACTION_FREE = -1;
	public final int ACTION_DOWN = 0;
	public final int ACTION_DRAG = 1;
	public final int ACTION_UP = 2;
	
	ActionInput wheel;
	ActionInput button;
	int[] action = new int[10];
	
	public TouchControl()
	{
		wheel = new ActionInput("Wheel", 0, 320, 200);
		button = new ActionInput("Button", 0, 100, 100);
		Arrays.fill(action, -1);
	}

	public void draw(Engine engine, int flags)
	{
//		switch(flags)
//		{
//		case gRenderMenu:
//			break;
//		case gRenderGame:
//			wheel.draw(engine, flags);
//			button.draw(engine, flags);
//			break;
//			
//		default:
//			break;
//		}
	}

	public void getInput()
	{
		for(int i = 0; i < 10; i++) {
			if(BuildGdx.input.isTouched(i))
			{
				if(action[i] == ACTION_FREE)
					action[i] = ACTION_DOWN;
				else action[i] = ACTION_DRAG;
			} else {
				if(action[i] != ACTION_FREE && action[i] != ACTION_UP)
					action[i] = ACTION_UP;
				else if(action[i] == ACTION_UP)
					action[i] = ACTION_FREE;
			}

			wheel.getInput(i, action[i]);
			button.getInput(i, action[i]);
		}
	}
}

class ActionInput
{
	public int x;
	public int y;
	public int width = 64, heigth = 64;
	public int type;
	public String name;
	private int touchX = -1, touchY = -1;
	private int id = -1;
	
	
	public ActionInput(String name, int type, int x, int y)
	{
		this.name = name;
		this.type = type;
		this.x = x;
		this.y = y;
	}
	
	public void draw(Engine engine, int flags)
	{
		engine.rotatesprite(x << 16, y << 16, 65536, 0, 0, 8, 0, 24, 0, 0, Engine.xdim, Engine.ydim);
		if(id != -1) {
			int rx = BuildGdx.input.getX(id) - (x + 32);
			int ry = BuildGdx.input.getY(id) - (y + 32);
			int dist = rx*rx + ry*ry;
			engine.rotatesprite(BuildGdx.input.getX(id) << 16, BuildGdx.input.getY(id) << 16, 8192, 0, 0, 8, 7, 16, 0, 0, Engine.xdim, Engine.ydim);
		}
	}
	
	public void getInput(int i, int action)
	{
		if(BuildGdx.input.isTouched(i))
		{
			if(BuildGdx.input.getX(i) >= x && BuildGdx.input.getX(i) < (x + width) &&
					BuildGdx.input.getY(i) >= y && BuildGdx.input.getY(i) < (y + heigth))
			{
				if(action == 0 && id == -1)
				{
					touchX = BuildGdx.input.getX(i);
					touchY = BuildGdx.input.getY(i);
					id = i;
				}
			}
		} 
		
		if(id == i) {
			if(BuildGdx.input.isTouched(i) && id != -1)
				System.out.println(i + " " + name + " " + (BuildGdx.input.getX(i) - touchX));
			else if( action == 2 ) {
				touchX = -1;
				touchY = -1;
				id = -1;
			}
		}
	}
}
