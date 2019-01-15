/*
 * "POLYMOST" code originally written by Ken Silverman
 * Ken Silverman's official web site: "http://www.advsys.net/ken"
 * See the included license file "BUILDLIC.TXT" for license info.
 *
 * This file has been modified from Ken Silverman's original release
 * by Jonathon Fowler (jf@jonof.id.au)
 * by the EDuke32 team (development@voidpoint.com)
 * by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Render;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.Math.*;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Loader.MDAnimation.*;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.klabs;
import static ru.m210projects.Build.Pragmas.mulscale;
import static ru.m210projects.Build.Pragmas.scale;
import static ru.m210projects.Build.Render.GLInfo.*;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.*;
import static ru.m210projects.Build.Render.Types.GL10.*;
import static ru.m210projects.Build.Strhandler.Bstrlen;
import static ru.m210projects.Build.Loader.Model.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGDX;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Engine.Point;
import ru.m210projects.Build.Loader.MDModel;
import ru.m210projects.Build.Loader.MDSkinmap;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.MD2.MD2Frame;
import ru.m210projects.Build.Loader.MD2.MD2Model;
import ru.m210projects.Build.Loader.MD3.MD3Model;
import ru.m210projects.Build.Loader.MD3.MD3Surface;
import ru.m210projects.Build.Loader.MD3.MD3Vertice;
import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.TextureHandle.ValueResolver;
import ru.m210projects.Build.Render.TextureHandle.BTexture;
import ru.m210projects.Build.Render.TextureHandle.Pthtyp;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.Render.Types.Hudtyp;
import ru.m210projects.Build.Render.Types.Palette;
import ru.m210projects.Build.Render.Types.Tile2model;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;

public abstract class Polymost implements Renderer {
	
	public static long TexDebug = -1;
	class GLSurfaceArray {
		
		private GLSurface[] items;
		private int size;
		
		public GLSurfaceArray() {
			items = new GLSurface[4];
		}
		
		public GLSurface build()
		{
			if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
			if(items[size] == null)
				items[size] = new GLSurface();
			items[size].clear();
			return items[size++];
		}
		
		public GLSurface get (int index) {
			if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
			return items[index];
		}

		public void clear () {
			size = 0;
		}
		
		public boolean isEmpty () {
			return size == 0;
		}
		
		protected GLSurface[] resize (int newSize) {
			GLSurface[] newItems = new GLSurface[newSize];
			GLSurface[] items = this.items;
			System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
			this.items = newItems;
			return newItems;
		}
		
		public GLSurface[] toArray () {
			GLSurface[] array = new GLSurface[size];
			System.arraycopy(items, 0, array, 0, size);
			return array;
		}
	}
	
	class GLFog {
		
		public int shade, pal;
		public float combvis;

		private final FloatBuffer color = BufferUtils.newFloatBuffer(4);
	
		public void copy(GLFog src)
		{
			this.shade = src.shade;
			this.combvis = src.combvis;
			this.pal = src.pal;
		}
		
		public void clear()
		{
			shade = 0;
			combvis = 0;
			pal = 0;
		}
		
		public void apply()
		{
			if(nofog) return;
			
			float start, end;
	        if (combvis == 0)
	        {
	        	start = FULLVIS_BEGIN;
	        	end = FULLVIS_END;
	        } 
	        else if (shade >= numshades-1)
	        {
	        	start = -1;
	        	end = 0.001f;
	        }
	        else
	        {
	            start = (shade > 0) ? 0 : -(FOGDISTCONST * shade) / combvis;
	            end = (FOGDISTCONST * (numshades-1-shade)) / combvis;
	        }
	        
	        if(UseBloodPal && pal == 1) //Blood's pal 1
			{
	        	start = 0;
				if(end > 2)
					end = 2;
			}

	        color.clear();
	        color.put(palookupfog[pal][0] / 63.f);
	        color.put(palookupfog[pal][1] / 63.f);
	        color.put(palookupfog[pal][2] / 63.f);
	        color.put(0);
	        color.flip();
	        
	        gl.glFogfv(GL_FOG_COLOR, color);	
		    gl.glFogf(GL_FOG_START, start);
		    gl.glFogf(GL_FOG_END, end);
		}
	}
	
	class GLSurface {
//		public int type;
		public int numvertices;
		public Pthtyp pth;
		public short picnum;
		public byte shade;

		public FloatBuffer buffer;
		public GLFog fog;

		public GLSurface()
		{
			int maxvertices = 64;
			buffer = allocateBuffer( (2 + 4 + 3) * maxvertices );
			fog = new GLFog();
		}

		public void clear() {
//			type = 0;
			numvertices = 0;
			picnum = 0;
			pth = null;
			buffer.clear();
			fog.clear();
		}
		
		private FloatBuffer allocateBuffer( int numFloats )
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect( numFloats * 4 );
			buffer.order(ByteOrder.nativeOrder());
			return buffer.asFloatBuffer();
		}
	}
	
	private GLSurfaceArray surfaces = new GLSurfaceArray();
	private int surfaceType = 0;
	public GLFog globalfog = new GLFog();
	
	protected short globalpicnum;
	protected int globalorientation;
	private int numscans, numbunches;
	private int lastageclock;
	private boolean drunk;
	private float drunkIntensive = 1.0f;

	private final int SPREXT_NOTMD = 1;
	private final int SPREXT_NOMDANIM = 2;
	private final int SPREXT_AWAY1 = 4;
	private final int SPREXT_AWAY2 = 8;
//	private final int SPREXT_TSPRACCESS = 16;
//	private final int SPREXT_TEMPINVISIBLE = 32;
	
	private SPRITE[] tspriteptr = new SPRITE[MAXSPRITESONSCREEN + 1];

	private int spritesx[] = new int[MAXSPRITESONSCREEN + 1];
	private int spritesy[] = new int[MAXSPRITESONSCREEN + 1];
	private int spritesz[] = new int[MAXSPRITESONSCREEN + 1];
	
	protected final static int MAXWALLSB = ((MAXWALLS >> 2) + (MAXWALLS >> 3));

	private short[] p2 = new short[MAXWALLSB], thesector = new short[MAXWALLSB], thewall = new short[MAXWALLSB];
	private short maskwall[] = new short[MAXWALLSB];
	private int maskwallcnt;

	private short[] bunchfirst = new short[MAXWALLSB], bunchlast = new short[MAXWALLSB];

	private final int CACHEAGETIME = 16;

	private int global_cf_z;
	private float global_cf_xpanning, global_cf_ypanning, global_cf_heinum;
	private int global_cf_shade, global_cf_pal;

	private float[] alphahackarray = new float[MAXTILES];
	private float shadescale = 1.1f;
	private int shadescale_unbounded = 0;
	
	private boolean nofog;

	// For GL_LINEAR fog:
	private final int FOGDISTCONST = 600;
	private final float FULLVIS_BEGIN = (float) 2.9e30;
	private final float FULLVIS_END = (float) 3.0e30;

	private IntBuffer polymosttext;

	private float curpolygonoffset; // internal polygon offset stack for drawing flat sprites to avoid depth fighting

	public static short drawingskybox = 0;

	BTexture frameTexture;
	private int framew;
	private int frameh;
	private int framesize;

	private float gyxscale, gxyaspect, gviewxrange, ghalfx, grhalfxdown10,
			grhalfxdown10x;
	private double ghoriz;
	private float gcosang, gsinang, gcosang2, gsinang2;
	private float gchang, gshang, ogshang, gctang, gstang;
	private float gtang = 0.0f;
	private double guo, gux; // Screen-based texture mapping parameters
	private double guy;
	private double gvo;
	private double gvx;
	private double gvy;
	private double gdo, gdx, gdy;
	
	private int[] sectorborder = new int[256];
	private double[] dxb1 = new double[MAXWALLSB],
			dxb2 = new double[MAXWALLSB];
	private byte[] ptempbuf = new byte[MAXWALLSB << 1];

	private final float[][] matrix = new float[4][4];

	private int srepeat = 0, trepeat = 0;

	private double SCISDIST = 1.0; // 1.0: Close plane clipping distance
	// private final int USEZBUFFER = 1; //1:use zbuffer (slow, nice sprite
	// rendering), 0:no zbuffer (fast, bad sprite rendering)
	// private final int LINTERPSIZ = 4; //log2 of interpolation size. 4:pretty
	// fast&acceptable quality, 0:best quality/slow!
	// private final int DEPTHDEBUG = 0; //1:render distance instead of texture,
	// for debugging only!, 0:default
	private final int CULL_DELAY = 2;
	
	private PolyClipper clipper;

	private int lastcullcheck = 0;
	private int glmultisample, glnvmultisamplehint;

	private final TextureCache textureCache;
	private DefScript defs;
	
	private int[] h_xsize = new int[MAXTILES], h_ysize = new int[MAXTILES];
	private byte[] h_xoffs = new byte[MAXTILES], h_yoffs = new byte[MAXTILES];

	private GL10 gl;
	private Engine engine;
	
	private boolean showlines = false;

	public Polymost(Engine engine) {
		BuildGDX.app.setFrame(FrameType.GL);
		this.gl = BuildGDX.graphics.getGL10();
		this.engine = engine;
		
		this.textureCache = createTextureCache();
		this.clipper = new PolyClipper(this);
		
		for(int i = 0; i < 16; i++)
			drawpoly[i] = new Polygon();
		for(int i = 0; i < 8; i++) 
			dmaskwall[i] = new Surface();
		for(int i = 0; i < 6; i++)
			dsprite[i] = new Surface();
		for(int i = 0; i < dsin.length; i++)
			dsin[i] = new Vector2();
		for(int i = 0; i < dcoord.length; i++)
			dcoord[i] = new Vector2();
		Arrays.fill(spritewall, -1);
		
		init();

		Console.Println(GLInfo.renderer + " " + GLInfo.version + " initialized", OSDTEXT_GOLD);
	}
	
	@Override
	public void setDefs(DefScript defs) {
		this.textureCache.setTextureInfo(defs != null ? defs.texInfo : null);
		if(this.defs != null)
			gltexinvalidateall();
		this.defs = defs;
	}

	private TextureCache createTextureCache() {
		return new TextureCache(new ValueResolver<Integer>() {
			@Override
			public Integer get() {
				return anisotropy();
			}
		});
	}
	
	protected int setBoundTextureDetail(BTexture detailTexture, int texunits)
	{
		gl.glActiveTexture(++texunits);

		gl.glEnable(GL_TEXTURE_2D);
		bindTexture(detailTexture);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_MODULATE);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 2.0f);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		return texunits;
	}
	
	protected int setBoundTextureGlow(BTexture glowTexture, int texunits)
	{
		gl.glActiveTexture(++texunits);

		gl.glEnable(GL_TEXTURE_2D);
		bindTexture(glowTexture);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_MODULATE);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
		gl.glTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);

		gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 2.0f);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		return texunits;
	}

	public static boolean clampingMode(int dameth) {
		return ((dameth & 4) >> 2) == 1;
	}

	public static boolean alphaMode(int dameth) {
		return (dameth & 256) == 0;
	}

	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth) {
		textureCache.invalidate(dapicnum, dapalnum, clampingMode(dameth));
	}

	// Make all textures "dirty" so they reload, but not re-allocate
	// This should be much faster than polymost_glreset()
	// Use this for palette effects ... but not ones that change every frame!
	public void gltexinvalidateall() {
		textureCache.invalidateall();

		textureCache.changePalette(curpalette);
		
		clearskins(true);
	}

	public void gltexinvalidate8() {
		textureCache.invalidateall();
		clearskins(true);
	}

	public void clearskins(boolean bit8only) {
		if(defs == null) return;
		
		for (int i=MAXTILES-1; i>=0; i--) {
			Model m = defs.mdInfo.getModel(i);
	        if(m != null && !bit8only) m.clearSkins();
	        
	        Model vox = defs.mdInfo.getVoxel(i);
	        if(vox != null && !textureCache.isUseShader()) 
	        	vox.clearSkins();
	    }
	}
	
	@Override
	public void gltexapplyprops() {
		int gltexfiltermode = Console.Geti("r_texturemode");
		textureCache.updateSettings(gltexfiltermode);

		if(defs == null)
			return;
		
		int anisotropy = anisotropy();
		for (int i=MAXTILES-1; i>=0; i--) {
			Model m = defs.mdInfo.getModel(i);
	        if(m != null)
	        	m.setSkinParams(gltexfiltermode, anisotropy);
	    }
	}

	public int gltexcacnum = -1;
	float glox1, gloy1, glox2, gloy2;

	@Override
	public void uninit() {
		// Reset if this is -1 (meaning 1st texture call ever), or > 0 (textures
		// in memory)
		if (gltexcacnum < 0) {
			gltexcacnum = 0;

			// Hack for polymost_dorotatesprite calls before 1st polymost_drawrooms()
			gcosang = gcosang2 = 16384 / 262144.0f;
			gsinang = gsinang2 = 0.0f;
		} else {
			textureCache.uninit();
			clearskins(false);
		}

		if (polymosttext != null) {
			//polymosttext.dispose();
			gl.glDeleteTextures(1, polymosttext);
		}
		polymosttext = null;

		//
		// Cachefile_Free();
		// polymost_cachesync();
	}

	@Override
	public void init() {
		init2drender();
		GLInfo.init(gl);

		if (GLInfo.vendor.compareTo("NVIDIA Corporation") == 0) {
			gl.glHint(GL_FOG_HINT, GL_NICEST);
		} else {
			gl.glHint(GL_FOG_HINT, GL_DONT_CARE);
		}
		gl.glFogi(GL_FOG_MODE, GL_LINEAR); // GL_EXP

		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		if (glmultisample > 0 && GLInfo.multisample != 0) {
			if (GLInfo.nvmultisamplehint != 0)
				gl.glHint(GL_MULTISAMPLE_FILTER_HINT_NV,
						glnvmultisamplehint != 0 ? GL_NICEST : GL_FASTEST);
			gl.glEnable(GL_MULTISAMPLE);
		}
		
		if((GLInfo.multitex == 0 || GLInfo.envcombine == 0)) {
			if (Console.Geti("r_detailmapping") != 0) {
				Console.Println("Your OpenGL implementation doesn't support detail mapping. Disabling...", 0);
				Console.Set("r_detailmapping", 0);
			}

			if (r_glowmapping != 0) {
				Console.Println("Your OpenGL implementation doesn't support glow mapping. Disabling...", 0);
				r_glowmapping = 0;
			}
		}

		if (r_vbos != 0 && (GLInfo.vbos == 0)) {
			Console.Println("Your OpenGL implementation doesn't support Vertex Buffer Objects. Disabling...", 0);
			r_vbos = 0;
		}
	}

	public void resizeglcheck() // Ken Build method
	{
		if ((glox1 != windowx1) || (gloy1 != windowy1) || (glox2 != windowx2) || (gloy2 != windowy2)) {
	        glox1 = windowx1; gloy1 = windowy1;
			glox2 = windowx2; gloy2 = windowy2;

			gl.glViewport(windowx1, ydim - (windowy2 + 1), windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
			
			gl.glMatrixMode(GL_PROJECTION);
			gl.glLoadIdentity();
			
//			glPerspective(65, xdimen / (float) ydimen, 0.0001f, 2000);
			float ang = (87 - (24 * gshang * gshang)) * 320.0f / xdimen;
			glPerspective(ang / 256.0f, xdimen / (float) (ydimen), -ydimen, ydimen);

			gl.glMatrixMode(GL_MODELVIEW);
			gl.glLoadIdentity();

			EnableFog();
		}
	}
	
	public void glPerspective(float fovyInDegrees, float aspectRatio, float znear, float zfar)
	{
	    float ymax = (float) (znear * Math.tan(fovyInDegrees * Math.PI / 360.0));
	    float xmax = ymax * aspectRatio;
//	    gl.glFrustumf(-xmax, xmax, -ymax, ymax, znear, zfar);
	    glFrustumf(-xmax, xmax, -ymax, ymax, znear, zfar);
	}
	
	public void glFrustumf(float left, float right, float bottom, float top, float znear, float zfar)
	{
		float A = (right + left) / (right - left);
		float B = (top + bottom) / (top - bottom);
		float C = -(zfar + znear) / (zfar - znear);
		float D = -(2 * zfar * znear) / (zfar - znear);
		
		matrix[0][0] = 2.0f * znear / (right - left); //0
		matrix[0][1] = 0.0f; //4
		matrix[0][2] = A; //8
		matrix[0][3] = 0.0f; //12
		matrix[1][0] = 0.0f; //1
		matrix[1][1] = 2.0f * znear / (top - bottom); //5
		matrix[1][2] = B; //9
		matrix[1][3] = 0.0f; //13
		matrix[2][0] = 0.0f; //2
		matrix[2][1] = 0.0f; //6
		matrix[2][2] = C; //2000 * C // 10
		matrix[2][3] = D; //14
		matrix[3][0] = 0.0f; //3
		matrix[3][1] = 0.0f; //7
		matrix[3][2] = -1.0f; //1f - matrix[2][2]; // 11
		matrix[3][3] = 0.0f; //15	
		
		gl.glLoadMatrixf(matrix);
	}

	// (dpx,dpy) specifies an n-sided polygon. The polygon must be a convex
	// clockwise loop.
	// n must be <= 8 (assume clipping can double number of vertices)
	// method: 0:solid, 1:masked(255 is transparent), 2:transluscent #1,
	// 3:transluscent #2
	// +4 means it's a sprite, so wraparound isn't needed
	
	private float getshadefactor(int shade)
	{
		int shadebound = (shadescale_unbounded != 0 || shade>=numshades) ? numshades : numshades-1;
	    float clamped_shade = min(max(shade*shadescale, 0), shadebound);
	    return ((float)(numshades-clamped_shade))/(float)numshades;
	}
	
	private int drawpoly_math(int nn, int i, int j, double ngux, double ngdx, double nguy, double ngdy, double nguo, double ngdo, double var)
	{
		double f = -(drawpoly[i].px * (ngux - ngdx * var) + drawpoly[i].py * (nguy - ngdy * var) + (nguo - ngdo * var)) /     
		        ((drawpoly[j].px - drawpoly[i].px) * (ngux - ngdx * var) + (drawpoly[j].py - drawpoly[i].py) * (nguy - ngdy * var));                       
		drawpoly[nn].uu = (float) ((drawpoly[j].px - drawpoly[i].px) * f + drawpoly[i].px);                                                                         
		drawpoly[nn].vv = (float) ((drawpoly[j].py - drawpoly[i].py) * f + drawpoly[i].py);                                                                          
		++nn;  
		
		return nn;
	}

	int pow2xsplit = 0;
	int skyclamphack = 0;
	
	private final Polygon drawpoly[] = new Polygon[16];
	private final Color polyColor = new Color();
	
	protected void drawpoly(Surface[] dm, int n, int method) {
		double ngdx = 0.0, ngdy = 0.0, ngdo = 0.0, ngux = 0.0, nguy = 0.0, nguo = 0.0;
		double ngvx = 0.0, ngvy = 0.0, ngvo = 0.0, dp, up, vp, du0 = 0.0, du1 = 0.0, dui, duj;
		double f, r, ox, oy, oz, ox2, oy2, oz2, uoffs;
		int i, j, k, nn, ix0, ix1, tsizx, tsizy, xx, yy;

		boolean dorot;

		Pthtyp pth = null, detailpth = null, glowpth = null;
		int texunits = GL_TEXTURE0;

		if (method == -1)
			return;

		if (n == 3) {
			if ((dm[0].px - dm[1].px) * (dm[2].py - dm[1].py) >= (dm[2].px - dm[1].px) * (dm[0].py - dm[1].py))
				return; // for triangle
		} else {
			f = 0; // f is area of polygon / 2
			for (i = n - 2, j = n - 1, k = 0; k < n; i = j, j = k, k++) {
				if(i < 0) return;
				f += (dm[i].px - dm[k].px) * dm[j].py;
			}
			if (f <= 0)
				return;
		}

		// Load texture (globalpicnum)
		if (globalpicnum >= MAXTILES)
			globalpicnum = 0;

		engine.setgotpic(globalpicnum);
		tsizx = tilesizx[globalpicnum];
		tsizy = tilesizy[globalpicnum];

		if (palookup[globalpal] == null)
			globalpal = 0;

		boolean HOM = false;
		if (waloff[globalpicnum] == null) {
			if(TexDebug != -1)
				TexDebug = System.nanoTime();

			if (engine.loadtile(globalpicnum) == null) {
				HOM = true;
				gl.glDisable(GL_TEXTURE_2D);
			}
			
			if(TexDebug != -1)
				System.out.println("Loading tile " + globalpicnum + " [" + ((System.nanoTime() - TexDebug) / 1000000f) + " ms]");
		}
	
		j = 0; dorot = ((gchang != 1.0) || (gctang != 1.0));
		if(dorot)
		{
			for (i = 0; i < n; i++) {
				ox = dm[i].px - ghalfx;
				oy = dm[i].py - ghoriz;
				oz = ghalfx;
	
				// Up/down rotation
				ox2 = ox;
				oy2 = oy * gchang - oz * gshang;
				oz2 = oy * gshang + oz * gchang;
	
				// Tilt rotation
				ox = ox2 * gctang - oy2 * gstang;
				oy = ox2 * gstang + oy2 * gctang;
				oz = oz2;
	
				r = ghalfx / oz;
				drawpoly[j].dd = (dm[i].px * gdx + dm[i].py * gdy + gdo) * r;
				drawpoly[j].uu = (dm[i].px * gux + dm[i].py * guy + guo) * r;
				drawpoly[j].vv = (dm[i].px * gvx + dm[i].py * gvy + gvo) * r;
	
				drawpoly[j].px = ox * r + ghalfx;
				drawpoly[j].py = oy * r + ghoriz;
				if ((j == 0) || (drawpoly[j].px != drawpoly[j-1].px) || (drawpoly[j].py != drawpoly[j - 1].py)) j++;
			}
		} 
		else	
	    {
	        for (i=0; i<n; i++)
	        {
	        	drawpoly[j].px = dm[i].px;
	        	drawpoly[j].py = dm[i].py;
	            if ((j==0) || (drawpoly[j].px != drawpoly[j - 1].px) || (drawpoly[j].py != drawpoly[j - 1].py)) j++;
	        }
	    }
		while ((j >= 3) && (drawpoly[j - 1].px == drawpoly[0].px)
				&& (drawpoly[j - 1].py == drawpoly[0].py))
			j--;
		if (j < 3)
			return;
		n = j;

		float hackscx, hackscy;

		if (skyclamphack != 0)
			method |= 4;

		pth = textureCache.cache(globalpicnum, globalpal, drawingskybox, clampingMode(method), alphaMode(method));
		if(pth == null) //hires texture not found
			return;

		if(!pth.isHighTile() && textureCache.isUseShader()) {
			textureCache.bindShader();
			textureCache.setShaderParams(globalpal, engine.getpalookup(globalvisibility, globalshade));
			gl.glDisable(GL_FOG);
		}
		bindTexture(pth.glpic);
		
		GLSurface surf = null;
		if(surfaceType == 1) { 
			surf = surfaces.build();
			surf.pth = pth;	
			surf.picnum = globalpicnum;
			surf.shade = (byte) globalshade;
		}
		
		if (srepeat != 0)
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		if (trepeat != 0)
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		// texture scale by parkar request
		if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)) && drawingskybox == 0) {
			gl.glMatrixMode(GL_TEXTURE);
			gl.glLoadIdentity();
			gl.glScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);
			gl.glMatrixMode(GL_MODELVIEW);
		}

		// detail texture
		if (Console.Geti("r_detailmapping") != 0 && usehightile && drawingskybox == 0)
			detailpth = textureCache.cache(globalpicnum, DETAILPAL, (short) 0, clampingMode(method), alphaMode(method));

		if (GLInfo.multisample != 0 && detailpth != null && detailpth.hicr != null && (detailpth.hicr.palnum == DETAILPAL)) {
			texunits = setBoundTextureDetail(detailpth.glpic, texunits);
	
			f = detailpth != null ? detailpth.hicr.xscale : 1.0;

			gl.glMatrixMode(GL_TEXTURE);
			gl.glLoadIdentity();

			if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)))
				gl.glScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);

			if (detailpth != null && detailpth.hicr != null && ((detailpth.hicr.xscale != 1.0f)
					|| (detailpth.hicr.yscale != 1.0f)))
				gl.glScalef(detailpth.hicr.xscale, detailpth.hicr.yscale, 1.0f);

			gl.glMatrixMode(GL_MODELVIEW);
		}
		
		if (r_glowmapping != 0 && usehightile && drawingskybox == 0)
			glowpth = textureCache.cache(globalpicnum, GLOWPAL, (short) 0, clampingMode(method), alphaMode(method));
		
		if (GLInfo.multisample != 0 && glowpth != null && glowpth.hicr != null && (glowpth.hicr.palnum == GLOWPAL))
			texunits = setBoundTextureGlow(glowpth.glpic, texunits);
		
		if (pth != null && pth.isHighTile()) {
			hackscx = pth.scalex;
			hackscy = pth.scaley;
			tsizx = pth.sizx;
			tsizy = pth.sizy;
		} else {
			hackscx = 1.0f;
			hackscy = 1.0f;
		}

		if (GLInfo.texnpot == 0) {
			for (xx = 1; xx < tsizx; xx += xx);
			ox2 = 1.0 / (double) xx;
			for (yy = 1; yy < tsizy; yy += yy);
			oy2 = 1.0 / (double) yy;
		} else {
			xx = tsizx;
			ox2 = 1.0 / (double) xx;
			yy = tsizy;
			oy2 = 1.0 / (double) yy;
		}

		if (((method & 3) == 0) && !HOM) {
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL_ALPHA_TEST); // alpha_test
		} else {
			float al = 0.0f; // PLAG : default alphacut was 0.32 before goodalpha
			if (pth != null && pth.hicr != null && pth.hicr.alphacut >= 0.0)
				al = pth.hicr.alphacut;
			if (alphahackarray[globalpicnum] != 0)
				al = alphahackarray[globalpicnum];
			if (waloff[globalpicnum] == null)
				al = 0.0f; // invalid textures ignore the alpha cutoff settings

			gl.glAlphaFunc(GL_GREATER, al);
			gl.glEnable(GL_BLEND);
			gl.glEnable(GL_ALPHA_TEST);
		}

        if (!dorot)
        {
            for (i=n-1; i>=0; i--)
            {
            	drawpoly[i].dd = drawpoly[i].px*gdx + drawpoly[i].py*gdy + gdo;
            	drawpoly[i].uu = drawpoly[i].px*gux + drawpoly[i].py*guy + guo;
            	drawpoly[i].vv = drawpoly[i].px*gvx + drawpoly[i].py*gvy + gvo;
            }
        }
        
		f = getshadefactor(globalshade); 

		if(UseBloodPal && globalpal == 1) polyColor.r = polyColor.g = polyColor.b = 1; //Blood's pal 1
		else polyColor.r = polyColor.g = polyColor.b = (float) f;
		
		switch (method & 3) {
		default:
		case 0:
		case 1:
			polyColor.a = 1.0f;
			break;
		case 2:
			polyColor.a = TRANSLUSCENT1;
			break;
		case 3:
			polyColor.a = TRANSLUSCENT2;
			break;
		}

		calcHictintingColor(pth);
		
		if(HOM) polyColor.a = 0.01f; // Hack to update Z-buffer for invalid mirror textures
		
		textureCache.shaderTransparent(polyColor.a);
		gl.glColor4f(polyColor.r, polyColor.g, polyColor.b, polyColor.a);

		// Hack for walls&masked walls which use textures that are not a power
		// of 2
		if ((pow2xsplit != 0) && (tsizx != xx)) {
			if (!dorot)
            {
                ngdx = gdx; ngdy = gdy; ngdo = gdo+(ngdx+ngdy)*.5;
                ngux = gux; nguy = guy; nguo = guo+(ngux+nguy)*.5;
                ngvx = gvx; ngvy = gvy; ngvo = gvo+(ngvx+ngvy)*.5;
            }
			else
			{
				ox = drawpoly[1].py - drawpoly[2].py;
				oy = drawpoly[2].py - drawpoly[0].py;
				oz = drawpoly[0].py - drawpoly[1].py;
				r = 1.0 / (ox * drawpoly[0].px + oy * drawpoly[1].px + oz * drawpoly[2].px);
				ngdx = (ox * drawpoly[0].dd + oy * drawpoly[1].dd + oz * drawpoly[2].dd) * r;
				ngux = (ox * drawpoly[0].uu + oy * drawpoly[1].uu + oz * drawpoly[2].uu) * r;
				ngvx = (ox * drawpoly[0].vv + oy * drawpoly[1].vv + oz * drawpoly[2].vv) * r;
				ox = drawpoly[2].px - drawpoly[1].px;
				oy = drawpoly[0].px - drawpoly[2].px;
				oz = drawpoly[1].px - drawpoly[0].px;
				ngdy = (ox * drawpoly[0].dd + oy * drawpoly[1].dd + oz * drawpoly[2].dd) * r;
				nguy = (ox * drawpoly[0].uu + oy * drawpoly[1].uu + oz * drawpoly[2].uu) * r;
				ngvy = (ox * drawpoly[0].vv + oy * drawpoly[1].vv + oz * drawpoly[2].vv) * r;
				ox = drawpoly[0].px - .5;
				oy = drawpoly[0].py - .5; // .5 centers texture nicely
				ngdo = drawpoly[0].dd - ox * ngdx - oy * ngdy;
				nguo = drawpoly[0].uu - ox * ngux - oy * nguy;
				ngvo = drawpoly[0].vv - ox * ngvx - oy * ngvy;
			}

			ngux *= hackscx;
			nguy *= hackscx;
			nguo *= hackscx;
			ngvx *= hackscy;
			ngvy *= hackscy;
			ngvo *= hackscy;
			uoffs = ((double) (xx - tsizx) * .5);
			ngux -= ngdx * uoffs;
			nguy -= ngdy * uoffs;
			nguo -= ngdo * uoffs;

			// Find min&max u coordinates (du0...du1)
			for (i = 0; i < n; i++) {
				ox = drawpoly[i].px;
				oy = drawpoly[i].py;
				
				f = (ox * ngux + oy * nguy + nguo)
						/ (ox * ngdx + oy * ngdy + ngdo);
				if (i == 0) {
					du0 = du1 = f;
					continue;
				}
				if (f < du0)
					du0 = f;
				else if (f > du1)
					du1 = f;
			}
			
			if((tsizx|tsizy) == 0) 
				return;

			f = 1.0 / (double) tsizx;
			ix0 = (int) floor(du0 * f);
			ix1 = (int) floor(du1 * f);

			for (; ix0 <= ix1; ix0++) {
				du0 = (double) ((ix0) * tsizx);
				du1 = (double) ((ix0 + 1) * tsizx);

				i = 0;
				nn = 0;
				duj = (drawpoly[i].px * ngux + drawpoly[i].py * nguy + nguo)
						/ (drawpoly[i].px * ngdx + drawpoly[i].py * ngdy + ngdo);
				do {
					j = i + 1;
					if (j == n) j = 0;
					dui = duj;
					duj = (drawpoly[j].px * ngux + drawpoly[j].py * nguy + nguo)
							/ (drawpoly[j].px * ngdx + drawpoly[j].py * ngdy + ngdo);

					if ((du0 <= dui) && (dui <= du1)) {
						drawpoly[nn].uu = drawpoly[i].px;
						drawpoly[nn].vv = drawpoly[i].py;
						nn++;
					}
					
					if (duj <= dui) {
						if ((du1 < duj) != (du1 < dui)) 
							nn = drawpoly_math(nn, i, j, ngux,  ngdx,  nguy,  ngdy,  nguo,  ngdo, du1);
						if ((du0 < duj) != (du0 < dui)) 
							nn = drawpoly_math(nn, i, j, ngux,  ngdx,  nguy,  ngdy,  nguo,  ngdo, du0);
					} else {
						if ((du0 < duj) != (du0 < dui)) 
							nn = drawpoly_math(nn, i, j, ngux,  ngdx,  nguy,  ngdy,  nguo,  ngdo, du0);
						if ((du1 < duj) != (du1 < dui)) 
							nn = drawpoly_math(nn, i, j, ngux,  ngdx,  nguy,  ngdy,  nguo,  ngdo, du1);
					}
					i = j;
				} while (i != 0);
				if (nn < 3)
					continue;

				gl.glBegin(GL_TRIANGLE_FAN);
				for (i = 0; i < nn; i++) {
					Polygon dpoly = drawpoly[i];
					ox = dpoly.uu;
					oy = dpoly.vv;
					dp = ox * ngdx + oy * ngdy + ngdo;
					up = ox * ngux + oy * nguy + nguo;
					vp = ox * ngvx + oy * ngvy + ngvo;
					r = 1.0 / dp;
					if (texunits > GL_TEXTURE0) {
						j = GL_TEXTURE0;
						while (j <= texunits)
							gl.glMultiTexCoord2d(j++, (up * r - du0 + uoffs) * ox2, vp * r * oy2);
					} else
						gl.glTexCoord2d((up * r - du0 + uoffs) * ox2, vp * r * oy2);
					gl.glVertex3d((ox - ghalfx) * r * grhalfxdown10x,
							(ghoriz - oy) * r * grhalfxdown10, r * (1.0 / 1024.0));
				}
				gl.glEnd();
			}
		} else {
			ox2 *= hackscx;
			oy2 *= hackscy;

			if(surfaceType == 0)
			{
				gl.glBegin(GL_TRIANGLE_FAN);
				for (i = 0; i < n; i++) {
					Polygon dpoly = drawpoly[i];

					r = 1.0f / dpoly.dd;
					if (texunits > GL_TEXTURE0) {
						j = GL_TEXTURE0;
						while (j <= texunits)
							gl.glMultiTexCoord2d(j++, dpoly.uu * r * ox2, dpoly.vv * r * oy2);
					} else
						gl.glTexCoord2d(dpoly.uu * r * ox2, dpoly.vv * r * oy2);

					gl.glVertex3d((dpoly.px - ghalfx) * r * grhalfxdown10x, (ghoriz - dpoly.py) * r * grhalfxdown10, r * (1.f / 1024.f));
				}
				gl.glEnd();
			} else {
				surf.numvertices = n;
				surf.fog.copy(globalfog);
				for (i = 0; i < n; i++) {
					Polygon dpoly = drawpoly[i];
					r = 1.0f / dpoly.dd;
					if(waloff[globalpicnum] != null) {
						surf.buffer.put((float) ((dpoly.px - ghalfx) * r * grhalfxdown10)); //x
						surf.buffer.put((float) ((ghoriz - dpoly.py) * r * grhalfxdown10)); //y
						surf.buffer.put((float) (r * (1.f / 1024.f))); //z

						surf.buffer.put((float) (dpoly.uu * r * ox2));
						surf.buffer.put((float) (dpoly.vv * r * oy2));
						
						surf.buffer.put(polyColor.r);
						surf.buffer.put(polyColor.g);
						surf.buffer.put(polyColor.b);
						surf.buffer.put(polyColor.a);
					}
				}
				surf.buffer.flip();
			}

			if(showlines) {
				gl.glDisable(GL_TEXTURE_2D);
				int[] p = new int[2];
				gl.glColor4f(1, 1, 1, 1);
				gl.glBegin(GL_LINES); 
				for (i = 1; i <= n; i++) { 
					p[0] = i-1; p[1] = i;
					if(i == n) { p[0] = i - 1; p[1] = 0; }
					for(int l = 0; l < 2; l++) {
						r = 1.0 / drawpoly[p[l]].dd; 
						gl.glVertex3d((drawpoly[p[l]].px - ghalfx) * r * grhalfxdown10x, (ghoriz - drawpoly[p[l]].py) * r * grhalfxdown10, r* (1.0 / 1024.0));
					}
				} 
				gl.glEnd(); 
				gl.glEnable(GL_TEXTURE_2D);
			}
		}

		if(GLInfo.multisample != 0) {
			while (texunits >= GL_TEXTURE0) {
				gl.glActiveTexture(texunits);
				gl.glMatrixMode(GL_TEXTURE);
				gl.glLoadIdentity();
				gl.glMatrixMode(GL_MODELVIEW);
				if (texunits > GL_TEXTURE0) {
					gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
					gl.glDisable(GL_TEXTURE_2D);
				}
				texunits--;
			}
		}

		if (srepeat != 0)
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
					GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP);
		if (trepeat != 0)
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
					GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP);
		
		if(!pth.isHighTile() && textureCache.isUseShader()) {
			textureCache.unbindShader();
			EnableFog();
		}
		
		if(HOM) gl.glEnable(GL_TEXTURE_2D);
	}

	// variables that are set to ceiling- or floor-members, depending
	// on which one is processed right now

	private final double nonparallaxed_ft[] = new double[4],
			nonparallaxed_px[] = new double[3],
			nonparallaxed_py[] = new double[3],
			nonparallaxed_dd[] = new double[3],
			nonparallaxed_uu[] = new double[3],
			nonparallaxed_vv[] = new double[3];

	private void nonparallaxed(double nx0, double ny0,
			double nx1, double ny1, double ryp0, double ryp1, float x0,
			float x1, float cf_y0, float cf_y1, int have_floor, int sectnum,
			boolean floor) {
		double fx, fy, ox, oy, oz, ox2, oy2, r;
		int i;

		SECTOR sec = sector[sectnum];
		
		if ((globalorientation & 64) == 0) {
			nonparallaxed_ft[0] = globalposx;
			nonparallaxed_ft[1] = globalposy;
			nonparallaxed_ft[2] = cosglobalang;
			nonparallaxed_ft[3] = singlobalang;
		} else {
			// relative alignment
			fx = (double) (wall[wall[sec.wallptr].point2].x - wall[sec.wallptr].x);
			fy = (double) (wall[wall[sec.wallptr].point2].y - wall[sec.wallptr].y);
			r = 1.0 / sqrt(fx * fx + fy * fy);
			fx *= r;
			fy *= r;
			
			nonparallaxed_ft[2] = cosglobalang * fx + singlobalang * fy;
			nonparallaxed_ft[3] = singlobalang * fx - cosglobalang * fy;
			nonparallaxed_ft[0] = ((double) (globalposx - wall[sec.wallptr].x))
					* fx + ((double) (globalposy - wall[sec.wallptr].y)) * fy;
			nonparallaxed_ft[1] = ((double) (globalposy - wall[sec.wallptr].y))
					* fx - ((double) (globalposx - wall[sec.wallptr].x)) * fy;
			if ((globalorientation & 4) == 0)
				globalorientation ^= 32;
			else
				globalorientation ^= 16;
		}

		gdx = 0;
		gdy = gxyaspect;
		if ((globalorientation & 2) == 0)
			if (global_cf_z - globalposz != 0) // PK 2012: don't allow div by zero
				gdy /= (double) (global_cf_z - globalposz);
		gdo = -ghoriz * gdy;
		if ((globalorientation & 8) != 0) {
			nonparallaxed_ft[0] /= 8;
			nonparallaxed_ft[1] /= -8;
			nonparallaxed_ft[2] /= 2097152;
			nonparallaxed_ft[3] /= 2097152;
		} else {
			nonparallaxed_ft[0] /= 16;
			nonparallaxed_ft[1] /= -16;
			nonparallaxed_ft[2] /= 4194304;
			nonparallaxed_ft[3] /= 4194304;
		}
		gux = nonparallaxed_ft[3] * ((double) viewingrange) / -65536.0;
		gvx = nonparallaxed_ft[2] * ((double) viewingrange) / -65536.0;
		guy = nonparallaxed_ft[0] * gdy;
		gvy = (double) nonparallaxed_ft[1] * gdy;
		guo = nonparallaxed_ft[0] * gdo;
		gvo = (double) nonparallaxed_ft[1] * gdo;
		guo += (double) (nonparallaxed_ft[2] - gux) * ghalfx;
		gvo -= (double) (nonparallaxed_ft[3] + gvx) * ghalfx;

		// Texture flipping
		if ((globalorientation & 4) != 0) {
			r = gux;
			gux = gvx;
			gvx = r;
			r = guy;
			guy = gvy;
			gvy = r;
			r = guo;
			guo = gvo;
			gvo = r;
		}
		if ((globalorientation & 16) != 0) {
			gux = -gux;
			guy = -guy;
			guo = -guo;
		}
		if ((globalorientation & 32) != 0) {
			gvx = -gvx;
			gvy = -gvy;
			gvo = -gvo;
		}

		// Texture panning
		fx = global_cf_xpanning * ((float) (1 << (picsiz[globalpicnum] & 15)))
				/ 256.0;
		fy = global_cf_ypanning * ((float) (1 << (picsiz[globalpicnum] >> 4)))
				/ 256.0;
		
		if ((globalorientation & (2 + 64)) == (2 + 64)) // Hack for panning for slopes w/ relative alignment
		{
			r = global_cf_heinum / 4096.0;
			r = 1.0 / sqrt(r * r + 1);
			if ((globalorientation & 4) == 0)
				fy *= r;
			else
				fx *= r;
		}

		guy += gdy * fx;
		guo += gdo * fx;
		gvy += gdy * fy;
		gvo += gdo * fy;

		if ((globalorientation & 2) != 0) // slopes
		{
			nonparallaxed_px[0] = x0;
			nonparallaxed_py[0] = ryp0 + ghoriz;
			nonparallaxed_px[1] = x1;
			nonparallaxed_py[1] = ryp1 + ghoriz;

			// Pick some point guaranteed to be not collinear to the 1st two
			// points
			ox = nx0 + (ny1 - ny0);
			oy = ny0 + (nx0 - nx1);
			ox2 = (double) (oy - globalposy) * gcosang - (double) (ox - globalposx) * gsinang;
			oy2 = (double) (ox - globalposx) * gcosang2 + (double) (oy - globalposy) * gsinang2;
			oy2 = 1.0 / oy2;
			nonparallaxed_px[2] = ghalfx * ox2 * oy2 + ghalfx;
			oy2 *= gyxscale;
			nonparallaxed_py[2] = oy2 + ghoriz;

			for (i = 0; i < 3; i++) {
				nonparallaxed_dd[i] = nonparallaxed_px[i] * gdx + nonparallaxed_py[i] * gdy + gdo;
				nonparallaxed_uu[i] = nonparallaxed_px[i] * gux + nonparallaxed_py[i] * guy + guo;
				nonparallaxed_vv[i] = nonparallaxed_px[i] * gvx + nonparallaxed_py[i] * gvy + gvo;
			}

			nonparallaxed_py[0] = cf_y0;
			nonparallaxed_py[1] = cf_y1;
			if (floor) 
				nonparallaxed_py[2] = (polymost_getflorzofslope(sectnum, ox, oy) - globalposz) * oy2 + ghoriz;
			else
				nonparallaxed_py[2] = (polymost_getceilzofslope(sectnum, ox, oy) - globalposz) * oy2 + ghoriz;

			ox = nonparallaxed_py[1] - nonparallaxed_py[2];
			oy = nonparallaxed_py[2] - nonparallaxed_py[0];
			oz = nonparallaxed_py[0] - nonparallaxed_py[1];
			r = 1.0 / (ox * nonparallaxed_px[0] + oy * nonparallaxed_px[1] + oz
					* nonparallaxed_px[2]);
			
			gdx = (ox * nonparallaxed_dd[0] + oy * nonparallaxed_dd[1] + oz
					* nonparallaxed_dd[2])
					* r;
			gux = (ox * nonparallaxed_uu[0] + oy * nonparallaxed_uu[1] + oz
					* nonparallaxed_uu[2])
					* r;
			gvx = (ox * nonparallaxed_vv[0] + oy * nonparallaxed_vv[1] + oz
					* nonparallaxed_vv[2])
					* r;
			ox = nonparallaxed_px[2] - nonparallaxed_px[1];
			oy = nonparallaxed_px[0] - nonparallaxed_px[2];
			oz = nonparallaxed_px[1] - nonparallaxed_px[0];
			gdy = (ox * nonparallaxed_dd[0] + oy * nonparallaxed_dd[1] + oz
					* nonparallaxed_dd[2])
					* r;
			guy = (ox * nonparallaxed_uu[0] + oy * nonparallaxed_uu[1] + oz
					* nonparallaxed_uu[2])
					* r;
			gvy = (ox * nonparallaxed_vv[0] + oy * nonparallaxed_vv[1] + oz
					* nonparallaxed_vv[2])
					* r;
			gdo = nonparallaxed_dd[0] - nonparallaxed_px[0] * gdx
					- nonparallaxed_py[0] * gdy;
			guo = nonparallaxed_uu[0] - nonparallaxed_px[0] * gux
					- nonparallaxed_py[0] * guy;
			gvo = nonparallaxed_vv[0] - nonparallaxed_px[0] * gvx
					- nonparallaxed_py[0] * gvy;

			if ((globalorientation & 64) != 0) // Hack for relative alignment on slopes
			{
				r = global_cf_heinum / 4096.0;
				r = sqrt(r * r + 1);
				if ((globalorientation & 4) == 0) {
					gvx *= r;
					gvy *= r;
					gvo *= r;
				} else {
					gux *= r;
					guy *= r;
					guo *= r;
				}
			}
		}
	
		clipper.setMethod((globalorientation >> 7) & 3);
		if (have_floor != 0) {
			if (globalposz >= polymost_getflorzofslope(sectnum, globalposx, globalposy))
				clipper.setMethod(-1); // Back-face culling
		} else {
			if (globalposz <= polymost_getceilzofslope(sectnum, globalposx, globalposy))
				clipper.setMethod(-1); // Back-face culling
		}

		calc_and_apply_fog(global_cf_shade, sec.visibility,  global_cf_pal);

		pow2xsplit = 0;
		if (have_floor != 0) 
			clipper.domost(x0, cf_y0, x1, cf_y1); // flor
		else 
			clipper.domost(x1, cf_y1, x0, cf_y0); // ceil

		clipper.setMethod(0);
	}
	
	private void calc_ypanning(int refposz, double ryp0, double ryp1,
			double x0, double x1, short ypan, short yrepeat, boolean dopancor) {
		double t0 = ((float) (refposz - globalposz)) * ryp0 + ghoriz;
		double t1 = ((float) (refposz - globalposz)) * ryp1 + ghoriz;
		double t = ((gdx * x0 + gdo) * (float) yrepeat) / ((x1 - x0) * ryp0 * 2048.f);
		int i = (1 << (picsiz[globalpicnum] >> 4));
		if (i < tilesizy[globalpicnum]) i <<= 1;

		if(GLInfo.texnpot != 0)
	    {
			if(!dopancor) //texture scaled, it's need to fix
				t *= (float)tilesizy[globalpicnum] / i;
	        i = tilesizy[globalpicnum];
	    } else if (dopancor && defs != null && defs.texInfo.isHighTile(globalpicnum)) {
			// Carry out panning "correction" to make it look like classic in some
	        // cases, but failing in the general case.
			
	    	int yoffs = (int) ((i - tilesizy[globalpicnum]) * (255.0f / i));
			if (ypan > 256 - yoffs) 
				ypan -= yoffs;
		}

		float fy = ypan * i / 256.0f;
		gvx = (t0 - t1) * t;
		gvy = (x1 - x0) * t;
		gvo = -gvx * x0 - gvy * t0 + fy * gdo;
		gvx += fy * gdx;
		gvy += fy * gdy;
	}

	private final double drawalls_dd[] = new double[3],
			drawalls_vv[] = new double[3], drawalls_ft[] = new double[4];
	private WALL drawalls_nwal = new WALL();

	private void drawalls(int bunch) {
		SECTOR sec, nextsec;
		WALL wal, wal2;
		double x0, x1, cy0, cy1, fy0, fy1, xp0, yp0, xp1, yp1, ryp0, ryp1, nx0, ny0, nx1, ny1;
		double t, t0, t1, ocy0, ocy1, ofy0, ofy1, oxp0, oyp0;
		double oguo, ogux, oguy, fwalxrepeat;
		int i, x, y, z, wallnum, sectnum, nextsectnum;

		sectnum = thesector[bunchfirst[bunch]];
		sec = sector[sectnum];

		calc_and_apply_fog(sec.floorshade, sec.visibility,  sec.floorpal);

		for (z = bunchfirst[bunch]; z >= 0; z = p2[z]) {
			// DRAW WALLS SECTION!

			wallnum = thewall[z];
			wal = wall[wallnum];
			wal2 = wall[wal.point2];
			nextsectnum = wal.nextsector;
			nextsec = nextsectnum >= 0 ? sector[nextsectnum] : null;
			
			fwalxrepeat = (double) (wal.xrepeat & 0xFF);

			// Offset&Rotate 3D coordinates to screen 3D space
			x = wal.x - globalposx;
			y = wal.y - globalposy;
			xp0 = (double) y * gcosang - (double) x * gsinang;
			yp0 = (double) x * gcosang2 + (double) y * gsinang2;
			x = wal2.x - globalposx;
			y = wal2.y - globalposy;
			xp1 = (double) y * gcosang - (double) x * gsinang;
			yp1 = (double) x * gcosang2 + (double) y * gsinang2;

			oxp0 = xp0;
			oyp0 = yp0;

			// Clip to close parallel-screen plane
			if (yp0 < SCISDIST) {
				if (yp1 < SCISDIST)
					continue;
				t0 = (SCISDIST - yp0) / (yp1 - yp0);
				xp0 = (xp1 - xp0) * t0 + xp0;
				yp0 = SCISDIST;
				nx0 = (wal2.x - wal.x) * t0 + wal.x;
				ny0 = (wal2.y - wal.y) * t0 + wal.y;
			} else {
				t0 = 0.f;
				nx0 = wal.x;
				ny0 = wal.y;
			}
			if (yp1 < SCISDIST) {
				t1 = (SCISDIST - oyp0) / (yp1 - oyp0);
				xp1 = (xp1 - oxp0) * t1 + oxp0;
				yp1 = SCISDIST;
				nx1 = (wal2.x - wal.x) * t1 + wal.x;
				ny1 = (wal2.y - wal.y) * t1 + wal.y;
			} else {
				t1 = 1.f;
				nx1 = wal2.x;
				ny1 = wal2.y;
			}

			ryp0 = 1.f / yp0;
			ryp1 = 1.f / yp1;

			// Generate screen coordinates for front side of wall
			x0 = ghalfx * xp0 * ryp0 + ghalfx;
			x1 = ghalfx * xp1 * ryp1 + ghalfx;
			if (x1 <= x0)
				continue;

			ryp0 *= gyxscale;
			ryp1 *= gyxscale;

			polymost_getzsofslope(sectnum, nx0, ny0);
			cy0 = (dceilzsofslope - globalposz) * ryp0 + ghoriz;
			fy0 = (dfloorzsofslope - globalposz) * ryp0 + ghoriz;
			polymost_getzsofslope(sectnum, nx1, ny1);
			cy1 = (dceilzsofslope - globalposz) * ryp1 + ghoriz;
			fy1 = (dfloorzsofslope - globalposz) * ryp1 + ghoriz;

			{ //DRAW FLOOR
				globalpicnum = sec.floorpicnum;
				globalshade = sec.floorshade;
				globalpal = sec.floorpal;
				globalorientation = sec.floorstat;
				if ((picanm[globalpicnum] & 192) != 0)
					globalpicnum += engine.animateoffs(globalpicnum, sectnum);

				global_cf_shade = sec.floorshade;
				global_cf_pal = sec.floorpal;
				global_cf_z = sec.floorz;
				global_cf_xpanning = sec.floorxpanning;
				global_cf_ypanning = sec.floorypanning;
				global_cf_heinum = sec.floorheinum;
				
				if ((globalorientation & 1) == 0) {
					nonparallaxed(nx0, ny0, nx1, ny1, ryp0, ryp1,
						(float) x0, (float) x1, (float) fy0, (float) fy1, 1,
						sectnum, true);
				} else if ((nextsectnum < 0) || ((sector[nextsectnum].floorstat & 1) == 0))
					drawbackground(sectnum, x0, x1, fy0, fy1, true);
				
			} //END DRAW FLOOR
			
			{ //DRAW CEILING
				globalpicnum = sec.ceilingpicnum;
				globalshade = sec.ceilingshade;
				globalpal = (int) (sec.ceilingpal & 0xFF);
				globalorientation = sec.ceilingstat;
				if ((picanm[globalpicnum] & 192) != 0)
					globalpicnum += engine.animateoffs(globalpicnum, sectnum);

				global_cf_shade = sec.ceilingshade;
				global_cf_pal = sec.ceilingpal;
				global_cf_z = sec.ceilingz;
				global_cf_xpanning = sec.ceilingxpanning;
				global_cf_ypanning = sec.ceilingypanning;
				global_cf_heinum = sec.ceilingheinum;
	
				if ((globalorientation & 1) == 0) {
					nonparallaxed(nx0, ny0, nx1, ny1, ryp0, ryp1,
						(float) x0, (float) x1, (float) cy0, (float) cy1, 0,
						sectnum, false);
				} else if ((nextsectnum < 0) || ((sector[nextsectnum].ceilingstat & 1) == 0))
					drawbackground(sectnum, x0, x1, cy0, cy1, false);
		
			} //END DRAW CEILING

			gdx = (ryp0 - ryp1) * gxyaspect / (x0 - x1);
			gdy = 0;
			gdo = ryp0 * gxyaspect - gdx * x0;
			gux = (t0 * ryp0 - t1 * ryp1) * gxyaspect * fwalxrepeat * 8.0 / (x0 - x1);
			guo = t0 * ryp0 * gxyaspect * fwalxrepeat * 8.0 - gux * x0;
			guo += (float) wal.xpanning * gdo;
			gux += (float) wal.xpanning * gdx;
			guy = 0;
			ogux = gux;
			oguy = guy;
			oguo = guo;

			if (nextsectnum >= 0) {
				polymost_getzsofslope(nextsectnum, nx0, ny0);
				ocy0 = (dceilzsofslope - globalposz) * ryp0 + ghoriz;
				ofy0 = (dfloorzsofslope - globalposz) * ryp0 + ghoriz;
				polymost_getzsofslope(nextsectnum, nx1, ny1);
				ocy1 = (dceilzsofslope - globalposz) * ryp1 + ghoriz;
				ofy1 = (dfloorzsofslope - globalposz) * ryp1 + ghoriz;

				if ((wal.cstat & 48) == 16)
					maskwall[maskwallcnt++] = (short) z;

				if (((cy0 < ocy0) || (cy1 < ocy1))
						&& (((sec.ceilingstat & sector[nextsectnum].ceilingstat) & 1)) == 0) {
					globalpicnum = wal.picnum;
					globalshade = wal.shade;
					globalpal = (int) (wal.pal & 0xFF);
					if ((picanm[globalpicnum] & 192) != 0)
						globalpicnum += engine.animateoffs(globalpicnum,
								wallnum + 16384);

					if ((wal.cstat & 4) == 0)
						i = sector[nextsectnum].ceilingz;
					else
						i = sec.ceilingz;

					// over
					calc_ypanning(i, ryp0, ryp1, x0, x1, wal.ypanning, wal.yrepeat, (wal.cstat & 4) != 0);

					if ((wal.cstat & 8) != 0) // xflip
					{
						t = fwalxrepeat * 8 + wal.xpanning * 2;
						gux = gdx * t - gux;
						guy = gdy * t - guy;
						guo = gdo * t - guo;
					}
					if ((wal.cstat & 256) != 0) {
						gvx = -gvx;
						gvy = -gvy;
						gvo = -gvo;
					} // yflip

					
					int shade = wal.shade;
					if(UseBloodPal && (globalpal == 1 || sec.floorpal == 1)) //Blood's pal 1
						shade = 0;
					calc_and_apply_fog(shade, sec.visibility, sec.floorpal);
					
					if(surfaceType == 0)
						pow2xsplit = 1;
					clipper.domost((float) x1, (float) ocy1, (float) x0, (float) ocy0);
					if ((wal.cstat & 8) != 0) {
						gux = ogux;
						guy = oguy;
						guo = oguo;
					}
				}
				if (((ofy0 < fy0) || (ofy1 < fy1))
						&& (((sec.floorstat & sector[nextsectnum].floorstat) & 1)) == 0) {
					if ((wal.cstat & 2) == 0) {
						drawalls_nwal.set(wal);
					} else {
						drawalls_nwal.set(wall[wal.nextwall]);
						guo += (float) (drawalls_nwal.xpanning - wal.xpanning) * gdo;
						gux += (float) (drawalls_nwal.xpanning - wal.xpanning) * gdx;
						guy += (float) (drawalls_nwal.xpanning - wal.xpanning) * gdy;
					}
					globalpicnum = drawalls_nwal.picnum;
					globalshade = drawalls_nwal.shade;
					globalpal = (int) (drawalls_nwal.pal & 0xFF);
					if ((picanm[globalpicnum] & 192) != 0)
						globalpicnum += engine.animateoffs(globalpicnum,
								wallnum + 16384);

					if ((drawalls_nwal.cstat & 4) == 0)
						i = sector[nextsectnum].floorz;
					else
						i = sec.ceilingz;

					// under
					calc_ypanning(i, ryp0, ryp1, x0, x1,
							drawalls_nwal.ypanning, wal.yrepeat,
							(drawalls_nwal.cstat & 4) == 0);

					if ((wal.cstat & 8) != 0) // xflip
					{
						t = (fwalxrepeat * 8 + drawalls_nwal.xpanning * 2);
						gux = gdx * t - gux;
						guy = gdy * t - guy;
						guo = gdo * t - guo;
					}
					if ((drawalls_nwal.cstat & 256) != 0) {
						gvx = -gvx;
						gvy = -gvy;
						gvo = -gvo;
					} // yflip

					int shade = drawalls_nwal.shade;
					if(UseBloodPal && (globalpal == 1 || sec.floorpal == 1)) //Blood's pal 1
						shade = 0;
					calc_and_apply_fog(shade, sec.visibility, sec.floorpal);

					if(surfaceType == 0)
						pow2xsplit = 1;
					clipper.domost((float) x0, (float) ofy0, (float) x1, (float) ofy1);
					if ((wal.cstat & (2 + 8)) != 0) {
						guo = oguo;
						gux = ogux;
						guy = oguy;
					}
				}
			}

			if ((nextsectnum < 0) || (wal.cstat & 32) != 0) // White/1-way wall
			{
				do
				{
					boolean maskingOneWay = (nextsectnum >= 0 && (wal.cstat&32) != 0);
					if (maskingOneWay)
	                {
	                    if (getclosestpointonwall(globalposx, globalposy, wallnum, projPoint) == 0 && klabs(globalposx - (int)projPoint.x) + klabs(globalposy - (int)projPoint.y) <= 128)
	                        break;
	                }
					
					globalpicnum = (nextsectnum < 0) ? wal.picnum : wal.overpicnum;
					globalshade = wal.shade;
					globalpal = (int) (wal.pal & 0xFF);
					if ((picanm[globalpicnum] & 192) != 0)
						globalpicnum += engine.animateoffs(globalpicnum, wallnum + 16384);

	                boolean nwcs4 = (wal.cstat & 4) == 0;

	                if (nextsectnum >= 0) { i = nwcs4 ? nextsec.ceilingz : sec.ceilingz; }
	                else { i = nwcs4 ? sec.ceilingz : sec.floorz; }

	                // white
					calc_ypanning(i, ryp0, ryp1, x0, x1, wal.ypanning, wal.yrepeat, nwcs4 && !maskingOneWay);
					
					if ((wal.cstat & 8) != 0) // xflip
					{
						t = (fwalxrepeat * 8 + wal.xpanning * 2);
						gux = gdx * t - gux;
						guy = gdy * t - guy;
						guo = gdo * t - guo;
					}
					if ((wal.cstat & 256) != 0) // yflip
					{ 
						gvx = -gvx;
						gvy = -gvy;
						gvo = -gvo;
					} 

					int shade = wal.shade;
					if(UseBloodPal && (globalpal == 1 || sec.floorpal == 1)) //Blood's pal 1
						shade = 0;
					calc_and_apply_fog(shade, sec.visibility, sec.floorpal);
					if(surfaceType == 0)
						pow2xsplit = 1;
					clipper.domost((float)x0, (float)cy0, (float)x1, (float)cy1);
				} while (false);
		
			}

			if (nextsectnum >= 0)
				if (((gotsector[nextsectnum >> 3] & pow2char[nextsectnum & 7]) == 0)
						&& (clipper.testvisiblemost((float) x0, (float) x1) != 0))
					polymost_scansector(nextsectnum); 
		}
	}

	private int polymost_bunchfront(int b1, int b2) {
		double x1b1, x1b2, x2b1, x2b2;
		int b1f, b2f, i;

		b1f = bunchfirst[b1];
		x1b1 = dxb1[b1f];
		x2b2 = dxb2[bunchlast[b2]];
		if (x1b1 >= x2b2)
			return (-1);
		b2f = bunchfirst[b2];
		x1b2 = dxb1[b2f];
		x2b1 = dxb2[bunchlast[b1]];
		if (x1b2 >= x2b1)
			return (-1);

		if (x1b1 >= x1b2) {
			for (i = b2f; dxb2[i] <= x1b1 && p2[i] != -1; i = p2[i]);
			return (wallfront(b1f, i));
		}

		for (i = b1f; dxb2[i] <= x1b2 && p2[i] != -1; i = p2[i]);
		return (wallfront(i, b2f));
	}

	public int wallfront(int l1, int l2) {
		WALL wal;
		int x11, y11, x21, y21, x12, y12, x22, y22, dx, dy, t1, t2;

		wal = wall[thewall[l1]];
		x11 = wal.x;
		y11 = wal.y;
		wal = wall[wal.point2];
		x21 = wal.x;
		y21 = wal.y;
		wal = wall[thewall[l2]];
		x12 = wal.x;
		y12 = wal.y;
		wal = wall[wal.point2];
		x22 = wal.x;
		y22 = wal.y;

		dx = x21 - x11;
		dy = y21 - y11;
		t1 = dmulscale(x12 - x11, dy, -dx, y12 - y11, 2); // p1(l2) vs. l1
		t2 = dmulscale(x22 - x11, dy, -dx, y22 - y11, 2); // p2(l2) vs. l1
		if (t1 == 0) {
			t1 = t2;
			if (t1 == 0)
				return (-1);
		}
		if (t2 == 0)
			t2 = t1;
		if ((t1 ^ t2) >= 0) {
			t2 = dmulscale(globalposx - x11, dy, -dx, globalposy - y11, 2); // pos vs. l1
			return ((t2 ^ t1) >= 0 ? 1 : 0);
		}

		dx = x22 - x12;
		dy = y22 - y12;
		t1 = dmulscale(x11 - x12, dy, -dx, y11 - y12, 2); // p1(l1) vs. l2
		t2 = dmulscale(x21 - x12, dy, -dx, y21 - y12, 2); // p2(l1) vs. l2
		if (t1 == 0) {
			t1 = t2;
			if (t1 == 0)
				return (-1);
		}
		if (t2 == 0)
			t2 = t1;
		if ((t1 ^ t2) >= 0) {
			t2 = dmulscale(globalposx - x12, dy, -dx, globalposy - y12, 2); // pos vs. l2
			return ((t2 ^ t1) < 0 ? 1 : 0);
		}
		return (-2);
	}

	private void polymost_scansector(int sectnum) {
		double d, xp1, yp1, xp2, yp2;
		WALL wal, wal2;
		SPRITE spr;
		int z, zz, startwall, endwall, numscansbefore, scanfirst, bunchfrst, nextsectnum;
		int xs, ys, x1, y1, x2, y2;

		if (sectnum < 0)
			return;

		if (automapping == 1)
			show2dsector[sectnum >> 3] |= pow2char[sectnum & 7];

		sectorborder[0] = sectnum;
		int sectorbordercnt = 1;
		do {
			sectnum = sectorborder[--sectorbordercnt];

			for (z = headspritesect[sectnum]; z >= 0; z = nextspritesect[z]) {
				spr = sprite[z];
				if ((((spr.cstat & 0x8000) == 0) || showinvisibility)
						&& (spr.xrepeat > 0) && (spr.yrepeat > 0) &&
						  (spritesortcnt < MAXSPRITESONSCREEN)) {
					xs = spr.x - globalposx;
					ys = spr.y - globalposy;
					if (((spr.cstat & 48) != 0)
							|| (xs * gcosang + ys * gsinang > 0)
							|| (usemodels && defs != null && defs.mdInfo.getModel(spr.picnum) != null)) {
						if ((spr.cstat & (64 + 48)) != (64 + 16)
								|| dmulscale(sintable[(spr.ang + 512) & 2047],
										-xs, sintable[spr.ang & 2047], -ys, 6) > 0)
						{
							if (tsprite[spritesortcnt] == null)
								tsprite[spritesortcnt] = new SPRITE();
							tsprite[spritesortcnt].set(sprite[z]);

							tsprite[spritesortcnt++].owner = (short) z;
						}
					}
				}
			}

			gotsector[sectnum >> 3] |= pow2char[sectnum & 7];

			bunchfrst = numbunches;
			numscansbefore = numscans;

			if(sector[sectnum] == null) continue;
			startwall = sector[sectnum].wallptr;
			endwall = sector[sectnum].wallnum + startwall;
			scanfirst = numscans;
			xp2 = 0;
			yp2 = 0;
			if(startwall < 0 || endwall < 0) continue;
			for (z = startwall; z < endwall; z++) {
				wal = wall[z];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = wall[wal.point2];
				if(wal2 == null) continue;
				x1 = wal.x - globalposx;
				y1 = wal.y - globalposy;
				x2 = wal2.x - globalposx;
				y2 = wal2.y - globalposy;

				nextsectnum = wal.nextsector; // Scan close sectors

				if ((nextsectnum >= 0)
						&& ((wal.cstat & 32) == 0)
						&& sectorbordercnt < sectorborder.length
						&& ((gotsector[nextsectnum >> 3] & pow2char[nextsectnum & 7]) == 0)) {
					d = x1 * y2 - x2 * y1;
					xp1 = (double) (x2 - x1);
					yp1 = (double) (y2 - y1);
					if (d * d <= (xp1 * xp1 + yp1 * yp1) * (SCISDIST * SCISDIST * 260.0))
					{
						sectorborder[sectorbordercnt++] = nextsectnum;
						gotsector[nextsectnum>>3] |= pow2char[nextsectnum&7];
					}
				}

				if ((z == startwall) || (wall[z - 1].point2 != z)) {
					xp1 = ((double) y1 * (double) cosglobalang - (double) x1
							* (double) singlobalang) / 64.0;
					yp1 = ((double) x1 * (double) cosviewingrangeglobalang + (double) y1
							* (double) sinviewingrangeglobalang) / 64.0;
				} else {
					xp1 = xp2;
					yp1 = yp2;
				}
				xp2 = ((double) y2 * (double) cosglobalang - (double) x2
						* (double) singlobalang) / 64.0;
				yp2 = ((double) x2 * (double) cosviewingrangeglobalang + (double) y2
						* (double) sinviewingrangeglobalang) / 64.0;

				if ((yp1 >= SCISDIST) || (yp2 >= SCISDIST))
					if ((double) xp1 * (double) yp2 < (double) xp2 * (double) yp1) // if wall is facing you...
					{
						if(numscans >= 3600) continue;
						if (yp1 >= SCISDIST)
							dxb1[numscans] = (double) xp1 * ghalfx / (double) yp1 + ghalfx;
						else
							dxb1[numscans] = -1e32;

						if (yp2 >= SCISDIST)
							dxb2[numscans] = (double) xp2 * ghalfx / (double) yp2 + ghalfx;
						else
							dxb2[numscans] = 1e32;

						if (dxb1[numscans] < dxb2[numscans]) {
							thesector[numscans] = (short) sectnum;
							thewall[numscans] = (short) z;
							p2[numscans] = (short) (numscans + 1);
							numscans++;
						}
					}

				if ((wall[z].point2 < z) && (scanfirst < numscans)) {
					p2[numscans - 1] = (short) scanfirst;
					scanfirst = numscans;
				}
			}

			for (z = numscansbefore; z < numscans; z++) {
				if(z >= MAXWALLSB || p2[z] >= MAXWALLSB) continue;
				if ((wall[thewall[z]].point2 != thewall[p2[z]])
						|| (dxb2[z] > dxb1[p2[z]])) {
					bunchfirst[numbunches++] = p2[z];
					p2[z] = -1;
				}
			}

			for (z = bunchfrst; z < numbunches; z++) {
				if(p2[z] >= MAXWALLSB) continue;
				for (zz = bunchfirst[z]; p2[zz] >= 0; zz = p2[zz]);
				bunchlast[z] = (short) zz;
			}
		} while (sectorbordercnt > 0);		
	}
	
	private void drawpapersky(int sectnum, double x0, double x1, double y0, double y1, boolean floor)
	{
		double ox, oy, t;

		short[] dapskyoff = zeropskyoff;
		int dapskybits = pskybits;

		if(dapskybits < 0) dapskybits = 0;
		// Use clamping for tiled sky textures
		for (int i = (1 << dapskybits) - 1; i > 0; i--)
			if (dapskyoff[i] != dapskyoff[i - 1]) {
				skyclamphack = r_parallaxskyclamping;
				break;
			}

		SECTOR sec = sector[sectnum];
		drawalls_dd[0] = xdimen * .0000001; // Adjust sky depth based on screen size!
		t = tilesizx[globalpicnum] << dapskybits; //(1 << (picsiz[globalpicnum] & 15)) << dapskybits;
		drawalls_vv[1] = drawalls_dd[0] * (xdimscale * viewingrange) / (65536.0 * 65536.0);
		drawalls_vv[0] = drawalls_dd[0]
				* ((tilesizy[globalpicnum] >> 1) + parallaxyoffs)
				- drawalls_vv[1] * ghoriz;
		int i = (1 << (picsiz[globalpicnum] >> 4));
		if (i != tilesizy[globalpicnum])
			i += i;
	
		// Hack to draw black rectangle below sky when looking up/down...
		gdx = 0;
		if(floor)
			gdy = gxyaspect / 262144.0;
		else gdy = gxyaspect / -262144.0;
		gdo = -ghoriz * gdy;
		gux = 0;
		guy = 0;
		guo = 0;
		gvx = 0;
		gvy = 0;
		gvo = 0;

		int oskyclamphack = skyclamphack;
		skyclamphack = 0;
		if(floor) {
			oy = (((double) tilesizy[globalpicnum]) * drawalls_dd[0] - drawalls_vv[0]) / drawalls_vv[1];
			
			if ((oy > y0) && (oy > y1)) {
				clipper.domost((float)x0,(float)oy,(float)x1,(float)oy);
			}
			else if ((oy > y0) != (oy > y1))
			{
				//  fy0                      fy1
                //     \                    /
                //oy----------      oy----------
                //        \              /
                //         fy1        fy0
				ox = (oy-y0)*(x1-x0)/(y1-y0) + x0;
				if (oy > y0) 
				{
					clipper.domost((float)x0,(float)oy,(float)ox,(float)oy);
					clipper.domost((float)ox,(float)oy,(float)x1,(float)y1); 
				}
				else 
				{ 
					clipper.domost((float)x0,(float)y0,(float)ox,(float)oy);
				 	clipper.domost((float)ox,(float)oy,(float)x1,(float)oy); 
				 }
			}
			else 
				clipper.domost((float)x0,(float)y0,(float)x1,(float)y1);
		}
		else {
			oy = -drawalls_vv[0] / drawalls_vv[1];

			if ((oy < y0) && (oy < y1)) {
				clipper.domost((float)x1,(float)oy,(float)x0,(float)oy);
			}
	        else if ((oy < y0) != (oy < y1))
	        {
	            /*         cy1        cy0
	            //        /              \
	            //oy----------      oy---------
	            //    /                   \
	            //  cy0                     cy1 */
	            ox = (oy-y0)*(x1-x0)/(y1-y0) + x0;
	            if (oy < y0)
	            {
	                clipper.domost((float)ox,(float)oy,(float)x0,(float)oy);
	                clipper.domost((float)x1,(float)y1,(float)ox,(float)oy);
	            }
	            else
	            {
	                clipper.domost((float)ox,(float)oy,(float)x0,(float)y0);
	                clipper.domost((float)x1,(float)oy,(float)ox,(float)oy);
	            }
	        }
	        else
	            clipper.domost((float)x1,(float)y1,(float)x0,(float)y0);
		}
		skyclamphack = oskyclamphack;

		double panning = sec.ceilingypanning;
		if(floor) panning = sec.floorypanning;

		if (r_parallaxskypanning != 0)
			drawalls_vv[0] += drawalls_dd[0] * panning * i / 256.0;
	
		gdx = 0;
		gdy = 0;
		gdo = drawalls_dd[0];
		gux = gdo //ширина текстуры
				* (t * xdimscale * yxaspect * viewingrange)
				/ (16384.0 * 65536.0 * 65536.0 * 5.0 * 1024.0);
		guy = 0; // guo calculated later
		gvx = 0;
		gvy = drawalls_vv[1];
		gvo = drawalls_vv[0];
		
		i = globalpicnum;
		double r = (y1 - y0) / (x1 - x0); // slope of line
		oy = viewingrange / (ghalfx * 256.0);
		double oz = 1 / oy;

		int y = ((int)(((x0-ghalfx)*oy)+globalang)>>(11-dapskybits));
		
		panning = sec.ceilingxpanning;
		if(floor) panning = sec.floorxpanning;
		
		double fx = x0;
		do {
			globalpicnum = (short) (dapskyoff[y & ((1 << dapskybits) - 1)] + i);
			
			guo = gdo * (t * ((double) (globalang - (y << (11 - dapskybits))))
				/ 2048.0 + (double) ((r_parallaxskypanning != 0) ? panning : 0))
				- gux * ghalfx;
			y++;
			ox = fx;
			fx = ((double) ((y << (11 - dapskybits)) - globalang)) * oz + ghalfx;
			if (fx > x1) {
				fx = x1;
				i = -1;
			}

			pow2xsplit = 0;
			if(floor)
				clipper.domost((float) ox, (float) ((ox - x0) * r + y0), (float) fx, (float) ((fx - x0) * r + y0));
			else clipper.domost((float) fx, (float) ((fx - x0) * r + y0), (float) ox, (float) ((ox - x0) * r + y0));
		} while (i >= 0);
	}
	
	private int[] skywalx = { -512, 512, 512, -512 }, 
				  skywaly = { -512, -512, 512, 512 };
	private void drawskybox(double x0, double x1, double y0, double y1, boolean floor)
	{
		double ox, oy, t;
		int x, y;
		double _xp0, _yp0, _xp1, _yp1, _oxp0, _oyp0, _t0, _t1;
		double _ryp0, _ryp1, _x0, _x1, _cy0, _fy0, _cy1, _fy1, _ox0, _ox1;
		double ny0, ny1;

		pow2xsplit = 0;
		skyclamphack = 1;

		for (int i = 0; i < 4; i++) {
			x = skywalx[i & 3];
			y = skywaly[i & 3];
			_xp0 = (double) y * gcosang - (double) x * gsinang;
			_yp0 = (double) x * gcosang2 + (double) y * gsinang2;
			x = skywalx[(i + 1) & 3];
			y = skywaly[(i + 1) & 3];
			_xp1 = (double) y * gcosang - (double) x * gsinang;
			_yp1 = (double) x * gcosang2 + (double) y * gsinang2;

			_oxp0 = _xp0;
			_oyp0 = _yp0;

			// Clip to close parallel-screen plane
			if (_yp0 < SCISDIST) {
				if (_yp1 < SCISDIST)
					continue;
				_t0 = (SCISDIST - _yp0) / (_yp1 - _yp0);
				_xp0 = (_xp1 - _xp0) * _t0 + _xp0;
				_yp0 = SCISDIST;
			} else {
				_t0 = 0.f;
			}
			if (_yp1 < SCISDIST) {
				_t1 = (SCISDIST - _oyp0) / (_yp1 - _oyp0);
				_xp1 = (_xp1 - _oxp0) * _t1 + _oxp0;
				_yp1 = SCISDIST;
			} else {
				_t1 = 1.f;
			}

			_ryp0 = 1.f / _yp0;
			_ryp1 = 1.f / _yp1;

			// Generate screen coordinates for front side of wall
			_x0 = ghalfx * _xp0 * _ryp0 + ghalfx;
			_x1 = ghalfx * _xp1 * _ryp1 + ghalfx;
			if (_x1 <= _x0)
				continue;
			if ((_x0 >= x1) || (x0 >= _x1))
				continue;

			_ryp0 *= gyxscale;
			_ryp1 *= gyxscale;

			_cy0 = -8192.f * _ryp0 + ghoriz;
			_fy0 = 8192.f * _ryp0 + ghoriz;
			_cy1 = -8192.f * _ryp1 + ghoriz;
			_fy1 = 8192.f * _ryp1 + ghoriz;

			_ox0 = _x0;
			_ox1 = _x1;

			// Make sure: x0<=_x0<_x1<=_x1
			 ny0 = y0;
			 ny1 = y1;
			if (_x0 < x0) {
				t = (x0 - _x0) / (_x1 - _x0);
				_cy0 += (_cy1 - _cy0) * t;
				_fy0 += (_fy1 - _fy0) * t;
				_x0 = x0;
			}
			 else if (_x0 > x0)
			 ny0 += (_x0 - x0) * (y1 - y0) / (x1 - x0);
			if (_x1 > x1) {
				t = (x1 - _x1) / (_x1 - _x0);
				_cy1 += (_cy1 - _cy0) * t;
				_fy1 += (_fy1 - _fy0) * t;
				_x1 = x1;
			}
			 else if (_x1 < x1)
			 ny1 += (_x1 - x1) * (y1 - y0) / (x1 - x0);

			// floor of skybox

			drawalls_ft[0] = 512 / 16;
			drawalls_ft[1] = -512 / -16;
			if(floor)
				drawalls_ft[1] = 512 / -16;
			
			drawalls_ft[2] = ((float) cosglobalang)
					* (1.f / 2147483648.f);
			drawalls_ft[3] = ((float) singlobalang)
					* (1.f / 2147483648.f);
			gdx = 0;
			gdy = gxyaspect * -(1.f / 4194304.f);
			if(floor)
				gdy = gxyaspect * (1.f / 4194304.f);
			gdo = -ghoriz * gdy;
			gux = (double) drawalls_ft[3] * ((double) viewingrange)
					/ -65536.0;
			gvx = (double) drawalls_ft[2] * ((double) viewingrange)
					/ -65536.0;
			guy = (double) drawalls_ft[0] * gdy;
			gvy = (double) drawalls_ft[1] * gdy;
			guo = (double) drawalls_ft[0] * gdo;
			gvo = (double) drawalls_ft[1] * gdo;
			guo += (double) (drawalls_ft[2] - gux) * ghalfx;
			gvo -= (double) (drawalls_ft[3] + gvx) * ghalfx;
			
			if(floor) {
				gvx = -gvx;
				gvy = -gvy;
				gvo = -gvo; // y-flip skybox floor
	
				drawingskybox = 6; // floor/6th texture/index 4 of  skybox
			
				if ((_fy0 > ny0) && (_fy1 > ny1))
					clipper.domost((float)_x0,(float)_fy0,(float)_x1,(float)_fy1);
				else if ((_fy0 > ny0) != (_fy1 > ny1))
				{
					t = (_fy0-ny0)/(ny1-ny0-_fy1+_fy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _fy0 + (_fy1-_fy0)*t;
					if (ny0 > _fy0) {
						clipper.domost((float)_x0,(float)ny0,(float)ox,(float)oy);
						clipper.domost((float)ox,(float)oy,(float)_x1,(float)_fy1); }
					else {
						clipper.domost((float)_x0,(float)_fy0,(float)ox,(float)oy);
						clipper.domost((float)ox,(float)oy,(float)_x1,(float)ny1); }
				}
				else
					clipper.domost((float)_x0,(float)ny0,(float)_x1,(float)ny1);
			} else {
				
				drawingskybox = 5; // ceiling/5th texture/index 4 of skybox

				if ((_cy0 < ny0) && (_cy1 < ny1))
					clipper.domost((float)_x1,(float)_cy1,(float)_x0,(float)_cy0);
				else if ((_cy0 < ny0) != (_cy1 < ny1))
				{
					t = (_cy0-ny0)/(ny1-ny0-_cy1+_cy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _cy0 + (_cy1-_cy0)*t;
					if (ny0 < _cy0) {
						clipper.domost((float)ox,(float)oy,(float)_x0,(float)ny0);
						clipper.domost((float)_x1,(float)_cy1,(float)ox,(float)oy); }
					else {
						clipper.domost((float)ox,(float)oy,(float)_x0,(float)_cy0);
						clipper.domost((float)_x1,(float)ny1,(float)ox,(float)oy); }
				}
				else
					clipper.domost((float)_x1,(float)ny1,(float)_x0,(float)ny0);
			}

			// wall of skybox

			drawingskybox = (short) (i + 1); // i+1th texture/index i of skybox

			gdx = (_ryp0 - _ryp1) * gxyaspect * (1.f / 512.f)
					/ (_ox0 - _ox1);
			gdy = 0;
			gdo = _ryp0 * gxyaspect * (1.f / 512.f) - gdx * _ox0;
			gux = (_t0 * _ryp0 - _t1 * _ryp1) * gxyaspect
					* (64.f / 512.f) / (_ox0 - _ox1);
			guo = _t0 * _ryp0 * gxyaspect * (64.f / 512.f) - gux
					* _ox0;
			guy = 0;
			_t0 = -8192.0 * _ryp0 + ghoriz;
			_t1 = -8192.0 * _ryp1 + ghoriz;
			t = ((gdx * _ox0 + gdo) * 8.f)
					/ ((_ox1 - _ox0) * _ryp0 * 2048.f);
			gvx = (_t0 - _t1) * t;
			gvy = (_ox1 - _ox0) * t;
			gvo = -gvx * _ox0 - gvy * _t0;
			
			if(floor) {
				if ((_cy0 > ny0) && (_cy1 > ny1))
					clipper.domost((float)_x0,(float)_cy0,(float)_x1,(float)_cy1);
				else if ((_cy0 > ny0) != (_cy1 > ny1))
				{
					t = (_cy0-ny0)/(ny1-ny0-_cy1+_cy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _cy0 + (_cy1-_cy0)*t;
					if (ny0 > _cy0) {
						clipper.domost((float)_x0,(float)ny0,(float)ox,(float)oy);
						clipper.domost((float)ox,(float)oy,(float)_x1,(float)_cy1); }
					else {
						clipper.domost((float)_x0,(float)_cy0,(float)ox,(float)oy);
						clipper.domost((float)ox,(float)oy,(float)_x1,(float)ny1); }
				}
				else
					clipper.domost((float)_x0,(float)ny0,(float)_x1,(float)ny1);
			} else {
				if ((_fy0 < ny0) && (_fy1 < ny1))
					clipper.domost((float)_x1,(float)_fy1,(float)_x0,(float)_fy0);
				else if ((_fy0 < ny0) != (_fy1 < ny1))
				{
					t = (_fy0-ny0)/(ny1-ny0-_fy1+_fy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _fy0 + (_fy1-_fy0)*t;
					if (ny0 < _fy0) {
						clipper.domost((float)ox,(float)oy,(float)_x0,(float)ny0);
						clipper.domost((float)_x1,(float)_fy1,(float)ox,(float)oy); }
					else {
						clipper.domost((float)ox,(float)oy,(float)_x0,(float)_fy0);
				 		clipper.domost((float)_x1,(float)ny1,(float)ox,(float)oy); }
				}
				else
					clipper.domost((float)_x1,(float)ny1,(float)_x0,(float)ny0);
			}
		}

		// Ceiling of skybox

		drawingskybox = 6; // floor/6th texture/index 5 of skybox
		if(floor) 
			drawingskybox = 5;
		

		drawalls_ft[0] = 512 / 16;
		drawalls_ft[1] = 512 / -16;
		if(floor) 
			drawalls_ft[1] = -512 / -16;
		drawalls_ft[2] = ((float) cosglobalang)
				* (1.f / 2147483648.f);
		drawalls_ft[3] = ((float) singlobalang)
				* (1.f / 2147483648.f);
		gdx = 0;
		gdy = gxyaspect * (1.f / 4194304.f);
		if(floor)
			gdy = gxyaspect * (-1.f / 4194304.f);
		gdo = -ghoriz * gdy;
		gux = (double) drawalls_ft[3] * ((double) viewingrange)
				/ -65536.0;
		gvx = (double) drawalls_ft[2] * ((double) viewingrange)
				/ -65536.0;
		guy = (double) drawalls_ft[0] * gdy;
		gvy = (double) drawalls_ft[1] * gdy;
		guo = (double) drawalls_ft[0] * gdo;
		gvo = (double) drawalls_ft[1] * gdo;
		guo += (double) (drawalls_ft[2] - gux) * ghalfx;
		gvo -= (double) (drawalls_ft[3] + gvx) * ghalfx;
		
		if(floor)
			clipper.domost((float) x0, (float) y0, (float) x1, (float) y1);
		else {
			gvx = -gvx;
			gvy = -gvy;
			gvo = -gvo; // y-flip skybox floor
			clipper.domost((float) x1, (float) y1, (float) x0, (float) y0);
		}

		skyclamphack = 0;

		drawingskybox = 0;
	}

	private void drawbackground(int sectnum, double x0, double x1, double y0, double y1, boolean floor) {
		// Parallaxing sky... hacked for Ken's mountain texture;

		SECTOR sec = sector[sectnum];
		int shade = sec.floorshade;
		int pal = sec.floorpal;
		if(!floor)
		{
			shade = sec.ceilingshade;
			pal = sec.ceilingpal;
		}
		
		calc_and_apply_skyfog(shade, sec.visibility,  pal);

		if (!usehightile || defs == null || defs.texInfo.findTexture(globalpicnum, globalpal, 1) == null)
			drawpapersky(sectnum, x0, x1, y0, y1, floor);
		else
			drawskybox(x0, x1, y0, y1, floor);

		skyclamphack = 0;
		calc_and_apply_fog(shade, sec.visibility,  pal);
	}
	
	private final double[] drawrooms_px = new double[6],
			drawrooms_py = new double[6], drawrooms_pz = new double[6],
			drawrooms_px2 = new double[6], drawrooms_py2 = new double[6],
			drawrooms_pz2 = new double[6], drawrooms_sx = new double[6],
			drawrooms_sy = new double[6];

	public double defznear = 0.1;
	public double defzfar = 0.9;
	public void drawrooms() // eduke32
	{
		int i, j, n, n2, closest;
		double ox, oy, oz, ox2, oy2, oz2, r;

		if(offscreenrendering) {
			if(setviewcnt == 1)
				ogshang = gshang;
		} else if(ogshang != -1) gshang = ogshang;
		
		resizeglcheck();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glDepthFunc(GL_LEQUAL); // NEVER,LESS,(,L)EQUAL,GREATER,(NOT,G)EQUAL,ALWAYS
		gl.glDepthRange(defznear, defzfar); //<- this is more widely supported than glPolygonOffset

		// Polymost supports true look up/down :) Here, we convert horizon to angle.
		// gchang&gshang are cos&sin of this angle (respectively)
		gyxscale = xdimenscale / 131072.0f;
		gxyaspect = (viewingrange / 65536.0f) * xyaspect * 5.0f / 262144.0f;
		gviewxrange = viewingrange * xdimen / (32768.0f * 1024.0f);
		gcosang = cosglobalang / 262144.0f;
		gsinang = singlobalang / 262144.0f;
		gcosang2 = gcosang * (viewingrange / 65536.0f);
		gsinang2 = gsinang * (viewingrange / 65536.0f);
		ghalfx = halfxdimen;
		grhalfxdown10 = 1.0f / (ghalfx * 1024.0f); //viewport
		// global cos/sin height angle
		ghoriz = ydimen >> 1;
		r = (ghoriz - globalhoriz);
		gshang = (float) (r / sqrt(r * r + ghalfx * ghalfx));
		gchang = (float) sqrt(1.0f - gshang * gshang);
		
		// global cos/sin tilt angle
		gctang = (float) cos(gtang);
		gstang = (float) sin(gtang);

		if (abs(gstang) < .001) // This hack avoids nasty precision bugs in domost()
		{
			gstang = 0;
			if (gctang > 0)
				gctang = 1.0f;
			else
				gctang = -1.0f;
		}

		if (inpreparemirror)
			gstang = -gstang;

		// Generate viewport trapezoid (for handling screen up/down)
		drawrooms_px[0] = drawrooms_px[3] = 0 - 1;
		drawrooms_px[1] = drawrooms_px[2] = windowx2 + 1 - windowx1 + 2;
		drawrooms_py[0] = drawrooms_py[1] = 0 - 1;
		drawrooms_py[2] = drawrooms_py[3] = windowy2 + 1 - windowy1 + 2;
		n = 4;

		for (i = 0; i < n; i++) {
			ox = drawrooms_px[i] - ghalfx;
			oy = drawrooms_py[i] - ghoriz;
			oz = ghalfx;

			// Tilt rotation (backwards)
			ox2 = ox * gctang + oy * gstang;
			oy2 = oy * gctang - ox * gstang;
			oz2 = oz;

			// Up/down rotation (backwards)
			drawrooms_px[i] = ox2;
			drawrooms_py[i] = oy2 * gchang + oz2 * gshang;
			drawrooms_pz[i] = oz2 * gchang - oy2 * gshang;
		}

		// Clip to SCISDIST plane
		n2 = 0;
		for (i = 0; i < n; i++) {
			j = i + 1;
			if (j >= n) j = 0;
			if (drawrooms_pz[i] >= SCISDIST) {
				drawrooms_px2[n2] = drawrooms_px[i];
				drawrooms_py2[n2] = drawrooms_py[i];
				drawrooms_pz2[n2] = drawrooms_pz[i];
				n2++;
			}

			if ((drawrooms_pz[i] >= SCISDIST) != (drawrooms_pz[j] >= SCISDIST)) {
				r = (SCISDIST - drawrooms_pz[i]) / (drawrooms_pz[j] - drawrooms_pz[i]);
				drawrooms_px2[n2] = (drawrooms_px[j] - drawrooms_px[i]) * r + drawrooms_px[i];
				drawrooms_py2[n2] = (drawrooms_py[j] - drawrooms_py[i]) * r + drawrooms_py[i];
				drawrooms_pz2[n2] = SCISDIST;
				n2++;
			}
		}

		if (n2 < 3) {
			return;
		}
		for (i = 0; i < n2; i++) {
			r = ghalfx / drawrooms_pz2[i];
			drawrooms_sx[i] = drawrooms_px2[i] * r + ghalfx;
			drawrooms_sy[i] = drawrooms_py2[i] * r + ghoriz;
		}
		
		clipper.initmosts(drawrooms_sx, drawrooms_sy, n2);

		numscans = numbunches = 0;

		// MASKWALL_BAD_ACCESS
		// Fixes access of stale maskwall[maskwallcnt] (a "scan" index, in BUILD lingo):
		maskwallcnt = 0;
		if (globalcursectnum >= MAXSECTORS) {
			globalcursectnum -= MAXSECTORS;
		}
		else {
			i = globalcursectnum;
			globalcursectnum = engine.updatesectorz(globalposx, globalposy, globalposz, globalcursectnum);
			if (globalcursectnum < 0)
				globalcursectnum = (short) i;
		}

		polymost_scansector(globalcursectnum);
		
		surfaces.clear();
		
		grhalfxdown10x = grhalfxdown10;

		if (inpreparemirror) {
			grhalfxdown10x = -grhalfxdown10;
			inpreparemirror = false;

			// see engine.c: INPREPAREMIRROR_NO_BUNCHES
			if (numbunches > 0) {
				drawalls(0);
				numbunches--;
				bunchfirst[0] = bunchfirst[numbunches];
				bunchlast[0] = bunchlast[numbunches];
			}
		} 

		while (numbunches > 0) {
			Arrays.fill(ptempbuf, 0, numbunches+3, (byte)0);
			ptempbuf[0] = 1;
			closest = 0; // Almost works, but not quite :(

			for (i = 1; i < numbunches; ++i) {
				j = polymost_bunchfront(i, closest);
				if (j < 0)
					continue;
				ptempbuf[i] = 1;
				if (j == 0) {
					ptempbuf[closest] = 1;
					closest = i;
				}
			}

			for (i = 0; i < numbunches; ++i) // Double-check
			{
				if (ptempbuf[i] != 0)
					continue;
				j = polymost_bunchfront(i, closest); //ArrayIndexOutOfBoundsException: -1 FIXME
				if (j < 0)
					continue;
				ptempbuf[i] = 1;
				if (j == 0) {
					ptempbuf[closest] = 1;
					closest = i;
					i = 0;
				}
			}
			
			drawalls(closest);

			numbunches--;
			bunchfirst[closest] = bunchfirst[numbunches];
			bunchlast[closest] = bunchlast[numbunches];
		}
		
		drawsurfaces(surfaces);
	}
	
	private final Surface[] dmaskwall = new Surface[8];

	private final float[] drawmaskwall_csy = new float[4], drawmaskwall_fsy = new float[4];
	private final int[] drawmaskwall_cz = new int[4], drawmaskwall_fz = new int[4];

	public void drawmaskwall(int damaskwallcnt) {
		float x0, x1, sx0, sy0, sx1, sy1, xp0, yp0, xp1, yp1, oxp0, oyp0, ryp0, ryp1;
		float r, t, t0, t1;
		int i, j, n, n2, z, sectnum, method;

		int m0, m1;
		SECTOR sec, nsec;
		WALL wal, wal2;

		// cullcheckcnt = 0;

		z = maskwall[damaskwallcnt];
		wal = wall[thewall[z]];
		wal2 = wall[wal.point2];
		sectnum = thesector[z];
		
		if(sectnum == -1 || wal.nextsector == -1) 
			return;
		
		sec = sector[sectnum];
		nsec = sector[wal.nextsector];
		
		globalpicnum = wal.overpicnum;
		if (globalpicnum >= MAXTILES)
			globalpicnum = 0;
		if ((picanm[globalpicnum] & 192) != 0)
			globalpicnum += engine.animateoffs(globalpicnum, thewall[z] + 16384);
		globalshade = (int) wal.shade;
		globalpal = (int) (wal.pal & 0xFF);
		globalorientation = (int) wal.cstat;

		sx0 = (float) (wal.x - globalposx);
		sx1 = (float) (wal2.x - globalposx);
		sy0 = (float) (wal.y - globalposy);
		sy1 = (float) (wal2.y - globalposy);
		yp0 = (float) (sx0 * gcosang2 + sy0 * gsinang2);
		yp1 = (float) (sx1 * gcosang2 + sy1 * gsinang2);
		if ((yp0 < SCISDIST) && (yp1 < SCISDIST))
			return;
		xp0 = (float) (sy0 * gcosang - sx0 * gsinang);
		xp1 = (float) (sy1 * gcosang - sx1 * gsinang);

		// Clip to close parallel-screen plane
		oxp0 = xp0;
		oyp0 = yp0;
		
		t0 = 0.f;
		
		if (yp0 < SCISDIST) {
			t0 = (float) ((SCISDIST - yp0) / (yp1 - yp0));
			xp0 = (xp1 - xp0) * t0 + xp0;
			yp0 = (float) SCISDIST;
		} 
		
		t1 = 1.f;
		
		if (yp1 < SCISDIST) {
			t1 = (float) ((SCISDIST - oyp0) / (yp1 - oyp0));
			xp1 = (xp1 - oxp0) * t1 + oxp0;
			yp1 = (float) SCISDIST;
		} 

		m0 = (int) ((wal2.x - wal.x) * t0 + wal.x);
		m1 = (int) ((wal2.y - wal.y) * t0 + wal.y);
		polymost_getzsofslope(sectnum, m0, m1);
		drawmaskwall_cz[0] = (int)dceilzsofslope;
		drawmaskwall_fz[0] = (int)dfloorzsofslope;
		polymost_getzsofslope(wal.nextsector, m0, m1);
		drawmaskwall_cz[1] = (int)dceilzsofslope;
		drawmaskwall_fz[1] = (int)dfloorzsofslope;
		m0 = (int) ((wal2.x - wal.x) * t1 + wal.x);
		m1 = (int) ((wal2.y - wal.y) * t1 + wal.y);
		polymost_getzsofslope(sectnum, m0, m1);
		drawmaskwall_cz[2] = (int)dceilzsofslope;
		drawmaskwall_fz[2] = (int)dfloorzsofslope;
		polymost_getzsofslope(wal.nextsector, m0, m1);
		drawmaskwall_cz[3] = (int)dceilzsofslope;
		drawmaskwall_fz[3] = (int)dfloorzsofslope;

		ryp0 = 1.f / yp0;
		ryp1 = 1.f / yp1;

		// Generate screen coordinates for front side of wall
		x0 = (float) (ghalfx * xp0 * ryp0 + ghalfx);
		x1 = (float) (ghalfx * xp1 * ryp1 + ghalfx);
		if (x1 <= x0)
			return;

		ryp0 *= gyxscale;
		ryp1 *= gyxscale;

		gdx = (ryp0 - ryp1) * gxyaspect / (x0 - x1);
		gdy = 0;
		gdo = ryp0 * gxyaspect - gdx * x0;

		gux = (t0 * ryp0 - t1 * ryp1) * gxyaspect
				* (float) (wal.xrepeat & 0xFF) * 8.f / (x0 - x1);
		guo = t0 * ryp0 * gxyaspect * (float) (wal.xrepeat & 0xFF) * 8.f - gux
				* x0;
		guo += (float) wal.xpanning * gdo;
		gux += (float) wal.xpanning * gdx;
		guy = 0;

		// mask
	    calc_ypanning(((wal.cstat & 4) == 0) ? max(nsec.ceilingz, sec.ceilingz) : min(nsec.floorz, sec.floorz), ryp0, ryp1,
	                  x0, x1, wal.ypanning, wal.yrepeat, false);
	    
		if ((wal.cstat & 8) != 0) // xflip
		{
			t = (float) ((wal.xrepeat & 0xFF) * 8 + wal.xpanning * 2);
			gux = gdx * t - gux;
			guy = gdy * t - guy;
			guo = gdo * t - guo;
		}
		if ((wal.cstat & 256) != 0) {
			gvx = -gvx;
			gvy = -gvy;
			gvo = -gvo;
		} // yflip

		method = 1;
		if(surfaceType == 0)
			pow2xsplit = 1;
		if ((wal.cstat & 128) != 0) {
			if ((wal.cstat & 512) == 0)
				method = 2;
			else
				method = 3;
		}

		int shade = wal.shade;
		if(UseBloodPal && (globalpal == 1 || sec.floorpal == 1)) //Blood's pal 1
			shade = 0;
		calc_and_apply_fog(shade, sec.visibility, sec.floorpal);
		
		drawmaskwall_csy[0] = (float) ((drawmaskwall_cz[0] - globalposz) * ryp0 + ghoriz);
		drawmaskwall_csy[1] = (float) ((drawmaskwall_cz[1] - globalposz) * ryp0 + ghoriz);
		drawmaskwall_csy[2] = (float) ((drawmaskwall_cz[2] - globalposz) * ryp1 + ghoriz);
		drawmaskwall_csy[3] = (float) ((drawmaskwall_cz[3] - globalposz) * ryp1 + ghoriz);
              
		drawmaskwall_fsy[0] = (float) ((drawmaskwall_fz[0] - globalposz) * ryp0 + ghoriz);
		drawmaskwall_fsy[1] = (float) ((drawmaskwall_fz[1] - globalposz) * ryp0 + ghoriz);
		drawmaskwall_fsy[2] = (float) ((drawmaskwall_fz[2] - globalposz) * ryp1 + ghoriz);
		drawmaskwall_fsy[3] = (float) ((drawmaskwall_fz[3] - globalposz) * ryp1 + ghoriz);

		// Clip 2 quadrilaterals
		// /csy3
		// / |
		// csy0------/----csy2
		// | /xxxxxxx|
		// | /xxxxxxxxx|
		// csy1/xxxxxxxxxxx|
		// |xxxxxxxxxxx/fsy3
		// |xxxxxxxxx/ |
		// |xxxxxxx/ |
		// fsy0----/------fsy2
		// | /
		// fsy1/

		dmaskwall[0].px = x0;
		dmaskwall[0].py = drawmaskwall_csy[1];
		dmaskwall[1].px = x1;
		dmaskwall[1].py = drawmaskwall_csy[3];
		dmaskwall[2].px = x1;
		dmaskwall[2].py = drawmaskwall_fsy[3];
		dmaskwall[3].px = x0;
		dmaskwall[3].py = drawmaskwall_fsy[1];
		n = 4;

		// Clip to (x0,csy[0])-(x1,csy[2])
		n2 = 0;
		t1 = (float) -((dmaskwall[0].px - x0)
				* (drawmaskwall_csy[2] - drawmaskwall_csy[0]) - (dmaskwall[0].py - drawmaskwall_csy[0])
						* (x1 - x0));
		for (i = 0; i < n; i++) {
			j = i + 1;
			if (j >= n)
				j = 0;

			t0 = t1;
			t1 = (float) -((dmaskwall[j].px - x0)
					* (drawmaskwall_csy[2] - drawmaskwall_csy[0]) - (dmaskwall[j].py - drawmaskwall_csy[0])
							* (x1 - x0));
			if (t0 >= 0) {
				dmaskwall[n2].px2 = dmaskwall[i].px;
				dmaskwall[n2].py2 = dmaskwall[i].py;
				n2++;
			}
			if ((t0 >= 0) != (t1 >= 0)) {
				r = t0 / (t0 - t1);
				dmaskwall[n2].px2 = (dmaskwall[j].px - dmaskwall[i].px) * r + dmaskwall[i].px;
				dmaskwall[n2].py2 = (dmaskwall[j].py - dmaskwall[i].py) * r + dmaskwall[i].py;
				n2++;
			}
		}
		if (n2 < 3)
			return;

		// Clip to (x1,fsy[2])-(x0,fsy[0])
		n = 0;
		t1 = (float) -((dmaskwall[0].px2 - x1)
				* (drawmaskwall_fsy[0] - drawmaskwall_fsy[2]) - (dmaskwall[0].py2 - drawmaskwall_fsy[2])
						* (x0 - x1));
		for (i = 0; i < n2; i++) {
			j = i + 1;
			if (j >= n2)
				j = 0;

			t0 = t1;
			t1 = (float) -((dmaskwall[j].px2 - x1)
					* (drawmaskwall_fsy[0] - drawmaskwall_fsy[2]) - (dmaskwall[j].py2 - drawmaskwall_fsy[2])
							* (x0 - x1));
			if (t0 >= 0) {
				dmaskwall[n].px = dmaskwall[i].px2;
				dmaskwall[n].py = dmaskwall[i].py2;
				n++;
			}
			if ((t0 >= 0) != (t1 >= 0)) {
				r = t0 / (t0 - t1);
				dmaskwall[n].px = (dmaskwall[j].px2 - dmaskwall[i].px2)
						* r + dmaskwall[i].px2;
				dmaskwall[n].py = (dmaskwall[j].py2 - dmaskwall[i].py2)
						* r + dmaskwall[i].py2;
				n++;
			}
		}
		if (n < 3)
			return;

		gl.glDepthRange(defznear+0.000001, defzfar-0.00001);
		drawpoly(dmaskwall, n, method);
		gl.glDepthRange(defznear, defzfar);
	}

	private static Vector2 projPoint = new Vector2();
	private int getclosestpointonwall(int posx, int posy, int dawall, Vector2 n)
	{
		WALL w = wall[dawall];
	    WALL p2 = wall[wall[dawall].point2];
	    int dx = p2.x - w.x;
	    int dy = p2.y - w.y;
		
	    float i = dx * (posx - w.x) + dy * (posy - w.y);

	    if (i < 0)
	        return 1;

	    float j = dx * dx + dy * dy;

	    if (i > j)
	        return 1;

	    i /= j;

	    n.set(dx * i + w.x, dy * i + w.y);

	    return 0;
	}
	
	private final float TSPR_OFFSET_FACTOR = 0.000008f;
	private float TSPR_OFFSET(SPRITE tspr, long dist)
	{
		float offset = (TSPR_OFFSET_FACTOR + ((tspr.owner != -1 ? tspr.owner & 31 : 1) * TSPR_OFFSET_FACTOR)) * dist * 0.025f;
		return -offset;
	}

	private final Surface dsprite[] = new Surface[6];
	private final float drawsprite_ft[] = new float[4];
	private final Vector2[] dsin = new Vector2[MAXSPRITES];
	private final Vector2[] dcoord = new Vector2[MAXSPRITES];
	private final int[] spritewall = new int[MAXSPRITES];

	private void drawsprite(int snum) {
		float f, c, s, fx, fy, sx0, sy0, sx1, xp0, yp0, xp1, yp1, oxp0, oyp0, ryp0, ryp1;
		float x0, y0, x1, y1, sc0, sf0, sc1, sf1, xv, yv, t0, t1;
		int i, j, spritenum, xoff = 0, yoff = 0, method, npoints;
		SPRITE tspr;
		int posx, posy;
		int oldsizx, oldsizy;
		int tsizx, tsizy;

		tspr = tspriteptr[snum];

		if (tspr.owner < 0 || tspr.picnum < 0 || tspr.picnum >= MAXTILES || tspr.sectnum < 0)
			return;

		globalpicnum = tspr.picnum;
		globalshade = tspr.shade;
		globalpal = tspr.pal & 0xFF;
		globalorientation = tspr.cstat;
		spritenum = tspr.owner;

//		globvis = globalvisibility;
//		if (sector[tspr.sectnum].visibility != 0)
//			globvis = mulscale((int) globvis, (byte) ((sector[tspr.sectnum].visibility & 0xFF) + 16), 4);

		if ((globalorientation & 48) != 48) {
			boolean flag;

			if ((picanm[globalpicnum] & 192) != 0) {
				globalpicnum += engine.animateoffs(globalpicnum, spritenum + 32768);
			}

			flag = (usehightile && h_xsize[globalpicnum] != 0);
			xoff = tspr.xoffset;
			yoff = tspr.yoffset;
			xoff += (byte) (flag ? h_xoffs[globalpicnum] : ((picanm[globalpicnum] >> 8) & 255));
			yoff += (byte) (flag ? h_yoffs[globalpicnum] : ((picanm[globalpicnum] >> 16) & 255));
		}

		method = 1 + 4;
		if ((tspr.cstat & 2) != 0) {
			if ((tspr.cstat & 512) == 0)
				method = 2 + 4;
			else
				method = 3 + 4;
		}
		
		int shade = (int) (globalshade / 1.5f);
		if(UseBloodPal) {
			if(tspr.pal == 5 && tspr.shade == 127)
				shade = 0; //Blood's shadows (for pal 1)
			if(globalpal == 1 || tspr.pal == 1) //Blood's pal 1
				shade = 0;
		}
		calc_and_apply_fog(shade, sector[tspr.sectnum].visibility, sector[tspr.sectnum].floorpal);
		
		posx = tspr.x;
		posy = tspr.y;

		while ((spriteext[tspr.owner].flags & SPREXT_NOTMD) == 0) {

			if(usemodels) {
				Tile2model entry = defs != null ? defs.mdInfo.getParams(tspr.picnum) : null;
				if (entry != null &&
						entry.model != null && entry.framenum >= 0) {
					if (tspr.owner < 0 || tspr.owner >= MAXSPRITES /* || tspr.statnum == TSPR_MIRROR */ ) {
						if (mddraw(tspr, xoff, yoff) != 0)
							return;
						break; // else, render as flat sprite
					}
	
					if (mddraw(tspr, xoff, yoff) != 0)
						return;
					break; // else, render as flat sprite
				}
			}
			
			if(usevoxels) {
				Tile2model entry = defs != null ? defs.mdInfo.getParams(globalpicnum) : null;
				if(entry != null) {
					int dist = (posx - globalposx) * (posx - globalposx) + (posy - globalposy) * (posy - globalposy);
					if(dist < 48000L * 48000L && entry.voxel != null) {
						if ((tspr.cstat & 48) != 48) {
							if (voxdraw(entry.voxel, tspr, xoff, yoff) != 0)
								return;
							break; // else, render as flat sprite
						}
			
						if ((tspr.cstat & 48) == 48) {
							voxdraw(entry.voxel, tspr, xoff, yoff);
							return;
						}
					}
				}
			}
			break;
		}
		
		if ((spriteext[tspr.owner].flags & SPREXT_AWAY1) != 0) {
			posx += (sintable[(tspr.ang + 512) & 2047] >> 13);
			posy += (sintable[(tspr.ang) & 2047] >> 13);

		} else if ((spriteext[tspr.owner].flags & SPREXT_AWAY2) != 0) {
			posx -= (sintable[(tspr.ang + 512) & 2047] >> 13);
			posy -= (sintable[(tspr.ang) & 2047] >> 13);
		}
		oldsizx = tsizx = tilesizx[globalpicnum];
		oldsizy = tsizy = tilesizy[globalpicnum];

		if (usehightile && h_xsize[globalpicnum] != 0) {
			tsizx = h_xsize[globalpicnum];
			tsizy = h_ysize[globalpicnum];
		}

		if (tsizx <= 0 || tsizy <= 0)
			return;
		
		long dist;

		float foffs, offsx, offsy;
		int ang;
		switch ((globalorientation >> 4) & 3) {
		case 0: // Face sprite
			// Project 3D to 2D
			if ((globalorientation & 4) != 0)
				xoff = -xoff;
			// NOTE: yoff not negated not for y flipping, unlike wall and floor
			// aligned sprites.
			
			dist = engine.qdist(globalposx - tspr.x, globalposy - tspr.y);
			ang = (engine.getangle(tspr.x - globalposx, tspr.y - globalposy) + 1024) & 2047;
			foffs = TSPR_OFFSET(tspr, dist);
			dist *= (dist >> 7);
			
			offsx = (float) (sintable[(ang + 512) & 2047] >> 6) * foffs;
			offsy = (float) (sintable[(ang) & 2047] >> 6) * foffs;

			sx0 = (float) (tspr.x - globalposx - offsx);
			sy0 = (float) (tspr.y - globalposy - offsy);
			xp0 = (float) (sy0 * gcosang - sx0 * gsinang);
			yp0 = (float) (sx0 * gcosang2 + sy0 * gsinang2);

			if (yp0 <= SCISDIST)
				return;
			ryp0 = 1.0f / yp0;
			sx0 = (float) (ghalfx * xp0 * ryp0 + ghalfx);
			sy0 = (float) ((tspr.z - globalposz) * gyxscale * ryp0 + ghoriz);

			f = ryp0 * (float) xdimen * (1.0f / 160.f);
			fx = ((float) tspr.xrepeat) * f;
			fy = ((float) tspr.yrepeat) * f * ((float) yxaspect * (1.0f / 65536.f));

			sx0 -= fx * (float) xoff;
			if ((tsizx & 1) != 0)
				sx0 += fx * 0.5f;
			sy0 -= fy * (float) yoff;
			if ((globalorientation & 128) != 0 && (tsizy & 1) != 0)
				sy0 += fy * 0.5f;

			fx *= ((float) tsizx);
			fy *= ((float) tsizy);

			dsprite[0].px = dsprite[3].px = sx0 - fx * .5f;
			dsprite[1].px = dsprite[2].px = sx0 + fx * .5f;
			if ((globalorientation & 128) == 0) {
				dsprite[0].py = dsprite[1].py = sy0 - fy;
				dsprite[2].py = dsprite[3].py = sy0;
			} else {
				dsprite[0].py = dsprite[1].py = sy0 - fy * .5f;
				dsprite[2].py = dsprite[3].py = sy0 + fy * .5f;
			}

			gdx = gdy = guy = gvx = 0;
			gdo = ryp0 * gviewxrange;
			if ((globalorientation & 4) == 0) {
				gux = (float) tsizx * gdo
						/ (dsprite[1].px  - dsprite[0].px  + .002);
				guo = -gux * (dsprite[0].px  - .001);
			} else {
				gux = (float) tsizx * gdo
						/ (dsprite[0].px  - dsprite[1].px  - .002);
				guo = -gux * (dsprite[1].px  + .001);
			}
			if ((globalorientation & 8) == 0) {
				gvy = (float) tsizy * gdo / (dsprite[3].py - dsprite[0].py + .002);
				gvo = -gvy * (dsprite[0].py - .001);
			} else {
				gvy = (float) tsizy * gdo / (dsprite[0].py - dsprite[3].py - .002);
				gvo = -gvy * (dsprite[3].py + .001);
			}

			// sprite panning
			if (spriteext[spritenum].xpanning != 0) {
				guy -= gdy * ((float) (spriteext[spritenum].xpanning) / 255.f)
						* tsizx;
				guo -= gdo * ((float) (spriteext[spritenum].xpanning) / 255.f)
						* tsizx;
				srepeat = 1;
			}
			if (spriteext[spritenum].ypanning != 0) {
				gvy -= gdy * ((float) (spriteext[spritenum].ypanning) / 255.f)
						* tsizy;
				gvo -= gdo * ((float) (spriteext[spritenum].ypanning) / 255.f)
						* tsizy;
				trepeat = 1;
			}

			// Clip sprites to ceilings/floors when no parallaxing and not
			// sloped
			if ((sector[tspr.sectnum].ceilingstat & 3) == 0) {
				sy0 = (float) (((sector[tspr.sectnum].ceilingz - globalposz))
						* gyxscale * ryp0 + ghoriz);
				if (dsprite[0].py < sy0)
					dsprite[0].py = dsprite[1].py = sy0;
			}
			if ((sector[tspr.sectnum].floorstat & 3) == 0) {
				sy0 = (float) (((sector[tspr.sectnum].floorz - globalposz))
						* gyxscale * ryp0 + ghoriz);
				if (dsprite[2].py > sy0)
					dsprite[2].py = dsprite[3].py = sy0;
			}

			tilesizx[globalpicnum] = (short) tsizx;
			tilesizy[globalpicnum] = (short) tsizy;

			gl.glDepthRange(defznear, defzfar-(10f / (dist + 1)));
	
			pow2xsplit = 0;
			drawpoly(dsprite, 4, method);

			gl.glDepthRange(defznear, defzfar);

			srepeat = 0;
			trepeat = 0;
			break;
	
		case 1: // Wall sprite

			// Project 3D to 2D
			if ((globalorientation & 4) != 0)
				xoff = -xoff;
			if ((globalorientation & 8) != 0)
				yoff = -yoff;

			posx += dcoord[tspr.owner].x;
			posy += dcoord[tspr.owner].y;

			xv = (float) tspr.xrepeat * ((float) (sintable[(tspr.ang) & 2047] * (1.0f / 65536.f)) - dsin[tspr.owner].x);
			yv = (float) tspr.xrepeat * ((float) (sintable[(tspr.ang + 1536) & 2047] * (1.0f / 65536.f)) - dsin[tspr.owner].y);

			f = (float) (tsizx >> 1) + (float) xoff;
			x0 = (float) (posx - globalposx) - xv * f;
			x1 = xv * (float) tsizx + x0;
			y0 = (float) (posy - globalposy) - yv * f;
			y1 = yv * (float) tsizx + y0;

			yp0 = (float) (x0 * gcosang2 + y0 * gsinang2);
			yp1 = (float) (x1 * gcosang2 + y1 * gsinang2);
			if ((yp0 <= SCISDIST) && (yp1 <= SCISDIST))
				return;
			xp0 = (float) (y0 * gcosang - x0 * gsinang);
			xp1 = (float) (y1 * gcosang - x1 * gsinang);

			// Clip to close parallel-screen plane
			oxp0 = xp0;
			oyp0 = yp0;
			if (yp0 < SCISDIST) {
				t0 = (float) ((SCISDIST - yp0) / (yp1 - yp0));
				xp0 = (xp1 - xp0) * t0 + xp0;
				yp0 = (float) SCISDIST;
			} else {
				t0 = 0.f;
			}
			if (yp1 < SCISDIST) {
				t1 = (float) ((SCISDIST - oyp0) / (yp1 - oyp0));
				xp1 = (xp1 - oxp0) * t1 + oxp0;
				yp1 = (float) SCISDIST;
			} else {
				t1 = 1.f;
			}

			f = ((float) tspr.yrepeat) * (float) tsizy * 4;

			ryp0 = 1.0f / yp0;
			ryp1 = 1.0f / yp1;
			sx0 = (float) (ghalfx * xp0 * ryp0 + ghalfx);
			sx1 = (float) (ghalfx * xp1 * ryp1 + ghalfx);
			ryp0 *= gyxscale;
			ryp1 *= gyxscale;

			tspr.z -= ((yoff * tspr.yrepeat) << 2);
			if ((globalorientation & 128) != 0) {
				tspr.z += ((tsizy * tspr.yrepeat) << 1);
				if ((tsizy & 1) != 0)
					tspr.z += (tspr.yrepeat << 1); // Odd yspans
			}

			sc0 = (float) (((tspr.z - globalposz - f)) * ryp0 + ghoriz);
			sc1 = (float) (((tspr.z - globalposz - f)) * ryp1 + ghoriz);
			sf0 = (float) (((tspr.z - globalposz)) * ryp0 + ghoriz);
			sf1 = (float) (((tspr.z - globalposz)) * ryp1 + ghoriz);

			gdx = (ryp0 - ryp1) * gxyaspect / (sx0 - sx1);
			gdy = 0;
			gdo = ryp0 * gxyaspect - gdx * sx0;

			if ((globalorientation & 4) != 0) {
				t0 = 1.f - t0;
				t1 = 1.f - t1;
			}

			// sprite panning
			if (spriteext[spritenum].xpanning != 0) {
				t0 -= ((float) (spriteext[spritenum].xpanning) / 255.f);
				t1 -= ((float) (spriteext[spritenum].xpanning) / 255.f);
				srepeat = 1;
			}
			gux = (t0 * ryp0 - t1 * ryp1) * gxyaspect * (float) tsizx
					/ (sx0 - sx1);
			guy = 0;
			guo = t0 * ryp0 * gxyaspect * (float) tsizx - gux * sx0;

			f = (float) ((tsizy) * (gdx * sx0 + gdo) / ((sx0 - sx1) * (sc0 - sf0)));
			if ((globalorientation & 8) == 0) {
				gvx = (sc0 - sc1) * f;
				gvy = (sx1 - sx0) * f;
				gvo = -gvx * sx0 - gvy * sc0;
			} else {
				gvx = (sf1 - sf0) * f;
				gvy = (sx0 - sx1) * f;
				gvo = -gvx * sx0 - gvy * sf0;
			}

			// sprite panning
			if (spriteext[spritenum].ypanning != 0) {
				gvx -= gdx * ((float) (spriteext[spritenum].ypanning) / 255.f)
						* tsizy;
				gvy -= gdy * ((float) (spriteext[spritenum].ypanning) / 255.f)
						* tsizy;
				gvo -= gdo * ((float) (spriteext[spritenum].ypanning) / 255.f)
						* tsizy;
				trepeat = 1;
			}

			// Clip sprites to ceilings/floors when no parallaxing
			if (tspr.sectnum != -1 && (sector[tspr.sectnum].ceilingstat & 1) == 0) {
				f = ((float) tspr.yrepeat) * (float) tsizy * 4;
				if (sector[tspr.sectnum].ceilingz > tspr.z - f) {
					sc0 = (float) (((sector[tspr.sectnum].ceilingz - globalposz))
							* ryp0 + ghoriz);
					sc1 = (float) (((sector[tspr.sectnum].ceilingz - globalposz))
							* ryp1 + ghoriz);
				}
			}
			if (tspr.sectnum != -1 && (sector[tspr.sectnum].floorstat & 1) == 0) {
				if (sector[tspr.sectnum].floorz < tspr.z) {
					sf0 = (float) (((sector[tspr.sectnum].floorz - globalposz))
							* ryp0 + ghoriz);
					sf1 = (float) (((sector[tspr.sectnum].floorz - globalposz))
							* ryp1 + ghoriz);
				}
			}

			if (sx0 > sx1) {
				if ((globalorientation & 64) != 0)
					return; // 1-sided sprite
				f = sx0;
				sx0 = sx1;
				sx1 = f;
				f = sc0;
				sc0 = sc1;
				sc1 = f;
				f = sf0;
				sf0 = sf1;
				sf1 = f;
			}

			dsprite[0].px  = sx0;
			dsprite[0].py = sc0;
			dsprite[1].px  = sx1;
			dsprite[1].py = sc1;
			dsprite[2].px  = sx1;
			dsprite[2].py = sf1;
			dsprite[3].px  = sx0;
			dsprite[3].py = sf0;

			tilesizx[globalpicnum] = (short) tsizx;
			tilesizy[globalpicnum] = (short) tsizy;

			if(spritewall[tspr.owner] != -1 && (tspr.cstat & 2) != 0)
				gl.glDepthMask(false);
			
			dist = engine.qdist(globalposx - tspr.x, globalposy - tspr.y);
			dist *= (dist >> 7);
			
			if(spritewall[tspr.owner] != -1 && dist > 0)
				gl.glDepthRange(defznear, defzfar-(Math.min(dist / 16384f, 40) / dist));

			curpolygonoffset += 0.01f;
			gl.glPolygonOffset(-curpolygonoffset, -curpolygonoffset);

			pow2xsplit = 0;
			drawpoly(dsprite, 4, method);

			gl.glPolygonOffset(0,0);
			gl.glDepthRange(defznear, defzfar);
			if(spritewall[tspr.owner] != -1 && (tspr.cstat & 2) != 0)
				gl.glDepthMask(true);
			
			srepeat = 0;
			trepeat = 0;
			
			break;
		
		case 2: // Floor sprite

			if ((globalorientation & 64) != 0)
				if ((globalposz > tspr.z) == ((globalorientation & 8) == 0))
					return;
			if ((globalorientation & 4) > 0)
				xoff = -xoff;
			if ((globalorientation & 8) > 0)
				yoff = -yoff;

			if (tspr.z < sector[tspr.sectnum].ceilingz)
				tspr.z += ((tspr.owner) & 31);
			if (tspr.z > sector[tspr.sectnum].floorz)
				tspr.z -= ((tspr.owner) & 31);

			i = (tspr.ang & 2047);
			c = (float) (sintable[(i + 512) & 2047] / 65536.0);
			s = (float) (sintable[i] / 65536.0);
			x0 = (float) ((tsizx >> 1) - xoff) * tspr.xrepeat;
			y0 = (float) ((tsizy >> 1) - yoff) * tspr.yrepeat;
			x1 = (float) ((tsizx >> 1) + xoff) * tspr.xrepeat;
			y1 = (float) ((tsizy >> 1) + yoff) * tspr.yrepeat;

			// Project 3D to 2D
			for (j = 0; j < 4; j++) {
				sx0 = (float) (tspr.x - globalposx);
				sy0 = (float) (tspr.y - globalposy);
				if (((j + 0) & 2) != 0) {
					sy0 -= s * y0;
					sx0 -= c * y0;
				} else {
					sy0 += s * y1;
					sx0 += c * y1;
				}
				if (((j + 1) & 2) != 0) {
					sx0 -= s * x0;
					sy0 += c * x0;
				} else {
					sx0 += s * x1;
					sy0 -= c * x1;
				}

				dsprite[j].px = sy0 * gcosang - sx0 * gsinang;
				dsprite[j].py = sx0 * gcosang2 + sy0 * gsinang2;
			}

			if (tspr.z < globalposz) // if floor sprite is above you, reverse order of points
			{
				f = (float) dsprite[0].px ;
				dsprite[0].px  = dsprite[1].px ;
				dsprite[1].px  = f;
				f = (float) dsprite[0].py;
				dsprite[0].py = dsprite[1].py;
				dsprite[1].py = f;
				f = (float) dsprite[2].px ;
				dsprite[2].px  = dsprite[3].px ;
				dsprite[3].px  = f;
				f = (float) dsprite[2].py;
				dsprite[2].py = dsprite[3].py;
				dsprite[3].py = f;
			}

			// Clip to SCISDIST plane
			npoints = 0;
			for (i = 0; i < 4; i++) {
				j = ((i + 1) & 3);
				if (dsprite[i].py >= SCISDIST) {
					dsprite[npoints].px2 = dsprite[i].px;
					dsprite[npoints].py2 = dsprite[i].py;
					npoints++;
				}
				if ((dsprite[i].py >= SCISDIST) != (dsprite[j].py >= SCISDIST)) {
					f = (float) ((SCISDIST - dsprite[i].py) / (dsprite[j].py - dsprite[i].py));
					dsprite[npoints].px2 = (float) ((dsprite[j].px - dsprite[i].px) * f + dsprite[i].px);
					dsprite[npoints].py2 = (float) ((dsprite[j].py - dsprite[i].py) * f + dsprite[i].py);
					npoints++;
				}
			}

			if (npoints < 3)
				return;

			// Project rotated 3D points to screen
			f = (float) ((tspr.z - globalposz)*gyxscale);
			for (j = 0; j < npoints; j++) {
				ryp0 = (float) (1.0 / dsprite[j].py2);
				dsprite[j].px = ghalfx * dsprite[j].px2 * ryp0 + ghalfx;
				dsprite[j].py = f * ryp0 + ghoriz;
			}

			// gd? Copied from floor rendering code
			gdx = 0;
			gdy = gxyaspect / (double) (tspr.z - globalposz);
			gdo = -ghoriz * gdy;
			// copied&modified from relative alignment
			xv = (float) tspr.x + s * x1 + c * y1;
			fx = (float) -(x0 + x1) * s;
			yv = (float) tspr.y + s * y1 - c * x1;
			fy = (float) +(x0 + x1) * c;
			f = (float) (1.0f / sqrt(fx * fx + fy * fy));
			fx *= f;
			fy *= f;
			drawsprite_ft[2] = singlobalang * fy + cosglobalang * fx;
			drawsprite_ft[3] = singlobalang * fx - cosglobalang * fy;
			drawsprite_ft[0] = ((float) (globalposy - yv)) * fy + ((float) (globalposx - xv)) * fx;
			drawsprite_ft[1] = ((float) (globalposx - xv)) * fy - ((float) (globalposy - yv)) * fx;
			gux = (double) drawsprite_ft[3] * ((double) viewingrange) / (-65536.0 * 262144.0);
			gvx = (double) drawsprite_ft[2] * ((double) viewingrange) / (-65536.0 * 262144.0);
			guy = (double) drawsprite_ft[0] * gdy;
			gvy = (double) drawsprite_ft[1] * gdy;
			guo = (double) drawsprite_ft[0] * gdo;
			gvo = (double) drawsprite_ft[1] * gdo;
			guo += (double) (drawsprite_ft[2] / 262144.0 - gux) * ghalfx;
			gvo -= (double) (drawsprite_ft[3] / 262144.0 + gvx) * ghalfx;
			f = 4.0f / (float) tspr.xrepeat;
			gux *= f;
			guy *= f;
			guo *= f;
			f = -4.0f / (float) tspr.yrepeat;
			gvx *= f;
			gvy *= f;
			gvo *= f;
			if ((globalorientation & 4) != 0) {
				gux = ((float) tsizx) * gdx - gux;
				guy = ((float) tsizx) * gdy - guy;
				guo = ((float) tsizx) * gdo - guo;
			}

			// sprite panning
			if (spriteext[spritenum].xpanning != 0) {
				guy -= gdy * ((float) (spriteext[spritenum].xpanning) / 255.f) * tsizx;
				guo -= gdo * ((float) (spriteext[spritenum].xpanning) / 255.f) * tsizx;
				srepeat = 1;
			}
			if (spriteext[spritenum].ypanning != 0) {
				gvy -= gdy * ((float) (spriteext[spritenum].ypanning) / 255.f) * tsizy;
				gvo -= gdo * ((float) (spriteext[spritenum].ypanning) / 255.f) * tsizy;
				trepeat = 1;
			}

			tilesizx[globalpicnum] = (short) tsizx;
			tilesizy[globalpicnum] = (short) tsizy;
			
			if((tspr.cstat & 2) != 0)
				gl.glDepthMask(false);
			
			gl.glDepthRange(defznear+0.000001, defzfar-0.00001);

			curpolygonoffset += 0.01f;
			gl.glPolygonOffset(-curpolygonoffset, -curpolygonoffset);
			
			pow2xsplit = 0;
			drawpoly(dsprite, npoints, method);
			
			gl.glDepthRange(defznear, defzfar);
			
			if((tspr.cstat & 2) != 0)
				gl.glDepthMask(true);

			srepeat = 0;
			trepeat = 0;

			break;
		case 3: // Voxel sprite
			break;
		}

		tilesizx[globalpicnum] = (short) oldsizx;
		tilesizy[globalpicnum] = (short) oldsizy;

		if (automapping == 1)
			show2dsprite[snum >> 3] |= pow2char[snum & 7];
	}

	private void EnableFog()
	{
		if (!nofog)
			gl.glEnable(GL_FOG);
	}

	@Override
	public void palfade(HashMap<String, FadeEffect> fades) {

		gl.glMatrixMode(GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glDisable(GL_TEXTURE_2D);
		gl.glDisable(GL_FOG);

		gl.glEnable(GL_BLEND);

		palfadergb.draw(gl);
		if(fades != null)
		{
			Iterator<FadeEffect> it = fades.values().iterator();
		    while(it.hasNext()) {
		    	FadeEffect obj = (FadeEffect)it.next();
		    	obj.draw(gl);
		    }
		}

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void precache(int dapicnum, int dapalnum, int datype) {
		// dapicnum and dapalnum are like you'd expect
		// datype is 0 for a wall/floor/ceiling and 1 for a sprite
		// basically this just means walls are repeating
		// while sprites are clamped

		if ((palookup[dapalnum] == null)
				&& (dapalnum < (MAXPALOOKUPS - RESERVEDPALS)))
			return;

//		Console.Println("precached " + dapicnum + " " + dapalnum + " type " + datype);
		textureCache.cache(dapicnum, dapalnum, (short) 0, clampingMode((datype & 1) << 2), false);

		if (datype == 0 || defs == null)
			return;
		
		if(textureCache.isUseShader() && usevoxels)
		{
			VOXModel vox = defs.mdInfo.getVoxel(dapicnum);
			if(vox != null) vox.loadskin(dapalnum, true);
		}
		
		if(usemodels) {
			MDModel m = (MDModel) defs.mdInfo.getModel(dapicnum);
	        if(m != null) {
	        	if(m.mdnum == 3) {
	        		int numsurfs = ((MD3Model) m).head.numSurfaces;
	        		for (int surfi = 0, skinnum; surfi < numsurfs; surfi++)
	        	    {
	        	    	skinnum = defs.mdInfo.getParams(dapicnum).skinnum;
	        			m.loadskin(defs, skinnum,dapalnum,surfi);
	        	    }
	        	} else m.loadskin(defs, 0, dapalnum, 0);
	        }
		}
	}

	private void calc_and_apply_fog(int shade, int vis, int pal)
	{
		globalfog.shade = shade;
		globalfog.combvis = globalvisibility * ((vis+16) & 0xFF);
		globalfog.pal = pal;
		if(surfaceType == 0) 
			globalfog.apply();
	}
	
	private void calc_and_apply_skyfog(int shade, int vis, int pal)
	{
		globalfog.shade = shade;
		globalfog.combvis = 0;
		globalfog.pal = pal;
		if(surfaceType == 0) 
			globalfog.apply();
	}

	public static void equation(Vector3 ret, float x1, float y1, float x2, float y2)
	{
	    if ((x2 - x1) != 0)
	    {
	        ret.x = (float)(y2 - y1)/(float)(x2 - x1);
	        ret.y = -1;
	        ret.z = (y1 - (ret.x * x1));
	    }
	    else // vertical
	    {
	        ret.x = 1;
	        ret.y = 0;
	        ret.z = -x1;
	    }
	}

	public boolean sameside(Vector3 eq, Vector2 p1, Vector2 p2) {
		float sign1, sign2;

		sign1 = eq.x * p1.x + eq.y * p1.y + eq.z;
		sign2 = eq.x * p2.x + eq.y * p2.y + eq.z;

		sign1 = sign1 * sign2;
		if (sign1 > 0) {
			// OSD_Printf("SAME SIDE !\n");
			return true;
		}
		// OSD_Printf("OPPOSITE SIDE !\n");
		return false;
	}
	
	private void drawsurfaces(GLSurfaceArray surfaces)
	{
		if(surfaceType != 1) return;
		
		//TODO:
		//	MultiTextures
		//	glTexParameteri
		//	glAlphaFunc
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL_COLOR_ARRAY);
		
		Pthtyp p = null;
		textureCache.bindShader();
		for(int i = 0; i < surfaces.size; i++)
		{
			GLSurface s = surfaces.get(i);
			if(!s.buffer.hasRemaining()) continue;

			if(s.pth != p) {
				textureCache.setShaderParams(s.pth.palnum, engine.getpalookup(globalvisibility, s.shade & 0xFF));
				bindTexture(s.pth.glpic);
			}
			p = s.pth;
			
			s.fog.apply();

			gl.glVertexPointer(3, GL_FLOAT, 4 * 9, s.buffer.position(0));
			gl.glTexCoordPointer(2, GL_FLOAT, 4 * 9, s.buffer.position(3));
			gl.glColorPointer(4, GL_FLOAT, 4 * 9, s.buffer.position(5));
			gl.glDrawArrays(GL_TRIANGLE_FAN, 0, s.numvertices);
		}
		textureCache.unbindShader();
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL_COLOR_ARRAY);
	}

	public void swapsprite(int k, int l, boolean z)
	{
		SPRITE stmp = tspriteptr[k];
		tspriteptr[k] = tspriteptr[l];
		tspriteptr[l] = stmp;

		int tmp = spritesx[k];
		spritesx[k] = spritesx[l];
		spritesx[l] = tmp;
		tmp = spritesy[k];
		spritesy[k] = spritesy[l];
		spritesy[l] = tmp;

		if(z) {
			tmp = spritesz[k];
			spritesz[k] = spritesz[l];
			spritesz[l] = tmp;
		}
	}
	
	// PLAG: sorting stuff
	private static Vector3 drawmasks_maskeq = new Vector3(), drawmasks_p1eq = new Vector3(), drawmasks_p2eq = new Vector3();
	private static final Vector2 drawmasks_dot = new Vector2(), drawmasks_dot2 = new Vector2(), drawmasks_middle = new Vector2(), drawmasks_pos = new Vector2(),
			drawmasks_spr = new Vector2();

	@Override
	public void drawmasks() {
		int i, j, k, l, gap, xs, ys, xp, yp, yoff, yspan;
		boolean modelp = false;

		for (i = spritesortcnt - 1; i >= 0; i--) {
			tspriteptr[i] = tsprite[i];
			if(tspriteptr[i].picnum < 0 || tspriteptr[i].picnum > MAXTILES) continue;
			xs = tspriteptr[i].x - globalposx;
			ys = tspriteptr[i].y - globalposy;
			yp = dmulscale(xs, cosviewingrangeglobalang, ys, sinviewingrangeglobalang, 6);

			modelp = (usemodels && defs != null && defs.mdInfo.getModel(tspriteptr[i].picnum) != null);

			if (yp > (4 << 8)) {
				xp = dmulscale(ys, cosglobalang, -xs, singlobalang, 6);
				if (mulscale(abs(xp + yp), xdimen, 24) >= yp) {
					spritesortcnt--; // Delete face sprite if on wrong side!
					if (i == spritesortcnt)
						continue;
					tspriteptr[i] = tspriteptr[spritesortcnt];
					spritesx[i] = spritesx[spritesortcnt];
					spritesy[i] = spritesy[spritesortcnt];
					continue;
				}
				spritesx[i] = scale(xp + yp, xdimen << 7, yp);
			} else if ((tspriteptr[i].cstat & 48) == 0) {
				if (!modelp) {
					spritesortcnt--; // Delete face sprite if on wrong side!
					if (i == spritesortcnt)
						continue;
					tspriteptr[i] = tspriteptr[spritesortcnt];
					spritesx[i] = spritesx[spritesortcnt];
					spritesy[i] = spritesy[spritesortcnt];
					continue;
				}
			}
			spritesy[i] = yp;
		}

		gap = 1;
		while (gap < spritesortcnt)
			gap = (gap << 1) + 1;
		for (gap >>= 1; gap > 0; gap >>= 1)
			// Sort sprite list
			for (i = 0; i < spritesortcnt - gap; i++)
				for (l = i; l >= 0; l -= gap) {
					if (spritesy[l] <= spritesy[l + gap])
						break;
					swapsprite(l, l+gap, false);
				}
		if (spritesortcnt > 0)
			spritesy[spritesortcnt] = (spritesy[spritesortcnt - 1] ^ 1);

		ys = spritesy[0];
		i = 0;
		for (j = 1; j <= spritesortcnt; j++) {
			if (spritesy[j] == ys)
				continue;
			ys = spritesy[j];
			if (j > i + 1) {
				for (k = i; k < j; k++) {
					spritesz[k] = tspriteptr[k].z;
					if(tspriteptr[k].picnum < 0 || tspriteptr[k].picnum > MAXTILES) continue;
					if ((tspriteptr[k].cstat & 48) != 32) {
						yoff = ((picanm[tspriteptr[k].picnum] >> 16) & 255) + tspriteptr[k].yoffset;
						spritesz[k] -= ((yoff * tspriteptr[k].yrepeat) << 2);
						yspan = (tilesizy[tspriteptr[k].picnum] * tspriteptr[k].yrepeat << 2);
						if ((tspriteptr[k].cstat & 128) == 0)
							spritesz[k] -= (yspan >> 1);
						if (klabs(spritesz[k] - globalposz) < (yspan >> 1))
							spritesz[k] = globalposz;
					}
				}
				for (k = i + 1; k < j; k++)
					for (l = i; l < k; l++)
						if (klabs(spritesz[k] - globalposz) < klabs(spritesz[l] - globalposz)) 
							swapsprite(k, l, true);
				for (k = i + 1; k < j; k++)
					for (l = i; l < k; l++) {
						if (tspriteptr[k].statnum < tspriteptr[l].statnum) 
							swapsprite(k, l, false);
						if((tspriteptr[k].cstat & 2) != 0) //transparent sort
							swapsprite(k, l, true);
					}
			}
			i = j;
		}

		curpolygonoffset = 0;

		drawmasks_pos.x = (float) globalposx;
		drawmasks_pos.y = (float) globalposy;
		
		gl.glEnable(GL10.GL_POLYGON_OFFSET_FILL);
		
		surfaces.clear();

		while (maskwallcnt != 0) {

			maskwallcnt--;

			drawmasks_dot.x = (float) wall[thewall[maskwall[maskwallcnt]]].x;
			drawmasks_dot.y = (float) wall[thewall[maskwall[maskwallcnt]]].y;
			drawmasks_dot2.x = (float) wall[wall[thewall[maskwall[maskwallcnt]]].point2].x;
			drawmasks_dot2.y = (float) wall[wall[thewall[maskwall[maskwallcnt]]].point2].y;

			equation(drawmasks_maskeq, drawmasks_dot.x, drawmasks_dot.y, drawmasks_dot2.x, drawmasks_dot2.y);
            equation(drawmasks_p1eq, drawmasks_pos.x, drawmasks_pos.y, drawmasks_dot.x, drawmasks_dot.y);
            equation(drawmasks_p2eq, drawmasks_pos.x, drawmasks_pos.y, drawmasks_dot2.x, drawmasks_dot2.y);
            
			drawmasks_middle.x = (drawmasks_dot.x + drawmasks_dot2.x) / 2;
			drawmasks_middle.y = (drawmasks_dot.y + drawmasks_dot2.y) / 2;

			i = spritesortcnt;
			while (i != 0) {
				i--;
				if (tspriteptr[i] != null) {
					drawmasks_spr.x = (float) tspriteptr[i].x;
					drawmasks_spr.y = (float) tspriteptr[i].y;

					if (!sameside(drawmasks_maskeq, drawmasks_spr, drawmasks_pos) && sameside(drawmasks_p1eq, drawmasks_middle, drawmasks_spr)
							&& sameside(drawmasks_p2eq, drawmasks_middle, drawmasks_spr)) {
						drawsprite(i);
						tspriteptr[i] = null;
					}
				}
			}
			drawmaskwall(maskwallcnt);
		}

		while (spritesortcnt != 0) {
			spritesortcnt--;
			if (tspriteptr[spritesortcnt] != null) {
				drawsprite(spritesortcnt);
			}
		}
		
		gl.glDisable(GL10.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(0, 0);

		drawsurfaces(surfaces);

		if (totalclock < lastcullcheck - CULL_DELAY)
			lastcullcheck = totalclock;
		if (totalclock >= lastcullcheck + CULL_DELAY)
			lastcullcheck = (totalclock + CULL_DELAY);

		if(drunk)
		{
			if(frameTexture == null || framew != xdim || frameh != ydim)
			{
				if(frameTexture != null) frameTexture.dispose();
				frameTexture = new BTexture();
				bindTexture(frameTexture);
				for (framesize = 1; framesize < Math.max(xdim, ydim); framesize *= 2);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL10.GL_RGB, framesize, framesize, 0, GL10.GL_RGB, GL_UNSIGNED_BYTE, null);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				framew = xdim; frameh = ydim;
			}
			
			gl.glReadBuffer(GL_BACK);
			bindTexture(frameTexture);
			gl.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, framesize, framesize);


			gl.glDisable(GL_DEPTH_TEST);
			gl.glDisable(GL_ALPHA_TEST);
			gl.glEnable(GL_TEXTURE_2D);
			bindTexture(frameTexture);

		
			gl.glMatrixMode(GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			
			float tilt = (drunkIntensive * 360) / 2048f;
			tilt = min(max(tilt, -MAXDRUNKANGLE), MAXDRUNKANGLE);

			gl.glScalef(0.95f, 1, 1);
			gl.glRotatef(tilt, 0, 0, 1.0f);

			gl.glMatrixMode(GL_MODELVIEW);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			
			float u = (float)xdim / framesize;
			float v = (float)ydim / framesize;

			gl.glColor4f(1, 1, 1, 0.5f);
			gl.glBegin(GL10.GL_TRIANGLE_FAN);
			gl.glTexCoord2f(0, 0);
			gl.glVertex2f( -1f, -1f );
			 
			gl.glTexCoord2f(0, v);
			gl.glVertex2f( -1f, 1f );
			 
			gl.glTexCoord2f(u, v);
			gl.glVertex2f(1f, 1f );
			 
			gl.glTexCoord2f(u, 0);
			gl.glVertex2f( 1f, -1f);
			gl.glEnd();
			

			gl.glMatrixMode(GL_MODELVIEW);
			gl.glPopMatrix();
			gl.glMatrixMode(GL_PROJECTION);
			gl.glPopMatrix();
			
			gl.glEnable(GL_DEPTH_TEST);
			gl.glEnable(GL_ALPHA_TEST);
			gl.glDisable(GL_TEXTURE_2D);
		}
	}

	protected int getclipmask(int a, int b, int c, int d) { // Ken did this
		int bA = a < 0 ? 1 : 0;
		int bB = b < 0 ? 1 : 0;
		int bC = c < 0 ? 1 : 0;
		int bD = d < 0 ? 1 : 0;

		d = (bA * 8) + (bB * 4) + (bC * 2) + bD;
		return (((d << 4) ^ 0xf0) | d);
	}

	public void md3_vox_calcmat_common(SPRITE tspr, Vector3 a0, float f, float[][] mat) {
		float g;
		float k0, k1, k2, k3, k4, k5, k6, k7;

		k0 = ((float) (tspr.x - globalposx)) * f / 1024.0f;
		k1 = ((float) (tspr.y - globalposy)) * f / 1024.0f;
		
		f = (float) (gcosang2 * gshang);
		g = (float) (gsinang2 * gshang);
		k4 = (float) sintable[(tspr.ang + spriteext[tspr.owner].angoff + 1024) & 2047] / 16384.0f;
		k5 = (float) sintable[(tspr.ang + spriteext[tspr.owner].angoff + 512) & 2047] / 16384.0f;
		k2 = k0 * (1 - k4) + k1 * k5;
		k3 = k1 * (1 - k4) - k0 * k5;
		k6 = (float) (f * gstang - gsinang * gctang);
		k7 = (float) (g * gstang + gcosang * gctang);
		
		//Mirrors
		if (grhalfxdown10x < 0) { 
			k6 = -k6;
			k7 = -k7;
		}
		
		mat[0][0] = k4 * k6 + k5 * k7;
		mat[1][0] = (float) (gchang * gstang);
		mat[2][0] = k4 * k7 - k5 * k6;
		mat[3][0] = k2 * k6 + k3 * k7;
		k6 = (float) (f * gctang + gsinang * gstang);
		k7 = (float) (g * gctang - gcosang * gstang);
		mat[0][1] = k4 * k6 + k5 * k7;
		mat[1][1] = (float) (gchang * gctang);
		mat[2][1] = k4 * k7 - k5 * k6;
		mat[3][1] = k2 * k6 + k3 * k7;
		k6 = (float) (gcosang2 * gchang);
		k7 = (float) (gsinang2 * gchang);
		mat[0][2] = k4 * k6 + k5 * k7;
		mat[1][2] = (float) -gshang;
		mat[2][2] = k4 * k7 - k5 * k6;
		mat[3][2] = k2 * k6 + k3 * k7;

		mat[3][0] += a0.y * mat[0][0] + a0.z * mat[1][0] + a0.x * mat[2][0];
		mat[3][1] += a0.y * mat[0][1] + a0.z * mat[1][1] + a0.x * mat[2][1];
		mat[3][2] += a0.y * mat[0][2] + a0.z * mat[1][2] + a0.x * mat[2][2];
	}
	
	public void md3_vox_calcmat_common(SPRITE tspr, Vector3 a0, float f, Matrix4 mat) {
		float yaw = globalang / (2048.0f / 360.0f) - 90.0f;
		float roll = gtang * 57.3f; //XXX 57.3f WFT
		float spriteang = ((tspr.ang + spriteext[tspr.owner].angoff + 512) & 2047) / (2048.0f / 360.0f);
		
		//gtang    tilt rotation (roll)
		//gstang = sin(qtang)
		//gctang = cos(qtang)
		
		//gchang   up/down rotation (pitch)
		//gshang   up/down rotation 
		
		/*
		double radplayerang = (globalang & 2047) * 2.0f * PI / 2048.0f; (yaw)
		gsinang = (float) (Math.cos(radplayerang) / 16.0);
		gcosang = (float) (Math.sin(radplayerang) / 16.0);
		gsinang2 = gsinang * ((double) viewingrange) / 65536.0;
		gcosang2 = gcosang * ((double) viewingrange) / 65536.0;
		*/

		mat.idt();
        mat.rotate(0.0f, 0.0f, -1.0f, roll); 
		mat.rotate(-1.0f, 0.0f, 0.0f, pitch);
		mat.rotate(0.0f, -1.0f, 0.0f, yaw);
		mat.scale(-1 / 16f, 1.0f, 1 / 16f);
		mat.translate(a0.y, a0.z, a0.x);
		mat.rotate(0.0f, -1.0f, 0.0f, spriteang);

		/*
		float[] tmp = m.getValues();
		mat[0][0] = tmp[0];
		mat[0][1] = tmp[1];
		mat[0][2] = tmp[2];
		mat[0][3] = tmp[3];
		
		mat[1][0] = tmp[4];
		mat[1][1] = tmp[5];
		mat[1][2] = tmp[6];
		mat[1][3] = tmp[7];
		
		mat[2][0] = tmp[8];
		mat[2][1] = tmp[9];
		mat[2][2] = tmp[10];
		mat[2][3] = tmp[11];
		
		mat[3][0] = tmp[12];
		mat[3][1] = tmp[13];
		mat[3][2] = tmp[14];
		mat[3][3] = tmp[15];
		*/
	}
	
	public void md3_vox_calcmat_common(SPRITE tspr, Vector3 a0) {
		float yaw = globalang / (2048.0f / 360.0f) - 90.0f;
		float roll = gtang * 57.3f; //XXX 57.3f WTF
		float spriteang = ((tspr.ang + spriteext[tspr.owner].angoff + 512) & 2047) / (2048.0f / 360.0f);

		gl.glLoadIdentity();
		gl.glRotatef(roll, 0, 0, -1);
		gl.glRotatef(pitch, -1, 0, 0);
		gl.glRotatef(yaw, 0, -1, 0);
		gl.glScalef(-1 / 16f, 1.0f, 1 / 16f);
		gl.glTranslatef(a0.y, a0.z, a0.x);
		gl.glRotatef(spriteang, 0.0f, -1.0f, 0.0f);
	}
	
	public int mddraw(SPRITE tspr, int xoff, int yoff)
	{
		Model vm = defs != null ? defs.mdInfo.getModel(tspr.picnum) : null;

		if(vm == null) return 0;

		if (vm.mdnum == 1) { return voxdraw((VOXModel) vm,tspr, xoff, yoff); }
	    if (vm.mdnum == 2) { return md2draw((MD2Model) vm, tspr, xoff, yoff); }
	    if (vm.mdnum == 3) { return md3draw((MD3Model) vm, tspr, xoff, yoff); }
	    return 0;
	}
	
	private Vector3 cScale = new Vector3();
	private Vector3 nScale = new Vector3();
	
	private void modelPrepare(MDModel m, SPRITE tspr, int xoff, int yoff)
	{
		float f = m.interpol; float g = 1-f;

		if(m.mdnum == 2)
			cScale.set(m.scale, m.scale, m.scale);
		else cScale.set(m.scale, -m.scale, m.scale);
		cScale.scl(g, g, g);
		
		if(m.mdnum == 2)
			nScale.set(m.scale, m.scale, m.scale);
		else nScale.set(m.scale, -m.scale, m.scale);
		nScale.scl(f, f, f);

    	modela0.x = modela0.y = 0; modela0.z = ((globalorientation & 8) != 0 ? -m.zadd : m.zadd) * m.scale;
    	float x0 = (float) tspr.x;
    	float k0 = (float) tspr.z;
    	if ( (globalorientation & 128) != 0 && (globalorientation&48) != 32 ) 
    		k0 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<1);

    	// Parkar: Changed to use the same method as centeroriented sprites
        if ((globalorientation & 8) != 0) //y-flipping
        {
        	yoff = -yoff;
            cScale.scl(1, -1, 1);
            nScale.scl(1, -1, 1);
            modela0.z = -modela0.z;
            k0 -= (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<2);
        }
        if ((globalorientation & 4) != 0) //x-flipping
        { 
        	xoff = -xoff;
        	cScale.scl(1, 1, -1);
            nScale.scl(1, 1, -1);
        	modela0.y = -modela0.y; 
        } 
        x0 += xoff * (tspr.xrepeat >> 2);
        k0 -= ((yoff * tspr.yrepeat) << 2);
		
        // yoffset differs from zadd in that it does not follow cstat&8 y-flipping
        modela0.z += m.yoffset * m.scale;

        f = ((float)tspr.xrepeat)/64*m.bscale;
        cScale.scl(-f, f, f);
        nScale.scl(-f, f, f);
        modela0.scl(f, -f, (tspr.yrepeat)/64.0f*m.bscale);

        // floor aligned
        float k1 = (float)tspr.y;
        if ((globalorientation&48)==32)
        {
        	cScale.scl(1, -1, -1);
            nScale.scl(1, -1, -1);
            modela0.z = -modela0.z;
            modela0.y = -modela0.y;
            f = modela0.x; modela0.x = modela0.z; modela0.z = f;
            k1 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)>>3);
        }
        
        f = (65536.0f*512.0f)/((float)(xdimen*viewingrange));
        g = (float) (32.0f/((float)(xdimen*gxyaspect)));
        cScale.scl(f, -f, g);
        nScale.scl(f, -f, g);
        
        modela0.y = (((float)(x0 	 -globalposx))/  1024.0f + modela0.y)*f;
        modela0.x = (((float)(k1     -globalposy))/ 1024.0f + modela0.x)*f;
        modela0.z = (((float)(k0     -globalposz))/ -16384.0f + modela0.z)*g;

//    	md3_vox_calcmat_common(tspr, dvoxa0);
        md3_vox_calcmat_common(tspr, modela0, f, matrix);

    	// floor aligned
        if ((globalorientation&48)==32)
        {
            f = matrix[1][0]; matrix[1][0] = matrix[2][0]*16.0f; matrix[2][0] = -f*(1.0f/16.0f);
            f = matrix[1][1]; matrix[1][1] = matrix[2][1]*16.0f; matrix[2][1] = -f*(1.0f/16.0f);
            f = matrix[1][2]; matrix[1][2] = matrix[2][2]*16.0f; matrix[2][2] = -f*(1.0f/16.0f);
        }

		matrix[0][3] = matrix[1][3] = matrix[2][3] = 0.f;
		matrix[3][3] = 1.f;
		
		gl.glMatrixMode(GL_MODELVIEW); // Let OpenGL (and perhaps hardware :) handle the matrix rotation
		gl.glLoadMatrixf(matrix);
		gl.glRotatef(-90, 0.0f, 1.0f, 0.0f);
		
		if((m.flags & MD_ROTATE)!= 0)
			gl.glRotatef(totalclock % 360, 0, 1, 0);

        if ((grhalfxdown10x >= 0) ^((globalorientation&8) != 0) ^((globalorientation&4) != 0)) 
        	gl.glFrontFace(GL_CW); else gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_CULL_FACE);
        if(m.mdnum == 2)
        	gl.glCullFace(GL_FRONT);
        else gl.glCullFace(GL_BACK);

        gl.glEnable(GL_TEXTURE_2D);

		polyColor.r = polyColor.g = polyColor.b = ((float)(numshades-min(max((globalshade * shadescale)+m.shadeoff,0),numshades)))/((float)numshades);

		if(defs != null) {
	        if ((m.flags&1) == 0 || (!(tspr.owner >= MAXSPRITES) && sector[sprite[tspr.owner].sectnum].floorpal!=0))
	        {
	        	Palette p = defs.texInfo.getTints(globalpal);
	            polyColor.r *= p.r / 255.0f;
	            polyColor.g *= p.g / 255.0f;
	            polyColor.b *= p.b / 255.0f;
	            
	            Palette pdetail = defs.texInfo.getTints(MAXPALOOKUPS-1);
	            if (pdetail.r != 255 || pdetail.g != 255 || pdetail.b != 255)
	            {
	                polyColor.r *= pdetail.r / 255.0f;
	                polyColor.g *= pdetail.g / 255.0f;
	                polyColor.b *= pdetail.b / 255.0f;
	            }
	        }
		}

	    if ((tspr.cstat&2) != 0) {
	    	if ((tspr.cstat&512) == 0) {
				polyColor.a = TRANSLUSCENT1;
	    	} else { 
				polyColor.a = TRANSLUSCENT2;
	    	}
	    } else polyColor.a = 1.0f;
	   
	    if (m.usesalpha) //Sprites with alpha in texture
	    {
	        float al = 0.0f;
	        if (alphahackarray[globalpicnum] != 0)
	            al=alphahackarray[globalpicnum];
	        gl.glEnable(GL_BLEND);
	        gl.glEnable(GL_ALPHA_TEST);
	        gl.glAlphaFunc(GL_GREATER,al);
	    }
	    else
	        if ((tspr.cstat&2) != 0) gl.glEnable(GL_BLEND);

	    gl.glColor4f(polyColor.r,polyColor.g,polyColor.b,polyColor.a);
	}

	private int md3draw(MD3Model m, SPRITE tspr, int xoff, int yoff)
	{
    	m.updateanimation(defs, tspr);

    	modelPrepare(m, tspr, xoff, yoff);
    	cScale.scl(1 / 64.0f);
    	nScale.scl(1 / 64.0f);

	    int rendered = 0, skinnum = -1;
	    for (int surfi = 0; surfi < m.head.numSurfaces; surfi++)
	    {
	    	MD3Surface s = m.surfaces[surfi];
	    	
	    	m.verticesBuffer.clear();
	    	for (int i = 0; i < s.numverts; i++)
	        {
				MD3Vertice v0 = s.xyzn[m.cframe * s.numverts + i];
                MD3Vertice v1 = s.xyzn[m.nframe * s.numverts + i];

				m.verticesBuffer.put(v0.x*cScale.x + v1.x*nScale.x);
				m.verticesBuffer.put(v0.z*cScale.z + v1.z*nScale.z);
				m.verticesBuffer.put(v0.y*cScale.y + v1.y*nScale.y);
	        }
	    	m.verticesBuffer.flip();

	    	skinnum = defs.mdInfo.getParams(tspr.picnum).skinnum;
			BTexture texid = m.loadskin(defs, skinnum,globalpal,surfi);
	        if (texid != null) {

		        bindTexture(texid);
		        
		        if ( Console.Geti("r_detailmapping") != 0 )
		        	texid = m.loadskin(defs, skinnum,DETAILPAL,surfi);
		        else
		        	texid = null;
		        
		        int texunits = GL_TEXTURE0;
		        
		        if (texid != null)
		        {
		        	texunits = setBoundTextureDetail(texid, texunits);
		            MDSkinmap sk = m.getSkin(DETAILPAL, skinnum, surfi);
		            if(sk != null) {
		            	float f = sk.param;
			            gl.glMatrixMode(GL_TEXTURE);
			            gl.glLoadIdentity();
			            gl.glScalef(f, f, 1.0f);
			            gl.glMatrixMode(GL_MODELVIEW);
		            }
		        }
		        
		        if (r_glowmapping != 0)
		        	texid = m.loadskin(defs, skinnum,GLOWPAL,surfi);
		        else
		        	texid = null;
		        
		        if (texid != null)
		        	texunits = setBoundTextureGlow(texid, texunits);

		        if(r_vertexarrays != 0)
		        {
		        	m.indicesBuffer.clear();
		        	for (int i = s.numtris - 1; i >= 0; i--)
		        		 for (int j = 0; j < 3; j++)
		        			 m.indicesBuffer.put((short) s.tris[i][j]);
		        	m.indicesBuffer.flip();
		        	
		        	int l = GL_TEXTURE0;
	                do
	                {
	                    gl.glClientActiveTexture(l++);
	                    gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	                    gl.glTexCoordPointer(2, GL_FLOAT, 0, s.uv);
	                } while (l <= texunits);
	                
	                gl.glEnableClientState(GL_VERTEX_ARRAY);
	                gl.glVertexPointer(3, GL_FLOAT, 0, m.verticesBuffer);
	                gl.glDrawElements(GL_TRIANGLES, 0, GL_UNSIGNED_SHORT, m.indicesBuffer);
		        }
		        else
		        {
			        gl.glBegin(GL_TRIANGLES);
		            for (int i = s.numtris - 1; i >= 0; i--)
		                for (int j = 0; j < 3; j++)
		                {
		                    int k = s.tris[i][j];
		                    if (texunits > GL_TEXTURE0)
		                    {
		                        int l = GL_TEXTURE0;
		                        while (l <= texunits)
		                            gl.glMultiTexCoord2d(l++, s.uv.get(2*k), s.uv.get(2*k+1));
		                    }
		                    else
		                        gl.glTexCoord2f(s.uv.get(2 * k), s.uv.get(2 * k + 1));

		                    float x = m.verticesBuffer.get(3 * k);
		                    float y = m.verticesBuffer.get(3 * k + 1);
		                    float z = m.verticesBuffer.get(3 * k + 2);

		                    gl.glVertex3d(x, y, z);
		                }
		            gl.glEnd();
		        }
	
		        while (texunits > GL_TEXTURE0)
		        {
		        	gl.glMatrixMode(GL_TEXTURE);
		            gl.glLoadIdentity();
		            gl.glMatrixMode(GL_MODELVIEW);
		            gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
		            gl.glDisable(GL_TEXTURE_2D);
		            if (r_vertexarrays != 0)
		            {
		                gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		                gl.glClientActiveTexture(texunits - 1);
		            }
		            gl.glActiveTexture(--texunits);
		        }
		        if (r_vertexarrays != 0) gl.glDisableClientState(GL_VERTEX_ARRAY);
		        rendered = 1;
		    } else break;
	    }

    	if (m.usesalpha) gl.glDisable(GL_ALPHA_TEST);
    	gl.glDisable(GL_CULL_FACE);
        gl.glLoadIdentity();

		return rendered;
	}
	
	private int md2draw(MD2Model m, SPRITE tspr, int xoff, int yoff)
	{
		m.updateanimation(defs, tspr);

		modelPrepare(m, tspr, xoff, yoff);

	    int rendered = 0, skinnum = defs.mdInfo.getParams(tspr.picnum).skinnum;
		BTexture texid = m.loadskin(defs, skinnum,globalpal,0);
        if (texid != null)
        {
	        bindTexture(texid);
	        if (Console.Geti("r_detailmapping") != 0)
	        	texid = m.loadskin(defs, skinnum,DETAILPAL,0);
	        else
	        	texid = null;
	        
	        int texunits = GL_TEXTURE0;
	        
	        if (texid != null)
	        {
	        	texunits = setBoundTextureDetail(texid, texunits);
	
	            MDSkinmap sk = m.getSkin(DETAILPAL, skinnum, 0);
	            if(sk != null) {
	            	float f = sk.param;
		            gl.glMatrixMode(GL_TEXTURE);
		            gl.glLoadIdentity();
		            gl.glScalef(f, f, 1.0f);
		            gl.glMatrixMode(GL_MODELVIEW);
	            }
	        }
	        
	        if (r_glowmapping != 0)
	        	texid = m.loadskin(defs,skinnum,GLOWPAL,0);
	        else
	        	texid = null;
	        
	        if (texid != null)
	        	texunits = setBoundTextureGlow(texid, texunits);
	        
	        MD2Frame cframe = m.frames[m.cframe], nframe = m.frames[m.nframe];

	        if(r_vertexarrays != 0)
	        {
	        	m.verticesBuffer.clear();
	        	for (int i = 0; i < m.tris.length; i++) //-60fps, but it's need for animation
	        		for( int j = 0; j < 3; j++) 
	        		{
	        			int idx = m.tris[i].vertices[j];
	        			float x = cframe.vertices[idx][0]*cScale.x + nframe.vertices[idx][0]*nScale.x;
	         			float y = cframe.vertices[idx][1]*cScale.y + nframe.vertices[idx][1]*nScale.y;
	         			float z = cframe.vertices[idx][2]*cScale.z + nframe.vertices[idx][2]*nScale.z;
	         			m.verticesBuffer.put(x);
	         			m.verticesBuffer.put(z);
	         			m.verticesBuffer.put(y);
	        		}
	        	m.verticesBuffer.flip();

	        	int l = GL_TEXTURE0;
                do
                {
                    gl.glClientActiveTexture(l++);
                    gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL_FLOAT, 0, m.uv);
                } while (l <= texunits);
                
                gl.glEnableClientState(GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL_FLOAT, 0, m.verticesBuffer);
                gl.glDrawElements(GL_TRIANGLES, 0, GL_UNSIGNED_SHORT, m.indicesBuffer);
	        }
	        else
	        {
		    	int c = 0, cmd;
		    	while((cmd = m.glcmds[c++]) != 0)
		    	{
		    		if(cmd < 0) { gl.glBegin(GL_TRIANGLE_FAN); cmd = -cmd; }
		    		else gl.glBegin(GL_TRIANGLE_STRIP);
		    			
		    		for( /*nothing*/; cmd > 0; cmd--, c += 3)
		    		{
		    			float s = Float.intBitsToFloat(m.glcmds[c + 0]);
		    			float t = Float.intBitsToFloat(m.glcmds[c + 1]);
		
		    			float x = cframe.vertices[m.glcmds[c + 2]][0]*cScale.x + nframe.vertices[m.glcmds[c + 2]][0]*nScale.x;
		    			float y = cframe.vertices[m.glcmds[c + 2]][1]*cScale.y + nframe.vertices[m.glcmds[c + 2]][1]*nScale.y;
		    			float z = cframe.vertices[m.glcmds[c + 2]][2]*cScale.z + nframe.vertices[m.glcmds[c + 2]][2]*nScale.z;
		
		    			gl.glTexCoord2d(s, t);
		    			gl.glVertex3d(x, z, y);
		    		}
		    		gl.glEnd();
		    	}

//		    	gl.glBegin(GL_TRIANGLES);
//		    	for( int i = 0; i < m.tris.length; i++)
//		    	{
//		    		for( int j = 0; j < 3; j++)
//		    		{
//		    			int vIdx = m.tris[i].vertices[j];
//		    			float x = cframe.vertices[vIdx][0]*m0x + nframe.vertices[vIdx][0]*m1x;
//		    			float y = cframe.vertices[vIdx][1]*m0y + nframe.vertices[vIdx][1]*m1y;
//		    			float z = cframe.vertices[vIdx][2]*m0z + nframe.vertices[vIdx][2]*m1z;
//		
//		    			int tIdx = m.tris[i].texCoords[j];
//		    			gl.glTexCoord2d(m.uv.get(2 * tIdx), m.uv.get(2 * tIdx + 1)); //uv rewrited for drawelements
//		    			gl.glVertex3d(x, z, y);
//		    		}
//		    	}
//		    	gl.glEnd();
	        }


	    	while (texunits > GL_TEXTURE0)
	        {
	        	gl.glMatrixMode(GL_TEXTURE);
	            gl.glLoadIdentity();
	            gl.glMatrixMode(GL_MODELVIEW);
	            gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
	            gl.glDisable(GL_TEXTURE_2D);
	            if (r_vertexarrays != 0)
	            {
	                gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	                gl.glClientActiveTexture(texunits - 1);
	            }
	            gl.glActiveTexture(--texunits);
	        }
	    	rendered = 1;
        }
    	
    	if (m.usesalpha) gl.glDisable(GL_ALPHA_TEST);
    	gl.glDisable(GL_CULL_FACE);
