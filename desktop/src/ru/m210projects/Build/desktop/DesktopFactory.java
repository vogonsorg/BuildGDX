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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglClipboard;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.utils.Clipboard;

import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication.Platform;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;
import ru.m210projects.Build.Architecture.BuildMessage;
import ru.m210projects.Build.Audio.BuildAudio;
import ru.m210projects.Build.Input.BuildControllers;
import ru.m210projects.Build.desktop.AWT.AWTGraphics;
import ru.m210projects.Build.desktop.AWT.AWTInput;
import ru.m210projects.Build.desktop.Controllers.JControllers;
import ru.m210projects.Build.desktop.Lwjgl.LwjglGraphics;
import ru.m210projects.Build.desktop.Lwjgl.LwjglInput;

public class DesktopFactory implements ApplicationFactory {

	private BuildConfiguration cfg;
	public DesktopFactory(BuildConfiguration cfg)
	{
		this.cfg = cfg;
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
		return new LwjglFiles();
	}

	@Override
	public BuildControllers getControllers() {
		return new JControllers();
	}

	@Override
	public Platform getPlatform() {
		Platform platform;
		final String osName = System.getProperty("os.name");
		if ( osName.startsWith("Windows") )
			platform = Platform.Windows;
		else if ( osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("OpenBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix") || osName.indexOf("aix") > 0 )
			platform = Platform.Linux;
		else if ( osName.startsWith("Mac OS X") || osName.startsWith("Darwin") )
			platform = Platform.MacOSX;
		else platform = null;
		
		return platform;
	}

	@Override
	public BuildInput getInput(FrameType type) {
		if(type == FrameType.GL) 
			return new LwjglInput();
		
		if(type == FrameType.Canvas)
			return new AWTInput();
		
		throw new UnsupportedOperationException("Unsupported frame type: " + type); 
	}

	@Override
	public BuildGraphics getGraphics(FrameType type) {
		if(type == FrameType.GL) 
			return new LwjglGraphics(cfg);
		
		if(type == FrameType.Canvas)
			return new AWTGraphics(cfg);
		
		throw new UnsupportedOperationException("Unsupported frame type: " + type); 
	}

	@Override
	public ApplicationType getApplicationType() {
		return ApplicationType.Desktop;
	}

	@Override
	public Clipboard getClipboard() {
		return new LwjglClipboard();
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return LwjglApplicationConfiguration.getDisplayModes();
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		return LwjglApplicationConfiguration.getDesktopDisplayMode();
	}

}
