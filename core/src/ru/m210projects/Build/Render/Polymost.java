package ru.m210projects.Build.Render;

import static java.lang.Math.*;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Loader.MDSprite.*;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.klabs;
import static ru.m210projects.Build.Pragmas.mulscale;
import static ru.m210projects.Build.Pragmas.scale;
import static ru.m210projects.Build.Render.GL10.GL_ALPHA;
import static ru.m210projects.Build.Render.GL10.GL_ALPHA_TEST;
import static ru.m210projects.Build.Render.GL10.GL_BACK;
import static ru.m210projects.Build.Render.GL10.GL_BLEND;
import static ru.m210projects.Build.Render.GL10.GL_CCW;
import static ru.m210projects.Build.Render.GL10.GL_CLAMP;
import static ru.m210projects.Build.Render.GL10.GL_CLAMP_TO_EDGE;
import static ru.m210projects.Build.Render.GL10.GL_COLOR_BUFFER_BIT;
import static ru.m210projects.Build.Render.GL10.GL_COMBINE_ALPHA_ARB;
import static ru.m210projects.Build.Render.GL10.GL_COMBINE_ARB;
import static ru.m210projects.Build.Render.GL10.GL_COMBINE_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_CULL_FACE;
import static ru.m210projects.Build.Render.GL10.GL_CW;
import static ru.m210projects.Build.Render.GL10.GL_DEPTH_BUFFER_BIT;
import static ru.m210projects.Build.Render.GL10.GL_DEPTH_TEST;
import static ru.m210projects.Build.Render.GL10.GL_DONT_CARE;
import static ru.m210projects.Build.Render.GL10.GL_FALSE;
import static ru.m210projects.Build.Render.GL10.GL_FASTEST;
import static ru.m210projects.Build.Render.GL10.GL_FILL;
import static ru.m210projects.Build.Render.GL10.GL_FOG;
import static ru.m210projects.Build.Render.GL10.GL_FOG_COLOR;
import static ru.m210projects.Build.Render.GL10.GL_FOG_END;
import static ru.m210projects.Build.Render.GL10.GL_FOG_HINT;
import static ru.m210projects.Build.Render.GL10.GL_FOG_MODE;
import static ru.m210projects.Build.Render.GL10.GL_FOG_START;
import static ru.m210projects.Build.Render.GL10.GL_FRONT;
import static ru.m210projects.Build.Render.GL10.GL_FRONT_AND_BACK;
import static ru.m210projects.Build.Render.GL10.GL_GREATER;
import static ru.m210projects.Build.Render.GL10.GL_INTERPOLATE_ARB;
import static ru.m210projects.Build.Render.GL10.GL_LEQUAL;
import static ru.m210projects.Build.Render.GL10.GL_LINE;
import static ru.m210projects.Build.Render.GL10.GL_LINEAR;
import static ru.m210projects.Build.Render.GL10.GL_LINES;
import static ru.m210projects.Build.Render.GL10.GL_MODELVIEW;
import static ru.m210projects.Build.Render.GL10.GL_MODULATE;
import static ru.m210projects.Build.Render.GL10.GL_MULTISAMPLE_ARB;
import static ru.m210projects.Build.Render.GL10.GL_MULTISAMPLE_FILTER_HINT_NV;
import static ru.m210projects.Build.Render.GL10.GL_NEAREST;
import static ru.m210projects.Build.Render.GL10.GL_NICEST;
import static ru.m210projects.Build.Render.GL10.GL_ONE_MINUS_SRC_ALPHA;
import static ru.m210projects.Build.Render.GL10.GL_OPERAND0_ALPHA_ARB;
import static ru.m210projects.Build.Render.GL10.GL_OPERAND0_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_OPERAND1_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_OPERAND2_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_PACK_ALIGNMENT;
import static ru.m210projects.Build.Render.GL10.GL_POINT;
import static ru.m210projects.Build.Render.GL10.GL_PREVIOUS_ARB;
import static ru.m210projects.Build.Render.GL10.GL_PROJECTION;
import static ru.m210projects.Build.Render.GL10.GL_QUADS;
import static ru.m210projects.Build.Render.GL10.GL_REPEAT;
import static ru.m210projects.Build.Render.GL10.GL_REPLACE;
import static ru.m210projects.Build.Render.GL10.GL_RGB_SCALE_ARB;
import static ru.m210projects.Build.Render.GL10.GL_SOURCE0_ALPHA_ARB;
import static ru.m210projects.Build.Render.GL10.GL_SOURCE0_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_SOURCE1_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_SOURCE2_RGB_ARB;
import static ru.m210projects.Build.Render.GL10.GL_SRC_ALPHA;
import static ru.m210projects.Build.Render.GL10.GL_SRC_COLOR;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE0_ARB;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_2D;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_COORD_ARRAY;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_ENV;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_ENV_MODE;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_MAG_FILTER;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_MIN_FILTER;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_WRAP_S;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_WRAP_T;
import static ru.m210projects.Build.Render.GL10.GL_TRIANGLES;
import static ru.m210projects.Build.Render.GL10.GL_TRIANGLE_FAN;
import static ru.m210projects.Build.Render.GL10.GL_TRIANGLE_STRIP;
import static ru.m210projects.Build.Render.GL10.GL_TRUE;
import static ru.m210projects.Build.Render.GL10.GL_UNSIGNED_BYTE;
import static ru.m210projects.Build.Render.GL10.GL_VERTEX_ARRAY;
import static ru.m210projects.Build.Render.TextureUtils.bindTexture;
import static ru.m210projects.Build.Render.TextureUtils.gloadtex;
import static ru.m210projects.Build.Render.TextureUtils.setupBoundTexture;
import static ru.m210projects.Build.Strhandler.Bstrcmp;
import static ru.m210projects.Build.Strhandler.Bstrlen;
import static ru.m210projects.Build.Types.Hightile.HICEFFECTMASK;
import static ru.m210projects.Build.Types.Hightile.hicfindsubst;
import static ru.m210projects.Build.Types.Hightile.hictinting;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import ru.m210projects.Build.Engine;
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
import ru.m210projects.Build.OnSceenDisplay.OSDCOMMAND;
import ru.m210projects.Build.OnSceenDisplay.OSDCVARFUNC;
import ru.m210projects.Build.Types.FadeEffect;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;
import ru.m210projects.Build.Types.Wallspriteinfo;
import ru.m210projects.Build.Types.GLInfo;
import ru.m210projects.Build.Types.Palette;
import ru.m210projects.Build.Types.Pthtyp;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;

public abstract class Polymost implements Renderer {

	protected short globalpicnum;
	protected int globalorientation;
	protected long globalx1;
	protected long globaly1;
	protected long globalx2;
	protected long globaly2;
	protected char globalxshift;
	protected char globalyshift;
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
	
//	private final int ROTATESPRITE_MAX = 2048;
	private final int RS_CENTERORIGIN = (1 << 30);

	private SPRITE[] tspriteptr = new SPRITE[MAXSPRITESONSCREEN + 1];
	private Wallspriteinfo[] wsprinfo;
//	private int[] wallchanged = new int[MAXWALLS];
//	private int[] spritechanged = new int[MAXSPRITES];

	private int spritesx[] = new int[MAXSPRITESONSCREEN + 1];
	private int spritesy[] = new int[MAXSPRITESONSCREEN + 1];
	private int spritesz[] = new int[MAXSPRITESONSCREEN + 1];
	
	private final int MAXWALLSB = ((MAXWALLS >> 2) + (MAXWALLS >> 3));
	protected int asm1;
	protected int asm2;

	protected int[] xb1 = new int[MAXWALLSB];
	private int[] xb2 = new int[MAXWALLSB];
	protected float[] rx1 = new float[MAXWALLSB];
	protected float[] ry1 = new float[MAXWALLSB];
	private float[] rx2 = new float[MAXWALLSB];
	private float[] ry2 = new float[MAXWALLSB];
	private short[] p2 = new short[MAXWALLSB], thesector = new short[MAXWALLSB], thewall = new short[MAXWALLSB];
	private short maskwall[] = new short[MAXWALLSB];
	private int maskwallcnt;

	private short[] bunchfirst = new short[MAXWALLSB], bunchlast = new short[MAXWALLSB];

	private final int CACHEAGETIME = 16;

	private int global_cf_z;
	private float global_cf_xpanning, global_cf_ypanning, global_cf_heinum;
	private int global_cf_shade, global_cf_pal;

	private float[] alphahackarray = new float[MAXTILES];
	private float shadescale = 1.0f;

	private boolean nofog;
	
	private int guniqhudid;
	
	// For GL_LINEAR fog:
	private final int FOGDISTCONST = 600;
	private final double FULLVIS_BEGIN = 2.9e30;
	private final double FULLVIS_END = 3.0e30;

	private int lastglpolygonmode = 0; // FUK
	private int glpolygonmode = 0; // 0:GL_FILL,1:GL_LINE,2:GL_POINT //FUK
	//private Texture polymosttext;
	private IntBuffer polymosttext;

	private float curpolygonoffset; // internal polygon offset stack for drawing flat sprites to avoid depth fighting

	//public static int gltexfiltermode = 0;

	// private int hicprecaching = 0;

	public static int drawingskybox = 0;

	private int shadescale_unbounded = 0;

	// private int r_usenewshading = 1;

	// Detail mapping cvar
	//private final OSDCVAR r_detailmapping;
	// Glow mapping cvar
	private int r_glowmapping = 1;

	// Vertex Array model drawing cvar
	private int r_vertexarrays = 1;

	// Vertex Buffer Objects model drawing cvars
	private int r_vbos = 1;
	private int r_npotwallmode;
	// model animation smoothing cvar
	public static int r_animsmoothing = 1;

	// line of sight checks before mddraw()
	// private int r_modelocclusionchecking = 0;

	// texture downsizing
	// is medium quality a good default?
//	private int r_downsize = 1; // FIXME Actually does not work
//	private int r_downsizevar = -1;

	private float fogresult;
	private float fogresult2;

	private FloatBuffer fogcol = BufferUtils.newFloatBuffer(4);
	private float fogtable[][] = new float[MAXPALOOKUPS][3];

	private double gyxscale, gxyaspect, gviewxrange, ghalfx, grhalfxdown10,
			grhalfxdown10x, ghoriz;
	private double gcosang, gsinang, gcosang2, gsinang2;
	private double gchang, gshang, gctang, gstang;
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

	private int vcnt, gtag;
	private final int VSPMAX = 4096; // <- careful!
	private vsptyp[] vsp = new vsptyp[VSPMAX];

	private int srepeat = 0, trepeat = 0;

	private final double SCISDIST = 1.0; // 1.0: Close plane clipping distance
	// private final int USEZBUFFER = 1; //1:use zbuffer (slow, nice sprite
	// rendering), 0:no zbuffer (fast, bad sprite rendering)
	// private final int LINTERPSIZ = 4; //log2 of interpolation size. 4:pretty
	// fast&acceptable quality, 0:best quality/slow!
	// private final int DEPTHDEBUG = 0; //1:render distance instead of texture,
	// for debugging only!, 0:default
	private final int CULL_DELAY = 2;

	private int lastcullcheck = 0;
	// private short[] cullmodel = new short[MAXSPRITES];
	// private int cullcheckcnt = 0;

	public static int glanisotropy = 1; // 0 = maximum supported by card
	// private int glusetexcompr = 1;
	// private int glusetexcache = 2;
	private int glmultisample, glnvmultisamplehint;
	// private int glwidescreen = 0, glprojectionhacks = 1;

	private final TextureCache textureCache;
	
	private int[] h_xsize = new int[MAXTILES], h_ysize = new int[MAXTILES];
	private byte[] h_xoffs = new byte[MAXTILES], h_yoffs = new byte[MAXTILES];

	private GL10 gl;
	private Engine engine;
	
	private boolean showlines = false;

	public Polymost(Engine engine, GL10 gl) {
		this.gl = gl;
		this.engine = engine;
		this.textureCache = createTextureCache(gl);
		for (int i = 0; i < VSPMAX; i++)
			vsp[i] = new vsptyp();
		wsprinfo = new Wallspriteinfo[MAXSPRITES];
		for (int i = 0; i < wsprinfo.length; i++)
			wsprinfo[i] = new Wallspriteinfo();

		init();
		
		OSDCOMMAND R_texture = new OSDCOMMAND( "r_texturemode", "r_texturemode: changes the texture filtering settings", new OSDCVARFUNC() { 
			@Override
			public void execute() {
				int gltexfiltermode = Console.Geti("r_texturemode");
				if (Console.osd_argc != 2) {
					Console.Println("Current texturing mode is " + gltexfiltermode);
					return;
				}
				try {
					int value = Integer.parseInt(osd_argv[1]);
					if(Console.Set("r_texturemode", value)) {
						gltexapplyprops();
						Console.Println("Texture filtering mode changed to " + value);
					} else Console.Println("Texture filtering mode out of range");
				} 
				catch(Exception e)
				{
					Console.Println("r_texturemode: Out of range");
				}
				/*
				if (Console.osd_argc != 2) {
					Console.Println("Current texturing mode is " + getGlFilter(gltexfiltermode).name);
					Console.Println("  Vaild modes are:");
					for (int m = 0; m < getGlFilterCount(); m++)
						Console.Println("     m" + " - " + getGlFilter(m).name);
					return;
				}

				string_t p = new string_t();
				int m = (int) Bstrtoul(Console.osd_argv[0], p, 10);
				if (p.var.equals(Console.osd_argv[0])) {
					// string
					for (int i = 0; i < getGlFilterCount(); i++) {
						if (Bstrcasecmp(Console.osd_argv[0], getGlFilter(i).name) == 0)
							break;
					}
					if (m == getGlFilterCount())
						m = gltexfiltermode; // no change
				} else {
					if (m < 0) {
						m = 0;
					} else if (m >= getGlFilterCount()) {
						m = getGlFilterCount() - 1;
					}
				}

				gltexfiltermode = m;
				gltexapplyprops();
				Console.Println("Texture filtering mode changed to " + getGlFilter(gltexfiltermode).name);
				*/
			} });
		R_texture.setRange(0, 5);
		Console.RegisterCvar(R_texture);
		
		Console.RegisterCvar(new  OSDCOMMAND( "r_detailmapping", "r_detailmapping: enable/disable detail mapping", 1, 0, 1));
		Console.RegisterCvar(new  OSDCOMMAND( "r_vbocount", "r_vbocount: sets the number of Vertex Buffer Objects to use when drawing models", 64, 0, 256));
		Console.Println(GLInfo.renderer + " " + GLInfo.version + " initialized", OSDTEXT_GOLD);
	}

	private TextureCache createTextureCache(GL10 gl) {
		return new TextureCache(gl, new ValueResolver<Integer>() {
			@Override
			public Integer get() {
				return anisotropy();
			}
		});
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
		clearskins();
	}

	public void gltexinvalidate8() {
		textureCache.invalidateall();
		clearskins();
	}

	public void clearskins() {
		for (int i = 0; i < MAXVOXELS; i++) {
			VOXModel v = voxmodels[i];
			if (v == null)
				continue;

			for (int j = 0; j < MAXPALOOKUPS; j++) {
				if (v.texid[j] != null) {
					gl.bglDeleteTextures(1, v.texid[j]);
					v.texid[j] = null;
				}
			}
		}
	}

	private int anisotropy() {
		if (GLInfo.maxanisotropy > 1.0) {
			if (glanisotropy <= 0 || glanisotropy > GLInfo.maxanisotropy)
				glanisotropy = (int) GLInfo.maxanisotropy;
		}
		return glanisotropy;
	}

