// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.desktop.Controllers;

import static com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isLinux;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isMac;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isWindows;

import java.io.File;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import ru.m210projects.Build.Architecture.BuildController;
import ru.m210projects.Build.Input.BuildControllers;

public class JControllers extends BuildControllers {
	
	private boolean load;

	@Override
	protected void getControllers(Array<BuildController> gamepads) {
		load();
		
		Controller[] inputs = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		for(int i = 0; i < inputs.length; i++) {
			Controller.Type type = inputs[i].getType();
			if (type == Controller.Type.STICK || 
					type == Controller.Type.GAMEPAD || 
					type == Controller.Type.WHEEL ||
					type == Controller.Type.FINGERSTICK) 
                gamepads.add(new JController(inputs[i]));
		}
	}
	
	private void load() {
		if (load) return;

		SharedLibraryLoader loader = new SharedLibraryLoader();
		File nativesDir = null;
		try {
			if (isWindows) {
				nativesDir = loader.extractFile(is64Bit ? "jinput-dx8_64.dll" : "jinput-dx8.dll", null).getParentFile();
				loader.extractFileTo(is64Bit ? "jinput-raw_64.dll" : "jinput-raw.dll", nativesDir);
			} else if (isMac) {
				nativesDir = loader.extractFile("libjinput-osx.jnilib", null).getParentFile();
			} else if (isLinux) {
				nativesDir = loader.extractFile(is64Bit ? "libjinput-linux64.so" : "libjinput-linux.so", null).getParentFile();
				loader.extractFileTo(is64Bit ? "libjinput-linux64.so" : "libjinput-linux.so", nativesDir);
			}
		} catch (Throwable ex) {
			throw new GdxRuntimeException("Unable to extract JInput natives.", ex);
		}
		System.setProperty("net.java.games.input.librarypath", nativesDir.getAbsolutePath());
		load = true;
	}

}
