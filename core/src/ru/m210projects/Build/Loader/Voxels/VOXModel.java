// VOXModel by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import static com.badlogic.gdx.graphics.GL20.GL_LUMINANCE;
import static com.badlogic.gdx.graphics.GL20.GL_RGBA;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Settings.GLSettings.glfiltermodes;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Iterator;

import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public class VOXModel extends Model {

	public GLTile[] texid;

	private static class VoxTileData extends TileData {
		public final TextureBuffer data;
		public final int width, height;
		public final boolean indexed;

		public VoxTileData(VOXModel vox, int dapal, boolean indexed) {
			this.width = vox.mytexx;
			this.height = vox.mytexy;
			this.indexed = indexed;

			TextureBuffer buffer = getTmpBuffer(indexed ? (width * height) : (width * height * 4));
			buffer.clear();

			if (!indexed) {
				int wpptr, wp, dacol;
				for (int x, y = 0; y < height; y++) {
					wpptr = y * width;
					for (x = 0; x < width; x++, wpptr++) {
						wp = wpptr << 2;
						dacol = vox.mytex[wpptr] & 0xFF;
						dacol = palookup[dapal][dacol] & 0xFF;

						buffer.putInt(wp, curpalette.getRGB(dacol) + (255 << 24));
					}
				}
			} else
				buffer.putBytes(vox.mytex, 0, width * height);
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
			return indexed ? GL_LUMINANCE : GL_RGBA;
		}

		@Override
		public PixelFormat getPixelFormat() {
			return indexed ? PixelFormat.Pal8 : PixelFormat.Rgba;
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
			for (int i = 0; i < 4; i++)
				v[i] = new vert_t();
		}
	}

	public class vert_t {
		public int x, y, z, u, v;
	}

	public void initQuads() {
		for (int vx = 0; vx < qcnt; vx++)
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

		if (texid[dapal] != null)
			return texid[dapal];

//		long startticks = System.currentTimeMillis();
		VoxTileData dat = new VoxTileData(this, dapal, bit8texture);
		texid[dapal] = new GLTile(dat, dapal, false);
		texid[dapal].setupTextureFilter(glfiltermodes[0], 1);
//		long etime = System.currentTimeMillis()-startticks;
//		System.out.println("Load voxskin: p" + dapal +  "... " + etime + " ms");

		return texid[dapal];
	}

	public GLTile bindSkin(TextureManager textureCache, int pal, int shade, float alpha) {
		int dapal = pal;
		if (textureCache.getShader() != null)
			pal = 0;

		if (texid[pal] == null)
			loadskin(pal, textureCache.getShader() != null);

		GLTile tile = textureCache.bind(texid[pal]);
		if (tile.isRequireShader()) {
			textureCache.getShader().setTextureParams(dapal, shade);
			textureCache.getShader().setDrawLastIndex(true);
			textureCache.getShader().setTransparent(alpha);
		}

		return tile;
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
		for (int i = 0; i < texid.length; i++) {
			GLTile tex = texid[i];
			if (tex == null)
				continue;

			tex.delete();
			texid[i] = null;
		}
	}
}