//    	gl.glPopAttrib();
        gl.glLoadIdentity();

		return rendered;
	}
	
	static final float dvoxphack[] = new float[2], dvoxclut[] = { 1, 1, 1, 1, 1, 1 };
	static final Vector3 dvoxfp = new Vector3(), dvoxm0 = new Vector3(), modela0 = new Vector3();

	public int voxdraw(VOXModel m, SPRITE tspr, int xoff, int yoff) {
		int i, j, fi, xx, yy, zz;
		float ru, rv;
		float f, g;
	
		if (m == null)
			return 0;
		if ((sprite[tspr.owner].cstat & 48) == 32)
			return 0;
		
		dvoxm0.x = m.scale;
		dvoxm0.y = m.scale;
		dvoxm0.z = m.scale;
		modela0.x = modela0.y = 0;
		modela0.z = ((globalorientation & 8) != 0 ? -m.zadd : m.zadd) * m.scale;

		f = ((float) tspr.xrepeat) * (256.0f / 320.0f) / 64.0f * m.bscale;
		if ((sprite[tspr.owner].cstat & 48) == 16)
			f *= 1.25f;
		if ((sprite[tspr.owner].cstat & 48) == 32)
			f *= 1.25f;

		dvoxm0.x *= f;
		modela0.x *= f;
		f = -f;
		dvoxm0.y *= f;
		modela0.y *= f;
		f = ((float) tspr.yrepeat) / 64.0f * m.bscale;
		dvoxm0.z *= f;
		modela0.z *= f;

		float x0 = (float) tspr.x;
		float k0 = (float) tspr.z;
		
		boolean xflip = false;
		boolean yflip = false;

		if (xflip = (globalorientation & 4) != 0)
			xoff = -xoff;
		
		if (yflip = (globalorientation & 8) != 0)
			yoff = -yoff;
		
		if (!yflip)
		{
			k0 -= ((yoff * tspr.yrepeat) << 2);
			if ((globalorientation & 128) != 0)
				k0 += ((m.zsiz * tspr.yrepeat) << 1);
		} 
		else
		{
			k0 += ((m.zsiz * tspr.yrepeat) << 1);
			if((globalorientation & 128) != 0)
				k0 -= ((m.zsiz * tspr.yrepeat) << 2);
			
			k0 += (yoff * tspr.yrepeat) << 2;
		}
		
		x0 += xoff * (tspr.xrepeat >> 2);

		f = (65536.0f * 512.0f) / ((float) (xdimen * viewingrange));
		g = 32.0f / (float) (xdimen * gxyaspect);
		
		dvoxm0.y *= f;
		if ((sprite[tspr.owner].cstat & 48) == 32)
			dvoxm0.y *= -1;
		modela0.y = (((float) (x0 - globalposx)) / 1024.0f + modela0.y) * f;
		dvoxm0.x *= -f;
		if ((sprite[tspr.owner].cstat & 48) == 32)
			dvoxm0.x *= -1;
		if(xflip) dvoxm0.x *= -1;
		modela0.x = (((float) (tspr.y - globalposy)) / -1024.0f + modela0.x) * -f;
		dvoxm0.z *= g;
		if(yflip) dvoxm0.z *= -1;
		modela0.z = (((float) (k0 - globalposz)) / -16384.0f + modela0.z) * g;

		if ((grhalfxdown10x >= 0) ^((globalorientation&8) != 0) ^((globalorientation&4) != 0)) 
			gl.glFrontFace(GL_CW); else gl.glFrontFace(GL_CCW);

		gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);

		gl.glEnable(GL_TEXTURE_2D);

		polyColor.r = polyColor.g = polyColor.b = ((float) (numshades - min(max((globalshade * shadescale) + m.shadeoff, 0), numshades))) / ((float) numshades);

		if(defs != null) {
		Palette p = defs.texInfo.getTints(globalpal);
		polyColor.r *= p.r / 255.0f;
		polyColor.g *= p.g / 255.0f;
		polyColor.b *= p.b / 255.0f;
		}
		
		if ((tspr.cstat & 2) != 0) {
			if ((tspr.cstat & 512) == 0)
				polyColor.a = TRANSLUSCENT1;
			else
				polyColor.a = TRANSLUSCENT2;
		} else
			polyColor.a = 1.0f;
		if ((tspr.cstat & 2) != 0)
			gl.glEnable(GL_BLEND);
		
		
		gl.glMatrixMode(GL_MODELVIEW); // Let OpenGL (and perhaps hardware :) handle the matrix rotation
		
		boolean newmatrix = false;
		
		// ------------ Matrix
		if(!newmatrix)
			md3_vox_calcmat_common(tspr, modela0, f, matrix);
		else { md3_vox_calcmat_common(tspr, modela0); }

		if(!newmatrix) {
			matrix[0][3] = matrix[1][3] = matrix[2][3] = 0.f;
			matrix[3][3] = 1.f;
			gl.glLoadMatrixf(matrix);
		}

		// transform to Build coords
		if ((tspr.cstat & 48) == 32) {
			gl.glScalef(dvoxm0.x / 64.0f, dvoxm0.z / 64.0f, dvoxm0.y / 64.0f);
			gl.glRotatef(90, 1.0f, 0.0f, 0.0f);
			gl.glTranslatef(-m.xpiv, -m.ypiv, -m.zpiv);
			gl.glRotatef(90, -1.0f, 0.0f, 0.0f);
			gl.glTranslatef(0, -m.ypiv, -m.zpiv);
		} else {
			gl.glScalef(dvoxm0.x / 64.0f, dvoxm0.z / 64.0f, dvoxm0.y / 64.0f);
			gl.glRotatef(90, 1.0f, 0.0f, 0.0f);
			gl.glTranslatef(-m.xpiv, -m.ypiv, -(m.zpiv + m.zsiz * 0.5f));
		}
		
		if((m.flags & MD_ROTATE)!= 0)
			gl.glRotatef(totalclock % 360, 0, 0, 1);

		ru = 1.f / ((float) m.mytexx);
		rv = 1.f / ((float) m.mytexy);

		dvoxphack[0] = 0;
		dvoxphack[1] = 1.f / 256.f;
		
		textureCache.bindShader();
		textureCache.setShaderParams(globalpal, engine.getpalookup(0, globalshade));
		textureCache.shaderTransparent(polyColor.a);

		if (m.texid[globalpal] == null)
			m.loadskin(globalpal, textureCache.isUseShader());
		else
			bindTexture(m.texid[globalpal]);

		if(r_vertexarrays != 0)
		{
			gl.glColor4f(polyColor.r, polyColor.g, polyColor.b, polyColor.a);
			gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	        gl.glTexCoordPointer(2, GL_FLOAT, 0, m.uv);
			gl.glEnableClientState(GL_VERTEX_ARRAY);
	        gl.glVertexPointer(3, GL_FLOAT, 0, m.verticesBuffer);
	        gl.glDrawElements(GL_QUADS, 0, GL_UNSIGNED_SHORT, m.indicesBuffer);
		} 
		else
		{
			gl.glBegin(GL_QUADS);
			for (i = 0, fi = 0; i < m.qcnt; i++) {
				if (i == m.qfacind[fi]) {
					f = dvoxclut[fi++];
					gl.glColor4f(polyColor.r * f, polyColor.g * f, polyColor.b * f, polyColor.a * f);
				}
	
				xx = m.quad[i].v[0].x + m.quad[i].v[2].x;
				yy = m.quad[i].v[0].y + m.quad[i].v[2].y;
				zz = m.quad[i].v[0].z + m.quad[i].v[2].z;
	
				for (j = 0; j < 4; j++) {
					gl.glTexCoord2d((m.quad[i].v[j].u) * ru, (m.quad[i].v[j].v) * rv);
					dvoxfp.x = ((float) m.quad[i].v[j].x) - dvoxphack[(xx > (m.quad[i].v[j].x * 2)) ? 1 : 0] + dvoxphack[(xx < (m.quad[i].v[j].x * 2)) ? 1 : 0];
					dvoxfp.y = ((float) m.quad[i].v[j].y) - dvoxphack[(yy > (m.quad[i].v[j].y * 2)) ? 1 : 0] + dvoxphack[(yy < (m.quad[i].v[j].y * 2)) ? 1 : 0];
					dvoxfp.z = ((float) m.quad[i].v[j].z) - dvoxphack[(zz > (m.quad[i].v[j].z * 2)) ? 1 : 0] + dvoxphack[(zz < (m.quad[i].v[j].z * 2)) ? 1 : 0];
					gl.glVertex3d(dvoxfp.x, dvoxfp.y, dvoxfp.z);
				}
			}
			gl.glEnd();
		}
	
		// ------------
		gl.glDisable(GL_CULL_FACE);
		gl.glLoadIdentity();
		
		textureCache.unbindShader();

		return 1;
	}

	@Override
	public void gltexinvalidateall(int flags) {
		if ((flags & 1) == 1)
			textureCache.uninit();
		if ((flags & 2) == 0)
			gltexinvalidateall();
		if ((flags & 8) == 0)
			gltexinvalidate8();
	}

	@Override
	public void clearview(int dacol) {
		gl.glClearColor(((float) (curpalette[3*dacol]&0xFF)) / 255.0f,
				((float) (curpalette[3*dacol+1]&0xFF)) / 255.0f,
				((float) (curpalette[3*dacol+2]&0xFF)) / 255.0f,
				0);
		gl.glClear(GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void nextpage() {
		int i;
		engine.faketimerhandler();

		if ((totalclock >= lastageclock + CACHEAGETIME) || (totalclock < lastageclock)) {
			lastageclock = totalclock;
			/* agecache(); */} // FIXME

		omdtims = mdtims;
		mdtims = engine.getticks();

		for (i = 0; i < MAXSPRITES; i++)
			if ((mdpause != 0 && spriteext[i].mdanimtims != 0) || ((spriteext[i].flags & SPREXT_NOMDANIM) != 0)) 
				spriteext[i].mdanimtims += mdtims - omdtims;

		beforedrawrooms = 1;
		ogshang = -1;
		gl.glFlush();
	}
	
	private ByteBuffer framebuffer;

	@Override
	public ByteBuffer getframebuffer(int x, int y, int w, int h, int format) {
		if (framebuffer != null) framebuffer.clear();
		if (framebuffer == null || framebuffer.capacity() < w * h * 3 )
			framebuffer = BufferUtils.newByteBuffer(w * h * 3);
		
		gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
		gl.glReadPixels(x, y, w, h, format, GL10.GL_UNSIGNED_BYTE, framebuffer);
		return framebuffer;
	}
	
	public int nearwall(int i, int range) {
		SPRITE spr = sprite[i];
		short sectnum = spr.sectnum;
		int xs = spr.x; int ys = spr.y;

		int vx = mulscale(sintable[(spr.ang + 2560) & 2047], range, 14);
		int xe = xs + vx;
		int vy = mulscale(sintable[(spr.ang + 2048) & 2047], range, 14);
		int ye = ys + vy;

		if ((sectnum < 0) || (sectnum >= numsectors))
			return (-1);
		
		short startwall = sector[sectnum].wallptr;
		int endwall = (startwall + sector[sectnum].wallnum - 1);
		for (int z = startwall; z <= endwall; z++) {
			WALL wal = wall[z];
			WALL  wal2 = wall[wal.point2];
			int x1 = wal.x;
			int y1 = wal.y;
			int x2 = wal2.x;
			int y2 = wal2.y;

			if ((x1 - xs) * (y2 - ys) < (x2 - xs) * (y1 - ys))
				continue;

			if(wal.nextsector != -1) {
				int daz = engine.getflorzofslope(sectnum, xs, ys);
				int daz2 = engine.getflorzofslope(wal.nextsector, xs, ys);

				boolean clipyou = false;
				int z1 = spr.z, z2 = spr.z;
				int yoff =  ((byte)(picanm[spr.picnum] >> 16) & 255);
				
				if((spr.cstat & 128) != 0) {
					z1 -= (yoff + tilesizy[spr.picnum] / 2) * (spr.yrepeat << 2);
					z2 += (tilesizy[spr.picnum] - (tilesizy[spr.picnum] / 2 + yoff)) * (spr.yrepeat << 2);
				} else 
					z1 -= (yoff + tilesizy[spr.picnum]) * (spr.yrepeat << 2);
				
				if (daz2 < daz - (1 << 8))
					if (z2 >= daz2) clipyou = true;
				if (!clipyou) {
					daz = engine.getceilzofslope(sectnum, xs, ys);
					daz2 = engine.getceilzofslope(wal.nextsector, xs, ys);
					if (daz2 > daz + (1 << 8))
							if (z1 <= daz2)
								clipyou = true;
				}
				
				if(!clipyou) continue;
			}
			
			Point out;
			if ((out = engine.lintersect(xs, ys, 0, xe, ye, 0, x1, y1, x2, y2)) != null) {
				int dist = dmulscale(out.getX() - xs, sintable[(spr.ang + 2560) & 2047], out.getY() - ys, sintable[(spr.ang + 2048) & 2047], 14);
				if(klabs(dist) <= 8) {
					int wallang = engine.getangle(wall[wal.point2].x - wal.x, wall[wal.point2].y - wal.y) - 512;
					int nx = out.getX() - mulscale(sintable[(wallang + 2560) & 2047], 4, 14);
					int ny = out.getY() - mulscale(sintable[(wallang + 2048) & 2047], 4, 14);
					dcoord[i].x = nx - spr.x;
					dcoord[i].y = ny - spr.y;
				}
				return z;
			}
		}

		return -1;
	}

	@Override
	public void preload() {
		System.err.println("Preload");
		for(int i = 0; i < MAXSPRITES; i++)
		{
			removeSpriteCorr(i);
			SPRITE spr = sprite[i];
			if(((spr.cstat >> 4) & 3) != 1 || spr.statnum == MAXSTATUS) 
				continue;
			
			addSpriteCorr(i);
		}
	}
	
	@Override
	public void addSpriteCorr(int snum) {
		int spr_wall = -1;
		SPRITE spr = sprite[snum];
		if((spr_wall = nearwall(snum, -64)) == -1) 
			if((spr.cstat & 64) != 0 || (spr_wall = nearwall(snum, 64)) == -1)
				return;

		spritewall[snum] = spr_wall;
		float sang = spr.ang * 360 / 2048;
		int wdx = wall[spr_wall].x-wall[wall[spr_wall].point2].x;
		int wdy = wall[spr_wall].y-wall[wall[spr_wall].point2].y;
		float wang = new Vector2(wdx, wdy).angle()-90;
		if(wang < 0) wang += 360;
		wang = BClipRange(wang, 0, 360);
		if(Math.abs(wang - sang) > 10) return;

		dsin[snum].x = (sintable[(spr.ang) & 2047] / 65536.0f) - (float) (Math.sin(Math.toRadians(wang)) / 4);
		dsin[snum].y = (sintable[(spr.ang + 1536) & 2047] / 65536.0f) - (float) (Math.sin(Math.toRadians(wang + 270)) / 4);
	}
	
	@Override
	public void removeSpriteCorr(int snum) {
		dsin[snum].set(0, 0);
		dcoord[snum].set(0, 0);
		spritewall[snum] = -1;
	}

	@Override
	public void settiltang(int tilt) {
	    if (tilt == 0) gtang = 0.0f;
	    else gtang = (float) (PI * tilt / 1024.0);
	}

	@Override
	public void setdrunk(float intensive) {
		if(intensive == 0) {
			drunk = false;
			drunkIntensive = 0;
		} else {
			drunk = true;
			drunkIntensive = intensive;
		}
	}

	@Override
	public float getdrunk() {
		return drunkIntensive;
	}
	
	public double polymost_getflorzofslope(int sectnum, double dax, double day) {
		if(sector[sectnum] == null) return 0;
		if ((sector[sectnum].floorstat & 2) == 0)
			return (sector[sectnum].floorz);

		WALL wal = wall[sector[sectnum].wallptr];
		int dx = wall[wal.point2].x - wal.x;
		int dy = wall[wal.point2].y - wal.y;
		long i = (engine.ksqrt(dx * dx + dy * dy) << 5);
		if (i == 0)
			return (sector[sectnum].floorz);

		double j = (dx * (day - wal.y) - dy * (dax - wal.x)) / 8;
		return sector[sectnum].floorz + sector[sectnum].floorheinum * j / i;
	}
	
	public double polymost_getceilzofslope(int sectnum, double dax, double day) {
		if ((sector[sectnum].ceilingstat & 2) == 0)
			return (sector[sectnum].ceilingz);

		WALL wal = wall[sector[sectnum].wallptr];
		int dx = wall[wal.point2].x - wal.x;
		int dy = wall[wal.point2].y - wal.y;
		long i = (engine.ksqrt(dx * dx + dy * dy) << 5);
		if (i == 0)
			return (sector[sectnum].ceilingz);
		
		double j = (dx * (day - wal.y) - dy * (dax - wal.x)) / 8;
		return sector[sectnum].ceilingz + sector[sectnum].ceilingheinum * j / i;
	}
	
	private static double dceilzsofslope, dfloorzsofslope;
	public void polymost_getzsofslope(int sectnum, double dax, double day) {
		SECTOR sec = sector[sectnum];
		if(sec == null) return;
		dceilzsofslope = sec.ceilingz;
		dfloorzsofslope = sec.floorz;
		if (((sec.ceilingstat | sec.floorstat) & 2) != 0) {
			WALL wal = wall[sec.wallptr];
			WALL wal2 = wall[wal.point2];
			int dx = wal2.x - wal.x;
			int dy = wal2.y - wal.y;
			long i = (engine.ksqrt(dx * dx + dy * dy) << 5);
			if (i == 0)
				return;
			double j = (dx * (day - wal.y) - dy * (dax - wal.x)) / 8;

			if ((sec.ceilingstat & 2) != 0)
				dceilzsofslope += sector[sectnum].ceilingheinum * j / i;
			if ((sec.floorstat & 2) != 0)
				dfloorzsofslope += sector[sectnum].floorheinum * j / i;
		}
	}
	

	@Override
	public String getname() {
		return "Polymost";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//2d drawing
	
	private final FloatBuffer vertices = BufferUtils.newFloatBuffer(8);
	private final FloatBuffer textures = BufferUtils.newFloatBuffer(8);

	private void init2drender()
	{
		vertices.put(new float[] { 0, 0, 1, 0, 1, 1, 0, 1 });
		textures.put(new float[] { 0, 0, 1 - 0.0001f, 0, 1 - 0.0001f, 1 - 0.0001f, 0, 1 - 0.0001f });
		vertices.rewind();
		textures.rewind();
		
		if (polymosttext == null) {
			// construct a 256x128 8-bit alpha-only texture for the font glyph
			// matrix
			byte[] tbuf;
			int tptr;
			int h, i, j;
			polymosttext = BufferUtils.newIntBuffer(1);

			tbuf = new byte[256 * 128];
			ByteBuffer fbuf = BufferUtils.newByteBuffer(256 * 128);

			for (h = 0; h < 256; h++) {
				tptr = (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 0; i < 8; i++) {
					for (j = 0; j < 8; j++) {
						if ((textfont[h * 8 + i] & pow2char[7 - j]) != 0)
							tbuf[tptr + j] = (byte) 255;
					}
					tptr += 256;
				}
			}

			for (h = 0; h < 256; h++) {
				tptr = 256 * 64 + (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 1; i < 7; i++) {
					for (j = 2; j < 6; j++) {
						if ((smalltextfont[h * 8 + i] & pow2char[7 - j]) != 0)
							tbuf[tptr + j - 2] = (byte) 255;
					}
					tptr += 256;
				}
			}

			fbuf.put(tbuf);
			fbuf.rewind();

			gl.glBindTexture(GL_TEXTURE_2D, polymosttext);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, 256, 128, 0, GL_ALPHA, GL_UNSIGNED_BYTE, fbuf);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

			fbuf.clear(); // Bfree(tbuf);
			fbuf = null;
			tbuf = null;
		} 
	}
	
	protected int globalx1;
	protected int globaly1;
	protected int globalx2;
	protected int globaly2;
	
	private int guniqhudid;
	
	protected int[] xb1 = new int[MAXWALLSB];
	protected int[] xb2 = new int[MAXWALLSB];
	protected float[] rx1 = new float[MAXWALLSB];
	protected float[] ry1 = new float[MAXWALLSB];

	private int allocpoints = 0, slist[], npoint2[];
	private raster[] rst;
	private final float[] trapextx = new float[2];
		
	private final SPRITE hudsprite = new SPRITE();
	
//	private final int ROTATESPRITE_MAX = 2048;
	private final int RS_CENTERORIGIN = (1 << 30);
	
	protected int asm1; //drawmapview
	protected int asm2; //drawmapview
	
	@Override
	public abstract void drawmapview(int dax, int day, int zoome, int ang);
	
	@Override
	public abstract void drawoverheadmap(int cposx, int cposy, int czoom, short cang);

	protected void setpolymost2dview() {
		if (gloy1 != -1) {
			gl.glViewport(0, 0, xdim, ydim);
			gl.glMatrixMode(GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrthof(0, xdim, ydim, 0, -1, 1);
			gl.glMatrixMode(GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		gloy1 = -1;

		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_TEXTURE_2D);
		gl.glDisable(GL_BLEND);
	}

	protected void fillpolygon(int npoints) {

		for (int z = 0; z < npoints; z++) {
			if (xb1[z] >= npoints)
				xb1[z] = 0;
		}

		if (palookup[globalpal] == null)
			globalpal = 0;

		globalx1 = mulscale(globalx1, xyaspect, 16);
		globaly2 = mulscale(globaly2, xyaspect, 16);
		gux = asm1 / 4294967296.0;
		gvx = asm2 / 4294967296.0;
		guy = globalx1 / 4294967296.0;
		gvy = -globaly2 / 4294967296.0;
		guo = (xdim * gux + ydim * guy) * -0.5 + globalposx / 4294967296.0;
		gvo = (xdim * gvx + ydim * gvy) * -0.5 - globalposy / 4294967296.0;

		for (int i = npoints - 1; i >= 0; i--) {
			rx1[i] /= 4096.0f;
			ry1[i] /= 4096.0f;
		}
		
		gl.glDisable(GL_FOG);

		setpolymost2dview(); 
		gl.glEnable(GL_ALPHA_TEST);
		gl.glEnable(GL_TEXTURE_2D);
		Pthtyp pth = textureCache.cache(globalpicnum, globalpal, (short) 0,  false, true);

		bindTexture(pth.glpic);
		float f = getshadefactor(globalshade), a = 0.0f;
		
		switch ((globalorientation >> 7) & 3) {
		case 0:
		case 1:
			a = 1.0f;
			gl.glDisable(GL_BLEND);
			break;
		case 2:
			a = TRANSLUSCENT1;
			gl.glEnable(GL_BLEND);
			break;
		case 3:
			a = TRANSLUSCENT2;
			gl.glEnable(GL_BLEND);
			break;
		}

		gl.glColor4f(f, f, f, a);

		tessectrap(rx1, ry1, xb1, npoints); // vertices + textures

		EnableFog();
	}
	
	private void drawtrap(float x0, float x1, float y0, float x2, float x3, float y1) {
		if (y0 == y1) return;
		
		drawpoly[0].px = x0;
		drawpoly[0].py = y0;
		drawpoly[2].py = y1;
		
		int n = 3;
		if (x0 == x1) {
			drawpoly[1].px = x3;
			drawpoly[1].py = y1;
			drawpoly[2].px = x2;
		} else if (x2 == x3) {
			drawpoly[1].px = x1;
			drawpoly[1].py = y0;
			drawpoly[2].px = x3;
		} else {
			drawpoly[1].px = x1;
			drawpoly[1].py = y0;
			drawpoly[2].px = x3;
			drawpoly[3].px = x2;
			drawpoly[3].py = y1;
			n = 4;
		}

		gl.glBegin(GL_TRIANGLE_FAN);
		for (int i = 0; i < n; i++) {
			drawpoly[i].px = min(max(drawpoly[i].px, trapextx[0]), trapextx[1]);
			gl.glTexCoord2d(
				drawpoly[i].px * gux + drawpoly[i].py * guy + guo,
				drawpoly[i].px * gvx + drawpoly[i].py * gvy + gvo);
			gl.glVertex2d(drawpoly[i].px, drawpoly[i].py);
		}
		gl.glEnd();
	}

	private void tessectrap(float[] px, float[] py, int[] point2, int numpoints) {
		float x0, x1, m0, m1;
		int i, j, k, z, i0, i1, i2, i3, npoints, gap, numrst;

		if (numpoints + 16 > allocpoints) // 16 for safety
		{
			allocpoints = numpoints + 16;
			rst = new raster[allocpoints];
			for (i = 0; i < allocpoints; i++)
				rst[i] = new raster();

			slist = new int[allocpoints];

			npoint2 = new int[allocpoints];
		}

		// Remove unnecessary collinear points:
		for (i = 0; i < numpoints; i++)
			npoint2[i] = point2[i];
		npoints = numpoints;
		z = 0;

		for (i = 0; i < numpoints; i++) {
			j = npoint2[i];
			if ((point2[i] < i) && (i < numpoints - 1))
				z = 3;

			if (j < 0) continue;
			k = npoint2[j];
			if (k < 0) continue;

			m0 = (px[j] - px[i])
					* (py[k] - py[j]);
			m1 = (py[j] - py[i]) * (px[k] - px[j]);
			if (m0 < m1) {
				z |= 1;
				continue;
			}
			if (m0 > m1) {
				z |= 2;
				continue;
			}
			npoint2[i] = k;
			npoint2[j] = -1;
			npoints--;
			i--; // collinear
		}

		if (z == 0)
			return;
		trapextx[0] = trapextx[1] = px[0];
		for (i = j = 0; i < numpoints; i++) {
			if (npoint2[i] < 0)
				continue;
			if (px[i] < trapextx[0])
				trapextx[0] = px[i];
			if (px[i] > trapextx[1])
				trapextx[1] = px[i];
			slist[j++] = i;
		}

		if (z != 3) // Simple polygon... early out
		{
			gl.glBegin(GL_TRIANGLE_FAN);
			for (i = 0; i < npoints; i++) {
				j = slist[i];
				gl.glTexCoord2f((float) (px[j] * gux + py[j] * guy + guo),
						(float) (px[j] * gvx + py[j] * gvy + gvo));
				gl.glVertex2d(px[j], py[j]);
			}
			gl.glEnd();
			return;
		}

		// Sort points by y's
		for (gap = (npoints >> 1); gap != 0; gap >>= 1)
			for (i = 0; i < npoints - gap; i++)
				for (j = i; j >= 0; j -= gap) {
					if (py[npoint2[slist[j]]] <= py[npoint2[slist[j + gap]]])
						break;
					k = slist[j];
					slist[j] = slist[j + gap];
					slist[j + gap] = k;
				}

		numrst = 0;
		for (z = 0; z < npoints; z++) {
			i0 = slist[z];
			i1 = npoint2[i0];
			if (py[i0] == py[i1] || npoint2[i1] == -1)
				continue;
			i2 = i1;
			i3 = npoint2[i1];
			if (py[i1] == py[i3]) {
				i2 = i3;
				i3 = npoint2[i3];
			}

			// i0 i3
			// \ /
			// i1--i2
			// / \ ~
			// i0 i3

			if ((py[i1] < py[i0]) && (py[i2] < py[i3])) // Insert raster
			{
				for (i = numrst; i > 0; i--) {
					if (rst[i - 1].xi * (py[i1] - rst[i - 1].y) + rst[i - 1].x < px[i1])
						break;
					rst[i + 1].set(rst[i - 1]);
				}
				numrst += 2;
				if ((i & 1) != 0) // split inside area
				{
					j = i - 1;
					x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
					x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi + rst[j + 1].x;
					drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1, py[i1]);
					rst[j].x = x0;
					rst[j].y = py[i1];
					rst[j + 3].x = x1;
					rst[j + 3].y = py[i1];
				}

				m0 = (px[i0] - px[i1]) / (py[i0] - py[i1]);
				m1 = (px[i3] - px[i2]) / (py[i3] - py[i2]);

				j = ((px[i1] > px[i2] || (i1 == i2) && (m0 >= m1)) ? 1 : 0) + i;
				if(j < 0) continue;
				k = (i << 1) + 1 - j;

				rst[j].i = i0;
				rst[j].xi = m0;
				rst[j].x = px[i1];
				rst[j].y = py[i1];
				rst[k].i = i3;
				rst[k].xi = m1;
				rst[k].x = px[i2];
				rst[k].y = py[i2];
			} else {
				// NOTE:don't count backwards!
				if (i1 == i2) {
					for (i = 0; i < numrst; i++)
						if (rst[i].i == i1)
							break;
				} else {
					for (i = 0; i < numrst; i++)
						if ((rst[i].i == i1) || (rst[i].i == i2))
							break;
				}
				j = i & ~1;

				if ((py[i1] > py[i0]) && (py[i2] > py[i3])) // Delete raster
				{
					for (; j <= i + 1; j += 2) {
						x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
						if ((i == j) && (i1 == i2))
							x1 = x0;
						else
							x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi
									+ rst[j + 1].x;
						drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1,py[i1]);
						rst[j].x = x0;
						rst[j].y = py[i1];
						rst[j + 1].x = x1;
						rst[j + 1].y = py[i1];
					}
					numrst -= 2;
					for (; i < numrst; i++)
						rst[i].set(rst[i + 2]);
				} else {
					x0 = (py[i1] - rst[j].y) * rst[j].xi + rst[j].x;
					x1 = (py[i1] - rst[j + 1].y) * rst[j + 1].xi + rst[j + 1].x;

					drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1, py[i1]);
					rst[j].x = x0;
					rst[j].y = py[i1];
					rst[j + 1].x = x1;
					rst[j + 1].y = py[i1];

					if (py[i0] < py[i3]) {
						rst[i].x = px[i2];
						rst[i].y = py[i2];
						rst[i].i = i3;
					} else {
						rst[i].x = px[i1];
						rst[i].y = py[i1];
						rst[i].i = i0;
					}
					rst[i].xi = (px[rst[i].i] - rst[i].x) / (py[rst[i].i] - py[i1]);
				}

			}
		}
	}
	
	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		int oxpos = xpos;
		
		if(textureCache.isUseShader())
			gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, polymosttext);

		setpolymost2dview();
		gl.glDisable(GL_FOG);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glDepthMask(GL_FALSE); // disable writing to the z-buffer

		if (backcol >= 0) {
			gl.glColor4ub(curpalette[3*backcol]&0xFF,
					curpalette[3*backcol+1]&0xFF, curpalette[3*backcol+2]&0xFF,
					255);
			int c = Bstrlen(text);

			gl.glBegin(GL_TRIANGLE_FAN);
			gl.glVertex2i(xpos, ypos);
			gl.glVertex2i(xpos, ypos + (fontsize != 0 ? 6 : 8));
			int x = xpos + (c << (3 - fontsize));
			int y = ypos + (fontsize != 0 ? 6 : 8);
			gl.glVertex2i(x, y);
			gl.glVertex2i(xpos + (c << (3 - fontsize)), ypos);
			gl.glEnd();
		}

		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);
		gl.glColor4ub(curpalette[3*col]&0xFF, curpalette[3*col+1]&0xFF, curpalette[3*col+2]&0xFF, 255);

		float txc = (fontsize != 0 ? (4.0f / 256.0f) : (8.0f / 256.0f));
		float tyc = (fontsize != 0 ? (6.0f / 128.0f) : (8.0f / 128.0f));

		gl.glBegin(GL_TRIANGLE_STRIP);

		int c = 0, line = 0;
		int x, y, yoffs;
		float tx, ty;

		while (c < text.length && text[c] != '\0') {
			if(text[c] == '\n')
			{
				text[c] = 0;
				line += 1;
				xpos = oxpos - (8 >> fontsize);
			}
			if(text[c] == '\r')
				text[c] = 0;
			
			tx = (text[c] % 32) / 32.0f;
			ty = ((text[c] / 32) + (fontsize * 8)) / 16.0f;

			yoffs = line * (fontsize != 0 ? 6 : 8);

			x = xpos + (int) (scale * (8 >> fontsize));
			y = ypos + (int) (scale * (fontsize != 0 ? 6 : 8));

			gl.glTexCoord2f(tx, ty);
			gl.glVertex2i(xpos, ypos + yoffs);
			gl.glTexCoord2f(tx, ty + tyc);
			gl.glVertex2i(xpos, y + yoffs);
			gl.glTexCoord2f(tx + txc, ty);
			gl.glVertex2i(x, ypos + yoffs);
			gl.glTexCoord2f(tx + txc, ty + tyc);
			gl.glVertex2i(x, y + yoffs);

			xpos += scale * (8 >> fontsize);
			c++;
		}

		gl.glEnd();

		gl.glDepthMask(GL_TRUE); // re-enable writing to the z-buffer

		EnableFog();
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		gl.glDisable(GL_FOG);

		setpolymost2dview(); // JBF 20040205: more efficient setup

		col = palookup[0][col] & 0xFF;
		gl.glBegin(GL_LINES);
		gl.glColor4ub(curpalette[3*col]&0xFF, curpalette[3*col+1]&0xFF, curpalette[3*col+2]&0xFF, 255);
		gl.glVertex2f(x1 / 4096.0f, y1 / 4096.0f);
		gl.glVertex2f(x2 / 4096.0f, y2 / 4096.0f);
		gl.glEnd();
		
		EnableFog();
	}
	
	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2,
			int cy2) {

		if (picnum >= MAXTILES) return;
		if ((cx1 > cx2) || (cy1 > cy2)) return;
		if (z <= 16) return;
		
		if ((picanm[picnum] & 192) != 0)
			picnum += engine.animateoffs((short) picnum, (short) 0xc000);
		
		if ((tilesizx[picnum] <= 0) || (tilesizy[picnum] <= 0))
			return;

		if ((dastat & 128) == 0 || beforedrawrooms != 0)
			dorotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2, guniqhudid);
	}
	
	protected void dorotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2,
			int cy2, int uniqid) {

		int ourxyaspect = xyaspect;
		if (usemodels && defs != null && defs.mdInfo.getHudInfo(picnum, dastat).angadd != 0)
	    {
			Tile2model entry = defs != null ? defs.mdInfo.getParams(picnum) : null;
	        if (entry.model != null && entry.framenum >= 0)
	        {
	        	dorotatesprite3d(sx,sy,z,a,picnum,dashade,dapalnum,dastat,cx1,cy1,cx2,cy2,uniqid);
	            return;
	        }
	    }
		
		short ogpicnum = globalpicnum;
		globalpicnum = (short) picnum;
		int ogshade = globalshade;
		globalshade = dashade;
		int ogpal = globalpal;
		globalpal = dapalnum & 0xFF;

		if ((dastat&10) == 2)
            gl.glViewport(windowx1,ydim-(windowy2+1),windowx2-windowx1+1,windowy2-windowy1+1);
        else
        {
            gl.glViewport(0,0,xdim,ydim);
            glox1 = -1; //Force fullscreen (glox1=-1 forces it to restore)
        }
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, xdim-1, ydim-1, 0, -1, 1);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_ALPHA_TEST);
		gl.glEnable(GL_TEXTURE_2D);

		int method = 0;
		if ((dastat & 64) == 0) {
			method = 1;
			if ((dastat & 1) != 0) {
				if ((dastat & 32) == 0)
					method = 2;
				else
					method = 3;
			}
		} else
			method |= 256; // non-transparent 255 color

		method |= 4; // Use OpenGL clamping - dorotatesprite never repeats

		int xsiz = tilesizx[globalpicnum];
		int ysiz = tilesizy[globalpicnum];

		int xoff = 0, yoff = 0;
		if ((dastat & 16) == 0) {
			xoff = (int) ((byte) ((picanm[globalpicnum] >> 8) & 255)) + (xsiz >> 1);
			yoff = (int) ((byte) ((picanm[globalpicnum] >> 16) & 255)) + (ysiz >> 1);
		}

		if ((dastat & 4) != 0)
			yoff = ysiz - yoff;

		if ((dastat & 2) == 0) {
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim)
				ourxyaspect = (10 << 16) / 12;
		} else {
			// dastat&2: Auto window size scaling
			int oxdim = xdim, zoomsc;
			int xdim = oxdim; // SHADOWS global

			int ouryxaspect = yxaspect;
			ourxyaspect = xyaspect;

			// screen center to s[xy], 320<<16 coords.
			int normxofs = sx - (320 << 15), normyofs = sy - (200 << 15);
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim) {
				xdim = (4 * ydim) / 3;

				ouryxaspect = (12 << 16) / 10;
				ourxyaspect = (10 << 16) / 12;
			}

			// nasty hacks go here
			if ((dastat & 8) == 0) {
				int twice_midcx = (cx1 + cx2) + 2;

				// screen x center to sx1, scaled to viewport
				int scaledxofs = scale(normxofs, scale(xdimen, xdim, oxdim), 320);
				int xbord = 0;
				if ((dastat & (256 | 512)) != 0) {
					xbord = scale(oxdim - xdim, twice_midcx, oxdim);
					if ((dastat & 512) == 0)
						xbord = -xbord;
				}

				sx = ((twice_midcx + xbord) << 15) + scaledxofs;
				zoomsc = xdimenscale;
				sy = (((cy1 + cy2) + 2) << 15) + mulscale(normyofs, zoomsc, 16);
			} else {
				// If not clipping to startmosts, & auto-scaling on, as a
				// hard-coded bonus, scale to full screen instead
				sx = (xdim << 15) + scale(normxofs, xdim, 320);

				if ((dastat & 512) != 0)
					sx += (oxdim - xdim) << 16;
				else if (alphaMode(dastat))
					sx += (oxdim - xdim) << 15;

				if ((dastat & RS_CENTERORIGIN) != 0)
					sx += oxdim << 15;

				zoomsc = scale(xdim, ouryxaspect, 320);
				sy = (ydim << 15) + mulscale(normyofs, zoomsc, 16);
			}

			z = mulscale(z, zoomsc, 16);
		}

		gl.glEnable(GL_CLIP_PLANE0);
		gl.glClipPlanef(GL_CLIP_PLANE0, 1, 0, 0, -cx1);
		gl.glEnable(GL_CLIP_PLANE0 + 1);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 1, -1, 0, 0, cx2);
		
		gl.glEnable(GL_CLIP_PLANE0 + 2);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 2, 0, 1, 0, -cy1);
		gl.glEnable(GL_CLIP_PLANE0 + 3);
		gl.glClipPlanef(GL_CLIP_PLANE0 + 3, 0, -1, 0, cy2);
			
		float aspectFix = ((dastat & 2) != 0) || ((dastat & 8) == 0) ? ourxyaspect / 65536.0f : 1.0f;
		float cx = sx / 65536.0f;
		float cy = sy / 65536.0f;
		gl.glTranslatef(cx, cy, 0);
		gl.glScalef(1, 1 / aspectFix, 0);
		gl.glRotatef(360.0f * a / 2048.0f, 0, 0, 1);
		gl.glScalef(z * aspectFix / 65536.0f, z * aspectFix / 65536.0f, 0);
		gl.glTranslatef(-xoff, -yoff, 0);
		gl.glScalef(xsiz, ysiz, 0);

		gl.glDisable(GL_FOG);
		drawrotate(method, dastat);
		EnableFog();
		
		gl.glDisable(GL_CLIP_PLANE0);
		gl.glDisable(GL_CLIP_PLANE0 + 1);
		gl.glDisable(GL_CLIP_PLANE0 + 2);
		gl.glDisable(GL_CLIP_PLANE0 + 3);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPopMatrix();

		globalpicnum = ogpicnum;
		globalshade = ogshade;
		globalpal = ogpal & 0xFF;
	}
	
	protected void drawrotate(int method, int dastat) {

		if (globalpicnum >= MAXTILES)
			globalpicnum = 0;
		if (palookup[globalpal] == null)
			globalpal = 0;
		
		engine.setgotpic(globalpicnum);
		int tsizx = tilesizx[globalpicnum];
		int tsizy = tilesizy[globalpicnum];

		if (waloff[globalpicnum] == null) {
			//
			engine.loadtile(globalpicnum);
			if (waloff[globalpicnum] == null) {
				tsizx = tsizy = 1;
				method = 1;
			}
		}

		Pthtyp pth = textureCache.cache(globalpicnum, globalpal, (short) 0, clampingMode(method), alphaMode(method));
		if(pth == null) //hires texture not found
			return;

		if(!pth.isHighTile()) {
			textureCache.bindShader();
			textureCache.setShaderParams(globalpal, engine.getpalookup(0, globalshade));
		}
		bindTexture(pth.glpic);

		float hackscx = 1.0f, hackscy = 1.0f;
		if (pth != null && pth.isHighTile()) {
			tsizx = pth.sizx;
			tsizy = pth.sizy;
		}

		float ox2 = hackscx / calcSize(tsizx);
		float oy2 = hackscy / calcSize(tsizy);
		
		gl.glMatrixMode(GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glScalef(tsizx, tsizy, 1.0f);
		gl.glScalef(ox2, oy2, 1.0f);
		
		// texture scale by parkar request
 		if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)))
			gl.glScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);
		
		if((dastat & 4) != 0) {
			gl.glScalef(1, -1, 1.0f);
			gl.glTranslatef(0, -1, 0);
		}

		if (((method & 3) == 0)) {
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL_ALPHA_TEST);
		} else {
			gl.glEnable(GL_BLEND);
			gl.glEnable(GL_ALPHA_TEST);
		}

		polyColor.r = polyColor.g = polyColor.b = getshadefactor(globalshade); 
		switch (method & 3) {
			default:
			case 0: case 1: polyColor.a = 1.0f; break;
			case 2: polyColor.a = TRANSLUSCENT1; break;
			case 3: polyColor.a = TRANSLUSCENT2; break;
		}
		calcHictintingColor(pth);
		
		textureCache.shaderTransparent(polyColor.a);
		gl.glColor4f(polyColor.r, polyColor.g, polyColor.b, polyColor.a);
	
		gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL_VERTEX_ARRAY);

		gl.glTexCoordPointer(2, GL_FLOAT, 0, textures);
		gl.glVertexPointer(2, GL_FLOAT, 0, vertices);
		
		gl.glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

		gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL_VERTEX_ARRAY);

		gl.glMatrixMode(GL_TEXTURE);
		gl.glPopMatrix();
		
		if(!pth.isHighTile()) 
			textureCache.unbindShader();
	}

	protected void calcHictintingColor(Pthtyp pth)
	{
		if(defs == null) return;
		// tinting happens only to hightile textures, and only if the
		// texture we're
		// rendering isn't for the same palette as what we asked for
		
		if (pth != null && pth.isHighTile()) {
			if (pth.hicr.palnum != globalpal) {
				// apply tinting for replaced textures
				
				Palette p = defs.texInfo.getTints(globalpal);
	            polyColor.r *= p.r / 255.0f;
	            polyColor.g *= p.g / 255.0f;
	            polyColor.b *= p.b / 255.0f;
			}
			
			Palette pdetail = defs.texInfo.getTints(MAXPALOOKUPS-1);
            if (pdetail.r != 255 || pdetail.g != 255 || pdetail.b != 255)
            {
                polyColor.r *= pdetail.r / 255.0f;
                polyColor.g *= pdetail.g / 255.0f;
                polyColor.b *= pdetail.b / 255.0f;
            }
		}
	}
	
	protected void dorotatesprite3d(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2,
			int cy2, int uniqid)
	{
		int xoff = 0, yoff = 0, xsiz, ysiz;
		int ogshade, ogpal;

		int oldviewingrange;
        float ogxyaspect;
        float x1, y1, z1;
        hudsprite.reset((byte)0);

        if (defs == null || (defs.mdInfo.getHudInfo(picnum, dastat).flags&1) != 0) return; //"HIDE" is specified in DEF

        float ogchang = gchang; gchang = 1.0f;
        float ogshang = gshang; gshang = 0.0f; 
        float d = z/(65536.0f*16384.0f);
        float ogctang = gctang; gctang = (float)sintable[(a+512)&2047]*d;
        float ogstang = gstang; gstang = (float)sintable[a&2047]*d;
        ogshade  = (int) globalshade;  globalshade  = dashade;
        ogpal    = globalpal;    globalpal = dapalnum;
        ogxyaspect = gxyaspect; gxyaspect = 1.0f;
        oldviewingrange = viewingrange; viewingrange = 65536;
        
        Hudtyp hudInfo = defs.mdInfo.getHudInfo(picnum, dastat);

        x1 = hudInfo.xadd;
        y1 = hudInfo.yadd;
        z1 = hudInfo.zadd;

        if ((hudInfo.flags&2) == 0) //"NOBOB" is specified in DEF
        {
        	float fx = (sx)*(1.0f/65536.0f);
        	float fy = (sy)*(1.0f/65536.0f);

            if ((dastat&16) != 0)
            {
                xsiz = tilesizx[picnum]; ysiz = tilesizy[picnum];
                xoff = (int) ((byte) ((picanm[picnum] >> 8) & 255)) + (xsiz >> 1);
    			yoff = (int) ((byte) ((picanm[picnum] >> 16) & 255)) + (ysiz >> 1);

                d = z/(65536.0f*16384.0f);
                float cosang, sinang;
                float cosang2 = cosang = (float)sintable[(a+512)&2047]*d;
                float sinang2 = sinang = (float)sintable[a&2047]*d;
                if ((dastat&2) != 0 || ((dastat&8) == 0)) //Don't aspect unscaled perms
                    { d = (float)xyaspect/65536.0f; cosang2 *= d; sinang2 *= d; }
                fx += -(double)xoff*cosang2+ (double)yoff*sinang2;
                fy += -(double)xoff*sinang - (double)yoff*cosang;
            }

            if ((dastat&2) == 0)
            {
                x1 += fx/((double)(xdim<<15))-1.0; //-1: left of screen, +1: right of screen
                y1 += fy/((double)(ydim<<15))-1.0; //-1: top of screen, +1: bottom of screen
            }
            else
            {
                x1 += fx/160.0-1.0; //-1: left of screen, +1: right of screen
                y1 += fy/100.0-1.0; //-1: top of screen, +1: bottom of screen
            }
        }
        hudsprite.ang = (short) (hudInfo.angadd+globalang);

        if ((dastat&4) != 0) { x1 = -x1; y1 = -y1; }

        hudsprite.xrepeat = hudsprite.yrepeat = 32;

        hudsprite.x = (int)(((double)gcosang*z1 - (double)gsinang*x1)*16384.0 + globalposx);
        hudsprite.y = (int)(((double)gsinang*z1 + (double)gcosang*x1)*16384.0 + globalposy);
        hudsprite.z = (int)(globalposz + y1*16384.0*0.8);
        

        hudsprite.picnum = (short) picnum;
        hudsprite.shade = (byte) dashade;
        hudsprite.pal = (short) dapalnum;
        hudsprite.owner = (short) (uniqid+MAXSPRITES);
        globalorientation = (dastat&1)+((dastat&32)<<4)+((dastat&4)<<1);
        hudsprite.cstat = (short) globalorientation;

        if ((dastat&10) == 2)
            gl.glViewport(windowx1,ydim-(windowy2+1),windowx2-windowx1+1,windowy2-windowy1+1);
        else
        {
            gl.glViewport(0,0,xdim,ydim);
            glox1 = -1; //Force fullscreen (glox1=-1 forces it to restore)
        }

       
        gl.glMatrixMode(GL_PROJECTION);
      
        if ((dastat&10) == 2)
        {
            float ratioratio = (float)xdim/ydim;
            matrix[0][0] = (float)ydimen*(ratioratio >= 1.6f?1.2f:1); matrix[0][2] = 1.0f;
            matrix[1][1] = (float)xdimen; matrix[1][2] = 1.0f;
            matrix[2][2] = 1.0f; matrix[2][3] = (float)ydimen*(ratioratio >= 1.6f?1.2f:1);
            matrix[3][2] = -1.0f;
        }
        else { matrix[0][0] = matrix[2][3] = 1.0f; matrix[1][1] = ((float)xdim)/((float)ydim); matrix[2][2] = 1.0001f; matrix[3][2] = 1-matrix[2][2]; }
        gl.glLoadMatrixf(matrix);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        

        if ((hudInfo.flags&8) != 0) //NODEPTH flag
            gl.glDisable(GL_DEPTH_TEST);
        else
        {
            gl.glEnable(GL_DEPTH_TEST);
            gl.glClear(GL_DEPTH_BUFFER_BIT);
        }

        gl.glDisable(GL_FOG);
        mddraw(hudsprite, 0, 0);

        EnableFog();

        viewingrange = oldviewingrange;
        gxyaspect = ogxyaspect;
        globalshade  = ogshade;
        globalpal    = ogpal;
        gchang = ogchang;
        gshang = ogshang;
        gctang = ogctang;
        gstang = ogstang;
	}
}

class raster {
	float x, y, xi;
	int i;

	public void set(raster src) {
		this.x = src.x;
		this.y = src.y;
		this.xi = src.xi;
		this.i = src.i;
	}
};

class Polygon
{
	double dd, uu, vv;
	double px, py;
}

class Surface
{
	double px, py;
	double px2, py2;
	float spx;
	int spt;
}
