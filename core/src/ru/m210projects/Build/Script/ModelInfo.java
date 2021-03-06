// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.


package ru.m210projects.Build.Script;

import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXUNIQHUDID;

import ru.m210projects.Build.Loader.MDModel;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.Loader.Voxels.Voxel;
import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Render.Types.Tile2model;

public class ModelInfo {
	
    public class AnimationInfo
	{
		public final String framestart;
		public final String frameend;
		public final int fpssc; 
		public final int flags;
		
		public AnimationInfo(String framestart, String frameend, int fpssc, int flags)
		{
			this.framestart = framestart;
			this.frameend = frameend;
			this.fpssc = fpssc;
			this.flags = flags;
		}
	}
    
    public class SkinInfo
    {
    	public final String skinfn;
    	public final int palnum;
    	public final int skinnum;
    	public final int surfnum;
    	public final double param;
    	public final double specpower;
    	public final double specfactor;
    	
    	public SkinInfo(String skinfn, int palnum, int skinnum, int surfnum, double param, double specpower, double specfactor)
    	{
    		this.skinfn = skinfn;
			this.palnum = palnum;
			this.skinnum = skinnum;
			this.surfnum = surfnum;
			this.param = param;
			this.specpower = specpower;
			this.specfactor = specfactor;
    	}
    }
    
	private Tile2model[] cache = new Tile2model[MAXTILES];
	private Hudtyp[][] hudInfo = new Hudtyp[2][MAXTILES];
	public static class Spritesmooth {
		public float smoothduration;
		public short mdcurframe;
		public short mdoldframe;
		public short mdsmooth;
	}
	private Spritesmooth[] spritesmooth = new Spritesmooth[MAXSPRITES+MAXUNIQHUDID];

	public ModelInfo()
	{
//		for(int i = 0; i < cache.length; i++)
//			cache[i] = new Tile2model();
//		for(int i = 0; i < 2; i++)
//			for(int j = 0; j < MAXTILES; j++)
//				hudInfo[i][j] = new Hudtyp();
		for (int i = 0; i < spritesmooth.length; i++)
			spritesmooth[i] = new Spritesmooth();
	}
	
	public ModelInfo(ModelInfo src, boolean disposable)
	{
		for(int i = 0; i < cache.length; i++) {
			if(src.cache[i] != null)
				cache[i] = src.cache[i].clone(disposable);
		}
		for(int i = 0; i < 2; i++) 
			for(int j = 0; j < MAXTILES; j++) {
				if(src.hudInfo[i] != null && src.hudInfo[i][j] != null)
					hudInfo[i][j] = src.hudInfo[i][j].clone();
			}
		for (int i = 0; i < spritesmooth.length; i++)
			spritesmooth[i] = new Spritesmooth();
	}

	public Spritesmooth getSmoothParams(int i)
	{
		return spritesmooth[i];
	}

	public Model getModel(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum].model;
		
		return null;
	}
	
	public Voxel getVoxel(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum].voxel;
		
		return null;
	}
	
	public VOXModel getVoxModel(int picnum)
	{
		if(cache[picnum] != null && cache[picnum].voxel != null)
			return cache[picnum].voxel.model;
		
		return null;
	}
	
	public Tile2model getParams(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum];
		
		return null;
	}
	
	public Hudtyp getHudInfo(int picnum, int flags)
	{
		if(hudInfo[(flags>>2)&1] != null)
			return hudInfo[(flags>>2)&1][picnum];
		
		return null;
	}
	
	public int addModelInfo(Model md, int picnum, String framename, int skinnum, float smooth)
	{
		if (picnum >= MAXTILES) return(-2);
	    if (framename == null) return(-3);
	    if(md == null) return -1;
	   
	    int i = -3;
	    switch(md.mdnum)
	    {
	    case 1:
	    	smooth = skinnum = i = 0;
	    	break;
	    case 2:
	    case 3:
	    	i = ((MDModel)md).getFrameIndex(framename);
	    	break;
	    }
	    
	    if(cache[picnum] == null)
	    	cache[picnum] = new Tile2model();
	    //else flush(); //XXX

	    cache[picnum].model = md;
	    cache[picnum].framenum = i;
	    cache[picnum].skinnum = skinnum;
	    cache[picnum].smoothduration = smooth;

	    return i;
	}
	
	public int addVoxelInfo(Voxel md, int picnum)
	{
		if (picnum >= MAXTILES) return(-2);
	    if(md == null) return -1;
	    
	    if(cache[picnum] == null)
	    	cache[picnum] = new Tile2model();

	    cache[picnum].voxel = md;
	    return 0;
	}

	public void removeModelInfo(Model md)
	{
		for (int i=MAXTILES-1; i>=0; i--) {
			if(cache[i] == null) continue;
			
	        if (cache[i].model == md || (cache[i].voxel != null && cache[i].voxel.model == md)) 
	        {
	        	cache[i].model = null;
	            cache[i].voxel = null;
	            cache[i] = null;
	        }
	    }
	}
	
	public int addHudInfo(int tilex, double xadd, double yadd, double zadd, short angadd, int flags, int fov)
	{
	    if (tilex >= MAXTILES) return -2;
	    
	    if(hudInfo[(flags>>2)&1] == null || hudInfo[(flags>>2)&1][tilex] == null)
	    	hudInfo[(flags>>2)&1][tilex] = new Hudtyp();

	    Hudtyp hud = hudInfo[(flags>>2)&1][tilex];

	    hud.xadd = (float) xadd;
	    hud.yadd = (float) yadd;
	    hud.zadd = (float) zadd;
	    hud.angadd = (short) (angadd|2048);
	    hud.flags = (short)flags;
	    hud.fov = (short)fov;

	    return 0;
	}

	public void dispose() {
		for (int i=MAXTILES-1; i>=0; i--) {
			if(cache[i] == null) continue;
			
			if(!cache[i].disposable) 
				continue;
			
	        if (cache[i].model != null) {
	            cache[i].model.free();
	            cache[i].model = null;
	        }
	        if (cache[i].voxel != null) {
	        	if(cache[i].voxel.model != null)
	        		cache[i].voxel.model.free();
	            cache[i].voxel = null;
	        }
	        
	        cache[i] = null;
	    }
	}
}
