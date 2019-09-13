package ru.m210projects.Build.FileHandle;

import java.io.File;

public class FileUtils {

	public static boolean isExtension(String filename, String ext)
	{
		return filename.endsWith("." + ext);
	}
	
	public static String getExtension(String filename)
	{
		if(filename == null || filename.isEmpty())
			return null;
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
	
	public static String getFullName(String path)
	{
		if(path != null) {
			int index = -1;
			if((index = path.lastIndexOf(File.separator)) != -1 
					|| (index = path.lastIndexOf("\\")) != -1 
					|| (index = path.lastIndexOf("/")) != -1)
				path = path.substring(index + 1);
		}
		
		return path;
	}
	
	public static String getCorrectPath(String path)
	{
		if(path != null) {
			String[] separators = { "/"/* - Linux separator*/, "\\" };
			for(String separator : separators)
				if(!separator.equals(File.separator) && path.contains(separator))
					path = path.replace(separator, File.separator);
		}
		
		return path;
	}
}
