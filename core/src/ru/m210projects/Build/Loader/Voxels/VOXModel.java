// VOXModel by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import static com.badlogic.gdx.graphics.GL20.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.UseBloodPal;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.globalshade;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Settings.GLSettings.glfiltermodes;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Iterator;

import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public class VOXModel extends Model {

	public GLTile[] texid;

	private static class VoxTileData extends TileData {
		public final TextureBuffer data;
		public final int width, height;

		public VoxTileData(VOXModel vox, int dapal, boolean indexed) {
			this.width = vox.mytexx;
			this.height = vox.mytexy;

			TextureBuffer buffer = getTmpBuffer(width * height * 4);
			buffer.clear();

			int wpptr, wp, dacol;
			for (int x, y = 0; y < height; y++) {
				wpptr = y * width;
				for (x = 0; x < width; x++, wpptr++) {
					wp = wpptr << 2;

					if(indexed) {
						buffer.put(wp, vox.mytex[wpptr]);
					} else {
						dacol = vox.mytex[wpptr] & 0xFF;
//						if(UseBloodPal && dapal == 1) //Blood's pal 1
//						{
//							int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
//							dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
//						} else
							dacol = palookup[dapal][dacol] & 0xFF;

//						dacol *= 3;
//						if (gammabrightness == 0) { XXX
//							r = curpalette[dacol + 0] & 0xFF;
//							g = curpalette[dacol + 1] & 0xFF;
//							b = curpalette[dacol + 2] & 0xFF;
//						}
//						else {
//							byte[] brighttable = britable[curbrightness];
//							r = brighttable[curpalette[dacol + 0] & 0xFF] & 0xFF;
//							g = brighttable[curpalette[dacol + 1] & 0xFF] & 0xFF;
//							b = brighttable[curpalette[dacol + 2] & 0xFF] & 0xFF;
//						}
//						rgb = ( 255 << 24 ) + ( b << 16 ) + ( g << 8 ) + ( r << 0 );
						buffer.putInt(wp, curpalette.getRGB(dacol) + ( 255 << 24 ));
					}
				}
			}

			this.data = buffer;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public ByteBuffer getPixels() {
			return data.getBuffer();
		}

		@Override
		public int getGLType() {
			return GL_UNSIGNED_BYTE;
		}

		@Override
		public int getGLInternalFormat() {
			return GL_RGBA;
		}

		@Override
		public int getGLFormat() {
			return GL_RGBA;
		}

		@Override
		public PixelFormat getPixelFormat() {
			return PixelFormat.Rgba;
		}

		@Override
		public boolean hasAlpha() {
			return false;
		}

		@Override
		public boolean isClamped() {
			return true;
		}

		@Override
		public boolean isHighTile() {
			return false;
		}
	};

	public class voxrect_t {
		public vert_t[] v = new vert_t[4];
		public voxrect_t() {
			for(int i = 0; i < 4; i++)
				v[i] = new vert_t();
		}
	}

	public class vert_t { public int x, y, z, u, v; }

	public void initQuads()
	{
		for(int vx = 0; vx < qcnt; vx++)
			quad[vx] = new voxrect_t();
	}
	public voxrect_t[] quad;
	public int qcnt, qfacind[] = new int[7];
	public int mytexx, mytexy;
	public byte[] mytex;
	public int xsiz, ysiz, zsiz;
	public float xpiv, ypiv, zpiv;
	public int is8bit;
	public FloatBuffer uv;

	public GLTile loadskin(int dapal, boolean bit8texture) {
		if (palookup[dapal] == null || bit8texture)
			dapal = 0;

		if(texid[dapal] != null)
			return texid[dapal];

//		long startticks = System.currentTimeMillis();
		VoxTileData dat = new VoxTileData(this, dapal, bit8texture);
		texid[dapal] = new GLTile(dat, dapal, false);
		texid[dapal].setupTextureFilter(glfiltermodes[0], 1);
//		long etime = System.currentTimeMillis()-startticks;
//		System.out.println("Load voxskin: p" + dapal +  "... " + etime + " ms");

		return texid[dapal];
	}

	@Override
	public Iterator<GLTile[]> getSkins() {
		Iterator<GLTile[]> it = new Iterator<GLTile[]>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public GLTile[] next() {
				return texid;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	@Override
	public void clearSkins() {
		for(int i = 0; i < texid.length; i++)
		{
			GLTile tex = texid[i];
			if(tex == null) continue;

			tex.delete();
			texid[i] = null;
		}
	}
}
