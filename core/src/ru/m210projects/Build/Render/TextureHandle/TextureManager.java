// This file is part of BuildGDX.
// Copyright (C) 2017-2021  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.GL_LUMINANCE;
import static com.badlogic.gdx.graphics.GL20.GL_RGB;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.pSmallTextfont;
import static ru.m210projects.Build.Engine.pTextfont;
import static ru.m210projects.Build.Render.Types.GL10.GL_MODELVIEW;
import static ru.m210projects.Build.Render.Types.GL10.GL_RGB_SCALE;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE0;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_ENV;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Script.TextureHDInfo;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.Tile;

public abstract class TextureManager {

	protected final Engine engine;
	protected final GLTileArray cache;
	protected TextureHDInfo info;
	protected GLTile bindedTile;
	protected GLTile palette; // to shader
	protected GLTile palookups[]; // to shader
	protected int texunits = GL_TEXTURE0;
	protected ExpandTexture expand = ExpandTexture.Both;

	public enum ExpandTexture {
		Horizontal(1), Vertical(2), Both(1 | 2);

		private byte bit;

		ExpandTexture(int bit) {
			this.bit = (byte) bit;
		}

		public byte get() {
			return bit;
		}
	};

	public TextureManager(Engine engine, ExpandTexture opt) {
		this.engine = engine;
		this.cache = new GLTileArray(MAXTILES);
		this.palookups = new GLTile[MAXPALOOKUPS];
		this.expand = opt;
	}

	public void setTextureInfo(TextureHDInfo info) {
		this.info = info;
	}

	protected GLTile get(PixelFormat fmt, int dapicnum, int dapalnum, int skybox, boolean clamping, boolean alpha) {
		Hicreplctyp si = (GLSettings.useHighTile.get() && info != null) ? info.findTexture(dapicnum, dapalnum, skybox)
				: null;

		if (si == null) {
			if (skybox != 0 || dapalnum >= (MAXPALOOKUPS - RESERVEDPALS))
				return null;
		}

		GLTile tile = cache.get(dapicnum, dapalnum, clamping, skybox);
		if (si != null && tile != null && tile.hicr == null && si.skybox == null) { // GDX 29.05.2020 skybox check added
			// (if you're switching between 8bit and hrp textures, old loaded texture should
			// be disposed. Addon HRP support)
			cache.dispose(dapicnum); // old 8-bit texture
			tile = null;
		}

		boolean useMipMaps = GLSettings.textureFilter.get().mipmaps;
		if (tile != null && tile.getPixelFormat() == fmt) {
			if (tile.isInvalidated()) {
				tile.setInvalidated(false);

				TileData data = loadPic(fmt, si, dapicnum, dapalnum, clamping, alpha, skybox);
				tile.update(data, useMipMaps);
			}
		} else {
			if (tile != null)
				cache.dispose(dapicnum); // old texture

			if (si != null && dapalnum != 0 && info.findTexture(dapicnum, 0, skybox) == si
					&& (tile = cache.get(dapicnum, 0, clamping, skybox)) != null)
				return tile;

			TileData data = loadPic(fmt, si, dapicnum, dapalnum, clamping, alpha, skybox);
			if (data == null)
				return null;

			tile = allocTile(data, si, dapicnum, dapalnum, skybox, alpha, useMipMaps);
		}

		if (GLInfo.multisample != 0 && dapalnum >= (MAXPALOOKUPS - RESERVEDPALS)) {
			BuildGdx.gl.glActiveTexture(++texunits);
			BuildGdx.gl.glEnable(GL_TEXTURE_2D);
		}

		return tile;
	}

	public GLTile bind(GLTile tile) {
		if (bindedTile == tile)
			return tile;

//		if (shader != null && tile.getPixelFormat() != PixelFormat.Pal8 && bindedTile != null
//				&& bindedTile.getPixelFormat() == PixelFormat.Pal8) {
//			BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//			shader.end();
//		}

		tile.bind();
//		if (shader != null && tile.getPixelFormat() == PixelFormat.Pal8
//				&& (bindedTile == null || bindedTile.getPixelFormat() != PixelFormat.Pal8)) {
//			BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//			shader.begin();
//		}

		bindedTile = tile;

		return tile;
	}

