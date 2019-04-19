package ru.m210projects.Build.Types;

//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

import java.nio.ByteBuffer;

import static com.badlogic.gdx.graphics.GL20.GL_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static ru.m210projects.Build.Engine.pow2char;
import static ru.m210projects.Build.Engine.textfont;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.TextureHandle.BTexture;
import ru.m210projects.Build.Render.TextureHandle.Pthtyp;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;

public class TextFont extends TileFont {

	public TextFont() {
		super(FontType.Bitmap, textfont, 8, 8, 16, 16);
	}
	
	@Override
	public Pthtyp getGL(TextureCache textureCache, int col) {
		if(atlas == null)
			init(textureCache, col);
		
		return atlas;
	}

	@Override
	public Pthtyp init(TextureCache textureCache, int col) {
		// construct a 8-bit alpha-only texture for the font glyph matrix
		byte[] tbuf;
		int tptr;
		int h, i, j;
		atlas = new Pthtyp();
		atlas.glpic = new BTexture(128, 128);
		
		sizx = atlas.sizx = 128;
		sizy = atlas.sizy = 128;

		tbuf = new byte[sizx * sizy];
		ByteBuffer fbuf = BufferUtils.newByteBuffer(sizx * sizy);

		for (h = 0; h < 256; h++) {
			tptr = (h % 16) * 8 + (h / 16) * sizx * 8;
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					if ((textfont[h * 8 + i] & pow2char[7 - j]) != 0)
						tbuf[tptr + j] = (byte) 255;
				}
				tptr += sizx;
			}
		}

		fbuf.put(tbuf);
		fbuf.rewind();

		atlas.glpic.bind();
		BuildGdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, sizx, sizy, 0, GL_ALPHA, GL_UNSIGNED_BYTE, fbuf);	
		atlas.glpic.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		return atlas;
	}

}
