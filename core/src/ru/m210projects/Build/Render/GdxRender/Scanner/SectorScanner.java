package ru.m210projects.Build.Render.GdxRender.Scanner;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Engine.MAXSECTORS;
import static ru.m210projects.Build.Engine.MAXSPRITESONSCREEN;
import static ru.m210projects.Build.Engine.MAXWALLS;
import static ru.m210projects.Build.Engine.globalposx;
import static ru.m210projects.Build.Engine.globalposy;
import static ru.m210projects.Build.Engine.globalposz;
import static ru.m210projects.Build.Engine.headspritesect;
import static ru.m210projects.Build.Engine.nextspritesect;
import static ru.m210projects.Build.Engine.pow2char;
import static ru.m210projects.Build.Engine.sector;
import static ru.m210projects.Build.Engine.showinvisibility;
import static ru.m210projects.Build.Engine.sintable;
import static ru.m210projects.Build.Engine.sprite;
import static ru.m210projects.Build.Engine.wall;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Pragmas;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;
import ru.m210projects.Build.Render.GdxRender.BuildCamera;
import ru.m210projects.Build.Render.GdxRender.Pool;
import ru.m210projects.Build.Render.GdxRender.Tesselator.Vertex;
import ru.m210projects.Build.Render.GdxRender.WorldMesh;
import ru.m210projects.Build.Render.GdxRender.WorldMesh.Heinum;

public abstract class SectorScanner {

	private Pool<WallFrustum3d> pFrustumPool = new Pool<WallFrustum3d>() {
		@Override
		protected WallFrustum3d newObject() {
			return new WallFrustum3d();
		}
	};

	private Pool<VisibleSector> pSectorPool = new Pool<VisibleSector>() {
		@Override
		protected VisibleSector newObject() {
			return new VisibleSector();
		}
	};

	private Vector2 projPoint = new Vector2();

	private PotentiallyVisibleSet pvs;

	private ArrayList<VisibleSector> sectors;
	private WallFrustum3d[] portqueue; // to linkedlist
	private final int queuemask; // pay attention!
	private int pqhead, pqtail;

	private VisibleSector[] handled;
	private WallFrustum3d[] gotviewport;
	private WallFrustum3d[] skyviewport;
	private byte[] gotwall;
	private byte[] wallflags;
	private Engine engine;

//	public SPRITE[] tsprite;
//	public int spritesortcnt;
	public Integer[] maskwall = new Integer[MAXWALLS]; // XXX memory leak
	public int maskwallcnt;

	private int skyCeilingPic, skyCeilingPal;
	private int skyFloorPic, skyFloorPal;

	private PolygonClipper cl = new PolygonClipper();

	public SectorScanner(Engine engine) {
		this.engine = engine;
		pvs = new PotentiallyVisibleSet();

		sectors = new ArrayList<VisibleSector>();
		portqueue = new WallFrustum3d[512];
		queuemask = portqueue.length - 1;
		tsprite = new SPRITE[MAXSPRITESONSCREEN + 1];

		gotviewport = new WallFrustum3d[MAXSECTORS];
		skyviewport = new WallFrustum3d[MAXSECTORS];
		handled = new VisibleSector[MAXSECTORS];
		gotwall = new byte[MAXWALLS >> 3];
		wallflags = new byte[MAXWALLS];
	}

	public void init() {
		pvs.info.init(engine);
	}

