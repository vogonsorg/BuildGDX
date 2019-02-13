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

package ru.m210projects.Build.Pattern.MenuItems;

import static ru.m210projects.Build.Gameutils.coordsConvertXScaled;
import static ru.m210projects.Build.Gameutils.coordsConvertYScaled;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils.ConvertType;

public abstract class SliderDrawable {
	public int width = -1;
	public int range = -1;
	
	public SliderDrawable(int width, int range) {
		this.width = width;
		this.range = range;
	}
	
	public abstract void drawBackground(int x, int y, int shade, int pal);
	
	public abstract void drawSlider(int x, int y, int shade, int pal);

	public void dbDrawBackground(Engine draw, int x, int y, int height, int col)
	{
		int x1 = coordsConvertXScaled(x, ConvertType.Normal);
		int y1 = coordsConvertYScaled(y);
		int x2 = coordsConvertXScaled(x + range, ConvertType.Normal);
		int y2 = coordsConvertYScaled(y + height);
		
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x2 * 4096, y1 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y2 * 4096, x2 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x1 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x2 * 4096, y1 * 4096, x2 * 4096, y2 * 4096, col);
	}
	
	public void dbDrawSlider(Engine draw, int x, int y, int height, int col)
	{
		int x1 = coordsConvertXScaled(x, ConvertType.Normal);
		int y1 = coordsConvertYScaled(y);
		int x2 = coordsConvertXScaled(x + width, ConvertType.Normal);
		int y2 = coordsConvertYScaled(y + height);
		
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x2 * 4096, y1 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y2 * 4096, x2 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x1 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x2 * 4096, y1 * 4096, x2 * 4096, y2 * 4096, col);
	}
}
