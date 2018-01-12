package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.FileHandle.Cache1D.kClose;
import static ru.m210projects.Build.FileHandle.Cache1D.kFileLength;
import static ru.m210projects.Build.FileHandle.Cache1D.kGetBuffer;
import static ru.m210projects.Build.FileHandle.Cache1D.kGetBytes;
import static ru.m210projects.Build.FileHandle.Cache1D.kOpen;
import static ru.m210projects.Build.FileHandle.Cache1D.kRead;
import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.Render.GL10.GL_ARRAY_BUFFER_ARB;
import static ru.m210projects.Build.Render.GL10.GL_ELEMENT_ARRAY_BUFFER_ARB;
import static ru.m210projects.Build.Render.GL10.GL_REPEAT;
import static ru.m210projects.Build.Render.GL10.GL_STREAM_DRAW_ARB;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_2D;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_MAG_FILTER;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_MIN_FILTER;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_WRAP_S;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_WRAP_T;
import static ru.m210projects.Build.Render.Polymost.r_animsmoothing;
import static ru.m210projects.Build.Render.TextureUtils.getGlFilter;
import static ru.m210projects.Build.Strhandler.Bstrcasecmp;
import static ru.m210projects.Build.Strhandler.Bstrcmp;
import static ru.m210projects.Build.Types.Hightile.HICEFFECTMASK;
import static ru.m210projects.Build.Types.Hightile.hictinting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.m210projects.Build.Loader.MD2.MD2Frame;
import ru.m210projects.Build.Loader.MD2.MD2Loader;
import ru.m210projects.Build.Loader.MD2.MD2Model;
import ru.m210projects.Build.Loader.MD3.MD3Frame;
import ru.m210projects.Build.Loader.MD3.MD3Loader;
import ru.m210projects.Build.Loader.MD3.MD3Model;
import ru.m210projects.Build.Loader.Voxels.KVXLoader;
import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GL10;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Hudtyp;
import ru.m210projects.Build.Types.Spriteext;
import ru.m210projects.Build.Types.Spritesmooth;
import ru.m210projects.Build.Types.Tile2model;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

public class MDSprite {
	
	public static final int MDANIM_LOOP = 0;
	public static final int MDANIM_ONESHOT = 1;
	
	public static List<Model> models;
	public static Hudtyp[][] hudmem;
	
	public static VOXModel[] voxmodels = new VOXModel[MAXVOXELS];
	
	public static int mdinited;
	public static int mdpause;
	public static int curextra=MAXTILES;
	
	public static int globalnoeffect = 0;
	
	public static long mdtims, omdtims;
	public static int allocvbos = 0, curvbo = 0;
	public static IntBuffer vertvbos;
	public static IntBuffer indexvbos;
	
	public static void md_freevbos(GL10 gl)
	{
	    for (int i = 0; i < models.size(); i++)
	        if (models.get(i).mdnum == 3)
	        {
	            MD3Model m = (MD3Model) models.get(i);
	            if (m.vbos != null)
	            {
	                //            OSD_Printf("freeing model %d vbo\n",i);
	                gl.bglDeleteBuffersARB(m.vbos);
	                m.vbos = null;
	            }
	        }

	    if (allocvbos != 0)
	    {
	        gl.bglDeleteBuffersARB(indexvbos);
	        gl.bglDeleteBuffersARB(vertvbos);
	        allocvbos = 0;
	    }
	}
	
	public static int qloadkvx(int voxindex, String filename)
	{
		int i, dasiz, lengcnt, lengtot;

		ByteBuffer buffer = kGetBuffer(filename, 0);
		if(buffer == null) return -1;

		lengcnt = 0;
		lengtot = buffer.capacity();
		
		buffer.order( ByteOrder.LITTLE_ENDIAN);

		for(i=0;i<MAXVOXMIPS;i++)
		{
			dasiz = buffer.getInt();
			lengcnt += dasiz+4;
			if (lengcnt >= lengtot-768) break;
		}
		
		if (voxmodels[voxindex] != null)
	    {
	        voxfree(voxmodels[voxindex]);
	        voxmodels[voxindex] = null;
	    }
		buffer.rewind();
	    voxmodels[voxindex] = KVXLoader.load(buffer);
		return 0;
	}
	
	public static void voxfree(VOXModel m)
	{
	    if (m == null) return;
	    if (m.mytex != null) m.mytex = null;
	    if (m.quad != null) m.quad = null;
	    if (m.texid != null) m.texid = null;
	    m = null;
	}
	
