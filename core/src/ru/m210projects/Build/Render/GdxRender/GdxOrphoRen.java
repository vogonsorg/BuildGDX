// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Render.GdxRender;

import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_LUMINANCE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Engine.pow2char;
import static ru.m210projects.Build.Engine.smalltextfont;
import static ru.m210projects.Build.Engine.textfont;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Render.Types.GL10.GL_ALPHA_TEST;
import static ru.m210projects.Build.Render.Types.GL10.GL_INTENSITY;
import static ru.m210projects.Build.Settings.GLSettings.glfiltermodes;
import static ru.m210projects.Build.Strhandler.Bstrlen;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.OrphoRenderer;
import ru.m210projects.Build.Render.Renderer.Transparent;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.Tile.AnimType;

public class GdxOrphoRen extends OrphoRenderer {

	protected final TextureManager textureCache;
	protected final GdxBatch batch;
	protected final ShapeRenderer shape;
	protected GLTile textAtlas;

	public GdxOrphoRen(Engine engine, TextureManager textureCache) {
		super(engine);
		this.textureCache = textureCache;

		this.batch = new GdxBatch();
		this.shape = new ShapeRenderer();
	}

	@Override
	public void init() {
		if(textAtlas == null) {
			// construct a 256x128 8-bit alpha-only texture for the font glyph matrix
//			UnsafeBuffer ub = getTmpBuffer();
			ByteBuffer ub = BufferUtils.newByteBuffer(256 * 128);

			int tptr, i, j;
			for (int h = 0; h < 256; h++) {
				tptr = (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 0; i < 8; i++) {
					for (j = 0; j < 8; j++) {
						if ((textfont[h * 8 + i] & pow2char[7 - j]) != 0)
							ub.put(tptr + j, (byte) (0xFF));
					}
					tptr += 256;
				}
			}

			for (int h = 0; h < 256; h++) {
				tptr = 256 * 64 + (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 1; i < 7; i++) {
					for (j = 2; j < 6; j++) {
						if ((smalltextfont[h * 8 + i] & pow2char[7 - j]) != 0)
							ub.put(tptr + j - 2, (byte) (0xFF));
					}
					tptr += 256;
				}
			}

			textAtlas = new GLTile(256, 128);
			textAtlas.bind();

			int internalformat = GL_INTENSITY; // ... and GL_LUMINANCE doesn't work in GL3.0
			int format = GL_LUMINANCE;
			BuildGdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, internalformat, textAtlas.getWidth(), textAtlas.getHeight(), 0, format, GL_UNSIGNED_BYTE, ub);
			textAtlas.setupTextureFilter(glfiltermodes[0], 1);
		}
	}

	@Override
	public void uninit() {
		if (textAtlas != null)
			textAtlas.delete();
		textAtlas = null;
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		BuildGdx.gl.glDisable(GL_ALPHA_TEST);
		BuildGdx.gl.glDepthMask(false); // disable writing to the z-buffer
		BuildGdx.gl.glEnable(GL_BLEND);

		int xsiz = (fontsize != 0 ? 4 : 8);
		int ysiz = (fontsize != 0 ? 6 : 8);

		xpos <<= 16;
		ypos <<= 16;

		bindBatch();
		if (backcol >= 0) {
			batch.setColor(curpalette.getRed(backcol) / 255.0f, curpalette.getGreen(backcol) / 255.0f,curpalette.getBlue(backcol) / 255.0f, 1.0f);
//			batch.draw(textAtlas, xpos, ypos, Bstrlen(text) * xsiz, 8, 0, 0, 64, 0, 1, 1, 0, (int) (scale * 65536), 8, 0, 0, xdim - 1, ydim - 1);
		}

		int oxpos = xpos;
		int c = 0, line = 0, yoffs;
		batch.setColor(curpalette.getRed(col) / 255.0f, curpalette.getGreen(col) / 255.0f,curpalette.getBlue(col) / 255.0f, 1.0f);
		while (c < text.length && text[c] != '\0') {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * (8 >> fontsize));
			}
			if (text[c] == '\r') text[c] = 0;
			yoffs = (int) (scale * line * (8 >> fontsize));

//			batch.draw(textAtlas, xpos, ypos, xsiz, ysiz,
//				0, -yoffs, (text[c] % 32) * 8, (text[c] / 32) * 8 + (fontsize * 64), xsiz, ysiz,
//				0, (int) (scale * 65536), 8, 0, 0, xdim - 1, ydim - 1);

			xpos += scale * (xsiz << 16);
			c++;
		}
		BuildGdx.gl.glDepthMask(true); // re-enable writing to the z-buffer
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		col = palookup[0][col] & 0xFF;

		shape.begin(ShapeType.Line);
		shape.setColor(curpalette.getRed(col), curpalette.getGreen(col), curpalette.getBlue(col), 255);
		shape.line(x1 / 4096.0f, ydim - y1 / 4096.0f, x2 / 4096.0f, ydim - y2 / 4096.0f);
		shape.end();
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {

		if (picnum >= MAXTILES) return;
		if ((cx1 > cx2) || (cy1 > cy2)) return;
		if (z <= 16) return;

		Tile pic = engine.getTile(picnum);
		if (pic.getType() != AnimType.None)
			picnum += engine.animateoffs(picnum, 0xc000);

		if (!pic.hasSize())
			return;

		int method = 0;
		if ((dastat & 64) == 0) {
			method = 1;
			if ((dastat & 1) != 0) {
				if ((dastat & 32) == 0)
					method = 2;
				else
					method = 3;
			}
		} else
			method |= 256; // non-transparent 255 color
		method |= 4; // Use OpenGL clamping - dorotatesprite never repeats

		int xsiz = pic.getWidth();
		int ysiz = pic.getHeight();

		int xoff = 0, yoff = 0;
		if ((dastat & 16) == 0) {
			xoff = pic.getOffsetX() + (xsiz >> 1);
			yoff = pic.getOffsetY() + (ysiz >> 1);
		}

		if ((dastat & 4) != 0)
			yoff = ysiz - yoff;

		if (picnum >= MAXTILES) picnum = 0;
		if (palookup[dapalnum & 0xFF] == null)
			dapalnum = 0;

		engine.setgotpic(picnum);
		if (pic.data == null)
			engine.loadtile(picnum);

		GLTile pth = textureCache.bind(picnum, dapalnum, dashade, 0, method);
		if(pth == null) return;

		if (((method & 3) == 0))
			batch.disableBlending();
		else batch.enableBlending();

		float shade = (numshades - min(max(dashade, 0), numshades)) / (float) numshades;
		float alpha = 1.0f;
		switch (method & 3) {
			case 2: alpha = TRANSLUSCENT1; break;
			case 3: alpha = TRANSLUSCENT2; break;
		}

		bindBatch();
		batch.setColor(shade, shade, shade, alpha);
//		batch.draw(pth.glpic, sx, sy, xsiz, ysiz, xoff, yoff, a, z, dastat, cx1, cy1, cx2, cy2);
	}

	private void bindBatch()
	{
		if(!batch.isDrawing())
			batch.begin();
	}

	@Override
	public void nextpage() {
		if(batch.isDrawing())
			batch.end();
	}

}
