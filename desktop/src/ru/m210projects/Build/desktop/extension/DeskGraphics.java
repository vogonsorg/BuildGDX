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

import ru.m210projects.Build.Types.BDisplay;
import ru.m210projects.Build.Types.BGraphics;
import ru.m210projects.Build.Types.BDisplay.DisplayType;
import ru.m210projects.Build.desktop.extension.software.SoftDisplay;


public class DeskGraphics implements BGraphics {

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
	
	private BDisplay currdisplay;
	private GLDisplay gldisplay;
	private SoftDisplay sdisplay;
	
	DeskGraphics (DeskApplicationConfiguration config) {
		this.config = config;
	}

	public BDisplay getDisplay() {
		return currdisplay;
	}

	@Override
	public int getHeight () {
		return currdisplay.getHeight();
	}

	@Override
	public int getWidth () {
		return currdisplay.getWidth();
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
		if(currdisplay.getType() == DisplayType.GL)
			return GLDisplay.glVersion;
		
		return null;
	}

	public boolean shouldRender () {
		synchronized (this) {
			boolean rq = requestRendering;
			requestRendering = false;
			return rq || isContinuous || currdisplay.isDirty();
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
		return currdisplay.setFullscreenMode(displayMode);
	}
	
	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setWindowedMode (int width, int height) {
		return currdisplay.setWindowedMode(width, height);
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return currdisplay.getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode () {
		return currdisplay.getDisplayMode();
	}

	@Override
	public void setTitle (String title) {
		currdisplay.setTitle(title);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take
	 * effect.
	 */
	@Override
	public void setUndecorated (boolean undecorated) {
		currdisplay.setUndecorated(undecorated);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take
	 * effect.
	 */
	@Override
	public void setResizable (boolean resizable) {
		this.config.resizable = resizable;
		currdisplay.setResizable(resizable);
	}

	@Override
	public BufferFormat getBufferFormat () {
		return currdisplay.getBufferFormat();
	}

	@Override
	public void setVSync (boolean vsync) {
		this.vsync = vsync;
		currdisplay.setVSync(vsync);
	}

	@Override
	public boolean supportsExtension (String extension) {
		return currdisplay.supportsExtension(extension);
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
		return currdisplay.isFullscreen();
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
	public void setCursor (com.badlogic.gdx.graphics.Cursor cursor) {}

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
		if(currdisplay.getType() == DisplayType.GL)
			return gldisplay.setDisplayConfiguration(gamma, brightness, contrast);
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
		if(currdisplay.getType() == DisplayType.GL)
			return gldisplay.isGL30Available();
		return false;
	}

	@Override
	public GL20 getGL20() {
		if(currdisplay.getType() == DisplayType.GL)
			return gldisplay.getGL20();
		return null;
	}

	@Override
	public GL30 getGL30() {
		if(currdisplay.getType() == DisplayType.GL)
			return gldisplay.getGL30();
		return null;
	}

	@Override
	public void setGL20(GL20 gl20) {
		if(currdisplay.getType() == DisplayType.GL)
			gldisplay.setGL20(gl20);
	}

	@Override
	public void setGL30(GL30 gl30) {
		if(currdisplay.getType() == DisplayType.GL)
			gldisplay.setGL20(gl30);
	}

	@Override
	public byte[] getFrame() {
		if(currdisplay.getType() == DisplayType.Software)
			return sdisplay.getCanvas().getFrame();
		
		return null;
	}
	
	@Override
	public void changepalette(byte[] palette) {
		if(currdisplay.getType() == DisplayType.Software)
			sdisplay.getCanvas().changepalette(palette);
	}

	@Override
	public void setDisplayType(DisplayType type) {
		if(type == DisplayType.Software) {
			if(sdisplay == null) {
				sdisplay = new SoftDisplay(config);
			}
			this.currdisplay = sdisplay;
		} 
		else if(type == DisplayType.GL) {
			if(gldisplay == null)
				gldisplay = new GLDisplay(config);
			this.currdisplay = gldisplay;
		}
		this.setVSync(config.vSyncEnabled);
	}

	@Override
	public DisplayType getDisplayType() {
		return currdisplay.getType();
	}
}
