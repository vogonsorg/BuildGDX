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
import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_TEST;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.*;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Render.Types.GL10.GL_ALPHA_TEST;
import static ru.m210projects.Build.Net.Mmulti.connecthead;
import static ru.m210projects.Build.Net.Mmulti.connectpoint2;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.OrphoRenderer;
import ru.m210projects.Build.Render.Renderer.Transparent;
import ru.m210projects.Build.Render.GdxRender.WorldMesh.GLSurface;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.Tile.AnimType;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.TileFont.FontType;
import ru.m210projects.Build.Types.WALL;

public class GdxOrphoRen extends OrphoRenderer {

	protected final TextureManager textureCache;
	protected final GdxBatch batch;
	protected final ShapeRenderer shape;
	protected ShaderProgram bitmapShader;
	protected GDXRenderer parent;

	public GdxOrphoRen(Engine engine, GDXRenderer parent) {
		super(engine);
		this.textureCache = parent.textureCache;
		this.parent = parent;

		this.batch = new GdxBatch();
		this.shape = new ShapeRenderer();
		this.bitmapShader = createBitmapShader();
	}

	public ShaderProgram createBitmapShader() {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "varying vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
				+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";

		String fragmentShader = "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "varying LOWP vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "void main()\n" //
				+ "{" //
				+ "	float alpha = texture2D(u_texture, v_texCoords).a;" //
				+ "	gl_FragColor = vec4(v_color.rgb, alpha);\n" //
				+ "}"; //

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader) {
			@Override
			public void begin() {
				super.begin();
				GDXRenderer.currentShader = this;
			}
		};
		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	@Override
	public void init() {
		resize(xdim, ydim);
	}

	@Override
	public void uninit() {
	}

	protected void drawoverheadline(WALL wal, int cposx, int cposy, float cos, float sin, int col) {
		WALL wal2 = wall[wal.point2];

		int ox = cposx - wal.x;
		int oy = cposy - wal.y;
		float x1 = ox * cos - oy * sin + xdim * 2048;
		float y1 = ox * sin + oy * cos + ydim * 2048;

		ox = cposx - wal2.x;
		oy = cposy - wal2.y;

		float x2 = ox * cos - oy * sin + xdim * 2048;
		float y2 = ox * sin + oy * cos + ydim * 2048;

		drawline256((int) x1, (int) y1, (int) x2, (int) y2, col);
	}

	@Override
	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		float cos = (float) Math.cos((512 - cang) * buildAngleToRadians) * czoom / 4.0f;
		float sin = (float) Math.sin((512 - cang) * buildAngleToRadians) * czoom / 4.0f;

		for (int i = 0; i < numsectors; i++) {
			if ((!fullmap && (show2dsector[i >> 3] & (1 << (i & 7))) == 0) || !Gameutils.isValidSector(i))
				continue;

			SECTOR sec = sector[i];
			if (!Gameutils.isValidWall(sec.wallptr) || sec.wallnum < 3)
				continue;

			int startwall = sec.wallptr;
			for (int j = 0; j < sec.wallnum; j++, startwall++) {
				if (!Gameutils.isValidWall(startwall) || !Gameutils.isValidWall(wall[startwall].point2))
					continue;

				WALL wal = wall[startwall];
				if (isShowRedWalls() && wal.nextwall >= 0) {
					if (Gameutils.isValidSector(wal.nextsector)) {
						if (isWallVisible(startwall, i))
							drawoverheadline(wal, cposx, cposy, cos, sin, getWallColor(wal));
					}
				}

				if (wal.nextwall >= 0)
					continue;

				Tile pic = engine.getTile(wal.picnum);
				if (!pic.hasSize())
					continue;

				drawoverheadline(wal, cposx, cposy, cos, sin, getWallColor(wal));
			}
		}

