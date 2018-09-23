package ru.m210projects.Build.desktop.extension;
import java.io.File;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglClipboard;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;

public class DeskApplication implements Application {

	protected final DeskGraphics graphics;
	protected final LwjglFiles files;
	protected final DeskInput input;
	protected final LwjglNet net;
	protected final ApplicationListener listener;
	protected Thread mainLoopThread;
	protected boolean running = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
	protected int logLevel = LOG_INFO;
	protected ApplicationLogger applicationLogger;
	protected String preferencesdir;
	protected Files.FileType preferencesFileType;

	public DeskApplication (ApplicationListener listener, String title, int width, int height) {
		this(listener, createConfig(title, width, height));
	}

	public DeskApplication (ApplicationListener listener) {
		this(listener, null, 640, 480);
	}

	public DeskApplication (ApplicationListener listener, DeskApplicationConfiguration config) {
		this(listener, config, new DeskGraphics(config));
	}

	DeskApplicationConfiguration config;
	public DeskApplication (ApplicationListener listener, DeskApplicationConfiguration config, DeskGraphics graphics) {
		LwjglNativesLoader.load();
		setApplicationLogger(new LwjglApplicationLogger());

		if (config.title == null) config.title = listener.getClass().getSimpleName();
		this.graphics = graphics;
		files = new LwjglFiles();
		input = new DeskInput();
		net = new LwjglNet();
		this.listener = listener;
		this.preferencesdir = config.preferencesDirectory;
		this.preferencesFileType = config.preferencesFileType;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.files = files;
		Gdx.input = input;
		Gdx.net = net;
		this.config = config;
		initialize(config);
	}

	private static DeskApplicationConfiguration createConfig (String title, int width, int height) {
		DeskApplicationConfiguration config = new DeskApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.vSyncEnabled = true;
		return config;
	}

	private void initialize (final DeskApplicationConfiguration config) {
		mainLoopThread = new Thread("BuildEngine Application") {
			@Override
			public void run () {
				try {
					DeskApplication.this.mainLoop();
				} catch (Throwable t) {
					Gdx.input.setCursorCatched(false);
					if(listener != null) {
						listener.pause();
						listener.dispose();
					}
					if (t instanceof RuntimeException)
						throw (RuntimeException)t;
					else
						throw new GdxRuntimeException(t);
				}
			}
		};
		mainLoopThread.start();
	}

	void mainLoop () {
		SnapshotArray<LifecycleListener> lifecycleListeners = this.lifecycleListeners;
		listener.create();
		boolean resize = true;

		graphics.lastTime = System.nanoTime();
		boolean wasActive = true;
		while (running) {
			graphics.getDisplay().process();
			if (graphics.getDisplay().isCloseRequested()) exit();

			boolean isActive = graphics.getDisplay().isActive();
			if (wasActive && !isActive) { // if it's just recently minimized from active state
				wasActive = false;
				synchronized (lifecycleListeners) {
					LifecycleListener[] listeners = lifecycleListeners.begin();
					for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
						 listeners[i].pause();
					lifecycleListeners.end();
				}
				listener.pause();
			}
			if (!wasActive && isActive) { // if it's just recently focused from minimized state
				wasActive = true;
				synchronized (lifecycleListeners) {
					LifecycleListener[] listeners = lifecycleListeners.begin();
					for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
						listeners[i].resume();
					lifecycleListeners.end();
				}
				listener.resume();
			}

			boolean shouldRender = false;

			graphics.config.x = graphics.getDisplay().getX();
			graphics.config.y = graphics.getDisplay().getY();
			if (resize || graphics.getDisplay().wasResized()
				|| graphics.getDisplay().getWidth() != graphics.config.width
				|| graphics.getDisplay().getHeight() != graphics.config.height) {
				resize = false;
				graphics.config.width = graphics.getDisplay().getWidth();
				graphics.config.height = graphics.getDisplay().getHeight();
				graphics.getDisplay().resize(graphics.config.width, graphics.config.height);
				if (listener != null) listener.resize(graphics.config.width, graphics.config.height);
				graphics.requestRendering();
			}
			
			if (executeRunnables()) shouldRender = true;

			// If one of the runnables set running to false, for example after an exit().
			if (!running) break;

			
			shouldRender |= graphics.shouldRender();

			if (!isActive && graphics.config.backgroundFPS == -1) shouldRender = false;
			int frameRate = isActive ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
			if (shouldRender) {
				input.update();
				graphics.updateTime();
				graphics.frameId++;
				listener.render();
				graphics.getDisplay().update();
			} else {
				// Sleeps to avoid wasting CPU in an empty loop.
				if (frameRate == -1) frameRate = 10;
				if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
				if (frameRate == 0) frameRate = 30;
			}
			if (frameRate > 0) graphics.getDisplay().sync(frameRate);
		}

		synchronized (lifecycleListeners) {
			LifecycleListener[] listeners = lifecycleListeners.begin();
			for (int i = 0, n = lifecycleListeners.size; i < n; ++i) {
				listeners[i].pause();
				listeners[i].dispose();
			}
			lifecycleListeners.end();
		}
		listener.pause();
		listener.dispose();
		graphics.getDisplay().destroy();
		if (graphics.config.forceExit) System.exit(-1);
	}

	public boolean executeRunnables () {
		synchronized (runnables) {
			for (int i = runnables.size - 1; i >= 0; i--)
				executedRunnables.add(runnables.get(i));
			runnables.clear();
		}
		if (executedRunnables.size == 0) return false;
		do
			executedRunnables.pop().run();
		while (executedRunnables.size > 0);
		return true;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public Audio getAudio () {
		return null;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public DeskGraphics getGraphics () {
		return graphics;
	}

	@Override
	public Input getInput () {
		return input;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion () {
		System.out.println("LWJGL2 version " + org.lwjgl.Sys.getVersion());
		return 198;
	}

	public void stop () {
		running = false;
		try {
			mainLoopThread.join();
		} catch (Exception ex) {
		}
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();

	@Override
	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new LwjglPreferences(new LwjglFileHandle(new File(preferencesdir, name), preferencesFileType));
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public Clipboard getClipboard () {
		return new LwjglClipboard();
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel () {
		return logLevel;
	}

	@Override
	public void setApplicationLogger (ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger () {
		return applicationLogger;
	}


	@Override
	public void exit () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				running = false;
			}
		});
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}
}
