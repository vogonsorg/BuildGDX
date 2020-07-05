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

import static java.lang.Math.abs;
import static ru.m210projects.Build.Engine.globalposx;
import static ru.m210projects.Build.Engine.globalposy;

import com.badlogic.gdx.math.Vector2;

public class PolyClipper {

	private class Most extends Surface {
		double spx;
		int spt;
	}

	private class vsptyp {
		double x, cy[] = new double[2], fy[] = new double[2];
		int n, p, tag, ctag, ftag;

		public void set(vsptyp src) {
			this.x = src.x;
			for (int i = 0; i < 2; i++)
				this.cy[i] = src.cy[i];
			for (int i = 0; i < 2; i++)
				this.fy[i] = src.fy[i];
			this.n = src.n;
			this.p = src.p;
			this.tag = src.tag;
			this.ctag = src.ctag;
			this.ftag = src.ftag;
		}
	};

	private int domostpolymethod = 0;
	private final float DOMOST_OFFSET = 0.01f;
	private int vcnt, gtag;
	private final int VSPMAX = 4096; // <- careful!
	private vsptyp[] vsp = new vsptyp[VSPMAX];
	private final Most[] domost = new Most[4];
	private final double[] domost_cy = new double[2], domost_cv = new double[2];
	private Polymost r;

	public PolyClipper(Polymost render) {
		for (int i = 0; i < 4; i++)
			domost[i] = new Most();
		for (int i = 0; i < VSPMAX; i++)
			vsp[i] = new vsptyp();
		this.r = render;
	}

	public void setMethod(int method) {
		domostpolymethod = method;
	}

