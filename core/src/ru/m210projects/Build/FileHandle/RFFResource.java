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

import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;

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
		4 - preload
		8 - prelock
		16 - crypted
	*/
	private class RRESHANDLE extends RESHANDLE {
		public int offset;
//		public int modified;
		public int flags;
		public int pos;
		
		public RRESHANDLE(String filename, int fileid, byte[] data) { 
			this.filename = filename;
			this.fileformat = BfileExtension(filename);
			this.fileid = fileid;
			this.buffer = data;
			this.size = data.length;
			this.paktype = DAT;
		}

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

		@Override
		public byte[] getBytes() {
			if(buffer == null) {
				buffer = new byte[size];

				if(Blseek(File, offset, SEEK_SET) == -1) {
					System.err.println("Error seeking to resource!");
				}
				if(Bread(File, buffer, size) == -1) {
					System.err.println("Error loading resource!");
				}
				if((flags & 0x10) != 0) {
					int siz = 256;
					if(size < 256)
						siz = size;
					encrypt(buffer, siz, 0);
				}
			}
			return buffer;
		}
	}

	private int File = -1;
	private boolean Crypted;
	private List<RRESHANDLE> files = new ArrayList<RRESHANDLE>();
	private byte[] readbuf = new byte[4];
	
	public RFFResource(byte[] data) throws Exception
	{
		if(data != null) {
			
			if((char)data[0] != 'R' || (char)data[1] != 'F' || (char)data[2] != 'F' || data[3] != 0x1A) 
				throw new ResourceException("RFF header corrupted");
			
			int pos = 0;
			int revision = LittleEndian.getInt(data, pos += 4);

			if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0300 )
				Crypted = true;
		    else if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0200 )
		    	Crypted = false;
		    else if( revision == 0x168f0130)
		    	throw new ResourceException("RFF alpha version is not supported!");
		    else 
		    	throw new ResourceException("Unknown RFF version: " + Integer.toHexString(revision));
		    
			int offFat = LittleEndian.getInt(data, pos += 4);
			NumFiles = LittleEndian.getInt(data, pos += 4);
			if(NumFiles != 0) {
				byte[] buffer = new byte[NumFiles * 48];
				System.arraycopy(data, offFat, buffer, 0, buffer.length);

				if(Crypted) {
					if(revision == 0x0300)
						encrypt(buffer, buffer.length, offFat);
					else if(revision == 0x0301) {
						encrypt(buffer, buffer.length, offFat + offFat * (revision & 0xFF));
					}
				}
				
				Console.Println("Found " + NumFiles + " files in packed RFF archive", 0);
				byte[] buf = new byte[48];
				for(int i = 0; i < NumFiles; i++) {
					System.arraycopy(buffer, 48 * i, buf, 0, 48);
					RRESHANDLE file = new RRESHANDLE(buf);
					file.buffer = new byte[file.size];
					System.arraycopy(data, file.offset, file.buffer, 0, file.size);
					file.paktype = DAT;
					if((file.flags & 0x10) != 0) {
						int size = 256;
						if(file.size < 256)
							size = file.size;
						encrypt(file.buffer, size, 0);
					}
					file.byteBuffer = BufferUtils.newByteBuffer(file.size);
					file.byteBuffer.put(file.buffer);
					file.byteBuffer.rewind();
					lookup.put(file.filename, files.size());
					files.add(file);
				}
			}
			
			for(int i = 0; i < NumFiles; i++) {
				RRESHANDLE file = files.get(i);
				if((file.flags & 4) != 0) //preload
					Lock(i);
				if((file.flags & 8) != 0) //prelock
					Lock(i);
			}
		} else
			throw new ResourceException("Can't load packed RFF file");
	}
	
	public RFFResource(String FileName) throws ResourceException
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

			if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0300 )
				Crypted = true;
		    else if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0200 )
		    	Crypted = false;
		    else if( revision == 0x168f0130)
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
				
				byte[] buf = new byte[48];
				for(int i = 0; i < NumFiles; i++) {
					System.arraycopy(buffer, 48 * i, buf, 0, 48);
					RRESHANDLE res = new RRESHANDLE(buf);
					if(res.size >= 0) {
						lookup.put(res.filename, files.size());
						files.add(res);
					}
					else Console.Println("Error: negative file size! " + res.filename + " size: " + res.size, OSDTEXT_RED);
				}
			}
			
			for(int i = 0; i < files.size(); i++) {
				RRESHANDLE file = files.get(i);
				if((file.flags & 4) != 0) //preload
					Lock(i);
				if((file.flags & 8) != 0) //prelock
					Lock(i);
			}
			NumFiles = files.size();
		} else
			throw new ResourceException("File not found: " + new File(FilePath + FileName).getAbsolutePath());
	}

	@Override
	public byte[] Lock(int filenum) {
		if(filenum == -1) return null;
		RRESHANDLE file = files.get(filenum);
		if(file == null) return null;
		return file.getBytes();
	}
	
	@Override
	public ByteBuffer bLock(int filenum) {
		if(filenum == -1) return null;
		
		RRESHANDLE file = files.get(filenum);
		if(file.byteBuffer == null) {
			byte[] tmp = file.getBytes();
			file.byteBuffer = BufferUtils.newByteBuffer(file.size);
			file.byteBuffer.put(tmp);
		}
		file.byteBuffer.rewind();
		return file.byteBuffer;
	}
	
	@Override
	public int Lookup(String filename) {
		if (filename != null && !filename.isEmpty()) {
			Integer out = lookup.get(toLowerCase(filename));
			if(out != null) {
				int i = out.intValue();
				files.get(i).pos = 0;
				return i;
			}
		}
	
//		for(int i = NumFiles - 1; i >= 0; i--)
//		{
//			boolean bad = false;
//			for(int j = 0; j < filename.length(); j++)
//			{
//				if (filename != null && filename.isEmpty()) break;
//				String compare = files.get(i).filename;
//				if (Compare(filename, compare, j))
//					{ bad = true; break; }
//			}
//			if(bad) continue;
//
//			files.get(i).pos = 0;
//			return i;
//			
//		}
		return -1;
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
		if(file.paktype == DAT) {
			leng = Math.min(leng, file.size-file.pos);
			System.arraycopy(file.buffer, file.pos, buffer, 0, leng);
			file.pos += leng;
			return(leng);
		}
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
	public int Read(int filenum, int len) {
		if(filenum < 0) return -1;
		
		RRESHANDLE file = files.get(filenum);
		if(len >= file.size-file.pos)
			return 0;
		
		if(file.paktype == DAT) 
			System.arraycopy(file.buffer, file.pos, readbuf, 0, len);
		else {	
			int i = file.offset+file.pos;
			int groupfilpos = Bfpos(File);
			if (i != groupfilpos) 
				Blseek(File, i, SEEK_SET);
			Bread(File,readbuf,len);
		}
		file.pos += len;
		
		//Decrypt first?
		
		if(len == 1)
			return readbuf[0] & 0xFF;
		else if(len == 2) 
			return LittleEndian.getShort(readbuf);
		else if(len == 4) 
			return LittleEndian.getInt(readbuf);

		return 0;
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
		if(file.paktype != DAT) 
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
		if(File == -1)
			return -1;
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
		if(File != -1) {
			Bclose(File);
			File = -1;
		}
	}
	
	@Override
	public int FilePos(int handle) {
		if(handle < 0) return -1;
		RRESHANDLE file = files.get(handle);
		return file.pos;
	}

	@Override
	public boolean addResource(String filename, byte[] buf, int fileid) {
		if(filename == null || buf == null) return false;
		lookup.put(filename, files.size());
		RRESHANDLE file = new RRESHANDLE(filename, fileid, buf);
		files.add(file);
		NumFiles++;
		
		return true;
	}
}
