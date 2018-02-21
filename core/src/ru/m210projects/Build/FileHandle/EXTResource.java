package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.Strhandler.Bstrcasecmp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.BufferUtils;

public class EXTResource extends IResource {

	private class ERESHANDLE extends RESHANDLE {
		
		public FileEntry entry;
		public int fil;

		public ERESHANDLE(FileEntry file, int fileid) {
			this.filename = file.getName();
			this.fileformat = file.getExtension();
			this.entry = file;
			this.fileid = fileid;
			this.fil = -1;
			this.paktype = EXT;
		}
	}
	
	private List<ERESHANDLE> files = new ArrayList<ERESHANDLE>();

	public boolean addResource(String filename, int fileid)
	{
		if(filename == null) return false;
		
		FileEntry entry = cache.checkFile(filename);
		if(entry != null)
		{
			files.add(new ERESHANDLE(entry, fileid));
			return true;
		}
		return false;
	}

	@Override
	public int Lookup(String filename) {
		
		for(int i = 0; i < files.size(); i++) {
			ERESHANDLE file = files.get(i);
			
			boolean bad = false;
			for(int j = 0; j < filename.length(); j++)
			{
				if (filename != null && filename.isEmpty()) break;
				String compare = file.filename;
				if (j >= compare.length() || compare.codePointAt(j) >= toupperlookup.length
						|| toupperlookup[filename.codePointAt(j)]
								!= toupperlookup[compare.codePointAt(j)])
				{ bad = true; break; }
			}
			if(bad) continue;

			file.fil = Bopen(file.entry.getPath(), "r");
			return i;
		}
		
		return -1;
	}

	@Override
	public int Lookup(int fileId, String type) {
		if(type == null) {
			System.err.println("type == null");
			return -1;
		}

		for(int i = 0; i < files.size(); i++) {
			ERESHANDLE file = files.get(i);
			if(Bstrcasecmp(type, file.fileformat) == 0) {
				if(fileId == file.fileid) {
					file.fil = Bopen(file.entry.getPath(), "r");
					return i;
				}
			} 
		}
		return -1;
	}

	@Override
	public int Read(int handle, byte[] buffer, int leng) {
		if(handle < 0) return -1;

		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return -1;
		
		return Bread(file.fil, buffer, leng);
	}

	@Override
	public byte[] Lock(int handle) {
		if(handle < 0) return null;
		
		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return null;
		
		int leng = Bfilelength(file.fil);
		byte[] buffer = new byte[leng];
		Bread(file.fil,buffer,leng);
		return buffer;
	}

	@Override
	public ByteBuffer bLock(int handle) {
		if(handle < 0) return null;
		
		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return null;
		
		ByteBuffer byteBuffer = null;
		int leng = Bfilelength(file.fil);
		byte[] buffer = new byte[leng];
		Bread(file.fil,buffer,leng);
		
		byteBuffer = BufferUtils.newByteBuffer(leng);
		byteBuffer.put(buffer);
		byteBuffer.rewind();
		
		return byteBuffer;
	}

	@Override
	public int Seek(int handle, int offset, int whence) {
		if(handle < 0) return -1;
		
		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return -1;
		
		return Blseek(file.fil, offset, whence);
	}

	@Override
	public int Size(int handle) {
		if(handle < 0) return -1;
		
		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return -1;
		
		return Bfilelength(file.fil);
	}

	@Override
	public String Name(int handle) {
		if(handle < 0) return null;
		
		ERESHANDLE file = files.get(handle);
		return file.filename;
	}

	@Override
	public int FileId(int handle) {
		if(handle < 0) return -1;
		
		ERESHANDLE file = files.get(handle);
		return file.fileid;
	}

	@Override
	public int Pos() {
		return -1;
	}

	@Override
	public int Close(int handle) {
		if(handle < 0) return -1;
		
		ERESHANDLE file = files.get(handle);
		if(file.fil < 0) return -1;
		
		Bclose(file.fil);
		file.fil = -1;
		return 0;
	}

	@Override
	public List<RESHANDLE> fList() {
		List<RESHANDLE> list = new ArrayList<RESHANDLE>();
		list.addAll(files);
		return list;
	}

	@Override
	public void Dispose() {
		files.clear();
	}
}
