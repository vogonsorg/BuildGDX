//This file is part of BuildGDX.
//Copyright (C) 2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.Strhandler.toLowerCase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ru.m210projects.Build.OnSceenDisplay.Console;

public class ZipGroup extends Group {

	private static byte[] readbuf = new byte[4];
	
	private ZipFile zfile;
	
	private class ZipResource extends GroupResource {
		private class ZipInputStream extends BufferedInputStream { 
			private static final int DEFAULT_BUFFER_SIZE = 8192;
			
			public ZipInputStream(InputStream in) {
				super(in, DEFAULT_BUFFER_SIZE);
			}
			
			private void reinit() throws IOException
			{
				this.in = zfile.getInputStream(entry);
				this.buf = new byte[DEFAULT_BUFFER_SIZE];
				this.pos = 0;
				this.count = 0;
			}
			
			public int seek(long offset, Whence whence) {
				synchronized(parent) {
					try {
						int opos;
						switch (whence)
				        {
				        	case End: 
				        		offset += size;	
				        	case Set: 
				        		if(offset < 0) return -1;
				        		
				        		if(isClosed()) 
									reinit();
				        		
				        		int pos = position();
				        		if(pos < offset)
				        			this.skip(offset - pos); 
				        		else if(pos > offset) {
				        			reinit();
				        			this.skip(offset); 
				        		}
				        		break;
				        	case Current: 
				        		if(offset == 0) break;
				        		opos = position();
				        		if(opos < opos + offset) {
				        			this.skip(offset);
				        		}
				        		else {
				        			reinit();
				        			this.skip(opos + offset); 
				        		}
				        		break;
				        }
						return size - this.available();
					} catch (Exception e) { e.printStackTrace(); }
					return -1;
				}
			}
			
			private boolean isClosed()
			{
				if(in == null)
					return true;
				
				try {
					return this.available() == 0;
				} catch (Exception e) { e.printStackTrace(); }
				
				return true;
			}
			
			public int position()
			{	
				synchronized(parent) {
					try {
						return size - this.available();
					} catch (Exception e) { e.printStackTrace(); }
					
					return -1;
				}
			}
					
			@Override
			public int read(byte b[]) {
				synchronized(parent) {
					return read(b, 0, b.length);
				}
		    }
			
			@Override
			public int read(byte b[], int off, int len)
			{
				synchronized(parent) {
					int var = -1;
					try {
						var = super.read(b, off, len);
					} catch (Exception e) { e.printStackTrace(); }
					return var;
				}
			}
			
			@Override
			public long skip(long n) throws IOException {
				synchronized(parent) {
					long n1 = 0, sum = 0;
					while(sum != n)
					{
						n1 = super.skip(n - n1);
						sum += n1;
					}
					
					return sum;
				}
			}
		}
		
		private ZipInputStream bis;
		private final ZipEntry entry;
		
		public ZipResource(ZipEntry entry) throws IOException {
			super(ZipGroup.this);
			this.handleName(entry.getName());
			
			String fileid = entry.getComment();
			if(fileid != null && !fileid.isEmpty()) {
				fileid = fileid.replaceAll("[^0-9]", ""); 
				this.fileid = Integer.parseInt(fileid);
			}
			
			this.entry = entry;
			this.size = (int) entry.getSize();

			if(debug && size > 0) System.out.println("\t" + filenamext + ", size: " +  size);
		}
		
		@Override
		protected void handleName(String fullname) //zips can handle folders, so we must add separators to replacer
		{
			this.filenamext = toLowerCase(fullname).replaceAll("[^a-zA-Z0-9_. /-]", "");
			if(filenamext.contains("/")) filenamext = filenamext.replace("/", File.separator);
			
			int point = filenamext.lastIndexOf('.');
			if(point != -1) {
				this.fileformat = filenamext.substring(point + 1);
				this.filename = filenamext.substring(0, point);
			} else {
				this.fileformat = "";
				this.filename = this.filenamext;
			}
		}
		
