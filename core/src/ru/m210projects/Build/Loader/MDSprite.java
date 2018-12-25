/*
* MDSprite for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been modified
* by the EDuke32 team (development@voidpoint.com)
* by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.setupBoundTexture;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.setupBoundTextureWrap;
import static ru.m210projects.Build.Render.Types.GL10.*;
import static ru.m210projects.Build.Strhandler.Bstrcasecmp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;

import ru.m210projects.Build.DefScript;
import ru.m210projects.Build.Loader.MD2.MD2Model;
import ru.m210projects.Build.Loader.MD3.MD3Model;
import ru.m210projects.Build.Loader.Voxels.KVXLoader;
import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.TextureHandle.BTexture;
import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Render.Types.Spriteext;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile2model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

public class MDSprite {
	
	public static Spritesmooth[] spritesmooth;
	public static class Spritesmooth {
		public float smoothduration;
		public short mdcurframe;
		public short mdoldframe;
		public short mdsmooth;
	}
	
	public static final int MDANIM_LOOP = 0;
	public static final int MDANIM_ONESHOT = 1;
	
//	public static List<Model> models;
	private static HashMap<Integer, Model> models;
	public static Hudtyp[][] hudmem; //XXX
	
//	public static VOXModel[] voxmodels = new VOXModel[MAXVOXELS];
	
	public static int mdinited;
	public static int mdpause;
//	public static int curextra=MAXTILES;
	
	public static int globalnoeffect = 0;
	
	public static long mdtims, omdtims;
	public static int allocvbos = 0, curvbo = 0;
	public static IntBuffer vertvbos;
	public static IntBuffer indexvbos;
	private static Tile2model[] tile2model;
	
	public static void md_freevbos()
	{
	    for (int i = 0; i < models.size(); i++)
	        if (models.get(i) != null && models.get(i).mdnum == 3)
	        {
	            MD3Model m = (MD3Model) models.get(i);
	            if (m.vbos != null)
	            {
	                //            OSD_Printf("freeing model %d vbo\n",i);
	                Gdx.gl.glDeleteBuffers(0, m.vbos);
	                m.vbos = null;
	            }
	        }

	    if (allocvbos != 0)
	    {
	    	Gdx.gl.glDeleteBuffers(0, indexvbos);
	    	Gdx.gl.glDeleteBuffers(0, vertvbos);
	        allocvbos = 0;
	    }
	}
	
//	public static int qloadkvx(int voxindex, String filename)
//	{
//		int i, dasiz, lengcnt, lengtot;
//
//		ByteBuffer buffer = kGetBuffer(filename, 0);
//		if(buffer == null) return -1;
//
//		lengcnt = 0;
//		lengtot = buffer.capacity();
//		
//		buffer.order( ByteOrder.LITTLE_ENDIAN);
//
//		for(i=0;i<MAXVOXMIPS;i++)
//		{
//			dasiz = buffer.getInt();
//			lengcnt += dasiz+4;
//			if (lengcnt >= lengtot-768) break;
//		}
//		
//		if (voxmodels[voxindex] != null)
//	    {
//	        voxfree(voxmodels[voxindex]);
//	        voxmodels[voxindex] = null;
//	    }
//		buffer.rewind();
//	    voxmodels[voxindex] = KVXLoader.load(buffer);
//		return 0;
//	}
	
	public static void voxfree(VOXModel m)
	{
	    if (m == null) return;
	    if (m.mytex != null) m.mytex = null;
	    if (m.quad != null) m.quad = null;
	    if (m.texid != null) m.texid = null;
	    m = null;
	}
	
	// VBO generation and allocation
	public static void mdloadvbos(MD3Model m)
	{
	    m.vbos = BufferUtils.newIntBuffer(m.head.numSurfaces);
	    Gdx.gl.glGenBuffers(0, m.vbos);

	    int i = 0;
	    while (i < m.head.numSurfaces)
	    {
	    	 Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, m.vbos.get(i));
	    	 Gdx.gl.glBufferData(GL_ARRAY_BUFFER, 0, m.surfaces[i].uv, GL_STATIC_DRAW);
	    	 i++;
	    }
	    Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public static void md_allocvbos()
	{
		int r_vbocount = Console.Geti("r_vbocount");
	    indexvbos = BufferUtils.newIntBuffer(r_vbocount);
	    vertvbos = BufferUtils.newIntBuffer(r_vbocount);

	    if (r_vbocount != allocvbos)
	    {
	    	indexvbos.position(allocvbos);
	        Gdx.gl.glGenBuffers(0, indexvbos);
	        vertvbos.position(allocvbos);
	        Gdx.gl.glGenBuffers(0, vertvbos);

	        int i = allocvbos;
	        while (i < r_vbocount)
	        {
	        	Gdx.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexvbos.get(i)); //maxmodeltris * 3 XXX
	        	Gdx.gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexvbos.capacity(), null, GL_STREAM_DRAW);
	        	Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, vertvbos.get(i)); //maxmodelverts
	        	Gdx.gl.glBufferData(GL_ARRAY_BUFFER, vertvbos.capacity(), null, GL_STREAM_DRAW);
	            i++;
	        }

	        Gdx.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

	        allocvbos = r_vbocount;
	    }
	}

	public static void freevbos()
	{
	    int i;

	    if(mdinited == 0)
	    	return;
	    
	    for (i=0; i<models.size(); i++) {
	    	Model mdl = models.get(i);
	    	if (mdl != null && mdl.mdnum == 3)
	        {
	            MD3Model m = (MD3Model) mdl;
	            if (m.vbos != null)
	            {
//	                Gdx.gl.glDeleteBuffersARB(m.head.numsurfs, m.vbos);
	                m.vbos = null; //Bfree(m.vbos);
	            }
	        }
	    }

//	    if (allocvbos)
//	    {
//	    	Gdx.gl.glDeleteBuffersARB(allocvbos, indexvbos);
//	    	Gdx.gl.glDeleteBuffersARB(allocvbos, vertvbos);
//	        allocvbos = 0;
//	    }
	}
	
	public static void mdinit()
	{
		models = new HashMap<Integer, Model>();
		hudmem = new Hudtyp[2][MAXTILES];
		tile2model = new Tile2model[MAXTILES];
		spritesmooth = new Spritesmooth[MAXSPRITES+MAXUNIQHUDID];
		
		for (int i = 0; i < spritesmooth.length; i++)
			spritesmooth[i] = new Spritesmooth();
		
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < MAXTILES; j++)
				hudmem[i][j] = new Hudtyp();

		for(int i = 0; i < tile2model.length; i++)
			tile2model[i] = new Tile2model();

	    mdinited = 1;
	}

	public static BTexture mdloadskin(DefScript defs, MDModel m, int number, int pal, int surf)
	{
	    String skinfile = null;
	    BTexture texidx = null;
	    BTexture[] texptr = null;
	    int idptr = -1;
	    MDSkinmap sk, skzero = null;
	    long startticks;

	    if (m.mdnum == 2)
	        surf = 0;
	    
	    if (pal >= MAXPALOOKUPS || defs == null)
	    	return null;
	    

	    for (sk = m.skinmap; sk != null; sk = sk.next)
	    {
	    	int i = -1;
	    	if (sk.palette == pal && sk.skinnum == number && sk.surfnum == surf)
	        {
	            skinfile = sk.fn;
	            idptr = defs.hiresInfo.getPaletteEffect(pal);
	            texptr = sk.texid;
	            if(texptr != null)
	            	texidx = texptr[idptr];
	            //OSD_Printf("Using exact match skin (pal=%d,skinnum=%d,surfnum=%d) %s\n",pal,number,surf,skinfile);
	            break;
	        }
	        //If no match, give highest priority to number, then pal.. (Parkar's request, 02/27/2005)
	        else if ((sk.palette ==   0) && (sk.skinnum == number) && (sk.surfnum == surf) && (i < 5)) { i = 5; skzero = sk; }
	        else if ((sk.palette == pal) && (sk.skinnum ==      0) && (sk.surfnum == surf) && (i < 4)) { i = 4; skzero = sk; }
	        else if ((sk.palette ==   0) && (sk.skinnum ==      0) && (sk.surfnum == surf) && (i < 3)) { i = 3; skzero = sk; }
	        else if ((sk.palette ==   0) && (sk.skinnum == number) && (i < 2)) { i = 2; skzero = sk; }
	        else if ((sk.palette == pal) && (sk.skinnum ==      0) && (i < 1)) { i = 1; skzero = sk; }
	        else if ((sk.palette ==   0) && (sk.skinnum ==      0) && (i < 0)) { i = 0; skzero = sk; }
	    }

	    if (sk == null)
	    {
	        if (pal >= (MAXPALOOKUPS - RESERVEDPALS))
	            return null;

	        if (skzero != null)
	        {
	            skinfile = skzero.fn;
	            idptr = defs.hiresInfo.getPaletteEffect(pal);
	            texptr = skzero.texid;
	            if(texptr != null)
	            	texidx = texptr[idptr];
	            //OSD_Printf("Using def skin 0,0 as fallback, pal=%d\n", pal);
	        }
	        else
	        {
	            if (number >= m.numskins)
	                number = 0;

	            if(m.mdnum != 2) {
	            	Console.Println("Couldn't load skin", OSDTEXT_YELLOW);
	            	defs.mdInfo.removeModelInfo(m);
	            	return null;
	            }

	            MD2Model md = (MD2Model) m;

	            skinfile = md.skinfn + number*64;
	            idptr = number*m.texid.length + defs.hiresInfo.getPaletteEffect(pal);
	            texptr = m.texid;
	            if(texptr != null)
	            	texidx = texptr[idptr];
	            //OSD_Printf("Using MD2/MD3 skin (%d) %s, pal=%d\n",number,skinfile,pal);
	        }
	    }
	    
	    if (skinfile == null)
	        return null;

	    if (texidx != null)
	        return texidx;
	    
	    // possibly fetch an already loaded multitexture :_)
	    if (pal >= (MAXPALOOKUPS - RESERVEDPALS))
	        for (int i=0; i < models.size(); i++) {
	        	if(models.get(i) == null || models.get(i).mdnum < 2)
	        		continue;
	        	MDModel mi = (MDModel) models.get(i);
	            for (skzero = mi.skinmap; skzero != null; skzero = skzero.next)
	                if (Bstrcasecmp(skzero.fn, sk.fn) == 0 && skzero.texid[defs.hiresInfo.getPaletteEffect(pal)] != null)
	                {
	                    int f = defs.hiresInfo.getPaletteEffect(pal);
	                    sk.texid[f] = skzero.texid[f];
	                    return sk.texid[f];
	                }
	        }

	    texidx = null;
	   
	    if (!kExist(skinfile, 0))
	    {
	    	Console.Println("Skin " + skinfile  + " not found.", OSDTEXT_YELLOW);
	    	defs.mdInfo.removeModelInfo(m);
	        skinfile = null;
	        return null;
	    }

	    startticks = System.currentTimeMillis();
	    try {
	    	byte[] data = kGetBytes(skinfile, 0);
			Pixmap pix = new Pixmap(data, 0, data.length);
			texidx = new BTexture(pix, true); 
	    	m.usesalpha = true;
	    } catch(Exception e) {
	    	Console.Println("Couldn't load file: " + skinfile, OSDTEXT_YELLOW);
	    	defs.mdInfo.removeModelInfo(m);
	        skinfile = null;
	    	return null;
	    }
	    
		int gltexfiltermode = Console.Geti("r_texturemode");

		setupBoundTexture(gltexfiltermode, GLInfo.anisotropy());
		setupBoundTextureWrap(GL_REPEAT);

	    long etime = System.currentTimeMillis()-startticks;
	    
	    System.out.println("Load skin: p" + pal +  "-e" + defs.hiresInfo.getPaletteEffect(pal) + " \"" + skinfile + "\"... " + etime + " ms");

        texptr[idptr] = texidx;
	    return texidx;
	}

}

