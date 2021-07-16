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

package ru.m210projects.Build.Render;

import static ru.m210projects.Build.Engine.headspritesect;
import static ru.m210projects.Build.Engine.nextspritesect;
import static ru.m210projects.Build.Engine.numsectors;
import static ru.m210projects.Build.Engine.sector;
import static ru.m210projects.Build.Engine.show2dsector;
import static ru.m210projects.Build.Engine.sintable;
import static ru.m210projects.Build.Engine.wall;
import static ru.m210projects.Build.Engine.wx1;
import static ru.m210projects.Build.Engine.wx2;
import static ru.m210projects.Build.Engine.wy1;
import static ru.m210projects.Build.Engine.wy2;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Engine.yxaspect;
import static ru.m210projects.Build.Gameutils.BClipRange;
import static ru.m210projects.Build.Gameutils.isValidSector;
import static ru.m210projects.Build.Net.Mmulti.connecthead;
import static ru.m210projects.Build.Net.Mmulti.connectpoint2;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.klabs;
import static ru.m210projects.Build.Pragmas.mulscale;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Render.Renderer.Transparent;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.WALL;

public abstract class OrphoRenderer {

	protected final Engine engine;

	public OrphoRenderer(Engine engine) {
		this.engine = engine;
	}

	public abstract void init();

	public abstract void uninit();

