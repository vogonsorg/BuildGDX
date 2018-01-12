package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.Strhandler.Bstrcmp;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;

public class GRPResource extends IResource {
	
	private int File = -1;
	private int NumFiles;

	private List<GRESHANDLE> files = new ArrayList<GRESHANDLE>();

	private class GRESHANDLE extends RESHANDLE {
		public int offset;
		public int pos;

		public GRESHANDLE(byte[] data, int offset) {
			filename = new String(data, 0, 12);
			this.fileformat = toLowerCase(filename.substring(filename.lastIndexOf('.') + 1));
			size = LittleEndian.getInt(data, 12);
			this.offset = offset;
//			System.out.println(filename+ " " + offset + " size: " +  size);
		}
	}
	
	public GRPResource(String FileName) throws Exception {
		if(FileName != null && !FileName.isEmpty() && (File = Bopen(FileName, "r")) != -1) 
		{
			if(Bfilelength(File) == -1) 
				throw new ResourceException("ERROR: nFileLength == -1");
			
			byte[] buf = new byte[16];
			Bread(File, buf, 16);
			String strbuf = new String(buf, 0, 12);
			if(Bstrcmp(strbuf, "KenSilverman") != 0) {
				throw new ResourceException("GRP header corrupted");
			}
			
			NumFiles = LittleEndian.getInt(buf, 12);
			int HeaderSize = (NumFiles + 1)<<4;
			
			if(NumFiles != 0) {
				byte[] buffer = new byte[NumFiles<<4];
				if(Bread(File, buffer, buffer.length) == -1) 
					throw new ResourceException("GRP dictionary corrupted");
				
				Console.Println("Found " + NumFiles + " files in " + FileName + " archive", 0);

				int offset = HeaderSize;
				for(int i = 0; i < NumFiles; i++) {
					for(int j = 0; j < 16; j++) 
						buf[j] = buffer[(i<<4) + j];
					
					GRESHANDLE file = new GRESHANDLE(buf, offset);
					offset += file.size;
					files.add(file);
				}
			}
		} else
			throw new ResourceException("File not found: " + new File(FilePath + FileName).getAbsolutePath());
	}
	
	@Override
	public int Lookup(String filename) {
		for(int i = NumFiles - 1; i >= 0; i--)
		{
			boolean bad = false;
			for(int j = 0; j < filename.length(); j++)
			{
				if (filename != null && filename.isEmpty()) break;
				String compare = files.get(i).filename;
				if (j >= compare.length() || toupperlookup[filename.codePointAt(j)] != toupperlookup[compare.codePointAt(j)])
				{ bad = true; break; }
			}
			if(bad) continue;

			files.get(i).pos = 0;
			return i;
		}
		return -1;
	}
	
	@Override
	public byte[] Lock(int filenum)
	{
		if(filenum == -1) return null;
		GRESHANDLE file = files.get(filenum);
		
		if(file.buffer == null) {
			file.buffer = new byte[file.size];
			if(Blseek(File, file.offset, SEEK_SET) == -1) {
				System.err.println("Error seeking to resource!");
			}
			if(Bread(File, file.buffer, file.size) == -1) {
				System.err.println("Error loading resource!");
			}
		}
		return file.buffer;
	}
	
	@Override
	public ByteBuffer bLock(int filenum) {
		
		if(filenum == -1) return null;
		
		GRESHANDLE file = files.get(filenum);
		if(file.byteBuffer == null) {
			byte[] tmp = new byte[file.size];

			if(Blseek(File, file.offset, SEEK_SET) == -1) {
				System.err.println("Error seeking to resource!");
			}
			if(Bread(File, tmp, file.size) == -1) {
				System.err.println("Error loading resource!");
			}
			
			file.byteBuffer = BufferUtils.newByteBuffer(file.size);
			file.byteBuffer.put(tmp);
			tmp = null;
		}
		file.byteBuffer.rewind();
		file.byteBuffer.limit(file.size);
		return file.byteBuffer;
	}
	
	@Override
	public int Read(int filenum, byte[] buffer, int leng)
	{
		if(filenum < 0) return -1;
		
		GRESHANDLE file = files.get(filenum);
		int i = file.offset+file.pos;
		int groupfilpos = Bfpos(File);
		if (i != groupfilpos) 
			Blseek(File, i, SEEK_SET);
		
		leng = Math.min(leng, file.size-file.pos);
		leng = Bread(File,buffer,leng);
	
		file.pos += leng;
		return(leng);
	}
	
	@Override
	public int Seek(int handle, int offset, int whence) {
		if(handle < 0) return -1;
		
		GRESHANDLE file = files.get(handle);
		switch (whence)
        {
        	case SEEK_SET:
        		file.pos = offset; break;
        	case SEEK_END:
        		file.pos = file.size+offset;
	            break;
        	case SEEK_CUR:
        		file.pos += offset; break;
        }
        Blseek(File, file.pos, SEEK_SET);
        return file.pos;
	}
	
	@Override
	public int Size(int filenum) {
		if(filenum < 0) return -1;
		
		GRESHANDLE file = files.get(filenum);
		return file.size;
	}

	@Override
	public int Lookup(int fileId, String type) {
//		Console.Println("GRP.Lookup(fileId, type): Not supported operation!", OSDTEXT_RED);
		return -1;
	}

	@Override
	public String Name(int handle) {
		if(handle < 0) return null;
		GRESHANDLE file = files.get(handle);
		return file.filename;
	}

	@Override
	public int FileId(int handle) {
		return -1;
	}

	@Override
	public int Pos() {
		return Bfpos(File);
	}
	
	@Override
	public int Close(int handle) {
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
		Bclose(File);
	}
}
