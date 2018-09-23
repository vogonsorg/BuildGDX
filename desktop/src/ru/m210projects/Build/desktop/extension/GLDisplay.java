package ru.m210projects.Build.desktop.extension;

import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.BufferFormat;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.Types.BDisplay;

public class GLDisplay implements BDisplay {
	
	GL10 gl10;
	GL20 gl20;
	GL30 gl30;

	DeskApplicationConfiguration config;
	
	/** The suppored OpenGL extensions */
	static Array<String> extensions;
	static GLVersion glVersion;
	
	boolean usingGL30;
	boolean softwareMode;
	boolean resize = false;
	BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);

	public GLDisplay(DeskApplicationConfiguration config)
	{
		LwjglNativesLoader.load();
		this.config = config;
		if (config.useHDPI) {
			System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
		}

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
			Display.setIcon(icons);
		}
		
		Display.setTitle(config.title);
		Display.setResizable(config.resizable);
		Display.setInitialBackground(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
			config.initialBackgroundColor.b);

		Display.setLocation(config.x, config.y);
		createDisplayPixelFormat(config.useGL30, config.gles30ContextMajorVersion, config.gles30ContextMinorVersion);
		initiateGL();
	}

	private void createDisplayPixelFormat (boolean useGL30, int gles30ContextMajor, int gles30ContextMinor) {
		try {
			if (useGL30) {
				ContextAttribs context = new ContextAttribs(gles30ContextMajor, gles30ContextMinor).withForwardCompatible(false)
					.withProfileCore(true);
				try {
					Display.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil,
						config.samples), context);
				} catch (Exception e) {
					System.out.println("LwjglGraphics: OpenGL " + gles30ContextMajor + "." + gles30ContextMinor
						+ "+ core profile (GLES 3.0) not supported.");
					createDisplayPixelFormat(false, gles30ContextMajor, gles30ContextMinor);
					return;
				}
				System.out.println("LwjglGraphics: created OpenGL " + gles30ContextMajor + "." + gles30ContextMinor
					+ "+ core profile (GLES 3.0) context. This is experimental!");
				usingGL30 = true;
			} else {
				Display
					.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil, config.samples));
				usingGL30 = false;
			}
			bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples,
				false);
		} catch (Exception ex) {
			Display.destroy();
			try {
				Thread.sleep(200);
			} catch (InterruptedException ignored) {
			}
			try {
				Display.create(new PixelFormat(0, 16, 8));
				if (getDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 8, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 16, 8, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 32) {
					bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
				}
			} catch (Exception ex2) {
				Display.destroy();
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignored) {
				}
				try {
					Display.create(new PixelFormat());
				} catch (Exception ex3) {
					if (!softwareMode && config.allowSoftwareMode) {
						softwareMode = true;
						System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
						createDisplayPixelFormat(useGL30, gles30ContextMajor, gles30ContextMinor);
						return;
					}
					throw new GdxRuntimeException("OpenGL is not supported by the video driver.", ex3);
				}
				if (getDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 8, 0, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 8, 0, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 32) {
					bufferFormat = new BufferFormat(8, 8, 8, 8, 8, 0, 0, false);
				}
			}
		}
	}

	public void initiateGLInstances () {
		if (usingGL30) {
			gl30 = new DeskGL30();
			gl20 = gl30;
		} else {
			gl20 = new DeskGL20();
			gl10 = new DeskGL10();
		}

		Gdx.gl = gl10;
		Gdx.gl20 = gl20;
		Gdx.gl30 = gl30;
	}
	
	/**
	 * Only needed when setupDisplay() is not called.
	 */
	void initiateGL() {
		extractVersion();
		extractExtensions();
		initiateGLInstances();
	}
	
	private static void extractVersion () {
		String versionString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		String vendorString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VENDOR);
		String rendererString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	private static void extractExtensions () {
		extensions = new Array<String>();
		if (glVersion.isVersionEqualToOrHigher(3, 2)) {
			int numExtensions = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
			for (int i = 0; i < numExtensions; ++i)
				extensions.add(org.lwjgl.opengl.GL30.glGetStringi(GL20.GL_EXTENSIONS, i));
		} else {
			extensions.addAll(org.lwjgl.opengl.GL11.glGetString(GL20.GL_EXTENSIONS).split(" "));
		}
	}

	@Override
	public void setTitle(String title) {
		Display.setTitle(title);
	}

	@Override
	public void setUndecorated(boolean undecorated) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", undecorated ? "true" : "false");
	}

	@Override
	public void setResizable(boolean resizable) {
		Display.setResizable(resizable);
	}

	@Override
	public void sync(int frameRate) {
		Display.sync(frameRate);
	}
	
	@Override
	public void setVSync(boolean vsync) {
		Display.setVSyncEnabled(vsync);
	}

	@Override
	public boolean isFullscreen() {
		return Display.isFullscreen();
	}

	@Override
	public int getX() {
		return Display.getX();
	}

	@Override
	public int getY() {
		return Display.getY();
	}

	@Override
	public void update() {
		Display.update(false);
	}

	@Override
	public void destroy() {
		Display.destroy();
	}

	@Override
	public void process() {
		Display.processMessages();
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
	public boolean wasResized() {
		return Display.wasResized();
	}

	@Override
	public int getWidth () {
		return (int)(Display.getWidth() * Display.getPixelScaleFactor());
	}
	
	@Override
	public int getHeight () {
		return (int)(Display.getHeight() * Display.getPixelScaleFactor());
	}

	@Override
	public BufferFormat getBufferFormat() {
		return bufferFormat;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return extensions.contains(extension, false);
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		if (getWidth() == width && getHeight() == height && !Display.isFullscreen()) {
			return true;
		}

		try {
			org.lwjgl.opengl.DisplayMode targetDisplayMode = null;
			
			targetDisplayMode = new org.lwjgl.opengl.DisplayMode(width, height);

			boolean resizable = config.resizable;

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(false);
			// Workaround for bug in LWJGL whereby resizable state is lost on DisplayMode change
			if (resizable == Display.isResizable()) {
				Display.setResizable(!resizable);
			}
			Display.setResizable(resizable);

			float scaleFactor = Display.getPixelScaleFactor();
			config.width = (int)(targetDisplayMode.getWidth() * scaleFactor);
			config.height = (int)(targetDisplayMode.getHeight() * scaleFactor);
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
			resize = true;
			return true;
		} catch (LWJGLException e) {
			return false;
		}
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		org.lwjgl.opengl.DisplayMode mode = ((LwjglDisplayMode)displayMode).mode;
		try {
			if (!mode.isFullscreenCapable()) {
				Display.setDisplayMode(mode);
			} else {
				Display.setDisplayModeAndFullscreen(mode);
			}
			float scaleFactor = Display.getPixelScaleFactor();
			config.width = (int)(mode.getWidth() * scaleFactor);
			config.height = (int)(mode.getHeight() * scaleFactor);
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
			resize = true;
			return true;
		} catch (LWJGLException e) {
			return false;
		}
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		try {
			org.lwjgl.opengl.DisplayMode[] availableDisplayModes = Display.getAvailableDisplayModes();
			DisplayMode[] modes = new DisplayMode[availableDisplayModes.length];

			int idx = 0;
			for (org.lwjgl.opengl.DisplayMode mode : availableDisplayModes) {
				if (mode.isFullscreenCapable()) {
					modes[idx++] = new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(),
						mode.getBitsPerPixel(), mode);
				}
			}

			return modes;
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't fetch available display modes", e);
		}
	}

	@Override
	public DisplayMode getDisplayMode() {
		org.lwjgl.opengl.DisplayMode mode = Display.getDesktopDisplayMode();
		return new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(), mode);
	}
	
	private class LwjglDisplayMode extends DisplayMode {
		org.lwjgl.opengl.DisplayMode mode;

		public LwjglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, org.lwjgl.opengl.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}

	@Override
	public DisplayType getType() {
		return DisplayType.GL;
	}
	
	@Override
	public boolean isDirty() {
		return Display.isDirty();
	}
	
	protected boolean setDisplayConfiguration(float gamma, float brightness, float contrast) {
		try {
			/*
			 * 1, 0, 1 - default
			 * gamma: 0.3 - 2
			 * brightness: -0.5 - 0.5
			 * contrast: 0 - 2
			 */
			Display.setDisplayConfiguration(gamma, brightness, contrast);
		} catch (Exception e) { return false; }
		
		return true;
	}
	
	public boolean isGL20Available () {
		return gl20 != null;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public void setGL20 (GL20 gl20) {
		this.gl20 = gl20;
		if (gl30 == null) {
			Gdx.gl20 = gl20;
		}
	}

	public boolean isGL30Available () {
		return gl30 != null;
	}

	public GL30 getGL30 () {
		return gl30;
	}

	public void setGL30 (GL30 gl30) {
		this.gl30 = gl30;
		if (gl30 != null) {
			this.gl20 = gl30;

			Gdx.gl = gl10;
			Gdx.gl20 = gl20;
			Gdx.gl30 = gl30;
		}
	}

	@Override
	public void resize(int width, int height) {
		Gdx.gl.glViewport(0, 0, width, height);
	}
}