		// Draw sprites
		if (isShowSprites()) {
			for (int i = 0; i < numsectors; i++) {
				if (!fullmap && (show2dsector[i >> 3] & (1 << (i & 7))) == 0)
					continue;

				for (int j = headspritesect[i]; j >= 0; j = nextspritesect[j]) {
					SPRITE spr = sprite[j];

					if ((spr.cstat & 0x8000) != 0 || spr.xrepeat == 0 || spr.yrepeat == 0 || !isSpriteVisible(spr))
						continue;

					switch (spr.cstat & 48) {
					case 0:
						if (((gotsector[i >> 3] & (1 << (i & 7))) > 0) && (czoom > 96)) {
							int ox = cposx - spr.x;
							int oy = cposy - spr.y;
							float dx = ox * cos - oy * sin;
							float dy = ox * sin + oy * cos;
							int daang = (spr.ang - cang) & 0x7FF;
							int nZoom = czoom * spr.yrepeat;
							int sx = (int) (dx + xdim * 2048);
							int sy = (int) (dy + ydim * 2048);

							rotatesprite(sx * 16, sy * 16, nZoom, (short) daang, spr.picnum, spr.shade, spr.pal,
									(spr.cstat & 2) >> 1, wx1, wy1, wx2, wy2);
						}
						break;
					case 16:
						if (isShowWallSprites()) {
							Tile pic = engine.getTile(spr.picnum);
							int x1 = spr.x;
							int y1 = spr.y;
							byte xoff = (byte) (pic.getOffsetX() + spr.xoffset);
							if ((spr.cstat & 4) > 0)
								xoff = (byte) -xoff;

							int dax = sintable[spr.ang & 2047] * spr.xrepeat;
							int day = sintable[(spr.ang + 1536) & 2047] * spr.xrepeat;
							int k = (pic.getWidth() >> 1) + xoff;
							x1 -= mulscale(dax, k, 16);
							int x2 = x1 + mulscale(dax, pic.getWidth(), 16);
							y1 -= mulscale(day, k, 16);
							int y2 = y1 + mulscale(day, pic.getWidth(), 16);

							int ox = cposx - x1;
							int oy = cposy - y1;
							x1 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y1 = (int) (ox * sin + oy * cos) + (ydim << 11);

							ox = cposx - x2;
							oy = cposy - y2;
							x2 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y2 = (int) (ox * sin + oy * cos) + (ydim << 11);

							drawline256(x1, y1, x2, y2, getSpriteColor(spr));
						}
						break;
					case 32:
						if (isShowFloorSprites()) {
							Tile pic = engine.getTile(spr.picnum);
							byte xoff = (byte) (pic.getOffsetX() + spr.xoffset);
							byte yoff = (byte) (pic.getOffsetY() + spr.yoffset);
							if ((spr.cstat & 4) > 0)
								xoff = (byte) -xoff;
							if ((spr.cstat & 8) > 0)
								yoff = (byte) -yoff;

							int cosang = sintable[(spr.ang + 512) & 2047];
							int sinang = sintable[spr.ang & 2047];

							int dax = ((pic.getWidth() >> 1) + xoff) * spr.xrepeat;
							int day = ((pic.getHeight() >> 1) + yoff) * spr.yrepeat;
							int x1 = spr.x + dmulscale(sinang, dax, cosang, day, 16);
							int y1 = spr.y + dmulscale(sinang, day, -cosang, dax, 16);
							int l = pic.getWidth() * spr.xrepeat;
							int x2 = x1 - mulscale(sinang, l, 16);
							int y2 = y1 + mulscale(cosang, l, 16);
							l = pic.getHeight() * spr.yrepeat;
							int k = -mulscale(cosang, l, 16);
							int x3 = x2 + k;
							int x4 = x1 + k;
							k = -mulscale(sinang, l, 16);
							int y3 = y2 + k;
							int y4 = y1 + k;

							int ox = cposx - x1;
							int oy = cposy - y1;
							x1 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y1 = (int) (ox * sin + oy * cos) + (ydim << 11);

							ox = cposx - x2;
							oy = cposy - y2;
							x2 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y2 = (int) (ox * sin + oy * cos) + (ydim << 11);

							ox = cposx - x3;
							oy = cposy - y3;
							x3 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y3 = (int) (ox * sin + oy * cos) + (ydim << 11);

							ox = cposx - x4;
							oy = cposy - y4;
							x4 = (int) (ox * cos - oy * sin) + (xdim << 11);
							y4 = (int) (ox * sin + oy * cos) + (ydim << 11);

							int col = getSpriteColor(spr);
							drawline256(x1, y1, x2, y2, col);
							drawline256(x2, y2, x3, y3, col);
							drawline256(x3, y3, x4, y4, col);
							drawline256(x4, y4, x1, y1, col);
						}
						break;
					}
				}
			}
		}

