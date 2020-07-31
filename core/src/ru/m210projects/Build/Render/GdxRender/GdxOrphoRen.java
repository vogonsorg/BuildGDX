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
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.globalpal;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.pSmallTextfont;
import static ru.m210projects.Build.Engine.pTextfont;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Render.Types.GL10.GL_ALPHA_TEST;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.OrphoRenderer;
import ru.m210projects.Build.Render.Renderer.Transparent;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.Tile.AnimType;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.TileFont.FontType;

public class GdxOrphoRen extends OrphoRenderer {

	protected final TextureManager textureCache;
	protected final GdxBatch batch;
	protected final ShapeRenderer shape;

	public GdxOrphoRen(Engine engine, TextureManager textureCache) {
		super(engine);
		this.textureCache = textureCache;

		this.batch = new GdxBatch();
		this.shape = new ShapeRenderer();
	}

	@Override
	public void init() {
	}

	@Override
	public void uninit() {
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		if (font.type == FontType.Tilemap) {
			if (palookup[col] == null)
				col = 0;

			int nTile = (Integer) font.ptr;
			if (engine.getTile(nTile).data == null && engine.loadtile(nTile) == null)
				return;
		}

		GLTile atlas = font.getGL(textureCache, col);
		if (atlas == null)
			return;

		int opal = globalpal;
		globalpal = col;

		textureCache.bind(atlas);

		BuildGdx.gl.glDisable(GL_ALPHA_TEST);
		BuildGdx.gl.glDepthMask(false); // disable writing to the z-buffer
		BuildGdx.gl.glEnable(GL_BLEND);
		BuildGdx.gl.glEnable(GL_TEXTURE_2D);

		xpos <<= 16;
		ypos <<= 16;

		bindBatch();
//		if (shade >= 0) {
//			batch.setColor(curpalette.getRed(shade) / 255.0f, curpalette.getGreen(shade) / 255.0f,curpalette.getBlue(shade) / 255.0f, 1.0f);
//			batch.draw(atlas, xpos, ypos, text.length * xsiz, 8, 0, 0, 64, 0, 1, 1, 0, (int) (scale * 65536), 8, 0, 0, xdim - 1, ydim - 1);
//		}

		int oxpos = xpos;
		int c = 0, line = 0, yoffs;
		float tx, ty;
		int df = font.sizx / font.cols;

		batch.setColor(curpalette.getRed(col) / 255.0f, curpalette.getGreen(col) / 255.0f,curpalette.getBlue(col) / 255.0f, 1.0f);
		while (c < text.length && text[c] != '\0') {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * font.charsizx);
			}
			if (text[c] == '\r') text[c] = 0;
			yoffs = (int) (scale * line * font.charsizy);

			tx = (text[c] % font.cols) * df;
			ty = (text[c] / font.cols) * df;

			batch.draw(atlas, xpos, ypos, font.charsizx, font.charsizy,
					0, -yoffs, tx, ty, font.charsizx, font.charsizy,
					0, (int) (scale * 65536), 8, 0, 0, xdim - 1, ydim - 1);

			xpos += scale * (font.charsizx << 16);
			c++;
		}
		BuildGdx.gl.glDepthMask(true); // re-enable writing to the z-buffer

		globalpal = opal;

		textureCache.unbind();
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		printext(fontsize == 0 ? pTextfont : pSmallTextfont, xpos, ypos, text, col, 0, Transparent.None, scale);
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
		batch.draw(pth, sx, sy, xsiz, ysiz, xoff, yoff, a, z, dastat, cx1, cy1, cx2, cy2);
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