	@Override
	public void gltexapplyprops() {
		int gltexfiltermode = Console.Geti("r_texturemode");
		textureCache.updateSettings(gltexfiltermode);

		if(models == null)
			return;
		
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i).mdnum < 2)
				continue;
			
			MDModel m = (MDModel) models.get(i);
			for (int j = 0; j < m.numskins * (HICEFFECTMASK + 1); j++) { 
				if (m.texid[j] == null)
					continue;
				bindTexture(gl, m.texid[j]);
				setupBoundTexture(gl, gltexfiltermode, anisotropy());
			}

			for (MDSkinmap sk = m.skinmap; sk != null; sk = sk.next) {
				for (int j = 0; j < (HICEFFECTMASK + 1); j++) {
					if (sk.texid[j] == null)
						continue;
					bindTexture(gl, sk.texid[j]);
					setupBoundTexture(gl, gltexfiltermode, anisotropy());
				}
			}
		}

	}

	public int gltexcacnum = -1;
	float glox1, gloy1, glox2, gloy2;

	@Override
	public void uninit() {
		for (int i = MAXPALOOKUPS - 1; i >= 0; i--) {
			fogtable[i][0] = palookupfog[i][0] / 63.f;
			fogtable[i][1] = palookupfog[i][1] / 63.f;
			fogtable[i][2] = palookupfog[i][2] / 63.f;
		}

		// Reset if this is -1 (meaning 1st texture call ever), or > 0 (textures
		// in memory)
		if (gltexcacnum < 0) {
			gltexcacnum = 0;

			// Hack for polymost_dorotatesprite calls before 1st
			// polymost_drawrooms()
			gcosang = gcosang2 = 16384 / 262144.0;

			gsinang = gsinang2 = 0.0;
		} else {
			textureCache.uninit();
			clearskins();
		}

		if (polymosttext != null) {
			//polymosttext.dispose();
			gl.bglDeleteTextures(1, polymosttext);
		}
		polymosttext = null;
		freevbos();

		//
		// Cachefile_Free();
		// polymost_cachesync();
	}

	@Override
	public void init() {
		GLInfo.init(gl);

		if (Bstrcmp(GLInfo.vendor, "NVIDIA Corporation") == 0) {
			gl.bglHint(GL_FOG_HINT, GL_NICEST);
		} else {
			gl.bglHint(GL_FOG_HINT, GL_DONT_CARE);
		}
		gl.bglFogi(GL_FOG_MODE, GL_LINEAR); // GL_EXP

		gl.bglBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		if (glmultisample > 0 && GLInfo.multisample != 0) {
			if (GLInfo.nvmultisamplehint != 0)
				gl.bglHint(GL_MULTISAMPLE_FILTER_HINT_NV,
						glnvmultisamplehint != 0 ? GL_NICEST : GL_FASTEST);
			gl.bglEnable(GL_MULTISAMPLE_ARB);
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
	
	private int glprojectionhacks = 1;
	public float get_projhack_ratio()
	{
	    if (glprojectionhacks != 0)
	    {
	        float mul = (float) (gshang * gshang);
	        return 1.05f + mul * mul * mul * mul;
	    }

	    // No projection hacks (legacy or new-aspect)
	    return 1.f;
	}

	public void resizeglcheck() // Ken Build method
	{
		// FUK
		if (lastglpolygonmode != glpolygonmode) {
			lastglpolygonmode = glpolygonmode;
			switch (glpolygonmode) {
			default:
			case 0:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				break;
			case 1:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
				break;
			case 2:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
				break;
			}
		}
		if (glpolygonmode != 0) // FUK
		{
			gl.bglClearColor(1.0, 1.0, 1.0, 0.0);
			gl.bglClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			gl.bglDisable(GL_TEXTURE_2D);
		}
		
		if ((glox1 != windowx1) || (gloy1 != windowy1) || (glox2 != windowx2) || (gloy2 != windowy2)) {
			int ourxdimen = (windowx2-windowx1+1);
	        float ratio = get_projhack_ratio();
	        int fovcorrect = (int) (ourxdimen*ratio - ourxdimen);

	        ratio = 1.f/ratio;
	        
	        glox1 = windowx1; gloy1 = windowy1;
			glox2 = windowx2; gloy2 = windowy2;

			gl.bglViewport(windowx1-(fovcorrect/2), ydim - (windowy2 + 1), ourxdimen+fovcorrect, windowy2 - windowy1 + 1);

			gl.bglMatrixMode(GL_PROJECTION);

			for (float[] row: matrix)
			    Arrays.fill(row, 0.0f);

			matrix[0][0] = ydimen * ratio;
			matrix[0][2] = 1.0f;
			matrix[1][1] = xdimen;
			matrix[1][2] = 1.0f;
			matrix[2][2] = 1.0f;
			matrix[2][3] = ydimen * ratio;
			matrix[3][2] = -1.0f;
		
			gl.bglLoadMatrixf(matrix);
			
			gl.bglMatrixMode(GL_MODELVIEW);
			gl.bglLoadIdentity();

			if (!nofog) gl.bglEnable(GL_FOG);
		}
		
	}
	/*
	public void resizeglcheck() // Ken Build method
	{
		for (float[] row: matrix)
		    Arrays.fill(row, 0.0f);

		// FUK
		if (lastglpolygonmode != glpolygonmode) {
			lastglpolygonmode = glpolygonmode;
			switch (glpolygonmode) {
			default:
			case 0:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				break;
			case 1:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
				break;
			case 2:
				gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
				break;
			}
		}
		if (glpolygonmode != 0) // FUK
		{
			gl.bglClearColor(1.0, 1.0, 1.0, 0.0);
			gl.bglClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			gl.bglDisable(GL_TEXTURE_2D);
		}

		if ((glox1 != windowx1) || (gloy1 != windowy1) || (glox2 != windowx2) || (gloy2 != windowy2)) {
			glox1 = windowx1;
			gloy1 = windowy1;
			glox2 = windowx2;
			gloy2 = windowy2;

			gl.bglViewport(windowx1, ydim - (windowy2 + 1), windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);

			gl.bglMatrixMode(GL_PROJECTION);

			matrix[0][0] = ydimen;
			matrix[0][2] = 1.0f;
			matrix[1][1] = xdimen;
			matrix[1][2] = 1.0f;
			matrix[2][2] = 1.0f;
			matrix[2][3] = ydimen;
			matrix[3][2] = -1.0f;
		
			gl.bglLoadMatrixf(matrix);
			// gluPerspective ( 90, (GLint)width/ (GLint)height, 0.0, 200.0 );

			gl.bglMatrixMode(GL_MODELVIEW);
			gl.bglLoadIdentity();

			if (!nofog)
				gl.bglEnable(GL_FOG);
		}
	}
	*/

	// (dpx,dpy) specifies an n-sided polygon. The polygon must be a convex
	// clockwise loop.
	// n must be <= 8 (assume clipping can double number of vertices)
	// method: 0:solid, 1:masked(255 is transparent), 2:transluscent #1,
	// 3:transluscent #2
	// +4 means it's a sprite, so wraparound isn't needed
	int pow2xsplit = 0;
	int skyclamphack = 0;
	private final double drawpoly_dd[] = new double[16],
			drawpoly_uu[] = new double[16], drawpoly_vv[] = new double[16],
			drawpoly_px[] = new double[16], drawpoly_py[] = new double[16];
	private final float drawpoly_pc[] = new float[4];

	private void drawpoly(double[] dpx, double[] dpy, int n, int method) {
		double ngdx = 0.0, ngdy = 0.0, ngdo = 0.0, ngux = 0.0, nguy = 0.0, nguo = 0.0;
		double ngvx = 0.0, ngvy = 0.0, ngvo = 0.0, dp, up, vp, du0 = 0.0, du1 = 0.0, dui, duj;
		double f, r, ox, oy, oz, ox2, oy2, oz2, uoffs;
		int i, j, k, nn, ix0, ix1, tsizx, tsizy, xx, yy;

		boolean dorot;

		Pthtyp pth, detailpth = null, glowpth = null;
		int texunits = GL_TEXTURE0_ARB;

		if (method == -1)
			return;

		if (n == 3) {
			if ((dpx[0] - dpx[1]) * (dpy[2] - dpy[1]) >= (dpx[2] - dpx[1])
					* (dpy[0] - dpy[1]))
				return; // for triangle
		} else {
			f = 0; // f is area of polygon / 2
			for (i = n - 2, j = n - 1, k = 0; k < n; i = j, j = k, k++)
				f += (dpx[i] - dpx[k]) * dpy[j];
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

		if (waloff[globalpicnum] == null) {
			engine.loadtile(globalpicnum);
			if (waloff[globalpicnum] == null) {
				tsizx = tsizy = 1;
				method = 1; // Hack to update Z-buffer for invalid mirror textures
			}
		}

		j = 0; dorot = ((gchang != 1.0) || (gctang != 1.0));
		if(dorot)
		{
			for (i = 0; i < n; i++) {
				ox = dpx[i] - ghalfx;
				oy = dpy[i] - ghoriz;
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
				drawpoly_dd[j] = (dpx[i] * gdx + dpy[i] * gdy + gdo) * r;
				drawpoly_uu[j] = (dpx[i] * gux + dpy[i] * guy + guo) * r;
				drawpoly_vv[j] = (dpx[i] * gvx + dpy[i] * gvy + gvo) * r;
	
				drawpoly_px[j] = ox * r + ghalfx;
				drawpoly_py[j] = oy * r + ghoriz;
				if ((j == 0) || (drawpoly_px[j] != drawpoly_px[j - 1]) || (drawpoly_py[j] != drawpoly_py[j - 1])) j++;
			}
		} 
		else	
	    {
	        for (i=0; i<n; i++)
	        {
	        	drawpoly_px[j] = dpx[i];
	        	drawpoly_py[j] = dpy[i];
	            if ((j==0) || (drawpoly_px[j] != drawpoly_px[j-1]) || (drawpoly_py[j] != drawpoly_py[j-1])) j++;
	        }
	    }
		while ((j >= 3) && (drawpoly_px[j - 1] == drawpoly_px[0])
				&& (drawpoly_py[j - 1] == drawpoly_py[0]))
			j--;
		if (j < 3)
			return;
		n = j;

		float hackscx, hackscy;

		if (skyclamphack != 0)
			method |= 4;

		pth = textureCache.cache(globalpicnum, globalpal, clampingMode(method), alphaMode(method));
		if(pth == null) //hires texture not found
			return;

		bindTexture(gl, pth.glpic);

		if (srepeat != 0)
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		if (trepeat != 0)
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		// texture scale by parkar request
		if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)) && drawingskybox == 0) {
			gl.bglMatrixMode(GL_TEXTURE);
			gl.bglLoadIdentity();
			gl.bglScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);
			gl.bglMatrixMode(GL_MODELVIEW);
		}

		// detail texture
		if (Console.Geti("r_detailmapping") != 0 && usehightile && drawingskybox == 0 && hicfindsubst(globalpicnum, DETAILPAL, 0) != null)
			detailpth = textureCache.cache(globalpicnum, DETAILPAL, clampingMode(method), alphaMode(method));

		if (GLInfo.multisample != 0 && detailpth != null && detailpth.hicr != null && (detailpth.hicr.palnum == DETAILPAL)) {
			gl.bglActiveTextureARB(++texunits);

			gl.bglEnable(GL_TEXTURE_2D);
			gl.bglBindTexture(GL_TEXTURE_2D, detailpth.glpic);

			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_MODULATE);

			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);

			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);

			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);

			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 2.0f);

			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

			f = detailpth != null ? detailpth.hicr.xscale : 1.0;

			gl.bglMatrixMode(GL_TEXTURE);
			gl.bglLoadIdentity();

			if (pth != null && pth.hicr != null && ((pth.hicr.xscale != 1.0f) || (pth.hicr.yscale != 1.0f)))
				gl.bglScalef(pth.hicr.xscale, pth.hicr.yscale, 1.0f);

			if (detailpth != null && detailpth.hicr != null && ((detailpth.hicr.xscale != 1.0f)
					|| (detailpth.hicr.yscale != 1.0f)))
				gl.bglScalef(detailpth.hicr.xscale, detailpth.hicr.yscale, 1.0f);

			gl.bglMatrixMode(GL_MODELVIEW);
		}
		
		if (r_glowmapping != 0 && usehightile && drawingskybox == 0 && hicfindsubst(globalpicnum, GLOWPAL, 0) != null)
			glowpth = textureCache.cache(globalpicnum, GLOWPAL, clampingMode(method), alphaMode(method));
		
		if (GLInfo.multisample != 0 && glowpth != null && glowpth.hicr != null && (glowpth.hicr.palnum == GLOWPAL))
		{
			gl.bglActiveTextureARB(++texunits);
		
			gl.bglEnable(GL_TEXTURE_2D);
			gl.bglBindTexture(GL_TEXTURE_2D, glowpth.glpic);
		
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_INTERPOLATE_ARB);
		
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);
		
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);
		
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE2_RGB_ARB, GL_TEXTURE);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND2_RGB_ARB, GL_ONE_MINUS_SRC_ALPHA);
		
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
			gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);
		
			gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
			gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
		}

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

		if (((method & 3) == 0)) {
			gl.bglDisable(GL_BLEND);
			gl.bglDisable(GL_ALPHA_TEST); // alpha_test
		} else {
			float al = 0.0f; // PLAG : default alphacut was 0.32 before goodalpha
			if (pth != null && pth.hicr != null && pth.hicr.alphacut >= 0.0)
				al = pth.hicr.alphacut;
			if (alphahackarray[globalpicnum] != 0)
				al = alphahackarray[globalpicnum];
			if (waloff[globalpicnum] == null)
				al = 0.0f; // invalid textures ignore the alpha cutoff settings

			gl.bglAlphaFunc(GL_GREATER, al);
			gl.bglEnable(GL_BLEND);
			gl.bglEnable(GL_ALPHA_TEST);
		}

        if (!dorot)
        {
            for (i=n-1; i>=0; i--)
            {
            	drawpoly_dd[i] = drawpoly_px[i]*gdx + drawpoly_py[i]*gdy + gdo;
            	drawpoly_uu[i] = drawpoly_px[i]*gux + drawpoly_py[i]*guy + guo;
            	drawpoly_vv[i] = drawpoly_px[i]*gvx + drawpoly_py[i]*gvy + gvo;
            }
        }
        
		f = ((float) (numshades - min(max(globalshade * shadescale, 0), numshades))) / ((float) numshades);
		if(globalpal == 1) drawpoly_pc[0] = drawpoly_pc[1] = drawpoly_pc[2] = 1; //Blood's pal 1
		else drawpoly_pc[0] = drawpoly_pc[1] = drawpoly_pc[2] = (float) f;
		
		switch (method & 3) {
		default:
		case 0:
			drawpoly_pc[3] = 1.0f;
			break;
		case 1:
			drawpoly_pc[3] = 1.0f;
			break;
		case 2:
			drawpoly_pc[3] = TRANSLUSCENT1;
			break;
		case 3:
			drawpoly_pc[3] = TRANSLUSCENT2;
			break;
		}

		// tinting happens only to hightile textures, and only if the
		// texture we're
		// rendering isn't for the same palette as what we asked for
		if ((hictinting[globalpal].f & 4) == 0) {
			if (pth != null && pth.isHighTile()) {
				if (pth.hicr.palnum != globalpal) {
					// apply tinting for replaced textures
					drawpoly_pc[0] *= (float) hictinting[globalpal].r / 255.0;
					drawpoly_pc[1] *= (float) hictinting[globalpal].g / 255.0;
					drawpoly_pc[2] *= (float) hictinting[globalpal].b / 255.0;
				}
				if (hictinting[MAXPALOOKUPS - 1].r != 255
						|| hictinting[MAXPALOOKUPS - 1].g != 255
						|| hictinting[MAXPALOOKUPS - 1].b != 255) {
					drawpoly_pc[0] *= (float) hictinting[MAXPALOOKUPS - 1].r / 255.0;
					drawpoly_pc[1] *= (float) hictinting[MAXPALOOKUPS - 1].g / 255.0;
					drawpoly_pc[2] *= (float) hictinting[MAXPALOOKUPS - 1].b / 255.0;
				}
			}
			// hack: this is for drawing the 8-bit crosshair recolored in
			// polymost
			else if ((hictinting[globalpal].f & 8) != 0) {
				drawpoly_pc[0] *= (float) hictinting[globalpal].r / 255.0;
				drawpoly_pc[1] *= (float) hictinting[globalpal].g / 255.0;
				drawpoly_pc[2] *= (float) hictinting[globalpal].b / 255.0;
			}
		}
		if (drunk && (method & 3) == 0) {
			gl.bglEnable(GL_BLEND);
			drawpoly_pc[3] = drunkIntensive;
		}

		gl.bglColor4f(drawpoly_pc[0], drawpoly_pc[1], drawpoly_pc[2], drawpoly_pc[3]);

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
				ox = drawpoly_py[1] - drawpoly_py[2];
				oy = drawpoly_py[2] - drawpoly_py[0];
				oz = drawpoly_py[0] - drawpoly_py[1];
				r = 1.0 / (ox * drawpoly_px[0] + oy * drawpoly_px[1] + oz
						* drawpoly_px[2]);
				ngdx = (ox * drawpoly_dd[0] + oy * drawpoly_dd[1] + oz
						* drawpoly_dd[2])
						* r;
				ngux = (ox * drawpoly_uu[0] + oy * drawpoly_uu[1] + oz
						* drawpoly_uu[2])
						* r;
				ngvx = (ox * drawpoly_vv[0] + oy * drawpoly_vv[1] + oz
						* drawpoly_vv[2])
						* r;
				ox = drawpoly_px[2] - drawpoly_px[1];
				oy = drawpoly_px[0] - drawpoly_px[2];
				oz = drawpoly_px[1] - drawpoly_px[0];
				ngdy = (ox * drawpoly_dd[0] + oy * drawpoly_dd[1] + oz
						* drawpoly_dd[2])
						* r;
				nguy = (ox * drawpoly_uu[0] + oy * drawpoly_uu[1] + oz
						* drawpoly_uu[2])
						* r;
				ngvy = (ox * drawpoly_vv[0] + oy * drawpoly_vv[1] + oz
						* drawpoly_vv[2])
						* r;
				ox = drawpoly_px[0] - .5;
				oy = drawpoly_py[0] - .5; // .5 centers texture nicely
				ngdo = drawpoly_dd[0] - ox * ngdx - oy * ngdy;
				nguo = drawpoly_uu[0] - ox * ngux - oy * nguy;
				ngvo = drawpoly_vv[0] - ox * ngvx - oy * ngvy;
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
				ox = drawpoly_px[i];
				oy = drawpoly_py[i];
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

			f = 1.0 / (double) tsizx;
			ix0 = (int) floor(du0 * f);
			ix1 = (int) floor(du1 * f);

			for (; ix0 <= ix1; ix0++) {
				du0 = (double) ((ix0) * tsizx);
				du1 = (double) ((ix0 + 1) * tsizx);

				i = 0;
				nn = 0;
				duj = (drawpoly_px[i] * ngux + drawpoly_py[i] * nguy + nguo)
						/ (drawpoly_px[i] * ngdx + drawpoly_py[i] * ngdy + ngdo);
				do {
					j = i + 1;
					if (j == n)
						j = 0;

					dui = duj;
					duj = (drawpoly_px[j] * ngux + drawpoly_py[j] * nguy + nguo)
							/ (drawpoly_px[j] * ngdx + drawpoly_py[j] * ngdy + ngdo);

					if ((du0 <= dui) && (dui <= du1)) {
						drawpoly_uu[nn] = drawpoly_px[i];
						drawpoly_vv[nn] = drawpoly_py[i];
						nn++;
					}
					if (duj <= dui) {
						if ((du1 < duj) != (du1 < dui)) {
							f = -(drawpoly_px[i] * (ngux - ngdx * du1)
									+ drawpoly_py[i] * (nguy - ngdy * du1) + (nguo - ngdo
											* du1))
									/ ((drawpoly_px[j] - drawpoly_px[i])
											* (ngux - ngdx * du1) + (drawpoly_py[j] - drawpoly_py[i])
													* (nguy - ngdy * du1));
							drawpoly_uu[nn] = (drawpoly_px[j] - drawpoly_px[i])
									* f + drawpoly_px[i];
							drawpoly_vv[nn] = (drawpoly_py[j] - drawpoly_py[i])
									* f + drawpoly_py[i];
							nn++;
						}
						if ((du0 < duj) != (du0 < dui)) {
							f = -(drawpoly_px[i] * (ngux - ngdx * du0)
									+ drawpoly_py[i] * (nguy - ngdy * du0) + (nguo - ngdo
											* du0))
									/ ((drawpoly_px[j] - drawpoly_px[i])
											* (ngux - ngdx * du0) + (drawpoly_py[j] - drawpoly_py[i])
													* (nguy - ngdy * du0));
							drawpoly_uu[nn] = (drawpoly_px[j] - drawpoly_px[i])
									* f + drawpoly_px[i];
							drawpoly_vv[nn] = (drawpoly_py[j] - drawpoly_py[i])
									* f + drawpoly_py[i];
							nn++;
						}
					} else {
						if ((du0 < duj) != (du0 < dui)) {
							f = -(drawpoly_px[i] * (ngux - ngdx * du0)
									+ drawpoly_py[i] * (nguy - ngdy * du0) + (nguo - ngdo
											* du0))
									/ ((drawpoly_px[j] - drawpoly_px[i])
											* (ngux - ngdx * du0) + (drawpoly_py[j] - drawpoly_py[i])
													* (nguy - ngdy * du0));
							drawpoly_uu[nn] = (drawpoly_px[j] - drawpoly_px[i])
									* f + drawpoly_px[i];
							drawpoly_vv[nn] = (drawpoly_py[j] - drawpoly_py[i])
									* f + drawpoly_py[i];
							nn++;
						}
						if ((du1 < duj) != (du1 < dui)) {
							f = -(drawpoly_px[i] * (ngux - ngdx * du1)
									+ drawpoly_py[i] * (nguy - ngdy * du1) + (nguo - ngdo
											* du1))
									/ ((drawpoly_px[j] - drawpoly_px[i])
											* (ngux - ngdx * du1) + (drawpoly_py[j] - drawpoly_py[i])
													* (nguy - ngdy * du1));
							drawpoly_uu[nn] = (drawpoly_px[j] - drawpoly_px[i])
									* f + drawpoly_px[i];
							drawpoly_vv[nn] = (drawpoly_py[j] - drawpoly_py[i])
									* f + drawpoly_py[i];
							nn++;
						}
					}
					i = j;
				} while (i != 0);
				if (nn < 3)
					continue;

				gl.bglBegin(GL_TRIANGLE_FAN);
				for (i = 0; i < nn; i++) {
					ox = drawpoly_uu[i];
					oy = drawpoly_vv[i];
					dp = ox * ngdx + oy * ngdy + ngdo;
					up = ox * ngux + oy * nguy + nguo;
					vp = ox * ngvx + oy * ngvy + ngvo;
					r = 1.0 / dp;
					if (texunits > GL_TEXTURE0_ARB) {
						j = GL_TEXTURE0_ARB;
						while (j <= texunits)
							gl.bglMultiTexCoord2dARB(j++, (up * r - du0 + uoffs) * ox2, vp * r * oy2);
					} else
						gl.bglTexCoord2d((up * r - du0 + uoffs) * ox2, vp * r * oy2);
					gl.bglVertex3d((ox - ghalfx) * r * grhalfxdown10x,
							(ghoriz - oy) * r * grhalfxdown10, r * (1.0 / 1024.0));
				}
				gl.bglEnd();
			}
		} else {
			ox2 *= hackscx;
			oy2 *= hackscy;
		
			gl.bglBegin(GL_TRIANGLE_FAN);
			for (i = 0; i < n; i++) {
				r = 1.0f / drawpoly_dd[i];
				if (texunits > GL_TEXTURE0_ARB) {
					j = GL_TEXTURE0_ARB;
					while (j <= texunits)
						gl.bglMultiTexCoord2dARB(j++, drawpoly_uu[i] * r * ox2, drawpoly_vv[i] * r * oy2);
				} else
					gl.bglTexCoord2d(drawpoly_uu[i] * r * ox2, drawpoly_vv[i] * r * oy2);

				gl.bglVertex3d((drawpoly_px[i] - ghalfx) * r * grhalfxdown10x, (ghoriz - drawpoly_py[i]) * r * grhalfxdown10, r * (1.f / 1024.f));
			}
			gl.bglEnd();

			if(showlines) {
				gl.bglDisable(GL_TEXTURE_2D);
				int[] p = new int[2];
				gl.bglColor4f(1, 1, 1, 1);
				gl.bglBegin(GL_LINES); 
				for (i = 1; i <= n; i++) { 
					p[0] = i-1; p[1] = i;
					if(i == n) { p[0] = i - 1; p[1] = 0; }
					for(int l = 0; l < 2; l++) {
						r = 1.0 / drawpoly_dd[p[l]]; 
						gl.bglVertex3d((drawpoly_px[p[l]] - ghalfx) * r * grhalfxdown10x, (ghoriz - drawpoly_py[p[l]]) * r * grhalfxdown10, r* (1.0 / 1024.0));
					}
				} 
				gl.bglEnd(); 
				gl.bglEnable(GL_TEXTURE_2D);
			}
		}

		if(GLInfo.multisample != 0) {
			while (texunits >= GL_TEXTURE0_ARB) {
				gl.bglActiveTextureARB(texunits);
				gl.bglMatrixMode(GL_TEXTURE);
				gl.bglLoadIdentity();
				gl.bglMatrixMode(GL_MODELVIEW);
				if (texunits > GL_TEXTURE0_ARB) {
					gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 1.0f);
					gl.bglDisable(GL_TEXTURE_2D);
				}
				texunits--;
			}
		}

		if (srepeat != 0)
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
					GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP);
		if (trepeat != 0)
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
					GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP);
	}

	/* Init viewport boundary (must be 4 point convex loop): // (px[0],py[0]).----.(px[1],py[1]) // / \ // / \ //(px[3],py[3]).--------------.(px[2],py[2]) */
	private void initmosts(double[] px, double[] py, int n) {
		int i, j, k, imin;

		vcnt = 1; // 0 is dummy solid node

		if (n < 3)
			return;
		imin = (px[1] < px[0]) ? 1 : 0;
		for (i = n - 1; i >= 2; i--)
			if (px[i] < px[imin])
				imin = i;

		vsp[vcnt].x = (float) px[imin];
		vsp[vcnt].cy[0] = vsp[vcnt].fy[0] = (float) py[imin];
		vcnt++;
		i = imin + 1;
		if (i >= n)
			i = 0;
		j = imin - 1;
		if (j < 0)
			j = n - 1;
		do {
			if (px[i] < px[j]) {
				if ((vcnt > 1) && (px[i] <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = (float) px[i];
				vsp[vcnt].cy[0] = (float) py[i];
				k = j + 1;
				if (k >= n)
					k = 0;
				vsp[vcnt].fy[0] = (float) ((px[i] - px[k]) * (py[j] - py[k])
						/ (px[j] - px[k]) + py[k]);
				vcnt++;
				i++;
				if (i >= n)
					i = 0;
			} else if (px[j] < px[i]) {
				if ((vcnt > 1) && (px[j] <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = (float) px[j];
				vsp[vcnt].fy[0] = (float) py[j];
				k = i - 1;
				if (k < 0)
					k = n - 1;
				// (px[k],py[k])
				// (px[j],?)
				// (px[i],py[i])
				vsp[vcnt].cy[0] = (float) ((px[j] - px[k]) * (py[i] - py[k])
						/ (px[i] - px[k]) + py[k]);
				vcnt++;
				j--;
				if (j < 0)
					j = n - 1;
			} else {
				if ((vcnt > 1) && (px[i] <= vsp[vcnt - 1].x))
					vcnt--;
				vsp[vcnt].x = (float) px[i];
				vsp[vcnt].cy[0] = (float) py[i];
				vsp[vcnt].fy[0] = (float) py[j];
				vcnt++;
				i++;
				if (i >= n)
					i = 0;
				if (i == j)
					break;
				j--;
				if (j < 0)
					j = n - 1;
			}
		} while (i != j);
		if (px[i] > vsp[vcnt - 1].x) {
			vsp[vcnt].x = (float) px[i];
			vsp[vcnt].cy[0] = vsp[vcnt].fy[0] = (float) py[i];
			vcnt++;
		}

		for (i = 0; i < vcnt; i++) {
			vsp[i].cy[1] = vsp[i + 1].cy[0];
			vsp[i].ctag = i;
			vsp[i].fy[1] = vsp[i + 1].fy[0];
			vsp[i].ftag = i;
			vsp[i].n = i + 1;
			vsp[i].p = i - 1;
		}
		vsp[vcnt - 1].n = 0;
		vsp[0].p = vcnt - 1;
		gtag = vcnt;

		// VSPMAX-1 is dummy empty node
		for (i = vcnt; i < VSPMAX; i++) {
			vsp[i].n = i + 1;
			vsp[i].p = i - 1;
		}
		
		vsp[VSPMAX - 1].n = vcnt;
		vsp[vcnt].p = VSPMAX - 1;
	}

	private void vsdel(int i) {
		int pi, ni;
		// Delete i
		pi = vsp[i].p;
		ni = vsp[i].n;
		vsp[ni].p = pi;
		vsp[pi].n = ni;

		// Add i to empty list
		vsp[i].n = vsp[VSPMAX - 1].n;
		vsp[i].p = VSPMAX - 1;
		vsp[vsp[VSPMAX - 1].n].p = i;
		vsp[VSPMAX - 1].n = i;
	}

	private int vsinsaft(int i) {
		int r;
		// i = next element from empty list
		r = vsp[VSPMAX - 1].n;
		vsp[vsp[r].n].p = VSPMAX - 1;
		vsp[VSPMAX - 1].n = vsp[r].n;

		vsp[r].set(vsp[i]); // copy i to r

		// insert r after i
		vsp[r].p = i;
		vsp[r].n = vsp[i].n;
		vsp[vsp[i].n].p = r;
		vsp[i].n = r;

		return (r);
	}

	private int testvisiblemost(float x0, float x1) {
		int i, newi;
		for (i = vsp[0].n; i != 0; i = newi) {
			newi = vsp[i].n;
			if ((x0 < vsp[newi].x) && (vsp[i].x < x1) && (vsp[i].ctag >= 0))
				return (1);
		}
		return (0);
	}

	private final double[] domost_dpx = new double[4],
			domost_dpy = new double[4];
	private final float[] domost_spx = new float[4], /*domost_spy = new float[4],*/
			domost_cy = new float[2], domost_cv = new float[2];
	private int domost_spt[] = new int[4];
	private int domostpolymethod = 0;
	private final float DOMOST_OFFSET = 0.01f;

	private void domost(float x0, float y0, float x1, float y1) {
		float d, f, n, t, slop, dx, dx0, dx1, nx, nx0, ny0, nx1, ny1;
		int i, j, k, z, ni, vcnt = 0, scnt, newi;

		boolean dir = (x0 < x1);
		
		if (dir) //clip dmost (floor)
	    {
	        y0 -= DOMOST_OFFSET;
	        y1 -= DOMOST_OFFSET;
	    }
		else //clip umost (ceiling)
		{
	        if (x0 == x1) return;
	        f = x0;
			x0 = x1;
			x1 = f;
			f = y0;
			y0 = y1;
			y1 = f;

	        y0 += DOMOST_OFFSET;
	        y1 += DOMOST_OFFSET; //necessary?
		}

		slop = (y1 - y0) / (x1 - x0);
		for (i = vsp[0].n; i != 0; i = newi) {
			newi = vsp[i].n;
			nx0 = vsp[i].x;
			nx1 = vsp[newi].x;
			if ((x0 >= nx1) || (nx0 >= x1) || (vsp[i].ctag <= 0))
				continue;
			dx = nx1 - nx0;
			domost_cy[0] = vsp[i].cy[0];
			domost_cv[0] = vsp[i].cy[1] - domost_cy[0];
			domost_cy[1] = vsp[i].fy[0];
			domost_cv[1] = vsp[i].fy[1] - domost_cy[1];

			scnt = 0;

			// Test if left edge requires split (x0,y0) (nx0,cy(0)),<dx,cv(0)>
			if ((x0 > nx0) && (x0 < nx1)) {
				t = (x0 - nx0) * domost_cv[dir?1:0] - (y0 - domost_cy[dir?1:0]) * dx;
				if (((!dir) && (t < 0)) || ((dir) && (t > 0))) {
					domost_spx[scnt] = x0;
//					domost_spy[scnt] = y0;
					domost_spt[scnt] = -1;
					scnt++;
				}
			}

			// Test for intersection on umost (j == 0) and dmost (j == 1)
			for (j = 0; j < 2; j++) {
				d = (y0 - y1) * dx - (x0 - x1) * domost_cv[j];
				n = (y0 - domost_cy[j]) * dx - (x0 - nx0) * domost_cv[j];
				if ((abs(n) <= abs(d)) && (d * n >= 0) && (d != 0)) {
					t = n / d;
					nx = (x1 - x0) * t + x0;
					if ((nx > nx0) && (nx < nx1)) {
						domost_spx[scnt] = nx;
//						domost_spy[scnt] = (y1 - y0) * t + y0;
						domost_spt[scnt] = j;
						scnt++;
					}
				}
			}

			// Nice hack to avoid full sort later :)
			if ((scnt >= 2) && (domost_spx[scnt - 1] < domost_spx[scnt - 2])) {
				f = domost_spx[scnt - 1];
				domost_spx[scnt - 1] = domost_spx[scnt - 2];
				domost_spx[scnt - 2] = f;
//				f = domost_spy[scnt - 1];
//				domost_spy[scnt - 1] = domost_spy[scnt - 2];
//				domost_spy[scnt - 2] = f;
				j = domost_spt[scnt - 1];
				domost_spt[scnt - 1] = domost_spt[scnt - 2];
				domost_spt[scnt - 2] = j;
			}

			// Test if right edge requires split
			if ((x1 > nx0) && (x1 < nx1)) {
				t = (x1 - nx0) * domost_cv[dir?1:0] - (y1 - domost_cy[dir?1:0]) * dx;
				if (((!dir) && (t < 0)) || ((dir) && (t > 0))) {
					domost_spx[scnt] = x1;
//					domost_spy[scnt] = y1;
					domost_spt[scnt] = -1;
					scnt++;
				}
			}

			vsp[i].tag = vsp[newi].tag = -1;
			for (z = 0; z <= scnt; z++, i = vcnt) {
				if (z < scnt) {
					vcnt = vsinsaft(i);
					t = (domost_spx[z] - nx0) / dx;
					vsp[i].cy[1] = t * domost_cv[0] + domost_cy[0];
					vsp[i].fy[1] = t * domost_cv[1] + domost_cy[1];
					vsp[vcnt].x = domost_spx[z];
					vsp[vcnt].cy[0] = vsp[i].cy[1];
					vsp[vcnt].fy[0] = vsp[i].fy[1];
					vsp[vcnt].tag = domost_spt[z];
				}

				ni = vsp[i].n;
				if (ni == 0)
					continue; // this 'if' fixes many bugs!
				dx0 = vsp[i].x;
				if (x0 > dx0)
					continue;
				dx1 = vsp[ni].x;
				if (x1 < dx1)
					continue;
				ny0 = (dx0 - x0) * slop + y0;
				ny1 = (dx1 - x0) * slop + y0;

				// dx0 dx1
				// ~ ~
				// ----------------------------
				// t0+=0 t1+=0
				// vsp[i].cy[0] vsp[i].cy[1]
				// ============================
				// t0+=1 t1+=3
				// ============================
				// vsp[i].fy[0] vsp[i].fy[1]
				// t0+=2 t1+=6
				//
				// ny0 ? ny1 ?

				k = 4;
				if ((vsp[i].tag == 0) || (ny0 <= vsp[i].cy[0]+DOMOST_OFFSET)) k--;
	            if ((vsp[i].tag == 1) || (ny0 >= vsp[i].fy[0]-DOMOST_OFFSET)) k++;
	            if ((vsp[ni].tag == 0) || (ny1 <= vsp[i].cy[1]+DOMOST_OFFSET)) k -= 3;
	            if ((vsp[ni].tag == 1) || (ny1 >= vsp[i].fy[1]-DOMOST_OFFSET)) k += 3;

				if (!dir) {
					switch (k) {
					case 1:
					case 2: 
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].cy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].cy[1];
						domost_dpx[2] = dx0;
						domost_dpy[2] = ny0;
						vsp[i].cy[0] = ny0;
						vsp[i].ctag = gtag;
						drawpoly(domost_dpx, domost_dpy, 3, domostpolymethod);
						break;
					case 3:
					case 6:
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].cy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].cy[1];
						domost_dpx[2] = dx1;
						domost_dpy[2] = ny1;
						drawpoly(domost_dpx, domost_dpy, 3, domostpolymethod);
						vsp[i].cy[1] = ny1;
						vsp[i].ctag = gtag;
						break;
					case 4:
					case 5:
					case 7:
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].cy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].cy[1];
						domost_dpx[2] = dx1;
						domost_dpy[2] = ny1;
						domost_dpx[3] = dx0;
						domost_dpy[3] = ny0;
						vsp[i].cy[0] = ny0;
						vsp[i].cy[1] = ny1;
						vsp[i].ctag = gtag;
						drawpoly(domost_dpx, domost_dpy, 4, domostpolymethod);
						break;
					case 8:
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].cy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].cy[1];
						domost_dpx[2] = dx1;
						domost_dpy[2] = vsp[i].fy[1];
						domost_dpx[3] = dx0;
						domost_dpy[3] = vsp[i].fy[0];
						vsp[i].ctag = vsp[i].ftag = -1;
						drawpoly(domost_dpx, domost_dpy, 4, domostpolymethod);
						break;
					default:
						break;
					}
				} else {
					switch (k) {
					case 7:
					case 6:
						domost_dpx[0] = dx0;
						domost_dpy[0] = ny0;
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].fy[1];
						domost_dpx[2] = dx0;
						domost_dpy[2] = vsp[i].fy[0];
						vsp[i].fy[0] = ny0;
						vsp[i].ftag = gtag;
						drawpoly(domost_dpx, domost_dpy, 3, domostpolymethod);
						break;
					case 5:
					case 2:
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].fy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = ny1;
						domost_dpx[2] = dx1;
						domost_dpy[2] = vsp[i].fy[1];
						vsp[i].fy[1] = ny1;
						vsp[i].ftag = gtag;
						drawpoly(domost_dpx, domost_dpy, 3, domostpolymethod);
						break;
					case 4:
					case 3:
					case 1:
						domost_dpx[0] = dx0;
						domost_dpy[0] = ny0;
						domost_dpx[1] = dx1;
						domost_dpy[1] = ny1;
						domost_dpx[2] = dx1;
						domost_dpy[2] = vsp[i].fy[1];
						domost_dpx[3] = dx0;
						domost_dpy[3] = vsp[i].fy[0];
						vsp[i].fy[0] = ny0;
						vsp[i].fy[1] = ny1;
						vsp[i].ftag = gtag;
						drawpoly(domost_dpx, domost_dpy, 4, domostpolymethod);
						break;
					case 0:
						domost_dpx[0] = dx0;
						domost_dpy[0] = vsp[i].cy[0];
						domost_dpx[1] = dx1;
						domost_dpy[1] = vsp[i].cy[1];
						domost_dpx[2] = dx1;
						domost_dpy[2] = vsp[i].fy[1];
						domost_dpx[3] = dx0;
						domost_dpy[3] = vsp[i].fy[0];
						vsp[i].ctag = vsp[i].ftag = -1;
						drawpoly(domost_dpx, domost_dpy, 4, domostpolymethod);
						break;
					default:
						break;
					}
				}
			}
		}

		gtag++;

		// Combine neighboring vertical strips with matching collinear
		// top&bottom edges
		// This prevents x-splits from propagating through the entire scan

		i = vsp[0].n;
		while (i != 0) {
			ni = vsp[i].n;
			if ((vsp[i].cy[0] >= vsp[i].fy[0])
					&& (vsp[i].cy[1] >= vsp[i].fy[1])) {
				vsp[i].ctag = vsp[i].ftag = -1;
			}
			if ((vsp[i].ctag == vsp[ni].ctag) && (vsp[i].ftag == vsp[ni].ftag)) {
				vsp[i].cy[1] = vsp[ni].cy[1];
				vsp[i].fy[1] = vsp[ni].fy[1];
				vsdel(ni);
			} else
				i = ni;
		}
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
		
		
		if ((globalorientation & (2 + 64)) == (2 + 64)) // Hack for panning for
		// slopes w/ relative
		// alignment
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
				nonparallaxed_py[2] = (engine.getflorzofslope((short) sectnum, (int) ox, (int) oy) - globalposz) * oy2 + ghoriz;
			else
				nonparallaxed_py[2] = (engine.getceilzofslope((short) sectnum,
						(int) ox, (int) oy) - globalposz) * oy2 + ghoriz;

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

			if ((globalorientation & 64) != 0) // Hack for relative alignment on
			// slopes
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
		domostpolymethod = (globalorientation >> 7) & 3;
		if (have_floor != 0) {
			if (globalposz >= engine.getflorzofslope((short) sectnum, globalposx,
					globalposy))
				domostpolymethod = -1; // Back-face culling
		} else {
			if (globalposz <= engine.getceilzofslope((short) sectnum, globalposx,
					globalposy))
				domostpolymethod = -1; // Back-face culling
		}

		if (!nofog)  // noparalaxed
			calc_and_apply_fog(globalpicnum, global_cf_shade, sec.visibility,  global_cf_pal);

		pow2xsplit = 0;
		if (have_floor != 0)
			domost(x0, cf_y0, x1, cf_y1); // flor
		else
			domost(x1, cf_y1, x0, cf_y0); // ceil

		domostpolymethod = 0;
	}
	
	// Are we using the mode that uploads non-power-of-two wall textures like they
	// render in classic?
	private boolean isnpotmode()
	{
	    // The glinfo.texnpot check is so we don't have to deal with that case in
	    // gloadtile_art().
	    return GLInfo.texnpot != 0 &&
	        // r_npotwallmode is NYI for hightiles. We require r_hightile off
	        // because in calc_ypanning(), the repeat would be multiplied by a
	        // factor even if no modified texture were loaded.
	        !usehightile &&
	        r_npotwallmode != 0;
	}
	
	private void calc_ypanning(int refposz, double ryp0, double ryp1,
			double x0, double x1, short ypan, short yrepeat, boolean dopancor) {
		double t0 = ((float) (refposz - globalposz)) * ryp0 + ghoriz;
		double t1 = ((float) (refposz - globalposz)) * ryp1 + ghoriz;
		double t = ((gdx * x0 + gdo) * (float) yrepeat) / ((x1 - x0) * ryp0 * 2048.f);
		int i = (1 << (picsiz[globalpicnum] >> 4));
		if (i < tilesizy[globalpicnum]) i <<= 1;

		if (isnpotmode())
	    {
	        t *= (float)tilesizy[globalpicnum] / i;
	        i = tilesizy[globalpicnum];
	    } else if (dopancor) {
			// Carry out panning "correction" to make it look like classic in some
	        // cases, but failing in the general case.
			int yoffs = (int) ((i - tilesizy[globalpicnum]) * (255.0f / i));
			if (ypan > 256 - yoffs) 
				ypan -= yoffs;
		}

		double fy = (float) ypan * ((float) i) / 256.0;
		gvx = (t0 - t1) * t;
		gvy = (x1 - x0) * t;
		gvo = -gvx * x0 - gvy * t0 + fy * gdo;
		gvx += fy * gdx;
		gvy += fy * gdy;
	}

	boolean fROR = false, cROR = false;
	
	private final double drawalls_dd[] = new double[3],
			drawalls_vv[] = new double[3], drawalls_ft[] = new double[4];
	private WALL drawalls_nwal = new WALL();

	private void drawalls(int bunch) { //XXX
		SECTOR sec, nextsec;
		WALL wal, wal2;
		double x0, x1, cy0, cy1, fy0, fy1, xp0, yp0, xp1, yp1, ryp0, ryp1, nx0, ny0, nx1, ny1;
		double t, t0, t1, ocy0, ocy1, ofy0, ofy1, oxp0, oyp0;
		double oguo, ogux, oguy;
		int i, x, y, z, wallnum, sectnum, nextsectnum;

		sectnum = thesector[bunchfirst[bunch]];
		sec = sector[sectnum];
		
		int fSector = -1, cSector = -1;
		
		if((sector[globalcursectnum].floorstat & 1024) != 0)
			if(globalcursectnum != sectnum)
				cSector = sectnum;
		
		if((sector[globalcursectnum].ceilingstat & 1024) != 0)
			if(globalcursectnum != sectnum)
				fSector = sectnum;
		
		if (!nofog)
			calc_and_apply_fog(sec.floorpicnum, sec.floorshade, sec.visibility,  sec.floorpal);

		for (z = bunchfirst[bunch]; z >= 0; z = p2[z]) {
			
			// DRAW WALLS SECTION!
			
			wallnum = thewall[z];
			wal = wall[wallnum];
			wal2 = wall[wal.point2];
			nextsectnum = wal.nextsector;
			nextsec = nextsectnum >= 0 ? sector[nextsectnum] : null;

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

			engine.getzsofslope((short) sectnum, (int) nx0, (int) ny0);
			cy0 = ((float) (ceilzsofslope - globalposz)) * ryp0 + ghoriz;
			fy0 = ((float) (floorzsofslope - globalposz)) * ryp0 + ghoriz;
			engine.getzsofslope((short) sectnum, (int) nx1, (int) ny1);
			cy1 = ((float) (ceilzsofslope - globalposz)) * ryp1 + ghoriz;
			fy1 = ((float) (floorzsofslope - globalposz)) * ryp1 + ghoriz;

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
					if(!cROR || sectnum != cSector) { 
						nonparallaxed(nx0, ny0, nx1, ny1, ryp0, ryp1,
							(float) x0, (float) x1, (float) fy0, (float) fy1, 1,
							sectnum, true);
					}
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
					if(!fROR || sectnum != fSector) {
						nonparallaxed(nx0, ny0, nx1, ny1, ryp0, ryp1,
							(float) x0, (float) x1, (float) cy0, (float) cy1, 0,
							sectnum, false);
					}
				} else if ((nextsectnum < 0) || ((sector[nextsectnum].ceilingstat & 1) == 0))
					drawbackground(sectnum, x0, x1, cy0, cy1, false);
				
			} //END DRAW CEILING

			gdx = (ryp0 - ryp1) * gxyaspect / (x0 - x1);
			gdy = 0;
			gdo = ryp0 * gxyaspect - gdx * x0;
			gux = (t0 * ryp0 - t1 * ryp1) * gxyaspect * (float) (wal.xrepeat & 0xFF) * 8.f / (x0 - x1);
			guo = t0 * ryp0 * gxyaspect * (float) (wal.xrepeat & 0xFF) * 8.f - gux * x0;
			guo += (float) wal.xpanning * gdo;
			gux += (float) wal.xpanning * gdx;
			guy = 0;
			ogux = gux;
			oguy = guy;
			oguo = guo;

			if (nextsectnum >= 0) {
				engine.getzsofslope((short) nextsectnum, (int) nx0, (int) ny0);
				ocy0 = ((float) (ceilzsofslope - globalposz)) * ryp0 + ghoriz;
				ofy0 = ((float) (floorzsofslope - globalposz)) * ryp0 + ghoriz;
				engine.getzsofslope((short) nextsectnum, (int) nx1, (int) ny1);
				ocy1 = ((float) (ceilzsofslope - globalposz)) * ryp1 + ghoriz;
				ofy1 = ((float) (floorzsofslope - globalposz)) * ryp1 + ghoriz;

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

					if (!nofog) {
						int shade = wal.shade;
						if(globalpal == 1 || sec.floorpal == 1) //Blood's pal 1
							shade = 0;
						calc_and_apply_fog(wal.picnum, shade, sec.visibility, sec.floorpal);
					}

					pow2xsplit = 1;
					domost((float) x1, (float) ocy1, (float) x0, (float) ocy0);
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
						t = (float) ((wal.xrepeat & 0xFF) * 8 + drawalls_nwal.xpanning * 2);
						gux = gdx * t - gux;
						guy = gdy * t - guy;
						guo = gdo * t - guo;
					}
					if ((drawalls_nwal.cstat & 256) != 0) {
						gvx = -gvx;
						gvy = -gvy;
						gvo = -gvo;
					} // yflip

					if (!nofog) {
						int shade = drawalls_nwal.shade;
						if(globalpal == 1 || sec.floorpal == 1) //Blood's pal 1
							shade = 0;
						calc_and_apply_fog(drawalls_nwal.picnum, shade, sec.visibility, sec.floorpal);
					}

					pow2xsplit = 1;
					domost((float) x0, (float) ofy0, (float) x1, (float) ofy1);
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
						t = (float) ((wal.xrepeat & 0xFF) * 8 + wal.xpanning * 2);
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

					if (!nofog) {
						int shade = wal.shade;
						if(globalpal == 1 || sec.floorpal == 1) //Blood's pal 1
							shade = 0;
						calc_and_apply_fog(wal.picnum, shade, sec.visibility, sec.floorpal);
					}
					pow2xsplit = 1;
					domost((float)x0, (float)cy0, (float)x1, (float)cy1);
				} while (false);
		
			}

			if (nextsectnum >= 0)
				if (((gotsector[nextsectnum >> 3] & pow2char[nextsectnum & 7]) == 0)
						&& (testvisiblemost((float) x0, (float) x1) != 0))
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
			for (i = b2f; dxb2[i] <= x1b1; i = p2[i]);
			return (wallfront(b1f, i));
		}
		for (i = b1f; dxb2[i] <= x1b2; i = p2[i]);
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
				if ((((spr.cstat & 0x8000) == 0) || (showinvisibility != 0))
						&& (spr.xrepeat > 0) && (spr.yrepeat > 0)) {
					xs = spr.x - globalposx;
					ys = spr.y - globalposy;
					if (((spr.cstat & 48) != 0)
							|| (xs * gcosang + ys * gsinang > 0)
							|| (usemodels && tile2model[spr.picnum] != null && tile2model[spr.picnum].modelid >= 0)) {
						if ((spr.cstat & (64 + 48)) != (64 + 16)
								|| dmulscale(sintable[(spr.ang + 512) & 2047],
										-xs, sintable[spr.ang & 2047], -ys, 6) > 0)
							if (engine.addtsprite(z) != 0)
								break;
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
						/*&& ((wal.cstat & 32) == 0)*/
						&& sectorbordercnt < sectorborder.length
						&& ((gotsector[nextsectnum >> 3] & pow2char[nextsectnum & 7]) == 0)) {
					d = (double) x1 * (double) y2 - (double) x2 * (double) y1;
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

			for (z = numscansbefore; z < numscans; z++)
				if ((wall[thewall[z]].point2 != thewall[p2[z]])
						|| (dxb2[z] > dxb1[p2[z]])) {
					bunchfirst[numbunches++] = p2[z];
					p2[z] = -1;
				}

			for (z = bunchfrst; z < numbunches; z++) {
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

		// multi-psky stuff
		for (int i = 0; i < pskynummultis; i++) {
			if (globalpicnum == pskymultilist[i]) {
				dapskybits = pskymultibits[i];
				dapskyoff = pskymultioff[i];
				break;
			}
		}

		// Use clamping for tiled sky textures
		for (int i = (1 << dapskybits) - 1; i > 0; i--)
			if (dapskyoff[i] != dapskyoff[i - 1]) {
				skyclamphack = r_parallaxskyclamping;
				break;
			}

		SECTOR sec = sector[sectnum];
		
		drawalls_dd[0] = (float) xdimen * .0000001; // Adjust sky depth based on screen size!
		t = (double) ((1 << (picsiz[globalpicnum] & 15)) << dapskybits);
		drawalls_vv[1] = drawalls_dd[0]
				* ((double) xdimscale * (double) viewingrange)
				/ (65536.0 * 65536.0);
		drawalls_vv[0] = drawalls_dd[0]
				* ((double) ((tilesizy[globalpicnum] >> 1) + parallaxyoffs))
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
		
		int oskyclamphack = skyclamphack;
		skyclamphack = 0;
		if(floor) {
			gvy = gdy; //(double) (tilesizy[globalpicnum] * gdy);
			gvo = gdo; //(double) (tilesizy[globalpicnum] * gdo);
			oy = (((double) tilesizy[globalpicnum]) * drawalls_dd[0] - drawalls_vv[0]) / drawalls_vv[1];
			
			if ((oy > y0) && (oy > y1)) {
				domost((float)x0,(float)oy,(float)x1,(float)oy);
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
					domost((float)x0,(float)oy,(float)ox,(float)oy);
					domost((float)ox,(float)oy,(float)x1,(float)y1); 
				}
				else 
				{ 
					domost((float)x0,(float)y0,(float)ox,(float)oy);
				 	domost((float)ox,(float)oy,(float)x1,(float)oy); 
				 }
			}
			else 
				domost((float)x0,(float)y0,(float)x1,(float)y1);
		}
		else  {
			gvy = gdy; //(double) (tilesizy[globalpicnum] * gdy);
			gvo = gdo; //(double) (tilesizy[globalpicnum] * gdo);
			oy = -drawalls_vv[0] / drawalls_vv[1];

			if ((oy < y0) && (oy < y1))
				domost((float)x1,(float)oy,(float)x0,(float)oy);
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
	                domost((float)ox,(float)oy,(float)x0,(float)oy);
	                domost((float)x1,(float)y1,(float)ox,(float)oy);
	            }
	            else
	            {
	                domost((float)ox,(float)oy,(float)x0,(float)y0);
	                domost((float)x1,(float)oy,(float)ox,(float)oy);
	            }
	        }
	        else
	            domost((float)x1,(float)y1,(float)x0,(float)y0);
		}
		skyclamphack = oskyclamphack;

		double panning = sec.ceilingypanning;
		if(floor) panning = sec.floorypanning;
		
		if (r_parallaxskypanning != 0)
			drawalls_vv[0] += drawalls_dd[0] * panning * ((double) i) / 256.0;
	
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

		int y = ((((int) ((x0 - ghalfx) * oy)) + globalang) >> (11 - dapskybits));
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
				domost((float) ox, (float) ((ox - x0) * r + y0), (float) fx, (float) ((fx - x0) * r + y0));
			else domost((float) fx, (float) ((fx - x0) * r + y0), (float) ox, (float) ((ox - x0) * r + y0));
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
					domost((float)_x0,(float)_fy0,(float)_x1,(float)_fy1);
				else if ((_fy0 > ny0) != (_fy1 > ny1))
				{
					t = (_fy0-ny0)/(ny1-ny0-_fy1+_fy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _fy0 + (_fy1-_fy0)*t;
					if (ny0 > _fy0) {
						domost((float)_x0,(float)ny0,(float)ox,(float)oy);
						domost((float)ox,(float)oy,(float)_x1,(float)_fy1); }
					else {
						domost((float)_x0,(float)_fy0,(float)ox,(float)oy);
						domost((float)ox,(float)oy,(float)_x1,(float)ny1); }
				}
				else
					domost((float)_x0,(float)ny0,(float)_x1,(float)ny1);
			} else {
				
				drawingskybox = 5; // ceiling/5th texture/index 4 of skybox

				if ((_cy0 < ny0) && (_cy1 < ny1))
					domost((float)_x1,(float)_cy1,(float)_x0,(float)_cy0);
				else if ((_cy0 < ny0) != (_cy1 < ny1))
				{
					t = (_cy0-ny0)/(ny1-ny0-_cy1+_cy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _cy0 + (_cy1-_cy0)*t;
					if (ny0 < _cy0) {
						domost((float)ox,(float)oy,(float)_x0,(float)ny0);
						domost((float)_x1,(float)_cy1,(float)ox,(float)oy); }
					else {
						domost((float)ox,(float)oy,(float)_x0,(float)_cy0);
						domost((float)_x1,(float)ny1,(float)ox,(float)oy); }
				}
				else
					domost((float)_x1,(float)ny1,(float)_x0,(float)ny0);
			}

			// wall of skybox

			drawingskybox = i + 1; // i+1th texture/index i of skybox

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
					domost((float)_x0,(float)_cy0,(float)_x1,(float)_cy1);
				else if ((_cy0 > ny0) != (_cy1 > ny1))
				{
					t = (_cy0-ny0)/(ny1-ny0-_cy1+_cy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _cy0 + (_cy1-_cy0)*t;
					if (ny0 > _cy0) {
						domost((float)_x0,(float)ny0,(float)ox,(float)oy);
						domost((float)ox,(float)oy,(float)_x1,(float)_cy1); }
					else {
						domost((float)_x0,(float)_cy0,(float)ox,(float)oy);
						domost((float)ox,(float)oy,(float)_x1,(float)ny1); }
				}
				else
					domost((float)_x0,(float)ny0,(float)_x1,(float)ny1);
			} else {
				if ((_fy0 < ny0) && (_fy1 < ny1))
					domost((float)_x1,(float)_fy1,(float)_x0,(float)_fy0);
				else if ((_fy0 < ny0) != (_fy1 < ny1))
				{
					t = (_fy0-ny0)/(ny1-ny0-_fy1+_fy0);
					ox = _x0 + (_x1-_x0)*t;
					oy = _fy0 + (_fy1-_fy0)*t;
					if (ny0 < _fy0) {
						domost((float)ox,(float)oy,(float)_x0,(float)ny0);
						domost((float)_x1,(float)_fy1,(float)ox,(float)oy); }
					else {
						domost((float)ox,(float)oy,(float)_x0,(float)_fy0);
				 		domost((float)_x1,(float)ny1,(float)ox,(float)oy); }
				}
				else
					domost((float)_x1,(float)ny1,(float)_x0,(float)ny0);
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
			domost((float) x0, (float) y0, (float) x1, (float) y1);
		else {
			gvx = -gvx;
			gvy = -gvy;
			gvo = -gvo; // y-flip skybox floor
			domost((float) x1, (float) y1, (float) x0, (float) y0);
		}

		skyclamphack = 0;

		drawingskybox = 0;
	}

	private void drawbackground(int sectnum, double x0, double x1, double y0, double y1, boolean floor) {
		// Parallaxing sky... hacked for Ken's mountain texture;

		SECTOR sec = sector[sectnum];
		int picnum = sec.floorpicnum;
		int shade = sec.floorshade;
		int pal = sec.floorpal;
		if(!floor)
		{
			picnum = sec.ceilingpicnum;
			shade = sec.ceilingshade;
			pal = sec.ceilingpal;
		}
		
		if (!nofog)  
			calc_and_apply_skyfog(picnum, shade, sec.visibility,  pal);

		if (!usehightile || hicfindsubst(globalpicnum, globalpal, 1) == null)
			drawpapersky(sectnum, x0, x1, y0, y1, floor);
		else
			drawskybox(x0, x1, y0, y1, floor);

		skyclamphack = 0;
		if (!nofog) 
			calc_and_apply_fog(picnum, shade, sec.visibility,  pal);
	}
	
	private final double[] drawrooms_px = new double[6],
			drawrooms_py = new double[6], drawrooms_pz = new double[6],
			drawrooms_px2 = new double[6], drawrooms_py2 = new double[6],
			drawrooms_pz2 = new double[6], drawrooms_sx = new double[6],
			drawrooms_sy = new double[6];

	public void drawrooms() // eduke32
	{
		int i, j, n, n2, closest;
		double ox, oy, oz, ox2, oy2, oz2, r;

		resizeglcheck();
		gl.bglClear(GL_DEPTH_BUFFER_BIT);
		gl.bglDisable(GL_BLEND);
		gl.bglEnable(GL_TEXTURE_2D);
		gl.bglEnable(GL_DEPTH_TEST);

//		Пока что лучший результат
//		gl.bglDepthFunc(GL10.GL_ALWAYS); // NEVER,LESS,(,L)EQUAL,GREATER,(NOT,G)EQUAL,ALWAYS
//		gl.bglPolygonOffset(0, 0);
		

//		gl.bglDepthRange(0.0001, 1.0); // <- this is more widely supported than glPolygonOffset
		gl.bglDepthFunc(GL_LEQUAL); // NEVER,LESS,(,L)EQUAL,GREATER,(NOT,G)EQUAL,ALWAYS
		gl.bglDepthRange(0.0, 1.0); //<- this is more widely supported than glPolygonOffset

		// Polymost supports true look up/down :) Here, we convert horizon to angle.
		// gchang&gshang are cos&sin of this angle (respectively)
		gyxscale = ((double) xdimenscale) / 131072.0;
		gxyaspect = ((double) xyaspect * (double) viewingrange) * (5.0 / (65536.0 * 262144.0));
		gviewxrange = ((double) viewingrange) * ((double) xdimen) / (32768.0 * 1024.0);
		gcosang = ((double) cosglobalang) / 262144.0;
		gsinang = ((double) singlobalang) / 262144.0;
		gcosang2 = gcosang * ((double) viewingrange) / 65536.0;

		gsinang2 = gsinang * ((double) viewingrange) / 65536.0;
		ghalfx = (double) halfxdimen;
		grhalfxdown10 = 1.0 / (((double) ghalfx) * 1024); //viewport
		ghoriz = (double) globalhoriz;

		// global cos/sin height angle
		r = (double) ((ydimen >> 1) - ghoriz);
		gshang = r / sqrt(r * r + ghalfx * ghalfx);
		gchang = sqrt(1.0 - gshang * gshang);
		ghoriz = (double) (ydimen >> 1);

		// global cos/sin tilt angle
		gctang = cos(gtang);
		gstang = sin(gtang);

		if (abs(gstang) < .001) // This hack avoids nasty precision bugs in domost()
		{
			gstang = 0;
			if (gctang > 0)
				gctang = 1.0;
			else
				gctang = -1.0;
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
			if (j >= n)
				j = 0;
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
		initmosts(drawrooms_sx, drawrooms_sy, n2);

		numscans = numbunches = 0;

		// MASKWALL_BAD_ACCESS
		// Fixes access of stale maskwall[maskwallcnt] (a "scan" index, in BUILD lingo):
		maskwallcnt = 0;
		cROR = false; fROR = false; //I think it's a bad method, temporary solution I hope.
		if (globalcursectnum >= MAXSECTORS) {
			globalcursectnum -= MAXSECTORS;
			if((sector[globalcursectnum].floorstat & 1024) != 0)
				cROR = true;
			if((sector[globalcursectnum].ceilingstat & 1024) != 0)
				fROR = true;
		}
		else {
			i = globalcursectnum;
			globalcursectnum = engine.updatesector(globalposx, globalposy, globalcursectnum);
			if (globalcursectnum < 0)
				globalcursectnum = (short) i;
		}

		polymost_scansector(globalcursectnum);
		
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
	}

	private final double[] drawmaskwall_dpx = new double[8],
			drawmaskwall_dpy = new double[8],
			drawmaskwall_dpx2 = new double[8],
			drawmaskwall_dpy2 = new double[8];
	private final float[] drawmaskwall_csy = new float[4],
			drawmaskwall_fsy = new float[4];
	private final int[] drawmaskwall_cz = new int[4],
			drawmaskwall_fz = new int[4];

	public void drawmaskwall(int damaskwallcnt) {
		float x0, x1, sx0, sy0, sx1, sy1, xp0, yp0, xp1, yp1, oxp0, oyp0, ryp0, ryp1;
		float r, t, t0, t1;
		int i, j, n, n2, z, sectnum, method;

		int m0, m1;
		SECTOR sec, nsec;
		WALL wal, wal2;
		
		gl.bglDepthRange(0.0, 0.99999);

		// cullcheckcnt = 0;

		z = maskwall[damaskwallcnt];
		wal = wall[thewall[z]];
		wal2 = wall[wal.point2];
		sectnum = thesector[z];
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
		engine.getzsofslope((short) sectnum, m0, m1);
		drawmaskwall_cz[0] = ceilzsofslope;
		drawmaskwall_fz[0] = floorzsofslope;
		engine.getzsofslope(wal.nextsector, m0, m1);
		drawmaskwall_cz[1] = ceilzsofslope;
		drawmaskwall_fz[1] = floorzsofslope;
		m0 = (int) ((wal2.x - wal.x) * t1 + wal.x);
		m1 = (int) ((wal2.y - wal.y) * t1 + wal.y);
		engine.getzsofslope((short) sectnum, m0, m1);
		drawmaskwall_cz[2] = ceilzsofslope;
		drawmaskwall_fz[2] = floorzsofslope;
		engine.getzsofslope(wal.nextsector, m0, m1);
		drawmaskwall_cz[3] = ceilzsofslope;
		drawmaskwall_fz[3] = floorzsofslope;

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
		pow2xsplit = 1;
		if ((wal.cstat & 128) != 0) { // FIXME
			if ((wal.cstat & 512) == 0)
				method = 2;
			else
				method = 3;
		}

		if (!nofog) {
			int shade = wal.shade;
			if(globalpal == 1 || sec.floorpal == 1) //Blood's pal 1
				shade = 0;
			calc_and_apply_fog(wal.picnum, shade, sec.visibility, sec.floorpal);
		}
		
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

		drawmaskwall_dpx[0] = x0;
		drawmaskwall_dpy[0] = drawmaskwall_csy[1];
		drawmaskwall_dpx[1] = x1;
		drawmaskwall_dpy[1] = drawmaskwall_csy[3];
		drawmaskwall_dpx[2] = x1;
		drawmaskwall_dpy[2] = drawmaskwall_fsy[3];
		drawmaskwall_dpx[3] = x0;
		drawmaskwall_dpy[3] = drawmaskwall_fsy[1];
		n = 4;

		// Clip to (x0,csy[0])-(x1,csy[2])
		n2 = 0;
		t1 = (float) -((drawmaskwall_dpx[0] - x0)
				* (drawmaskwall_csy[2] - drawmaskwall_csy[0]) - (drawmaskwall_dpy[0] - drawmaskwall_csy[0])
						* (x1 - x0));
		for (i = 0; i < n; i++) {
			j = i + 1;
			if (j >= n)
				j = 0;

			t0 = t1;
			t1 = (float) -((drawmaskwall_dpx[j] - x0)
					* (drawmaskwall_csy[2] - drawmaskwall_csy[0]) - (drawmaskwall_dpy[j] - drawmaskwall_csy[0])
							* (x1 - x0));
			if (t0 >= 0) {
				drawmaskwall_dpx2[n2] = drawmaskwall_dpx[i];
				drawmaskwall_dpy2[n2] = drawmaskwall_dpy[i];
				n2++;
			}
			if ((t0 >= 0) != (t1 >= 0)) {
				r = t0 / (t0 - t1);
				drawmaskwall_dpx2[n2] = (drawmaskwall_dpx[j] - drawmaskwall_dpx[i])
						* r + drawmaskwall_dpx[i];
				drawmaskwall_dpy2[n2] = (drawmaskwall_dpy[j] - drawmaskwall_dpy[i])
						* r + drawmaskwall_dpy[i];
				n2++;
			}
		}
		if (n2 < 3)
			return;

		// Clip to (x1,fsy[2])-(x0,fsy[0])
		n = 0;
		t1 = (float) -((drawmaskwall_dpx2[0] - x1)
				* (drawmaskwall_fsy[0] - drawmaskwall_fsy[2]) - (drawmaskwall_dpy2[0] - drawmaskwall_fsy[2])
						* (x0 - x1));
		for (i = 0; i < n2; i++) {
			j = i + 1;
			if (j >= n2)
				j = 0;

			t0 = t1;
			t1 = (float) -((drawmaskwall_dpx2[j] - x1)
					* (drawmaskwall_fsy[0] - drawmaskwall_fsy[2]) - (drawmaskwall_dpy2[j] - drawmaskwall_fsy[2])
							* (x0 - x1));
			if (t0 >= 0) {
				drawmaskwall_dpx[n] = drawmaskwall_dpx2[i];
				drawmaskwall_dpy[n] = drawmaskwall_dpy2[i];
				n++;
			}
			if ((t0 >= 0) != (t1 >= 0)) {
				r = t0 / (t0 - t1);
				drawmaskwall_dpx[n] = (drawmaskwall_dpx2[j] - drawmaskwall_dpx2[i])
						* r + drawmaskwall_dpx2[i];
				drawmaskwall_dpy[n] = (drawmaskwall_dpy2[j] - drawmaskwall_dpy2[i])
						* r + drawmaskwall_dpy2[i];
				n++;
			}
		}
		if (n < 3)
			return;

		drawpoly(drawmaskwall_dpx, drawmaskwall_dpy, n, method);
		gl.bglDepthRange(0.0, 1.0);
	}

//	private static int findwall_dist;
	private static Vector2 projPoint = new Vector2();
//	private int polymost_findwall(SPRITE tspr, int tsizx, int tsizy, int rd)
//	{
//		findwall_dist = rd;
//	    int dist = 4, closest = -1, dst;
//	    SECTOR sect = sector[tspr.sectnum];
//
//	    
//	    for (int i=sect.wallptr; i<sect.wallptr + sect.wallnum; i++)
//	    {
//	    	if ((wall[i].nextsector == -1 || ((sector[wall[i].nextsector].ceilingz > (tspr.z - ((tsizy * tspr.yrepeat) << 2))) ||
//	                sector[wall[i].nextsector].floorz < tspr.z)) && getclosestpointonwall(tspr, i, projPoint) == 0)
//	        {
//	            dst = (int) (klabs((int)(tspr.x - projPoint.x)) + klabs((int)(tspr.y - projPoint.y)));
//
//	            if (dst <= dist)
//	            {
//	                dist = dst;
//	                closest = i;
//	            }
//	        }
//	    }
//
//	    findwall_dist = dist;
//	    return closest;
//	}

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
	private float TSPR_OFFSET(SPRITE tspr)
	{
		float dist = sepdist(globalposx - tspr.x, globalposy - tspr.y, globalposz - tspr.z);
		float offset = (TSPR_OFFSET_FACTOR + ((tspr.owner != -1 ? tspr.owner & 61 : 1) * TSPR_OFFSET_FACTOR)) * dist * 0.025f;
		return offset;
	}
	
	// dz: in Build coordinates
	private int sepdist(int x, int y, int z)
	{
		int dx = (int) klabs(x);
		int dy = (int) klabs(y);
		int dz = (int) klabs(z);
	
	    if (dx < dy) {
	    	int tmp = dx;
	    	dx = dy;
	    	dy = tmp;
	    }

	    if (dx < dz) {
	    	int tmp = dx;
	    	dx = dz;
	    	dz = tmp;
	    }

	    dy += dz;

	    return dx - (dx>>4) + (dy>>2) + (dy>>3);
	}


	private final double[] drawsprite_px = new double[6],
			drawsprite_py = new double[6];
	private final float drawsprite_ft[] = new float[4],
			drawsprite_px2[] = new float[6], drawsprite_py2[] = new float[6];

	public void drawsprite(int snum) {
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

		if(voxrotate[globalpicnum])	
			tspr.ang = (short) ((8 * totalclock) & 0x7FF);
		
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
		
		if (!nofog) {
			int shade = (int) (globalshade / 1.5f);
			if(tspr.pal == 5 && tspr.shade == 127)
				shade = 0; //Blood's shadows (for pal 1)
			if(globalpal == 1 || tspr.pal == 1) //Blood's pal 1
				shade = 0;
			calc_and_apply_fog(tspr.picnum, shade, sector[tspr.sectnum].visibility, sector[tspr.sectnum].floorpal);
		}
		
		while ((spriteext[tspr.owner].flags & SPREXT_NOTMD) == 0) {

			if (usemodels && tile2model[Ptile2tile(tspr.picnum, tspr.pal)] != null &&
					tile2model[Ptile2tile(tspr.picnum, tspr.pal)].modelid >= 0 &&
					tile2model[Ptile2tile(tspr.picnum, tspr.pal)].framenum >= 0) {
				if (tspr.owner < 0 || tspr.owner >= MAXSPRITES /* || tspr.statnum == TSPR_MIRROR */ ) {
					if (mddraw(tspr, xoff, yoff) != 0)
						return;
					break; // else, render as flat sprite
				}

				if (mddraw(tspr, xoff, yoff) != 0)
					return;
				break; // else, render as flat sprite
			}
			
			if (usevoxels && (tspr.cstat & 48) != 48 && tiletovox[globalpicnum] >= 0 && voxmodels[tiletovox[globalpicnum]] != null) {
				if (voxdraw(voxmodels[tiletovox[globalpicnum]], tspr, xoff, yoff) != 0)
					return;
				break; // else, render as flat sprite
			}

			if ((tspr.cstat & 48) == 48 && voxmodels[globalpicnum] != null) {
				voxdraw(voxmodels[globalpicnum], tspr, xoff, yoff);
				return;
			}
			break;
		}
		
//		if ((((tspr.cstat&2) != 0) || (textureCache.gltexmayhavealpha(tspr.picnum,tspr.pal))))
//	    {
//	        curpolygonoffset += 0.01f;
//	        gl.bglPolygonOffset(-curpolygonoffset, -curpolygonoffset);
//	    }
		
//		if ((tspr.cstat & 32) != 0) {
//			curpolygonoffset += 0.01f;
//		    gl.bglPolygonOffset(-curpolygonoffset, -curpolygonoffset);
//		}
		
		posx = tspr.x;
		posy = tspr.y;

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

		float foffs, offsx, offsy;
		int ang;
		switch ((globalorientation >> 4) & 3) {
		case 0: // Face sprite
			// Project 3D to 2D
			if ((globalorientation & 4) != 0)
				xoff = -xoff;
			// NOTE: yoff not negated not for y flipping, unlike wall and floor
			// aligned sprites.
			
			ang = (engine.getangle(tspr.x - globalposx, tspr.y - globalposy) + 1024) & 2047;
			foffs = TSPR_OFFSET(tspr);
			
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
			if ((tsizy & 1) != 0)
				sy0 += fy * 0.5f;

			fx *= ((float) tsizx);
			fy *= ((float) tsizy);

			drawsprite_px[0] = drawsprite_px[3] = sx0 - fx * .5;
			drawsprite_px[1] = drawsprite_px[2] = sx0 + fx * .5;
			if ((globalorientation & 128) == 0) {
				drawsprite_py[0] = drawsprite_py[1] = sy0 - fy;
				drawsprite_py[2] = drawsprite_py[3] = sy0;
			} else {
				drawsprite_py[0] = drawsprite_py[1] = sy0 - fy * .5;
				drawsprite_py[2] = drawsprite_py[3] = sy0 + fy * .5;
			}

			gdx = gdy = guy = gvx = 0;
			gdo = ryp0 * gviewxrange;
			if ((globalorientation & 4) == 0) {
				gux = (float) tsizx * gdo
						/ (drawsprite_px[1] - drawsprite_px[0] + .002);
				guo = -gux * (drawsprite_px[0] - .001);
			} else {
				gux = (float) tsizx * gdo
						/ (drawsprite_px[0] - drawsprite_px[1] - .002);
				guo = -gux * (drawsprite_px[1] + .001);
			}
			if ((globalorientation & 8) == 0) {
				gvy = (float) tsizy * gdo / (drawsprite_py[3] - drawsprite_py[0] + .002);
				gvo = -gvy * (drawsprite_py[0] - .001);
			} else {
				gvy = (float) tsizy * gdo / (drawsprite_py[0] - drawsprite_py[3] - .002);
				gvo = -gvy * (drawsprite_py[3] + .001);
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
				if (drawsprite_py[0] < sy0)
					drawsprite_py[0] = drawsprite_py[1] = sy0;
			}
			if ((sector[tspr.sectnum].floorstat & 3) == 0) {
				sy0 = (float) (((sector[tspr.sectnum].floorz - globalposz))
						* gyxscale * ryp0 + ghoriz);
				if (drawsprite_py[2] > sy0)
					drawsprite_py[2] = drawsprite_py[3] = sy0;
			}

			tilesizx[globalpicnum] = (short) tsizx;
			tilesizy[globalpicnum] = (short) tsizy;

			pow2xsplit = 0;
			drawpoly(drawsprite_px, drawsprite_py, 4, method);

			srepeat = 0;
			trepeat = 0;

			break;
	
		case 1: // Wall sprite

			curpolygonoffset += 0.01f;
	        gl.bglPolygonOffset(-curpolygonoffset, -curpolygonoffset);
	        
			// Project 3D to 2D
			if ((globalorientation & 4) != 0)
				xoff = -xoff;
			if ((globalorientation & 8) != 0)
				yoff = -yoff;

//			ang = (getangle(tspr.x - globalposx, tspr.y - globalposy) + 1024) & 2047;
//			float dist = sepdist(globalposx - tspr.x, globalposy - tspr.y, 0);
//			float WALLSPR_OFFSET_FACTOR = 0.008f;
//			if(dist > 4096) dist = 4096;
//			foffs = 0.05f + (tspr.owner & 61) * 0.25f; //TSPR_OFFSET(tspr); //(WALLSPR_OFFSET_FACTOR + ((tspr.owner != -1 ? tspr.owner & 61 : 1) * WALLSPR_OFFSET_FACTOR)) * dist * 0.1f;
//			offsx = (foffs * sintable[(ang + 512) & 2047]) / 16384.f;
//			offsy = (foffs * sintable[ang]) / 16384.f;

			xv = (float) tspr.xrepeat * (float) sintable[(tspr.ang) & 2047] * (1.0f / 65536.f);
			yv = (float) tspr.xrepeat * (float) sintable[(tspr.ang + 1536) & 2047] * (1.0f / 65536.f);
			f = (float) (tsizx >> 1) + (float) xoff;
			x0 = (float) (posx - globalposx /*+ offsx*/) - xv * f;
			x1 = xv * (float) tsizx + x0;
			y0 = (float) (posy - globalposy /*+ offsy*/) - yv * f;
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

			drawsprite_px[0] = sx0;
			drawsprite_py[0] = sc0;
			drawsprite_px[1] = sx1;
			drawsprite_py[1] = sc1;
			drawsprite_px[2] = sx1;
			drawsprite_py[2] = sf1;
			drawsprite_px[3] = sx0;
			drawsprite_py[3] = sf0;

			tilesizx[globalpicnum] = (short) tsizx;
			tilesizy[globalpicnum] = (short) tsizy;

			pow2xsplit = 0;
			drawpoly(drawsprite_px, drawsprite_py, 4, method);

			srepeat = 0;
			trepeat = 0;
			
			gl.bglPolygonOffset(0,0);

			break;
		
		case 2: // Floor sprite

			if ((globalorientation & 64) != 0)
				if ((globalposz > tspr.z) == ((globalorientation & 8) == 0))
					return;
			if ((globalorientation & 4) > 0)
				xoff = -xoff;
			if ((globalorientation & 8) > 0)
				yoff = -yoff;
			
			if ((tspr.z - sector[tspr.sectnum].ceilingz) < (sector[tspr.sectnum].floorz - tspr.z))
				tspr.z += (tspr.owner & 31);
			else
				tspr.z -= (tspr.owner & 31);

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

				drawsprite_px[j] = sy0 * gcosang - sx0 * gsinang;
				drawsprite_py[j] = sx0 * gcosang2 + sy0 * gsinang2;
			}

			if (tspr.z < globalposz) // if floor sprite is above you, reverse order of points
			{
				f = (float) drawsprite_px[0];
				drawsprite_px[0] = drawsprite_px[1];
				drawsprite_px[1] = f;
				f = (float) drawsprite_py[0];
				drawsprite_py[0] = drawsprite_py[1];
				drawsprite_py[1] = f;
				f = (float) drawsprite_px[2];
				drawsprite_px[2] = drawsprite_px[3];
				drawsprite_px[3] = f;
				f = (float) drawsprite_py[2];
				drawsprite_py[2] = drawsprite_py[3];
				drawsprite_py[3] = f;
			}

			// Clip to SCISDIST plane
			npoints = 0;
			for (i = 0; i < 4; i++) {
				j = ((i + 1) & 3);
				if (drawsprite_py[i] >= SCISDIST) {
					drawsprite_px2[npoints] = (float) drawsprite_px[i];
					drawsprite_py2[npoints] = (float) drawsprite_py[i];
					npoints++;
				}
				if ((drawsprite_py[i] >= SCISDIST) != (drawsprite_py[j] >= SCISDIST)) {
					f = (float) ((SCISDIST - drawsprite_py[i]) / (drawsprite_py[j] - drawsprite_py[i]));
					drawsprite_px2[npoints] = (float) ((drawsprite_px[j] - drawsprite_px[i]) * f + drawsprite_px[i]);
					drawsprite_py2[npoints] = (float) ((drawsprite_py[j] - drawsprite_py[i]) * f + drawsprite_py[i]);
					npoints++;
				}
			}

			if (npoints < 3)
				return;

			// Project rotated 3D points to screen
			SECTOR sec = sector[tspr.sectnum];
			float fadjust = 0;

             // unfortunately, offsetting by only 1 isn't enough on most Android devices
            if (tspr.z == sec.ceilingz || tspr.z == sec.ceilingz + 1) {
            	tspr.z = sec.ceilingz + 2; fadjust = (tspr.owner & 31); }

            if (tspr.z == sec.floorz || tspr.z == sec.floorz - 1) {
                tspr.z = sec.floorz - 2; fadjust = -(tspr.owner & 31); }

			f = (float) ((tspr.z - globalposz + fadjust)*gyxscale);
			for (j = 0; j < npoints; j++) {
				ryp0 = 1 / drawsprite_py2[j];
				drawsprite_px[j] = ghalfx * drawsprite_px2[j] * ryp0 + ghalfx;
				drawsprite_py[j] = f * ryp0 + ghoriz;
			}

			// gd? Copied from floor rendering code
			gdx = 0;
			gdy = gxyaspect / (double) (tspr.z - globalposz + fadjust);
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

			pow2xsplit = 0;
			drawpoly(drawsprite_px, drawsprite_py, npoints, method);

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

	private final double dorotatesprite_px[] = new double[8],
			dorotatesprite_py[] = new double[8],
			dorotatesprite_px2[] = new double[8],
			dorotatesprite_py2[] = new double[8];
	private final float[][] matrix = new float[4][4];
	private final SPRITE hudsprite = new SPRITE();

	public void dorotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2,
			int cy2, int uniqid) {
		
		int xoff, yoff, xsiz, ysiz, method;
		int ogpicnum, ogshade, ogpal;
		double ogchang, ogshang, ogctang, ogstang, oghalfx, oghoriz;
		double ogrhalfxdown10, ogrhalfxdown10x;
		double d, cosang, sinang, cosang2, sinang2;

		for (float[] row: matrix)
		    Arrays.fill(row, 0.0f);

		int ourxyaspect = 0;
		
		if (usemodels && hudmem != null && hudmem[(dastat&4)>>2][picnum].angadd != 0)
	    {
	        int tilenum = Ptile2tile(picnum,dapalnum);

	        if (tile2model[tilenum].modelid >= 0 &&
	            tile2model[tilenum].framenum >= 0)
	        {
	            int oldviewingrange;
	            double ogxyaspect;
	            double x1, y1, z1;
	            hudsprite.reset((byte)0);

	            if ((hudmem[(dastat&4)>>2][picnum].flags&1) != 0) return; //"HIDE" is specified in DEF

	            ogchang = gchang; gchang = 1.0;
	            ogshang = gshang; gshang = 0.0; d = (double)z/(65536.0*16384.0);
	            ogctang = gctang; gctang = (double)sintable[(a+512)&2047]*d;
	            ogstang = gstang; gstang = (double)sintable[a&2047]*d;
	            ogshade  = (int) globalshade;  globalshade  = dashade;
	            ogpal    = globalpal;    globalpal = dapalnum;
	            ogxyaspect = gxyaspect; gxyaspect = 1.0;
	            oldviewingrange = viewingrange; viewingrange = 65536;

	            x1 = hudmem[(dastat&4)>>2][picnum].xadd;
	            y1 = hudmem[(dastat&4)>>2][picnum].yadd;
	            z1 = hudmem[(dastat&4)>>2][picnum].zadd;

	            if ((hudmem[(dastat&4)>>2][picnum].flags&2) == 0) //"NOBOB" is specified in DEF
	            {
	                double fx = ((double)sx)*(1.0/65536.0);
	                double fy = ((double)sy)*(1.0/65536.0);

	                if ((dastat&16) != 0)
	                {
	                    xsiz = tilesizx[picnum]; ysiz = tilesizy[picnum];
	                    xoff = (int) ((byte) ((picanm[picnum] >> 8) & 255)) + (xsiz >> 1);
	        			yoff = (int) ((byte) ((picanm[picnum] >> 16) & 255)) + (ysiz >> 1);

	                    d = (double)z/(65536.0*16384.0);
	                    cosang2 = cosang = (double)sintable[(a+512)&2047]*d;
	                    sinang2 = sinang = (double)sintable[a&2047]*d;
	                    if ((dastat&2) != 0 || ((dastat&8) == 0)) //Don't aspect unscaled perms
	                        { d = (double)xyaspect/65536.0; cosang2 *= d; sinang2 *= d; }
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
	            hudsprite.ang = (short) (hudmem[(dastat&4)>>2][picnum].angadd+globalang);

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
	                gl.bglViewport(windowx1,ydim-(windowy2+1),windowx2-windowx1+1,windowy2-windowy1+1);
	            else
	            {
	                gl.bglViewport(0,0,xdim,ydim);
	                glox1 = -1; //Force fullscreen (glox1=-1 forces it to restore)
	            }

	           
                gl.bglMatrixMode(GL_PROJECTION);
              
                if ((dastat&10) == 2)
                {
                    float ratioratio = (float)xdim/ydim;
                    matrix[0][0] = (float)ydimen*(ratioratio >= 1.6f?1.2f:1); matrix[0][2] = 1.0f;
                    matrix[1][1] = (float)xdimen; matrix[1][2] = 1.0f;
                    matrix[2][2] = 1.0f; matrix[2][3] = (float)ydimen*(ratioratio >= 1.6f?1.2f:1);
                    matrix[3][2] = -1.0f;
                }
                else { matrix[0][0] = matrix[2][3] = 1.0f; matrix[1][1] = ((float)xdim)/((float)ydim); matrix[2][2] = 1.0001f; matrix[3][2] = 1-matrix[2][2]; }
                gl.bglLoadMatrixf(matrix);
                gl.bglMatrixMode(GL_MODELVIEW);
                gl.bglLoadIdentity();
	            

	            if ((hudmem[(dastat&4)>>2][picnum].flags&8) != 0) //NODEPTH flag
	                gl.bglDisable(GL_DEPTH_TEST);
	            else
	            {
	                gl.bglEnable(GL_DEPTH_TEST);
	                gl.bglClear(GL_DEPTH_BUFFER_BIT);
	            }

	            gl.bglDisable(GL_FOG);
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

	            return;
	        }
	    }
		
		ogpicnum = globalpicnum;
		globalpicnum = (short) picnum;
		ogshade = (int) globalshade;
		globalshade = dashade;
		ogpal = globalpal;
		globalpal = (int) (dapalnum & 0xFF);
		oghalfx = ghalfx;
		ghalfx = (double) (xdim >> 1);
		ogrhalfxdown10 = grhalfxdown10;
		grhalfxdown10 = 1.0 / (((double) ghalfx) * 1024);
		ogrhalfxdown10x = grhalfxdown10x;
		grhalfxdown10x = grhalfxdown10;
		oghoriz = ghoriz;
		ghoriz = (double) (ydim >> 1);
		ogchang = gchang;
		gchang = 1.0;
		ogshang = gshang;
		gshang = 0.0;
		ogctang = gctang;
		gctang = 1.0;
		ogstang = gstang;
		gstang = 0.0;

		gl.bglViewport(0, 0, xdim, ydim);
		glox1 = -1; // Force fullscreen (glox1=-1 forces it to restore)
		gl.bglMatrixMode(GL_PROJECTION);

		matrix[0][0] = matrix[2][3] = 1.0f;
		matrix[1][1] = ((float) xdim) / ((float) ydim);
		matrix[2][2] = 1.0001f;
		matrix[3][2] = 1 - matrix[2][2];

		gl.bglPushMatrix();
		gl.bglLoadMatrixf(matrix);
		gl.bglMatrixMode(GL_MODELVIEW);
		gl.bglPushMatrix();
		gl.bglLoadIdentity();

		gl.bglDisable(GL_DEPTH_TEST);
		gl.bglDisable(GL_ALPHA_TEST);
		gl.bglEnable(GL_TEXTURE_2D);

		method = 0;
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

		xsiz = tilesizx[globalpicnum];
		ysiz = tilesizy[globalpicnum];

		if ((dastat & 16) != 0) {
			xoff = 0;
			yoff = 0;
		} else {
			xoff = (int) ((byte) ((picanm[globalpicnum] >> 8) & 255)) + (xsiz >> 1);
			yoff = (int) ((byte) ((picanm[globalpicnum] >> 16) & 255)) + (ysiz >> 1);
		}

		if ((dastat & 4) != 0)
			yoff = ysiz - yoff;

		int cx1_plus_cx2 = cx1 + cx2;
		int cy1_plus_cy2 = cy1 + cy2;

		if ((dastat & 2) == 0) {
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim) {
				ourxyaspect = (10 << 16) / 12;
			}
		} else {
			// dastat&2: Auto window size scaling
			int oxdim = xdim;
			int xdim = oxdim; // SHADOWS global

			int zoomsc;
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
				int twice_midcx = cx1_plus_cx2 + 2;

				// screen x center to sx1, scaled to viewport
				int scaledxofs = scale(normxofs, scale(xdimen, xdim, oxdim), 320);

				int xbord = 0;

				if ((dastat & (256 | 512)) != 0) {
					xbord = scale(oxdim - xdim, twice_midcx, oxdim);

					if ((dastat & 512) == 0)
						xbord = -xbord;
				}

				sx = ((twice_midcx + xbord) << 15) + scaledxofs;

				zoomsc = xdimenscale; // = scale(xdimen,yxaspect,320);
				sy = (int) (((cy1_plus_cy2 + 2) << 15) + mulscale(normyofs, zoomsc, 16));
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

		d = (double) z / (65536.0 * 16384.0);
		cosang2 = cosang = (double) sintable[(a + 512) & 2047] * d;
		sinang2 = sinang = (double) sintable[a & 2047] * d;
		if (((dastat & 2) != 0) || ((dastat & 8) == 0)) // Don't aspect unscaled perms
		{
			d = (double) ourxyaspect / 65536.0;
			cosang2 *= d;
			sinang2 *= d;
		}

		dorotatesprite_px[0] = (double) sx / 65536.0 - (double) xoff * cosang2 + (double) yoff * sinang2;
		dorotatesprite_py[0] = (double) sy / 65536.0 - (double) xoff * sinang - (double) yoff * cosang;
		dorotatesprite_px[1] = dorotatesprite_px[0] + (double) xsiz * cosang2;
		dorotatesprite_py[1] = dorotatesprite_py[0] + (double) xsiz * sinang;
		dorotatesprite_px[3] = dorotatesprite_px[0] - (double) ysiz * sinang2;
		dorotatesprite_py[3] = dorotatesprite_py[0] + (double) ysiz * cosang;
		dorotatesprite_px[2] = dorotatesprite_px[1] + dorotatesprite_px[3] - dorotatesprite_px[0];
		dorotatesprite_py[2] = dorotatesprite_py[1] + dorotatesprite_py[3] - dorotatesprite_py[0];

		int n = 4;

		gdx = 0;
		gdy = 0;
		gdo = 1.0;

		d = 1.0 / (dorotatesprite_px[0] * (dorotatesprite_py[1] - dorotatesprite_py[3])
				+ dorotatesprite_px[1]
						* (dorotatesprite_py[3] - dorotatesprite_py[0])
				+ dorotatesprite_px[3]
						* (dorotatesprite_py[0] - dorotatesprite_py[1]));
		gux = (dorotatesprite_py[3] - dorotatesprite_py[0]) * ((double) xsiz - .0001) * d;
		guy = (dorotatesprite_px[0] - dorotatesprite_px[3]) * ((double) xsiz - .0001) * d;
		guo = 0 - dorotatesprite_px[0] * gux - dorotatesprite_py[0] * guy;

		if ((dastat & 4) == 0) {
			gvx = (dorotatesprite_py[0] - dorotatesprite_py[1]) * ((double) ysiz - .0001) * d;
			gvy = (dorotatesprite_px[1] - dorotatesprite_px[0]) * ((double) ysiz - .0001) * d;
			gvo = 0 - dorotatesprite_px[0] * gvx - dorotatesprite_py[0] * gvy;
		} else {
			gvx = (dorotatesprite_py[1] - dorotatesprite_py[0]) * ((double) ysiz - .0001) * d;
			gvy = (dorotatesprite_px[0] - dorotatesprite_px[1]) * ((double) ysiz - .0001) * d;
			gvo = (double) ysiz - .0001 - dorotatesprite_px[0] * gvx - dorotatesprite_py[0] * gvy;
		}

		cx2++;
		cy2++;

		// Clippoly4 (converted from int to double)
		int nn = z = 0;
		do {
			double fx, x1, x2;
			int zz = z + 1;
			if (zz == n)
				zz = 0;
			x1 = dorotatesprite_px[z];
			x2 = dorotatesprite_px[zz] - x1;
			if ((cx1 <= x1) && (x1 <= cx2)) {
				dorotatesprite_px2[nn] = x1;
				dorotatesprite_py2[nn] = dorotatesprite_py[z];
				nn++;
			}
			if (x2 <= 0)
				fx = cx2;
			else
				fx = cx1;
			d = fx - x1;
			if ((d < x2) != (d < 0)) {
				dorotatesprite_px2[nn] = fx;
				dorotatesprite_py2[nn] = (dorotatesprite_py[zz] - dorotatesprite_py[z])
						* d / x2 + dorotatesprite_py[z];
				nn++;
			}
			if (x2 <= 0)
				fx = cx1;
			else
				fx = cx2;
			d = fx - x1;
			if ((d < x2) != (d < 0)) {
				dorotatesprite_px2[nn] = fx;
				dorotatesprite_py2[nn] = (dorotatesprite_py[zz] - dorotatesprite_py[z])
						* d / x2 + dorotatesprite_py[z];
				nn++;
			}
			z = zz;
		} while (z != 0);

		if (nn >= 3) {

			n = z = 0;
			do {
				double fy, y1, y2;
				int zz = z + 1;
				if (zz == nn)
					zz = 0;
				y1 = dorotatesprite_py2[z];
				y2 = dorotatesprite_py2[zz] - y1;
				if ((cy1 <= y1) && (y1 <= cy2)) {
					dorotatesprite_py[n] = y1;
					dorotatesprite_px[n] = dorotatesprite_px2[z];
					n++;
				}
				if (y2 <= 0)
					fy = cy2;
				else
					fy = cy1;
				d = fy - y1;
				if ((d < y2) != (d < 0)) {
					dorotatesprite_py[n] = fy;
					dorotatesprite_px[n] = (dorotatesprite_px2[zz] - dorotatesprite_px2[z])
							* d / y2 + dorotatesprite_px2[z];
					n++;
				}
				if (y2 <= 0)
					fy = cy1;
				else
					fy = cy2;
				d = fy - y1;
				if ((d < y2) != (d < 0)) {
					dorotatesprite_py[n] = fy;
					dorotatesprite_px[n] = (dorotatesprite_px2[zz] - dorotatesprite_px2[z])
							* d / y2 + dorotatesprite_px2[z];
					n++;
				}
				z = zz;
			} while (z != 0);

			gl.bglDisable(GL_FOG);

			pow2xsplit = 0;

			drawpoly(dorotatesprite_px, dorotatesprite_py, n, method);
			EnableFog();
		}

		gl.bglMatrixMode(GL_PROJECTION);
		gl.bglPopMatrix();
		gl.bglMatrixMode(GL_MODELVIEW);
		gl.bglPopMatrix();

		globalpicnum = (short) ogpicnum;
		globalshade = ogshade;
		globalpal = ogpal & 0xFF;
		ghalfx = oghalfx;
		grhalfxdown10 = ogrhalfxdown10;
		grhalfxdown10x = ogrhalfxdown10x;
		ghoriz = oghoriz;
		gchang = ogchang;
		gshang = ogshang;
		gctang = ogctang;
		gstang = ogstang;

//		engine.setaspect_new();
	}
	
	private void EnableFog()
	{
		if (!nofog)
			gl.bglEnable(GL_FOG);
	}

	private final float[] trapextx = new float[2], drawtrap_px = new float[4],
			drawtrap_py = new float[4];

	private void drawtrap(float x0, float x1, float y0, float x2, float x3,
			float y1) {
		int i, n = 3;

		if (y0 == y1)
			return;
		drawtrap_px[0] = x0;
		drawtrap_py[0] = y0;
		drawtrap_py[2] = y1;
		if (x0 == x1) {
			drawtrap_px[1] = x3;
			drawtrap_py[1] = y1;
			drawtrap_px[2] = x2;
		} else if (x2 == x3) {
			drawtrap_px[1] = x1;
			drawtrap_py[1] = y0;
			drawtrap_px[2] = x3;
		} else {
			drawtrap_px[1] = x1;
			drawtrap_py[1] = y0;
			drawtrap_px[2] = x3;
			drawtrap_px[3] = x2;
			drawtrap_py[3] = y1;
			n = 4;
		}

		gl.bglBegin(GL_TRIANGLE_FAN);
		for (i = 0; i < n; i++) {
			drawtrap_px[i] = min(max(drawtrap_px[i], trapextx[0]), trapextx[1]);
			gl.bglTexCoord2f(
					(float) (drawtrap_px[i] * gux + drawtrap_py[i] * guy + guo),
					(float) (drawtrap_px[i] * gvx + drawtrap_py[i] * gvy + gvo));
			gl.bglVertex2f(drawtrap_px[i], drawtrap_py[i]);
		}
		gl.bglEnd();
	}

	private int allocpoints = 0, slist[], npoint2[];
	private raster[] rst;

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

			if (j < 0)
				continue;
			k = npoint2[j];

			m0 = (px[j] - px[i]) * (py[k] - py[j]);
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
			gl.bglBegin(GL_TRIANGLE_FAN);
			for (i = 0; i < npoints; i++) {
				j = slist[i];
				gl.bglTexCoord2f((float) (px[j] * gux + py[j] * guy + guo),
						(float) (px[j] * gvx + py[j] * gvy + gvo));
				gl.bglVertex2f(px[j], py[j]);
			}
			gl.bglEnd();
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
						drawtrap(rst[j].x, rst[j + 1].x, rst[j].y, x0, x1,
								py[i1]);
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
					rst[i].xi = (px[rst[i].i] - rst[i].x)
							/ (py[rst[i].i] - py[i1]);
				}

			}
		}
	}

	public void fillpolygon(int npoints) {

		for (int z = 0; z < npoints; z++) {
			if (xb1[z] >= npoints)
				xb1[z] = 0;
		}
		
		if (palookup[globalpal] == null)
			globalpal = 0;

		Pthtyp pth;
		float f, a = 0.0f;
		int shadebound = (shadescale_unbounded != 0 || globalshade >= numshades) ? numshades
				: numshades - 1;

		globalx1 = mulscale((int) globalx1, xyaspect, 16);
		globaly2 = mulscale((int) globaly2, xyaspect, 16);
		gux = ((double) asm1) * (1.0 / 4294967296.0);
		gvx = ((double) asm2) * (1.0 / 4294967296.0);
		guy = ((double) globalx1) * (1.0 / 4294967296.0);
		gvy = ((double) globaly2) * (-1.0 / 4294967296.0);
		guo = (((double) xdim) * gux + ((double) ydim) * guy) * -.5
				+ ((double) globalposx) * (1.0 / 4294967296.0);
		gvo = (((double) xdim) * gvx + ((double) ydim) * gvy) * -.5
				- ((double) globalposy) * (1.0 / 4294967296.0);

		for (int i = npoints - 1; i >= 0; i--) {
			rx1[i] = rx1[i] / 4096.0f;
			ry1[i] = ry1[i] / 4096.0f;
		}
		
		gl.bglDisable(GL_FOG);

		if (gloy1 != -1)
			setpolymost2dview(); // disables blending, texturing, and depth testing
		gl.bglEnable(GL_ALPHA_TEST);
		gl.bglEnable(GL_TEXTURE_2D);
		pth = textureCache.cache(globalpicnum, globalpal, false, true);

		gl.bglBindTexture(GL_TEXTURE_2D, pth.glpic);

		f = (numshades - min(max((globalshade * shadescale), 0), shadebound))
				/ numshades;

		switch ((globalorientation >> 7) & 3) {
		case 0:
		case 1:
			a = 1.0f;
			gl.bglDisable(GL_BLEND);
			break;
		case 2:
			a = TRANSLUSCENT1;
			gl.bglEnable(GL_BLEND);
			break;
		case 3:
			a = TRANSLUSCENT2;
			gl.bglEnable(GL_BLEND);
			break;
		}

		gl.bglColor4f(f, f, f, a);

		tessectrap(rx1, ry1, xb1, npoints); // vertices + textures
		
		EnableFog();
	}

	public int drawtilescreen(int tilex, int tiley, int wallnum, int dimen,
			int tilezoom, boolean usehitile, int[] loadedhitile) {

		float xdime, ydime, xdimepad, ydimepad, scx, scy, ratio = 1.0f;
		int i;
		Pthtyp pth;

		if (GLInfo.texnpot == 0) {
			i = (1 << (picsiz[wallnum] & 15));
			if (i < tilesizx[wallnum])
				i += i;
			xdimepad = (float) i;
			i = (1 << (picsiz[wallnum] >> 4));
			if (i < tilesizy[wallnum])
				i += i;
			ydimepad = (float) i;
		} else {
			xdimepad = (float) tilesizx[wallnum];
			ydimepad = (float) tilesizy[wallnum];
		}
		xdime = (float) tilesizx[wallnum];
		xdimepad = xdime / xdimepad;
		ydime = (float) tilesizy[wallnum];
		ydimepad = ydime / ydimepad;

		if ((xdime <= dimen) && (ydime <= dimen)) {
			scx = xdime;
			scy = ydime;
		} else {
			scx = (float) dimen;
			scy = (float) dimen;
			if (xdime < ydime)
				scx *= xdime / ydime;
			else
				scy *= ydime / xdime;
		}

		{
			boolean ousehightile = usehightile;
			usehightile = usehitile && usehightile;
			pth = textureCache.cache(wallnum, 0, true, false);
			if (usehightile)
				loadedhitile[wallnum >> 3] |= (1 << (wallnum & 7));
			usehightile = ousehightile;
		}

		gl.bglBindTexture(GL_TEXTURE_2D, pth.glpic);

		gl.bglDisable(GL_ALPHA_TEST);

		if (tilezoom != 0) {
			if (scx > scy)
				ratio = dimen / scx;
			else
				ratio = dimen / scy;
		}

		if (pth == null || pth.hasAlpha()) {
			gl.bglDisable(GL_TEXTURE_2D);
			gl.bglBegin(GL_TRIANGLE_FAN);
			if (gammabrightness != 0)
				gl.bglColor4f((float) curpalette[255].r / 255.0f,
						(float) curpalette[255].g / 255.0f,
						(float) curpalette[255].b / 255.0f, 1.0f);
			else
				gl.bglColor4f(
						(float) britable[curbrightness][curpalette[255].r] / 255.0f,
						(float) britable[curbrightness][curpalette[255].g] / 255.0f,
						(float) britable[curbrightness][curpalette[255].b] / 255.0f,
						1.0f);
			gl.bglVertex2f((float) tilex, (float) tiley);
			gl.bglVertex2f((float) tilex + (scx * ratio), (float) tiley);
			gl.bglVertex2f((float) tilex + (scx * ratio), (float) tiley
					+ (scy * ratio));
			gl.bglVertex2f((float) tilex, (float) tiley + (scy * ratio));
			gl.bglEnd();
		}

		gl.bglColor4f(1, 1, 1, 1);
		gl.bglEnable(GL_TEXTURE_2D);
		gl.bglEnable(GL_BLEND);
		gl.bglBegin(GL_TRIANGLE_FAN);
		gl.bglTexCoord2f(0, 0);
		gl.bglVertex2f((float) tilex, (float) tiley);
		gl.bglTexCoord2f(xdimepad, 0);
		gl.bglVertex2f((float) tilex + (scx * ratio), (float) tiley);
		gl.bglTexCoord2f(xdimepad, ydimepad);
		gl.bglVertex2f((float) tilex + (scx * ratio), (float) tiley
				+ (scy * ratio));
		gl.bglTexCoord2f(0, ydimepad);
		gl.bglVertex2f((float) tilex, (float) tiley + (scy * ratio));
		gl.bglEnd();

		return (0);
	}
	
	

	@Override
	public void palfade(HashMap<String, FadeEffect> fades) {

		gl.bglMatrixMode(GL_PROJECTION);
		gl.bglPushMatrix();
		gl.bglLoadIdentity();
		gl.bglMatrixMode(GL_MODELVIEW);
		gl.bglPushMatrix();
		gl.bglLoadIdentity();

		gl.bglDisable(GL_DEPTH_TEST);
		gl.bglDisable(GL_ALPHA_TEST);
		gl.bglDisable(GL_TEXTURE_2D);

		gl.bglEnable(GL_BLEND);

		palfadergb.draw(gl);
		if(fades != null)
		{
			Iterator<FadeEffect> it = fades.values().iterator();
		    while(it.hasNext()) {
		    	FadeEffect obj = (FadeEffect)it.next();
		    	obj.draw(gl);
		    }
		}

		gl.bglMatrixMode(GL_MODELVIEW);
		gl.bglPopMatrix();
		gl.bglMatrixMode(GL_PROJECTION);
		gl.bglPopMatrix();
		
		gl.bglBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public int printchar(int xpos, int ypos, int col, int backcol, char ch, int fontsize) {
		float tx, ty, txc, tyc;

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

			gl.bglBindTexture(GL_TEXTURE_2D, polymosttext);
			gl.bglTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, 256, 128, 0, GL_ALPHA, GL_UNSIGNED_BYTE, fbuf);
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

			fbuf.clear(); // Bfree(tbuf);
			fbuf = null;
			tbuf = null;
		} else
			gl.bglBindTexture(GL_TEXTURE_2D, polymosttext);

		setpolymost2dview(); // disables blending, texturing, and depth testing
		gl.bglDisable(GL_ALPHA_TEST);
		gl.bglDepthMask(GL_FALSE); // disable writing to the z-buffer

		if (backcol >= 0) {
			gl.bglColor4ub((byte) curpalette[backcol].r,
					(byte) curpalette[backcol].g, (byte) curpalette[backcol].b,
					(byte) 255);
			gl.bglBegin(GL_QUADS);
			gl.bglVertex2i(xpos, ypos);
			gl.bglVertex2i(xpos, ypos + (fontsize != 0 ? 6 : 8));
			int x = xpos + (1 << (3 - fontsize));
			int y = ypos + (fontsize != 0 ? 6 : 8);
			gl.bglVertex2i(x, y);
			gl.bglVertex2i(xpos + (1 << (3 - fontsize)), ypos);
			gl.bglEnd();
		}

		gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		gl.bglDisable(GL_FOG);
		 
		gl.bglEnable(GL_TEXTURE_2D);
		gl.bglEnable(GL_BLEND);
		if(curpalette[col] == null)
			gl.bglColor4ub(255, 255, 255, 255);
		else
			gl.bglColor4ub(curpalette[col].r, curpalette[col].g, curpalette[col].b, 255);

		txc = (float) (fontsize != 0 ? (4.0 / 256.0) : (8.0 / 256.0));
		tyc = (float) (fontsize != 0 ? (6.0 / 128.0) : (8.0 / 128.0));

		gl.bglBegin(GL_QUADS);
	
		tx = (float) ((ch % 32) / 32.0);
		ty = (float) (((ch / 32) + (fontsize * 8)) / 16.0);

		int x = xpos + (8 >> fontsize);
		int y = ypos + (fontsize != 0 ? 6 : 8);

		gl.bglTexCoord2f(tx, ty); // 0
		gl.bglVertex2i(xpos, ypos);

		gl.bglTexCoord2f(tx + txc, ty); // 1
		gl.bglVertex2i(x, ypos);

		gl.bglTexCoord2f(tx + txc, ty + tyc); // 2
		gl.bglVertex2i(x, y);

		gl.bglTexCoord2f(tx, ty + tyc); // 3
		gl.bglVertex2i(xpos, y);

		gl.bglEnd();

		gl.bglDepthMask(GL_TRUE); // re-enable writing to the z-buffer

		EnableFog();

		return 0;
	}

	@Override
	public int printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize) {
		float tx, ty, txc, tyc;
		int c;
		int line = 0;
		int oxpos = xpos;

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

			gl.bglBindTexture(GL_TEXTURE_2D, polymosttext);
			gl.bglTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, 256, 128, 0, GL_ALPHA, GL_UNSIGNED_BYTE, fbuf);
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.bglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

			fbuf.clear(); // Bfree(tbuf);
			fbuf = null;
			tbuf = null;
		} else
			gl.bglBindTexture(GL_TEXTURE_2D, polymosttext);

		setpolymost2dview(); // disables blending, texturing, and depth testing
		gl.bglDisable(GL_ALPHA_TEST);
		gl.bglDepthMask(GL_FALSE); // disable writing to the z-buffer

		if (backcol >= 0) {
			gl.bglColor4ub((byte) curpalette[backcol].r,
					(byte) curpalette[backcol].g, (byte) curpalette[backcol].b,
					(byte) 255);
			c = Bstrlen(text);

			gl.bglBegin(GL_QUADS);
			gl.bglVertex2i(xpos, ypos);
			gl.bglVertex2i(xpos, ypos + (fontsize != 0 ? 6 : 8));
			int x = xpos + (c << (3 - fontsize));
			int y = ypos + (fontsize != 0 ? 6 : 8);
			gl.bglVertex2i(x, y);
			gl.bglVertex2i(xpos + (c << (3 - fontsize)), ypos);
			gl.bglEnd();
		}