	// VBO generation and allocation
	public static void mdloadvbos(MD3Model m, GL10 gl)
	{
	    m.vbos = BufferUtils.newIntBuffer(m.head.numSurfaces);
	    gl.bglGenBuffersARB(m.vbos);

	    int i = 0;
	    while (i < m.head.numSurfaces)
	    {
	    	gl.bglBindBufferARB(GL_ARRAY_BUFFER_ARB, m.vbos.get(i));
//XXX	    	gl.bglBufferDataARB(GL_ARRAY_BUFFER_ARB, m.surfaces[i].uv, GL_STATIC_DRAW_ARB);
	        i++;
	    }
	    gl.bglBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
	}
	
	public static void md_allocvbos(GL10 gl)
	{
		int r_vbocount = Console.Geti("r_vbocount");
	    indexvbos = BufferUtils.newIntBuffer(r_vbocount);
	    vertvbos = BufferUtils.newIntBuffer(r_vbocount);

	    if (r_vbocount != allocvbos)
	    {
	    	indexvbos.position(allocvbos);
	        gl.bglGenBuffersARB(indexvbos);
	        vertvbos.position(allocvbos);
	        gl.bglGenBuffersARB(vertvbos);

	        int i = allocvbos;
	        while (i < r_vbocount)
	        {
	            gl.bglBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexvbos.get(i)); //maxmodeltris * 3 XXX
	            gl.bglBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB, null, GL_STREAM_DRAW_ARB);
	            gl.bglBindBufferARB(GL_ARRAY_BUFFER_ARB, vertvbos.get(i)); //maxmodelverts
	            gl.bglBufferDataARB(GL_ARRAY_BUFFER_ARB, null, GL_STREAM_DRAW_ARB);
	            i++;
	        }

