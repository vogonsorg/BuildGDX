package ru.m210projects.Build.Render.Software;

import java.awt.Dimension;
import javax.swing.JFrame;

public class JDisplay
{
	private final JFrame m_frame;
	private final JCanvas canvas;
	
	public JDisplay(int width, int height, String title)
	{
		Dimension size = new Dimension(width, height);
		canvas = new JCanvas(width, height);
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);

		m_frame = new JFrame();
		m_frame.add(canvas);
		m_frame.pack();
		m_frame.setResizable(false);
		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_frame.setLocationRelativeTo(null);
		m_frame.setTitle(title);
		m_frame.setVisible(true);

		canvas.setFocusable(true);
		canvas.requestFocus();
	}
	
	public JCanvas getCanvas()
	{
		return canvas;
	}

	public void setTitle(String title)
	{
		m_frame.setTitle(title);
	}
}
