package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;
import static ru.m210projects.Build.Strhandler.Bstrcmp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.FileHandle.IResource.RESHANDLE;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class Cache1D {
	
	public static final int GRP = 1;
	public static final int RFF = 2;
	public static final int ZIP = 4;
	public static final int EXT = 8;
	private static final int DYNAMIC = 16;
	
	static final int MAXGROUPFILES = 16;
	static final int MAXOPENFILES = 64;
	
	static List<IResource> groupfil = new ArrayList<IResource>();
	
	static int[] filegrp = new int[MAXOPENFILES];
	static int[] filehan;

	final static int grpsign = 0x536E654B;
	final static int zipsign = 0x04034b50;
	final static int rffsign = 0x1A464652;
	
	static {
		if(filehan == null)
		{
			filehan = new int[MAXOPENFILES];
			Arrays.fill(filehan, -1);
		}
	}
	
	public static IResource checkgroupfile(String filename) throws Exception
	{
		int handle = Bopen(filename, "r");
		if(handle != -1) {
			byte[] buf = new byte[12];
			Bread(handle, buf, buf.length);
			
			ByteBuffer bb = ByteBuffer.wrap(buf, 0, 4);
	    	bb.order( ByteOrder.LITTLE_ENDIAN);
	    	int sign = bb.getInt();
	    	Bclose(handle);

	    	switch(sign)
	    	{
	    	case grpsign: //KenS
	    		String strbuf = new String(buf, 0, 12);
				if(Bstrcmp(strbuf, "KenSilverman") == 0) {
					GRPResource grp = new GRPResource(filename);
					grp.type = GRP;
					grp.name = filename;
					return grp;
				}
	    		break;
	    	case zipsign:
	    		ZIPResource zip = new ZIPResource(filename);
	    		zip.name = filename;
	    		zip.type = ZIP;
	    		return zip;
	    	case rffsign:
	    		RFFResource rff = new RFFResource(filename);
	    		rff.name = filename;
	    		rff.type = RFF;
				return rff;
	    	}	
		}
		return null;
	}
	
	public static int initgroupfile(String filename, boolean isStatic) throws Exception
	{
		if (groupfil.size() >= MAXGROUPFILES) return -1;
		
		IResource res = checkgroupfile(filename);
		if(res != null)
		{
			if(!isStatic)
				res.type |= DYNAMIC;
			groupfil.add(res);
			return groupfil.size()-1;
		}

		return -1;
	}
	
	public static int kGroupNew(String name, boolean dynamic)
	{
		if (groupfil.size() >= MAXGROUPFILES) return -1;

		EXTResource group = new EXTResource();

		group.name = name;
		group.type = EXT;
		if(dynamic)
			group.type |= DYNAMIC;
		groupfil.add(group);
		
		return groupfil.size()-1;
	}
	
	public static boolean kGroupAdd(int groupnum, String filename, int fileid)
	{
		if(groupnum < 0 || filename == null || filename.isEmpty()) return false;
		IResource group = groupfil.get(groupnum);
		if((group.type & EXT) != 0)
		{
			EXTResource ext = (EXTResource) group;
			return ext.addResource(filename, fileid);
		}
		return false;
	}

	public static void kDynamicClear()
	{
		for (Iterator<IResource> iterator = groupfil.iterator(); iterator.hasNext();) {
			IResource group = iterator.next();
			if ((group.type & DYNAMIC) != 0) {
				System.err.println("remove dynamic group: " + group.name);
				group.Dispose();
				iterator.remove();
			}
		}
	}
	
	public static List<RESHANDLE> kDynamicList()
	{
		List<RESHANDLE> list = new ArrayList<RESHANDLE>();
		for (Iterator<IResource> iterator = groupfil.iterator(); iterator.hasNext();) {
			IResource group = iterator.next();
			if ((group.type & DYNAMIC) != 0) {
				list.addAll(group.fList());
			}
		}
		return list;
	}
	
	public static List<RESHANDLE> kList(int groupnum)
	{
		if(groupnum < 0) return null;

		return groupfil.get(groupnum).fList();
	}

	public static boolean kExist(int fileId, String type)
	{
		int i = -1;
		for(int k=groupfil.size()-1;k>=0;k--)
		{
			IResource group = groupfil.get(k);
			if (group.type != GRP) {
				if((i = group.Lookup(fileId, type)) != -1)
				{
					group.Close(i);
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean kExist(String filename, int searchfirst)
	{
		if (searchfirst == 0)
			if (cache.checkFile(filename) != null)
				return true;
		
		int i = -1;
		for(int k=groupfil.size()-1;k>=0;k--)
		{
			if (searchfirst == 1) k = 0;
			IResource group = groupfil.get(k);
			if((i = group.Lookup(filename)) != -1) {
				group.Close(i);
				return true;
			}
		}
		return false;
	}

	public static int kOpen(String filename, int searchfirst)
	{
		int i, k, fil, newhandle;
		
		newhandle = MAXOPENFILES-1;
		while (filehan[newhandle] != -1)
		{
			newhandle--;
			if (newhandle < 0)
			{
				Console.Println("TOO MANY FILES OPEN IN FILE GROUPING SYSTEM!", OSDTEXT_RED);
				System.exit(0);
			}
		}
		
		//Search in dynamic group first
		for(k=groupfil.size()-1;k>=0;k--)
		{
			IResource group = groupfil.get(k);
			if ((group.type & DYNAMIC) != 0) {
				if((i = group.Lookup(filename)) != -1)
				{
					filegrp[newhandle] = k;
					filehan[newhandle] = i;
					return newhandle;
				}
			}
		}

		if (searchfirst == 0) {
			if ((fil = Bopen(filename, "r")) != -1)
			{
				filegrp[newhandle] = 255;
				filehan[newhandle] = fil;
				return newhandle;
			}
		}
		
		for(k=groupfil.size()-1;k>=0;k--)
		{
			if (searchfirst == 1) k = 0;
			IResource group = groupfil.get(k);
			if((i = group.Lookup(filename)) != -1)
			{
				filegrp[newhandle] = k;
				filehan[newhandle] = i;

				return newhandle;
			}
		}
		return -1;
	}

	public static int kRead(int handle, byte[] buffer, int leng)
	{
		int filenum = filehan[handle];
		int groupnum = filegrp[handle];

		if (groupnum == 255) return(Bread(filenum,buffer,leng));
		
		if (groupnum >= 0 && groupnum < groupfil.size())
			return groupfil.get(groupnum).Read(filenum, buffer, leng);
		return(0);
	}

	public static byte[] kGetBytes(int fileId, String type)
	{
		int handle = kOpen(fileId, type);
		byte[] out = null;
		if (handle >= 0 && (out = kLock(handle)) != null)
			kClose(handle);

		return out;
	}
	
	public static byte[] kGetBytes(String filename, int searchfirst)
	{
		int handle = kOpen(filename, searchfirst);
		byte[] out = null;
		if (handle >= 0 && (out = kLock(handle)) != null)
			kClose(handle);

		return out;
	}
	
	public static ByteBuffer kGetBuffer(int fileId, String type)
	{
		int handle = kOpen(fileId, type);
		ByteBuffer out = null;
		if (handle >= 0 && (out = kbLock(handle)) != null)
			kClose(handle);

		return out;
	}
	
	public static ByteBuffer kGetBuffer(String filename, int searchfirst)
	{
		int handle = kOpen(filename, searchfirst);
		ByteBuffer out = null;
		if (handle >= 0 && (out = kbLock(handle)) != null)
			kClose(handle);

		return out;
	}
	
	public static int klseek(int handle, int offset, int whence) {
		int groupnum = filegrp[handle];
		if (groupnum == 255) return Blseek(filehan[handle], offset, whence);
	
		if (groupnum >= 0 && groupnum < groupfil.size())
	        return groupfil.get(groupnum).Seek(filehan[handle], offset, whence);
		
	    return(-1);
	}
	
	public static int kFileLength(int handle)
	{
		int groupnum = filegrp[handle];
		if (groupnum == 255)
			return Bfilelength(filehan[handle]);
		int i = filehan[handle];
		if (groupnum >= 0 && groupnum < groupfil.size())
			return(groupfil.get(groupnum).Size(i));
		
		return -1;
	}
	
	public static void kClose(int handle) {
		if (handle < 0) return;
		int groupnum = filegrp[handle];
		if (groupnum == 255) 
			Bclose(filehan[handle]);
		else if (groupnum >= 0 && groupnum < groupfil.size())
			groupfil.get(groupnum).Close(filehan[handle]);
		
		filehan[handle] = -1;
	}


	
	
	
	
	
	
	

	public static void kfilelist()
	{
		System.out.println("Start list");
		for(int i = 0; i < MAXOPENFILES-1; i++)
		{
			if(filehan[i] != -1)
			{
				int groupnum = filegrp[i];
				System.out.println(i + " " + groupfil.get(groupnum).Name(filehan[i]));
			}
		}
		System.out.println();
	}
	
	private static int kOpen(int fileId, String type)
	{
		int newhandle = MAXOPENFILES-1;
		while (filehan[newhandle] != -1)
		{
			newhandle--;
			if (newhandle < 0)
			{
				Console.Println("TOO MANY FILES OPEN IN FILE GROUPING SYSTEM!", OSDTEXT_RED);
				System.exit(0);
			}
		}
		
		for(int k=groupfil.size()-1;k>=0;k--)
		{
			IResource group = groupfil.get(k);
			if (group.type != GRP)
			{
				int i = -1;
				if((i = group.Lookup(fileId, type)) != -1)
				{
					filegrp[newhandle] = k;
					filehan[newhandle] = i;

					return newhandle;
				}
			}
		}

		return -1;
	}
	
	private static byte[] kLock(int handle)
	{
		int filenum = filehan[handle];
		int groupnum = filegrp[handle];
		if (groupnum == 255) {
			int leng = Bfilelength(filenum);
			byte[] buffer = new byte[leng];
			Bread(filenum,buffer,leng);
			return buffer;
		}
		
		if (groupnum >= 0 && groupnum < groupfil.size())
			return groupfil.get(groupnum).Lock(filenum);
		
		return null;
	}
	
	private static ByteBuffer kbLock(int handle)
	{
		int filenum = filehan[handle];
		int groupnum = filegrp[handle];
		if (groupnum == 255) {
			ByteBuffer byteBuffer = null;
			int leng = Bfilelength(filenum);
			byte[] buffer = new byte[leng];
			Bread(filenum,buffer,leng);
			
			byteBuffer = BufferUtils.newByteBuffer(leng);
			byteBuffer.put(buffer);
			byteBuffer.rewind();
			
			return byteBuffer;
		}
		
		if (groupnum >= 0 && groupnum < groupfil.size())
			return groupfil.get(groupnum).bLock(filenum);
		
		return null;
	}
}
