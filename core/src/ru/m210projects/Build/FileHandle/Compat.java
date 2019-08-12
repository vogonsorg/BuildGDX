package ru.m210projects.Build.FileHandle;
//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Locale;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;
import static ru.m210projects.Build.FileHandle.DirectoryEntry.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

public class Compat {
	
	private static Locale usLocal = Locale.US;
	public static String FilePath;
	public static String FileUserdir;
	public static boolean FileIndicator;
	public static boolean FileDebug = false;
	private static RandomAccessFile raf;
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

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
		cache = init(path, userPath);
		DirectoryEntry user = cache.addDirectory("<userdir>", FileUserdir);
		user.InitDirectory(userPath);
	}
	
	public static String getRelativePath(String path, String parent)
	{
		if(path.length() > parent.length())
			path = path.substring(parent.length());
		else if (parent.startsWith(path))
			return null;
		
		//Debug XXX
//		String OS = System.getProperty("os.name");
//		if( (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) ) {
//			Console.LogPrint(parent + " " + new File(parent).getAbsolutePath());
//			
//			Console.LogPrint(toLowerCase(path));
//		}

		return toLowerCase(path);
	}
	
	public static String bCorrectPath(String path)
	{
		if(path != null) {
			if(!File.separator.equals("\\") && path.contains("\\"))
				path = path.replace("\\", File.separator);
			else if(!File.separator.equals("/") && path.contains("/"))
				path = path.replace("/", File.separator);
		}
		
		return path;
	}
	
	public static String getFilename(String path)
	{
		if(path != null) {
			int index = path.lastIndexOf("\\");
			if(index != -1)
				path = path.substring(index + 1);
			else {
				index = path.lastIndexOf("/");
				if(index != -1)
					path = path.substring(index + 1);
			}
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
				File handle = Bcheck(filename, opt);
				if(handle != null) {
					if(opt.equals("R")) 
						opt = "r";
					raf = new RandomAccessFile(handle, opt);
				} else return -1;
			} else {
				if(opt.equals("rw")) {
					String fpath = FilePath + filename;
					raf = new RandomAccessFile(fpath, opt);
					cache.addFile(new File(fpath));
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

			if(FileDebug)
				System.out.println("Opening " + filename + " [ " + (MAXOPENFILES-newhandle-1) + " / " + MAXOPENFILES + " ]");
			
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
				raf_list[handle] = null;
				var = 0;
				file.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return var;
	}
	
	public static int Blseek(int handle, long offset, int whence) {
		int var = -1;
		
		FileIndicator = true;
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
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
	    } 
		return var;
	}
	
	public static boolean BfileExtension(String filename, String ext)
	{
		return filename.endsWith("." + ext);
	}
	
	public static String BfileExtension(String filename)
	{
		if(filename == null || filename.isEmpty())
			return null;
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
 	
	public static int Bread(int handle, byte[] buf, int len) {
		int var = -1;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		try {
			var = fis.read(buf, 0, len);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
	    } catch (IndexOutOfBoundsException e) {
	    	return -1;
	    }
		return var;
	}
	
	public static ByteBuffer Bread(int handle, int position, int len)
	{
		ByteBuffer out = null;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		try {
			out = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, position, len);
			out.order(ByteOrder.LITTLE_ENDIAN);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
	    }
		
		return out;
	}
	
	public static ByteBuffer Bbuffer(int handle)
	{
		ByteBuffer out = null;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		try {
			out = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.length());
			out.order(ByteOrder.LITTLE_ENDIAN);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
	    }
		
		return out;
	}
	
	private static byte[] tmpbyte = new byte[1];
	private static byte[] tmpshort = new byte[2];
	private static byte[] tmpint = new byte[4];
	
	public static int Bread(int handle, int len) {
		int var = -1;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		try {
			if(len == 1)
				return fis.readByte() & 0xFF;
			else if(len == 2) {
				fis.read(tmpshort, 0, len);
				return LittleEndian.getShort(tmpshort);
			}
			else if(len == 4) {
				
				fis.read(tmpint, 0, len);
				return LittleEndian.getInt(tmpint);
			}
		
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
	    } catch (IndexOutOfBoundsException e) {
	    	return -1;
	    }
		return var;
	}
	
	public static int Bwrite(int handle, byte[] buf, int len) {
		int var = -1;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		try {
			fis.write(buf, 0, len);
			var = len;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to fileid " + handle + " \r\n " + e.getMessage());
	    } 
		return var;
	}
	
	public static int Bwrite(int handle, char[] buf, int len) {
		int var = -1;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		byte[] buffer = new byte[len];
		
		try {
			for(int i = 0; i < len && i < buf.length; i++) 
				buffer[i] = (byte) buf[i];
			fis.write(buffer);
			var = len;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to fileid " + handle + " \r\n " + e.getMessage());
	    } 
		return var;
	}
	
	public static int Bwrite(int handle, int data, int len) {
		int var = -1;
		
		FileIndicator = true;
		RandomAccessFile fis = raf_list[handle];
		byte[] buf = null;
		
		try {           
			if(len == 1) {
				buf = tmpbyte;
				buf[0] = (byte) data;
			} 
			else if(len == 2) {
				buf = tmpshort;
				LittleEndian.putShort(buf, 0, (short) data);
			}
			else if(len == 4) {
				buf = tmpint;
				LittleEndian.putInt(buf, 0, data);
			}

			fis.write(buf);
			var = len;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't write to fileid " + handle + " \r\n " + e.getMessage());
	    } 
		return var;
	}

	public static int Bfilelength(int handle) {
		int var = -1;
		RandomAccessFile fis = raf_list[handle];
		try {
			var = (int) fis.length();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't read fileid " + handle + " \r\n " + e.getMessage());
		}
		return var;
	}

	public static int Bfpos(int handle) {
		int var = -1;
		RandomAccessFile fis = raf_list[handle];
		try {
			var = (int) fis.getChannel().position();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return var;
	}
}
