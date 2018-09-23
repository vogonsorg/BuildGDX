package ru.m210projects.Build.desktop.extension.software;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.BufferFormat;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ru.m210projects.Build.Types.BDisplay;
import ru.m210projects.Build.desktop.extension.DeskApplicationConfiguration;

public class SoftDisplay implements BDisplay {
	
	DeskApplicationConfiguration config;
	boolean resize = false;
	JDisplay display;
	
	public SoftDisplay(DeskApplicationConfiguration config)
	{
		boolean displayCreated = false;
		display = new JDisplay(config.width, config.height);
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

	public JCanvas getCanvas()
	{
		return display.getCanvas();
	}
	
	@Override
	public DisplayType getType() {
		return DisplayType.Software;
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
	public void sync(int frameRate) {
		/* unsupported */
	}

	@Override
	public void setVSync(boolean vsync) {
		/* unsupported */
	}

	@Override
	public boolean isFullscreen() {
		return false;
	}

	@Override
	public int getX() {
		return display.getX();
	}

	@Override
	public int getY() {
		return display.getY();
	}

	@Override
	public void update() {
		display.getCanvas().update();
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void process() {
		display.getCanvas().update();
	}

	@Override
	public boolean isCloseRequested() {
		return false;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean wasResized() {
		return false;
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
	public boolean setWindowedMode(int width, int height) {
		return true; //XXX
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		return false;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return null;
	}

	@Override
	public DisplayMode getDisplayMode() {
		return null;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public void resize(int width, int height) {
	
	}

}
