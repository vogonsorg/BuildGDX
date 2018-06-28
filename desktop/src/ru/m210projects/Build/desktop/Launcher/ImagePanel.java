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

package ru.m210projects.Build.desktop.Launcher;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private Thread thread;

    public ImagePanel(final URL resource) {
    	thread = new Thread(new Runnable() 
		{
			public void run()
			{
				try {         
			           image = ImageIO.read(resource);
			        } catch (Exception ex) {}
			}
		});
    	thread.start();
	}

	@Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null); 
    }
	
	public boolean isLoaded()
	{
		return !thread.isAlive();
	}
}
