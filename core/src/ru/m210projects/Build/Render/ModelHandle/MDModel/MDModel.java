/*
* MDModel for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Render.ModelHandle.MDModel;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.MAXUNIQHUDID;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.timerticspersec;
import static ru.m210projects.Build.Gameutils.BClipRange;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.MDANIM_ONESHOT;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.mdpause;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.mdtims;
import static ru.m210projects.Build.Render.ModelHandle.Model.MD_ROTATE;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ModelHandle.DefMD;
import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Script.ModelsInfo;
import ru.m210projects.Build.Script.ModelsInfo.SpriteAnim;
import ru.m210projects.Build.Script.ModelsInfo.Spritesmooth;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SPRITE;

public abstract class MDModel implements GLModel {

	public MDSkinmap skinmap;

	public int numframes, cframe, nframe, fpssc;
	public boolean usesalpha;
	public float oldtime, curtime, interpol;
	public MDAnimation animations;
	public long mdanimtims;
	public short mdanimcur;

	protected int flags;
	protected float yoffset, zadd, bscale;

	public MDModel(DefMD md) {
		this.skinmap = md.getSkins();
		this.animations = md.getAnimations();
		this.numframes = md.getFrames();
		this.flags = md.getFlags();
		this.zadd = md.getYOffset(true);
		this.yoffset = md.getYOffset(false);
		this.bscale = md.getScale();
	}

	public abstract GLTile loadTexture(String skinfile, int palnum, int effectnum);

	public float getYOffset(boolean yflipping) {
		return !yflipping ? yoffset : zadd;
	}

	public float getBScale() {
		return bscale;
	}

	public float getShadeOff() {
		return 0;
	}

	@Override
	public boolean isRotating() {
		return (flags & MD_ROTATE) != 0;
	}

	@Override
	public boolean isTintAffected() {
		return (flags & 1) == 0;
	}

	public void updateanimation(ModelsInfo mdInfo, SPRITE tspr) {
		if (numframes < 2) {
			interpol = 0;
			return;
		}

		int tile = tspr.picnum;

		cframe = nframe = mdInfo.getParams(tspr.picnum).framenum;
		boolean smoothdurationp = (GLSettings.animSmoothing.get() && (mdInfo.getParams(tile).smoothduration != 0));

		Spritesmooth smooth = (tspr.owner < MAXSPRITES + MAXUNIQHUDID) ? mdInfo.getSmoothParams(tspr.owner) : null;
		SpriteAnim sprext = (tspr.owner < MAXSPRITES + MAXUNIQHUDID) ? mdInfo.getAnimParams(tspr.owner) : null;

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

			System.err.println("Set " + cframe);
			smooth.mdsmooth = 1;
			return;
		}

		int fps = (smooth.mdsmooth != 0) ? Math.round((1.0f / (mdInfo.getParams(tile).smoothduration)) * 66.f)
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

	protected MDSkinmap getSkin(int palnum, int skinnum, int surfnum) {
		for (MDSkinmap sk = skinmap; sk != null; sk = sk.next)
			if (sk.palette == palnum && skinnum == sk.skinnum && surfnum == sk.surfnum)
				return sk;

		return null;
	}

	public GLTile getSkin(final int pal, int skinnum, int surfnum, int effectnum) {
		String skinfile = null;
		GLTile texidx = null;
		GLTile[] texptr = null;
		MDSkinmap sk, skzero = null;

		if (this.getType() == Type.Md2)
			surfnum = 0;

		if (pal >= MAXPALOOKUPS)
			return null;

		int i = -1;
		for (sk = skinmap; sk != null; sk = sk.next) {
			if (sk.palette == pal && sk.skinnum == skinnum && sk.surfnum == surfnum) {
				skinfile = sk.fn;
				texptr = sk.texid;
				if (texptr != null)
					texidx = texptr[effectnum];
				// OSD_Printf("Using exact match skin (pal=%d,skinnum=%d,surfnum=%d)
				// %s\n",pal,skinnum,surf,skinfile);
				break;
			}
			// If no match, give highest priority to skinnum, then pal.. (Parkar's request,
			// 02/27/2005)
			else if ((sk.palette == 0) && (sk.skinnum == skinnum) && (sk.surfnum == surfnum) && (i < 5)) {
				i = 5;
				skzero = sk;
			} else if ((sk.palette == pal) && (sk.skinnum == 0) && (sk.surfnum == surfnum) && (i < 4)) {
				i = 4;
				skzero = sk;
			} else if ((sk.palette == 0) && (sk.skinnum == 0) && (sk.surfnum == surfnum) && (i < 3)) {
				i = 3;
				skzero = sk;
			} else if ((sk.palette == 0) && (sk.skinnum == skinnum) && (i < 2)) {
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
				texptr = skzero.texid;
				if (texptr != null)
					texidx = texptr[effectnum];
				// OSD_Printf("Using def skin 0,0 as fallback, pal=%d\n", pal);
			} else {
				Console.Println("Couldn't load skin", Console.OSDTEXT_YELLOW);
				return null;
			}
		}

		if (skinfile == null)
			return null;

		if (texidx != null)
			return texidx;

		return texptr[effectnum] = loadTexture(skinfile, pal, effectnum);
	}

}