	public ArrayList<VisibleSector> process(BuildCamera cam, WorldMesh mesh, int sectnum) {
		if (!Gameutils.isValidSector(sectnum))
			return sectors;

		pvs.process(cam, mesh, sectnum);

		Arrays.fill(gotviewport, null);
		Gameutils.fill(gotwall, (byte) 0);
		Gameutils.fill(wallflags, (byte) 0);
		Arrays.fill(handled, null);

		skyCeilingPic = skyCeilingPal = skyFloorPic = skyFloorPal = -1;

		maskwallcnt = 0;
		spritesortcnt = 0;

		sectors.clear();
		pqhead = pqtail = 0;
		pSectorPool.reset();
		pFrustumPool.reset();

		int cursectnum = sectnum;
		portqueue[(pqtail++) & queuemask] = pFrustumPool.obtain().set(cam, sectnum);
		WallFrustum3d pFrustum = portqueue[pqhead];
		gotviewport[sectnum] = pFrustum;

		while (pqhead != pqtail) {
			sectnum = pFrustum.sectnum;

			VisibleSector sec = handled[sectnum];
			if (handled[sectnum] == null)
				sec = pSectorPool.obtain().set(sectnum);

			if (!pFrustum.handled) {
				pFrustum.handled = true;

				int startwall = sector[sectnum].wallptr;
				int endwall = sector[sectnum].wallnum + startwall;
				for (int z = startwall; z < endwall; z++) {
					WALL wal = wall[z];
					if (!pvs.checkWall(z))
						continue;

					int nextsectnum = wal.nextsector;
					if (pFrustum.wallInFrustum(mesh.getPoints(Heinum.Max, sectnum, z))) {
						gotwall[z >> 3] |= pow2char[z & 7];
						if (nextsectnum != -1) {
							if (!checkWallRange(nextsectnum, wal.nextwall)) {
								int theline = wal.nextwall;
								int gap = (numsectors >> 1);
								short i = (short) gap;
								while (gap > 1) {
									gap >>= 1;
									if (sector[i].wallptr < theline)
										i += gap;
									else
										i -= gap;
								}
								while (sector[i].wallptr > theline)
									i--;
								while (sector[i].wallptr + sector[i].wallnum <= theline)
									i++;
								nextsectnum = i;

								System.err.println("Error on " + i);
								wal.nextsector = i; // XXX
							}

							if (pFrustum.wallInFrustum(mesh.getPoints(Heinum.Lower, sectnum, z)))
								wallflags[z] |= 1;
							if (pFrustum.wallInFrustum(mesh.getPoints(Heinum.Upper, sectnum, z)))
								wallflags[z] |= 2;

							if (!pvs.checkSector(nextsectnum))
								continue;

							WallFrustum3d portal = null;
							if ((((sector[sectnum].ceilingstat & sector[nextsectnum].ceilingstat) & 1) != 0)
									|| (((sector[sectnum].floorstat & sector[nextsectnum].floorstat) & 1) != 0)) {
								portal = pFrustum.clone(pFrustumPool);
								portal.sectnum = nextsectnum;
							} else {
								// Handle the next portal
								ArrayList<Vertex> points;
								if ((points = mesh.getPoints(Heinum.Portal, sectnum, z)) == null)
									continue;

								WallFrustum3d clip = null;
								boolean bNearPlaneClipped;
								if (bNearPlaneClipped = NearPlaneCheck(cam, points)) {
									float posx = globalposx;
									float posy = globalposy;

									if ((sector[sectnum].isParallaxCeiling()) || (sector[sectnum].isParallaxFloor())
											|| (projectionToWall(posx, posy, wal, projPoint)
													&& Math.abs(posx - projPoint.x) + Math
															.abs(posy - projPoint.y) <= cam.near * cam.xscale * 2)) {
										clip = pFrustum.clone(pFrustumPool);
										clip.sectnum = nextsectnum;
									}
								}

//								int len = length;
								if ((sectnum == cursectnum || bNearPlaneClipped) && clip == null) {
									points = cl.ClipPolygon(cam.frustum, points);
									if (points.size() < 3)
										continue;

//									len = cl.getSize();
//									if (len < 3)
//										continue;
								}

								if (wal.isOneWay() && clip == null)
									continue;

								portal = clip != null ? clip : pFrustum.build(cam, pFrustumPool, points, nextsectnum);
							}

							if (portal != null) { // is in frustum
								wallflags[z] |= 4;
								if (gotviewport[nextsectnum] == null) {
									portqueue[(pqtail++) & queuemask] = (gotviewport[nextsectnum] = portal);
								} else {
									WallFrustum3d nextp = gotviewport[nextsectnum];
									if ((nextp = nextp.expand(portal)) != null) {
										if (handled[nextsectnum] != null) {
											portqueue[(pqtail++) & queuemask] = nextp;
										}
									}
								}
							}
						}
					}
				}
			}

			if (handled[sectnum] == null)
				handled[sectnum] = sec;

			if (pFrustum.next != null)
				pFrustum = pFrustum.next;
			else
				pFrustum = portqueue[(++pqhead) & queuemask];
		}

		pqhead = pqtail = 0;
		sectnum = cursectnum;
		portqueue[(pqtail++) & queuemask] = gotviewport[cursectnum];
		skyviewport[cursectnum] = gotviewport[cursectnum];
		gotviewport[cursectnum] = null;

		do {
			pFrustum = portqueue[(pqhead++) & queuemask];
			sectnum = pFrustum.sectnum;
			VisibleSector sec = handled[sectnum];

			boolean isParallaxCeiling = sector[sectnum].isParallaxCeiling();
			boolean isParallaxFloor = sector[sectnum].isParallaxFloor();

			int startwall = sector[sectnum].wallptr;
			int endwall = sector[sectnum].wallnum + startwall;
			for (int z = startwall; z < endwall; z++) {
				WALL wal = wall[z];
				int nextsectnum = wal.nextsector;

				if ((gotwall[z >> 3] & pow2char[z & 7]) == 0)
					continue;

				if (nextsectnum != -1) {
					if (gotviewport[nextsectnum] != null) {
						portqueue[(pqtail++) & queuemask] = gotviewport[nextsectnum];
						skyviewport[nextsectnum] = gotviewport[nextsectnum];
						gotviewport[nextsectnum] = null;
					}
				}
				if (wal.isMasked() || wal.isOneWay())
					maskwall[maskwallcnt++] = z;

				if ((isParallaxFloor && pFrustum.wallInFrustum(mesh.getPoints(Heinum.SkyLower, sectnum, z)))
						|| (isParallaxCeiling && pFrustum.wallInFrustum(mesh.getPoints(Heinum.SkyUpper, sectnum, z)))) {

					if (isParallaxCeiling) {
						skyCeilingPic = sector[sectnum].ceilingpicnum;
						skyCeilingPal = sector[sectnum].ceilingpal;
					}

					if (isParallaxFloor) {
						skyFloorPic = sector[sectnum].floorpicnum;
						skyFloorPal = sector[sectnum].floorpal;
					}

					sec.skywalls.add(z);
				}

				sec.walls.add(z);
				sec.wallflags.add(wallflags[z]);
			}

			startwall = sector[sectnum].wallptr;
			endwall = sector[sectnum].wallnum + startwall;
			byte secflags = 0;
			if (!isParallaxFloor && isSectorVisible(pFrustum, cam.frustum.planes[0], true, sectnum))
				secflags |= 1;
			if (!isParallaxCeiling && isSectorVisible(pFrustum, cam.frustum.planes[0], false, sectnum))
				secflags |= 2;

			checkSprites(pFrustum, sectnum);

			sec.secflags = secflags;
//			sec.setBounds(pFrustum.getBounds());
			sec.x1 = sec.y1 = 0;
			sec.x2 = xdim;
			sec.y2 = ydim;
			sectors.add(sec);
		} while (pqhead != pqtail);

		// Arrays.sort(maskwall, 0, maskwallcnt, comp); // masks sort TODO
		return sectors;
	}

