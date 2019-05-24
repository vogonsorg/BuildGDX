// KVXLoader by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import static java.lang.Math.max;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.FileHandle.Cache1D.kGetBuffer;
import static ru.m210projects.Build.Pragmas.klabs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

import ru.m210projects.Build.Loader.Voxels.VOXModel.voxrect_t;
import ru.m210projects.Build.Render.TextureHandle.BTexture;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

public class KVXLoader {

	//For loading/conversion only
	private static int xsiz, ysiz, zsiz, yzsiz;
	
	private static HashMap<Integer, Byte> vcol;
	private static Vector2[] shp;
	private static int shcntmal[], shcnt, shcntp;
	private static int mytexo5, zbit[], gmaxx, gmaxy, garea, pow2m1[];

	private static FloatArray vertices;
	private static ShortArray indicies;
	private static FloatArray uvs;
	private static int[] pal;

	public static Voxel load(ByteBuffer buffer)
	{
		vertices = new FloatArray();
		indicies = new ShortArray();
		uvs = new FloatArray();
		
		try {
		    Voxel vox = new Voxel(buffer);
		    pal = getPalette(buffer);	
		    int voxmip = 0;
		    VOXModel vm = vox2poly(vox, voxmip);
	
		    if (vm != null)
		    {
		        vm.mdnum = 1; //VOXel model id
		        vm.scale = vm.bscale = 1.0f;
		        vm.xsiz = xsiz; vm.ysiz = ysiz; vm.zsiz = zsiz;
		        vm.xpiv = vox.xpiv[voxmip] / 256.0f; vm.ypiv = vox.ypiv[voxmip] / 256.0f; vm.zpiv = vox.zpiv[voxmip] / 256.0f;
		        vm.is8bit = 1;
		        vm.texid = new BTexture[MAXPALOOKUPS];
		        
		        vm.verticesBuffer = BufferUtils.newFloatBuffer(vertices.size);
		        vm.indicesBuffer = BufferUtils.newShortBuffer(indicies.size);
		        vm.uv = BufferUtils.newFloatBuffer(uvs.size); 
		        
		        vm.verticesBuffer.put(vertices.toArray());
		        vm.indicesBuffer.put(indicies.toArray());
		        vm.uv.put(uvs.toArray());
		        
		        vm.verticesBuffer.flip();
		        vm.indicesBuffer.flip();
		        vm.uv.flip();
		        
		        vox.model = vm;
		    }
		    
		    dispose();
	
		    return(vox);
		} catch (Exception e) {
			return null;
		}
	}

	public static Voxel load(String filepath)
	{
		ByteBuffer buffer = kGetBuffer(filepath, 0);
		if(buffer == null) return null;

		buffer.order(ByteOrder.LITTLE_ENDIAN);
	    return KVXLoader.load(buffer);
	}
	
	private static int[] getPalette(ByteBuffer dat)
	{
		int pal[] = new int[256];
		dat.position(dat.capacity() - 768);			
		    
		byte[] buf = new byte[768];
		dat.get(buf);
		    
		for (int i=0; i<256; i++)
			pal[i] = ((buf[3 * i + 0])<<18)+((buf[3 * i + 1])<<10)+((buf[3 * i + 2])<<2)+(i<<24);
		    
		return pal;
	}
	
//	private static void loadkvx(ByteBuffer dat)
//	{
//		int i, j;
//		int mip = 0;
//		
//		slabxoffs = new int[5][];
//		xyoffs = new short[5][][];
//		data = new byte[5][];
//		
////		while(dat.position() < dat.capacity() - 768) {
//		    int mip1leng = dat.getInt();
//		    xsiz = dat.getInt();
//		    ysiz = dat.getInt();
//		    zsiz = dat.getInt();
//		    
//		    pivot = new Vector3(dat.getInt() / 256.0f, dat.getInt() / 256.0f, dat.getInt() / 256.0f);
//		    int offset = ((xsiz + 1) << 2) + (xsiz * (ysiz + 1) << 1);
//		    slabxoffs[mip] = new int[xsiz+1];
//		    for(i = 0; i <= xsiz; i++) 
//		    	slabxoffs[mip][i] = dat.getInt() - offset;
//	
//		    xyoffs[mip] = new short[xsiz][ysiz+1];
//		    for (i = 0; i < xsiz; ++i)
//				for (j = 0; j <= ysiz; ++j)
//					xyoffs[mip][i][j] = dat.getShort();
//		    
//		    i = dat.capacity() - dat.position() - 768;
//		    if(i < mip1leng-(24+offset)) 
//		    	return;
//
//		    data[mip] = new byte[mip1leng-(24+offset)];
//		    dat.get(data[mip]);
//		    
////		    mip++;
////		}
//		  
//		pal = getPalette(dat);	
//	}
	
