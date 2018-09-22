package ru.m210projects.Build.desktop.extension;

import java.awt.Toolkit;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ru.m210projects.Build.Render.Software.JDisplay;
import ru.m210projects.Build.Types.BGraphics;

public class DeskGraphicsSoft implements BGraphics {

	JDisplay display;
	DeskApplicationConfiguration config;
	
	long frameId = -1;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	
	DeskGraphicsSoft (DeskApplicationConfiguration config) {
		this.config = config;
	}
	
	void setupDisplay () {
		boolean displayCreated = false;

		if(!config.fullscreen) {
			displayCreated = setWindowedMode(config.width, config.height);
		} else {
			DisplayMode bestMode = null;
			for(DisplayMode mode: getDisplayModes()) {
				if(mode.width == config.width && mode.height == config.height) {
					if(bestMode == null || bestMode.refreshRate < this.getDisplayMode().refreshRate) {
						bestMode = mode;
					}
				}
			}
			if(bestMode == null) {
				bestMode = this.getDisplayMode();
			}
			displayCreated = setFullscreenMode(bestMode);
		}
		if (!displayCreated) {
			if (config.setDisplayModeCallback != null) {
				config = config.setDisplayModeCallback.onFailure(config);
				if (config != null) {
					displayCreated = setWindowedMode(config.width, config.height);
				}
			}
			if (!displayCreated) {
				throw new GdxRuntimeException("Couldn't set display mode " + config.width + "x" + config.height + ", fullscreen: "
					+ config.fullscreen);
			}
		}
		if (config.iconPaths.size > 0) {
			ByteBuffer[] icons = new ByteBuffer[config.iconPaths.size];
			for (int i = 0, n = config.iconPaths.size; i < n; i++) {
				Pixmap pixmap = new Pixmap(Gdx.files.getFileHandle(config.iconPaths.get(i), config.iconFileTypes.get(i)));
				if (pixmap.getFormat() != Format.RGBA8888) {
					Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
					rgba.drawPixmap(pixmap, 0, 0);
					pixmap.dispose();
					pixmap = rgba;
				}
				icons[i] = ByteBuffer.allocateDirect(pixmap.getPixels().limit());
				icons[i].put(pixmap.getPixels()).flip();
				pixmap.dispose();
			}
			display.setIcon(icons);
		}
		
		display.setTitle(config.title);
		display.setResizable(config.resizable);
		display.setLocation(config.x, config.y);
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
	public int getWidth() {
		return Math.max(1, display.getCanvas().getWidth());
	}

	@Override
	public int getHeight() {
		return Math.max(1, display.getCanvas().getHeight());
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

	@Override
	public GLVersion getGLVersion() {
		return null;
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
	public Monitor getPrimaryMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Monitor getMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Monitor[] getMonitors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DisplayMode getDisplayMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTitle(String title) {
		display.setTitle(title);
	}

	@Override
	public void setUndecorated(boolean undecorated) {
		display.setUndecorated(undecorated);
	}

	@Override
	public void setResizable(boolean resizable) {
		display.setResizable(resizable);
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
	public void setContinuousRendering(boolean isContinuous) {
		this.isContinuous = isContinuous;
	}

	@Override
	public boolean isContinuousRendering() {
		return isContinuous;
	}

	@Override
	public void requestRendering() {
		synchronized (this) {
			requestRendering = true;
		}
	}

	@Override
	public boolean isFullscreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSystemCursor(SystemCursor systemCursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMaxFramerate(int fps) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultDisplayConfiguration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

}