	public int getSkyPal(Heinum h) {
		if (h == Heinum.SkyLower)
			return skyFloorPal;
		return skyCeilingPal;
	}

	public int getSkyPicnum(Heinum h) {
		if (h == Heinum.SkyLower)
			return skyFloorPic;
		return skyCeilingPic;
	}

	private boolean checkWallRange(int sectnum, int z) {
		return z >= sector[sectnum].wallptr && z < (sector[sectnum].wallptr + sector[sectnum].wallnum);
	}

	private void checkSprites(WallFrustum3d pFrustum, int sectnum) {
		for (int z = headspritesect[sectnum]; z >= 0; z = nextspritesect[z]) {
			SPRITE spr = sprite[z];

			if ((((spr.cstat & 0x8000) == 0) || showinvisibility) && (spr.xrepeat > 0) && (spr.yrepeat > 0)
					&& (spritesortcnt < MAXSPRITESONSCREEN)) {
				int xs = spr.x - globalposx;
				int ys = spr.y - globalposy;
				if ((spr.cstat & (64 + 48)) != (64 + 16) || Pragmas.dmulscale(sintable[(spr.ang + 512) & 2047], -xs,
						sintable[spr.ang & 2047], -ys, 6) > 0) {
					if (spriteInFrustum(pFrustum, spr)) {
						SPRITE tspr = addTSprite();
						tspr.set(spr);
						tspr.owner = (short) z;
					}
				}
			}
		}
	}

