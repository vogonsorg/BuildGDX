/*
 * High-colour textures support for Polymost
 * by Jonathon Fowler
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.Types;

public class Pthtyp {

	public BTexture glpic;
	public short picnum;
	public short palnum;
	public short effects;
	public short flags; // 1 = clamped (dameth&4), 2 = hightile, 4 = skybox face, 8 = hasalpha, 16 = hasfullbright, 128 = invalidated, 256 = non-transparent
	public short skyface;
	public Hicreplctyp hicr;
	public Pthtyp next;

	public short sizx, sizy;
	public float scalex, scaley;

	public boolean isClamped() {
		return (flags & 1) != 0;
	}

	public void setClamped(boolean mode) {
		setBit(mode, 1);
	}

	public boolean isHighTile() {
		return (flags & 2) != 0;
	}

	public boolean isSkyboxFace() {
		return (flags & 4) != 0;
	}

	public boolean hasAlpha() {
		return (flags & 8) != 0;
	}

	public void setHasAlpha(boolean mode) {
		setBit(mode, 8);
	}
	
	public void setHighTile(boolean mode) {
		setBit(mode, 2);
	}
	
	public void setSkyboxFace(boolean mode) {
		setBit(mode, 4);
	}

	// public boolean hasFullbright() {
	// return (flags & 16) != 0;
	// }
	//
	// public void setFullbright(boolean mode) {
	// setBit(mode, 16);
	// }

	public boolean isInvalidated() {
		return (flags & 128) != 0;
	}

	public void setInvalidated(boolean mode) {
		setBit(mode, 128);
	}

	public boolean isNonTransparent() {
		return (flags & 256) != 0;
	}

	private void setBit(boolean mode, int bit) {
		if (mode) {
			flags |= bit;
		} else {
			flags &= ~bit;
		}
	}
}
