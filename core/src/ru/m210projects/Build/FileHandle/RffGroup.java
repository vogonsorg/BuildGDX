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

import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.m210projects.Build.FileHandle.Cache1D.PackageType;
import ru.m210projects.Build.FileHandle.Resource.Whence;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;
import ru.m210projects.Build.Types.UnsafeBuffer;

public class RffGroup extends Group {

	private static byte[] readbuf = new byte[4];
	
	private Resource file = null;
	private boolean crypted;
	
	private class RffResource extends GroupResource {
		
		public int offset;
		public byte flags;
		public int pos;

		public RffResource(byte[] data) {
			super(RffGroup.this);
			
			offset = LittleEndian.getInt(data, 16); 
			size = LittleEndian.getInt(data, 20); 
			flags = data[32]; 
			
			String fmt = new String(data, 33, 3);
			String name = new String(data, 36, 8);
			this.handleName(name + "." + fmt);
			fileid = LittleEndian.getInt(data, 44);
			
			if(debug && size > 0) System.out.println("\t" + filenamext + ", fileid: " + fileid + ", size: " +  size);
		}

		@Override
		public void flush() {
			super.flush();
			this.close();
		}
		
		@Override
		public void close() { synchronized(parent) { pos = 0; } }
		
		@Override
		public int seek(long offset, Whence whence) {
			synchronized(parent) {
				switch (whence)
		        {
		        	case Set: pos = (int) offset; break;
		        	case Current: pos += offset; break;
		        	case End: pos = size + (int) offset;  break;
		        }
				file.seek(pos, Whence.Set);
		        return pos;
			}
		}

		@Override
		public int read(byte[] buf, int len) {
			synchronized(parent) {
				if(pos >= size) 
					return -1;

				len = Math.min(len, size - pos);
				int i = offset + pos;
				int groupfilpos = file.position();
				if (i != groupfilpos) 
					file.seek(i, Whence.Set);
	
				len = file.read(buf,len);
				if(((flags & 0x10) != 0) && pos < 256) 
					encrypt(buf, 0, Math.min(256 - pos, len), pos);

				pos += len;
				return len;
			}
		}
		
		@Override
		public int read(byte[] buf, int offs, int len) {
			synchronized(parent) {
				if(pos >= size) 
					return -1;

				len = Math.min(len, size - pos);
				int i = offset + pos;
				int groupfilpos = file.position();
				if (i != groupfilpos) 
					file.seek(i, Whence.Set);
	
				len = file.read(buf, offs, len);
				if(((flags & 0x10) != 0) && pos < 256) 
					encrypt(buf, offs, Math.min(256 - pos, len), pos);

				pos += len;
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
				if(len > size - pos)
					return null;
				
				int i = offset + pos;
				int groupfilpos = file.position();
				if (i != groupfilpos) 
					file.seek(i, Whence.Set);

				if(file.read(readbuf, len) != len)
					return null;
				
				if(((flags & 0x10) != 0) && pos < 256) 
					encrypt(readbuf, 0, Math.min(256 - pos, len), pos);
				
				pos += len;
				return readbuf[0];
			}
		}
		
		@Override
		public Short readShort() {
			synchronized(parent) {
				int len = 2;
				if(len > size - pos)
					return null;
				
				int i = offset + pos;
				int groupfilpos = file.position();
				if (i != groupfilpos) 
					file.seek(i, Whence.Set);

				if(file.read(readbuf, len) != len)
					return null;
				
				if(((flags & 0x10) != 0) && pos < 256) 
					encrypt(readbuf, 0, Math.min(256 - pos, len), pos);
				
				pos += len;
				return (short) ( ( (readbuf[1] & 0xFF) << 8 ) + ( readbuf[0] & 0xFF ) );
			}
		}
		
