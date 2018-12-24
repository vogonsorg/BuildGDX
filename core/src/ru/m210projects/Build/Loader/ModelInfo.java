package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Engine.MAXTILES;

import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Types.Tile2model;

public class ModelInfo {

	private Tile2model[] cache = new Tile2model[MAXTILES];
	private Hudtyp[][] hudmem = new Hudtyp[2][MAXTILES];

	public ModelInfo()
	{
		for(int i = 0; i < cache.length; i++)
			cache[i] = new Tile2model();
		
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < MAXTILES; j++)
				hudmem[i][j] = new Hudtyp();
	}

	public Model getModel(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum].model;
		
		return null;
	}
	
	public Tile2model getParams(int picnum)
	{
		if(cache[picnum] != null)
			return cache[picnum];
		
		return null;
	}
	
	public int addModelInfo(Model md, int picnum, String framename, int skinnum, float smooth)
	{
		if (picnum >= MAXTILES) return(-2);
	    if (framename == null) return(-3);
	    if(md == null) return 0;
	   
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
	
	public void removeModelInfo(Model md)
	{
		for (int i=MAXTILES-1; i>=0; i--) {
	        if (cache[i].model == md) 
	            cache[i].model = null;
	    }
	}
	
	public int addHudInfo(int tilex, double xadd, double yadd, double zadd, short angadd, int flags, int fov)
	{
	    if (tilex >= MAXTILES) return -2;
	    
	    Hudtyp hudInfo = hudmem[(flags>>2)&1][tilex];

	    hudInfo.xadd = (float) xadd;
	    hudInfo.yadd = (float) yadd;
	    hudInfo.zadd = (float) zadd;
	    hudInfo.angadd = (short) (angadd|2048);
	    hudInfo.flags = (short)flags;
	    hudInfo.fov = (short)fov;

	    return 0;
	}

}
