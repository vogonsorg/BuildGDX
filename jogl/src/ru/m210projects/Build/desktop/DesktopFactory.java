// This file is part of BuildGDX.
// Copyright (C) 2017-2020  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

import java.util.List;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication.Platform;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildController;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;
import ru.m210projects.Build.Architecture.BuildMessage;
import ru.m210projects.Build.Audio.BuildAudio;
import ru.m210projects.Build.Input.BuildControllers;
import ru.m210projects.Build.desktop.jogl.JoglFrame;
import ru.m210projects.Build.desktop.jogl.JoglGraphics;
import ru.m210projects.Build.desktop.jogl.JoglInput;

public class DesktopFactory implements ApplicationFactory {

	private final BuildConfiguration cfg;
	private final ApplicationListener listener;

	public DesktopFactory(ApplicationListener listener, BuildConfiguration cfg) {
		this.cfg = cfg;
		this.listener = listener;
		GdxNativesLoader.load();
	}

	@Override
	public ApplicationListener getListener() {
		return listener;
	}

	@Override
	public BuildConfiguration getConfiguration() {
		return cfg;
	}

	@Override
	public BuildMessage getMessage() {
		return new DesktopMessage(false);
	}

	@Override
	public BuildAudio getAudio() {
		return new BuildAudio();
	}

	@Override
	public Files getFiles() {
		return null;
	}

	@Override
	public BuildControllers getControllers() {
		return new BuildControllers() {
			@Override
			protected void getControllers(List<BuildController> gamepads) {

			}
		};
	}

	@Override
	public Platform getPlatform() {
		Platform platform;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows"))
			platform = Platform.Windows;
		else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("OpenBSD")
				|| osName.startsWith("SunOS") || osName.startsWith("Unix") || osName.indexOf("aix") > 0)
			platform = Platform.Linux;
		else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin"))
			platform = Platform.MacOSX;
		else
			platform = null;

		return platform;
	}

	@Override
	public BuildFrame getFrame() {
		return new JoglFrame(listener, cfg);
	}

	@Override
	public ApplicationType getApplicationType() {
		return ApplicationType.Desktop;
	}

	@Override
	public Clipboard getClipboard() {
		return null;
	}

	@Override
	public int getVersion() {
		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if (dot != -1) {
				version = version.substring(0, dot);
			}
		}
		return Integer.parseInt(version);
	}

	public static void InitVideoModes() {
		// VideoMode.initVideoModes(LwjglApplicationConfiguration.getDisplayModes(),
		// LwjglApplicationConfiguration.getDesktopDisplayMode());
	}

	public static void InitSoundDrivers() {
//		BuildAudio.registerDriver(Driver.Sound, new ALSoundDrv(new ALSoundDrv.DriverCallback() {
//			public ALAudio InitDriver() throws Throwable {
//				return new LwjglAL();
//			}
//		}, "OpenAL 1.15.1"));
//
//		BuildAudio.registerDriver(Driver.Sound, new ALSoundDrv(new ALSoundDrv.DriverCallback() {
//			public ALAudio InitDriver() throws Throwable {
//				return new GdxAL();
//			}
//		}, "OpenAL 1.18.1"));
	}
}
