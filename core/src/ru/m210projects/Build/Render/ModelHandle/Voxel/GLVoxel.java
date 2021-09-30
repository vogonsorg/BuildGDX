package ru.m210projects.Build.Render.ModelHandle.Voxel;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.palookup;

import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Types.Tile;

public abstract class GLVoxel implements GLModel {

	protected Tile skinData; // indexed texture data
	protected GLTile[] texid;
	public int xsiz, ysiz, zsiz;
	public float xpiv, ypiv, zpiv;
	protected final Color color = new Color();
	protected int flags;

	public GLVoxel(int flags) {
		this.texid = new GLTile[MAXPALOOKUPS];
		this.flags = flags;
	}

	@Override
	public Type getType() {
		return Type.Voxel;
	}

	@Override
	public boolean isRotating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getScale() {
		return 1.0f;
	}

	public GLVoxel setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		return this;
	}

	public int getSkinWidth() {
		return skinData.getWidth();
	}

	public int getSkinHeight() {
		return skinData.getHeight();
	}

	public GLTile loadSkin(GLTile dst, int dapal) {
		if (palookup[dapal] == null || dst.getPixelFormat() == PixelFormat.Pal8)
			dapal = 0;

		long startticks = System.nanoTime();
		TileData dat = new VoxelSkin(dst.getPixelFormat(), skinData, dapal);
		dst.update(dat, dapal, false);
		dst.unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
		dst.unsafeSetAnisotropicFilter(1, true);
		texid[dapal] = dst;
		long etime = System.nanoTime() - startticks;
		System.out.println("Load voxskin: p" + dapal + "... " + (etime / 1000000.0f) + " ms");

		return dst;
	}

	public GLTile getSkin(PixelFormat fmt, int pal) {
		if (palookup[pal] == null || fmt == PixelFormat.Pal8)
			return texid[0];
		return texid[pal];
	}

	@Override
	public Iterator<GLTile[]> getSkins() {
		Iterator<GLTile[]> it = new Iterator<GLTile[]>() {
			private GLTile[] current = texid;

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public GLTile[] next() {
				GLTile[] out = current;
				current = null;
				return out;
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
