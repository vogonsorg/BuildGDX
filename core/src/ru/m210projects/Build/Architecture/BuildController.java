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

package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.math.Vector2;

public interface BuildController {

	public void resetButtonStatus();

	public boolean buttonPressed();
	
	public boolean buttonPressed(int buttonCode);
	
	public boolean buttonStatusOnce(int buttonCode);
	
	public boolean buttonStatus(int buttonCode);
	
	public int getButtonCount();
	
	public int getAxisCount();
	
	public int getPovCount();
	
	public Vector2 getStickValue(int aCode1, int aCode2, float deadZone);

	public String getName();

	public void update();
	
}
