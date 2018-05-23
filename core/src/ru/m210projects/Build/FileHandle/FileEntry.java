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

package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Compat.*;

import java.io.File;


public class FileEntry {
	private File file;
	private String extension;
	private String relPath;
	private DirectoryEntry parent;
	
	public FileEntry(File file, DirectoryEntry parent, String relPath)
	{
		this.file = file;
		this.parent = parent;
		String filename = file.getName();
		this.extension = toLowerCase(filename.substring(filename.lastIndexOf('.') + 1));
		this.relPath = relPath;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public String getName()
	{
		return toLowerCase(file.getName());
	}
	
	public String getExtension()
	{
		return extension;
	}
	
	public String getPath()
	{
		return relPath;
	}
	
	public DirectoryEntry getParent()
	{
		return parent;
	}
	
	public String toString()
	{
		return file.getAbsolutePath() + ", Extension: " + extension;
	}
}
