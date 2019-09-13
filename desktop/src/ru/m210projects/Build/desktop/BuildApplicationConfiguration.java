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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Array;

public class BuildApplicationConfiguration extends LwjglApplicationConfiguration {

	public boolean borderless;
	
	Array<String> iconPaths = new Array<String>();
	Array<FileType> iconFileTypes = new Array<FileType>();
	
	public void addIcon (String path, FileType fileType) {
		iconPaths.add(path);
		iconFileTypes.add(fileType);
	}
	
	public Array<String> getIconPaths()
	{
		return iconPaths;
	}
	
	public Array<FileType> getIconFileTypes()
	{
		return iconFileTypes;
	}
}
