package ru.m210projects.Build.Script;

import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXUNIQHUDID;

import com.badlogic.gdx.utils.Disposable;

import ru.m210projects.Build.Loader.MDModel;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Types.Tile2model;

public class ModelInfo implements Disposable {

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
		for(int i = 0; i < cache.length; i++)
			cache[i] = new Tile2model();
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < MAXTILES; j++)
				hudInfo[i][j] = new Hudtyp();
		for (int i = 0; i < spritesmooth.length; i++)
			spritesmooth[i] = new Spritesmooth();
	}
	
	public ModelInfo(ModelInfo src)
	{
		for(int i = 0; i < cache.length; i++)
			cache[i] = src.cache[i].clone();
		for(int i = 0; i < 2; i++) 
			for(int j = 0; j < MAXTILES; j++)
				this.hudInfo[i][j] = src.hudInfo[i][j].clone();
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
	
	public Model getVoxel(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum].voxel;
		
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
		return hudInfo[(flags>>2)&1][picnum];
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

	    cache[picnum].model = md;
	    cache[picnum].framenum = i;
	    cache[picnum].skinnum = skinnum;
	    cache[picnum].smoothduration = smooth;

	    return i;
	}
	
	public int addVoxelInfo(VOXModel md, int picnum)
	{
		if (picnum >= MAXTILES) return(-2);
	    if(md == null) return -1;

	    cache[picnum].voxel = md;
	    return 0;
	}
	
	public void removeModelInfo(Model md)
	{
		for (int i=MAXTILES-1; i>=0; i--) {
	        if (cache[i].model == md) 
	            cache[i].model = null;
	        if (cache[i].voxel == md) 
	            cache[i].voxel = null;
	    }
	}
	
	public int addHudInfo(int tilex, double xadd, double yadd, double zadd, short angadd, int flags, int fov)
	{
	    if (tilex >= MAXTILES) return -2;
	    
	    Hudtyp hud = hudInfo[(flags>>2)&1][tilex];

	    hud.xadd = (float) xadd;
	    hud.yadd = (float) yadd;
	    hud.zadd = (float) zadd;
	    hud.angadd = (short) (angadd|2048);
	    hud.flags = (short)flags;
	    hud.fov = (short)fov;

	    return 0;
	}

	@Override
	public void dispose() {
		for (int i=MAXTILES-1; i>=0; i--) {
	        if (cache[i].model != null) {
	            cache[i].model.free();
	            cache[i].model = null;
	        }
	        if (cache[i].voxel != null) {
	            cache[i].voxel.free();
	            cache[i].voxel = null;
	        }
	    }
	}
}