		@Override
		public Integer readInt() {
			synchronized(parent) {
				int len = 4;
				if(len > size - pos)
					return null;
				
				int i = offset + pos;
				int groupfilpos = file.position();
				if (i != groupfilpos) 
					file.seek(i, Whence.Set);

				if(file.read(readbuf, len) != len)
					return null;
				
				if(((flags & 0x10) != 0) && pos < 256) 
					encrypt(readbuf, 0, Math.min(256 - pos, len), pos);
				
				pos += len;
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
			synchronized(parent) { return pos; }
		}

		@Override
		public ResourceData getData() {
			synchronized(parent) {
				if(buffer == null) {
					if(file.seek(offset, Whence.Set) == -1) {
						Console.Println("Error seeking to resource!");
						return null;
					}
					
					if(file instanceof FileResource) {
						ResourceData fileData = ((FileResource)file).read(size, new Runnable() {
							@Override
							public void run() {
								flush();
							}
						});
						if(fileData == null)
						{
							Console.Println("Error loading resource!");
							return null;
						}
						
						buffer = new ResourceData(fileData.getBuffer()) {
							@Override
							public byte get(int i) {
								if((flags & 0x10) != 0 && i < 256)
									return (byte) (super.get(i) ^ (i >> 1));
								return super.get(i);
							}
							
							@Override
							public short getShort(int i) {
								if((flags & 0x10) != 0) {
									byte b1 = get(i);
									byte b2 = get(i + 1);
									
									return (short) (( (b2 & 0xFF) << 8 ) + ( b1 & 0xFF ));
								} 
								
								return super.getShort(i);
							}
							
							@Override
							public int getInt(int i) {
								if((flags & 0x10) != 0) {
									byte b1 = get(i);
									byte b2 = get(i + 1);
									byte b3 = get(i + 2);
									byte b4 = get(i + 3);
									
									return ( (b4 & 0xFF) << 24 ) + ( (b3 & 0xFF) << 16 ) + ( (b2 & 0xFF) << 8 ) + ( b1 & 0xFF );
								} 
								
								return super.getInt(i);
							}
							
							@Override
							public UnsafeBuffer get(byte[] dst, int offset, int length) {
								super.get(dst, offset, length);
								
								int pos = position() - length;
								if(((flags & 0x10) != 0) && pos < 256) 
									encrypt(dst, 0, Math.min(256 - pos, length), pos);
								
								return this;
							}
							
							@Override
							public ByteBuffer getBuffer() { 
								if((flags & 0x10) != 0) {
									byte[] bytes = new byte[512];
									ByteBuffer bb = ByteBuffer.allocateDirect(size);

									int rem, len;
									while((rem = buffer.remaining()) > 0) {
										len = Math.min(512, rem);
										buffer.get(bytes, 0, len);
										bb.put(bytes, 0, len);
									}
									bb.rewind();
									
									return bb.order(ByteOrder.LITTLE_ENDIAN);
								}
								
								return super.getBuffer();
							}
						};
					} else {
						byte[] tmp = getBytes();
						if(tmp == null) return null;
						buffer = new ResourceData(tmp);
					}
				}
	
				buffer.rewind();
				return buffer;
			}
		}
		
		@Override
		public byte[] getBytes() {
			synchronized(parent) {
				int size = this.size();
				if(size > 0) {
					if(file.seek(offset, Whence.Set) == -1) {
						Console.Println("Error seeking to resource!");
						return null;
					}
	
					byte[] data = new byte[size];
					if(file.read(data, size) == -1) {
						Console.Println("Error loading resource!");
						return null;
					}
					
					if((flags & 0x10) != 0) 
						encrypt(data, 0, Math.min(256, size), 0);
					
					return data;
				}
				return null;
			}
		}

		@Override
		public boolean isClosed() {
			synchronized(parent) {
				if(file != null)
					return file.isClosed();
				return true;
			}
		}
	}
	
	public RffGroup(Resource groupFile, PackageType type) throws Exception
	{
		this.file = groupFile;
		this.type = type;
		
		if(type == PackageType.PackedRff)
			file.getData();
		
		if(file.position() != 4) {
			file.seek(0, Whence.Set);
    		file.read(readbuf);
			if((char)readbuf[0] != 'R' || (char)readbuf[1] != 'F' || (char)readbuf[2] != 'F' || readbuf[3] != 0x1A)
				throw new Exception("RFF header corrupted");
		} //else already checked
		
		int revision = file.readInt();
		
		//100 - 768
		//101 - 769
		//121 - 769
		//share - 66048
		//share111 - 769
		//alpha - 378470704
		
		if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0300 )
			crypted = true;
	    else if ( (revision & 0xFFF00000) == 0 && (revision & 0xFF00) == 0x0200 )
	    	crypted = false;
	    else if( revision == 0x168f0130)
	    	throw new Exception("RFF alpha version is not supported!");
	    else 
	    	throw new Exception("Unknown RFF version: " + Integer.toHexString(revision));

		int offFat = file.readInt();
		this.numfiles = file.readInt();
		
		if(numfiles != 0) {
			byte[] buffer = new byte[numfiles * 48];
			
			if(file.seek(offFat, Whence.Set) == -1)
				throw new Exception("r == -1");

			if(file.read(buffer, buffer.length) == -1)
				throw new Exception("RFF dictionary corrupted");
			
			if(crypted) {
				if(revision == 0x0300)
					encrypt(buffer, 0, buffer.length, offFat);
				else if(revision == 0x0301) {
					encrypt(buffer, 0, buffer.length, offFat + offFat * (revision & 0xFF));
				}
			}
			
			byte[] buf = new byte[48];
			for(int i = 0; i < numfiles; i++) {
				System.arraycopy(buffer, 48 * i, buf, 0, 48);
				RffResource res = new RffResource(buf);
				if(res.size > 0) {
					add(res);
				} else Console.Println("Error: negative file size! " + res.filename + " size: " + res.size, OSDTEXT_RED);
			}
		}
		numfiles = filelist.size();
		
		for(GroupResource gres : filelist)
		{
			RffResource res = (RffResource) gres;
			if((res.flags & 4) != 0) //preload
	    		res.getData();
			if((res.flags & 8) != 0) //prelock
				res.getData();
		}
	}

	private void encrypt(byte[] buffer, int offset, int size, int offFat) {
		int i = 0;
		while(i < size) {
			int key = offFat++ >> 1;
	    	int data = buffer[i++];

	    	buffer[offset + i - 1] = (byte) (data ^ key);
		}
	}
	
	@Override
	protected boolean open(GroupResource res) {
		if(file == null) return false;
		
		RffResource rres = (RffResource) res;
		if(rres != null) {
			rres.pos = 0;
			return true;
		}
		
		return false;
	}

	@Override
	public int position() {
		if(file != null)
			return file.position();
		
		return -1;
	}

	@Override
	public void dispose() {
		super.dispose();
		if(file != null && !file.isClosed())
			file.close();
		file = null;
	}

}
