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

package ru.m210projects.Build.Types;

import static com.badlogic.gdx.graphics.GL20.*;

import java.nio.ByteBuffer;
import java.util.HashSet;

import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public class TileFont {

	public abstract static class TileFontData extends TileData {

		public final TextureBuffer data;
		public final int width, height;

		public TileFontData(int width, int height) {
			this.width = width;
			this.height = height;
			this.data = buildAtlas(getTmpBuffer(width * height));
		}

		public abstract TextureBuffer buildAtlas(TextureBuffer data);

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
			return true;
		}

		@Override
		public boolean isClamped() {
			return false;
		}

		@Override
		public boolean isHighTile() {
			return false;
		}
	};

	public static final HashSet<TileFont> managedFont = new HashSet<TileFont>();

	public enum FontType {
		Tilemap, Bitmap
	};

	public GLTile atlas;

	public Object ptr;
	public FontType type;
	public int charsizx;
	public int charsizy;
	public int cols, rows;
	public int sizx = -1, sizy = -1;

	public TileFont(FontType type, Object ptr, int charsizx, int charsizy, int cols, int rows) {
		this.ptr = ptr;
		this.type = type;
		this.charsizx = charsizx;
		this.charsizy = charsizy;
		this.cols = cols;
		this.rows = rows;

		this.sizx = charsizx * cols;
		this.sizy = charsizy * rows;

		managedFont.add(this);
	}

	public GLTile getGL(TextureManager textureCache, int col) {
		return textureCache.bind((Integer) ptr, col, 0, 0, 0);
	}

	public void uninit() {
		if (atlas != null) {
			atlas.delete();
			atlas = null;
		}
	}

	public void dispose() {
		uninit();
		managedFont.remove(this);
	}

}