	private static void putvox(int x, int y, int z, byte col)
	{
	    z += x*yzsiz + y*zsiz;
	    vcol.put(z, col);
	}
		
	private static byte getvox(int x, int y, int z)
	{
		z += x*yzsiz + y*zsiz;
		Byte col = vcol.get(z);
		if (col == null) 
			return 0; //(0x808080);
		return (byte) ((pal[col & 0xFF] & 0xFFFFFFFFL) >> 24);
	}
	
	//Set all bits in vbit from (x,y,z0) to (x,y,z1-1) to 1's
	private static void setzrange1(int[] lptr, int z0, int z1)
	{
	    int z, ze;
	    if (((z0^z1)&~31) == 0) { lptr[z0>>5] |= ((~(-1<<(z1&31)))&(-1<<(z0&31))); return; }
	    z = (z0>>5); ze = (z1>>5);
	    lptr[z] |= (-1<<(z0&31)); for (z++; z<ze; z++) lptr[z] = -1;
	    lptr[z] |=~(-1<<(z1&31));
	}
		
	private static void setrect(int x0, int y0, int dx, int dy)
	{
	    int i, c, m, m1, x;

	    i = y0*mytexo5 + (x0>>5); dx += x0-1; c = (dx>>5) - (x0>>5);
	    m = ~pow2m1[x0&31]; m1 = pow2m1[(dx&31)+1];
	    if (c == 0) { for (m&=m1; dy != 0; dy--,i+=mytexo5) zbit[i] |= m; }
	    else
	    {
	        for (; dy != 0; dy--,i+=mytexo5)
	        {
	            zbit[i] |= m;
	            for (x=1; x<c; x++) zbit[i+x] = -1;
	            zbit[i+x] |= m1;
	        }
	    }
	}
	
	private static int isolid(int[] vbit, int x, int y, int z)
	{
	    if ((x & 0xFFFFFFFFL) >= (xsiz & 0xFFFFFFFFL)) return(0);
	    if ((y & 0xFFFFFFFFL) >= (ysiz & 0xFFFFFFFFL)) return(0);
	    if ((z & 0xFFFFFFFFL) >= (zsiz & 0xFFFFFFFFL)) return(0);
	    z += x*yzsiz + y*zsiz; return(vbit[z>>5]&(1<<(z&31)));
	}
	
	private static void daquad(VOXModel gvox, int i, int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int face)
	{
		if(i == 0)
			cntquad(gvox, x0, y0, z0, x1, y1, z1, x2, y2, z2, face);
		else addquad(gvox, x0, y0, z0, x1, y1, z1, x2, y2, z2, face);
	}
	
	private static void cntquad(VOXModel gvox, int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int face)
	{
	    int x, y, z;
	    
	    x = (int) klabs(x2-x0); y = (int) klabs(y2-y0); z = (int) klabs(z2-z0);
	    if (x == 0) x = z; else if (y == 0) y = z;
	    if (x < y) { z = x; x = y; y = z; }
	    shcntmal[shcnt+y*shcntp+x]++;
	    if (x > gmaxx) gmaxx = x;
	    if (y > gmaxy) gmaxy = y;
	    garea += x*y;
	    gvox.qcnt++;
	}
	
