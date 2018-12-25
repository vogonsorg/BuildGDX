/*
* Model for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import ru.m210projects.Build.Render.TextureHandle.BTexture;

public class Model {
	
	public static final int MD_ROTATE = 2;
	
	public int mdnum; //VOX=1, MD2=2, MD3=3
	public int modelid;
	public int shadeoff;
	public float scale, bscale, zadd, yoffset;
	public BTexture[] texid;	// skins XXX for voxels only
	public int flags;
	
	public IntBuffer vbos;
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
}
