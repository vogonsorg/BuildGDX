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

package ru.m210projects.Build.Types;

public class Palette {
	public int r;
	public int g;
	public int b;
	public int f;
	
	public Palette() { }
	
	public Palette(int r, int g, int b, int f)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.f = f;
		
	}
	public int[] array;
	
	public int[] toArray() {
		if(array == null)
			array = new int[4];
		array[0] = r;
		array[1] = g;
		array[2] = b;
		array[3] = f;
		
		return array;
	}
}
