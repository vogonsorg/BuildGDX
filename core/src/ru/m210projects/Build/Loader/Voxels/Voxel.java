// Voxel by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import ru.m210projects.Build.FileHandle.Resource;

public class Voxel {
	
	public static final int MAXVOXMIPS = 5;
	
	public int xsiz[], ysiz[], zsiz[];
	public int xpiv[], ypiv[], zpiv[];
	public short xyoffs[][][];
	public int[][] slabxoffs;
	public byte[][] data;
	public int scale = 65536;
	
	public VOXModel model;
	
	public Voxel(Resource dat) throws Exception
	{
		int i, j;
		int mip = 0;
		
		xsiz = new int[MAXVOXMIPS];
		ysiz = new int[MAXVOXMIPS];
		zsiz = new int[MAXVOXMIPS];
		
		xpiv = new int[MAXVOXMIPS];
		ypiv = new int[MAXVOXMIPS];
		zpiv = new int[MAXVOXMIPS];
		
		xyoffs = new short[MAXVOXMIPS][][];
		slabxoffs = new int[MAXVOXMIPS][];
		data = new byte[MAXVOXMIPS][];

		while(dat.position() < dat.size() - 768) {
		    int mip1leng = dat.readInt();
		    int xs = xsiz[mip] = dat.readInt();
		    int ys = ysiz[mip] = dat.readInt();
		    zsiz[mip] = dat.readInt();
		    
		    xpiv[mip] = dat.readInt();
		    ypiv[mip] = dat.readInt();
		    zpiv[mip] = dat.readInt();

		    int offset = ((xs + 1) << 2) + (xs * (ys + 1) << 1);
		    slabxoffs[mip] = new int[xs+1];
		    for(i = 0; i <= xs; i++) 
		    	slabxoffs[mip][i] = dat.readInt() - offset;
	
		    xyoffs[mip] = new short[xs][ys+1];
		    for (i = 0; i < xs; ++i)
				for (j = 0; j <= ys; ++j)
					xyoffs[mip][i][j] = dat.readShort();
		    
		    i = dat.size() - dat.position() - 768;
		    if(i < mip1leng-(24+offset)) 
		    	break;

		    data[mip] = new byte[mip1leng-(24+offset)];
		    dat.read(data[mip]);

		    mip++;
		}

		if(mip == 0)
			throw new Exception("Can't load voxel");
	}
	
	public VOXModel getModel() {
		return model;
	}
}
