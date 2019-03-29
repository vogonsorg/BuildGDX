package ru.m210projects.Build.desktop.gl;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ru.m210projects.Build.Architecture.BuildApplication.Frame;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;
import ru.m210projects.Build.Architecture.GLFrame;

public class GLFrameImpl implements GLFrame, Frame {
	
	protected final GLInput input;
	protected final GLGraphics graphics;

	public GLFrameImpl(LwjglApplicationConfiguration config)
	{
		graphics = new GLGraphics(config);
		input = new GLInput();
	}

	@Override
	public void setVSync(boolean vsync) {
		graphics.setVSync(vsync);
	}

	@Override
	public void init() {
		try {
			graphics.setupDisplay();
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}

		graphics.resize = true;
		graphics.lastTime = System.nanoTime();
	}

	@Override
	public boolean update() {
		graphics.config.x = Display.getX();
		graphics.config.y = Display.getY();
		if (graphics.resize || Display.wasResized()
			|| (int)(Display.getWidth() * Display.getPixelScaleFactor()) != graphics.config.width
			|| (int)(Display.getHeight() * Display.getPixelScaleFactor()) != graphics.config.height) {
			graphics.resize = false;
			graphics.config.width = (int)(Display.getWidth() * Display.getPixelScaleFactor());
			graphics.config.height = (int)(Display.getHeight() * Display.getPixelScaleFactor());
			Gdx.gl.glViewport(0, 0, graphics.config.width, graphics.config.height);
			graphics.requestRendering();
			return true;
		}
		return false;
	}

	@Override
	public boolean isCloseRequested() {
		return Display.isCloseRequested();
	}

	@Override
	public boolean isActive() {
		return Display.isActive();
	}

	@Override
	public boolean isReady() {
		return Display.isCreated();
	}
	
	@Override
	public void destroy() {
		// Workaround for bug in LWJGL whereby resizable state is lost on DisplayMode change
		Display.setResizable(false);
		Display.destroy();
		
		Gdx.gl = BuildGdx.gl = null;
		Gdx.gl20 = BuildGdx.gl20 = null;
		Gdx.gl30 = BuildGdx.gl30 = null;
	}

	@Override
	public BuildInput getInput() {
		return input;
	}

	@Override
	public boolean checkRender(boolean shouldRender) {
		boolean isActive = Display.isActive();
		input.update();
		shouldRender |= graphics.shouldRender();
		
		if (!isActive && graphics.config.backgroundFPS == -1) shouldRender = false;
		int frameRate = isActive ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
		if (shouldRender) {
			graphics.updateTime();
			graphics.frameId++;
		} else {
			// Sleeps to avoid wasting CPU in an empty loop.
			if (frameRate == -1) frameRate = 10;
			if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
			if (frameRate == 0) frameRate = 30;
		}
		if (frameRate > 0) Display.sync(frameRate);
		
		return shouldRender;
	}

	@Override
	public BuildGraphics getGraphics() {
		return graphics;
	}

	@Override
	public FrameType getType() {
		return FrameType.GL;
	}

	@Override
	public void setMaxFramerate(int fps) {
		graphics.config.foregroundFPS = fps;
		graphics.config.backgroundFPS = fps;
	}

	@Override
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast) {
		try {
			Display.setDisplayConfiguration(gamma, brightness, contrast);
		} catch (Exception e) { e.printStackTrace(); return false; }
		
		return true;
	}

	@Override
	public void setDefaultDisplayConfiguration() {
		setDisplayConfiguration(1.0f, 0.0f, 1.0f);
	}

	@Override
	public void repaint() {
		Display.update(false);
	}

	@Override
	public int getX() {
		return graphics.config.x;
	}

	@Override
	public int getY() {
		return graphics.config.y;
	}
}