		// draw player
		for (int i = connecthead; i >= 0; i = connectpoint2[i]) {
			SPRITE pPlayer = getPlayerSprite(i);
			if (pPlayer == null || !isValidSector(pPlayer.sectnum))
				continue;

			int ox = cposx - pPlayer.x;
			int oy = cposy - pPlayer.y;

			float dx = ox * cos - oy * sin;
			float dy = ox * sin + oy * cos;

			int dang = (pPlayer.ang - cang) & 0x7FF;
			if (i == viewindex && !scrollmode) {
				dx = 0;
				dy = viewindex ^ i;
				dang = 0;
			}

			if (i == viewindex || isShowAllPlayers()) {
				int nZoom = czoom * (klabs((sector[pPlayer.sectnum].floorz - pPlayer.z) >> 8) + pPlayer.yrepeat);
				nZoom = BClipRange(nZoom, 22000, 0x20000);

				int sx = (int) (dx + xdim * 2048);
				int sy = (int) (dy + ydim * 2048);

				rotatesprite(sx * 16, sy * 16, nZoom, (short) dang, getPlayerPicnum(i), pPlayer.shade, pPlayer.pal,
						(pPlayer.cstat & 2) >> 1, wx1, wy1, wx2, wy2);
			}
		}
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) { // TODO:
		beforedrawrooms = 0;

		Arrays.fill(gotsector, (byte) 0);

		ShaderProgram shader = parent.getTextureShader();
		Matrix4 worldTrans = parent.transform;

		float zoom = 32 / (float) zoome;
		shader.begin();

		shader.setUniformMatrix("u_projTrans", worldTrans.setToOrtho(zoom * xdim / 2, zoom * (-xdim / 2),
				zoom * -(ydim / 2), zoom * ydim / 2, -parent.cam.far, parent.cam.far));
		shader.setUniformMatrix("u_modelView", worldTrans.idt());
		parent.setFrustum(null);

		int sortnum = 0;
		for (int s = 0; s < numsectors; s++) {
			SECTOR sec = sector[s];

			if (fullmap || (show2dsector[s >> 3] & pow2char[s & 7]) != 0) {
				if (isShowFloorSprites()) {
					// Collect floor sprites to draw
					for (int i = headspritesect[s]; i >= 0; i = nextspritesect[i])
						if ((sprite[i].cstat & 48) == 32) {
							if (sortnum >= MAXSPRITESONSCREEN)
								break;

							if ((sprite[i].cstat & (64 + 8)) == (64 + 8))
								continue;

							if (tsprite[sortnum] == null)
								tsprite[sortnum] = new SPRITE();
							tsprite[sortnum].set(sprite[i]);
							tsprite[sortnum++].owner = (short) i;
						}

					// XXX
				}

				if (isShowSprites()) {
					for (int i = headspritesect[s]; i >= 0; i = nextspritesect[i])
						if ((show2dsprite[i >> 3] & pow2char[i & 7]) != 0) {
							if (sortnum >= MAXSPRITESONSCREEN)
								break;

							if (tsprite[sortnum] == null)
								tsprite[sortnum] = new SPRITE();
							tsprite[sortnum].set(sprite[i]);
							tsprite[sortnum++].owner = (short) i;
						}

					// XXX
				}

				gotsector[s >> 3] |= pow2char[s & 7];
				if (sec.isParallaxFloor())
					continue;
				globalpal = sec.floorpal;

				int globalpicnum = sec.floorpicnum;
				if (globalpicnum >= MAXTILES)
					globalpicnum = 0;
				engine.setgotpic(globalpicnum);
				Tile pic = engine.getTile(globalpicnum);

				if (!pic.hasSize())
					continue;

				if (pic.getType() != AnimType.None) {
					globalpicnum += engine.animateoffs(globalpicnum, s);
					pic = engine.getTile(globalpicnum);
				}

				if (!pic.isLoaded())
					engine.loadtile(globalpicnum);

				globalshade = max(min(sec.floorshade, numshades - 1), 0);

				GLSurface flor = parent.world.getFloor(s);
				if (flor != null) {
					worldTrans.setToRotation(0, 0, 1, (512 - ang) * buildAngleToDegrees);
					worldTrans.translate(-dax / parent.cam.xscale, -day / parent.cam.xscale,
							-sector[s].floorz / parent.cam.yscale);
					shader.setUniformMatrix("u_transform", worldTrans);

					parent.drawSurf(flor, 0, worldTrans);
				}
			}
		}

		if (isShowSprites()) {
			// Sort sprite list
			int gap = 1;
			while (gap < sortnum)
				gap = (gap << 1) + 1;
			for (gap >>= 1; gap > 0; gap >>= 1)
				for (int i = 0; i < sortnum - gap; i++)
					for (int j = i; j >= 0; j -= gap) {
						if (sprite[tsprite[j].owner].z <= sprite[tsprite[j + gap].owner].z)
							break;

						short tmp = tsprite[j].owner;
						tsprite[j].owner = tsprite[j + gap].owner;
						tsprite[j + gap].owner = tmp;
					}

			for (int s = sortnum - 1; s >= 0; s--) {
				SPRITE spr = sprite[tsprite[s].owner];
				if ((spr.cstat & 32768) == 0) {
					if (spr.picnum >= MAXTILES)
						spr.picnum = 0;

					Tile pic = engine.getTile(spr.picnum);

				}
			}
		}

		shader.setUniformMatrix("u_projTrans", parent.cam.combined);
		shader.setUniformMatrix("u_modelView", parent.cam.view);

		shader.end();
	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {

		if (font.type == FontType.Tilemap) {
			if (palookup[col] == null)
				col = 0;

			int nTile = (Integer) font.ptr;
			if (!engine.getTile(nTile).isLoaded() && engine.loadtile(nTile) == null)
				return;
		}

		batch.flush();
		batch.setClip(0, 0, xdim - 1, ydim - 1);

		ShaderProgram oldShader = batch.getShader();
		batch.setShader(bitmapShader);

		GLTile atlas = font.getGL(textureCache, PixelFormat.Pal8, col);
		if (atlas == null)
			return;

		textureCache.bind(atlas);

		BuildGdx.gl.glDisable(GL_DEPTH_TEST);
		BuildGdx.gl.glDisable(GL_ALPHA_TEST);
		BuildGdx.gl.glDepthMask(false); // disable writing to the z-buffer
		BuildGdx.gl.glEnable(GL_BLEND);
		BuildGdx.gl.glEnable(GL_TEXTURE_2D);

		xpos <<= 16;
		ypos <<= 16;

		bindBatch();
		int oxpos = xpos;
		int c = 0, line = 0, yoffs;
		float tx, ty;
		int df = font.sizx / font.cols;

		batch.setColor(curpalette.getRed(col) / 255.0f, curpalette.getGreen(col) / 255.0f,
				curpalette.getBlue(col) / 255.0f, 1.0f);

		while (c < text.length && text[c] != '\0') {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * font.charsizx);
			}
			if (text[c] == '\r')
				text[c] = 0;
			yoffs = (int) (scale * line * font.charsizy);

			tx = (text[c] % font.cols) * df;
			ty = (text[c] / font.cols) * df;

			batch.draw(atlas, xpos, ypos, font.charsizx, font.charsizy, 0, -yoffs, tx, ty, font.charsizx, font.charsizy,
					0, (int) (scale * 65536), 8, 0, 0, xdim - 1, ydim - 1);

			xpos += scale * (font.charsizx << 16);
			c++;
		}
		BuildGdx.gl.glDepthMask(true); // re-enable writing to the z-buffer

		batch.setShader(oldShader);
	}

	public void resize(int width, int height) {
		batch.resize(width, height);
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		printext(fontsize == 0 ? pTextfont : pSmallTextfont, xpos, ypos, text, col, 0, Transparent.None, scale);
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		float sx1 = x1 / 4096.0f;
		float sy1 = ydim - y1 / 4096.0f;
		float sx2 = x2 / 4096.0f;
		float sy2 = ydim - y2 / 4096.0f;

		if (sx1 < 0 && sx2 < 0 || sx1 > xdim && sx2 > xdim)
			return;

		if (sy1 < 0 && sy2 < 0 || sy1 > ydim && sy2 > ydim)
			return;

		col = palookup[0][col] & 0xFF;

		shape.begin(ShapeType.Line);
		shape.setColor(curpalette.getRed(col), curpalette.getGreen(col), curpalette.getBlue(col), 255);
		shape.line(sx1, sy1, sx2, sy2);
		shape.end();
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {

		if (picnum >= MAXTILES)
			return;
		if ((cx1 > cx2) || (cy1 > cy2))
			return;
		if (z <= 16)
			return;

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

		if (picnum >= MAXTILES)
			picnum = 0;
		if (palookup[dapalnum & 0xFF] == null)
			dapalnum = 0;

		engine.setgotpic(picnum);
		if (pic.data == null)
			engine.loadtile(picnum);

		GLTile pth = textureCache.get(PixelFormat.Rgba, picnum, dapalnum, 0, method); // XXX
		if (pth == null)
			return;

		GLTile lastBinded = textureCache.getLastBinded();
		if (textureCache.bind(pth)) {
//			System.err.println("Error! " + " " + pth.getPixelFormat());
//			if (lastBinded != null)
//				System.err.println(lastBinded.getPixelFormat());
		}

		if (((method & 3) == 0))
			batch.disableBlending();
		else
			batch.enableBlending();

		float shade = (numshades - min(max(dashade, 0), numshades)) / (float) numshades;
		float alpha = 1.0f;
		switch (method & 3) {
		case 2:
			alpha = TRANSLUSCENT1;
			break;
		case 3:
			alpha = TRANSLUSCENT2;
			break;
		}

		bindBatch();
		batch.setColor(shade, shade, shade, alpha);
		batch.draw(pth, sx, sy, xsiz, ysiz, xoff, yoff, a, z, dastat, cx1, cy1, cx2, cy2);
	}

	public void setTexture(GLTile tile) {
		batch.setTexture(tile);
	}

	public void setColor(float r, float g, float b, float a) {
		batch.setColor(r, g, b, a);
	}

	public void addVertex(float x, float y, float u, float v) {
		batch.addVertex(x, y, u, v);
	}

	public void begin() {
		if (!batch.isDrawing())
			batch.begin();
		else
			batch.flush();
	}

	public void end() {
		if (batch.isDrawing())
			batch.end();
	}

	public void draw(GLTile tile, int x, int y, int angle, int scale, float r, float g, float b, float alpha) {
		bindBatch();
		if (alpha == 1.0)
			batch.disableBlending();
		else
			batch.enableBlending();

		batch.setColor(r, g, b, alpha);
		batch.draw(tile, x << 16, y << 16, tile.getWidth(), tile.getHeight(), 0, xdim, angle, scale, 16 | 8 | 32 | 4, 0,
				0, xdim - 1, ydim - 1);
		batch.end();
	}

	private void bindBatch() {
		Gdx.gl.glDisable(GL_CULL_FACE);
		Gdx.gl.glDisable(GL_DEPTH_TEST);

		if (!batch.isDrawing())
			batch.begin();
	}

	@Override
	public void nextpage() {
		if (batch.isDrawing())
			batch.end();
	}

}
