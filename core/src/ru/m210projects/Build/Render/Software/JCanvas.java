package ru.m210projects.Build.Render.Software;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.swing.JPanel;

public class JCanvas extends JPanel {
	private static final long serialVersionUID = 2237851324087823108L;
	
	private final BufferedImage display;
	private final int[] raster;

	public JCanvas(int width, int height)
	{
		display = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		raster = ((DataBufferInt)display.getRaster().getDataBuffer()).getData();
	}
	
	@Override
    public void paintComponent(Graphics g) 
    {
		g.drawImage(display, 0, 0, null);
    }
	
	public int[] getFrameBuffer()
	{
		return raster;
	}
	
	public void DrawPixel(int x, int y, int color)
	{
		int index = x + y * display.getWidth();
		raster[index] = color;
	}
	
	public void clearview(int col)
	{
		Arrays.fill(raster, col);
	}
	
	public void update()
	{
		this.repaint();
	}
}
