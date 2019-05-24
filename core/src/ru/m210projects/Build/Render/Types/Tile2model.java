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

public class Tile2model {
	// maps build tiles to particular animation frames of a model
	public Model 	model;
	public Voxel	voxel;
	public int     	skinnum;
    public int     	framenum;   // calculate the number from the name when declaring
    public float   	smoothduration;
    public boolean disposable;
    
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
