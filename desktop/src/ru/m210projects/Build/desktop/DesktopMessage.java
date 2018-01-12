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
			int dialogResult = JOptionPane.showConfirmDialog (null, message + "\n\rDo you want to send a log file to developers?", header,JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
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