//		gl.bglPushAttrib(GL_POLYGON_BIT); // we want to have readable text in FIXME decreasing fps?
		// wireframe mode, too
		gl.bglPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		gl.bglDisable(GL_FOG);
		 
		gl.bglEnable(GL_TEXTURE_2D);
		gl.bglEnable(GL_BLEND);
		if(curpalette[col] == null)
			gl.bglColor4ub(255, 255, 255, 255);
		else
			gl.bglColor4ub(curpalette[col].r, curpalette[col].g, curpalette[col].b, 255);

		txc = (float) (fontsize != 0 ? (4.0 / 256.0) : (8.0 / 256.0));
		tyc = (float) (fontsize != 0 ? (6.0 / 128.0) : (8.0 / 128.0));

		gl.bglBegin(GL_QUADS);

		c = 0;
		while (c < text.length && text[c] != '\0') {
			if(text[c] == '\n')
			{
				text[c] = 0;
				line += 1;
				xpos = oxpos - (8 >> fontsize);
			}
			if(text[c] == '\r')
				text[c] = 0;
			
			tx = (float) ((text[c] % 32) / 32.0);
			ty = (float) (((text[c] / 32) + (fontsize * 8)) / 16.0);

			int yoffs = line * (fontsize != 0 ? 6 : 8);

			int x = xpos + (8 >> fontsize);
			int y = ypos + (fontsize != 0 ? 6 : 8);

			gl.bglTexCoord2f(tx, ty); // 0
			gl.bglVertex2i(xpos, ypos + yoffs);

			gl.bglTexCoord2f(tx + txc, ty); // 1
			gl.bglVertex2i(x, ypos + yoffs);

			gl.bglTexCoord2f(tx + txc, ty + tyc); // 2
			gl.bglVertex2i(x, y + yoffs);

			gl.bglTexCoord2f(tx, ty + tyc); // 3
			gl.bglVertex2i(xpos, y + yoffs);

			xpos += (8 >> fontsize);
			c++;
		}

		gl.bglEnd();

		gl.bglDepthMask(GL_TRUE); // re-enable writing to the z-buffer