		public ZipResource open()
		{
			if(zfile == null)
			{
				Console.Println("Group is closed!", Console.OSDTEXT_RED);
				return null;
			}
			
			try {
				if(bis == null)
					this.bis = new ZipInputStream(zfile.getInputStream(entry));
				else if(buffer == null) bis.reinit();
				
				this.seek(0, Whence.Set);
				return this;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		public void close() {
			this.seek(0, Whence.Set);
			this.flush();
			try {				
				bis.close();
			} catch (Exception e) { e.printStackTrace(); }
		}

		@Override
		public int seek(long offset, Whence whence) {
			synchronized(parent) {
				if(buffer != null)
				{
					switch (whence)
			        {
			        	case Set: buffer.position((int) offset); break;
			        	case Current: buffer.position(buffer.position() + (int)offset); break;
			        	case End: buffer.position(size + (int) offset);  break;
			        }
			        return position();
				}
				
				if(zfile == null)
					return -1;
				
				return bis.seek(offset, whence);
			}
		}

		@Override
		public int read(byte[] buf, int len) {
			synchronized(parent) {
				if(position() >= size) 
					return -1;
				
				len = Math.min(len, size - position());
				
				if(buffer != null)
				{
					buffer.get(buf, 0, len);
					return len;
				}

				if(zfile == null)
					return -1;
				
				len = bis.read(buf, 0, len);
				if(len == -1)
					return -1;

				return len;
			}
		}
		
		@Override
		public int read(byte[] buf) {
			synchronized(parent) {
				return read(buf, buf.length);
			}
		}

		@Override
		public Byte readByte() {
			synchronized(parent) {
				int len = 1;
				if(len > size - position())
					return null;
				
				if(buffer != null)
					return buffer.get();
				
				if(zfile == null || bis.read(readbuf, 0, len) != len)
					return null;
				
				return readbuf[0];
			}
		}
		
		@Override
		public Short readShort() {
			synchronized(parent) {
				int len = 2;
				if(len > size - position())
					return null;
				
				if(buffer != null)
					return buffer.getShort();
				
				if(zfile == null || bis.read(readbuf, 0, len) != len)
					return null;
				
				return (short) ( ( (readbuf[1] & 0xFF) << 8 ) + ( readbuf[0] & 0xFF ) );
			}
		}
		
		@Override
		public Integer readInt() {
			synchronized(parent) {
				int len = 4;
				if(len > size - position())
					return null;
				
				if(buffer != null)
					return buffer.getInt();
				
				if(zfile == null || bis.read(readbuf, 0, len) != len)
					return null;
				
				return ( (readbuf[3] & 0xFF) << 24 ) + ( (readbuf[2] & 0xFF) << 16 ) + ( (readbuf[1] & 0xFF) << 8 ) + ( readbuf[0] & 0xFF );
			}
		}
		
		@Override
		public String readString(int len)
		{
			synchronized(parent) {
				byte[] data = new byte[len];
				if(read(data) != len)
					return null;
				
				return new String(data);
			}
		}

		@Override
		public int position() {
			synchronized(parent) {
				if(buffer != null)
					return buffer.position();
				return bis.position();
			}
		}

		@Override
		public ResourceData getData() {
			synchronized(parent) {
				if(isClosed())
					parent.open(this);
				
				if(buffer == null) {
					byte[] tmp = getBytes();
					if(tmp == null) return null;
					
					buffer = new ResourceData(tmp);
				}
	
				buffer.rewind();
				return buffer;
			}
		}
		
		@Override
		public byte[] getBytes() {
			synchronized(parent) {
				if(isClosed())
					parent.open(this);
				
				if(buffer != null)
				{
					byte[] data = new byte[buffer.capacity()];
					buffer.rewind();
					buffer.get(data);
					return data;
				}
				
				int size = this.size();
				if(size > 0 && zfile != null) {
					int opos = position();
					if(bis.seek(0, Whence.Set) == -1) {
						Console.Println("Error seeking to resource!");
						return null;
					}
	
					byte[] data = new byte[size];
					if(bis.read(data) == -1) {
						Console.Println("Error loading resource!");
						return null;
					}
					bis.seek(opos, Whence.Set);
	
					return data;
				}
				return null;
			}
		}

		@Override
		public boolean isClosed() {
			return bis == null && buffer == null;
		}
	}

	public ZipGroup(String path) throws IOException
	{
		if(path != null && !path.isEmpty()) {
			zfile = new ZipFile(path);
			ArrayList<ZipEntry> zfilelist = new ArrayList<ZipEntry>(Collections.list(zfile.entries()));
			numfiles = zfilelist.size();
			if(numfiles != 0) {
				for(int i = 0; i < numfiles; i++) {
					ZipEntry entry = zfilelist.get(i);
					ZipResource res = new ZipResource(entry);
					if(res.size > 0) {
						add(res);
					}
				}
			}
			numfiles = filelist.size();
		}
	}
	
	public void removeFolders()
	{
		List<GroupResource> list = getList();
		filelist.clear();
		lookup.clear();
		
		for(GroupResource res : list) {
			int index = res.filenamext.lastIndexOf(File.separator);
			if(index != -1)
				res.filenamext = res.filenamext.substring(index + 1);
			
			int point = res.filenamext.lastIndexOf('.');
			if(point != -1) {
				res.filename = res.filenamext.substring(0, point);
			} else res.filename = res.filenamext;
			
			this.add(res);
		}
	}

	@Override
	protected boolean open(GroupResource res) {
		ZipResource zres = (ZipResource) res;
		if(zres != null && zres.open() != null)
			return true;
		
		return false;
	}
	
	
	@Override
	public int position() {
		return 0;
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			zfile.close();
			zfile = null;
		} catch (IOException e) {}
	}

}
