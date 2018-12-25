/*
* Tile2model for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been modified
* by the EDuke32 team (development@voidpoint.com)
* by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Types;

import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.Voxels.VOXModel;

public class Tile2model {
	// maps build tiles to particular animation frames of a model
	public Model 	model;
	public VOXModel	voxel;
	public int     	skinnum;
    public int     	framenum;   // calculate the number from the name when declaring
    public float   	smoothduration;  
}
