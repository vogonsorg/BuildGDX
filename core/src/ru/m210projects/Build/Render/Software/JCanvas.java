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

package ru.m210projects.Build.Render.Software;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import static ru.m210projects.Build.Engine.*;

public class JCanvas extends Canvas {
	private static final long serialVersionUID = 2237851324087823108L;
	
	private BufferedImage display;
	private byte[] raster;
	private IndexColorModel paletteModel;

	public JCanvas(int width, int height)
	{
		paletteModel = new IndexColorModel(1, 256, curpalette, 0, false);
		display = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, paletteModel);
		raster = ((DataBufferByte)display.getRaster().getDataBuffer()).getData();
	}

	@Override
	public void update( Graphics g ) { paint(g); }
	
	@Override
	public void paint( Graphics g ) { g.drawImage(display, 0, 0, null); }

	public byte[] getFrame() { return raster; }
	
	public void update() { repaint(); }
	
	public void changepalette(byte[] palette) {
		paletteModel = new IndexColorModel(1, 256, palette, 0, false);
		display = new BufferedImage(paletteModel, display.getRaster(), false, null);
	}
	
	public int getHeight()
	{
		return display.getHeight();
	}
	
	public int getWidth()
	{
		return display.getWidth();
	}
}
