package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;

import com.badlogic.gdx.utils.BufferUtils;

public class RFFResource extends IResource {
	
	/*
	flags 
		4 - seq
		16 - crypted
	*/
	private class RRESHANDLE extends RESHANDLE {
		public int offset;
//		public int modified;
		public int flags;
		public int pos;

		public RRESHANDLE(byte[] data) {
			offset = LittleEndian.getInt(data, 16); 
			size = LittleEndian.getInt(data, 20); 
//			modified = LittleEndian.getInt(data, 28); 
			flags = data[32] & 0xFF; 
			fileformat =  toLowerCase(new String(data, 33, 3));
			fileformat = fileformat.replaceAll("[^a-zA-Z0-9_-]", ""); 
			filename = new String(data, 36, 8);
			filename = filename.replaceAll("[^a-zA-Z0-9_-]", "");
			filename += "." + fileformat;
			filename = toLowerCase(filename);
			fileid = LittleEndian.getInt(data, 44);
			paktype = RFF;
//			System.out.println(filename + " " + fileformat + " " + fileid);
		}
	}

	private int File = -1;
	private boolean Crypted;
	private int NumFiles;
	private List<RRESHANDLE> files = new ArrayList<RRESHANDLE>();

	public RFFResource(String FileName) throws Exception
	{
		if(FileName != null && !FileName.isEmpty() && (File = Bopen(FileName, "r")) != -1) {
			if(Bfilelength(File) == -1) 
				throw new ResourceException("ERROR: nFileLength == -1");

			byte header[] = new byte[4];
			Bread(File, header, 4);
			if((char)header[0] != 'R' || (char)header[1] != 'F' || (char)header[2] != 'F' || header[3] != 0x1A) {
				throw new ResourceException("RFF header corrupted");
			}
			
			int revision = Bread(File, 4);
			
			//100 - 768
			//101 - 769
			//121 - 769
			//share - 66048
			//share111 - 769
			//alpha - 378470704

			if ( (revision & 0xFF00) == 0x0300 )
				Crypted = true;
		    else if ( (revision & 0xFF00) == 0x0200 )
		    	Crypted = false;
		    else if( (revision & 0x168f0130) != 0)
		    	throw new ResourceException("RFF alpha version is not supported!");
		    else 
		    	throw new ResourceException("Unknown RFF version: " + Integer.toHexString(revision));
		    
			int offFat = Bread(File, 4);
			NumFiles = Bread(File, 4);
			
			if(NumFiles != 0) {
				byte[] buffer = new byte[NumFiles * 48];

				if(Blseek(File, offFat, SEEK_SET) == -1)
					System.err.println("r == -1");
				
				if(Bread(File, buffer, buffer.length) == -1)
					throw new ResourceException("RFF dictionary corrupted");
				
				if(Crypted) {
					if(revision == 0x0300)
						encrypt(buffer, buffer.length, offFat);
					else if(revision == 0x0301) {
						encrypt(buffer, buffer.length, offFat + offFat * (revision & 0xFF));
					}
				}
				
				Console.Println("Found " + NumFiles + " files in " + FileName + " archive", 0);
				byte[] buf = new byte[48];
				for(int i = 0; i < NumFiles; i++) {
					for(int j = 0; j < 48; j++) 
						buf[j] = buffer[48 * i + j];
					files.add(new RRESHANDLE(buf));
				}
			}
			
			for(int i = 0; i < NumFiles; i++) {
				RRESHANDLE file = files.get(i);
				if((file.flags & 4) != 0) //SEQ Files
					Lock(i);
				if((file.flags & 8) != 0) 
					Lock(i);
			}
		} else
			throw new ResourceException("File not found: " + new File(FilePath + FileName).getAbsolutePath());
	}

	@Override
	public byte[] Lock(int filenum) {
		if(filenum == -1) return null;
		
		RRESHANDLE file = files.get(filenum);
		if(file.buffer == null) {
			file.buffer = new byte[file.size];

			if(Blseek(File, file.offset, SEEK_SET) == -1) {
				System.err.println("Error seeking to resource!");
			}
			if(Bread(File, file.buffer, file.size) == -1) {
				System.err.println("Error loading resource!");
			}
			if((file.flags & 0x10) != 0) {
				int size = 256;
				if(file.size < 256)
					size = file.size;
				encrypt(file.buffer, size, 0);
			}
		}
		return file.buffer;
	}
	
	@Override
	public ByteBuffer bLock(int filenum) {
		if(filenum == -1) return null;
		
		RRESHANDLE file = files.get(filenum);
		if(file.byteBuffer == null) {
			byte[] tmp = new byte[file.size];

			if(Blseek(File, file.offset, SEEK_SET) == -1) {
				System.err.println("Error seeking to resource!");
			}
			if(Bread(File, tmp, file.size) == -1) {
				System.err.println("Error loading resource!");
			}
			if((file.flags & 0x10) != 0) {
				int size = 256;
				if(file.size < 256)
					size = file.size;
				encrypt(tmp, size, 0);
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
		
		/*
		for(int i = 0; i < NumFiles; i++) {
			RRESHANDLE file = files.get(i);
			
			boolean bad = false;
			String compare = file.filename;
			int ext = 0;
			for(int j = 0; j < filename.length(); j++)
			{
				if (filename != null && filename.isEmpty()) break;
				
				if (toupperlookup[filename.codePointAt(j)] == '.') //extension
				{
					if(compare.length() > j) { bad = true; break; }

					compare = file.fileformat;
					ext = j+1;
					continue;
				}

				if ((j-ext) >= compare.length() || toupperlookup[filename.codePointAt(j)] 
					!= toupperlookup[compare.codePointAt(j-ext)])
				{ bad = true; break; }
			}
			if(bad) continue;

			file.pos = 0;
			return i;
		}
		
		return -1;
		*/
	}

	@Override
	public int Size(int filenum) {
		if(filenum < 0) return -1;
		
		RRESHANDLE file = files.get(filenum);
		return file.size;
	}

	@Override
	public int Lookup(int fileId, String type) {
		if(type == null) {
			System.err.println("type == null");
			return -1;
		}

		type =  toLowerCase(type);
		for(int i = 0; i < NumFiles; i++) {
			RRESHANDLE file = files.get(i);
			if(type.equals(file.fileformat)) {
				if(fileId == file.fileid)  {
					file.pos = 0;
					return i;
				}
			} 
		}
		return -1;
	}
	
	@Override
	public int Read(int filenum, byte[] buffer, int leng)
	{
		if(filenum < 0) return -1;
		
		RRESHANDLE file = files.get(filenum);
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
		
		RRESHANDLE file = files.get(handle);
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
	
	private void encrypt(byte[] buffer, int size, int offFat) {
		int i = 0;
		while(i < size) {
			int key = offFat++ >> 1;
	    	int data = buffer[i++];

	    	buffer[i-1] = (byte) (data ^ key);
		}
	}

	@Override
	public String Name(int handle) {
		if(handle < 0) return null;
		RRESHANDLE file = files.get(handle);
		return file.filename;
	}

	@Override
	public int FileId(int handle) {
		if(handle < 0) return -1;
		
		RRESHANDLE file = files.get(handle);
		return file.fileid;
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
		File = -1;
	}
}
