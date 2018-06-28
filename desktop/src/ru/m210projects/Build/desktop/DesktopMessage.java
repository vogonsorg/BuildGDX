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

import ru.m210projects.Build.Types.Message;

public class DesktopMessage implements Message {
	JOptionPane frame;
	URL icon;

	public DesktopMessage(URL icon)
	{
		try {
			this.icon = icon;
			frame = new JOptionPane();
			frame.setMessageType(JOptionPane.ERROR_MESSAGE);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
	}
	
	@Override
	public boolean show(String header, String message, boolean send) {
		if(send) {
			if(frame != null) {
				frame.setMessage(message);
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
		} 
		else
		{
			if(frame != null) {
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
	}

	@Override
	public void dispose() {
		frame = null;
	}
}
