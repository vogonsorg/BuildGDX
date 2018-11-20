package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.Application;

import ru.m210projects.Build.Architecture.BuildFrame.FrameType;

public interface BuildApplication extends Application {

	public BuildFrame getFrame();
	
	public void setFrame(FrameType type);
	
	public FrameType getFrameType();

	public BuildInput getInput();
	
	public BuildGraphics getGraphics();
	
	public void setMaxFramerate(int fps);
	
	public interface Frame extends BuildFrame
	{
		public void init();

		public void setVSync(boolean vsync);
		
		public boolean update();
		
		public boolean render(boolean shouldRender);

		public boolean isCloseRequested();
		
		public boolean isActive();

		public void destroy();
	}
}