	private static void addquad(VOXModel gvox, int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int face)
	{
	    int i, j, x, y, z, xx, yy, nx = 0, ny = 0, nz = 0;
	    voxrect_t qptr;
	    int lptr;

	    x = (int) klabs(x2-x0); y = (int) klabs(y2-y0); z = (int) klabs(z2-z0);
	    if (x==0) { x = y; y = z; i = 0; }
	    else if (y==0) { y = z; i = 1; }
	    else i = 2;
	    if (x < y) { z = x; x = y; y = z; i += 3; }
	    z = shcntmal[shcnt+y*shcntp+x]++;
	  
	    lptr = (int) (shp[z].y*gvox.mytexx+shp[z].x);
	    
	    switch (face)
	    {
		    case 0:
		        ny = y1; x2 = x0; x0 = x1; x1 = x2; break;
		    case 1:
		        ny = y0; y0++; y1++; y2++; break;
		    case 2:
		        nz = z1; y0 = y2; y2 = y1; y1 = y0; z0++; z1++; z2++; break;
		    case 3:
		        nz = z0; break;
		    case 4:
		        nx = x1; y2 = y0; y0 = y1; y1 = y2; x0++; x1++; x2++; break;
		    case 5:
		        nx = x0; break;
	    }
	    
	    for (yy=0; yy<y; yy++,lptr+=gvox.mytexx)
	        for (xx=0; xx<x; xx++)
	        {
	            switch (face)
	            {
	            case 0:
	                if (i < 3) { nx = x1+x-1-xx; nz = z1+yy;   } //back
	                else { nx = x1+y-1-yy; nz = z1+xx;   }
	                break;
	            case 1:
	                if (i < 3) { nx = x0+xx;     nz = z0+yy;   } //front
	                else { nx = x0+yy;     nz = z0+xx;   }
	                break;
	            case 2:
	                if (i < 3) { nx = x1-x+xx;   ny = y1-1-yy; } //bot
	                else { nx = x1-1-yy;   ny = y1-1-xx; }
	                break;
	            case 3:
	                if (i < 3) { nx = x0+xx;     ny = y0+yy;   } //top
	                else { nx = x0+yy;     ny = y0+xx;   }
	                break;
	            case 4:
	                if (i < 3) { ny = y1+x-1-xx; nz = z1+yy;   } //right
	                else { ny = y1+y-1-yy; nz = z1+xx;   }
	                break;
	            case 5:
	                if (i < 3) { ny = y0+xx;     nz = z0+yy;   } //left
	                else { ny = y0+yy;     nz = z0+xx;   }
	                break;
	            }
	           
	            gvox.mytex[lptr + xx] = getvox(nx,ny,nz);
	        }

	    qptr = gvox.quad[gvox.qcnt];
	    qptr.v[0].x = x0; qptr.v[0].y = y0; qptr.v[0].z = z0;
	    qptr.v[1].x = x1; qptr.v[1].y = y1; qptr.v[1].z = z1;
	    qptr.v[2].x = x2; qptr.v[2].y = y2; qptr.v[2].z = z2;
	    for (j=0; j<3; j++) { qptr.v[j].u = (int) shp[z].x; qptr.v[j].v = (int) shp[z].y; }
	    if (i < 3) qptr.v[1].u += x; else qptr.v[1].v += y;
	    qptr.v[2].u += x; qptr.v[2].v += y;

	    qptr.v[3].u = qptr.v[0].u - qptr.v[1].u + qptr.v[2].u;
	    qptr.v[3].v = qptr.v[0].v - qptr.v[1].v + qptr.v[2].v;
	    qptr.v[3].x = qptr.v[0].x - qptr.v[1].x + qptr.v[2].x;
	    qptr.v[3].y = qptr.v[0].y - qptr.v[1].y + qptr.v[2].y;
	    qptr.v[3].z = qptr.v[0].z - qptr.v[1].z + qptr.v[2].z;
	    if (gvox.qfacind[face] < 0) gvox.qfacind[face] = gvox.qcnt;

	    int vertexOffset = vertices.size / 3;
	    for(i = 0; i < 4; i++) {
	    	vertices.addAll(qptr.v[i].x, qptr.v[i].y, qptr.v[i].z);
	    	uvs.addAll(qptr.v[i].u / (float)gvox.mytexx, qptr.v[i].v / (float)gvox.mytexy);
	    }
	    indicies.addAll(new short[] {(short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset) });
	    
	    gvox.qcnt++;
	}
	
