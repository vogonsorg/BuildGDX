/*
* MDSkinmap for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Render.TextureHandle.TextureHDInfo.HICEFFECTMASK;
import ru.m210projects.Build.Render.Types.BTexture;

public class MDSkinmap {
	public int palette; // Build palette number
	public int skinnum, surfnum;   // Skin identifier, surface number
    public String fn;   // Skin filename
    public BTexture[] texid = new BTexture[HICEFFECTMASK+1];   // OpenGL texture numbers for effect variations
    public MDSkinmap next;
    public float param, specpower, specfactor;
}
