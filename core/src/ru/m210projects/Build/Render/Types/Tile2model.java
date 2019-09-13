/*
* Tile2model for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been modified
* by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Render.Types;

import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.Voxels.Voxel;
import ru.m210projects.Build.Script.ModelInfo.AnimationInfo;
import ru.m210projects.Build.Script.ModelInfo.SkinInfo;

public class Tile2model {
	// maps build tiles to particular animation frames of a model
	public Model 	model;
	public Voxel	voxel;
	public int     	skinnum;
    public int     	framenum;   // calculate the number from the name when declaring
    public float   	smoothduration;
    public boolean disposable;
    
    //info for load
    public String filename;
    public String framename;  
    public float scale; 
    public int shadeoff; 
    public float zadd; 
    public float yoffset; 
    public int flags;
    public AnimationInfo animInfo;
    public SkinInfo skinInfo;
    
    public Tile2model clone(boolean disposable)
    {
    	Tile2model out = new Tile2model();
    	
    	out.model = model;
    	out.voxel = voxel;
    	out.skinnum = skinnum;
    	out.framenum = framenum;
    	out.smoothduration = smoothduration;
    	out.disposable = disposable;
    	
    	return out;
    }
}