	/**
	 *
	 * @param tilenum
	 * @param pal
	 * @param shade
	 * @param skybox
	 * @param method  0: solid, 1: masked(255 is transparent), 2: transluscent #1,
	 *                3: transluscent #2, 4: it's a sprite, so wraparound isn't
	 *                needed
	 * @return GLTile
	 */
	public GLTile bind(PixelFormat fmt, int tilenum, int pal, int shade, int skybox, int method) {
		GLTile tile = get(fmt, tilenum, pal, skybox, clampingMode(method), alphaMode(method));
		if (tile == null)
			return null;

		bind(tile);
		setTextureParameters(tile, tilenum, pal, shade, skybox, method);
		return tile;
	}

	public abstract void setTextureParameters(GLTile tile, int tilenum, int pal, int shade, int skybox, int method);

	public void precache(PixelFormat fmt, int dapicnum, int dapalnum, boolean clamped) {
		get(fmt, dapicnum, dapalnum, 0, clamped, true);
	}

	public int getTextureUnits() {
		return texunits;
	}

	public void unbind() {
		if (GLInfo.multisample == 0)
			return;

		while (texunits >= GL_TEXTURE0) {
			BuildGdx.gl.glActiveTexture(texunits);
			BuildGdx.gl.glMatrixMode(GL_TEXTURE);
			BuildGdx.gl.glLoadIdentity();
			BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
			if (texunits > GL_TEXTURE0) {
				BuildGdx.gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
				BuildGdx.gl.glDisable(GL_TEXTURE_2D);
			}
			texunits--;
		}
		texunits = GL_TEXTURE0;
	}

	protected TileData loadPic(PixelFormat fmt, Hicreplctyp hicr, int dapicnum, int dapalnum, boolean clamping,
			boolean alpha, int skybox) {

		// System.err.println("loadPic " + dapicnum + " " + dapalnum + " clamping: " +
		// clamping);
		if (hicr != null) {
			String fn = checkResource(hicr, dapicnum, skybox);
			byte[] data = BuildGdx.cache.getBytes(fn, 0);
			if (data != null) {
				try {
					return new PixmapTileData(new Pixmap(data, 0, data.length), clamping, expand.get());
				} catch (Throwable t) {
					t.printStackTrace();
					if (skybox != 0)
						return null;
				}
			}
		}

		if (fmt == PixelFormat.Pal8)
			return new IndexedTileData(engine.getTile(dapicnum), clamping, alpha, expand.get());
		return new RGBTileData(engine.getTile(dapicnum), dapalnum, clamping, alpha, expand.get());
	}

	protected String checkResource(Hicreplctyp hicr, int dapic, int facen) {
		if (hicr == null)
			return null;

		String fn = null;
		if (facen > 0) {
			if (hicr.skybox == null || facen > 6 || hicr.skybox.face[facen - 1] == null)
				return null;

			fn = hicr.skybox.face[facen - 1];
		} else
			fn = hicr.filename;

		if (!BuildGdx.cache.contains(fn, 0)) {
			Console.Print("Hightile[" + dapic + "]: File \"" + fn + "\" not found");
			if (facen > 0)
				hicr.skybox.ignore = 1;
			else
				hicr.ignore = 1;
			return null;
		}

		return fn;
	}

	protected final Color polyColor = new Color();

	public Color getshadefactor(int shade, int method) {
		float fshade = min(max(shade * 1.04f, 0), numshades);
		float f = (numshades - fshade) / numshades;

		polyColor.r = polyColor.g = polyColor.b = f;

		switch (method & 3) {
		default:
		case 0:
		case 1:
			polyColor.a = 1.0f;
			break;
		case 2:
			polyColor.a = TRANSLUSCENT1;
			break;
		case 3:
			polyColor.a = TRANSLUSCENT2;
			break;
		}

		return polyColor;
	}

	public GLTile newTile(TileData pic, int palnum, boolean useMipMaps) {
		return new GLTile(pic, palnum, useMipMaps);
	}

