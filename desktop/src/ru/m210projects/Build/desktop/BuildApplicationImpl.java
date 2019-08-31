// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.desktop;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglClipboard;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;

import ru.m210projects.Build.Architecture.BuildApplication;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;
import ru.m210projects.Build.Audio.BuildAudio;
import ru.m210projects.Build.Render.Renderer.RenderType;
import ru.m210projects.Build.desktop.Controllers.JControllers;
import ru.m210projects.Build.desktop.gl.GLFrameImpl;
import ru.m210projects.Build.desktop.software.SoftFrameImpl;

public class BuildApplicationImpl implements BuildApplication {
	protected Frame frame;
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
	protected final BuildApplicationConfiguration config;
	protected final LwjglFiles files;
	protected final LwjglNet net;
	protected final Platform platform;
	
	public BuildApplicationImpl (ApplicationListener listener, DesktopMessage message, RenderType type, BuildApplicationConfiguration config) {
		setApplicationLogger(new LwjglApplicationLogger());
		
		if (config.title == null) config.title = listener.getClass().getSimpleName();
		this.config = config;
		
		setFrame(type.getFrameType());

		this.listener = listener;
		this.preferencesdir = config.preferencesDirectory;
		this.preferencesFileType = config.preferencesFileType;

		files = new LwjglFiles();
		net = new LwjglNet(config);

		Gdx.app = this;
		Gdx.files = files;
		Gdx.net = net;
		
		BuildGdx.app = this;
		BuildGdx.files = Gdx.files;
		BuildGdx.net = Gdx.net;
		BuildGdx.audio = new BuildAudio();
		BuildGdx.message = message;
		BuildGdx.controllers = new JControllers();

		initialize();
		
		final String osName = System.getProperty("os.name");
		if ( osName.startsWith("Windows") )
			platform = Platform.Windows;
		else if ( osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("OpenBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix") || osName.indexOf("aix") > 0 )
			platform = Platform.Linux;
		else if ( osName.startsWith("Mac OS X") || osName.startsWith("Darwin") )
			platform = Platform.MacOSX;
		else platform = null;
	}

	@Override
	public FrameType getFrameType() {
		return frame.getType();
	}
	
	@Override
	public BuildFrame getFrame() {
		return frame;
	}
	
	@Override
	public void setFrame(FrameType type)
	{
		if(frame == null || frame.getType() != type) {
			if(frame != null) frame.destroy();
			Frame fr = null;
			if(type == FrameType.Canvas) {
				fr = new SoftFrameImpl(config); 
			} else 
				fr = new GLFrameImpl(config);
			
			if(frame != null) fr.init();

			this.frame = fr;
			Gdx.graphics = frame.getGraphics();
			Gdx.input = frame.getInput();
			
			BuildGdx.graphics = frame.getGraphics();
			BuildGdx.input = frame.getInput();
			frame.setVSync(config.vSyncEnabled);
		}
	}

	private void initialize () {
		mainLoopThread = new Thread("Build Application") {
			@Override
			public void run () {
				try {
					BuildApplicationImpl.this.mainLoop();
				} catch (Throwable t) {
					Gdx.input.setCursorCatched(false);
					if(listener != null) {
						listener.pause();
						listener.dispose();
					}
					if(BuildGdx.audio != null)
						BuildGdx.audio.dispose();
					if(BuildGdx.message != null)
						BuildGdx.message.dispose();
					frame.destroy();
					
					if (t instanceof RuntimeException)
						throw (RuntimeException)t;
					else
						throw new GdxRuntimeException(t);
				}
			}
		};
		mainLoopThread.start();
	}

	private void mainLoop () {
		SnapshotArray<LifecycleListener> lifecycleListeners = this.lifecycleListeners;

		frame.init();

		listener.create();

		boolean wasActive = true;
		while (running) {
			if(!frame.isReady()) {
				// Try to solve a problem "Display is not created" when resolution changed
				continue;
			}
			
			frame.getInput().processMessages();
			if (frame.isCloseRequested()) exit();

			boolean isActive = frame.isActive();
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

			if(frame.update()) 
				listener.resize(config.width, config.height);
			
			boolean shouldRender = false;
			if (executeRunnables()) shouldRender = true;

			// If one of the runnables set running to false, for example after an exit().
			if (!running) break;

			if(frame.checkRender(shouldRender)) {
				listener.render();
				frame.repaint();
			}
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
		if(BuildGdx.audio != null)
			BuildGdx.audio.dispose();
		if(BuildGdx.message != null)
			BuildGdx.message.dispose();
		frame.destroy();
		if (config.forceExit) System.exit(-1);
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
	public BuildGraphics getGraphics () {
		return frame.getGraphics();
	}

	@Override
	public BuildInput getInput () {
		return frame.getInput();
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
		System.out.println("LWJGL2 version " + org.lwjgl.Sys.getVersion()); //2.9.3
		return 1910; //ligdx 1.9.10
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

	@Override
	public Platform getPlatform() {
		return platform;
	}
}
