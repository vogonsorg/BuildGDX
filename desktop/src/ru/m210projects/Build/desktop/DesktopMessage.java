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

import ru.m210projects.Build.Architecture.BuildMessage;
import ru.m210projects.Build.Types.BConfig;

public class DesktopMessage implements BuildMessage {
	private JOptionPane frame;
	private URL icon;
	private BConfig cfg;

	public DesktopMessage(URL icon, BConfig cfg)
	{
		try {
			this.icon = icon;
			this.cfg = cfg;
			frame = new JOptionPane();
			frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public boolean show(String header, String message, MessageType type) {
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
			if(frame != null) {
				if(type == MessageType.Crash) {
					frame.setMessageType(JOptionPane.ERROR_MESSAGE);
					frame.setMessage(message + "\r\n \r\n      Do you want to send a crash report?");
				} else {
					frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
					frame.setMessage(message);
				}
				frame.setOptionType(JOptionPane.YES_NO_OPTION);
				JDialog dialog = frame.createDialog(header);
				dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(icon));
				frame.setBackground(dialog.getBackground());
				
				dialog.setAlwaysOnTop(true);
		        dialog.setVisible(true);
		        dialog.dispose();
		        
		        Object selectedValue = frame.getValue();
		        if (selectedValue instanceof Integer) {
		        	if(((Integer)selectedValue).intValue() == JOptionPane.YES_OPTION)
						return true;
	            }
			}

			return false;
		case Info:
			if(frame != null) {
				frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
				frame.setMessage(message);
				frame.setOptionType(JOptionPane.DEFAULT_OPTION);
				final JDialog dlog = frame.createDialog(header);
				dlog.setIconImage(Toolkit.getDefaultToolkit().getImage(icon));
				frame.setBackground(dlog.getBackground());
				
				dlog.setAlwaysOnTop(true);
				dlog.setVisible(true);
				dlog.dispose();
			}
			return false;
		}
		
		return false;
	}

	@Override
	public void dispose() {
		frame = null;
	}
}
