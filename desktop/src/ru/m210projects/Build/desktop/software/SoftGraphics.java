//This file is part of BuildGDX.
//Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.desktop.software;

import java.awt.Image;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;

import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.desktop.BuildApplicationConfiguration;

public class SoftGraphics implements BuildGraphics {

	JDisplay display;
	BuildApplicationConfiguration config;
	
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
	
	SoftGraphics (BuildApplicationConfiguration config) {
		this.config = config;
	}
	
	protected JFrame setupDisplay () {
		display = new JDisplay(config.width, config.height, config.borderless);

		try {
			Array<String> iconPaths = getIconPaths(config);
			if (iconPaths.size > 0) {
				List<Image> icons = new ArrayList<Image>();
				for (int i = 0, n = iconPaths.size; i < n; i++) {
					FileHandle file = Gdx.files.getFileHandle(iconPaths.get(i), getIconFileTypes(config).get(i));
					ImageIcon icon = new ImageIcon(file.readBytes());
					icons.add(icon.getImage());
				}
				display.setIcon(icons);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		display.setTitle(config.title);
		display.setResizable(config.resizable);
		display.setLocation(config.x, config.y);

		return display.m_frame;
	}
	
	protected void show()
	{
		display.m_frame.setVisible(true);
		display.getCanvas().setFocusable(true);
		display.getCanvas().requestFocus();
	}
	
	protected JCanvas getCanvas()
	{
		return display.getCanvas();
	}
	
	protected void sync(int fps)
	{
		Sync.sync(fps);
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
		return display.isFullscreen();
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		java.awt.DisplayMode[] availableDisplayModes = display.getDisplayModes();
		DisplayMode[] modes = new DisplayMode[availableDisplayModes.length];

		int idx = 0;
		for (java.awt.DisplayMode mode : availableDisplayModes) {
			modes[idx++] = new SoftDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth(), mode);
		}

		return modes;
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode() {
		java.awt.DisplayMode mode = display.getDesktopDisplayMode();
		return new SoftDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth(), mode);
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return getDisplayMode();
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		java.awt.DisplayMode mode = ((SoftDisplayMode)displayMode).mode;
		
		if(display.setFullscreenMode(mode))
		{
			config.width = mode.getWidth();
			config.height = mode.getHeight();
			resize = true;
			
			return true;
		}

		return false;
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		
		if (getWidth() == width && getHeight() == height && !display.isFullscreen()) 
			return true;

		java.awt.DisplayMode targetDisplayMode = new java.awt.DisplayMode(width, height, 
				display.getDesktopDisplayMode().getRefreshRate(), display.getDesktopDisplayMode().getBitDepth());

		display.setWindowedMode(targetDisplayMode);
		display.setResizable(config.resizable);

		config.width = targetDisplayMode.getWidth();
		config.height = targetDisplayMode.getHeight();
		resize = true;
		return true;
	}

	@Override
	public void setVSync(boolean vsync) {
		this.vsync = vsync;
	}
	
	@Override
	public void setFramesPerSecond(int fps) {
		config.foregroundFPS = fps;
		config.backgroundFPS = fps;
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

	private class SoftDisplayMode extends DisplayMode {
		java.awt.DisplayMode mode;
		
		public SoftDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, java.awt.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Array<String> getIconPaths(LwjglApplicationConfiguration config) throws Exception
	{
		Field f = config.getClass().getDeclaredField("iconPaths"); 
		f.setAccessible(true);
		Array<String> icons = (Array<String>) f.get(config);
		return icons;
	}
	
	@SuppressWarnings("unchecked")
	public Array<FileType> getIconFileTypes(LwjglApplicationConfiguration config) throws Exception
	{
		Field f = config.getClass().getDeclaredField("iconFileTypes"); 
		f.setAccessible(true);
		Array<FileType> iconFileTypes = (Array<FileType>) f.get(config);
		return iconFileTypes;
	}
	
	// unsupported
	
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
	public boolean supportsExtension(String extension) {
		return false;
	}

	@Override
	public GL10 getGL10() {
		return null;
	}
	
	@Override
	public BufferFormat getBufferFormat() {
		return null;
	}

}