//		gl.bglPopAttrib();
		
		EnableFog();

		return 0;
	}

	public void polymost_precache(int dapicnum, int dapalnum, int datype) {
		// dapicnum and dapalnum are like you'd expect
		// datype is 0 for a wall/floor/ceiling and 1 for a sprite
		// basically this just means walls are repeating
		// while sprites are clamped
		// int mid;

		if ((palookup[dapalnum] == null)
				&& (dapalnum < (MAXPALOOKUPS - RESERVEDPALS)))
			return;// dapalnum = 0;

		// OSD_Printf("precached %d %d type %d\n", dapicnum, dapalnum, datype);
		// hicprecaching = 1;

		textureCache.cache(dapicnum, dapalnum, clampingMode((datype & 1) << 2), false);
		// hicprecaching = 0;

		if (datype == 0 || !usemodels)
			return;
		// FIXME:
		// mid = md_tilehasmodel(dapicnum,dapalnum);
		// if (mid < 0 || models[mid].mdnum < 2) return;
		//
		// {
		// int i,j=0;
		//
		// if (models[mid].mdnum == 3)
		// j = ((md3model_t *)models[mid]).head.numsurfs;
		//
		// for (i=0; i<=j; i++)
		// {
		// mdloadskin((md2model_t *)models[mid], 0, dapalnum, i);
		// }
		// }
	}

	private void fogcalc(int tile, int shade, int vis, int pal)
	{
// 		If models disabled, levels seems darker
//	    if (shade > 0 && 
//	        (!usehightile || hicfindsubst(tile, pal, 0) == null) &&
//	        (!usemodels /* || md_tilehasmodel(tile, pal) < 0 */)) 
		if (shade > 0)
		{
	    	shade >>= 1;
	    }
	   
	        
        float combvis = (float) globalvisibility * ((vis+16) & 0xFF);
        if (combvis == 0)
        {
        	fogresult = (float) FULLVIS_BEGIN;
            fogresult2 = (float) FULLVIS_END;
        } 
        else if (shade >= numshades-1)
        {
            fogresult = -1;
            fogresult2 = 0.001f;
        }
        else
        {
            combvis = 1.0f / combvis;
            fogresult = (shade > 0) ? 0 : -(FOGDISTCONST * shade) * combvis;
            fogresult2 = (FOGDISTCONST * (numshades-1-shade)) * combvis;
        }
       
        fogcol.put(fogtable[pal][0]);
		fogcol.put(fogtable[pal][1]);
		fogcol.put(fogtable[pal][2]);
		fogcol.put(0);
		fogcol.flip();
	}

	private void calc_and_apply_fog(int tile, int shade, int vis, int pal)
	{
		fogcalc(tile, shade, vis, pal);
	    gl.bglFogfv(GL_FOG_COLOR, fogcol);
	    if(pal == 1) //Blood's pal 1
		{
			fogresult = 0;
			if(fogresult2 > 2)
				fogresult2 = 2;
		}
	    gl.bglFogf(GL_FOG_START, fogresult);
	    gl.bglFogf(GL_FOG_END, fogresult2);
	}
	
	private void calc_and_apply_skyfog(int tile, int shade, int vis, int pal)
	{
		fogcalc(tile, shade, vis, pal);
	    gl.bglFogfv(GL_FOG_COLOR, fogcol);	
	    gl.bglFogf(GL_FOG_START, (float)FULLVIS_BEGIN);
	    gl.bglFogf(GL_FOG_END, (float)FULLVIS_END);
	}

	public void setpolymost2dview() {
		if (gloy1 != -1) {
			gl.bglViewport(0, 0, xdim, ydim);
			gl.bglMatrixMode(GL_PROJECTION);
			gl.bglLoadIdentity();
			gl.bglOrtho(0, xdim, ydim, 0, -1, 1);
			gl.bglMatrixMode(GL_MODELVIEW);
			gl.bglLoadIdentity();
		}

		gloy1 = -1;

		gl.bglDisable(GL_DEPTH_TEST);
		gl.bglDisable(GL_TEXTURE_2D);
		gl.bglDisable(GL_BLEND);
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
			if(tspriteptr[i].picnum == -1) continue;
			xs = tspriteptr[i].x - globalposx;
			ys = tspriteptr[i].y - globalposy;
			yp = dmulscale(xs, cosviewingrangeglobalang, ys, sinviewingrangeglobalang, 6);

			modelp = (usemodels &&
					tile2model[tspriteptr[i].picnum] != null &&
					tile2model[tspriteptr[i].picnum].modelid >= 0);

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

					SPRITE stmp = tspriteptr[l];
					tspriteptr[l] = tspriteptr[l + gap]; // swaplong(&tspriteptr[l],&tspriteptr[l+gap]);
					tspriteptr[l + gap] = stmp;

					int tmp = spritesx[l];
					spritesx[l] = spritesx[l + gap]; // swaplong(&spritesx[l],&spritesx[l+gap]);
					spritesx[l + gap] = tmp;

					tmp = spritesy[l];
					spritesy[l] = spritesy[l + gap];
					spritesy[l + gap] = tmp;
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
					if(tspriteptr[k].picnum == -1) continue;
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
						if (klabs(spritesz[k] - globalposz) < klabs(spritesz[l] - globalposz)) {
							SPRITE stmp = tspriteptr[k];
							tspriteptr[k] = tspriteptr[l]; // swaplong(&tspriteptr[k],&tspriteptr[l]);
							tspriteptr[l] = stmp;

							int tmp = spritesx[k];
							spritesx[k] = spritesx[l];
							spritesx[l] = tmp;

							tmp = spritesy[k];
							spritesy[k] = spritesy[l];
							spritesy[l] = tmp;

							tmp = spritesz[k];
							spritesz[k] = spritesz[l];
							spritesz[l] = tmp;
						}
				for (k = i + 1; k < j; k++)
					for (l = i; l < k; l++) {
						if (tspriteptr[k].statnum < tspriteptr[l].statnum) {
							SPRITE stmp = tspriteptr[k];
							tspriteptr[k] = tspriteptr[l]; // swaplong(&tspriteptr[k],&tspriteptr[l]);
							tspriteptr[l] = stmp;
							int tmp = spritesx[k];
							spritesx[k] = spritesx[l];
							spritesx[l] = tmp;

							tmp = spritesy[k];
							spritesy[k] = spritesy[l];
							spritesy[l] = tmp;
						}
						
						if((tspriteptr[k].cstat & 2) != 0) //transparent sort
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
						}
					}
			}
			i = j;
		}

		curpolygonoffset = 0;

		drawmasks_pos.x = (float) globalposx;
		drawmasks_pos.y = (float) globalposy;
		
		gl.bglEnable(GL10.GL_POLYGON_OFFSET_FILL);

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
		
		gl.bglDisable(GL10.GL_POLYGON_OFFSET_FILL);
		gl.bglPolygonOffset(0, 0);
		
		if (totalclock < lastcullcheck - CULL_DELAY)
			lastcullcheck = totalclock;
		if (totalclock >= lastcullcheck + CULL_DELAY)
			lastcullcheck = (totalclock + CULL_DELAY);

		indrawroomsandmasks = 0;
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2,
			int cy2) {

		if (picnum >= MAXTILES)
			return;

		if ((cx1 > cx2) || (cy1 > cy2))
			return;
		if (z <= 16)
			return;
		if ((picanm[picnum] & 192) != 0)
			picnum += engine.animateoffs((short) picnum, (short) 0xc000);
		if ((tilesizx[picnum] <= 0) || (tilesizy[picnum] <= 0))
			return;

		if ((dastat & 128) == 0 || beforedrawrooms != 0)
			dorotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2, guniqhudid);
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		col = palookup[0][col] & 0xFF;
		
		gl.bglDisable(GL_FOG);

		setpolymost2dview(); // JBF 20040205: more efficient setup

		gl.bglBegin(GL_LINES);
		gl.bglColor4ub((byte) curpalette[col].r, (byte) curpalette[col].g, (byte) curpalette[col].b, (byte) 255);
		gl.bglVertex2f((float) x1 / 4096.0f, (float) y1 / 4096.0f);
		gl.bglVertex2f((float) x2 / 4096.0f, (float) y2 / 4096.0f);
		gl.bglEnd();
		
		EnableFog();
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
		float yaw = (globalang & 2047) / (2048.0f / 360.0f) - 90.0f;
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
		float yaw = (globalang & 2047) / (2048.0f / 360.0f) - 90.0f;
		float roll = gtang * 57.3f; //XXX 57.3f WTF
		float spriteang = ((tspr.ang + spriteext[tspr.owner].angoff + 512) & 2047) / (2048.0f / 360.0f);

		gl.bglLoadIdentity();
		gl.bglRotatef(roll, 0, 0, -1);
		gl.bglRotatef(pitch, -1, 0, 0);
		gl.bglRotatef(yaw, 0, -1, 0);
		gl.bglScalef(-1 / 16f, 1.0f, 1 / 16f);
		gl.bglTranslatef(a0.y, a0.z, a0.x);
		gl.bglRotatef(spriteang, 0.0f, -1.0f, 0.0f);
	}
	
	public int mddraw(SPRITE tspr, int xoff, int yoff)
	{
	  
//	    if (r_vbos && (r_vbocount > allocvbos))
//	    {
//	        indexvbos = Brealloc(indexvbos, sizeof(GLuint) * r_vbocount);
//	        vertvbos = Brealloc(vertvbos, sizeof(GLuint) * r_vbocount);
//
//	        bglGenBuffersARB(r_vbocount - allocvbos, &(indexvbos[allocvbos]));
//	        bglGenBuffersARB(r_vbocount - allocvbos, &(vertvbos[allocvbos]));
//
//	        i = allocvbos;
//	        while (i < r_vbocount)
//	        {
//	            bglBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexvbos[i]);
//	            bglBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB, maxmodeltris * 3 * sizeof(uint16_t), NULL, GL_STREAM_DRAW_ARB);
//	            bglBindBufferARB(GL_ARRAY_BUFFER_ARB, vertvbos[i]);
//	            bglBufferDataARB(GL_ARRAY_BUFFER_ARB, maxmodelverts * sizeof(point3d), NULL, GL_STREAM_DRAW_ARB);
//	            i++;
//	        }
//
//	        bglBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,0);
//	        bglBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
//
//	        allocvbos = r_vbocount;
//	    }

//	    if (maxmodelverts > allocmodelverts)
//	    {
//	        point3d vl = Brealloc(vertlist, maxmodelverts);
//	        if (vl == null) { OSD_Printf("ERROR: Not enough memory to allocate %d vertices!\n",maxmodelverts); return 0; }
//	        vertlist = vl;
//	        allocmodelverts = maxmodelverts;
//	    }

		Model vm = models.get(tile2model[Ptile2tile(tspr.picnum,(tspr.owner >= MAXSPRITES) ? tspr.pal : sprite[tspr.owner].pal)].modelid);

		if (vm.mdnum == 1) { return voxdraw((VOXModel) vm,tspr, xoff, yoff); }
	    if (vm.mdnum == 2) { return md2draw((MD2Model) vm, tspr, xoff, yoff); }
	    if (vm.mdnum == 3) { return md3draw((MD3Model) vm, tspr, xoff, yoff); }
	    return 0;
	}

	private int md3draw(MD3Model m, SPRITE tspr, int xoff, int yoff)
	{
		int lpal = (tspr.owner >= MAXSPRITES) ? tspr.pal : sprite[tspr.owner].pal;
    	
//		if (r_vbos != 0 && (m.vbos == null))
//	        mdloadvbos(m, gl);
		
    	updateanimation(m, tspr, lpal);

    	float f = m.interpol; float g = 1-f;
    	
    	if (m.interpol < 0 || m.interpol > 1 ||
                m.cframe < 0 || m.cframe >= m.numframes ||
                m.nframe < 0 || m.nframe >= m.numframes)
        {
            if (m.interpol < 0)
                m.interpol = 0;
            if (m.interpol > 1)
                m.interpol = 1;
            if (m.cframe < 0)
                m.cframe = 0;
            if (m.cframe >= m.numframes)
                m.cframe = m.numframes - 1;
            if (m.nframe < 0)
                m.nframe = 0;
            if (m.nframe >= m.numframes)
                m.nframe = m.numframes - 1;
        }

    	float m0x = m.scale * g;
    	float m0y = -m.scale * g;
    	float m0z = m.scale * g;
    	
    	float m1x = m.scale * f;
    	float m1y = -m.scale * f;
    	float m1z = m.scale * f;
    	
        m0x *= (1.0f/64.0f); m1x *= (1.0f/64.0f);
        m0y *= (1.0f/64.0f); m1y *= (1.0f/64.0f);
        m0z *= (1.0f/64.0f); m1z *= (1.0f/64.0f);

    	modela0.x = modela0.y = 0; modela0.z = ((globalorientation & 8) != 0 ? -m.zadd : m.zadd) * m.scale;
    	float x0 = (float) tspr.x;
    	float k0 = (float) tspr.z;
    	if ( (globalorientation & 128) != 0 && (globalorientation&48) != 32 ) 
    		k0 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<1);

    	// Parkar: Changed to use the same method as centeroriented sprites
        if ((globalorientation & 8) != 0) //y-flipping
        {
        	yoff = -yoff;
            m0z = -m0z; m1z = -m1z; modela0.z = -modela0.z;
            k0 -= (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<2);
        }
        if ((globalorientation & 4) != 0) //x-flipping
        { 
        	xoff = -xoff;
        	m0y = -m0y; m1y = -m1y; 
        	modela0.y = -modela0.y; 
        } 
        x0 += xoff * (tspr.xrepeat >> 2);
        k0 -= ((yoff * tspr.yrepeat) << 2);
		
        // yoffset differs from zadd in that it does not follow cstat&8 y-flipping
        modela0.z += m.yoffset * m.scale;

        f = ((float)tspr.xrepeat)/64*m.bscale;
        m0x *= f; m1x *= f; modela0.x *= f; f = -f;   // 20040610: backwards models aren't cool
        m0y *= f; m1y *= f; modela0.y *= f;
        f = ((float)tspr.yrepeat)/64*m.bscale;
        m0z *= f; m1z *= f; modela0.z *= f;
		
        // floor aligned
        float k1 = (float)tspr.y;
        if ((globalorientation&48)==32)
        {
            m0z = -m0z; m1z = -m1z; modela0.z = -modela0.z;
            m0y = -m0y; m1y = -m1y; modela0.y = -modela0.y;
            f = modela0.x; modela0.x = modela0.z; modela0.z = f;
            k1 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)>>3);
        }
        
        f = (65536.0f*512.0f)/((float)(xdimen*viewingrange));
        g = (float) (32.0f/((float)(xdimen*gxyaspect)));
        m0y *= f; m1y *= f; modela0.y = (((float)(x0 	 -globalposx))/  1024.0f + modela0.y)*f;
        m0x *=-f; m1x *=-f; modela0.x = (((float)(k1     -globalposy))/ 1024.0f + modela0.x)*f;
        m0z *= g; m1z *= g; modela0.z = (((float)(k0     -globalposz))/ -16384.0f + modela0.z)*g;
		
