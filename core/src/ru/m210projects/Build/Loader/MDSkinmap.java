package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Types.Hightile.HICEFFECTMASK;

import com.badlogic.gdx.graphics.Texture;

public class MDSkinmap {
	public int palette; // Build palette number
	public int skinnum, surfnum;   // Skin identifier, surface number
    public String fn;   // Skin filename
    public Texture[] texid = new Texture[HICEFFECTMASK+1];   // OpenGL texture numbers for effect variations
    public MDSkinmap next;
    public float param, specpower, specfactor;
}
