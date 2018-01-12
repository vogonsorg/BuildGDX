package ru.m210projects.Build.FileHandle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;
import static ru.m210projects.Build.FileHandle.DirectoryEntry.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

public class Compat {
	
	private static Locale usLocal = Locale.US;
	public static String FilePath;
	public static String FileUserdir;
	private static RandomAccessFile raf;
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	public static File handle; 
	private static final int MAXOPENFILES = 64;
	public static RandomAccessFile[] raf_list = new RandomAccessFile[MAXOPENFILES];
	
	public static DirectoryEntry cache;
	
	public static String toLowerCase(String text)
	{
		if(text != null)
			return text.toLowerCase(usLocal); //Turkish language issue
		return null;
	}
	
	public static void initCacheList(String path, String userPath)
	{
		cache = updateCacheList(path);
		DirectoryEntry user = cache.addDirectory("<userdir>", null);
		user.InitDirectory(userPath);
	}
	
	public static String getRelativePath(String path, String parent)
	{
		if(path.length() > parent.length())
			path = path.substring(parent.length());
		else if (parent.startsWith(path))
			return null;

		return toLowerCase(path);
	}
	
	public static String getFilename(String path)
	{
		if(path != null) {
			int index = path.lastIndexOf("\\");
			if(index != -1)
				path = path.substring(index + 1);
		}
		
		return path;
	}
	
	public static File Bcheck(String filename, String opt)
	{
		if(cache == null)
			initCacheList(FilePath, FileUserdir);
		if(opt.equals("r") || opt.equals("R")) {
			FileEntry file = null;
			if(opt.equals("R")) {
				file = cache.checkDirectory("<userdir>").checkFile(new File(filename).getName());
			} else
				file = cache.checkFile(filename);
			if(file != null)
				return file.getFile();
		}
		return null;
	}
	
	public static int Bopen(String filename, String opt) {
		if(cache == null)
			initCacheList(FilePath, FileUserdir);
		
		int var = -1;
		try {
			if(opt.equals("r") || opt.equals("R")) {
				handle = Bcheck(filename, opt);
				if(handle != null) {
					if(opt.equals("R")) 
						opt = "r";
					raf = new RandomAccessFile(handle, opt);
				} else return -1;
			} else {
				if(opt.equals("rw")) {
					raf = new RandomAccessFile(FilePath + filename, opt);
					cache.addFile(new File(filename));
				} 
				else if(opt.equals("RW"))
				{
					raf = new RandomAccessFile(filename, "rw");
					File fil = new File(filename);
					cache.checkDirectory("<userdir>").addFile(fil);
				}
			}
			
			int newhandle = MAXOPENFILES-1;
			while (raf_list[newhandle] != null)
			{
				newhandle--;
				if (newhandle < 0)
				{
					Console.Println("TOO MANY FILES OPEN!", OSDTEXT_RED);
					System.exit(0);
				}
			}

			raf_list[newhandle] = raf;
		
			var = newhandle;
		} catch (Exception e) { 
			e.printStackTrace();
			return -1;
		} 
		return var;
	}

	public static int Bclose(int handle) {
		int var = -1;
		if(handle < 0)
			return -1;
		
		RandomAccessFile file = raf_list[handle];
		try {
			if (file != null) {
				file.close();
				raf_list[handle] = null;
				var = 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return var;
	}
	
	public static int Blseek(int handle, long offset, int whence) {
		int var = -1;
		
		RandomAccessFile fis = raf_list[handle];
		try {
			if(whence == SEEK_SET) {
				fis.getChannel().position(offset);			
			} else if(whence == SEEK_CUR) {
				fis.getChannel().position(fis.getChannel().position() + offset);		
			} else if(whence == SEEK_END) {
				fis.getChannel().position(fis.getChannel().size() + offset);			
			}
			
			var = (int) fis.getChannel().position();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file " + handle);
	    } 
		return var;
	}
	
	public static boolean BfileExtension(String filename, String ext)
	{
		return filename.endsWith("." + ext);
	}
	
	public static String BfileExtension(String filename)
	{
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
 	
	public static int Bread(int handle, byte[] buf, int len) {
		int var = -1;
		
		RandomAccessFile fis = raf_list[handle];
		try {
			fis.read(buf, 0, len);
			var = len;
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file " + handle);
	    } 
		return var;
	}
	
	public static int Bread(int handle, int len) {
		int var = -1;
		RandomAccessFile fis = raf_list[handle];
		try {
			if(len == 1)
				return fis.readByte() & 0xFF;
			else if(len == 2) {
				byte[] buf = new byte[2];
				fis.read(buf, 0, len);
				return LittleEndian.getShort(buf);
			}
			else if(len == 4) {
				byte[] buf = new byte[4];
				fis.read(buf, 0, len);
				return LittleEndian.getInt(buf);
			}
		
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file " + handle);
	    } 
		return var;
	}
	
	public static int Bwrite(int handle, byte[] buf, int len) {
		int var = -1;
		
		RandomAccessFile fis = raf_list[handle];
		try {
			fis.write(buf, 0, len);
			var = len;
		} catch (IOException e) {
			throw new RuntimeException("Couldn't save file " + handle);
	    } 
		return var;
	}
	
	public static int Bwrite(int handle, char[] buf, int len) {
		int var = -1;
		
		RandomAccessFile fis = raf_list[handle];
		byte[] buffer = new byte[len];
		
		try {
			for(int i = 0; i < len && i < buf.length; i++) {
				buffer[i] = (byte) buf[i];
			}
			fis.write(buffer);
			var = len;
		} catch (IOException e) {
			throw new RuntimeException("Couldn't save file " + handle);
	    } 
		return var;
	}
	
	public static int Bwrite(int handle, int data, int len) {
		int var = -1;
		
		RandomAccessFile fis = raf_list[handle];
		byte[] buf = null;
		
		try {           
			if(len == 1) {
				buf = new byte[1];
				buf[0] = (byte) data;
			} 
			else if(len == 2) {
				buf = new byte[2];
				LittleEndian.putShort(buf, 0, (short) data);
			}
			else if(len == 4) {
				buf = new byte[4];
				LittleEndian.putInt(buf, 0, data);
			}

			fis.write(buf);
			var = len;
		} catch (IOException e) {
			throw new RuntimeException("Couldn't save file " + handle);
	    } 
		return var;
	}

	public static int Bfilelength(int handle) {
		int var = -1;
		RandomAccessFile fis = raf_list[handle];
		try {
			var = (int) fis.length();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file " + handle);
		}
		return var;
	}

	public static int Bfpos(int handle) {
		int var = -1;
		RandomAccessFile fis = raf_list[handle];
		try {
			var = (int) fis.getChannel().position();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return var;
	}
}
