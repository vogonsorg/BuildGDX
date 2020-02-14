/*
 * "POLYMOST" code originally written by Ken Silverman
 * Ken Silverman's official web site: "http://www.advsys.net/ken"
 * See the included license file "BUILDLIC.TXT" for license info.
 *
 * This file has been modified from Ken Silverman's original release
 * by Jonathon Fowler (jf@jonof.id.au)
 * by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Render;

import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_TEST;
import static java.lang.Math.*;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.*;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.*;
import static ru.m210projects.Build.Render.Types.GL10.*;
import static ru.m210projects.Build.Render.Polymost.*;
import static ru.m210projects.Build.Strhandler.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Render.Renderer.Transparent;
import ru.m210projects.Build.Render.TextureHandle.Pthtyp;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;
import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Render.Types.Palette;
import ru.m210projects.Build.Render.Types.Tile2model;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.WALL;
import ru.m210projects.Build.Types.TileFont.FontType;

public class Polymost2D extends OrphoRenderer {

	private Polymost parent;
	private GL10 gl;
	private Engine engine;
	private IntBuffer polymosttext;
	private final TextureCache textureCache;

	private final FloatBuffer vertices = BufferUtils.newFloatBuffer(8);
	private final FloatBuffer textures = BufferUtils.newFloatBuffer(8);

	protected int globalx1;
	protected int globaly1;
	protected int globalx2;
	protected int globaly2;

	private int guniqhudid;

	protected int[] xb1 = new int[MAXWALLSB];
	protected int[] xb2 = new int[MAXWALLSB];
	protected float[] rx1 = new float[MAXWALLSB];
	protected float[] ry1 = new float[MAXWALLSB];

	private int allocpoints = 0, slist[], npoint2[];
	private raster[] rst;
	private final float[] trapextx = new float[2];

	private SPRITE hudsprite;

//	private final int ROTATESPRITE_MAX = 2048;
	private final int RS_CENTERORIGIN = (1 << 30);

	protected int asm1; // drawmapview
	protected int asm2; // drawmapview

	private double guo, gux; // Screen-based texture mapping parameters
	private double guy;
	private double gvo;
	private double gvx;
	private double gvy;
	private short globalpicnum;
	private int globalorientation;

	private final Polygon drawpoly[] = new Polygon[4];
	private final Color polyColor = new Color();

	// Overhead map settings

	public Polymost2D(Polymost parent) {
		this.parent = parent;
		this.gl = parent.gl;
		this.engine = parent.engine;
		this.textureCache = parent.textureCache;

		for (int i = 0; i < 4; i++)
			drawpoly[i] = new Polygon();

		vertices.put(new float[] { 0, 0, 1, 0, 1, 1, 0, 1 });
		textures.put(new float[] { 0, 0, 1 - 0.0001f, 0, 1 - 0.0001f, 1 - 0.0001f, 0, 1 - 0.0001f });
		vertices.rewind();
		textures.rewind();
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		WALL wal;
		SECTOR sec = null;

		int i, j, x, y, bakx1, baky1;
		int s, w, ox, oy, startwall, cx1, cy1, cx2, cy2;
		int bakgxvect, bakgyvect, npoints;
		int xvect, yvect, xvect2, yvect2, daslope;

		int tilenum, xoff, yoff, k, l, cosang, sinang, xspan, yspan;
		int xrepeat, yrepeat, x1, y1, x2, y2, x3, y3, x4, y4;

		beforedrawrooms = 0;

		Arrays.fill(gotsector, (byte) 0);

		cx1 = (windowx1 << 12);
		cy1 = (windowy1 << 12);
		cx2 = ((windowx2 + 1) << 12) - 1;
		cy2 = ((windowy2 + 1) << 12) - 1;
		zoome <<= 8;
		bakgxvect = (int) divscale(sintable[(1536 - ang) & 2047], zoome, 28);
		bakgyvect = (int) divscale(sintable[(2048 - ang) & 2047], zoome, 28);
		xvect = mulscale(sintable[(2048 - ang) & 2047], zoome, 8);
		yvect = mulscale(sintable[(1536 - ang) & 2047], zoome, 8);
		xvect2 = mulscale(xvect, yxaspect, 16);
		yvect2 = mulscale(yvect, yxaspect, 16);

		int sortnum = 0;

		for (s = 0; s < numsectors; s++) {
			sec = sector[s];

			if (fullmap || (show2dsector[s >> 3] & pow2char[s & 7]) != 0) {
				npoints = 0;
				i = 0;
				startwall = sec.wallptr;

				j = startwall;
				if (startwall < 0)
					continue;
				for (w = sec.wallnum; w > 0; w--, j++) {
					wal = wall[j];
					if (wal == null)
						continue;
					ox = wal.x - dax;
					oy = wal.y - day;
					x = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
					i |= getclipmask(x - cx1, cx2 - x, y - cy1, cy2 - y);
					rx1[npoints] = x;
					ry1[npoints] = y;
					xb1[npoints] = wal.point2 - startwall;
					if (xb1[npoints] < 0)
						xb1[npoints] = 0;

					npoints++;
				}

				if ((i & 0xf0) != 0xf0)
					continue;

				bakx1 = (int) rx1[0];
				baky1 = mulscale((int) ry1[0] - (ydim << 11), xyaspect, 16) + (ydim << 11);

				if (showflspr) {
					// Collect floor sprites to draw
					for (i = headspritesect[s]; i >= 0; i = nextspritesect[i])
						if ((sprite[i].cstat & 48) == 32) {
							if (sortnum >= MAXSPRITESONSCREEN)
								continue;
							if ((sprite[i].cstat & (64 + 8)) == (64 + 8))
								continue;

							if (tsprite[sortnum] == null)
								tsprite[sortnum] = new SPRITE();
							tsprite[sortnum].set(sprite[i]);
							tsprite[sortnum++].owner = (short) i;
						}
				}

				if(showspr)
				{
					for (i = headspritesect[s]; i >= 0; i = nextspritesect[i])
						if ((show2dsprite[i >> 3] & pow2char[i & 7]) != 0) {
							if (sortnum >= MAXSPRITESONSCREEN)
								continue;
		
							if (tsprite[sortnum] == null)
								tsprite[sortnum] = new SPRITE();
							tsprite[sortnum].set(sprite[i]);
							tsprite[sortnum++].owner = (short) i;
						}
				}

				gotsector[s >> 3] |= pow2char[s & 7];

				globalorientation = sec.floorstat;
				if ((globalorientation & 1) != 0)
					continue;
				globalpal = sec.floorpal;

				globalpicnum = sec.floorpicnum;
				if (globalpicnum >= MAXTILES)
					globalpicnum = 0;
				engine.setgotpic(globalpicnum);
				if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0))
					continue;

				if ((picanm[globalpicnum] & 192) != 0)
					globalpicnum += engine.animateoffs(globalpicnum, s); // FIXME
				if (waloff[globalpicnum] == null)
					engine.loadtile(globalpicnum);

				globalshade = max(min(sec.floorshade, numshades - 1), 0);

				if ((globalorientation & 64) == 0) {
					globalposx = dax;
					globalx1 = bakgxvect;
					globaly1 = bakgyvect;
					globalposy = day;
					globalx2 = bakgxvect;
					globaly2 = bakgyvect;
				} else {
					ox = wall[wall[startwall].point2].x - wall[startwall].x;
					oy = wall[wall[startwall].point2].y - wall[startwall].y;
					i = engine.ksqrt(ox * ox + oy * oy);
					if (i == 0)
						continue;
					i = 1048576 / i;
					globalx1 = mulscale(dmulscale(ox, bakgxvect, oy, bakgyvect, 10), i, 10);
					globaly1 = mulscale(dmulscale(ox, bakgyvect, -oy, bakgxvect, 10), i, 10);
					ox = (bakx1 >> 4) - (xdim << 7);
					oy = (baky1 >> 4) - (ydim << 7);
					globalposx = dmulscale(-oy, (int) globalx1, -ox, (int) globaly1, 28);
					globalposy = dmulscale(-ox, (int) globalx1, oy, (int) globaly1, 28);
					globalx2 = -globalx1;
					globaly2 = -globaly1;

					daslope = sector[s].floorheinum;
					i = engine.ksqrt(daslope * daslope + 16777216);
					globalposy = mulscale(globalposy, i, 12);
					globalx2 = mulscale((int) globalx2, i, 12);
					globaly2 = mulscale((int) globaly2, i, 12);
				}
				int globalxshift = (8 - (picsiz[globalpicnum] & 15));
				int globalyshift = (8 - (picsiz[globalpicnum] >> 4));
				if ((globalorientation & 8) != 0) {
					globalxshift++;
					globalyshift++;
				}

				if ((globalorientation & 0x4) > 0) {
					i = globalposx;
					globalposx = -globalposy;
					globalposy = -i;
					i = (int) globalx2;
					globalx2 = globaly1;
					globaly1 = i;
					i = (int) globalx1;
					globalx1 = -globaly2;
					globaly2 = -i;
				}
				if ((globalorientation & 0x10) > 0) {
					globalx1 = -globalx1;
					globaly1 = -globaly1;
					globalposx = -globalposx;
				}
				if ((globalorientation & 0x20) > 0) {
					globalx2 = -globalx2;
					globaly2 = -globaly2;
					globalposy = -globalposy;
				}
				asm1 = (int) (globaly1 << globalxshift);
				asm2 = (int) (globalx2 << globalyshift);
				globalx1 <<= globalxshift;
				globaly2 <<= globalyshift;
				globalposx = (globalposx << (20 + globalxshift)) + ((sec.floorxpanning) << 24);
				globalposy = (globalposy << (20 + globalyshift)) - ((sec.floorypanning) << 24);

				fillpolygon(npoints);
			}
		}

		if (showspr) {
			// Sort sprite list
			int gap = 1;
			while (gap < sortnum)
				gap = (gap << 1) + 1;
			for (gap >>= 1; gap > 0; gap >>= 1)
				for (i = 0; i < sortnum - gap; i++)
					for (j = i; j >= 0; j -= gap) {
						if (sprite[tsprite[j].owner].z <= sprite[tsprite[j + gap].owner].z)
							break;

						short tmp = tsprite[j].owner;
						tsprite[j].owner = tsprite[j + gap].owner;
						tsprite[j + gap].owner = tmp;
					}

			for (s = sortnum - 1; s >= 0; s--) {
				SPRITE spr = sprite[tsprite[s].owner];
				if ((spr.cstat & 32768) == 0) {
					npoints = 0;

					tilenum = spr.picnum;
					xoff = (byte) ((picanm[tilenum] >> 8) & 255) + spr.xoffset;
					yoff = (byte) ((picanm[tilenum] >> 16) & 255) + spr.yoffset;
					if ((spr.cstat & 4) > 0)
						xoff = -xoff;
					if ((spr.cstat & 8) > 0)
						yoff = -yoff;

					k = spr.ang & 2047;
					cosang = sintable[(k + 512) & 2047];
					sinang = sintable[k];
					xspan = tilesizx[tilenum];
					xrepeat = spr.xrepeat;
					yspan = tilesizy[tilenum];
					yrepeat = spr.yrepeat;
					ox = ((xspan >> 1) + xoff) * xrepeat;
					oy = ((yspan >> 1) + yoff) * yrepeat;
					x1 = spr.x + mulscale(sinang, ox, 16) + mulscale(cosang, oy, 16);
					y1 = spr.y + mulscale(sinang, oy, 16) - mulscale(cosang, ox, 16);
					l = xspan * xrepeat;
					x2 = x1 - mulscale(sinang, l, 16);
					y2 = y1 + mulscale(cosang, l, 16);
					l = yspan * yrepeat;
					k = -mulscale(cosang, l, 16);
					x3 = x2 + k;
					x4 = x1 + k;
					k = -mulscale(sinang, l, 16);
					y3 = y2 + k;
					y4 = y1 + k;

					xb1[0] = 1;
					xb1[1] = 2;
					xb1[2] = 3;
					xb1[3] = 0;
					npoints = 4;

					i = 0;

					ox = x1 - dax;
					oy = y1 - day;
					x = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
					i |= getclipmask(x - cx1, cx2 - x, y - cy1, cy2 - y);
					rx1[0] = x;
					ry1[0] = y;

					ox = x2 - dax;
					oy = y2 - day;
					x = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
					i |= getclipmask(x - cx1, cx2 - x, y - cy1, cy2 - y);
					rx1[1] = x;
					ry1[1] = y;

					ox = x3 - dax;
					oy = y3 - day;
					x = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
					i |= getclipmask(x - cx1, cx2 - x, y - cy1, cy2 - y);
					rx1[2] = x;
					ry1[2] = y;

					x = (int) (rx1[0] + rx1[2] - rx1[1]);
					y = (int) (ry1[0] + ry1[2] - ry1[1]);
					i |= getclipmask(x - cx1, cx2 - x, y - cy1, cy2 - y);
					rx1[3] = x;
					ry1[3] = y;

					if ((i & 0xf0) != 0xf0)
						continue;
					bakx1 = (int) rx1[0];
					baky1 = mulscale((int) ry1[0] - (ydim << 11), xyaspect, 16) + (ydim << 11);

					globalpicnum = spr.picnum;
					globalpal = spr.pal; // GL needs this, software doesn't
					if (globalpicnum >= MAXTILES)
						globalpicnum = 0;
					engine.setgotpic(globalpicnum);
					if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0))
						continue;
					if ((picanm[globalpicnum] & 192) != 0)
						globalpicnum += engine.animateoffs(globalpicnum, s);
					if (waloff[globalpicnum] == null)
						engine.loadtile(globalpicnum);

					// 'loading' the tile doesn't actually guarantee that it's there afterwards.
					// This can really happen when drawing the second frame of a floor-aligned
					// 'storm icon' sprite (4894+1)

					if ((sector[spr.sectnum].ceilingstat & 1) > 0)
						globalshade = ((int) sector[spr.sectnum].ceilingshade);
					else
						globalshade = ((int) sector[spr.sectnum].floorshade);
					globalshade = max(min(globalshade + spr.shade + 6, numshades - 1), 0);

					// relative alignment stuff
					ox = x2 - x1;
					oy = y2 - y1;
					i = ox * ox + oy * oy;
					if (i == 0)
						continue;
					i = (65536 * 16384) / i;
					globalx1 = mulscale(dmulscale(ox, bakgxvect, oy, bakgyvect, 10), i, 10);
					globaly1 = mulscale(dmulscale(ox, bakgyvect, -oy, bakgxvect, 10), i, 10);
					ox = y1 - y4;
					oy = x4 - x1;
					i = ox * ox + oy * oy;
					if (i == 0)
						continue;
					i = (65536 * 16384) / i;
					globalx2 = mulscale(dmulscale(ox, bakgxvect, oy, bakgyvect, 10), i, 10);
					globaly2 = mulscale(dmulscale(ox, bakgyvect, -oy, bakgxvect, 10), i, 10);

					ox = picsiz[globalpicnum];
					oy = ((ox >> 4) & 15);
					ox &= 15;
					if (pow2long[ox] != xspan) {
						ox++;
						globalx1 = mulscale(globalx1, xspan, ox);
						globaly1 = mulscale(globaly1, xspan, ox);
					}

					bakx1 = (bakx1 >> 4) - (xdim << 7);
					baky1 = (baky1 >> 4) - (ydim << 7);
					globalposx = dmulscale(-baky1, globalx1, -bakx1, globaly1, 28);
					globalposy = dmulscale(bakx1, globalx2, -baky1, globaly2, 28);

					if ((spr.cstat & 0x4) > 0) {
						globalx1 = -globalx1;
						globaly1 = -globaly1;
						globalposx = -globalposx;
					}
					asm1 = (int) (globaly1 << 2);
					globalx1 <<= 2;
					globalposx <<= (20 + 2);
					asm2 = (int) (globalx2 << 2);
					globaly2 <<= 2;
					globalposy <<= (20 + 2);

					// so polymost can get the translucency. ignored in software mode:
					globalorientation = ((spr.cstat & 2) << 7) | ((spr.cstat & 512) >> 2);

					fillpolygon(npoints);
				}
			}
		}
	}

	protected void setpolymost2dview() {
		if (parent.gloy1 != -1 || parent.gloy1 != windowy1) {
			gl.glViewport(0, 0, xdim, ydim);
			gl.glMatrixMode(GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrthof(0, xdim, ydim, 0, -1, 1);
			gl.glMatrixMode(GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		parent.gloy1 = -1;

		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_TEXTURE_2D);
		gl.glDisable(GL_BLEND);
	}

	protected void fillpolygon(int npoints) {

		for (int z = 0; z < npoints; z++) {
			if (xb1[z] >= npoints)
				xb1[z] = 0;
		}

		if (palookup[globalpal] == null)
			globalpal = 0;

		globalx1 = mulscale(globalx1, xyaspect, 16);
		globaly2 = mulscale(globaly2, xyaspect, 16);
		gux = asm1 / 4294967296.0;
		gvx = asm2 / 4294967296.0;
		guy = globalx1 / 4294967296.0;
		gvy = -globaly2 / 4294967296.0;
		guo = (xdim * gux + ydim * guy) * -0.5 + globalposx / 4294967296.0;
		gvo = (xdim * gvx + ydim * gvy) * -0.5 - globalposy / 4294967296.0;

		for (int i = npoints - 1; i >= 0; i--) {
			rx1[i] /= 4096.0f;
			ry1[i] /= 4096.0f;
		}

		gl.glDisable(GL_FOG);

		setpolymost2dview();
		gl.glEnable(GL_ALPHA_TEST);
		gl.glEnable(GL_TEXTURE_2D);
		Pthtyp pth = textureCache.cache(globalpicnum, globalpal, (short) 0, false, true);

		bindTexture(pth.glpic);
		float f = parent.getshadefactor(globalshade), a = 0.0f;

		switch ((globalorientation >> 7) & 3) {
		case 0:
		case 1:
			a = 1.0f;
			gl.glDisable(GL_BLEND);
			break;
		case 2:
			a = TRANSLUSCENT1;
			gl.glEnable(GL_BLEND);
			break;
		case 3:
			a = TRANSLUSCENT2;
			gl.glEnable(GL_BLEND);
			break;
		}

		gl.glColor4f(f, f, f, a);

		tessectrap(rx1, ry1, xb1, npoints); // vertices + textures
	}

	private void drawtrap(float x0, float x1, float y0, float x2, float x3, float y1) {
		if (y0 == y1)
			return;

		drawpoly[0].px = x0;
		drawpoly[0].py = y0;
		drawpoly[2].py = y1;

		int n = 3;
		if (x0 == x1) {
			drawpoly[1].px = x3;
			drawpoly[1].py = y1;
			drawpoly[2].px = x2;
		} else if (x2 == x3) {
			drawpoly[1].px = x1;
			drawpoly[1].py = y0;
			drawpoly[2].px = x3;
		} else {
			drawpoly[1].px = x1;
			drawpoly[1].py = y0;
			drawpoly[2].px = x3;
			drawpoly[3].px = x2;
			drawpoly[3].py = y1;
			n = 4;
		}

		gl.glBegin(GL_TRIANGLE_FAN);
		for (int i = 0; i < n; i++) {
			drawpoly[i].px = min(max(drawpoly[i].px, trapextx[0]), trapextx[1]);
			gl.glTexCoord2d(drawpoly[i].px * gux + drawpoly[i].py * guy + guo,
					drawpoly[i].px * gvx + drawpoly[i].py * gvy + gvo);
			gl.glVertex2d(drawpoly[i].px, drawpoly[i].py);
		}
		gl.glEnd();
	}

	private void tessectrap(float[] px, float[] py, int[] point2, int numpoints) {
		float x0, x1, m0, m1;
		int i, j, k, z, i0, i1, i2, i3, npoints, gap, numrst;

		if (numpoints + 16 > allocpoints) // 16 for safety
		{
			allocpoints = numpoints + 16;
			rst = new raster[allocpoints];
			for (i = 0; i < allocpoints; i++)
				rst[i] = new raster();

			slist = new int[allocpoints];

			npoint2 = new int[allocpoints];
		}

		// Remove unnecessary collinear points:
		for (i = 0; i < numpoints; i++)
			npoint2[i] = point2[i];
		npoints = numpoints;
		z = 0;

		for (i = 0; i < numpoints; i++) {
			j = npoint2[i];
			if ((point2[i] < i) && (i < numpoints - 1))
				z = 3;

			if (j < 0)
				continue;
			k = npoint2[j];
			if (k < 0)
				continue;

			m0 = (px[j] - px[i]) * (py[k] - py[j]);
			m1 = (py[j] - py[i]) * (px[k] - px[j]);
			if (m0 < m1) {
				z |= 1;
				continue;
			}
			if (m0 > m1) {
				z |= 2;
				continue;
			}
			npoint2[i] = k;
			npoint2[j] = -1;
			npoints--;
			i--; // collinear
		}

		if (z == 0)
			return;
		trapextx[0] = trapextx[1] = px[0];
		for (i = j = 0; i < numpoints; i++) {
			if (npoint2[i] < 0)
				continue;
			if (px[i] < trapextx[0])
				trapextx[0] = px[i];
			if (px[i] > trapextx[1])
				trapextx[1] = px[i];
			slist[j++] = i;
		}

		if (z != 3) // Simple polygon... early out
		{
			gl.glBegin(GL_TRIANGLE_FAN);
			for (i = 0; i < npoints; i++) {
				j = slist[i];
				gl.glTexCoord2f((float) (px[j] * gux + py[j] * guy + guo), (float) (px[j] * gvx + py[j] * gvy + gvo));
				gl.glVertex2d(px[j], py[j]);
			}
			gl.glEnd();
			return;
		}

		// Sort points by y's
		for (gap = (npoints >> 1); gap != 0; gap >>= 1)
			for (i = 0; i < npoints - gap; i++)
				for (j = i; j >= 0; j -= gap) {
					if (py[npoint2[slist[j]]] <= py[npoint2[slist[j + gap]]])
						break;
					k = slist[j];
					slist[j] = slist[j + gap];
					slist[j + gap] = k;
				}

		numrst = 0;
		for (z = 0; z < npoints; z++) {
			i0 = slist[z];
			i1 = npoint2[i0];
			if (py[i0] == py[i1] || npoint2[i1] == -1)
				continue;
			i2 = i1;
			i3 = npoint2[i1];
			if (py[i1] == py[i3]) {
				i2 = i3;
				i3 = npoint2[i3];
			}

			// i0 i3
			// \ /
			// i1--i2
			// / \ ~
			// i0 i3

			if ((py[i1] < py[i0]) && (py[i2] < py[i3])) // Insert raster
			{
				for (i = numrst; i > 0; i--) {
					if (rst[i - 1].xi * (py[i1] - rst[i - 1].y) + rst[i - 1].x < px[i1])
						break;
					rst[i + 1].set(rst[i - 1]);
				}
				numrst += 2;
				if ((i & 1) != 0) // split inside area
				{
					j = i - 1;
					x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
					x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi + rst[j + 1].x;
					drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1, py[i1]);
					rst[j].x = x0;
					rst[j].y = py[i1];
					rst[j + 3].x = x1;
					rst[j + 3].y = py[i1];
				}

				m0 = (px[i0] - px[i1]) / (py[i0] - py[i1]);
				m1 = (px[i3] - px[i2]) / (py[i3] - py[i2]);

				j = ((px[i1] > px[i2] || (i1 == i2) && (m0 >= m1)) ? 1 : 0) + i;
				if (j < 0)
					continue;
				k = (i << 1) + 1 - j;

				rst[j].i = i0;
				rst[j].xi = m0;
				rst[j].x = px[i1];
				rst[j].y = py[i1];
				rst[k].i = i3;
				rst[k].xi = m1;
				rst[k].x = px[i2];
				rst[k].y = py[i2];
			} else {
				// NOTE:don't count backwards!
				if (i1 == i2) {
					for (i = 0; i < numrst; i++)
						if (rst[i].i == i1)
							break;
				} else {
					for (i = 0; i < numrst; i++)
						if ((rst[i].i == i1) || (rst[i].i == i2))
							break;
				}
				j = i & ~1;

				if ((py[i1] > py[i0]) && (py[i2] > py[i3])) // Delete raster
				{
					for (; j <= i + 1; j += 2) {
						x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
						if ((i == j) && (i1 == i2))
							x1 = x0;
						else
							x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi + rst[j + 1].x;
						drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1, py[i1]);
						rst[j].x = x0;
						rst[j].y = py[i1];
						rst[j + 1].x = x1;
						rst[j + 1].y = py[i1];
					}
					numrst -= 2;
					for (; i < numrst; i++)
						rst[i].set(rst[i + 2]);
				} else {
					x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
					x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi + rst[j + 1].x;

					drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1, py[i1]);
					rst[j].x = x0;
					rst[j].y = py[i1];
					rst[j + 1].x = x1;
					rst[j + 1].y = py[i1];

					if (py[i0] < py[i3]) {
						rst[i].x = px[i2];
						rst[i].y = py[i2];
						rst[i].i = i3;
					} else {
						rst[i].x = px[i1];
						rst[i].y = py[i1];
						rst[i].i = i0;
					}
					rst[i].xi = (px[rst[i].i] - rst[i].x) / (py[rst[i].i] - py[i1]);
				}

			}
		}
	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		if (font.type == FontType.Tilemap) {
			if (palookup[col] == null)
				col = 0;

			int nTile = (Integer) font.ptr;
			if (waloff[nTile] == null && engine.loadtile(nTile) == null)
				return;
		}

		Pthtyp pth = font.getGL(textureCache, col);
		if (pth == null)
			return;

		bindTexture(pth.glpic);

		setpolymost2dview();
		gl.glDisable(GL_FOG);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glDepthMask(GL_FALSE); // disable writing to the z-buffer

		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);

		float alpha = 1.0f, f = parent.getshadefactor(shade);
		if (bit == Transparent.Bit1)
			alpha = TRANSLUSCENT1;
		if (bit == Transparent.Bit2)
			alpha = TRANSLUSCENT2;

		if (font.type == FontType.Tilemap)
			gl.glColor4f(f, f, f, alpha);
		else
			gl.glColor4ub(curpalette.getRed(col), curpalette.getGreen(col), curpalette.getBlue(col),
					(int) (alpha * 255));
		

		int c = 0, line = 0;
		int x, y, yoffs;

		float txc = font.charsizx / (float) font.sizx, tx;
		float tyc = font.charsizy / (float) font.sizy, ty;
		
		gl.glBegin(GL_TRIANGLE_STRIP);

		int oxpos = xpos;
		while (c < text.length && text[c] != 0) {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * font.charsizx);
			}

			if (text[c] == '\r')
				text[c] = 0;

			tx = (text[c] % font.cols) / (float) font.cols;
			ty = (text[c] / font.cols) / (float) font.rows;

			yoffs = (int) (scale * line * font.charsizy);

			x = xpos + (int) (scale * font.charsizx);
			y = ypos + (int) (scale * font.charsizy);

			gl.glTexCoord2f(tx, ty);
			gl.glVertex2i(xpos, ypos + yoffs);
			gl.glTexCoord2f(tx, ty + tyc);
			gl.glVertex2i(xpos, y + yoffs);
			gl.glTexCoord2f(tx + txc, ty);
			gl.glVertex2i(x, ypos + yoffs);
			gl.glTexCoord2f(tx + txc, ty + tyc);
			gl.glVertex2i(x, y + yoffs);
			
			xpos += scale * font.charsizx;
			c++;
		}
		
		gl.glEnd();

		gl.glDepthMask(GL_TRUE); // re-enable writing to the z-buffer
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		int oxpos = xpos;
//		if (textureCache.isUseShader())
//			gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, polymosttext);

		setpolymost2dview();
		gl.glDisable(GL_FOG);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glDepthMask(GL_FALSE); // disable writing to the z-buffer

		if (backcol >= 0) {
			gl.glColor4ub(curpalette.getRed(backcol), curpalette.getGreen(backcol), curpalette.getBlue(backcol),
					255);
			int c = Bstrlen(text);

			gl.glBegin(GL_TRIANGLE_FAN);
			gl.glVertex2i(xpos, ypos);
			gl.glVertex2i(xpos, ypos + (fontsize != 0 ? 6 : 8));
			int x = xpos + (c << (3 - fontsize));
			int y = ypos + (fontsize != 0 ? 6 : 8);
			gl.glVertex2i(x, y);
			gl.glVertex2i(xpos + (c << (3 - fontsize)), ypos);
			gl.glEnd();
		}

		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);
		gl.glColor4ub(curpalette.getRed(col), curpalette.getGreen(col), curpalette.getBlue(col), 255);
		float txc = (fontsize != 0 ? (4.0f / 256.0f) : (8.0f / 256.0f));
		float tyc = (fontsize != 0 ? (6.0f / 128.0f) : (8.0f / 128.0f));

		gl.glBegin(GL_TRIANGLE_STRIP);

		int c = 0, line = 0;
		int x, y, yoffs;
		float tx, ty;

		while (c < text.length && text[c] != '\0') {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * (8 >> fontsize));
			}
			if (text[c] == '\r')
				text[c] = 0;

			tx = (text[c] % 32) / 32.0f;
			ty = ((text[c] / 32) + (fontsize * 8)) / 16.0f;

			yoffs = (int) (scale * line * (fontsize != 0 ? 6 : 8));

			x = xpos + (int) (scale * (8 >> fontsize));
			y = ypos + (int) (scale * (fontsize != 0 ? 6 : 8));

			gl.glTexCoord2f(tx, ty);
			gl.glVertex2i(xpos, ypos + yoffs);
			gl.glTexCoord2f(tx, ty + tyc);
			gl.glVertex2i(xpos, y + yoffs);
			gl.glTexCoord2f(tx + txc, ty);
			gl.glVertex2i(x, ypos + yoffs);
			gl.glTexCoord2f(tx + txc, ty + tyc);
			gl.glVertex2i(x, y + yoffs);

			xpos += scale * (8 >> fontsize);
			c++;
		}

		gl.glEnd();

		gl.glDepthMask(GL_TRUE); // re-enable writing to the z-buffer
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		gl.glDisable(GL_FOG);

		setpolymost2dview(); // JBF 20040205: more efficient setup

		col = palookup[0][col] & 0xFF;
		gl.glBegin(GL_LINES);
		gl.glColor4ub(curpalette.getRed(col), curpalette.getGreen(col), curpalette.getBlue(col), 255);
		gl.glVertex2f(x1 / 4096.0f, y1 / 4096.0f);
		gl.glVertex2f(x2 / 4096.0f, y2 / 4096.0f);
		gl.glEnd();
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

		if ((picanm[picnum] & 192) != 0)
			picnum += engine.animateoffs((short) picnum, (short) 0xc000);

		if ((tilesizx[picnum] <= 0) || (tilesizy[picnum] <= 0))
			return;

		if ((dastat & 128) == 0 || beforedrawrooms != 0)
			dorotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2, guniqhudid);
	}

	protected void dorotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat,
			int cx1, int cy1, int cx2, int cy2, int uniqid) {

		int ourxyaspect = xyaspect;
		if (GLSettings.useModels.get() && parent.defs != null && parent.defs.mdInfo.getHudInfo(picnum, dastat) != null && parent.defs.mdInfo.getHudInfo(picnum, dastat).angadd != 0) {
			Tile2model entry = parent.defs != null ? parent.defs.mdInfo.getParams(picnum) : null;
			if (entry != null && entry.model != null && entry.framenum >= 0) {
				dorotatesprite3d(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2, uniqid);
				return;
			}
		}

		short ogpicnum = globalpicnum;
		globalpicnum = (short) picnum;
		int ogshade = globalshade;
		globalshade = dashade;
		int ogpal = globalpal;
		globalpal = dapalnum & 0xFF;

		if ((dastat & 10) == 2)
			gl.glViewport(windowx1, ydim - (windowy2 + 1), windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
		else {
			gl.glViewport(0, 0, xdim, ydim);
			parent.glox1 = -1; // Force fullscreen (glox1=-1 forces it to restore)
		}

		gl.glMatrixMode(GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, xdim - 1, ydim - 1, 0, -1, 1);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glEnable(GL_TEXTURE_2D);

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

		int xsiz = tilesizx[globalpicnum];
		int ysiz = tilesizy[globalpicnum];

		int xoff = 0, yoff = 0;
		if ((dastat & 16) == 0) {
			xoff = (int) ((byte) ((picanm[globalpicnum] >> 8) & 255)) + (xsiz >> 1);
			yoff = (int) ((byte) ((picanm[globalpicnum] >> 16) & 255)) + (ysiz >> 1);
		}

		if ((dastat & 4) != 0)
			yoff = ysiz - yoff;

		if ((dastat & 2) == 0) {
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim)
				ourxyaspect = (10 << 16) / 12;
		} else {
			// dastat&2: Auto window size scaling
			int oxdim = xdim, zoomsc;
			int xdim = oxdim; // SHADOWS global

			int ouryxaspect = yxaspect;
			ourxyaspect = xyaspect;

			// screen center to s[xy], 320<<16 coords.
			int normxofs = sx - (320 << 15), normyofs = sy - (200 << 15);
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim) {
				xdim = (4 * ydim) / 3;

				ouryxaspect = (12 << 16) / 10;
				ourxyaspect = (10 << 16) / 12;
			}

			// nasty hacks go here
			if ((dastat & 8) == 0) {
				int twice_midcx = (cx1 + cx2) + 2;

				
				// screen x center to sx1, scaled to viewport
				int scaledxofs = scale(normxofs, scale(xdimen, xdim, oxdim), 320);
				int xbord = 0;
				if ((dastat & (256 | 512)) != 0) {
					xbord = scale(oxdim - xdim, twice_midcx, oxdim);
					if ((dastat & 512) == 0)
						xbord = -xbord;
				}

				sx = ((twice_midcx + xbord) << 15) + scaledxofs;
				zoomsc = xdimenscale;
				sy = (((cy1 + cy2) + 2) << 15) + mulscale(normyofs, zoomsc, 16);
			} else {
				// If not clipping to startmosts, & auto-scaling on, as a
				// hard-coded bonus, scale to full screen instead
				sx = (xdim << 15) + scale(normxofs, xdim, 320);

				if ((dastat & 512) != 0)
					sx += (oxdim - xdim) << 16;
				else if ((dastat & 256) == 0)
					sx += (oxdim - xdim) << 15;

				if ((dastat & RS_CENTERORIGIN) != 0)
					sx += oxdim << 15;

				zoomsc = scale(xdim, ouryxaspect, 320);
				sy = (ydim << 15) + mulscale(normyofs, zoomsc, 16);
			}

			z = mulscale(z, zoomsc, 16);
		}

		gl.glEnable(GL_CLIP_PLANE0);
		gl.glClipPlanef(GL_CLIP_PLANE0, 1, 0, 0, -cx1);
		gl.glEnable(GL_CLIP_PLANE0 + 1);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 1, -1, 0, 0, cx2);

		gl.glEnable(GL_CLIP_PLANE0 + 2);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 2, 0, 1, 0, -cy1);
		gl.glEnable(GL_CLIP_PLANE0 + 3);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 3, 0, -1, 0, cy2);

		float aspectFix = ((dastat & 2) != 0) || ((dastat & 8) == 0) ? ourxyaspect / 65536.0f : 1.0f;
		float scale = z / 65536.0f;
		float cx = sx / 65536.0f;
		float cy = sy / 65536.0f;
		gl.glTranslatef(cx, cy, 0);
		gl.glScalef(1, 1 / aspectFix, 0);
		gl.glRotatef(360.0f * a / 2048.0f, 0, 0, 1);
		gl.glScalef(scale * aspectFix, scale * aspectFix, 0);
		gl.glTranslatef(-xoff, -yoff, 0);
		gl.glScalef(xsiz, ysiz, 0);

		gl.glDisable(GL_FOG);
		drawrotate(method, dastat);

		gl.glDisable(GL_CLIP_PLANE0);
		gl.glDisable(GL_CLIP_PLANE0 + 1);
		gl.glDisable(GL_CLIP_PLANE0 + 2);
		gl.glDisable(GL_CLIP_PLANE0 + 3);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPopMatrix();

		globalpicnum = ogpicnum;
		globalshade = ogshade;
		globalpal = ogpal & 0xFF;
	}

	protected void drawrotate(int method, int dastat) {

		if (globalpicnum >= MAXTILES)
			globalpicnum = 0;
		if (palookup[globalpal] == null)
			globalpal = 0;

		engine.setgotpic(globalpicnum);
		int tsizx = tilesizx[globalpicnum];
		int tsizy = tilesizy[globalpicnum];

		if (waloff[globalpicnum] == null) {
			//
			engine.loadtile(globalpicnum);
			if (waloff[globalpicnum] == null) {
				tsizx = tsizy = 1;
				method = 1;
			}
		}

		Pthtyp pth = textureCache.cache(globalpicnum, globalpal, (short) 0, textureCache.clampingMode(method),
				textureCache.alphaMode(method));
		if (pth == null) // hires texture not found
			return;

		if (!pth.isHighTile()) {
//			textureCache.bindShader();
//			textureCache.setShaderParams(globalpal, engine.getpalookup(0, globalshade));
		}
		bindTexture(pth.glpic);

		float hackscx = 1.0f, hackscy = 1.0f;
		if (pth != null && pth.isHighTile()) {
			tsizx = pth.sizx;
			tsizy = pth.sizy;
		}

		float ox2 = hackscx / calcSize(tsizx);
		float oy2 = hackscy / calcSize(tsizy);

		gl.glMatrixMode(GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glScalef(tsizx, tsizy, 1.0f);
		gl.glScalef(ox2, oy2, 1.0f);

		// texture scale by parkar request
		if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)))
			gl.glScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);

		if ((dastat & 4) != 0) {
			gl.glScalef(1, -1, 1.0f);
			gl.glTranslatef(0, -1, 0);
		}

		if (((method & 3) == 0)) {
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL_ALPHA_TEST);
		} else {
			gl.glEnable(GL_BLEND);
			gl.glEnable(GL_ALPHA_TEST);
		}

		polyColor.r = polyColor.g = polyColor.b = parent.getshadefactor(globalshade);
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

		if (parent.defs != null) {
			if (pth != null && pth.isHighTile()) {
				if (pth.hicr.palnum != globalpal) {
					// apply tinting for replaced textures

					Palette p = parent.defs.texInfo.getTints(globalpal);
					polyColor.r *= p.r / 255.0f;
					polyColor.g *= p.g / 255.0f;
					polyColor.b *= p.b / 255.0f;
				}

				Palette pdetail = parent.defs.texInfo.getTints(MAXPALOOKUPS - 1);
				if (pdetail.r != 255 || pdetail.g != 255 || pdetail.b != 255) {
					polyColor.r *= pdetail.r / 255.0f;
					polyColor.g *= pdetail.g / 255.0f;
					polyColor.b *= pdetail.b / 255.0f;
				}
			}
		}

//		textureCache.shaderTransparent(polyColor.a);
		gl.glColor4f(polyColor.r, polyColor.g, polyColor.b, polyColor.a);

		gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL_VERTEX_ARRAY);

		gl.glTexCoordPointer(2, GL_FLOAT, 0, textures);
		gl.glVertexPointer(2, GL_FLOAT, 0, vertices);

		gl.glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

		gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL_VERTEX_ARRAY);

		gl.glMatrixMode(GL_TEXTURE);
		gl.glPopMatrix();

//		if (!pth.isHighTile())
//			textureCache.unbindShader();
	}

	private void dorotatesprite3d(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat,
			int cx1, int cy1, int cx2, int cy2, int uniqid) {
		int xoff = 0, yoff = 0, xsiz, ysiz;
		int ogshade, ogpal;

		int oldviewingrange;
		float x1, y1, z1;
		if (hudsprite == null)
			hudsprite = new SPRITE();
		hudsprite.reset((byte) 0);
		
		Hudtyp hudInfo = null;
		if (parent.defs == null || ((hudInfo = parent.defs.mdInfo.getHudInfo(picnum, dastat)) != null && (hudInfo.flags & 1) != 0))
			return; // "HIDE" is specified in DEF

		float ogchang = parent.gchang;
		parent.gchang = 1.0f;
		float ogshang = parent.gshang;
		parent.gshang = 0.0f;
		float d = z / (65536.0f * 16384.0f);
		float ogctang = parent.gctang;
		parent.gctang = (float) sintable[(a + 512) & 2047] * d;
		float ogstang = parent.gstang;
		parent.gstang = (float) sintable[a & 2047] * d;
		ogshade = (int) globalshade;
		globalshade = dashade;
		ogpal = globalpal;
		globalpal = dapalnum;
		double ogxyaspect = parent.gxyaspect;
		parent.gxyaspect = 1.0f;
		oldviewingrange = viewingrange;
		viewingrange = 65536;

		x1 = hudInfo.xadd;
		y1 = hudInfo.yadd;
		z1 = hudInfo.zadd;

		if ((hudInfo.flags & 2) == 0) // "NOBOB" is specified in DEF
		{
			float fx = (sx) * (1.0f / 65536.0f);
			float fy = (sy) * (1.0f / 65536.0f);

			if ((dastat & 16) != 0) {
				xsiz = tilesizx[picnum];
				ysiz = tilesizy[picnum];
				xoff = (int) ((byte) ((picanm[picnum] >> 8) & 255)) + (xsiz >> 1);
				yoff = (int) ((byte) ((picanm[picnum] >> 16) & 255)) + (ysiz >> 1);

				d = z / (65536.0f * 16384.0f);
				float cosang, sinang;
				float cosang2 = cosang = (float) sintable[(a + 512) & 2047] * d;
				float sinang2 = sinang = (float) sintable[a & 2047] * d;
				if ((dastat & 2) != 0 || ((dastat & 8) == 0)) // Don't aspect unscaled perms
				{
					d = (float) xyaspect / 65536.0f;
					cosang2 *= d;
					sinang2 *= d;
				}
				fx += -(double) xoff * cosang2 + (double) yoff * sinang2;
				fy += -(double) xoff * sinang - (double) yoff * cosang;
			}

			if ((dastat & 2) == 0) {
				x1 += fx / ((double) (xdim << 15)) - 1.0; // -1: left of screen, +1: right of screen
				y1 += fy / ((double) (ydim << 15)) - 1.0; // -1: top of screen, +1: bottom of screen
			} else {
				x1 += fx / 160.0 - 1.0; // -1: left of screen, +1: right of screen
				y1 += fy / 100.0 - 1.0; // -1: top of screen, +1: bottom of screen
			}
		}
		hudsprite.ang = (short) (hudInfo.angadd + globalang);

		if ((dastat & 4) != 0) {
			x1 = -x1;
			y1 = -y1;
		}

		hudsprite.xrepeat = hudsprite.yrepeat = 32;

		hudsprite.x = (int) (((double) parent.gcosang * z1 - (double) parent.gsinang * x1) * 16384.0 + globalposx);
		hudsprite.y = (int) (((double) parent.gsinang * z1 + (double) parent.gcosang * x1) * 16384.0 + globalposy);
		hudsprite.z = (int) (globalposz + y1 * 16384.0 * 0.8);

		hudsprite.picnum = (short) picnum;
		hudsprite.shade = (byte) dashade;
		hudsprite.pal = (short) dapalnum;
		hudsprite.owner = (short) (uniqid + MAXSPRITES);
		hudsprite.cstat = (short) ((dastat & 1) + ((dastat & 32) << 4) + ((dastat & 4) << 1));

		if ((dastat & 10) == 2)
			gl.glViewport(windowx1, ydim - (windowy2 + 1), windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
		else {
			gl.glViewport(0, 0, xdim, ydim);
			parent.glox1 = -1; // Force fullscreen (glox1=-1 forces it to restore)
		}

		gl.glMatrixMode(GL_PROJECTION);

		if ((dastat & 10) == 2) {
			float ratioratio = (float) xdim / ydim;
			parent.matrix[0][0] = (float) ydimen * (ratioratio >= 1.6f ? 1.2f : 1);
			parent.matrix[0][2] = 1.0f;
			parent.matrix[1][1] = (float) xdimen;
			parent.matrix[1][2] = 1.0f;
			parent.matrix[2][2] = 1.0f;
			parent.matrix[2][3] = (float) ydimen * (ratioratio >= 1.6f ? 1.2f : 1);
			parent.matrix[3][2] = -1.0f;
		} else {
			parent.matrix[0][0] = parent.matrix[2][3] = 1.0f;
			parent.matrix[1][1] = ((float) xdim) / ((float) ydim);
			parent.matrix[2][2] = 1.0001f;
			parent.matrix[3][2] = 1 - parent.matrix[2][2];
		}
		gl.glLoadMatrixf(parent.matrix);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();

		if ((hudInfo.flags & 8) != 0) // NODEPTH flag
			gl.glDisable(GL_DEPTH_TEST);
		else {
			gl.glEnable(GL_DEPTH_TEST);
			gl.glClear(GL_DEPTH_BUFFER_BIT);
		}

		gl.glDisable(GL_FOG);
		parent.globalorientation = hudsprite.cstat;
		parent.mddraw(hudsprite, 0, 0);

		viewingrange = oldviewingrange;
		parent.gxyaspect = ogxyaspect;
		globalshade = ogshade;
		globalpal = ogpal;
		parent.gchang = ogchang;
		parent.gshang = ogshang;
		parent.gctang = ogctang;
		parent.gstang = ogstang;
	}

	@Override
	public void init() {
		if (polymosttext == null) {
			// construct a 256x128 8-bit alpha-only texture for the font glyph
			// matrix
			byte[] tbuf;
			int tptr;
			int h, i, j;
			polymosttext = BufferUtils.newIntBuffer(1);

			tbuf = new byte[256 * 128];
			ByteBuffer fbuf = BufferUtils.newByteBuffer(256 * 128);

			for (h = 0; h < 256; h++) {
				tptr = (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 0; i < 8; i++) {
					for (j = 0; j < 8; j++) {
						if ((textfont[h * 8 + i] & pow2char[7 - j]) != 0)
							tbuf[tptr + j] = (byte) 255;
					}
					tptr += 256;
				}
			}

			for (h = 0; h < 256; h++) {
				tptr = 256 * 64 + (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 1; i < 7; i++) {
					for (j = 2; j < 6; j++) {
						if ((smalltextfont[h * 8 + i] & pow2char[7 - j]) != 0)
							tbuf[tptr + j - 2] = (byte) 255;
					}
					tptr += 256;
				}
			}

			fbuf.put(tbuf);
			fbuf.rewind();

			gl.glBindTexture(GL_TEXTURE_2D, polymosttext);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_INTENSITY, 256, 128, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, fbuf);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		}
	}

	@Override
	public void uninit() {
		if (polymosttext != null) {
			// polymosttext.dispose();
			gl.glDeleteTextures(1, polymosttext);
		}
		polymosttext = null;
	}

	class raster {
		float x, y, xi;
		int i;

		public void set(raster src) {
			this.x = src.x;
			this.y = src.y;
			this.xi = src.xi;
			this.i = src.i;
		}
	}

	@Override
	public void nextpage() {};
}
