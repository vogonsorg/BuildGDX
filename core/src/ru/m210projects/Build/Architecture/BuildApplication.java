package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.Application;

import ru.m210projects.Build.Architecture.BuildFrame.FrameType;

public interface BuildApplication extends Application {
	
	public enum Platform { Windows, Linux, MacOSX, Android };

	public BuildFrame getFrame();
	
	public void setFrame(FrameType type);
	
	public FrameType getFrameType();

	public BuildInput getInput();
	
	public BuildGraphics getGraphics();
	
	public Platform getPlatform();
	
	public interface Frame extends BuildFrame
	{
		public void init();

		public void setVSync(boolean vsync);
		
		public boolean update();
		
		public boolean checkRender(boolean shouldRender);

		public boolean isCloseRequested();

		public void destroy();
		
		public void repaint();
	}
}