//    	md3_vox_calcmat_common(tspr, dvoxa0);
        md3_vox_calcmat_common(tspr, modela0, f, matrix);

    	// floor aligned
        if ((globalorientation&48)==32)
        {
            f = matrix[1][0]; matrix[1][0] = matrix[2][0]*16.0f; matrix[2][0] = -f*(1.0f/16.0f);
            f = matrix[1][1]; matrix[1][1] = matrix[2][1]*16.0f; matrix[2][1] = -f*(1.0f/16.0f);
            f = matrix[1][2]; matrix[1][2] = matrix[2][2]*16.0f; matrix[2][2] = -f*(1.0f/16.0f);
        }

        //Mirrors
        if (grhalfxdown10x < 0) { 
        	matrix[0][0] = -matrix[0][0];
			matrix[1][0] = -matrix[1][0];
			matrix[2][0] = -matrix[2][0];
			matrix[3][0] = -matrix[3][0];
        }
		matrix[0][3] = matrix[1][3] = matrix[2][3] = 0.f;
		matrix[3][3] = 1.f;

		gl.bglMatrixMode(GL_MODELVIEW); // Let OpenGL (and perhaps hardware :) handle the matrix rotation
		gl.bglLoadMatrixf(matrix);
		gl.bglRotatef(-90, 0.0f, 1.0f, 0.0f);