	private static Vector3[] tmpVec = { new Vector3(), new Vector3(), new Vector3(), new Vector3() };

	public boolean spriteInFrustum(WallFrustum3d frustum, SPRITE tspr) {
		Vector3[] points = tmpVec;
		float SIZEX = 0.5f;
		float SIZEY = 1.0f;

		Matrix4 mat = getSpriteMatrix(tspr);
		if (mat != null) {
			points[0].set(SIZEX, 0, -SIZEY).mul(mat);
			points[1].set(SIZEX, 0, 0).mul(mat);
			points[2].set(-SIZEX, 0, 0).mul(mat);
			points[3].set(-SIZEX, 0, -SIZEY).mul(mat);

			WallFrustum3d n = frustum;
			do {
				if (n.wallInFrustum(points, 4))
					return true;
				n = n.next;
			} while (n != null);
		}

		return false;
	}

	protected abstract Matrix4 getSpriteMatrix(SPRITE tspr);

	private SPRITE addTSprite() {
		if (tsprite[spritesortcnt] == null)
			tsprite[spritesortcnt] = new SPRITE();
		return tsprite[spritesortcnt++];
	}

	private boolean isSectorVisible(WallFrustum3d frustum, Plane near, boolean isFloor, int sectnum) {
		frustum.rebuild();

		Plane: for (int i = near == null ? 0 : -1; i < frustum.planes.length; i++) {
			Plane plane = (i == -1) ? near : frustum.planes[i];

			int startwall = sector[sectnum].wallptr;
			int endwall = sector[sectnum].wallnum + startwall;
			for (int z = startwall; z < endwall; z++) {
				WALL wal = wall[z];
				int wz = isFloor ? engine.getflorzofslope((short) sectnum, wal.x, wal.y)
						: engine.getceilzofslope((short) sectnum, wal.x, wal.y);

				if ((isFloor && !sector[sectnum].isSlopedFloor() && globalposz > wz)
						|| (!isFloor && !sector[sectnum].isSlopedCeiling() && globalposz < wz))
					continue;

				if (plane.testPoint(wal.x, wal.y, wz) != PlaneSide.Back)
					continue Plane;
			}

			if (frustum.next != null)
				return isSectorVisible(frustum.next, null, isFloor, sectnum);

			return false;
		}
		return true;
	}

	private boolean NearPlaneCheck(BuildCamera cam, ArrayList<? extends Vector3> points) {
		Plane near = cam.frustum.planes[0];
		for (int i = 0; i < points.size(); i++) {
			if (near.testPoint(points.get(i)) == PlaneSide.Back)
				return true;
		}
		return false;
	}

	public boolean projectionToWall(float posx, float posy, WALL w, Vector2 n) {
		WALL p2 = wall[w.point2];
		int dx = p2.x - w.x;
		int dy = p2.y - w.y;

		float i = dx * (posx - w.x) + dy * (posy - w.y);

		if (i < 0) {
			n.set(w.x, w.y);
			return false;
		}

		float j = dx * dx + dy * dy;
		if (i > j) {
			n.set(p2.x, p2.y);
			return false;
		}

		i /= j;

		n.set(dx * i + w.x, dy * i + w.y);
		return true;
	}

	public int getSpriteCount() {
		return spritesortcnt;
	}

	public SPRITE[] getSprites() {
		return tsprite;
	}

	public int getMaskwallCount() {
		return maskwallcnt;
	}

	public Integer[] getMaskwalls() {
		return maskwall;
	}
}
