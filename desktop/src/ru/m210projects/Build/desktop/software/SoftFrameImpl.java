package ru.m210projects.Build.desktop.software;

import ru.m210projects.Build.Architecture.SoftFrame;
import ru.m210projects.Build.desktop.BuildApplicationConfiguration;
import ru.m210projects.Build.Architecture.BuildApplication.Frame;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;

import com.badlogic.gdx.Gdx;

public class SoftFrameImpl implements SoftFrame, Frame {

	protected final SoftInput input;
	protected final SoftGraphics graphics;

	public SoftFrameImpl(BuildApplicationConfiguration config)
	{
		graphics = new SoftGraphics(config);
		input = new SoftInput();
		
		Gdx.gl = BuildGdx.gl = null;
		Gdx.gl20 = BuildGdx.gl20 = null;
		Gdx.gl30 = BuildGdx.gl30 = null;
	}
	
	@Override
	public BuildInput getInput() {
		return input;
	}

	@Override
	public BuildGraphics getGraphics() {
		return graphics;
	}

	@Override
	public FrameType getType() {
		return FrameType.Canvas;
	}

	@Override
	public void setMaxFramerate(int fps) {
		graphics.config.foregroundFPS = fps;
		graphics.config.backgroundFPS = fps;
	}

	@Override
	public void init() {
		graphics.setupDisplay();
		input.init(graphics.display);
		graphics.resize = true;
		graphics.lastTime = System.nanoTime();
	}
	
	@Override
	public boolean update() {
		graphics.config.x = graphics.display.getX();
		graphics.config.y = graphics.display.getY();
		if (graphics.resize || graphics.display.wasResized()
			|| graphics.getWidth() != graphics.config.width
			|| graphics.getHeight() != graphics.config.height) {
			graphics.resize = false;
			graphics.config.width = graphics.getWidth();
			graphics.config.height = graphics.getHeight();
			graphics.display.updateSize(graphics.config.width, graphics.config.height);
			input.reset();
			graphics.requestRendering();
			return true;
		}
		return false;
	}

	@Override
	public boolean checkRender(boolean shouldRender) {
		boolean isActive = isActive();
		input.update();
		shouldRender |= graphics.shouldRender();
		input.processEvents();
		
		if (!isActive && graphics.config.backgroundFPS == -1) shouldRender = false;
		int frameRate = isActive ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
		
		if(graphics.vsync) 
			frameRate = graphics.display.getDesktopDisplayMode().getRefreshRate();
		
		if (shouldRender) {
			graphics.updateTime();
			graphics.frameId++;
		} else {
			// Sleeps to avoid wasting CPU in an empty loop.
			if (frameRate == -1) frameRate = 10;
			if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
			if (frameRate == 0) frameRate = 30;
		}
		if (frameRate > 0) graphics.sync(frameRate);
		
		return shouldRender;
	}

	@Override
	public void setVSync(boolean vsync) {
		graphics.setVSync(vsync);
	}

	@Override
	public boolean isCloseRequested() {
		return graphics.display.isCloseRequested();
	}

	@Override
	public boolean isActive() {
		return graphics.display.isActive();
	}

	@Override
	public void destroy() {
		if(graphics.display != null)
			graphics.display.dispose();
	}

	@Override
	public byte[] getFrame() {
		return graphics.getCanvas().getFrame();
	}

	@Override
	public void changepalette(byte[] palette) {
		graphics.getCanvas().changepalette(palette);
	}

	@Override
	public void repaint() {
		graphics.getCanvas().repaint();
	}
	
	@Override
	public int getX() {
		return graphics.config.x;
	}

	@Override
	public int getY() {
		return graphics.config.y;
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
