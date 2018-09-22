package ru.m210projects.Build.desktop.extension;

import java.awt.Toolkit;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import ru.m210projects.Build.Types.BGraphics;
import ru.m210projects.Build.desktop.extension.BDisplay.DisplayType;


public class DeskGraphics2 implements BGraphics {

	long frameId = -1;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	boolean vsync = false;
	
	DeskApplicationConfiguration config;
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	
	BDisplay display;
	
	DeskGraphics2 (DeskApplicationConfiguration config) {
		this.config = config;
	}

	public int getHeight () {
		return display.getHeight();
	}

	public int getWidth () {
		return display.getWidth();
	}

	@Override
	public int getBackBufferWidth () {
		return getWidth();
	}

	@Override
	public int getBackBufferHeight () {
		return getHeight();
	}


	public long getFrameId () {
		return frameId;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public float getRawDeltaTime () {
		return deltaTime;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public GLVersion getGLVersion () {
		if(display.getType() == DisplayType.LWJGL)
			return GLDisplay.glVersion;
		
		return null;
	}

	public boolean shouldRender () {
		synchronized (this) {
			boolean rq = requestRendering;
			requestRendering = false;
			return rq || isContinuous || display.isDirty();
		}
	}

	public int getFramesPerSecond () {
		return fps;
	}

	void updateTime () {
		long time = System.nanoTime();
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	@Override
	public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getDensity () {
		if (config.overrideDensity != -1) return config.overrideDensity / 160f;
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 160f);
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return true;
	}

	@Override
	public Monitor getPrimaryMonitor () {
		return new LwjglMonitor(0, 0, "Primary Monitor");
	}

	@Override
	public Monitor getMonitor () {
		return getPrimaryMonitor();
	}

	@Override
	public Monitor[] getMonitors () {
		return new Monitor[] { getPrimaryMonitor() };
	}

	@Override
	public DisplayMode[] getDisplayModes (Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode (Monitor monitor) {
		return getDisplayMode();
	}

	@Override
	public boolean setFullscreenMode (DisplayMode displayMode) {
		return display.setFullscreenMode(displayMode);
	}
	
	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setWindowedMode (int width, int height) {
		return display.setWindowedMode(width, height);
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return display.getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode () {
		return display.getDisplayMode();
	}

	@Override
	public void setTitle (String title) {
		display.setTitle(title);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take
	 * effect.
	 */
	@Override
	public void setUndecorated (boolean undecorated) {
		display.setUndecorated(undecorated);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take
	 * effect.
	 */
	@Override
	public void setResizable (boolean resizable) {
		this.config.resizable = resizable;
		display.setResizable(resizable);
	}

	@Override
	public BufferFormat getBufferFormat () {
		return display.getBufferFormat();
	}

	@Override
	public void setVSync (boolean vsync) {
		this.vsync = vsync;
		display.setVSync(vsync);
	}

	@Override
	public boolean supportsExtension (String extension) {
		return display.supportsExtension(extension);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		this.isContinuous = isContinuous;
	}

	@Override
	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		synchronized (this) {
			requestRendering = true;
		}
	}

	@Override
	public boolean isFullscreen () {
		return display.isFullscreen();
	}

	/** A callback used by LwjglApplication when trying to create the display */
	public interface SetDisplayModeCallback {
		/** If the display creation fails, this method will be called. Suggested usage is to modify the passed configuration to use a
		 * common width and height, and set fullscreen to false.
		 * @return the configuration to be used for a second attempt at creating a display. A null value results in NOT attempting
		 *         to create the display a second time */
		public DeskApplicationConfiguration onFailure (LwjglApplicationConfiguration initialConfig);
	}

	@Override
	public com.badlogic.gdx.graphics.Cursor newCursor (Pixmap pixmap, int xHotspot, int yHotspot) {
		return new LwjglCursor(pixmap, xHotspot, yHotspot);
	}

	@Override
	public void setCursor (com.badlogic.gdx.graphics.Cursor cursor) {
//		if (canvas != null && SharedLibraryLoader.isMac) {
//			return;
//		}
//		try {
//			Mouse.setNativeCursor(((LwjglCursor)cursor).lwjglCursor);
//		} catch (LWJGLException e) {
//			throw new GdxRuntimeException("Could not set cursor image.", e);
//		}
	}

	@Override
	public void setSystemCursor (SystemCursor systemCursor) {
		if (SharedLibraryLoader.isMac)
			return;
		
		try {
			Mouse.setNativeCursor(null);
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't set system cursor");
		}
	}

	private class LwjglMonitor extends Monitor {
		protected LwjglMonitor (int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}

	@Override
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast) {
		if(display.getType() == DisplayType.LWJGL)
			return ((GLDisplay)display).setDisplayConfiguration(gamma, brightness, contrast);
		return false;
	}

	@Override
	public void setMaxFramerate(int fps) {
		config.foregroundFPS = fps;
		config.backgroundFPS = fps;
	}

	@Override
	public void setDefaultDisplayConfiguration() {
		setDisplayConfiguration(1.0f, 0.0f, 1.0f);
	}

	@Override
	public boolean isGL30Available() {
		if(display.getType() == DisplayType.LWJGL)
			return ((GLDisplay)display).isGL30Available();
		return false;
	}

	@Override
	public GL20 getGL20() {
		if(display.getType() == DisplayType.LWJGL)
			return ((GLDisplay)display).getGL20();
		return null;
	}

	@Override
	public GL30 getGL30() {
		if(display.getType() == DisplayType.LWJGL)
			return ((GLDisplay)display).getGL30();
		return null;
	}

	@Override
	public void setGL20(GL20 gl20) {
		if(display.getType() == DisplayType.LWJGL)
			((GLDisplay)display).setGL20(gl20);
	}

	@Override
	public void setGL30(GL30 gl30) {
		if(display.getType() == DisplayType.LWJGL)
			((GLDisplay)display).setGL20(gl30);
	}
}
