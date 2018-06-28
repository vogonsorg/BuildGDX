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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ru.m210projects.Build.Types.Message;

public class DesktopMessage implements Message {
	JOptionPane frame;

	public DesktopMessage()
	{
		try {
			frame = new JOptionPane();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
	}
	
	@Override
	public boolean show(String header, String message, boolean send) {
		if(send) {
			int dialogResult = JOptionPane.showOptionDialog (frame, message + "\n\rDo you want to send a log file to developers?", header,JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
			if(dialogResult == JOptionPane.YES_OPTION)
				return true;
			return false;
		} 
		else
		{
			if(frame != null) {
				frame.setMessage(message);
				frame.setMessageType(JOptionPane.ERROR_MESSAGE);
				JDialog dlog = frame.createDialog(null, header);
				frame.setBackground(dlog.getBackground());
				dlog.setAlwaysOnTop(true);
				dlog.setVisible(true);
			}
			return false;
		}
	}

	@Override
	public void dispose() {
		frame = null;
	}
}
