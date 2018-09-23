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

package ru.m210projects.Build.desktop.extension.software;

import java.awt.Dimension;
import java.nio.ByteBuffer;

import javax.swing.JFrame;

public class JDisplay
{
	private final JFrame m_frame;
	private final JCanvas canvas;
	private Dimension size;
	
	public JDisplay(int width, int height)
	{
		size = new Dimension(width, height);
		canvas = new JCanvas(width, height);
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);

		m_frame = new JFrame();
		m_frame.add(canvas);
		m_frame.pack();
		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_frame.setLocationRelativeTo(null);
		m_frame.setVisible(true);
		
		canvas.setFocusable(true);
		canvas.requestFocus();
	}
	
	public void setSize(int width, int height)
	{
		//size = new Dimension(width, height); XXX
		//canvas = new JCanvas(width, height);
	}
	
	public JCanvas getCanvas()
	{
		return canvas;
	}

	public void setTitle(String title)
	{
		m_frame.setTitle(title);
	}
	
	public int getX()
	{
		return m_frame.getX();
	}
	
	public int getY()
	{
		return m_frame.getY();
	}
	
	public void setUndecorated(boolean undecorated)
	{
		m_frame.setUndecorated(undecorated);
	}
	
	public void setResizable(boolean resizable) {
		m_frame.setResizable(false);
	}

	public void setLocation(int x, int y) {
		m_frame.setLocation(x, y);
	}

	public void setIcon(ByteBuffer[] icons) {
		
	}
}
