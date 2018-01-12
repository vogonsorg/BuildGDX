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