	private static VOXModel vox2poly(Voxel vox, int mip)
	{
		int i, j, x, y, z, v, ov, oz = 0, cnt, sc, x0, y0, dx, dy, bx0[], by0[];
		
		xsiz = vox.xsiz[mip];
    	ysiz = vox.ysiz[mip];
    	zsiz = vox.zsiz[mip];
    	
		yzsiz = ysiz*zsiz;
		int[] vbit = new int[((xsiz*yzsiz+31)>>3)+1]; //vbit: 1 bit per voxel: 0=air, 1=solid
		vcol = new HashMap<Integer, Byte>();
		pow2m1 = new int[33];
		
	    int cptr = 0;
	    int zleng, ztop, z1;
	    for (x = 0; x < xsiz; x++) //Set surface voxels to 1 else 0
	        for (y = 0, j = x * yzsiz; y < ysiz; y++, j += zsiz)
	        {
	        	int voxptr = vox.xyoffs[mip][x][y];
				int voxend = vox.xyoffs[mip][x][y+1];

	            z1 = 0;
	            while (voxptr < voxend)
	            {
	            	ztop = vox.data[mip][cptr] & 0xFF; 
	            	zleng = vox.data[mip][cptr + 1] & 0xFF;
	                if ((vox.data[mip][cptr + 2] & 16) == 0)
	                	setzrange1(vbit,j+z1,j+ztop);
	                z1 = ztop + zleng;
	                setzrange1(vbit,j+ztop,j+z1);
	                cptr += 3; //voxel color
	                for (z = ztop; z < z1; z++) {
	                	if(cptr >= vox.data[mip].length) break;
	                	putvox(x,y,z,vox.data[mip][cptr++]);
	                }
	                
	                voxptr += zleng + 3; 
	            }
	        }
	    
		VOXModel gvox = new VOXModel();
	   
	    //x is largest dimension, y is 2nd largest dimension
	    x = xsiz; y = ysiz; z = zsiz;
	    
	    if ((x < y) && (x < z)) x = z; else if (y < z) y = z;
	    if (x < y) { z = x; x = y; y = z; }
	    shcntp = x; i = x*y;
	    shcntmal = new int[i];
	    shcnt = -shcntp-1;
	    gmaxx = gmaxy = garea = 0;

	    if (pow2m1[32] != -1)  { 
	    	for (i = 0; i < 32; i++) 
	    		pow2m1[i] = (1<<i)-1; 
	    	pow2m1[32] = -1; 
	    }
	    
	    for (i = 0; i < 7; i++) 
	    	gvox.qfacind[i] = -1;

	    i = ((max(ysiz,zsiz)+1)<<2);
	    bx0 = new int[i<<1];
	    by0 = new int[i<<1];
	    
	    for (cnt = 0; cnt < 2; cnt++)
	    {
	        gvox.qcnt = 0;
	        
	        Arrays.fill(by0, -1); v = 0;
	        
	        for (i=-1; i<=1; i+=2) //add x surfaces
	            for (y=0; y<ysiz; y++)
	                for (x=0; x<=xsiz; x++)
	                    for (z=0; z<=zsiz; z++)
	                    {
	                        ov = v; v = (isolid(vbit, x,y,z) != 0 && (isolid(vbit, x,y+i,z) == 0))?1:0;
	                        if ((by0[z] >= 0) && ((by0[z] != oz) || (v >= ov)))
	                            { daquad(gvox, cnt, bx0[z],y,by0[z],x,y,by0[z],x,y,z,(i>=0)?1:0); by0[z] = -1; }
	                        if (v > ov) oz = z; else if ((v < ov) && (by0[z] != oz)) { bx0[z] = x; by0[z] = oz; }
	                    }
	        
	        for (i=-1; i<=1; i+=2) //add z surfaces
	            for (z=0; z<zsiz; z++)
	                for (x=0; x<=xsiz; x++)
	                    for (y=0; y<=ysiz; y++)
	                    {
	                        ov = v; v = (isolid(vbit, x,y,z) != 0 && (isolid(vbit, x,y,z-i) == 0))?1:0;
	                        if ((by0[y] >= 0) && ((by0[y] != oz) || (v >= ov)))
	                            { daquad(gvox, cnt, bx0[y],by0[y],z,x,by0[y],z,x,y,z,((i>=0)?1:0)+2); by0[y] = -1; }
	                        if (v > ov) oz = y; else if ((v < ov) && (by0[y] != oz)) { bx0[y] = x; by0[y] = oz; }
	                    }
	        
	        for (i=-1; i<=1; i+=2) //add y surfaces
	            for (x=0; x<xsiz; x++)
	                for (y=0; y<=ysiz; y++)
	                    for (z=0; z<=zsiz; z++)
	                    {
	                        ov = v; v = (isolid(vbit, x,y,z) != 0 && (isolid(vbit, x-i,y,z) == 0))?1:0;
	                        if ((by0[z] >= 0) && ((by0[z] != oz) || (v >= ov)))
	                            { daquad(gvox, cnt, x,bx0[z],by0[z],x,y,by0[z],x,y,z,((i>=0)?1:0)+4); by0[z] = -1; }
	                        if (v > ov) oz = z; else if ((v < ov) && (by0[z] != oz)) { bx0[z] = y; by0[z] = oz; }
	                    }

	        if(cnt == 0)
	        {
	        	shp = new Vector2[gvox.qcnt];
	        	for(int vc = 0; vc < gvox.qcnt; vc++)
	        		shp[vc] = new Vector2();
	
	        	sc = 0;
	            for (y=gmaxy; y != 0; y--)
	                for (x=gmaxx; x>=y; x--)
	                {
	                    i = shcntmal[shcnt+y*shcntp+x]; shcntmal[shcnt+y*shcntp+x] = sc; //shcnt changes from counter to head index
	                    for (; i>0; i--) { shp[sc].x = x; shp[sc].y = y; sc++; }
	                }
	            
	            for (gvox.mytexx=32; gvox.mytexx< gmaxx; gvox.mytexx<<=1);
	            for (gvox.mytexy=32; gvox.mytexy< gmaxy; gvox.mytexy<<=1);
	            
	            while (gvox.mytexx*gvox.mytexy*8 < garea*9) //This should be sufficient to fit most skins...
	                if (gvox.mytexx <= gvox.mytexy) gvox.mytexx <<= 1; else gvox.mytexy <<= 1;

	            mytexo5 = (gvox.mytexx>>5);
	            i = (((gvox.mytexx*gvox.mytexy+31)>>5)<<2);
	            zbit = new int[i];
	            v = gvox.mytexx*gvox.mytexy;
	       
	            skindidntfit:
	            for (z=0; z<sc; z++)
	            {
	                dx = (int) shp[z].x; dy = (int) shp[z].y; i = v;
	                do
	                {
	                	int a = (int) (Math.random()*32767);
	                	int b = (int) (Math.random()*32767);

	                    x0 = ((a*(gvox.mytexx+1-dx))>>15);
	                    y0 = ((b*(gvox.mytexy+1-dy))>>15);
	
	                    if (--i < 0) //Time-out! Very slow if this happens... but at least it still works :P
	                    {
	                    	Arrays.fill(zbit, 0);
	                        //Re-generate shp[].x/y (box sizes) from shcnt (now head indices) for next pass :/
	                        j = 0;
	                        for (y=gmaxy; y != 0; y--)
	                            for (x=gmaxx; x>=y; x--)
	                            {
	                                i = shcntmal[shcnt+y*shcntp+x];
	                                for (; j<i; j++) { shp[j].x = x0; shp[j].y = y0; }
	                                x0 = x; y0 = y;
	                            }
	                        for (; j<sc; j++) { shp[j].x = x0; shp[j].y = y0; }
	                        
	                        if (gvox.mytexx <= gvox.mytexy) gvox.mytexx <<= 1; else gvox.mytexy <<= 1;
	        	            mytexo5 = (gvox.mytexx>>5);
	        	            i = (((gvox.mytexx*gvox.mytexy+31)>>5)<<2);
	        	            v = gvox.mytexx*gvox.mytexy;
	        	            z = -1;
	        	            continue skindidntfit;
	                    }
	                }
	                while (isrectfree(x0,y0,dx,dy) == 0);
	                while ((y0 != 0) && (isrectfree(x0,y0-1,dx,1) != 0)) y0--;
	                while ((x0 != 0) && (isrectfree(x0-1,y0,1,dy) != 0)) x0--;
	                setrect(x0,y0,dx,dy);
	                shp[z].x = x0; shp[z].y = y0; //Overwrite size with top-left location
	            }
	 
	            gvox.quad = new voxrect_t[gvox.qcnt];
                gvox.initQuads();
                gvox.mytex = new byte[gvox.mytexx*gvox.mytexy];
	        }
	    }
	    return gvox;
	}
	
	private static int isrectfree(int x0, int y0, int dx, int dy)
	{
		int i, c, m, m1, x;

	    i = y0*mytexo5 + (x0>>5); dx += x0-1; 

	    c = (dx>>5) - (x0>>5);
	    m = ~pow2m1[x0&31]; m1 = pow2m1[(dx&31)+1];
	    if (c == 0) { for (m&=m1; dy != 0; dy--,i+=mytexo5) if ((zbit[i]&m) != 0) return(0); }
	    else
	    {
	        for (; dy != 0; dy--,i+=mytexo5)
	        {
	            if ((zbit[i]&m) != 0) return(0);
	            for (x=1; x<c; x++) if (zbit[i+x] != 0) return(0);
	            if ((zbit[i+x]&m1) != 0) return(0);
	        }
	    }

	    return(1);
	}
	
	private static void dispose()
	{
        vertices = null;
        indicies = null;
        uvs = null;
        vcol = null;
        shp = null;
        zbit = null;
        shcntmal = null;
        pow2m1 = null;
        pal = null;
	}
}