	protected GLTile allocTile(TileData data, Hicreplctyp si, int dapicnum, int dapalnum, int skybox, boolean alpha,
			boolean useMipMaps) {
		GLTile tile = newTile(data, data.isHighTile() ? si.palnum : dapalnum, useMipMaps);
		if (data.isHighTile()) {
			tile.setHighTile(si);
			tile.setHasAlpha(alpha);
			tile.setSkyboxFace(skybox);

			if (skybox > 0) {
				tile.scalex = tile.getWidth() / 64.0f;
				tile.scaley = tile.getHeight() / 64.0f;
			} else {
				Tile pic = engine.getTile(dapicnum);
				if (data instanceof PixmapTileData) {
					tile.width = ((PixmapTileData) data).getTileWidth();
					tile.height = ((PixmapTileData) data).getTileHeight();
				}
				int width = tile.getWidth();
				int height = tile.getHeight();

				tile.scalex = width / ((float) pic.getWidth());
				tile.scaley = height / ((float) pic.getHeight());
			}
		}
		data.dispose();

		cache.add(tile, dapicnum);
		return tile;
	}

	public void setFilter(GLFilter filter) {
		int anisotropy = GLSettings.textureAnisotropy.get();
		for (int i = MAXTILES - 1; i >= 0; i--) {
			cache.setFilter(i, filter, anisotropy);
		}
	}

	public void invalidate(int dapicnum, int dapalnum, boolean clamped) {
		GLTile tile = cache.get(dapicnum, dapalnum, clamped, 0);
		if (tile == null)
			return;

		if (!tile.isHighTile())
			tile.setInvalidated(true);
	}

	public void invalidateall() {
		for (int j = MAXTILES - 1; j >= 0; j--)
			cache.invalidate(j);
	}

	public boolean clampingMode(int dameth) {
		return ((dameth & 4) >> 2) == 1;
	}

	public boolean alphaMode(int dameth) {
		return (dameth & 256) == 0;
	}

	public void uninit() {
		Console.Println("TextureCache uninited!", Console.OSDTEXT_RED);

		for (int i = MAXTILES - 1; i >= 0; i--) {
			cache.dispose(i);
		}

		// GLAtlas dispose
		pTextfont.uninit();
		pSmallTextfont.uninit();
	}

	// Indexed texture params and methods

	private abstract class ShaderData extends DummyTileData {

		public ShaderData(byte[] buf, int w, int h, int bytes) {
			super(w, h);
			data.clear();
			data.putBytes(buf, 0, buf.length);
		}

		@Override
		public int getGLInternalFormat() {
			return GL_RGB;
		}

		@Override
		public int getGLFormat() {
			return GL_RGB;
		}

		@Override
		public PixelFormat getPixelFormat() {
			return PixelFormat.Rgb;
		}

		@Override
		public boolean hasAlpha() {
			return false;
		}
	}

	private class PaletteData extends ShaderData {
		public PaletteData(byte[] data) {
			super(data, 256, 1, 3);
		}
	}

	private class LookupData extends ShaderData {
		public LookupData(byte[] data) {
			super(data, 256, 64, 1);
		}

		@Override
		public int getGLFormat() {
			return GL_LUMINANCE;
		}
	}

	public GLTile getPalette() {
		return palette;
	}

	public GLTile getPalookup(int pal) {
		if (palookups[pal] == null || palookups[pal].isInvalidated()) {
			if (Engine.palookup[pal] == null) {
				return palookups[0];
			}

			TileData dat = new LookupData(Engine.palookup[pal]);
			if (palookups[pal] != null) {
				palookups[pal].setInvalidated(false);
				palookups[pal].update(dat, false);
			} else
				palookups[pal] = newTile(dat, 0, false);

			palookups[pal].unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
		}

		return palookups[pal];
	}

	public void disposePalette() {
		palette.dispose();
		palette = null;
		for (int i = 0; i < MAXPALOOKUPS; i++) {
			if (palookups[i] != null) {
				palookups[i].dispose();
				palookups[i] = null;
			}
		}

	}

	public void changePalette(byte[] pal) {
		TileData dat = new PaletteData(pal);

		if (palette != null)
			palette.update(dat, false);
		else
			palette = newTile(dat, 0, false);

		palette.unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
	}

	public void invalidatepalookup(int pal) {
		if (palookups[pal] != null)
			palookups[pal].setInvalidated(true);
	}

	public GLTile getLastBinded() {
		return bindedTile;
	}

	public boolean isUseShader(int dapic) {
		GLTile tile = cache.get(dapic);
		if (tile != null && tile.getPixelFormat() == PixelFormat.Pal8)
			return true;
		return false;
	}
}
