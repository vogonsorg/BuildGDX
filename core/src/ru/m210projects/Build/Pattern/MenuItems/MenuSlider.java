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

import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Strhandler.Bitoa;
import static ru.m210projects.Build.Strhandler.buildString;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils.ConvertType;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.Align;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;

public abstract class MenuSlider extends MenuItem {

	public int min;
	public int max;
	public int step;
	public int value;
	public boolean digital;
	public float digitalMax;
	public char[] dbuff; 
	public MenuProc callback;
	public int nSliderWidth;
	public int nSliderRange;
	
	private int touchX;
	private boolean isTouched;
	private MenuSlider touchedObj;
	
	public MenuSlider(Object text, BuildFont textStyle, int x, int y, int width, int value, int min, int max, 
			int step, MenuProc callback, boolean digital, int nSliderWidth, int nSliderRange) {
		super(text, textStyle);

		this.flags = 3 | 4;
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = BClipRange(value, min, max);
		
		this.nSliderWidth = nSliderWidth;
		this.nSliderRange = nSliderRange;
		
		this.digital = digital;
		this.digitalMax = 0;
		
		dbuff = new char[10];
	}
	
	public abstract void drawBackground(int x, int y, int shade, int pal);
	
	public abstract void drawSlider(int x, int y, int shade, int pal);

	@Override
	public void draw(MenuHandler handler) {
		int shade = handler.getShade(this);
		int pal = handler.getPal(font, this);

		if ( text != null )
			font.drawText(x, y, text, shade, pal, TextAlign.Left, 0, false);

		drawBackground(x + width - nSliderRange, y, 0, pal);

		if(digital)
		{
			Arrays.fill(dbuff, (char)0);
			if(digitalMax == 0)
				Bitoa(value, dbuff);
			else {
				String val = Float.toString(value / digitalMax);
				int index = val.indexOf('.');
				buildString(dbuff, 0, val);
				Arrays.fill(dbuff, index + 4, dbuff.length, (char)0);
			}

			font.drawText(x + width - nSliderRange - font.getAlign(dbuff).x - 10, y, dbuff, shade, pal, TextAlign.Left, 0, false);
		}
		
		int xRange = nSliderRange - nSliderWidth;
		int nRange = max - min;
		int dx = xRange * (value - min) / nRange - nSliderRange;
	
		drawSlider((x + width + dx), y, shade, pal);
	}
	
	public void dbDrawBackground(Engine draw, int x, int y, int col)
	{
		int x1 = coordsConvertXScaled(x, ConvertType.Normal);
		int y1 = coordsConvertYScaled(y);
		int x2 = coordsConvertXScaled(x + nSliderRange, ConvertType.Normal);
		int y2 = coordsConvertYScaled(y + font.nHeigth);
		
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x2 * 4096, y1 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y2 * 4096, x2 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x1 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x2 * 4096, y1 * 4096, x2 * 4096, y2 * 4096, col);
	}
	
	public void dbDrawSlider(Engine draw, int x, int y, int col)
	{
		int x1 = coordsConvertXScaled(x, ConvertType.Normal);
		int y1 = coordsConvertYScaled(y);
		int x2 = coordsConvertXScaled(x + nSliderWidth, ConvertType.Normal);
		int y2 = coordsConvertYScaled(y + font.nHeigth);
		
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x2 * 4096, y1 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y2 * 4096, x2 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x1 * 4096, y1 * 4096, x1 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x2 * 4096, y1 * 4096, x2 * 4096, y2 * 4096, col);
	}
	
	public void dbDrawDimensions(Engine draw, int col)
	{
		int x = coordsConvertXScaled(this.x - 1, ConvertType.Normal);
		int y = coordsConvertYScaled(this.y - 1);
		int x2 = coordsConvertXScaled(this.x + width + 1, ConvertType.Normal);
		int y2 = coordsConvertYScaled(this.y + font.nHeigth + 1);
		
		draw.getrender().drawline256(x * 4096, y * 4096, x2 * 4096, y * 4096, col);
		draw.getrender().drawline256(x * 4096, y2 * 4096, x2 * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x * 4096, y * 4096, x * 4096, y2 * 4096, col);
		draw.getrender().drawline256(x2 * 4096, y * 4096, x2 * 4096, y2 * 4096, col);
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		int val;

		switch(opt) {
		case UP:
			m_pMenu.mNavUp();
			break;
		case DW:
			m_pMenu.mNavDown();
			break;
		case LEFT:
		case MWDW:
			if(value <= 0) {
				int dv = (value - step) % -step;
		        val = value - step - dv;
		        if ( dv < 0 )
		        	val += step;
			}
			else 
			{
		        int dv = (value - 1) % step;
		        val = value - 1 - dv;
		        if ( dv < 0 )
		        	val -= step;
			}
			value = BClipRange(val, min, max);
			if(callback != null) {
				callback.run(handler, this);
			}
			break;
		case RIGHT:
		case MWUP:
			if ( value < 0 )
		    {
		        int dv = (value - 1) % -step;
		        val = value - 1 - dv;
		        if ( dv < 0 )
		        	val += step;
		    } else
		    {
		        int dv = (value + step) % step;
		        val = value + step - dv;
		        if ( dv < 0 )
		        	val -= step;
		    }
			value = BClipRange(val, min, max);
			if(callback != null) 
				callback.run(handler, this);
			break;
		case ENTER:
			if(callback != null) 
				callback.run(handler, this);
			break;
		case LMB:
			if(touchedObj == this)
			{
				int startx = x + width - nSliderRange + nSliderWidth / 2;
				float dr = (float)(touchX - startx) / (nSliderRange - nSliderWidth - 1);
				value = BClipRange((int) (dr * (max - min) + min), min, max);
				if(callback != null) 
					callback.run(handler, this);
			} 
		default:
			return m_pMenu.mNavigation(opt);
		}
		
		return false;
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		touchX = mx;
		isTouched = false;
		if(!Gdx.input.isTouched() && touchedObj != this)
			touchedObj = null;
		
		if(text != null)
		{
			Align align = font.getAlign(text);
			if(mx > x && mx < x + align.x)
			{
				if(my > y && my < y + align.y) 
					return true;
			}
		}

		int cx = x + width - nSliderRange;
		if(mx > cx && mx < cx + nSliderRange)
			if(my > y && my < y + font.nHeigth) {
				isTouched = true;
				if(Gdx.input.isTouched())
					touchedObj = this;
			}
		
		return isTouched;
	}

	@Override
	public void open() {
	}

	@Override
	public void close() {
	}

}