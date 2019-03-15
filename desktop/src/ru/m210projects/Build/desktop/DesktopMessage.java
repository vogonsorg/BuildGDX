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

package ru.m210projects.Build.desktop;

import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildMessage;
import ru.m210projects.Build.Pattern.BuildConfig;

public class DesktopMessage implements BuildMessage {
	private JOptionPane frame;
	private URL icon;
	private BuildConfig cfg;

	public DesktopMessage(URL icon, BuildConfig cfg)
	{
		this.icon = icon;
		this.cfg = cfg;
	}
	
	@Override
	public boolean show(String header, String message, MessageType type) {
		if(frame == null && (frame = InitFrame()) == null)
			return false;
		
		if(message.length() >= 384)
		{
			message = message.substring(0, 384);
			message += "...";
		}

		if(Gdx.graphics != null) {
			Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			cfg.fullscreen = 0;
		}

		switch(type)
		{
		case Question:
		case Crash:
			if(type == MessageType.Crash) {
				frame.setMessageType(JOptionPane.ERROR_MESSAGE);
				frame.setMessage(message + "\r\n \r\n      Do you want to send a crash report?");
			} else {
				frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
				frame.setMessage(message);
			}
			frame.setOptionType(JOptionPane.YES_NO_OPTION);
			JDialog dialog = frame.createDialog(header);
			if(icon != null)
				dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(icon));
			frame.setBackground(dialog.getBackground());
			dialog.setLocation(BuildGdx.app.getFrame().getX() + (BuildGdx.graphics.getWidth() - dialog.getWidth()) / 2, 
					BuildGdx.app.getFrame().getY() + (BuildGdx.graphics.getHeight() - dialog.getHeight()) / 2);
			dialog.setAlwaysOnTop(true);
	        dialog.setVisible(true);
	        dialog.dispose();
	        
	        Object selectedValue = frame.getValue();
	        if (selectedValue instanceof Integer) {
	        	if(((Integer)selectedValue).intValue() == JOptionPane.YES_OPTION)
					return true;
            }

			return false;
		case Info:
			frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			frame.setMessage(message);
			frame.setOptionType(JOptionPane.DEFAULT_OPTION);
			final JDialog dlog = frame.createDialog(header);
			if(icon != null)
				dlog.setIconImage(Toolkit.getDefaultToolkit().getImage(icon));
			frame.setBackground(dlog.getBackground());
			dlog.setLocation(BuildGdx.app.getFrame().getX() + (BuildGdx.graphics.getWidth() - dlog.getWidth()) / 2, 
					BuildGdx.app.getFrame().getY() + (BuildGdx.graphics.getHeight() - dlog.getHeight()) / 2);
			dlog.setAlwaysOnTop(true);
			dlog.setVisible(true);
			dlog.dispose();
			
			return false;
		}
		
		return false;
	}
	
	protected JOptionPane InitFrame()
	{
		JOptionPane frame = null;
		try {
			frame = new JOptionPane();
			frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { e.printStackTrace(); }
		return frame;
	}

	@Override
	public void dispose() {
		frame = null;
	}
}
