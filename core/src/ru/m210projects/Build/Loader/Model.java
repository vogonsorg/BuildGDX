/*
* MDSkinmap for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*/

package ru.m210projects.Build.Loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import ru.m210projects.Build.Render.Types.BTexture;

public class Model {
	public int mdnum; //VOX=1, MD2=2, MD3=3. NOTE: must be first in structure!
	public int modelid;
	public int shadeoff;
	public float scale, bscale, zadd, yoffset;
	public BTexture[] texid;	// skins
	public int flags;
	
	public IntBuffer vbos;
	public ShortBuffer indicesBuffer;
	public FloatBuffer verticesBuffer;
}
