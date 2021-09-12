/*
* Model for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;

import ru.m210projects.Build.Render.TextureHandle.GLTile;

public abstract class OldModel {

	public static final int MD_ROTATE = 2;

	public int mdnum; //VOX=1, MD2=2, MD3=3
	public int shadeoff;
	public float scale, bscale, zadd, yoffset;
	public int flags;

	public ShortBuffer indicesBuffer;
	public FloatBuffer verticesBuffer;

	public void setMisc(float scale, int shadeoff, float zadd, float yoffset, int flags)
	{
	    this.bscale = scale;
	    this.shadeoff = shadeoff;
	    this.zadd = zadd;
	    this.yoffset = yoffset;
	    this.flags = flags;
	}

	public void free() {
		clearSkins();
		indicesBuffer.clear();
		verticesBuffer.clear();
		indicesBuffer = null;
		verticesBuffer = null;
	}

	public abstract Iterator<GLTile[]> getSkins();

	public abstract void clearSkins();
}
