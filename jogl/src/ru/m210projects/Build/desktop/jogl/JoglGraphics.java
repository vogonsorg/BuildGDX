package ru.m210projects.Build.desktop.jogl;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildGraphics;

public class JoglGraphics extends BuildGraphics {

	/** The suppored OpenGL extensions */
	private static Array<String> extensions;
	private static GLVersion glVersion;
	private BuildConfiguration config;
	public GLWindow canvas;
	private BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);

	public JoglGraphics(BuildConfiguration config) {
		this.config = config;
	}

	@Override
	protected int getRefreshRate() {
		return 120; // rate;
	}

	@Override
	protected boolean isDirty() {
		return true;
	}

	@Override
	protected void sync(int fps) {

	}

	@Override
	public int getHeight() {
		return canvas.getHeight();
	}

	@Override
	public int getWidth() {
		return canvas.getWidth();
	}

	@Override
	public int getBackBufferWidth() {
		return getWidth();
	}

	@Override
	public int getBackBufferHeight() {
		return getHeight();
	}

	@Override
	public GraphicsType getType() {
		return GraphicsType.AndroidGL;
	}

	@Override
	public GLVersion getGLVersion() {
		return glVersion;
	}

	@Override
	protected void init() throws Exception {
		boolean displayCreated = false;
		if (!config.fullscreen) {
			displayCreated = setWindowedMode(config.width, config.height);
		} else {
			DisplayMode bestMode = null;
			for (DisplayMode mode : getDisplayModes()) {
				if (mode.width == config.width && mode.height == config.height) {
					if (bestMode == null || bestMode.refreshRate < mode.refreshRate) {
						bestMode = mode;
					}
				}
			}

			if (bestMode == null) {
				bestMode = this.getDesktopDisplayMode();
			}
			displayCreated = setFullscreenMode(bestMode);
		}
		if (!displayCreated) {
			throw new GdxRuntimeException("Couldn't set display mode " + config.width + "x" + config.height
					+ ", fullscreen: " + config.fullscreen);
		}

		Array<String> iconPaths = config.getIconPaths();
		if (iconPaths.size > 0) {
			ByteBuffer[] icons = new ByteBuffer[iconPaths.size];
			for (int i = 0, n = iconPaths.size; i < n; i++) {
				Pixmap pixmap = new Pixmap(
						BuildGdx.files.getFileHandle(iconPaths.get(i), config.getIconFileTypes().get(i)));
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
			// canvas.setIcon(icons); XXX
		}

		canvas.setTitle(config.title);
//		canvas.setResizable(config.resizable);
//		canvas.setInitialBackground(0,0,0);
		canvas.setPosition(config.x, config.y);
		setUndecorated(config.borderless);

//		int gles30ContextMajorVersion = 3;
//		int gles30ContextMinorVersion = 2;
//		createDisplayPixelFormat(config.useGL30, gles30ContextMajorVersion, gles30ContextMinorVersion);
	}

	protected void initiateGL(GL gl) throws Exception {
		extractVersion(gl);
		extractExtensions(gl);
		initiateGLInstances();
	}

	private static void extractVersion(GL gl) {
		String versionString = gl.glGetString(GL.GL_VERSION);
		String vendorString = gl.glGetString(GL.GL_VENDOR);
		String rendererString = gl.glGetString(GL.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	private static void extractExtensions(GL gl) {
		extensions = new Array<String>();
//		if (glVersion.isVersionEqualToOrHigher(3, 2)) {
//			int numExtensions = gl.glGetInteger(GL30.GL_NUM_EXTENSIONS);
//			for (int i = 0; i < numExtensions; ++i)
//				extensions.add(org.lwjgl.opengl.GL30.glGetStringi(GL20.GL_EXTENSIONS, i));
//		} else
		{
			extensions.addAll(gl.glGetString(GL20.GL_EXTENSIONS).split(" "));
		}
	}

	@Override
	public boolean supportsDisplayModeChange() {
		return true;
	}

	@Override
	public Monitor getPrimaryMonitor() {
		return null; // new LwjglMonitor(0, 0, "Primary Monitor");
	}

	@Override
	public Monitor getMonitor() {
		return getPrimaryMonitor();
	}

	@Override
	public Monitor[] getMonitors() {
		return new Monitor[] { getPrimaryMonitor() };
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return getDisplayMode();
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
//		org.lwjgl.opengl.DisplayMode mode = ((LwjglDisplayMode)displayMode).mode;
//		try {
//			if (!mode.isFullscreenCapable()) {
//				Display.setDisplayMode(mode);
//			} else {
//				Display.setDisplayModeAndFullscreen(mode);
//			}
//			float scaleFactor = Display.getPixelScaleFactor();
//			config.width = (int)(mode.getWidth() * scaleFactor);
//			config.height = (int)(mode.getHeight() * scaleFactor);
//			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
//			resize = true;
//			return true;
//		} catch (LWJGLException e) {
//			return false;
//		}
		return false; // XXX
	}

	/**
	 * Kindly stolen from
	 * http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not
	 * perfect but will do.
	 */
	@Override
	public boolean setWindowedMode(int width, int height) {
//		if (getWidth() == width && getHeight() == height && !canvas.isFullscreen()) {
//			return true;
//		}
//
//		try {
//			org.lwjgl.opengl.DisplayMode targetDisplayMode = null;
//			boolean fullscreen = false;
//
//			if (fullscreen) {
//				org.lwjgl.opengl.DisplayMode[] modes = Display.getAvailableDisplayModes();
//				int freq = 0;
//
//				for (int i = 0; i < modes.length; i++) {
//					org.lwjgl.opengl.DisplayMode current = modes[i];
//
//					if ((current.getWidth() == width) && (current.getHeight() == height)) {
//						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
//							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
//								targetDisplayMode = current;
//								freq = targetDisplayMode.getFrequency();
//							}
//						}
//
//						// if we've found a match for bpp and frequence against the
//						// original display mode then it's probably best to go for this one
//						// since it's most likely compatible with the monitor
//						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
//							&& (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
//							targetDisplayMode = current;
//							break;
//						}
//					}
//				}
//			} else {
//				targetDisplayMode = new org.lwjgl.opengl.DisplayMode(width, height);
//			}
//
//			if (targetDisplayMode == null) {
//				return false;
//			}
//
//			boolean resizable = !fullscreen && config.resizable;
//
//			Display.setDisplayMode(targetDisplayMode);
//			canvas.setFullscreen(fullscreen);
//			// Workaround for bug in LWJGL whereby resizable state is lost on DisplayMode change
//			if (resizable == canvas.isResizable()) {
//				canvas.setResizable(!resizable);
//			}
//			canvas.setResizable(resizable);
//
//			float scaleFactor = canvas.getPixelScaleFactor();
//			config.width = (int)(targetDisplayMode.getWidth() * scaleFactor);
//			config.height = (int)(targetDisplayMode.getHeight() * scaleFactor);
//			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
//			resize = true;
//			return true;
//		} catch (LWJGLException e) {
//			return false;
//		}

		GLCapabilities caps = new GLCapabilities(GLProfile.getDefault()); // XXX
		canvas = GLWindow.create(caps);
		canvas.setSize(width, height);

		return true; // XXX
	}

	@Override
	public DisplayMode[] getDisplayModes() {
//		try {
//			org.lwjgl.opengl.DisplayMode[] availableDisplayModes = Display.getAvailableDisplayModes();
//			DisplayMode[] modes = new DisplayMode[availableDisplayModes.length];
//
//			int idx = 0;
//			for (org.lwjgl.opengl.DisplayMode mode : availableDisplayModes) {
//				if (mode.isFullscreenCapable()) {
//					modes[idx++] = new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(),
//						mode.getBitsPerPixel(), mode);
//				}
//			}
//
//			return modes;
//		} catch (LWJGLException e) {
//			throw new GdxRuntimeException("Couldn't fetch available display modes", e);
//		}

		return null; // XXX
	}

	@Override
	public DisplayMode getDisplayMode() {
//		org.lwjgl.opengl.DisplayMode mode = Display.getDesktopDisplayMode();
//		return new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(), mode);

		return null; // XXX
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		canvas.setTitle(title);
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the
	 * changes to take effect.
	 */
	@Override
	public void setUndecorated(boolean undecorated) {
//		System.setProperty("org.lwjgl.opengl.Window.undecorated", undecorated ? "true" : "false"); XXX
	}

	/**
	 * Display must be reconfigured via {@link #setWindowedMode(int, int)} for the
	 * changes to take effect.
	 */
	@Override
	public void setResizable(boolean resizable) {
		this.config.resizable = resizable;
//		Display.setResizable(resizable); XXX
	}

	@Override
	public BufferFormat getBufferFormat() {
		return bufferFormat;
	}

	@Override
	public void setVSync(boolean vsync) {
		this.vsync = vsync;
//		canvas.getGL().setSwapInterval(vsync ? 1 : 0); XXX
	}

	@Override
	public boolean supportsExtension(String extension) {
		return extensions.contains(extension, false);
	}

	@Override
	public boolean isFullscreen() {
		return canvas.isFullscreen();
	}

	@Override
	public com.badlogic.gdx.graphics.Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		return null;
	}

	@Override
	public void setCursor(com.badlogic.gdx.graphics.Cursor cursor) {

	}

	@Override
	public void setSystemCursor(SystemCursor systemCursor) {

	}

	public void initiateGLInstances() throws Exception {
		gl10 = new Jogles10();
//		if (usingGL30) {
//			gl30 = (GL30) LwjglGL30.newInstance();
//			gl20 = gl30;
//		} else {
		gl20 = new Jogles20();
//		} XXX

		Gdx.gl = BuildGdx.gl = gl10;
		Gdx.gl20 = BuildGdx.gl20 = gl20;
		Gdx.gl30 = BuildGdx.gl30 = gl30;
	}

	@Override
	protected void update() {
		canvas.display();
	}

	@Override
	protected void updateSize(int width, int height) {
//		if (BuildGdx.gl != null)
//			BuildGdx.gl.glViewport(0, 0, width, height);
	}

	@Override
	protected boolean wasResized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int getX() {
		return canvas.getX();
	}

	@Override
	protected int getY() {
		return canvas.getY();
	}

	@Override
	protected boolean isCloseRequested() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void dispose() {
		// canvas.destroy();
	}

	@Override
	protected boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFramesPerSecond(int fps) {
		// TODO Auto-generated method stub

	}

	@Override
	public FrameType getFrameType() {
		return FrameType.GL;
	}

	@Override
	public Object extra(Option opt, Object... obj) {
		// TODO Auto-generated method stub
		if (opt == Option.GLSetConfiguration)
			return false;
		return null;
	}
}