	        gl.bglBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
	        gl.bglBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);

	        allocvbos = r_vbocount;
	    }
	}
	
	public static int md_definehud(int modelid, int tilex, double xadd, double yadd, double zadd, double angadd, int flags, int fov)
	{
	    if (mdinited == 0) mdinit();

	    if (tilex >= MAXTILES) return -2;

	    hudmem[(flags>>2)&1][tilex].xadd = (float) xadd;
	    hudmem[(flags>>2)&1][tilex].yadd = (float) yadd;
	    hudmem[(flags>>2)&1][tilex].zadd = (float) zadd;
	    hudmem[(flags>>2)&1][tilex].angadd = (short) (((short)angadd)|2048);
	    hudmem[(flags>>2)&1][tilex].flags = (short)flags;
	    hudmem[(flags>>2)&1][tilex].fov = (short)fov;

	    return 0;
	}
	
	public static int md_defineanimation(int modelid, String framestart, String frameend, int fpssc, int flags)
	{
	    MDAnimation ma = new MDAnimation();
	    int i = 0;

	    if (mdinited == 0) mdinit();
	    if (models.get(modelid).mdnum < 2) return 0;
	    
	    MDModel m = (MDModel) models.get(modelid);

	    //find index of start frame
	    i = framename2index(m,framestart);
	    if (i == m.numframes) return -2;
	    ma.startframe = i;

	    //find index of finish frame which must trail start frame
	    i = framename2index(m,frameend);
	    if (i == m.numframes) return -3;
	    ma.endframe = i;

	    ma.fpssc = fpssc;
	    ma.flags = flags;

	    ma.next = m.animations;
	    m.animations = ma;

	    return(0);
	}

	public static void freevbos()
	{
	    int i;

	    if(mdinited == 0)
	    	return;
	    
	    for (i=0; i<models.size(); i++)
	        if (models.get(i).mdnum == 3)
	        {
	            MD3Model m = (MD3Model) models.get(i);
	            if (m.vbos != null)
	            {
//	                Gdx.gl.glDeleteBuffersARB(m.head.numsurfs, m.vbos);
	                m.vbos = null; //Bfree(m.vbos);
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
		models = new ArrayList<Model>();
		hudmem = new Hudtyp[2][MAXTILES];
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < MAXTILES; j++)
				hudmem[i][j] = new Hudtyp();

		for(int i = 0; i < tile2model.length; i++)
			tile2model[i] = new Tile2model();

	    mdinited = 1;
	}
	
	public static int addtileP(int model,int tile,int pallet)
	{
	    if (curextra==MAXTILES+EXTRATILES-1)
	    {
	    	Console.Println("warning: max EXTRATILES reached", OSDTEXT_YELLOW);
	        return curextra;
	    }

	    if (tile2model[tile].modelid==-1)
	    {
	        tile2model[tile].pal=pallet;
	        return tile;
	    }

	    if (tile2model[tile].pal==pallet)
	        return tile;

	    while (tile2model[tile].next!=-1)
	    {
	        tile=tile2model[tile].next;
	        if (tile2model[tile].pal==pallet)
	            return tile;
	    }

	    tile2model[tile].next=curextra;
	    tile2model[curextra].pal=pallet;

	    return curextra++;
	}
	
	public static int md_defineframe(int modelid, String framename, int tilenume, int skinnum, float smoothduration, int pal)
	{
	    if (mdinited == 0) mdinit();

	    if (tilenume >= MAXTILES) return(-2);
	    if (framename == null) return(-3);

	    tilenume=addtileP(modelid,tilenume,pal);
	    Model m = models.get(modelid);

	    if (m.mdnum == 1)
	    {
	        tile2model[tilenume].modelid = modelid;
	        tile2model[tilenume].framenum = tile2model[tilenume].skinnum = 0;
	        return 0;
	    }
	
	    int i = framename2index(m, framename);	

	    tile2model[tilenume].modelid = modelid;
	    tile2model[tilenume].framenum = i;
	    tile2model[tilenume].skinnum = skinnum;
	    tile2model[tilenume].smoothduration = smoothduration;

	    return i;
	}
	
	public static int framename2index(Model vm, String nam)
	{
		int i = 0;
	    switch (vm.mdnum)
	    {
		    case 2:
		    {
		        MD2Model m = (MD2Model)vm;
		        for (i = 0; i < m.numframes; i++)
		        {
		        	MD2Frame fr = m.frames[i];
		            if (Bstrcmp(fr.name, nam) == 0) break;
		        }
		        if (i == m.numframes) return(-3); // frame name invalid
		    }
		    break;
		    case 3:
		    {
		    	MD3Model m = (MD3Model) vm;
		    	for (i = 0; i < m.numframes; i++)
		        {
		        	MD3Frame fr = m.frames[i];
		            if (Bstrcmp(fr.name, nam) == 0) break;
		        }
		        if (i == m.numframes) return(-3); // frame name invalid
		    }
		    break;
	    }
	    return(i);
	}
	
	public static int md_setmisc(int modelid, float scale, int shadeoff, float zadd, float yoffset, int flags)
	{
	    if (mdinited == 0) mdinit();

	    Model m = models.get(modelid);
	    m.bscale = scale;
	    m.shadeoff = shadeoff;
	    m.zadd = zadd;
	    m.yoffset = yoffset;
	    m.flags = flags;

	    return 0;
	}
	
	public static int md_undefinemodel(int modelid)
	{

	    if (mdinited == 0) return 0;

	    for (int i=MAXTILES+EXTRATILES-1; i>=0; i--)
	        if (tile2model[i].modelid == modelid)
	            tile2model[i].modelid = -1;

	    models.remove(modelid);

	    return 0;
	}
	
	public static int md_loadmodel(String fn)
	{
	    if (mdinited == 0) mdinit();

	    Model vm = mdload(fn); 
	    if (vm == null) return(-1);
	    models.add(vm);
	    vm.modelid = models.size()-1;

	    return vm.modelid;
	}

	public static Model mdload(String filnam)
	{
	    int fil = kOpen(filnam, 0); if (fil < 0) return null;
	    byte[] buf = new byte[kFileLength(fil)];
	    kRead(fil, buf, buf.length);
	    kClose(fil);
	    
	    ByteBuffer bb = ByteBuffer.wrap(buf);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	
    	Model vm = null;
    	if (Bstrcasecmp(BfileExtension(filnam),"kvx") == 0) 
		    if ((vm = KVXLoader.load(bb)) != null) 
		    	return(vm);

	    switch (bb.getInt(0))
	    {
		    case 0x32504449: //IDP2
		        vm = MD2Loader.load(bb);
		        break;
		    case 0x33504449: //IDP3
		        vm = MD3Loader.load(bb);
		        break; 
		    default:
		        vm = null; break;
	    }

//	    if (vm != null) XXX
//	    {
//	        md3model_t vm3 = (md3model_t)vm;
//
//	        // smuggle the file name into the model struct.
//	        // head.nam is unused as far as I can tell
//	        Bstrncpyz(vm3.head.nam, filnam, sizeof(vm3.head.nam));
//
//	        md3postload_common(vm3);
//	    }

	    return(vm);
	}

	public static void updateanimation(MDModel m, SPRITE tspr, int lpal)
	{
		MDAnimation anim;
	    int i, j, k;
	    int fps;

	    int tile;
	    boolean smoothdurationp;
	    Spritesmooth smooth;
	    Spriteext sprext;

	    if (m.numframes < 2)
	    {
	        m.interpol = 0;
	        return;
	    }

	    tile = Ptile2tile(tspr.picnum,lpal);
	    m.cframe = m.nframe = tile2model[tile].framenum;

	    smoothdurationp = (r_animsmoothing != 0 && (tile2model[tile].smoothduration != 0));

	    smooth = (tspr.owner < MAXSPRITES+MAXUNIQHUDID) ? spritesmooth[tspr.owner] : null;
	    sprext = (tspr.owner < MAXSPRITES+MAXUNIQHUDID) ? spriteext[tspr.owner] : null;

	    for (anim = m.animations; anim != null && anim.startframe != m.cframe; anim = anim.next)
	    {
	        /* do nothing */;
	    }

	    if (anim == null)
	    {
	        if (!smoothdurationp || ((smooth.mdoldframe == m.cframe) && (smooth.mdcurframe == m.cframe)))
	        {
	            m.interpol = 0;
	            return;
	        }

	        if (smooth.mdoldframe != m.cframe)
	        {
	            if (smooth.mdsmooth == 0)
	            {
	                sprext.mdanimtims = mdtims;
	                m.interpol = 0;
	                smooth.mdsmooth = 1;
	                smooth.mdcurframe = (short) m.cframe;
	            }

	            if (smooth.mdcurframe != m.cframe)
	            {
	                sprext.mdanimtims = mdtims;
	                m.interpol = 0;
	                smooth.mdsmooth = 1;
	                smooth.mdoldframe = smooth.mdcurframe;
	                smooth.mdcurframe = (short) m.cframe;
	            }
	        }
	        else 
	        {
	            sprext.mdanimtims = mdtims;
	            m.interpol = 0;
	            smooth.mdsmooth = 1;
	            smooth.mdoldframe = smooth.mdcurframe;
	            smooth.mdcurframe = (short) m.cframe;
	        }
	    }
	    else if (/* anim && */ sprext.mdanimcur != anim.startframe)
	    {
	        sprext.mdanimcur = (short) anim.startframe;
	        sprext.mdanimtims = mdtims;
	        m.interpol = 0;

	        if (!smoothdurationp)
	        {
	            m.cframe = m.nframe = anim.startframe;
	            return;
	        }

	        m.nframe = anim.startframe;
	        m.cframe = smooth.mdoldframe;
	        smooth.mdsmooth = 1;
	        return;
	    }

	    fps = (smooth.mdsmooth != 0) ? Math.round((1.0f / (float) (tile2model[tile].smoothduration)) * 66.f) : anim.fpssc;

	    i = (int) ((mdtims - sprext.mdanimtims)*((fps*timerticspersec)/120));

	    if (smooth.mdsmooth != 0)
	        j = 65536;
	    else
	        j = ((anim.endframe+1-anim.startframe)<<16);
	    // Just in case you play the game for a VERY long time...
	    if (i < 0) { i = 0; sprext.mdanimtims = mdtims; }
	    //compare with j*2 instead of j to ensure i stays > j-65536 for MDANIM_ONESHOT
	    if (anim != null && (i >= j+j) && (fps != 0) && mdpause == 0) //Keep mdanimtims close to mdtims to avoid the use of MOD
	        sprext.mdanimtims += j/((fps*timerticspersec)/120);

	    k = i;

	    if (anim != null && (anim.flags&MDANIM_ONESHOT) != 0)
	        { if (i > j-65536) i = j-65536; }
	    else { if (i >= j) { i -= j; if (i >= j) i %= j; } }

	    if (r_animsmoothing != 0 && smooth.mdsmooth != 0)
	    {
	        m.nframe = anim != null ? anim.startframe : smooth.mdcurframe;
	        m.cframe = smooth.mdoldframe;
	
	        if (k > 65535)
	        {
	            sprext.mdanimtims = mdtims;
	            m.interpol = 0;
	            smooth.mdsmooth = 0;
	            m.cframe = m.nframe;
	
	            smooth.mdoldframe = (short) m.cframe;
	            return;
	        }
	    }
	    else
	    {
	        m.cframe = (i>>16)+anim.startframe;
	        m.nframe = m.cframe+1;
	        if (m.nframe > anim.endframe) 
	            m.nframe = anim.startframe;

	        smooth.mdoldframe = (short) m.cframe;
	    }
	    m.interpol = ((float)(i&65535))/65536.f;
	}

	public static int hicfxmask(int pal)
	{
	    return (globalnoeffect != 0)?0:(hictinting[pal].f&HICEFFECTMASK);
	}
	
	public static int md_defineskin(int modelid, String skinfn, int palnum, int skinnum, int surfnum, double param, double specpower, double specfactor)
	{
	    if (mdinited == 0) mdinit();

	    if (skinfn == null) return -2;
	    if (palnum >= MAXPALOOKUPS) return -3;

	    if (models.get(modelid).mdnum < 2) return 0;
	    
	    MDModel m = (MDModel) models.get(modelid);
	    if (m.mdnum == 2) surfnum = 0;

	    MDSkinmap sk, skl = null;
	    for (sk = m.skinmap; sk != null; skl = sk, sk = sk.next)
	        if (sk.palette == palnum && skinnum == sk.skinnum && surfnum == sk.surfnum)
	            break;
	    
	    if (sk == null)
	    {
	        sk = new MDSkinmap();

	        if (skl == null) m.skinmap = sk;
	        else skl.next = sk;
	    }
	    else if (sk.fn != null) sk.fn = null;

	    sk.palette = palnum;
	    sk.skinnum = skinnum;
	    sk.surfnum = surfnum;
	    sk.param = (float) param;
	    sk.specpower = (float) specpower;
	    sk.specfactor = (float) specfactor;
	    sk.fn = skinfn;
	    
	    return 0;
	}
	
	public static Texture mdloadskin(GL10 gl, MDModel m, int number, int pal, int surf)
	{
	    String skinfile = null;
	    Texture texidx = null;
	    Texture[] texptr = null;
	    int idptr = -1;
	    MDSkinmap sk, skzero = null;
	    long startticks;

	    if (m.mdnum == 2)
	        surf = 0;
	    
	    if (pal >= MAXPALOOKUPS)
	    	return null;
	    

	    for (sk = m.skinmap; sk != null; sk = sk.next)
	    {
	    	int i = -1;
	    	if (sk.palette == pal && sk.skinnum == number && sk.surfnum == surf)
	        {
	            skinfile = sk.fn;
	            idptr = hicfxmask(pal);
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
	            idptr = hicfxmask(pal);
	            texptr = skzero.texid;
	            if(texptr != null)
	            	texidx = texptr[idptr];
	            //OSD_Printf("Using def skin 0,0 as fallback, pal=%d\n", pal);
	        }
	        else
	        {
	            if (number >= m.numskins)
	                number = 0;
	            
	            MD2Model md = (MD2Model) m;

	            skinfile = md.skinfn + number*64;
	            idptr = number*(HICEFFECTMASK+1) + hicfxmask(pal);
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
	        	if(models.get(i).mdnum < 2)
	        		continue;
	        	MDModel mi = (MDModel) models.get(i);
	            for (skzero = mi.skinmap; skzero != null; skzero = skzero.next)
	                if (Bstrcasecmp(skzero.fn, sk.fn) == 0 && skzero.texid[hicfxmask(pal)] != null)
	                {
	                    int f = hicfxmask(pal);
	                    sk.texid[f] = skzero.texid[f];
	                    return sk.texid[f];
	                }
	        }

	    texidx = null;
	    int filh = -1;
	    if ((filh = kOpen(skinfile, 0)) < 0)
	    {
	    	Console.Println("Skin " + skinfile  + " not found.", OSDTEXT_YELLOW);
	    	md_undefinemodel(m.modelid);
	        skinfile = null;
	        return null;
	    }
	    kClose(filh);

	    startticks = System.currentTimeMillis();
	    try {
	    	byte[] data = kGetBytes(skinfile, 0);
			Pixmap pix = new Pixmap(data, 0, data.length);
			texidx = new Texture(pix, true); 
	    	m.usesalpha = true;
	    } catch(Exception e) {
	    	Console.Println("Couldn't load file: " + skinfile, OSDTEXT_YELLOW);
	    	md_undefinemodel(m.modelid);
	        skinfile = null;
	    	return null;
	    }
	    
		int gltexfiltermode = Console.Geti("r_texturemode");

		gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,getGlFilter(gltexfiltermode).mag);
		gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,getGlFilter(gltexfiltermode).min);
//XXX	if (glinfo.maxanisotropy > 1.0)
//	    	gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAX_ANISOTROPY_EXT,glanisotropy);
	    gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
	    gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
	    
	    long etime = System.currentTimeMillis()-startticks;
	    
	    System.out.println("Load skin: p" + pal +  "-e" + hicfxmask(pal) + " \"" + skinfile + "\"... " + etime + " ms");

        texptr[idptr] = texidx;
	    return texidx;
	}

	public static int Ptile2tile(int tile,int pallet)
	{
	    int t=tile;
	    while (tile2model[tile] != null && (tile=tile2model[tile].next)!=-1)
	    	if (tile2model[tile].pal==pallet)
	            return tile;
	    return t;
	}
}