	public abstract void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale);

	public abstract void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale);

	public abstract void drawline256(int x1, int y1, int x2, int y2, int col);

	public abstract void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat,
			int cx1, int cy1, int cx2, int cy2);

	public abstract void nextpage();

	public abstract void drawmapview(int dax, int day, int zoome, int ang);

	// Overhead map settings

	public boolean fullmap, scrollmode;
	public int viewindex;

	protected int getclipmask(int a, int b, int c, int d) { // Ken did this
		int bA = a < 0 ? 1 : 0;
		int bB = b < 0 ? 1 : 0;
		int bC = c < 0 ? 1 : 0;
		int bD = d < 0 ? 1 : 0;

		d = (bA * 8) + (bB * 4) + (bC * 2) + bD;
		return (((d << 4) ^ 0xf0) | d);
	}

	public void setmapsettings(boolean fullmap, boolean scrollmode, int viewindex) {
		this.fullmap = fullmap;
		this.scrollmode = scrollmode;
		this.viewindex = viewindex;
	}

	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		int i, j, k, x1, y1, x2 = 0, y2 = 0, ox, oy;
		int z1, z2, startwall, endwall;
		int xvect, yvect, xvect2, yvect2;

		WALL wal, wal2;

		xvect = sintable[(-cang) & 2047] * czoom;
		yvect = sintable[(1536 - cang) & 2047] * czoom;
		xvect2 = mulscale(xvect, yxaspect, 16);
		yvect2 = mulscale(yvect, yxaspect, 16);

		// Draw red lines
		for (i = 0; i < numsectors; i++) {
			if (!fullmap && (show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			startwall = sector[i].wallptr;
			endwall = sector[i].wallptr + sector[i].wallnum;

			z1 = sector[i].ceilingz;
			z2 = sector[i].floorz;

			if (startwall < 0 || endwall < 0)
				continue;

			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if (wal == null)
					continue;
				k = wal.nextwall;
				if (k < 0 || k > j)
					continue;
				if (wal.nextsector < 0)
					continue;

				if (sector[wal.nextsector] != null
						&& ((sector[wal.nextsector].ceilingz != z1 || sector[wal.nextsector].floorz != z2
								|| (wall[wal.nextwall] != null
										&& ((wal.cstat | wall[wal.nextwall].cstat) & (16 + 32)) != 0)))
						&& isShowRedWalls()
						|| !fullmap && (show2dsector[wal.nextsector >> 3] & 1 << (wal.nextsector & 7)) == 0) {
					ox = wal.x - cposx;
					oy = wal.y - cposy;
					x1 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y1 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

					wal2 = wall[wal.point2];
					ox = wal2.x - cposx;
					oy = wal2.y - cposy;
					x2 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y2 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

					drawline256(x1, y1, x2, y2, getWallColor(wal));
				}
			}
		}

		// Draw sprites
		for (i = 0; i < numsectors; i++) {
			if (!fullmap && (show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			for (j = headspritesect[i]; j >= 0; j = nextspritesect[j]) {

			}

		}

		// Draw white lines
		for (i = 0; i < numsectors; i++) {

			if (!fullmap && (show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			startwall = sector[i].wallptr;
			endwall = sector[i].wallptr + sector[i].wallnum;

			if (startwall < 0 || endwall < 0)
				continue;

			k = -1;
			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if (wal == null)
					continue;
				if (wal.nextwall >= 0)
					continue;
				Tile pic = engine.getTile(wal.picnum);
				if (!pic.hasSize())
					continue;

				if (j == k) {
					x1 = x2;
					y1 = y2;
				} else {
					ox = wal.x - cposx;
					oy = wal.y - cposy;
					x1 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y1 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
				}

				k = wal.point2;
				wal2 = wall[k];
				if (wal2 == null)
					continue;

				ox = wal2.x - cposx;
				oy = wal2.y - cposy;
				x2 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
				y2 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

				drawline256(x1, y1, x2, y2, getWallColor(wal));
			}
		}

		// draw player
		for (i = connecthead; i >= 0; i = connectpoint2[i]) {
			SPRITE pPlayer = getPlayerSprite(i);
			if (pPlayer == null || !isValidSector(pPlayer.sectnum))
				continue;

			ox = pPlayer.x - cposx;
			oy = pPlayer.y - cposy;

			int dx = mulscale(ox, xvect, 16) - mulscale(oy, yvect, 16);
			int dy = mulscale(oy, xvect2, 16) + mulscale(ox, yvect2, 16);

			int dang = (pPlayer.ang - cang) & 0x7FF;
			if (i == viewindex && !scrollmode) {
				dx = 0;
				dy = viewindex ^ i;
				dang = 0;
			}

			if (i == viewindex || isShowAllPlayers()) {
				int nZoom = mulscale(yxaspect,
						czoom * (klabs((sector[pPlayer.sectnum].floorz - pPlayer.z) >> 8) + pPlayer.yrepeat), 16);
				nZoom = BClipRange(nZoom, 22000, 0x20000);
				int sx = (dx << 4) + (xdim << 15);
				int sy = (dy << 4) + (ydim << 15);

				rotatesprite(sx, sy, nZoom, (short) dang, getPlayerPicnum(i), pPlayer.shade, pPlayer.pal,
						(pPlayer.cstat & 2) >> 1, wx1, wy1, wx2, wy2);
			}
		}
	}

	public boolean isShowSprites() {
		return false;
	}

	public boolean isShowFloorSprites() {
		return false;
	}

	public boolean isShowWallSprites() {
		return false;
	}

	public boolean isShowRedWalls() {
		return true;
	}

	public boolean isShowAllPlayers() {
		return false;
	}

	public boolean isSpriteVisible(SPRITE spr) {
		return true;
	}

	public boolean isWallVisible(int w, int s) {
		WALL wal = wall[w];
		SECTOR sec = sector[s];
		if (wal.nextsector != 0) // red wall
			return (wal.nextwall <= w && ((sector[wal.nextsector].ceilingz != sec.ceilingz //
					|| sector[wal.nextsector].floorz != sec.floorz //
					|| ((wal.cstat | wall[wal.nextwall].cstat) & (16 + 32)) != 0)
					|| (!fullmap && (show2dsector[wal.nextsector >> 3] & 1 << (wal.nextsector & 7)) == 0)));
		return true;
	}

	public int getWallColor(WALL wal) {
		if (wal.nextsector != 0) // red wall
			return 31;
		return 31; // white wall
	}

	public int getSpriteColor(SPRITE spr) {
		switch (spr.cstat & 48) {
		case 0:
			return 31;
		case 16:
			return 31;
		case 32:
			return 31;
		}

		return 31;
	}

	public SPRITE getPlayerSprite(int player) {
		return null;
	}

	public int getPlayerPicnum(int player) {
		SPRITE spr = getPlayerSprite(player);
		return spr != null ? spr.picnum : -1;
	}
}
