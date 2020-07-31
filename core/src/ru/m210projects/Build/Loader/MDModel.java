/*
* MDModel for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXUNIQHUDID;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.timerticspersec;
import static ru.m210projects.Build.Gameutils.BClipRange;
import static ru.m210projects.Build.Loader.MDAnimation.MDANIM_ONESHOT;
import static ru.m210projects.Build.Loader.MDAnimation.mdpause;
import static ru.m210projects.Build.Loader.MDAnimation.mdtims;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;

import java.util.Iterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.PixmapTileData;
import ru.m210projects.Build.Render.Types.Spriteext;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Script.ModelInfo.Spritesmooth;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SPRITE;

public abstract class MDModel extends Model {
	public MDSkinmap skinmap;
	public int numskins, skinloaded; // set to 1+numofskin when a skin is loaded and the tex coords are modified,

	public int numframes, cframe, nframe, fpssc;
	public boolean usesalpha;
	public float oldtime, curtime, interpol;
	public MDAnimation animations;

	public abstract int getFrameIndex(String framename);

	public void updateanimation(DefScript defs, SPRITE tspr) {

		if (numframes < 2) {
			interpol = 0;
			return;
		}

		int tile = tspr.picnum;

		cframe = nframe = defs.mdInfo.getParams(tspr.picnum).framenum;

		boolean smoothdurationp = (GLSettings.animSmoothing.get() && (defs.mdInfo.getParams(tile).smoothduration != 0));

		Spritesmooth smooth = (tspr.owner < MAXSPRITES + MAXUNIQHUDID) ? defs.mdInfo.getSmoothParams(tspr.owner) : null;
		Spriteext sprext = (tspr.owner < MAXSPRITES + MAXUNIQHUDID) ? defs.mapInfo.getSpriteInfo(tspr.owner) : null;

		MDAnimation anim;
		for (anim = animations; anim != null && anim.startframe != cframe; anim = anim.next) {
			/* do nothing */;
		}

		if (anim == null) {
			if (!smoothdurationp || ((smooth.mdoldframe == cframe) && (smooth.mdcurframe == cframe))) {
				interpol = 0;
				return;
			}

			if (smooth.mdoldframe != cframe) {
				if (smooth.mdsmooth == 0) {
					sprext.mdanimtims = mdtims;
					interpol = 0;
					smooth.mdsmooth = 1;
					smooth.mdcurframe = (short) cframe;
				}

				if (smooth.mdcurframe != cframe) {
					sprext.mdanimtims = mdtims;
					interpol = 0;
					smooth.mdsmooth = 1;
					smooth.mdoldframe = smooth.mdcurframe;
					smooth.mdcurframe = (short) cframe;
				}
			} else {
				sprext.mdanimtims = mdtims;
				interpol = 0;
				smooth.mdsmooth = 1;
				smooth.mdoldframe = smooth.mdcurframe;
				smooth.mdcurframe = (short) cframe;
			}
		} else if (/* anim && */ sprext.mdanimcur != anim.startframe) {
			sprext.mdanimcur = (short) anim.startframe;
			sprext.mdanimtims = mdtims;
			interpol = 0;

			if (!smoothdurationp) {
				cframe = nframe = anim.startframe;
				return;
			}

			nframe = anim.startframe;
			cframe = smooth.mdoldframe;
			smooth.mdsmooth = 1;
			return;
		}

		int fps = (smooth.mdsmooth != 0) ? Math.round((1.0f / (defs.mdInfo.getParams(tile).smoothduration)) * 66.f)
				: anim.fpssc;

		int i = (int) ((mdtims - sprext.mdanimtims) * ((fps * timerticspersec) / 120));

		int j = 65536;
		if (smooth.mdsmooth == 0)
			j = ((anim.endframe + 1 - anim.startframe) << 16);

		// Just in case you play the game for a VERY long time...
		if (i < 0) {
			i = 0;
			sprext.mdanimtims = mdtims;
		}
		// compare with j*2 instead of j to ensure i stays > j-65536 for MDANIM_ONESHOT
		if (anim != null && (i >= j + j) && (fps != 0) && mdpause == 0) // Keep mdanimtims close to mdtims to avoid the
																		// use of MOD
			sprext.mdanimtims += j / ((fps * timerticspersec) / 120);

		int k = i;

		if (anim != null && (anim.flags & MDANIM_ONESHOT) != 0) {
			if (i > j - 65536)
				i = j - 65536;
		} else {
			if (i >= j) {
				i -= j;
				if (i >= j)
					i %= j;
			}
		}

		if (GLSettings.animSmoothing.get() && smooth.mdsmooth != 0) {
			nframe = anim != null ? anim.startframe : smooth.mdcurframe;
			cframe = smooth.mdoldframe;

			if (k > 65535) {
				sprext.mdanimtims = mdtims;
				interpol = 0;
				smooth.mdsmooth = 0;
				cframe = nframe;

				smooth.mdoldframe = (short) cframe;
				return;
			}
		} else {
			cframe = (i >> 16) + anim.startframe;
			nframe = cframe + 1;
			if (nframe > anim.endframe)
				nframe = anim.startframe;

			smooth.mdoldframe = (short) cframe;
		}
		interpol = BClipRange((i & 65535) / 65536.f, 0.0f, 1.0f);

		if (cframe < 0 || cframe >= numframes || nframe < 0 || nframe >= numframes) {
			if (cframe < 0)
				cframe = 0;
			if (cframe >= numframes)
				cframe = numframes - 1;
			if (nframe < 0)
				nframe = 0;
			if (nframe >= numframes)
				nframe = numframes - 1;
		}
	}

	public MDSkinmap getSkin(int palnum, int skinnum, int surfnum) {
		for (MDSkinmap sk = skinmap; sk != null; sk = sk.next)
			if (sk.palette == palnum && skinnum == sk.skinnum && surfnum == sk.surfnum)
				return sk;

		return null;
	}

	private void addSkin(MDSkinmap sk) {
		sk.next = skinmap;
		skinmap = sk;
	}

	public int setSkin(String skinfn, int palnum, int skinnum, int surfnum, double param, double specpower,
			double specfactor) {
		if (skinfn == null)
			return -2;
		if (palnum >= MAXPALOOKUPS)
			return -3;

		if (mdnum == 2)
			surfnum = 0;

		MDSkinmap sk = getSkin(palnum, skinnum, surfnum);
		if (sk == null) // no replacement yet defined
			addSkin(sk = new MDSkinmap());

		sk.palette = palnum;
		sk.skinnum = skinnum;
		sk.surfnum = surfnum;
		sk.param = (float) param;
		sk.specpower = (float) specpower;
		sk.specfactor = (float) specfactor;
		sk.fn = skinfn;

		return 0;
	}

	public int setAnimation(String framestart, String frameend, int fpssc, int flags) {
		MDAnimation ma = new MDAnimation();
		int i = 0;

		// find index of start frame
		i = getFrameIndex(framestart);
		if (i == numframes)
			return -2;
		ma.startframe = i;

		// find index of finish frame which must trail start frame
		i = getFrameIndex(frameend);
		if (i == numframes)
			return -3;
		ma.endframe = i;

		ma.fpssc = fpssc;
		ma.flags = flags;

		ma.next = animations;
		animations = ma;

		return 0;
	}

	public GLTile loadskin(DefScript defs, int number, int pal, int surf) {
		String skinfile = null;
		GLTile texidx = null;
		GLTile[] texptr = null;
		int idptr = -1;
		MDSkinmap sk, skzero = null;
//		long startticks;

		if (mdnum == 2)
			surf = 0;

		if (pal >= MAXPALOOKUPS || defs == null)
			return null;

		for (sk = skinmap; sk != null; sk = sk.next) {
			int i = -1;
			if (sk.palette == pal && sk.skinnum == number && sk.surfnum == surf) {
				skinfile = sk.fn;
				idptr = defs.texInfo.getPaletteEffect(pal);
				texptr = sk.texid;
				if (texptr != null)
					texidx = texptr[idptr];
				// OSD_Printf("Using exact match skin (pal=%d,skinnum=%d,surfnum=%d)
				// %s\n",pal,number,surf,skinfile);
				break;
			}
			// If no match, give highest priority to number, then pal.. (Parkar's request,
			// 02/27/2005)
			else if ((sk.palette == 0) && (sk.skinnum == number) && (sk.surfnum == surf) && (i < 5)) {
				i = 5;
				skzero = sk;
			} else if ((sk.palette == pal) && (sk.skinnum == 0) && (sk.surfnum == surf) && (i < 4)) {
				i = 4;
				skzero = sk;
			} else if ((sk.palette == 0) && (sk.skinnum == 0) && (sk.surfnum == surf) && (i < 3)) {
				i = 3;
				skzero = sk;
			} else if ((sk.palette == 0) && (sk.skinnum == number) && (i < 2)) {
				i = 2;
				skzero = sk;
			} else if ((sk.palette == pal) && (sk.skinnum == 0) && (i < 1)) {
				i = 1;
				skzero = sk;
			} else if ((sk.palette == 0) && (sk.skinnum == 0) && (i < 0)) {
				i = 0;
				skzero = sk;
			}
		}

		if (sk == null) {
			if (pal >= (MAXPALOOKUPS - RESERVEDPALS))
				return null;

			if (skzero != null) {
				skinfile = skzero.fn;
				idptr = defs.texInfo.getPaletteEffect(pal);
				texptr = skzero.texid;
				if (texptr != null)
					texidx = texptr[idptr];
				// OSD_Printf("Using def skin 0,0 as fallback, pal=%d\n", pal);
			} else {
				Console.Println("Couldn't load skin", OSDTEXT_YELLOW);
				defs.mdInfo.removeModelInfo(this);
				return null;
			}
		}

		if (skinfile == null)
			return null;

		if (texidx != null)
			return texidx;

		// possibly fetch an already loaded multitexture :_)
		if (pal >= (MAXPALOOKUPS - RESERVEDPALS)) {
			for (int i = MAXTILES - 1; i >= 0; i--) {
				Model m = defs.mdInfo.getModel(i);
				if (m == null || m.mdnum < 2)
					continue;

				MDModel mi = (MDModel) m;
				for (skzero = mi.skinmap; skzero != null; skzero = skzero.next)
					if (skzero.fn.equalsIgnoreCase(sk.fn) && skzero.texid[defs.texInfo.getPaletteEffect(pal)] != null) {
						int f = defs.texInfo.getPaletteEffect(pal);
						sk.texid[f] = skzero.texid[f];
						return sk.texid[f];
					}
			}
		}

		texidx = null;

		Resource res = BuildGdx.cache.open(skinfile, 0);
		if (res == null) {
			Console.Println("Skin " + skinfile + " not found.", OSDTEXT_YELLOW);
			defs.mdInfo.removeModelInfo(this);
			skinfile = null;
			return null;
		}

//		startticks = System.currentTimeMillis();
		try {
			byte[] data = res.getBytes();
			Pixmap pix = new Pixmap(data, 0, data.length);
			texidx = new GLTile(new PixmapTileData(pix, true, 0), 0, true);
			usesalpha = true;
		} catch (Exception e) {
			Console.Println("Couldn't load file: " + skinfile, OSDTEXT_YELLOW);
			defs.mdInfo.removeModelInfo(this);
			skinfile = null;
			return null;
		} finally {
			res.close();
		}
		texidx.setupTextureWrap(TextureWrap.Repeat);

//		long etime = System.currentTimeMillis() - startticks;
//		System.out.println("Load skin: p" + pal + "-e" + defs.texInfo.getPaletteEffect(pal) + " \"" + skinfile
//				+ "\"... " + etime + " ms");

		texptr[idptr] = texidx;
		return texidx;
	}

	@Override
	public Iterator<GLTile[]> getSkins() {
		Iterator<GLTile[]> it = new Iterator<GLTile[]>() {
			private MDSkinmap current = skinmap;

			@Override
			public boolean hasNext() {
				return current != null && current.next != null;
			}

			@Override
			public GLTile[] next() {
				MDSkinmap sk = current;
				current = sk.next;
				return sk.texid;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	@Override
	public void clearSkins() {
		for (MDSkinmap sk = skinmap; sk != null; sk = sk.next) {
			for (int j = 0; j < sk.texid.length; j++) {
				GLTile tex = sk.texid[j];
				if (tex == null)
					continue;

				tex.delete();
				sk.texid[j] = null;
			}
		}
	}
}