//        gl.bglPushAttrib(GL_POLYGON_BIT); FIXME decreasing fps?
        if ((grhalfxdown10x >= 0) ^((globalorientation&8) != 0) ^((globalorientation&4) != 0)) gl.bglFrontFace(GL_CW); else gl.bglFrontFace(GL_CCW);
        gl.bglEnable(GL_CULL_FACE);
        gl.bglCullFace(GL_BACK);

        gl.bglEnable(GL_TEXTURE_2D);

		drawpoly_pc[0] = drawpoly_pc[1] = drawpoly_pc[2] = ((float)(numshades-min(max((globalshade * shadescale)+m.shadeoff,0),numshades)))/((float)numshades);
	    if ((hictinting[globalpal].f&4) == 0)
	    {
	        if ((m.flags&1) == 0 || (!(tspr.owner >= MAXSPRITES) && sector[sprite[tspr.owner].sectnum].floorpal!=0))
	        {
	            drawpoly_pc[0] *= (float)hictinting[globalpal].r / 255.0;
	            drawpoly_pc[1] *= (float)hictinting[globalpal].g / 255.0;
	            drawpoly_pc[2] *= (float)hictinting[globalpal].b / 255.0;
	            if (hictinting[MAXPALOOKUPS-1].r != 255 || hictinting[MAXPALOOKUPS-1].g != 255 || hictinting[MAXPALOOKUPS-1].b != 255)
	            {
	                drawpoly_pc[0] *= (float)hictinting[MAXPALOOKUPS-1].r / 255.0f;
	                drawpoly_pc[1] *= (float)hictinting[MAXPALOOKUPS-1].g / 255.0f;
	                drawpoly_pc[2] *= (float)hictinting[MAXPALOOKUPS-1].b / 255.0f;
	            }
	        }
	        else globalnoeffect=1;
	    }

	    if ((tspr.cstat&2) != 0) {
	    	if ((tspr.cstat&512) == 0) {
				drawpoly_pc[3] = TRANSLUSCENT1;
	    	} else { 
				drawpoly_pc[3] = TRANSLUSCENT2;
	    	}
	    } else drawpoly_pc[3] = 1.0f;
	   
	    if (m.usesalpha) //Sprites with alpha in texture
	    {
	        float al = 0.0f;
	        if (alphahackarray[globalpicnum] != 0)
	            al=alphahackarray[globalpicnum];
	        gl.bglEnable(GL_BLEND);
	        gl.bglEnable(GL_ALPHA_TEST);
	        gl.bglAlphaFunc(GL_GREATER,al);
	    }
	    else
	        if ((tspr.cstat&2) != 0) gl.bglEnable(GL_BLEND);

	    gl.bglColor4f(drawpoly_pc[0],drawpoly_pc[1],drawpoly_pc[2],drawpoly_pc[3]);

	    int rendered = 0;
	    for (int surfi = 0; surfi < m.head.numSurfaces; surfi++)
	    {
	    	MD3Surface s = m.surfaces[surfi];
	    	
	    	m.verticesBuffer.clear();
	    	for (int i = 0; i < s.numverts; i++)
	        {
				MD3Vertice v0 = s.xyzn[m.cframe * s.numverts + i];
                MD3Vertice v1 = s.xyzn[m.nframe * s.numverts + i];

				m.verticesBuffer.put(v0.x*m0x + v1.x*m1x);
				m.verticesBuffer.put(v0.z*m0z + v1.z*m1z);
				m.verticesBuffer.put(v0.y*m0y + v1.y*m1y);
	        }
	    	m.verticesBuffer.flip();

			Texture texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,globalpal,surfi);
	        if (texid != null) {

		        bindTexture(gl, texid);
		        
		        if ( Console.Geti("r_detailmapping") != 0 )
		        	texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,DETAILPAL,surfi);
		        else
		        	texid = null;
		        
		        int texunits = GL_TEXTURE0_ARB;
		        
		        if (texid != null)
		        {
		            gl.bglActiveTextureARB(++texunits);
		
		            gl.bglEnable(GL_TEXTURE_2D);
		            gl.bglBindTexture(GL_TEXTURE_2D, texid);
		
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_MODULATE);
		
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);
		
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);
		
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);
		
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 2.0f);
		
		            gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
		            gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
		
		            for (MDSkinmap sk = m.skinmap; sk != null; sk = sk.next)
		                if (sk.palette == DETAILPAL && sk.skinnum == tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum && sk.surfnum == surfi)
		                    f = sk.param;
		
		            gl.bglMatrixMode(GL_TEXTURE);
		            gl.bglLoadIdentity();
		            gl.bglScalef(f, f, 1.0f);
		            gl.bglMatrixMode(GL_MODELVIEW);
		        }
		        
		        if (r_glowmapping != 0)
		        	texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,GLOWPAL,surfi);
		        else
		        	texid = null;
		        
		        if (texid != null)
		        {
		        	gl.bglActiveTextureARB(++texunits);
		
		        	gl.bglEnable(GL_TEXTURE_2D);
		        	gl.bglBindTexture(GL_TEXTURE_2D, texid);
		
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_INTERPOLATE_ARB);
		
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);
		
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);
		
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE2_RGB_ARB, GL_TEXTURE);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND2_RGB_ARB, GL_ONE_MINUS_SRC_ALPHA);
		
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
		        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);
		
		        	gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
		        	gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
		        }
	
		        if(r_vertexarrays != 0)
		        {
		        	m.indicesBuffer.clear();
		        	for (int i = s.numtris - 1; i >= 0; i--)
		        		 for (int j = 0; j < 3; j++)
		        			 m.indicesBuffer.put((short) s.tris[i][j]);
		        	m.indicesBuffer.flip();
		        	
		        	int l = GL_TEXTURE0_ARB;
	                do
	                {
	                    gl.bglClientActiveTextureARB(l++);
	                    gl.bglEnableClientState(GL_TEXTURE_COORD_ARRAY);
	                    gl.bglTexCoordPointer(2, 0, s.uv);
	                } while (l <= texunits);
	                
	                gl.bglEnableClientState(GL_VERTEX_ARRAY);
	                gl.bglVertexPointer(3, 0, m.verticesBuffer);
	                gl.bglDrawElements(GL_TRIANGLES, m.indicesBuffer);
		        }
		        else
		        {
			        gl.bglBegin(GL_TRIANGLES);
		            for (int i = s.numtris - 1; i >= 0; i--)
		                for (int j = 0; j < 3; j++)
		                {
		                    int k = s.tris[i][j];
		                    if (texunits > GL_TEXTURE0_ARB)
		                    {
		                        int l = GL_TEXTURE0_ARB;
		                        while (l <= texunits)
		                            gl.bglMultiTexCoord2dARB(l++, s.uv.get(2*k), s.uv.get(2*k+1));
		                    }
		                    else
		                        gl.bglTexCoord2f(s.uv.get(2 * k), s.uv.get(2 * k + 1));

		                    float x = m.verticesBuffer.get(3 * k);
		                    float y = m.verticesBuffer.get(3 * k + 1);
		                    float z = m.verticesBuffer.get(3 * k + 2);

		                    gl.bglVertex3d(x, y, z);
		                }
		            gl.bglEnd();
		        }
	
		        while (texunits > GL_TEXTURE0_ARB)
		        {
		        	gl.bglMatrixMode(GL_TEXTURE);
		            gl.bglLoadIdentity();
		            gl.bglMatrixMode(GL_MODELVIEW);
		            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 1.0f);
		            gl.bglDisable(GL_TEXTURE_2D);
		            if (r_vertexarrays != 0)
		            {
		                gl.bglDisableClientState(GL_TEXTURE_COORD_ARRAY);
		                gl.bglClientActiveTextureARB(texunits - 1);
		            }
		            gl.bglActiveTextureARB(--texunits);
		        }
		        if (r_vertexarrays != 0) gl.bglDisableClientState(GL_VERTEX_ARRAY);
		        rendered = 1;
		    } else break;
	    }

    	if (m.usesalpha) gl.bglDisable(GL_ALPHA_TEST);
    	gl.bglDisable(GL_CULL_FACE);
