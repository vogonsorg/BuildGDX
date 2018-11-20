package ru.m210projects.Build.desktop.software;

import java.awt.Toolkit;

import javax.swing.JFrame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;

import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Render.Types.GL10;

public class SoftGraphics implements BuildGraphics {

	JDisplay display;
	LwjglApplicationConfiguration config;
	
	long frameId = -1;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	
	boolean vsync = false;
	boolean resize = false;
	
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	
	SoftGraphics (LwjglApplicationConfiguration config) {
		this.config = config;
	}
	
	protected JFrame setupDisplay () {
		display = new JDisplay(config.width, config.height);
		
		display.setTitle(config.title);
		display.setResizable(config.resizable);
		display.setLocation(config.x, config.y);
		
		return display.m_frame;
	}
	
	protected JCanvas getCanvas()
	{
		return display.getCanvas();
	}
	
	public void sync(int fps)
	{
		
	}

	@Override
	public int getWidth() {
		return display.getCanvas().getWidth();
	}

	@Override
	public int getHeight() {
		return display.getCanvas().getHeight();
	}
	
	@Override
	public int getBackBufferWidth() {
		return getWidth();
	}

	@Override
	public int getBackBufferHeight() {
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

	public int getFramesPerSecond () {
		return fps;
	}

	protected void updateTime () {
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
	public void setTitle (String title) {
		display.setTitle(title);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take
	 * effect.
	 */
	@Override
	public void setUndecorated (boolean undecorated) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", undecorated ? "true" : "false");
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

	public boolean shouldRender () {
		synchronized (this) {
			boolean rq = requestRendering;
			requestRendering = false;
			return rq || isContinuous; // || Display.isDirty();
		}
	}
	
	@Override
	public boolean isFullscreen() {
		return false; //display.isFullscreen(); XXX
	}

	@Override
	public boolean isGL30Available() {
		return false;
	}

	@Override
	public GL20 getGL20() {
		return null;
	}

	@Override
	public GL30 getGL30() {
		return null;
	}

	@Override
	public void setGL20(GL20 gl20) {
	}

	@Override
	public void setGL30(GL30 gl30) {
	}

	@Override
	public GLVersion getGLVersion() {
		return null;
	}

	@Override
	public Monitor getPrimaryMonitor() {
		return null;
	}

	@Override
	public Monitor getMonitor() {
		return null;
	}

	@Override
	public Monitor[] getMonitors() {
		return null;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return null;
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return null;
	}

	@Override
	public DisplayMode getDisplayMode() {
		return null;
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return null;
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		return false;
	}

	@Override
	public void setVSync(boolean vsync) {
	}

	@Override
	public BufferFormat getBufferFormat() {
		return null;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return false;
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		return null;
	}

	@Override
	public void setCursor(Cursor cursor) {
	}

	@Override
	public void setSystemCursor(SystemCursor systemCursor) {
	}

	@Override
	public GL10 getGL10() {
		return null;
	}

}
