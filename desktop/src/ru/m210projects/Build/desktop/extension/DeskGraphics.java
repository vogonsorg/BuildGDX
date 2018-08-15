package ru.m210projects.Build.desktop.extension;


import java.awt.Canvas;
import java.awt.Toolkit;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.Types.BGraphics;


public class DeskGraphics implements BGraphics {

	/** The suppored OpenGL extensions */
	static Array<String> extensions;
	static GLVersion glVersion;

	GL10 gl10;
	GL20 gl20;
	GL30 gl30;
	long frameId = -1;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	Canvas canvas;
	boolean vsync = false;
	boolean resize = false;
	DeskApplicationConfiguration config;
	BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	boolean softwareMode;
	boolean usingGL30;

	DeskGraphics (DeskApplicationConfiguration config) {
		this.config = config;
	}

	DeskGraphics (Canvas canvas) {
		this.config = new DeskApplicationConfiguration();
		config.width = canvas.getWidth();
		config.height = canvas.getHeight();
		this.canvas = canvas;
	}

	DeskGraphics (Canvas canvas, DeskApplicationConfiguration config) {
		this.config = config;
		this.canvas = canvas;
	}


	public int getHeight () {
		if (canvas != null)
			return Math.max(1, canvas.getHeight());
		else
			return (int)(Display.getHeight() * Display.getPixelScaleFactor());
	}

	public int getWidth () {
		if (canvas != null)
			return Math.max(1, canvas.getWidth());
		else
			return (int)(Display.getWidth() * Display.getPixelScaleFactor());
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
		return glVersion;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	@Override
	public void setGL20 (GL20 gl20) {
		this.gl20 = gl20;
		if (gl30 == null) {
			Gdx.gl20 = gl20;
		}
	}

	@Override
	public boolean isGL30Available () {
		return gl30 != null;
	}

	@Override
	public GL30 getGL30 () {
		return gl30;
	}

	@Override
	public void setGL30 (GL30 gl30) {
		this.gl30 = gl30;
		if (gl30 != null) {
			this.gl20 = gl30;

			Gdx.gl = gl10;
			Gdx.gl20 = gl20;
			Gdx.gl30 = gl30;
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

	void setupDisplay () throws LWJGLException {
		if (config.useHDPI) {
			System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
		}

		if (canvas != null) {
			Display.setParent(canvas);
		} else {
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
		}
		Display.setTitle(config.title);
		Display.setResizable(config.resizable);
		Display.setInitialBackground(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
			config.initialBackgroundColor.b);

		Display.setLocation(config.x, config.y);
		createDisplayPixelFormat(config.useGL30, config.gles30ContextMajorVersion, config.gles30ContextMinorVersion);
		initiateGL();
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
	
	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setWindowedMode (int width, int height) {
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
	public DisplayMode[] getDisplayModes () {
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
	public DisplayMode getDisplayMode () {
		org.lwjgl.opengl.DisplayMode mode = Display.getDesktopDisplayMode();
		return new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(), mode);
	}

	@Override
	public void setTitle (String title) {
		Display.setTitle(title);
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
		Display.setResizable(resizable);
	}

	@Override
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	@Override
	public void setVSync (boolean vsync) {
		this.vsync = vsync;
		Display.setVSyncEnabled(vsync);
	}

	@Override
	public boolean supportsExtension (String extension) {
		return extensions.contains(extension, false);
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
			return rq || isContinuous || Display.isDirty();
		}
	}

	@Override
	public boolean isFullscreen () {
		return Display.isFullscreen();
	}

	public boolean isSoftwareMode () {
		return softwareMode;
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
		if (canvas != null && SharedLibraryLoader.isMac) {
			return;
		}
		try {
			Mouse.setNativeCursor(null);
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't set system cursor");
		}
	}

	private class LwjglDisplayMode extends DisplayMode {
		org.lwjgl.opengl.DisplayMode mode;

		public LwjglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, org.lwjgl.opengl.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}

	private class LwjglMonitor extends Monitor {
		protected LwjglMonitor (int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}

	@Override
	public boolean setDisplayConfiguration(float gamma, float brightness, float contrast) {
		try {
			/*
			 * 1, 0, 1 - default
			 * gamma: 0.3 - 2
			 * brightness: -0.5 - 0.5
			 * contrast: 0 - 2
			 */
			Display.setDisplayConfiguration(gamma, brightness, contrast);
			return true;
		} catch (Exception e) {}
		
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
}