//    	gl.bglPopAttrib();
        gl.bglLoadIdentity();

        globalnoeffect=0;
		return rendered;
	}
	
	private int md2draw(MD2Model m, SPRITE tspr, int xoff, int yoff)
	{
    	int lpal = (tspr.owner >= MAXSPRITES) ? tspr.pal : sprite[tspr.owner].pal;
    	
    	updateanimation(m, tspr, lpal);

    	float f = m.interpol; float g = 1-f;
    	
    	if (m.interpol < 0 || m.interpol > 1 ||
                m.cframe < 0 || m.cframe >= m.numframes ||
                m.nframe < 0 || m.nframe >= m.numframes)
        {
            if (m.interpol < 0)
                m.interpol = 0;
            if (m.interpol > 1)
                m.interpol = 1;
            if (m.cframe < 0)
                m.cframe = 0;
            if (m.cframe >= m.numframes)
                m.cframe = m.numframes - 1;
            if (m.nframe < 0)
                m.nframe = 0;
            if (m.nframe >= m.numframes)
                m.nframe = m.numframes - 1;
        }
    	MD2Frame cframe = m.frames[m.cframe], nframe = m.frames[m.nframe];
    	
    	float m0x = m.scale * g;
    	float m0y = m.scale * g;
    	float m0z = m.scale * g;
    	float m1x = m.scale * f;
    	float m1y = m.scale * f;
    	float m1z = m.scale * f;
    	modela0.x = modela0.y = 0; modela0.z = ((globalorientation & 8) != 0 ? -m.zadd : m.zadd) * m.scale;
    	float x0 = (float) tspr.x;
    	float k0 = (float) tspr.z;
    	
    	if ( (globalorientation & 128) != 0 && (globalorientation&48) != 32 ) 
    		k0 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<1);

    	// Parkar: Changed to use the same method as centeroriented sprites
        if ((globalorientation & 8) != 0) //y-flipping
        {
        	yoff = -yoff;
            m0z = -m0z; m1z = -m1z; modela0.z = -modela0.z;
            k0 -= (float)((tilesizy[tspr.picnum]*tspr.yrepeat)<<2);
        }
        if ((globalorientation & 4) != 0) //x-flipping
        { 
        	xoff = -xoff;
        	m0y = -m0y; m1y = -m1y; 
        	modela0.y = -modela0.y; 
        } 
        
        x0 += xoff * (tspr.xrepeat >> 2);
        k0 -= ((yoff * tspr.yrepeat) << 2);
        // yoffset differs from zadd in that it does not follow cstat&8 y-flipping
        modela0.z += m.yoffset * m.scale;

        f = ((float)tspr.xrepeat)/64*m.bscale;
        m0x *= f; m1x *= f; modela0.x *= f; f = -f;   // 20040610: backwards models aren't cool
        m0y *= f; m1y *= f; modela0.y *= f;
        f = ((float)tspr.yrepeat)/64*m.bscale;
        m0z *= f; m1z *= f; modela0.z *= f;
		
        // floor aligned
        float k1 = (float)tspr.y;
        if ((globalorientation&48)==32)
        {
            m0z = -m0z; m1z = -m1z; modela0.z = -modela0.z;
            m0y = -m0y; m1y = -m1y; modela0.y = -modela0.y;
            f = modela0.x; modela0.x = modela0.z; modela0.z = f;
            k1 += (float)((tilesizy[tspr.picnum]*tspr.yrepeat)>>3);
        }
        
        f = (65536.0f*512.0f)/((float)(xdimen*viewingrange));
        g = (float) (32.0f/((float)(xdimen*gxyaspect)));
        m0y *= f; m1y *= f; modela0.y = (((float)(x0	-globalposx))/  1024.0f + modela0.y)*f;
        m0x *=-f; m1x *=-f; modela0.x = (((float)(k1     -globalposy))/ 1024.0f + modela0.x)*f;
        m0z *= g; m1z *= g; modela0.z = (((float)(k0     -globalposz))/ -16384.0f + modela0.z)*g;
		
//    	md3_vox_calcmat_common(tspr, dvoxa0);
        md3_vox_calcmat_common(tspr, modela0, f, matrix);

    	// floor aligned
        if ((globalorientation&48)==32)
        {
            f = matrix[1][0]; matrix[1][0] = matrix[2][0]*16.0f; matrix[2][0] = -f*(1.0f/16.0f);
            f = matrix[1][1]; matrix[1][1] = matrix[2][1]*16.0f; matrix[2][1] = -f*(1.0f/16.0f);
            f = matrix[1][2]; matrix[1][2] = matrix[2][2]*16.0f; matrix[2][2] = -f*(1.0f/16.0f);
        }

        //Mirrors
        if (grhalfxdown10x < 0) { 
        	matrix[0][0] = -matrix[0][0];
			matrix[1][0] = -matrix[1][0];
			matrix[2][0] = -matrix[2][0];
			matrix[3][0] = -matrix[3][0];
        }
		matrix[0][3] = matrix[1][3] = matrix[2][3] = 0.f;
		matrix[3][3] = 1.f;

		gl.bglMatrixMode(GL_MODELVIEW); // Let OpenGL (and perhaps hardware :) handle the matrix rotation
		gl.bglLoadMatrixf(matrix);
		gl.bglRotatef(-90, 0.0f, 1.0f, 0.0f);

//        gl.bglPushAttrib(GL_POLYGON_BIT); FIXME decreasing fps?
        if ((grhalfxdown10x >= 0) ^((globalorientation&8) != 0) ^((globalorientation&4) != 0)) gl.bglFrontFace(GL_CW); else gl.bglFrontFace(GL_CCW);
        gl.bglEnable(GL_CULL_FACE);
        gl.bglCullFace(GL_FRONT);

        gl.bglEnable(GL_TEXTURE_2D);

		drawpoly_pc[0] = drawpoly_pc[1] = drawpoly_pc[2] = ((float)(numshades-min(max((globalshade * shadescale)+m.shadeoff,0),numshades)))/((float)numshades);
	    if ((hictinting[globalpal].f&4) == 0)
	    {
	        if ((m.flags&1) == 0 || (!(tspr.owner >= MAXSPRITES) && sector[sprite[tspr.owner].sectnum].floorpal!=0))
	        {
	            drawpoly_pc[0] *= (float)hictinting[globalpal].r / 255.0;
	            drawpoly_pc[1] *= (float)hictinting[globalpal].g / 255.0;
	            drawpoly_pc[2] *= (float)hictinting[globalpal].b / 255.0;
	            if (hictinting[MAXPALOOKUPS-1].r != 255 || hictinting[MAXPALOOKUPS-1].g != 255 || hictinting[MAXPALOOKUPS-1].b != 255)
	            {
	                drawpoly_pc[0] *= (float)hictinting[MAXPALOOKUPS-1].r / 255.0f;
	                drawpoly_pc[1] *= (float)hictinting[MAXPALOOKUPS-1].g / 255.0f;
	                drawpoly_pc[2] *= (float)hictinting[MAXPALOOKUPS-1].b / 255.0f;
	            }
	        }
	        else globalnoeffect=1;
	    }

	    if ((tspr.cstat&2) != 0) {
	    	if ((tspr.cstat&512) == 0) {
				drawpoly_pc[3] = TRANSLUSCENT1;
	    	} else { 
				drawpoly_pc[3] = TRANSLUSCENT2;
	    	}
	    } else drawpoly_pc[3] = 1.0f;
	    if (m.usesalpha) //Sprites with alpha in texture
	    {
	        float al = 0.0f;
	        if (alphahackarray[globalpicnum] != 0)
	            al=alphahackarray[globalpicnum];
	        gl.bglEnable(GL_BLEND);
	        gl.bglEnable(GL_ALPHA_TEST);
	        gl.bglAlphaFunc(GL_GREATER,al);
	    }
	    else
	        if ((tspr.cstat&2) != 0) gl.bglEnable(GL_BLEND);

	    gl.bglColor4f(drawpoly_pc[0],drawpoly_pc[1],drawpoly_pc[2],drawpoly_pc[3]);

	    int rendered = 0;
		Texture texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,globalpal,0);
        if (texid != null)
        {
	        bindTexture(gl, texid);
	        if (Console.Geti("r_detailmapping") != 0)
	        	texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,DETAILPAL,0);
	        else
	        	texid = null;
	        
	        int texunits = GL_TEXTURE0_ARB;
	        
	        if (texid != null)
	        {
	            gl.bglActiveTextureARB(++texunits);
	
	            gl.bglEnable(GL_TEXTURE_2D);
	            gl.bglBindTexture(GL_TEXTURE_2D, texid);
	
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_MODULATE);
	
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);
	
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);
	
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);
	
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 2.0f);
	
	            gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
	            gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
	
	            for (MDSkinmap sk = m.skinmap; sk != null; sk = sk.next)
	                if (sk.palette == DETAILPAL && sk.skinnum == tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum && sk.surfnum == 0)
	                    f = sk.param;
	
	            gl.bglMatrixMode(GL_TEXTURE);
	            gl.bglLoadIdentity();
	            gl.bglScalef(f, f, 1.0f);
	            gl.bglMatrixMode(GL_MODELVIEW);
	        }
	        
	        if (r_glowmapping != 0)
	        	texid = mdloadskin(gl, m,tile2model[Ptile2tile(tspr.picnum,lpal)].skinnum,GLOWPAL,0);
	        else
	        	texid = null;
	        
	        if (texid != null)
	        {
	        	gl.bglActiveTextureARB(++texunits);
	
	        	gl.bglEnable(GL_TEXTURE_2D);
	        	gl.bglBindTexture(GL_TEXTURE_2D, texid);
	
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_ARB);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_ARB, GL_INTERPOLATE_ARB);
	
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_RGB_ARB, GL_PREVIOUS_ARB);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_RGB_ARB, GL_SRC_COLOR);
	
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE1_RGB_ARB, GL_TEXTURE);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND1_RGB_ARB, GL_SRC_COLOR);
	
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE2_RGB_ARB, GL_TEXTURE);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND2_RGB_ARB, GL_ONE_MINUS_SRC_ALPHA);
	
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_ALPHA_ARB, GL_REPLACE);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA_ARB, GL_PREVIOUS_ARB);
	        	gl.bglTexEnvf(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA_ARB, GL_SRC_ALPHA);
	
	        	gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
	        	gl.bglTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
	        }

	        if(r_vertexarrays != 0)
	        {
	        	m.verticesBuffer.clear();
	        	for (int i = 0; i < m.tris.length; i++) //-60fps, but it's need for animation
	        		for( int j = 0; j < 3; j++) 
	        		{
	        			int idx = m.tris[i].vertices[j];
	        			float x = cframe.vertices[idx][0]*m0x + nframe.vertices[idx][0]*m1x;
	         			float y = cframe.vertices[idx][1]*m0y + nframe.vertices[idx][1]*m1y;
	         			float z = cframe.vertices[idx][2]*m0z + nframe.vertices[idx][2]*m1z;
	         			m.verticesBuffer.put(x);
	         			m.verticesBuffer.put(z);
	         			m.verticesBuffer.put(y);
	        		}
	        	m.verticesBuffer.flip();
	        	
//	         	for (int i = 0; i < m.numverts; i++)
//	            {
//         			float x = cframe.vertices[i][0]*m0x + nframe.vertices[i][0]*m1x;
//         			float y = cframe.vertices[i][1]*m0y + nframe.vertices[i][1]*m1y;
//         			float z = cframe.vertices[i][2]*m0z + nframe.vertices[i][2]*m1z;
//
//         			m.verticesBuffer.put(x);
//         			m.verticesBuffer.put(z);
//         			m.verticesBuffer.put(y);
//	            }
	         	
	        	int l = GL_TEXTURE0_ARB;
                do
                {
                    gl.bglClientActiveTextureARB(l++);
                    gl.bglEnableClientState(GL_TEXTURE_COORD_ARRAY);
                    gl.bglTexCoordPointer(2, 0, m.uv);
                } while (l <= texunits);
                
                gl.bglEnableClientState(GL_VERTEX_ARRAY);
                gl.bglVertexPointer(3, 0, m.verticesBuffer);
                gl.bglDrawElements(GL_TRIANGLES, m.indicesBuffer);
	        }
	        else
	        {
		    	int c = 0, cmd;
		    	while((cmd = m.glcmds[c++]) != 0)
		    	{
		    		if(cmd < 0) { gl.bglBegin(GL_TRIANGLE_FAN); cmd = -cmd; }
		    		else gl.bglBegin(GL_TRIANGLE_STRIP);
		    			
		    		for( /*nothing*/; cmd > 0; cmd--, c += 3)
		    		{
		    			float s = Float.intBitsToFloat(m.glcmds[c + 0]);
		    			float t = Float.intBitsToFloat(m.glcmds[c + 1]);
		
		    			float x = cframe.vertices[m.glcmds[c + 2]][0]*m0x + nframe.vertices[m.glcmds[c + 2]][0]*m1x;
		    			float y = cframe.vertices[m.glcmds[c + 2]][1]*m0y + nframe.vertices[m.glcmds[c + 2]][1]*m1y;
		    			float z = cframe.vertices[m.glcmds[c + 2]][2]*m0z + nframe.vertices[m.glcmds[c + 2]][2]*m1z;
		
		    			gl.bglTexCoord2d(s, t);
		    			gl.bglVertex3d(x, z, y);
		    		}
		    		gl.bglEnd();
		    	}

//		    	gl.bglBegin(GL_TRIANGLES);
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
//		    			gl.bglTexCoord2d(m.uv.get(2 * tIdx), m.uv.get(2 * tIdx + 1)); //uv rewrited for drawelements
//		    			gl.bglVertex3d(x, z, y);
//		    		}
//		    	}
//		    	gl.bglEnd();
	        }


	    	while (texunits > GL_TEXTURE0_ARB)
	        {
	        	gl.bglMatrixMode(GL_TEXTURE);
	            gl.bglLoadIdentity();
	            gl.bglMatrixMode(GL_MODELVIEW);
	            gl.bglTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE_ARB, 1.0f);
	            gl.bglDisable(GL_TEXTURE_2D);
	            if (r_vertexarrays != 0)
	            {
	                gl.bglDisableClientState(GL_TEXTURE_COORD_ARRAY);
	                gl.bglClientActiveTextureARB(texunits - 1);
	            }
	            gl.bglActiveTextureARB(--texunits);
	        }
	    	rendered = 1;
        }
    	
    	if (m.usesalpha) gl.bglDisable(GL_ALPHA_TEST);
    	gl.bglDisable(GL_CULL_FACE);