	/*
	 * Init viewport boundary (must be 4 point convex loop): //
	 * (p[0].px,p[0].py).----.(p[1].px,p[1].py) // / \ // / \
	 * //(p[3].px,p[3].py).--------------.(p[2].px,p[2].py)
	 */
	public void initmosts(Surface[] p, int n) {
		int i, j, k, imin;

		vcnt = 1; // 0 is dummy solid node
		if (n < 3)
			return;
		imin = (p[1].px < p[0].px) ? 1 : 0;
		for (i = n - 1; i >= 2; i--)
			if (p[i].px < p[imin].px)
				imin = i;

		vsp[vcnt].x = p[imin].px;
		vsp[vcnt].cy[0] = vsp[vcnt].fy[0] = p[imin].py;
		vcnt++;

		i = imin + 1;
		if (i >= n)
			i = 0;
		j = imin - 1;
		if (j < 0)
			j = n - 1;

		do {
			if (p[i].px < p[j].px) {
				if ((vcnt > 1) && (p[i].px <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = p[i].px;
				vsp[vcnt].cy[0] = p[i].py;
				k = j + 1;
				if (k >= n)
					k = 0;
				vsp[vcnt].fy[0] = ((p[i].px - p[k].px) * (p[j].py - p[k].py) / (p[j].px - p[k].px) + p[k].py);
				vcnt++;
				i++;
				if (i >= n)
					i = 0;
			} else if (p[j].px < p[i].px) {
				if ((vcnt > 1) && (p[j].px <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = p[j].px;
				vsp[vcnt].fy[0] = p[j].py;
				k = i - 1;
				if (k < 0)
					k = n - 1;
				// (p[k].px,p[k].py)
				// (p[j].px,?)
				// (p[i].px,p[i].py)
				vsp[vcnt].cy[0] = ((p[j].px - p[k].px) * (p[i].py - p[k].py) / (p[i].px - p[k].px) + p[k].py);
				vcnt++;
				j--;
				if (j < 0)
					j = n - 1;
			} else {
				if ((vcnt > 1) && (p[i].px <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = p[i].px;
				vsp[vcnt].cy[0] = p[i].py;
				vsp[vcnt].fy[0] = p[j].py;
				vcnt++;
				i++;
				if (i >= n)
					i = 0;
				if (i == j)
					break;
				j--;
				if (j < 0)
					j = n - 1;
			}
		} while (i != j);

		if (p[i].px > vsp[vcnt - 1].x) {
			vsp[vcnt].x = p[i].px;
			vsp[vcnt].cy[0] = vsp[vcnt].fy[0] = p[i].py;
			vcnt++;
		}

		for (i = 0; i < vcnt; i++) {
			vsp[i].cy[1] = vsp[i + 1].cy[0];
			vsp[i].ctag = i;
			vsp[i].fy[1] = vsp[i + 1].fy[0];
			vsp[i].ftag = i;
			vsp[i].n = i + 1;
			vsp[i].p = i - 1;
		}
		vsp[vcnt - 1].n = 0;
		vsp[0].p = vcnt - 1;
		gtag = vcnt;

		// VSPMAX-1 is dummy empty node
		for (i = vcnt; i < VSPMAX; i++) {
			vsp[i].n = i + 1;
			vsp[i].p = i - 1;
		}

		vsp[VSPMAX - 1].n = vcnt;
		vsp[vcnt].p = VSPMAX - 1;
	}

	public void domost(double x0, double y0, double x1, double y1) {
		double d, f, n, t, slop, dx, dx0, dx1, nx, nx0, ny0, nx1, ny1;
		int i, j, k, z, ni, vcnt = 0, scnt, newi;

		boolean dir = (x0 < x1);

		if (dir) // clip dmost (floor)
		{
			y0 -= DOMOST_OFFSET;
			y1 -= DOMOST_OFFSET;
		} else // clip umost (ceiling)
		{
			if (x0 == x1)
				return;
			f = x0;
			x0 = x1;
			x1 = f;
			f = y0;
			y0 = y1;
			y1 = f;

			y0 += DOMOST_OFFSET;
			y1 += DOMOST_OFFSET; // necessary?
		}

		slop = (y1 - y0) / (x1 - x0);
		for (i = vsp[0].n; i != 0; i = newi) {
			newi = vsp[i].n;
			nx0 = vsp[i].x;
			nx1 = vsp[newi].x;
			if ((x0 >= nx1) || (nx0 >= x1) || (vsp[i].ctag <= 0))
				continue;
			dx = nx1 - nx0;
			domost_cy[0] = vsp[i].cy[0];
			domost_cv[0] = vsp[i].cy[1] - domost_cy[0];
			domost_cy[1] = vsp[i].fy[0];
			domost_cv[1] = vsp[i].fy[1] - domost_cy[1];

			scnt = 0;

			// Test if left edge requires split (x0,y0) (nx0,cy(0)),<dx,cv(0)>
			if ((x0 > nx0) && (x0 < nx1)) {
				t = (x0 - nx0) * domost_cv[dir ? 1 : 0] - (y0 - domost_cy[dir ? 1 : 0]) * dx;
				if (((!dir) && (t < 0)) || ((dir) && (t > 0))) {
					domost[scnt].spx = x0;
					domost[scnt].spt = -1;
					scnt++;
				}
			}

			// Test for intersection on umost (j == 0) and dmost (j == 1)
			for (j = 0; j < 2; j++) {
				d = (y0 - y1) * dx - (x0 - x1) * domost_cv[j];
				n = (y0 - domost_cy[j]) * dx - (x0 - nx0) * domost_cv[j];
				if ((abs(n) <= abs(d)) && (d * n >= 0) && (d != 0)) {
					t = n / d;
					nx = (x1 - x0) * t + x0;
					if ((nx > nx0) && (nx < nx1)) {
						domost[scnt].spx = nx;
						domost[scnt].spt = j;
						scnt++;
					}
				}
			}

			// Nice hack to avoid full sort later :)
			if ((scnt >= 2) && (domost[scnt - 1].spx < domost[scnt - 2].spx)) {
				f = domost[scnt - 1].spx;
				domost[scnt - 1].spx = domost[scnt - 2].spx;
				domost[scnt - 2].spx = f;
				j = domost[scnt - 1].spt;
				domost[scnt - 1].spt = domost[scnt - 2].spt;
				domost[scnt - 2].spt = j;
			}

			// Test if right edge requires split
			if ((x1 > nx0) && (x1 < nx1)) {
				t = (x1 - nx0) * domost_cv[dir ? 1 : 0] - (y1 - domost_cy[dir ? 1 : 0]) * dx;
				if (((!dir) && (t < 0)) || ((dir) && (t > 0))) {
					domost[scnt].spx = x1;
					domost[scnt].spt = -1;
					scnt++;
				}
			}

			vsp[i].tag = vsp[newi].tag = -1;
			for (z = 0; z <= scnt; z++, i = vcnt) {
				if (z < scnt) {
					vcnt = vsinsaft(i);
					t = (domost[z].spx - nx0) / dx;
					vsp[i].cy[1] = t * domost_cv[0] + domost_cy[0];
					vsp[i].fy[1] = t * domost_cv[1] + domost_cy[1];
					vsp[vcnt].x = domost[z].spx;
					vsp[vcnt].cy[0] = vsp[i].cy[1];
					vsp[vcnt].fy[0] = vsp[i].fy[1];
					vsp[vcnt].tag = domost[z].spt;
				}

				ni = vsp[i].n;
				if (ni == 0)
					continue; // this 'if' fixes many bugs!
				dx0 = vsp[i].x;
				if (x0 > dx0)
					continue;
				dx1 = vsp[ni].x;
				if (x1 < dx1)
					continue;
				ny0 = (dx0 - x0) * slop + y0;
				ny1 = (dx1 - x0) * slop + y0;

				// dx0 dx1
				// ~ ~
				// ----------------------------
				// t0+=0 t1+=0
				// vsp[i].cy[0] vsp[i].cy[1]
				// ============================
				// t0+=1 t1+=3
				// ============================
				// vsp[i].fy[0] vsp[i].fy[1]
				// t0+=2 t1+=6
				//
				// ny0 ? ny1 ?

				k = 4;
				if ((vsp[i].tag == 0) || (ny0 <= vsp[i].cy[0] + DOMOST_OFFSET))
					k--;
				if ((vsp[i].tag == 1) || (ny0 >= vsp[i].fy[0] - DOMOST_OFFSET))
					k++;
				if ((vsp[ni].tag == 0) || (ny1 <= vsp[i].cy[1] + DOMOST_OFFSET))
					k -= 3;
				if ((vsp[ni].tag == 1) || (ny1 >= vsp[i].fy[1] - DOMOST_OFFSET))
					k += 3;

				if (!dir) {
					switch (k) {
					case 1:
					case 2:
						domost[0].px = dx0;
						domost[0].py = vsp[i].cy[0];
						domost[1].px = dx1;
						domost[1].py = vsp[i].cy[1];
						domost[2].px = dx0;
						domost[2].py = ny0;
						if (domostpolymethod != -1) {
							vsp[i].cy[0] = ny0;
							vsp[i].ctag = gtag;
							r.drawpoly(domost, 3, domostpolymethod);
						}
						break;
					case 3:
					case 6:
						domost[0].px = dx0;
						domost[0].py = vsp[i].cy[0];
						domost[1].px = dx1;
						domost[1].py = vsp[i].cy[1];
						domost[2].px = dx1;
						domost[2].py = ny1;
						if (domostpolymethod != -1) {
							r.drawpoly(domost, 3, domostpolymethod);
							vsp[i].cy[1] = ny1;
							vsp[i].ctag = gtag;
						}
						break;
					case 4:
					case 5:
					case 7:
						domost[0].px = dx0;
						domost[0].py = vsp[i].cy[0];
						domost[1].px = dx1;
						domost[1].py = vsp[i].cy[1];
						domost[2].px = dx1;
						domost[2].py = ny1;
						domost[3].px = dx0;
						domost[3].py = ny0;
						if (domostpolymethod != -1) {
							vsp[i].cy[0] = ny0;
							vsp[i].cy[1] = ny1;
							vsp[i].ctag = gtag;
							r.drawpoly(domost, 4, domostpolymethod);
						}
						break;
					case 8:
						domost[0].px = dx0;
						domost[0].py = vsp[i].cy[0];
						domost[1].px = dx1;
						domost[1].py = vsp[i].cy[1];
						domost[2].px = dx1;
						domost[2].py = vsp[i].fy[1];
						domost[3].px = dx0;
						domost[3].py = vsp[i].fy[0];
						if (domostpolymethod != -1) {
							vsp[i].ctag = vsp[i].ftag = -1;
							r.drawpoly(domost, 4, domostpolymethod);
						}
						break;
					default:
						break;
					}
				} else {
					switch (k) {
					case 7:
					case 6:
						domost[0].px = dx0;
						domost[0].py = ny0;
						domost[1].px = dx1;
						domost[1].py = vsp[i].fy[1];
						domost[2].px = dx0;
						domost[2].py = vsp[i].fy[0];
						if (domostpolymethod != -1) {
							vsp[i].fy[0] = ny0;
							vsp[i].ftag = gtag;
							r.drawpoly(domost, 3, domostpolymethod);
						}
						break;
					case 5:
					case 2:
						domost[0].px = dx0;
						domost[0].py = vsp[i].fy[0];
						domost[1].px = dx1;
						domost[1].py = ny1;
						domost[2].px = dx1;
						domost[2].py = vsp[i].fy[1];
						if (domostpolymethod != -1) {
							vsp[i].fy[1] = ny1;
							vsp[i].ftag = gtag;
							r.drawpoly(domost, 3, domostpolymethod);
						}
						break;
					case 4:
					case 3:
					case 1:
						domost[0].px = dx0;
						domost[0].py = ny0;
						domost[1].px = dx1;
						domost[1].py = ny1;
						domost[2].px = dx1;
						domost[2].py = vsp[i].fy[1];
						domost[3].px = dx0;
						domost[3].py = vsp[i].fy[0];
						if (domostpolymethod != -1) {
							vsp[i].fy[0] = ny0;
							vsp[i].fy[1] = ny1;
							vsp[i].ftag = gtag;
							r.drawpoly(domost, 4, domostpolymethod);
						}
						break;
					case 0:
						domost[0].px = dx0;
						domost[0].py = vsp[i].cy[0];
						domost[1].px = dx1;
						domost[1].py = vsp[i].cy[1];
						domost[2].px = dx1;
						domost[2].py = vsp[i].fy[1];
						domost[3].px = dx0;
						domost[3].py = vsp[i].fy[0];
						if (domostpolymethod != -1) {
							vsp[i].ctag = vsp[i].ftag = -1;
							r.drawpoly(domost, 4, domostpolymethod);
						}
						break;
					default:
						break;
					}
				}
			}
		}

		gtag++;

		// Combine neighboring vertical strips with matching collinear
		// top&bottom edges
		// This prevents x-splits from propagating through the entire scan

		i = vsp[0].n;
		while (i != 0) {
			ni = vsp[i].n;
			if ((vsp[i].cy[0] >= vsp[i].fy[0]) && (vsp[i].cy[1] >= vsp[i].fy[1])) {
				vsp[i].ctag = vsp[i].ftag = -1;
			}
			if ((vsp[i].ctag == vsp[ni].ctag) && (vsp[i].ftag == vsp[ni].ftag)) {
				vsp[i].cy[1] = vsp[ni].cy[1];
				vsp[i].fy[1] = vsp[ni].fy[1];
				vsdel(ni);
			} else
				i = ni;
		}
	}

	public int testvisiblemost(double x0, double x1) {
		int i, newi;
		for (i = vsp[0].n; i != 0; i = newi) {
			newi = vsp[i].n;
			if ((x0 < vsp[newi].x) && (vsp[i].x < x1) && (vsp[i].ctag >= 0))
				return (1);
		}
		return (0);
	}

	private void vsdel(int i) {
		int pi, ni;
		// Delete i
		pi = vsp[i].p;
		ni = vsp[i].n;
		vsp[ni].p = pi;
		vsp[pi].n = ni;

		// Add i to empty list
		vsp[i].n = vsp[VSPMAX - 1].n;
		vsp[i].p = VSPMAX - 1;
		vsp[vsp[VSPMAX - 1].n].p = i;
		vsp[VSPMAX - 1].n = i;
	}

	private int vsinsaft(int i) {
		int r;
		// i = next element from empty list
		r = vsp[VSPMAX - 1].n;
		vsp[vsp[r].n].p = VSPMAX - 1;
		vsp[VSPMAX - 1].n = vsp[r].n;

		vsp[r].set(vsp[i]); // copy i to r

		// insert r after i
		vsp[r].p = i;
		vsp[r].n = vsp[i].n;
		vsp[vsp[i].n].p = r;
		vsp[i].n = r;

		return (r);
	}

	private Vector2 projPoint = new Vector2();
	public float SCISDIST = 1.0f; // 1.0: Close plane clipping distance

	public float t0, t1;
	public float ryp0, ryp1;
	public float x0, y0, x1, y1;
	public float scrx0, scrx1;

	private float opyr0, opyr1;
	private float ot0, ot1;
	private float oscrx0, oscrx1;
	private float ox0, oy0, ox1, oy1;

	private double[] tx = new double[8];
	private double[] ty = new double[8];
	private double[] tz = new double[8];

	public boolean apply(Polymost par, boolean relative, float x1, float y1, float x2, float y2, boolean one_side) {
		// Offset&Rotate 3D coordinates to screen 3D space
		Vector2 p0 = projToScreen(par, x1, y1, relative);
		float xp0 = p0.x, yp0 = p0.y, oxp0 = p0.x, oyp0 = p0.y;
		Vector2 p1 = projToScreen(par, x2, y2, relative);
		float xp1 = p1.x, yp1 = p1.y;

		this.t0 = 0.0f;
		this.t1 = 1.0f;

		this.x0 = x1;
		this.y0 = y1;
		this.x1 = x2;
		this.y1 = y2;

		// Clip to close parallel-screen plane
		if (yp0 < SCISDIST) {
			if (yp1 < SCISDIST)
				return false;

			this.t0 = (SCISDIST - yp0) / (yp1 - yp0);
			xp0 = (xp1 - xp0) * t0 + xp0;
			yp0 = SCISDIST;
			this.x0 = (x2 - x1) * t0 + x1;
			this.y0 = (y2 - y1) * t0 + y1;
		}

		if (yp1 < SCISDIST) {
			this.t1 = (SCISDIST - oyp0) / (yp1 - oyp0);
			xp1 = (xp1 - oxp0) * t1 + oxp0;
			yp1 = SCISDIST;
			this.x1 = (x2 - x1) * t1 + x1;
			this.y1 = (y2 - y1) * t1 + y1;
		}

		this.ryp0 = 1.f / yp0;
		this.ryp1 = 1.f / yp1;

		// Generate screen coordinates for front side of wall
		scrx0 = par.ghalfx * xp0 * ryp0 + par.ghalfx;
		scrx1 = par.ghalfx * xp1 * ryp1 + par.ghalfx;
		if (one_side && scrx1 <= scrx0)
			return false;

		return true;
	}

	public boolean apply(Polymost par, float x1, float y1) {
		// Offset&Rotate 3D coordinates to screen 3D space
		Vector2 p0 = projToScreen(par, x1, y1, true);
		float xp0 = p0.x, yp0 = p0.y;

		this.t0 = 0.0f;
		this.t1 = 1.0f;

		this.x0 = x1;
		this.y0 = y1;
		this.x1 = 0;
		this.y1 = 0;

		// Clip to close parallel-screen plane
		if (yp0 <= SCISDIST)
			return false;

		this.ryp0 = 1.f / yp0;

		// Generate screen coordinates for front side of wall
		scrx0 = par.ghalfx * xp0 * ryp0 + par.ghalfx;

		return true;
	}

	public int apply(Polymost pol, int n, double[] px, double[] py, double[] pz, Surface[] screen) {
		// Clip to SCISDIST plane
		int n2 = 0;
		for (int i = 0; i < n; i++) {
			int j = i + 1;
			if (j >= n)
				j = 0;

			if (pz[i] >= SCISDIST) {
				tx[n2] = px[i];
				ty[n2] = py[i];
				tz[n2] = pz[i];
				n2++;
			}

			if ((pz[i] >= SCISDIST) != (pz[j] >= SCISDIST)) {
				double r = (SCISDIST - pz[i]) / (pz[j] - pz[i]);
				tx[n2] = (px[j] - px[i]) * r + px[i];
				ty[n2] = (py[j] - py[i]) * r + py[i];
				tz[n2] = SCISDIST;
				n2++;
			}
		}

		if (n2 < 3)
			return 0;

		// Project rotated 3D points to screen
		for (int i = 0; i < n2; i++) {
			double r = pol.ghalfx / tz[i];
			screen[i].px = tx[i] * r + pol.ghalfx;
			screen[i].py = ty[i] * r + pol.ghoriz;
		}

		return n2;
	}

	public int apply(Polymost pol, int n, Surface[] p, double pz, Surface[] screen) {
		// Clip to SCISDIST plane
		int n2 = 0;
		for (int i = 0; i < n; i++) {
			int j = i + 1;
			if (j >= n)
				j = 0;

			if (p[i].py >= SCISDIST) {
				tx[n2] = p[i].px;
				ty[n2] = p[i].py;
				n2++;
			}

			if ((p[i].py >= SCISDIST) != (p[j].py >= SCISDIST)) {
				double r = (SCISDIST - p[i].py) / (p[j].py - p[i].py);
				tx[n2] = (p[j].px - p[i].px) * r + p[i].px;
				ty[n2] = (p[j].py - p[i].py) * r + p[i].py;
				n2++;
			}
		}

		if (n2 < 3)
			return 0;

		// Project rotated 3D points to screen
		for (int j = 0; j < n2; j++) {
			double r = 1.0 / ty[j];
			screen[j].px = pol.ghalfx * tx[j] * r + pol.ghalfx;
			screen[j].py = pz * r + pol.ghoriz;
		}

		return n2;
	}

	public Vector2 projToScreen(Polymost par, float x, float y, boolean relative) {
		if (relative) {
			x -= globalposx;
			y -= globalposy;
		}
		projPoint.set(y * par.gcosang - x * par.gsinang, x * par.gcosang2 + y * par.gsinang2);

		return projPoint;
	}

	public void pop() {
		ox0 = x0;
		oy0 = y0;
		ox1 = x1;
		oy1 = y1;
		opyr0 = ryp0;
		opyr1 = ryp1;
		ot0 = t0;
		ot1 = t1;
		oscrx0 = scrx0;
		oscrx1 = scrx1;
	}

	public void push() {
		x0 = ox0;
		y0 = oy0;
		x1 = ox1;
		y1 = oy1;
		ryp0 = opyr0;
		ryp1 = opyr1;
		t0 = ot0;
		t1 = ot1;
		scrx0 = oscrx0;
		scrx1 = oscrx1;
	}

}
