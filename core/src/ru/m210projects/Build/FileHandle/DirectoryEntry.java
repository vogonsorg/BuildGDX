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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import static ru.m210projects.Build.FileHandle.Compat.*;

public class DirectoryEntry {
	private HashMap<String, DirectoryEntry> subDirectory;
	private HashMap<String, FileEntry> files;
	private String name;
	private String relativePath;
	private String absolutePath;
	private DirectoryEntry parentDir;
	private boolean inited;
	
	private static HashMap<String, DirectoryEntry> cache;

	protected DirectoryEntry(String name, String Path)
	{
		this.files = new HashMap<String, FileEntry>();
		this.subDirectory = new HashMap<String, DirectoryEntry>();
		this.name = name;
		if(Path != null)
			this.relativePath = getRelativePath(Path);
		this.absolutePath = Path;
	}
	
	public void addFile(File file)
	{
		files.put(toLowerCase(file.getName()), new FileEntry(file, this, getRelativePath(file.getAbsolutePath())));
	}
	
	public DirectoryEntry addDirectory(String name, String relPath)
	{
		DirectoryEntry added = new DirectoryEntry(name, relPath);
		subDirectory.put(toLowerCase(name), added);
		return added;
	}
	
	public String getAbsolutePath()
	{
		return absolutePath;
	}
	
	public String getRelativePath()
	{
		return relativePath;
	}
	
	public DirectoryEntry getParent()
	{
		return parentDir;
	}

	public FileEntry checkFile(String filepath)
	{
		if(filepath == null) return null;
		
		filepath = toLowerCase(filepath);
		DirectoryEntry dir = this;
		while(filepath.indexOf(File.separator) != -1)
		{
			int index = filepath.indexOf(File.separator);
			String folder = filepath.substring(0, index);
			if(dir == null)
				return null;
			dir = dir.checkDirectory(folder);
			filepath = filepath.substring(index + 1);
		}
		if(dir == null)
			return null;
		return dir.files.get(filepath);
	}

	public DirectoryEntry checkDirectory(String dirpath)
	{
		dirpath = toLowerCase(dirpath);
		DirectoryEntry dir = this;
		while(dirpath.indexOf(File.separator) != -1)
		{
			int index = dirpath.indexOf(File.separator);
			String folder = dirpath.substring(0, index);
			if(dir == null) return null;
			dir = dir.checkDirectory(folder);
			dirpath = dirpath.substring(index + 1);
		}
		if(dir == null) return null;
		DirectoryEntry subDir = dir.subDirectory.get(dirpath);
		if(subDir != null)
			subDir.InitDirectory(subDir.getAbsolutePath());
		return subDir;
	}

	public HashMap<String, FileEntry> getFiles()
	{
		return files;
	}

	public HashMap<String, DirectoryEntry> getDirectories()
	{
		return subDirectory;
	}
	
	public String getName()
	{
		return name;
	}

	protected static DirectoryEntry updateCacheList(String mainpath)
	{
		if(cache == null)
			cache = new HashMap<String, DirectoryEntry>();
		else cache.clear();
		String dirName = "<main>";
		DirectoryEntry dir = new DirectoryEntry(dirName, null);
	    cache.put(dirName, dir);
	    dir.InitDirectory(mainpath);
		return cache.get(dirName);
	}
	
	public boolean checkCacheList()
	{
		if(!inited) return false;
		int currentSize = files.size() + subDirectory.size();
		boolean isMain = name.equals("<main>");
		File directory = null;
		
		if(isMain) {
			directory = new File(FilePath);
			currentSize--; //because <userdir>
		} else directory = new File(absolutePath);
		
		File[] fList = directory.listFiles();
		if(fList != null && currentSize != fList.length)
		{
			DirectoryEntry userdir = null;
			if(isMain) userdir = checkDirectory("<userdir>");
			
			subDirectory.clear();
			files.clear();
			
			for (File file : fList) {
		    	if (file.isFile()) 
		    		addFile(file);
		        else {
		        	DirectoryEntry subDir = addDirectory(file.getName(), file.getAbsolutePath());
		        	subDir.parentDir = this;
		        }
		    }
			if(isMain) subDirectory.put("<userdir>", userdir);
			
			return true;
		}
		
		return false;
	}
	
	public void InitDirectory(String directoryPath)
	{
		if(inited)
			return;
		File directory = new File(directoryPath);
		File[] fList = directory.listFiles();
		if(fList != null) {
			for (File file : fList) {
		    	if (file.isFile()) 
		    		addFile(file);
		        else {
		        	DirectoryEntry subDir = addDirectory(file.getName(), file.getAbsolutePath());
		        	subDir.parentDir = this;
		        }
		    }
		}
		inited = true;
	}

	public String toString()
	{
		String out = "Directory name: " + getName() + "\r\n";
		out += "\r\nSubDirectories: \r\n";
		for (Iterator<String> it = getDirectories().keySet().iterator(); it.hasNext(); ) {
			String dir = it.next();
			out += "\t" + dir + "\r\n";
	    }
		out += "\r\nFiles: \r\n";
		for (Iterator<FileEntry> it = getFiles().values().iterator(); it.hasNext(); ) {
			FileEntry file = it.next();
			out += "\t" + file.getFile().getName() + "\r\n";
	    }

		return out;
	}
	
	public boolean isInited()
	{
		return inited;
	}
	
	private String getRelativePath(String path)
	{
		String mainpath = FilePath;
		if(name == "<userdir>")
			mainpath = FileUserdir;

		return Compat.getRelativePath(path, mainpath);
	}
}