//    	gl.bglPopAttrib();
        gl.bglLoadIdentity();

        globalnoeffect=0;
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

//		gl.bglPushAttrib(GL_POLYGON_BIT); FIXME decreasing fps?
		if ((grhalfxdown10x >= 0) ^((globalorientation&8) != 0) ^((globalorientation&4) != 0)) gl.bglFrontFace(GL_CW); else gl.bglFrontFace(GL_CCW);

		gl.bglEnable(GL_CULL_FACE);
		gl.bglCullFace(GL_BACK);

		gl.bglEnable(GL_TEXTURE_2D);

		drawpoly_pc[0] = drawpoly_pc[1] = drawpoly_pc[2] = ((float) (numshades - min(max((globalshade * shadescale) + m.shadeoff, 0), numshades))) / ((float) numshades);
		drawpoly_pc[0] *= (float) hictinting[globalpal].r / 255.0f;
		drawpoly_pc[1] *= (float) hictinting[globalpal].g / 255.0f;
		drawpoly_pc[2] *= (float) hictinting[globalpal].b / 255.0f;
		if ((tspr.cstat & 2) != 0) {
			if ((tspr.cstat & 512) == 0)
				drawpoly_pc[3] = TRANSLUSCENT1;
			else
				drawpoly_pc[3] = TRANSLUSCENT2;
		} else
			drawpoly_pc[3] = 1.0f;
		if ((tspr.cstat & 2) != 0)
			gl.bglEnable(GL_BLEND);
		
		
		gl.bglMatrixMode(GL_MODELVIEW); // Let OpenGL (and perhaps hardware :) handle the matrix rotation
		
		boolean newmatrix = false;
		
		// ------------ Matrix
		if(!newmatrix)
			md3_vox_calcmat_common(tspr, modela0, f, matrix);
		else { md3_vox_calcmat_common(tspr, modela0); }

		// Mirrors
		if (grhalfxdown10x < 0) {
			if(!newmatrix) {
				matrix[0][0] = -matrix[0][0];
				matrix[1][0] = -matrix[1][0];
				matrix[2][0] = -matrix[2][0];
				matrix[3][0] = -matrix[3][0];
			} else {
				System.out.println("mirror");
				//XXX gl tra?
			}
		}
		
		if(!newmatrix) {
			matrix[0][3] = matrix[1][3] = matrix[2][3] = 0.f;
			matrix[3][3] = 1.f;
			gl.bglLoadMatrixf(matrix);
		}
		
		// transform to Build coords
		if ((tspr.cstat & 48) == 32) {
			gl.bglScalef(dvoxm0.x / 64.0f, dvoxm0.z / 64.0f, dvoxm0.y / 64.0f);
			gl.bglRotatef(90, 1.0f, 0.0f, 0.0f);
			gl.bglTranslatef(-m.xpiv, -m.ypiv, -m.zpiv);
			gl.bglRotatef(90, -1.0f, 0.0f, 0.0f);
			gl.bglTranslatef(0, -m.ypiv, -m.zpiv);
		} else {
			gl.bglScalef(dvoxm0.x / 64.0f, dvoxm0.z / 64.0f, dvoxm0.y / 64.0f);
			gl.bglRotatef(90, 1.0f, 0.0f, 0.0f);
			gl.bglTranslatef(-m.xpiv, -m.ypiv, -(m.zpiv + m.zsiz * 0.5f));
		}

		ru = 1.f / ((float) m.mytexx);
		rv = 1.f / ((float) m.mytexy);

		dvoxphack[0] = 0;
		dvoxphack[1] = 1.f / 256.f;

		if (m.texid[globalpal] == null)
			m.texid[globalpal] = gloadtex(m.mytex, m.mytexx, m.mytexy, globalpal, gl);
		else
			gl.bglBindTexture(GL_TEXTURE_2D, m.texid[globalpal]);
		
		if(r_vertexarrays != 0)
		{
			gl.bglColor4f(drawpoly_pc[0], drawpoly_pc[1], drawpoly_pc[2], drawpoly_pc[3]);
			gl.bglEnableClientState(GL_TEXTURE_COORD_ARRAY);
	        gl.bglTexCoordPointer(2, 0, m.uv);
			gl.bglEnableClientState(GL_VERTEX_ARRAY);
	        gl.bglVertexPointer(3, 0, m.verticesBuffer);
	        gl.bglDrawElements(GL_QUADS, m.indicesBuffer);
		} 
		else
		{
			gl.bglBegin(GL_QUADS);
			for (i = 0, fi = 0; i < m.qcnt; i++) {
				if (i == m.qfacind[fi]) {
					f = dvoxclut[fi++];
					gl.bglColor4f(drawpoly_pc[0] * f, drawpoly_pc[1] * f, drawpoly_pc[2] * f, drawpoly_pc[3] * f);
				}
	
				xx = m.quad[i].v[0].x + m.quad[i].v[2].x;
				yy = m.quad[i].v[0].y + m.quad[i].v[2].y;
				zz = m.quad[i].v[0].z + m.quad[i].v[2].z;
	
				for (j = 0; j < 4; j++) {
					gl.bglTexCoord2d((m.quad[i].v[j].u) * ru, (m.quad[i].v[j].v) * rv);
					dvoxfp.x = ((float) m.quad[i].v[j].x) - dvoxphack[(xx > (m.quad[i].v[j].x * 2)) ? 1 : 0] + dvoxphack[(xx < (m.quad[i].v[j].x * 2)) ? 1 : 0];
					dvoxfp.y = ((float) m.quad[i].v[j].y) - dvoxphack[(yy > (m.quad[i].v[j].y * 2)) ? 1 : 0] + dvoxphack[(yy < (m.quad[i].v[j].y * 2)) ? 1 : 0];
					dvoxfp.z = ((float) m.quad[i].v[j].z) - dvoxphack[(zz > (m.quad[i].v[j].z * 2)) ? 1 : 0] + dvoxphack[(zz < (m.quad[i].v[j].z * 2)) ? 1 : 0];
					gl.bglVertex3d(dvoxfp.x, dvoxfp.y, dvoxfp.z);
				}
			}
			gl.bglEnd();
		}
	
		// ------------
		gl.bglDisable(GL_CULL_FACE);
//		gl.bglPopAttrib();
		gl.bglLoadIdentity();

		return 1;
	}

	public int clippoly(int npoints, int clipstat) {
		int z, zz, s1, s2, t, npoints2, start2, z1, z2, z3, z4, splitcnt;
		int cx1, cy1, cx2, cy2;

		cx1 = windowx1;
		cy1 = windowy1;
		cx2 = windowx2 + 1;
		cy2 = windowy2 + 1;
		cx1 <<= 12;
		cy1 <<= 12;
		cx2 <<= 12;
		cy2 <<= 12;

		if ((clipstat & 0xa) != 0) // Need to clip top or left
		{
			npoints2 = 0;
			start2 = 0;
			z = 0;
			splitcnt = 0;
			do {
				s2 = (int) (cx1 - rx1[z]);
				do {
					zz = (int) xb1[z];
					xb1[z] = -1;
					s1 = s2;
					s2 = (int) (cx1 - rx1[zz]);
					if (s1 < 0) {
						rx2[npoints2] = rx1[z];
						ry2[npoints2] = ry1[z];
						xb2[npoints2] = npoints2 + 1;
						npoints2++;
					}
					if ((s1 ^ s2) < 0) {
						rx2[npoints2] = rx1[z] + scale((int) (rx1[zz] - rx1[z]), s1, s1 - s2);
						ry2[npoints2] = ry1[z] + scale((int) (ry1[zz] - ry1[z]), s1, s1 - s2);
						if (s1 < 0)
							p2[splitcnt++] = (short) npoints2;
						xb2[npoints2] = npoints2 + 1;
						npoints2++;
					}
					z = zz;
				} while (xb1[z] >= 0);

				if (npoints2 >= start2 + 3) {
					xb2[npoints2 - 1] = start2;
					start2 = npoints2;
				} else
					npoints2 = start2;

				z = 1;
				while ((z < npoints) && (xb1[z] < 0))
					z++;
			} while (z < npoints);
			if (npoints2 <= 2)
				return (0);

			for (z = 1; z < splitcnt; z++)
				for (zz = 0; zz < z; zz++) {
					z1 = p2[z];
					z2 = (int) xb2[z1];
					z3 = p2[zz];
					z4 = (int) xb2[z3];
					s1 = (int) (abs(rx2[z1] - rx2[z2]) + abs(ry2[z1] - ry2[z2]));
					s1 += abs(rx2[z3] - rx2[z4]) + abs(ry2[z3] - ry2[z4]);
					s2 = (int) (abs(rx2[z1] - rx2[z4]) + abs(ry2[z1] - ry2[z4]));
					s2 += abs(rx2[z3] - rx2[z2]) + abs(ry2[z3] - ry2[z2]);
					if (s2 < s1) {
						t = (int) xb2[p2[z]];
						xb2[p2[z]] = xb2[p2[zz]];
						xb2[p2[zz]] = t;
					}
				}

			npoints = 0;
			start2 = 0;
			z = 0;
			splitcnt = 0;
			do {
				s2 = (int) (cy1 - ry2[z]);
				do {
					zz = (int) xb2[z];
					xb2[z] = -1;
					s1 = s2;
					s2 = (int) (cy1 - ry2[zz]);
					if (s1 < 0) {
						rx1[npoints] = rx2[z];
						ry1[npoints] = ry2[z];
						xb1[npoints] = npoints + 1;
						npoints++;
					}
					if ((s1 ^ s2) < 0) {
						rx1[npoints] = rx2[z] + scale((int) (rx2[zz] - rx2[z]), s1, s1 - s2);
						ry1[npoints] = ry2[z] + scale((int) (ry2[zz] - ry2[z]), s1, s1 - s2);
						if (s1 < 0)
							p2[splitcnt++] = (short) npoints;
						xb1[npoints] = npoints + 1;
						npoints++;
					}
					z = zz;
				} while (xb2[z] >= 0);

				if (npoints >= start2 + 3) {
					xb1[npoints - 1] = start2;
					start2 = npoints;
				} else
					npoints = start2;

				z = 1;
				while ((z < npoints2) && (xb2[z] < 0))
					z++;
			} while (z < npoints2);
			if (npoints <= 2)
				return (0);

			for (z = 1; z < splitcnt; z++)
				for (zz = 0; zz < z; zz++) {
					z1 = p2[z];
					z2 = (int) xb1[z1];
					z3 = p2[zz];
					z4 = (int) xb1[z3];
					s1 = (int) (abs(rx1[z1] - rx1[z2]) + abs(ry1[z1] - ry1[z2]));
					s1 += abs(rx1[z3] - rx1[z4]) + abs(ry1[z3] - ry1[z4]);
					s2 = (int) (abs(rx1[z1] - rx1[z4]) + abs(ry1[z1] - ry1[z4]));
					s2 += abs(rx1[z3] - rx1[z2]) + abs(ry1[z3] - ry1[z2]);
					if (s2 < s1) {
						t = (int) xb1[p2[z]];
						xb1[p2[z]] = xb1[p2[zz]];
						xb1[p2[zz]] = t;
					}
				}
		}

		if ((clipstat & 0x5) != 0) // Need to clip bottom or right
		{
			npoints2 = 0;
			start2 = 0;
			z = 0;
			splitcnt = 0;
			do {
				s2 = (int) (rx1[z] - cx2);
				do {
					zz = (int) xb1[z];
					xb1[z] = -1;
					s1 = s2;
					s2 = (int) (rx1[zz] - cx2);
					if (s1 < 0) {
						rx2[npoints2] = rx1[z];
						ry2[npoints2] = ry1[z];
						xb2[npoints2] = npoints2 + 1;
						npoints2++;
					}
					if ((s1 ^ s2) < 0) {
						rx2[npoints2] = rx1[z] + scale((int) (rx1[zz] - rx1[z]), s1, s1 - s2);
						ry2[npoints2] = ry1[z] + scale((int) (ry1[zz] - ry1[z]), s1, s1 - s2);
						if (s1 < 0)
							p2[splitcnt++] = (short) npoints2;
						xb2[npoints2] = npoints2 + 1;
						npoints2++;
					}
					z = zz;
				} while (xb1[z] >= 0);

				if (npoints2 >= start2 + 3) {
					xb2[npoints2 - 1] = start2;
					start2 = npoints2;
				} else
					npoints2 = start2;

				z = 1;
				while ((z < npoints) && (xb1[z] < 0))
					z++;
			} while (z < npoints);
			if (npoints2 <= 2)
				return (0);

			for (z = 1; z < splitcnt; z++)
				for (zz = 0; zz < z; zz++) {
					z1 = p2[z];
					z2 = (int) xb2[z1];
					z3 = p2[zz];
					z4 = (int) xb2[z3];
					s1 = (int) (abs(rx2[z1] - rx2[z2]) + abs(ry2[z1] - ry2[z2]));
					s1 += abs(rx2[z3] - rx2[z4]) + abs(ry2[z3] - ry2[z4]);
					s2 = (int) (abs(rx2[z1] - rx2[z4]) + abs(ry2[z1] - ry2[z4]));
					s2 += abs(rx2[z3] - rx2[z2]) + abs(ry2[z3] - ry2[z2]);
					if (s2 < s1) {
						t = (int) xb2[p2[z]];
						xb2[p2[z]] = xb2[p2[zz]];
						xb2[p2[zz]] = t;
					}
				}

			npoints = 0;
			start2 = 0;
			z = 0;
			splitcnt = 0;
			do {
				s2 = (int) (ry2[z] - cy2);
				do {
					zz = (int) xb2[z];
					xb2[z] = -1;
					s1 = s2;
					s2 = (int) (ry2[zz] - cy2);
					if (s1 < 0) {
						rx1[npoints] = rx2[z];
						ry1[npoints] = ry2[z];
						xb1[npoints] = npoints + 1;
						npoints++;
					}
					if ((s1 ^ s2) < 0) {
						rx1[npoints] = rx2[z] + scale((int) (rx2[zz] - rx2[z]), s1, s1 - s2);
						ry1[npoints] = ry2[z] + scale((int) (ry2[zz] - ry2[z]), s1, s1 - s2);
						if (s1 < 0)
							p2[splitcnt++] = (short) npoints;
						xb1[npoints] = npoints + 1;
						npoints++;
					}
					z = zz;
				} while (xb2[z] >= 0);

				if (npoints >= start2 + 3) {
					xb1[npoints - 1] = start2;
					start2 = npoints;
				} else
					npoints = start2;

				z = 1;
				while ((z < npoints2) && (xb2[z] < 0))
					z++;
			} while (z < npoints2);
			if (npoints <= 2)
				return (0);

			for (z = 1; z < splitcnt; z++)
				for (zz = 0; zz < z; zz++) {
					z1 = p2[z];
					z2 = (int) xb1[z1];
					z3 = p2[zz];
					z4 = (int) xb1[z3];
					s1 = (int) (abs(rx1[z1] - rx1[z2]) + abs(ry1[z1] - ry1[z2]));
					s1 += abs(rx1[z3] - rx1[z4]) + abs(ry1[z3] - ry1[z4]);
					s2 = (int) (abs(rx1[z1] - rx1[z4]) + abs(ry1[z1] - ry1[z4]));
					s2 += abs(rx1[z3] - rx1[z2]) + abs(ry1[z3] - ry1[z2]);
					if (s2 < s1) {
						t = (int) xb1[p2[z]];
						xb1[p2[z]] = xb1[p2[zz]];
						xb1[p2[zz]] = t;
					}
				}
		}
		return (npoints);
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
		Palette p = engine.getpal(dacol);
		if (p == null) {
			gl.bglClearColor(dacol / 255.0, dacol / 255.0, dacol / 255.0, 1);
			gl.bglClear(GL_COLOR_BUFFER_BIT);
			
			return;
		}
		gl.bglClearColor(((float) p.r) / 255.0,
				((float) p.g) / 255.0,
				((float) p.b) / 255.0,
				0);
		gl.bglClear(GL_COLOR_BUFFER_BIT);
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
	}

	@Override
	public void getFrameBuffer(int x, int y, int w, int h, int format, ByteBuffer pixels) {
		gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
		gl.glReadPixels(x, y, w, h, format, GL10.GL_UNSIGNED_BYTE, pixels);
	}

	@Override
	public void preload() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public abstract void drawmapview(int dax, int day, int zoome, int ang);
	
	@Override
	public abstract void drawoverheadmap(int cposx, int cposy, int czoom, short cang);

	@Override
	public void settiltang(int tilt) {
	    if (tilt == 0) gtang = 0.0f;
	    else gtang = (float) (PI * tilt / 1024.0);
	}

	@Override
	public void setdrunk(float intensive) {
		if(intensive == 0.0f) {
			drunk = false;
			drunkIntensive = 1.0f;
		}
		else {
			drunk = true;
			if(intensive < MaxDrunkIntensive)
				intensive = MaxDrunkIntensive;
			drunkIntensive = intensive;
		}
	}

	@Override
	public float getdrunk() {
		return drunkIntensive;
	}

	// private int recheck(int x, int y, int oldx, int j, SPRITE tspr, short datempsectnum) {
	// updatesectorz(tspr.x + x, tspr.y + y, tspr.z, datempsectnum);
	//
	// if (datempsectnum == -1) {
	// if (x == y || x != oldx)
	// return 0;
	//
	// int tmp = x;
	// x = y; // swaplong(&x,&y);
	// y = tmp;
	//
	// updatesector(tspr.x + x, tspr.y + y, datempsectnum);
	// }
	//
	// int i = 4;
	// do {
	// cullcheckcnt += 2;
	// if (cansee(globalposx, globalposy, globalposz, globalcursectnum,
	// tspr.x + x, tspr.y + y, tspr.z - (j * i) - 512,
	// datempsectnum) != 0)
	// return 1;
	// if (cansee(globalposx, globalposy, globalposz, globalcursectnum,
	// tspr.x + x, tspr.y + y, tspr.z - (j * (i - 1)) - 512,
	// datempsectnum) != 0)
	// return 1;
	// i -= 2;
	// } while (i != 0);
	//
	// cullcheckcnt++;
	// if (cansee(globalposx, globalposy, globalposz, globalcursectnum, tspr.x
	// + x, tspr.y + y, tspr.z - 512, datempsectnum) != 0)
	// return 1;
	//
	// if (x != y && x == oldx) {
	// int tmp = x;
	// x = y; // swaplong(&x,&y);
	// y = tmp;
	//
	// recheck(x, y, oldx, j, tspr, datempsectnum);
	// }
	// return 0;
	// }
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

class vsptyp {
	float x, cy[] = new float[2], fy[] = new float[2];
	int n, p, tag, ctag, ftag;

	public void set(vsptyp src) {
		this.x = src.x;
		for (int i = 0; i < 2; i++)
			this.cy[i] = src.cy[i];
		for (int i = 0; i < 2; i++)
			this.fy[i] = src.fy[i];
		this.n = src.n;
		this.p = src.p;
		this.tag = src.tag;
		this.ctag = src.ctag;
		this.ftag = src.ftag;
	}
};
