/*
* MDModel for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Render.ModelHandle.MDModel;

import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.MAXUNIQHUDID;
import static ru.m210projects.Build.Engine.timerticspersec;
import static ru.m210projects.Build.Gameutils.BClipRange;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.MDANIM_ONESHOT;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.mdpause;
import static ru.m210projects.Build.Render.ModelHandle.MDModel.MDAnimation.mdtims;

import ru.m210projects.Build.Render.ModelHandle.DefMD;
import ru.m210projects.Build.Render.Types.Spriteext;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Script.ModelsInfo.Spritesmooth;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SPRITE;

public class MDModel {

	public MDSkinmap skinmap;

	public int numframes, cframe, nframe, fpssc;
	public boolean usesalpha;
	public float oldtime, curtime, interpol;
	public MDAnimation animations;
	protected int flags;

	public MDModel(DefMD md) {
		this.skinmap = md.getSkins();
		this.animations = md.getAnimations();
		this.numframes = md.getFrames();
		this.flags = md.getFlags();
	}

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

}
