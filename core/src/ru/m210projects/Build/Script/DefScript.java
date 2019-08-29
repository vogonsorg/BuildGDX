/*
 * Definitions file parser for Build
 * by Jonathon Fowler (jf@jonof.id.au)
 * Remixed substantially by Ken Silverman
 * See the included license file "BUILDLIC.TXT" for license info.
 * 
 * This file has been modified
 * by the EDuke32 team (development@voidpoint.com)
 * by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Script;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Loader.Model.*;
import static ru.m210projects.Build.Strhandler.toLowerCase;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Disposable;

import ru.m210projects.Build.CRC32;
import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.FileEntry;
import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.FileHandle.Resource.ResourceData;
import ru.m210projects.Build.Loader.MDModel;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Loader.MD2.MD2Loader;
import ru.m210projects.Build.Loader.MD3.MD3Loader;
import ru.m210projects.Build.Loader.Voxels.KVXLoader;
import ru.m210projects.Build.Loader.Voxels.Voxel;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class DefScript implements Disposable {

	private boolean disposable;
	public TextureHDInfo texInfo;
	public ModelInfo mdInfo;
	public AudioInfo audInfo;
	private Engine engine;

	class DefTile {
		long crc32;
		byte[] waloff;
		short sizx, sizy, oldx, oldy;
		int picanm, oldanm;
		String hrp;
		byte alphacut;
		
		DefTile next;
		
		public DefTile(DefTile src)
		{
			this.crc32 = src.crc32;
			this.waloff = src.waloff;
			this.sizx = src.sizx;
			this.sizy = src.sizy;
			this.picanm = src.picanm;
			this.oldx = src.oldx;
			this.oldy = src.oldy;
			this.oldanm = src.oldanm;
			this.hrp = src.hrp;
			this.alphacut = src.alphacut;

			if(src.next != null)
				this.next = new DefTile(src.next);	
		}
		
		public DefTile(int sizx, int sizy, long crc32)
		{
			this.sizx = (short) sizx;
			this.sizy = (short) sizy;
			this.crc32 = crc32;
		}
		
		public DefTile getLast() {
			DefTile out = this;
			while(true) {
				DefTile n = out.next;
				if(n == null) 
					return out;
				out = n;
			}
		}
	}
	
	private DefTile[] tiles = new DefTile[MAXTILES];

	public DefScript(DefScript src) {
		this.disposable = true;
		this.texInfo = new TextureHDInfo(src.texInfo);
		this.mdInfo = new ModelInfo(src.mdInfo, src.disposable);
		this.audInfo = new AudioInfo(src.audInfo);
		this.engine = src.engine;
		for(int i = 0; i < MAXTILES; i++) {
			if(src.tiles[i] == null) continue;
			
			this.tiles[i] = new DefTile(src.tiles[i]);
		}
	}
	
	public DefScript(boolean disposable) {
		this.disposable = disposable;
		texInfo = new TextureHDInfo();
		mdInfo = new ModelInfo();
		audInfo = new AudioInfo();
	}

	protected int modelskin = -1, lastmodelskin = -1, seenframe = 0;

	private static final String skyfaces[] = 
	{ 
		"front face", 
		"right face", 
		"back face", 
		"left face", 
		"top face",
		"bottom face" 
	};

	private static enum Token {
		EOF,
		ERROR,
		INCLUDE,
		DEFINE,
		DEFINETEXTURE,
		DEFINESKYBOX,
		DEFINETINT,
		DEFINEMODEL,
		DEFINEMODELFRAME,
		DEFINEMODELANIM,
		DEFINEMODELSKIN,
		SELECTMODELSKIN,
		DEFINEVOXEL,
		DEFINEVOXELTILES,
		MODEL,
		FILE,
		SCALE,
		SHADE,
		FRAME,
		ANIM,
		SKIN,
		SURF,
		TILE,
		TILE0,
		TILE1,
		FRAME0,
		FRAME1,
		FPS,
		FLAGS,
		PAL,
		HUD,
		XADD,
		YADD,
		ZADD,
		ANGADD,
		FLIPPED,
		HIDE,
		NOBOB,
		NODEPTH,
		VOXEL,
		SKYBOX,
		FRONT,RIGHT,BACK,LEFT,TOP,BOTTOM,
		TINT,RED,GREEN,BLUE,
		TEXTURE,ALPHACUT,
		UNDEFMODEL,UNDEFMODELRANGE,UNDEFMODELOF,UNDEFTEXTURE,UNDEFTEXTURERANGE,
		SOUND,
		MUSIC,
		ROTATE,
		ID,
		T_TILEFROMTEXTURE,
		
		XOFFSET,
		YOFFSET,
		SMOOTHDURATION,
		DETAIL,
		GLOW,
		SPECULAR,
		NORMAL,
	 	PARAM,
    	SPECPOWER,
    	SPECFACTOR,
    	FOV,
		XSCALE,
	    YSCALE,
	    NOCOMPRESS,
	    NODOWNSIZE,
	    CRC,
    	
		;
	};
	
	private final static Map<String , Token> basetokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("include",         Token.INCLUDE          );
			put("#include",        Token.INCLUDE          );
			put("define",          Token.DEFINE           );
			put("#define",         Token.DEFINE           );

			// deprecated style
			put("definetexture",   Token.DEFINETEXTURE    );
			put("defineskybox",    Token.DEFINESKYBOX     );
			put("definetint",      Token.DEFINETINT       );
			put("definemodel",     Token.DEFINEMODEL      );
			put("definemodelframe",Token.DEFINEMODELFRAME );
			put("definemodelanim", Token.DEFINEMODELANIM  );
			put("definemodelskin", Token.DEFINEMODELSKIN  );
			put("selectmodelskin", Token.SELECTMODELSKIN  );
			put("definevoxel",     Token.DEFINEVOXEL      );
			put("definevoxeltiles",Token.DEFINEVOXELTILES );

			// new style
			put("model",             Token.MODEL             );
			put("voxel",             Token.VOXEL             );
			put("skybox",            Token.SKYBOX            );
			put("tint",              Token.TINT              );
			put("texture",           Token.TEXTURE           );
			put("tile",              Token.TEXTURE           );
			put("undefmodel",        Token.UNDEFMODEL        );
			put("undefmodelrange",   Token.UNDEFMODELRANGE   );
			put("undefmodelof",      Token.UNDEFMODELOF      );
			put("undeftexture",      Token.UNDEFTEXTURE      );
			put("undeftexturerange", Token.UNDEFTEXTURERANGE );
			
			// other stuff
			put("tilefromtexture",   Token.T_TILEFROMTEXTURE );
			
			//gdx
			put("music", Token.MUSIC );
			put("sound", Token.SOUND );
		}
	};
	
	private final static Map<String , Token> sound_musictokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("id",     Token.ID);
			put("file",	  Token.FILE);
		}
    };
	
	private final static Map<String , Token> modeltokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("scale",  Token.SCALE  );
			put("shade",  Token.SHADE  );
			put("zadd",   Token.ZADD   );
			put("frame",  Token.FRAME  );
			put("anim",   Token.ANIM   );
			put("skin",   Token.SKIN   );
			put("hud",    Token.HUD    );
		}
	};

	private final static Map<String , Token> modelframetokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("frame",  Token.FRAME   );
			put("name",   Token.FRAME   );
			put("tile",   Token.TILE   );
			put("tile0",  Token.TILE0  );
			put("tile1",  Token.TILE1  );
		}
	};

	private final static Map<String , Token> modelanimtokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("frame0", Token.FRAME0 );
			put("frame1", Token.FRAME1 );
			put("fps",    Token.FPS    );
			put("flags",  Token.FLAGS  );
		}
	};

	private final static Map<String , Token> modelskintokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("pal",    Token.PAL    );
			put("file",   Token.FILE   );
			put("surf",   Token.SURF   );
			put("surface",Token.SURF   );
		}
	};

	private final static Map<String , Token> modelhudtokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("tile",   Token.TILE   );
			put("tile0",  Token.TILE0  );
			put("tile1",  Token.TILE1  );
			put("xadd",   Token.XADD   );
			put("yadd",   Token.YADD   );
			put("zadd",   Token.ZADD   );
			put("angadd", Token.ANGADD );
			put("hide",   Token.HIDE   );
			put("nobob",  Token.NOBOB  );
			put("flipped",Token.FLIPPED);
			put("nodepth",Token.NODEPTH);
		}
	};

	private final static Map<String , Token> voxeltokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("tile",   Token.TILE   );
			put("tile0",  Token.TILE0  );
			put("tile1",  Token.TILE1  );
			put("scale",  Token.SCALE  );
			put("rotate", Token.ROTATE);
		}
	};

	private final static Map<String , Token> skyboxtokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("tile"   ,Token.TILE   );
			put("pal"    ,Token.PAL    );
			put("ft"     ,Token.FRONT  );put("front"  ,Token.FRONT  );put("forward",Token.FRONT  );
			put("rt"     ,Token.RIGHT  );put("right"  ,Token.RIGHT  );
			put("bk"     ,Token.BACK   );put("back"   ,Token.BACK   );
			put("lf"     ,Token.LEFT   );put("left"   ,Token.LEFT   );put("lt"     ,Token.LEFT   );
			put("up"     ,Token.TOP    );put("top"    ,Token.TOP    );put("ceiling",Token.TOP    );put("ceil"   ,Token.TOP    );
			put("dn"     ,Token.BOTTOM );put("bottom" ,Token.BOTTOM );put("floor"  ,Token.BOTTOM );put("down"   ,Token.BOTTOM );
		}
	}; 

	private final static Map<String , Token> tinttokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("pal",   Token.PAL );
			put("red",   Token.RED   );put("r", Token.RED );
			put("green", Token.GREEN );put("g", Token.GREEN );
			put("blue",  Token.BLUE  );put("b", Token.BLUE );
			put("flags", Token.FLAGS );
		}
	};

	private final static Map<String , Token> texturetokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("pal",    			Token.PAL);
			put("detail",    		Token.DETAIL);
			put("glow",    			Token.GLOW);
			put("specular",    		Token.SPECULAR);
			put("normal",    		Token.NORMAL);

			put( "file",            Token.FILE );
			put( "name", 			Token.FILE );
            put( "alphacut",        Token.ALPHACUT );
            put( "detailscale",     Token.XSCALE ); 
            put( "scale",  			Token.XSCALE ); 
            put( "xscale", 			Token.XSCALE ); 
            put( "intensity",  		Token.XSCALE );
            put( "yscale",          Token.YSCALE );
            put( "specpower",       Token.SPECPOWER ); 
            put( "specularpower", 	Token.SPECPOWER ); 
            put( "parallaxscale", 	Token.SPECPOWER );
            put( "specfactor",      Token.SPECFACTOR ); 
            put( "specularfactor", 	Token.SPECFACTOR ); 
            put( "parallaxbias", 	Token.SPECFACTOR );
            put( "nocompress",      Token.NOCOMPRESS );
            put( "nodownsize",      Token.NODOWNSIZE );
		}
	};
	
	private final static Map<String , Token> texturetokens_pal = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("file",     Token.FILE );
			put("name", Token.FILE );
			put("alphacut", Token.ALPHACUT );
		}
	};
	
	private final static Map<String , Token> tilefromtexturetokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			 put( "file",            Token.FILE );
			 put( "name",            Token.FILE );
			 put( "alphacut",        Token.ALPHACUT );
			 put( "xoffset",         Token.XOFFSET );
			 put( "xoff",            Token.XOFFSET );
			 put( "yoffset",         Token.YOFFSET );
			 put( "yoff",            Token.YOFFSET );
			 put( "texture",         Token.TEXTURE );
			 put( "ifcrc",         	 Token.CRC );
		}
	};

	public boolean loadScript(FileEntry file)
	{
		if(file == null)
		{
			Console.Println("Def error: script not found", OSDTEXT_RED);
			return false;
		}
		
		Resource res = BuildGdx.compat.open(file);
		byte[] data = res.getBytes();
		res.close();
		
		if(data == null) {
			Console.Println("File is exists, but data == null! Path:" + file.getPath());
			return false;
		}
		
		Scriptfile script = new Scriptfile(file.getPath(), data);
		script.path = file.getParent().getRelativePath();
		
		try {
			defsparser(script);
		} catch (Exception e) {
			e.printStackTrace();
			Console.Println("Def error: the script " + file.getPath() + " has errors", OSDTEXT_RED);
			return false;
		}
		
		return true;
	}
	
	public boolean loadScript(String name, byte[] buf)
	{
		if (buf == null) {
			Console.Println("Def error: script not found", OSDTEXT_RED);
			return false;
		}
		
		try {
			defsparser(new Scriptfile(name, buf));
		} catch (Exception e) {
			e.printStackTrace();
			Console.Println("Def error: the script " + name + " has errors", OSDTEXT_RED);
			return false;
		}
		
		return true;
	}

	private Token gettoken(Scriptfile sf, Map<String , Token> list) {
		int tok;
		if (sf == null) return Token.ERROR;
		if ((tok = sf.gettoken()) == -2) 
			return Token.EOF;

		Token out = list.get(toLowerCase(sf.textbuf.substring(tok, sf.textptr)));
		if (out != null)
			return out;

		sf.errorptr = sf.textptr;
		return Token.ERROR;
	}
	
	public void setEngine(Engine engine)
	{
		this.engine = engine;
	}
	
	private DefTile ImportTileFromTexture(String fn, int tile, long crc32, int alphacut, boolean istexture)
	{
		byte[] data = BuildGdx.cache.getBytes(fn, 0);
		if (data == null)
			return null;

		Pixmap pix = new Pixmap(data, 0, data.length);
		pix.setFilter(Filter.NearestNeighbour);
		
		Format fmt = pix.getFormat();

		int xsiz = pix.getWidth();
		int ysiz = pix.getHeight();
		
		DefTile deftile = new DefTile(xsiz, ysiz, crc32);
		deftile.waloff = new byte[xsiz * ysiz];
		deftile.oldanm = picanm[tile];
		deftile.oldx = tilesizx[tile];
		deftile.oldy = tilesizy[tile];

		ByteBuffer bb = pix.getPixels();
		byte[] waloff = deftile.waloff;
		
		for(int y = 0; y < ysiz; y++)
			for(int x = 0; x < xsiz; x++) {
				int r = (bb.get() & 0xFF) >> 2;
				int g = (bb.get() & 0xFF) >> 2;
				int b = (bb.get() & 0xFF) >> 2;
				if(fmt == Format.RGBA4444 || fmt == Format.RGBA8888) {
					if(bb.get() == 0) 
						waloff[x * ysiz + y] = -1;
					else waloff[x * ysiz + y] = engine.getclosestcol(r, g, b);
				} else waloff[x * ysiz + y] = engine.getclosestcol(r, g, b);
			}

		if (istexture) {
			deftile.hrp = fn;
			deftile.alphacut = (byte) alphacut;
		}

		return deftile;
	}
	
	protected void tilefromtextureparser(Scriptfile script) //XXX
	{
		Token token;
		int ttexturetokptr = script.ltextptr, ttextureend;
		String fn = null;
        Integer tile = -1, value;
        int talphacut = 255;
        boolean havexoffset = false, haveyoffset = false;
        int xoffset = 0, yoffset = 0;
        long tilecrc = 0;
        boolean istexture = false;
        
        if ((tile = script.getsymbol()) == null) return;
        if ((ttextureend = script.getbraces()) == -1) return;
        
        while (script.textptr < ttextureend)
        {
        	token = gettoken(script,tilefromtexturetokens);
        	switch (token)
            {
            default: break;
            case FILE:
            	fn = getFile(script);
                break;
            case ALPHACUT:
            	value = script.getsymbol();
            	if(value != null)
            	talphacut = value;
                talphacut = BClipRange(talphacut, 0, 255);
                break;
            case XOFFSET:
            	String xoffs = script.getstring();
            	if(xoffs.toUpperCase().equals("ART"))
            		xoffset =  (picanm[tile] & 0x0000FF00) >> 8;
            	else {
            		try {
            			xoffset = Byte.parseByte(xoffs);
            		} catch (Exception e) { Console.Println("Xoffset value out of range. Value: \"" + xoffs + "\" was disabled.", OSDTEXT_RED); break; }
            	}
            	xoffset = BClipRange(xoffset, -128, 127);
	            havexoffset = true;
                break;
            case YOFFSET:
            	String yoffs = script.getstring();
            	if(yoffs.toUpperCase().equals("ART"))
            		yoffset =  (picanm[tile] & 0x00FF0000) >> 16;
            	else {
            		try {
            			yoffset = Byte.parseByte(yoffs);
            		} catch (Exception e) { Console.Println("Yoffset value out of range. Value: \"" + yoffs + "\" was disabled.", OSDTEXT_RED); break; }
            	}
            	yoffset = BClipRange(yoffset, -128, 127);
	            haveyoffset = true;
                break;
            case TEXTURE:
                istexture = true;
                break;
            case CRC:
            	tilecrc = script.getsymbol() & 0xFFFFFFFFL;
            	break;
            }
        }
        
        if(tile < 0 || tile >= MAXTILES)
        {
        	Console.Println("Error: missing or invalid 'tile number' for texture definition near line " + script.filename + ":" + script.getlinum(ttexturetokptr), OSDTEXT_RED);
        	return;
        }
        
        if (fn == null)
        {
            // tilefromtexture <tile> { texhitscan }  sets the bit but doesn't change tile data
        	
        	DefTile deftile = new DefTile(tilesizx[tile], tilesizy[tile], tilecrc);
        	deftile.oldanm = picanm[tile];
    		deftile.oldx = tilesizx[tile];
    		deftile.oldy = tilesizy[tile];
    		
            if (havexoffset) {
            	deftile.picanm &= ~0x0000FF00;
            	deftile.picanm |= (xoffset & 0xFF) << 8;
            }
            if (haveyoffset) {
            	deftile.picanm &= ~0x00FF0000;
            	deftile.picanm |= (yoffset & 0xFF) << 16;
            }

            if (!havexoffset && !haveyoffset)
            	Console.Println("Error: missing 'file name' for tilefromtexture definition near line " + script.filename + ":" + script.getlinum(ttexturetokptr), OSDTEXT_RED);

            DefTile def = tiles[tile];
        	if(def != null && def.crc32 != 0) {
        		def = tiles[tile].getLast();
        		def.next = deftile;
        	} else tiles[tile] = deftile;
        	
            return;
        }
        
        DefTile texstatus = ImportTileFromTexture(fn, tile, tilecrc, talphacut, istexture);
//        if (texstatus == -3)
//        	Console.Println("Error: No palette loaded, in tilefromtexture definition near line " + script.filename + ":" + script.getlinum(ttexturetokptr), OSDTEXT_RED);
//        if (texstatus == -(3<<8))
//        	Console.Println("Error: \"" + fn +  "\" has more than one tile, in tilefromtexture definition near line " + script.filename + ":" + script.getlinum(ttexturetokptr), OSDTEXT_RED);
        if (texstatus == null)
        	return;
        
        texstatus.picanm &= ~0x00FFFF00;
        if (havexoffset)
        	texstatus.picanm |= (xoffset & 0xFF) << 8;
        if (haveyoffset)
        	texstatus.picanm |= (yoffset & 0xFF) << 16;

        DefTile def = tiles[tile];
    	if(def != null && def.crc32 != 0) {
    		def = tiles[tile].getLast();
    		def.next = texstatus;
    	} else if(def == null || disposable) {
    		tiles[tile] = texstatus;
    	} else 	
    		Console.Println("Error: \"" + fn +  "\" has more than one tile, in tilefromtexture definition near line " + script.filename + ":" + script.getlinum(ttexturetokptr), OSDTEXT_RED);
	}
	
	private String getFile(Scriptfile script)
	{
		String fn = script.getstring();
		if(fn == null) return null;
		
		if(script.path != null)
			fn = script.path + File.separator + fn;
		
		return fn;
	}
	
	private void defsparser(Scriptfile script)
    {
		String fn;
		Token token;
		ResourceData buffer;
		Integer ivalue;
		Double dvalue;
		
		Console.Println("Loading " + script.filename + "...");
		
		while (true)
        {
			switch(gettoken(script, basetokens))
			{
			case T_TILEFROMTEXTURE:
				tilefromtextureparser(script);
				break;
			case INCLUDE:
    			if ((fn = getFile(script)) == null) break;
                include(fn, script, script.ltextptr);
				break;
			case MODEL:
				int modelend;
    			String modelfn;
    			double mdscale=1.0, mzadd=0.0, myoffset=0.0;
    	        int shadeoffs=0, mdflags=0;
    	        int model_ok = 1;

    			modelskin = lastmodelskin = 0;
    	        seenframe = 0;
    	        
    	        if ((modelfn = script.getstring()) == null) break;
                if ((modelend = script.getbraces()) == -1) break;
                
                Resource res = BuildGdx.cache.open(modelfn, 0);
        		if(res == null) {
        			Console.Println("Warning: File not found" + modelfn, OSDTEXT_YELLOW);
                    script.textptr = modelend+1;
                    break;
        		}
        		
        		buffer = res.getData();
        		
        		Model m = null;
        	    switch (buffer.getInt(0))
        	    {
        		    case 0x32504449: //IDP2
        		        m = MD2Loader.load(buffer);
        		        break;
        		    case 0x33504449: //IDP3
        		        m = MD3Loader.load(buffer);
        		        break; 
        		    default:
        		    	if (res.getExtension().equals("kvx"))
                		    m = KVXLoader.load(buffer).model;  
        		    	break;
        	    }
        	    res.close();

                if (m == null)
                {
                	Console.Println("Warning: Failed loading MD2/MD3 model " + modelfn, OSDTEXT_YELLOW);
                    script.textptr = modelend+1;
                    break;
                }
                
                while (script.textptr < modelend)
                {
                	token = gettoken(script,modeltokens);
                	switch (token)
                    {
                		default: break;
	                    case SCALE:
	                    	dvalue = script.getdouble();
	                    	if(dvalue != null)
	                    		mdscale = dvalue; 
	                    	break;
	                    case SHADE:
	                    	if((ivalue = script.getsymbol()) != null)
	                    	shadeoffs = ivalue; break;
	                    case ZADD:
	                    	if((dvalue = script.getdouble()) != null)
	                    	mzadd = dvalue; break;
	                    case YOFFSET:
	                    	if((dvalue = script.getdouble()) != null)
	                    	myoffset = dvalue; break;
	                    case FLAGS:
	                    	if((ivalue = script.getsymbol()) != null)
	                    		mdflags = ivalue; break;
	                    case FRAME:
	                    {
	                    	int frametokptr = script.ltextptr;
	                        int frameend, happy=1;
	                        String framename = null;
	                        int ftilenume = -1, ltilenume = -1, tilex = 0;
	                        double smoothduration = 0.1;

	    	    			if ((frameend = script.getbraces()) == -1) break;
	    	    			
	    	    			while (script.textptr < frameend)
    	                    {
	    	    				switch (gettoken(script,modelframetokens))
    	                        {
    	                        default: break;
    	                        case FRAME:
    	                        	framename = script.getstring(); break;
    	                        case TILE:
    	                        	if((ivalue = script.getsymbol()) != null) {
    	                        		ftilenume = ivalue; 
    	                        		ltilenume = ftilenume; 
    	                        	}
    	                        	break;
    	                        case TILE0:
    	                        	if((ivalue = script.getsymbol()) != null)
    	                        		ftilenume = ivalue = script.getsymbol(); 
    	                        	break; //first tile number
    	                        case TILE1:
    	                        	if((ivalue = script.getsymbol()) != null)
    	                        		ltilenume = ivalue; 
    	                        	break; //last tile number (inclusive)
    	                        case SMOOTHDURATION:
    	                        	if((dvalue = script.getdouble()) != null)
    	                        		smoothduration = dvalue; 
    	                        	break;
    	                        }
    	                    }
	    	    			
	    	    			if (check_tile_range("model: frame", ftilenume, ltilenume, script, frametokptr))
	                        {
	                            model_ok = 0;
	                            break;
	                        }

	                        for (tilex = ftilenume; tilex <= ltilenume && happy != 0; tilex++)
	                        {
	                            switch (mdInfo.addModelInfo(m, tilex, framename, Math.max(0, modelskin), (float) smoothduration))
	                            {
	                            case -1:
	                                happy = 0; break; // invalid model id!?
	                            case -2:
	                            	Console.Println("Invalid tile number on line " + script.filename + ":" + script.getlinum(frametokptr), OSDTEXT_RED);
	                                happy = 0;
	                                break;
	                            case -3:
	                            	Console.Println("Invalid frame name on line " + script.filename + ":" + script.getlinum(frametokptr), OSDTEXT_RED);
	                                happy = 0;
	                                break;
	                            default:
	                                break;
	                            }

	                            model_ok &= happy;
	                        }

	                        seenframe = 1;
	                    }
	                    break;
	                    case ANIM:
	                    {
	                    	int animtokptr = script.ltextptr;
	                    	int animend, happy=1;
	                    	String startframe = null, endframe = null;
	                    	int flags = 0;
	                        double dfps = 1.0;

	    	    			if ((animend = script.getbraces()) == -1) break;
	                        while (script.textptr < animend)
	                        {
	                            switch (gettoken(script,modelanimtokens))
	                            {
	                            default: break;
	                            case FRAME0:
	                            	startframe = script.getstring(); break;
	                            case FRAME1:
	                            	endframe = script.getstring(); break;
	                            case FPS:
	                            	if((dvalue = script.getdouble()) != null)
	                            		dfps = dvalue; break; //animation frame rate
	                            case FLAGS:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	flags = ivalue; break;
	                            }
	                        }
	                        
	                        if (startframe == null) {
	                        	Console.Println("Error: missing 'start frame' for anim definition near line " + script.filename + ":" + script.getlinum(animtokptr), OSDTEXT_RED); 
	                        	happy = 0; 
	                        }
	                        	
	                        if (endframe == null) {
	                        	Console.Println("Error: missing 'end frame' for anim definition near line " + script.filename + ":" + script.getlinum(animtokptr), OSDTEXT_RED); 
	                        	happy = 0;
	                        }
	                        	model_ok &= happy;
	                        if (happy == 0 || m.mdnum < 2) break;
	                        
	                        switch (((MDModel) m).setAnimation(startframe, endframe, (int)(dfps*(65536.0*.001)), flags))
	                        {
		                        case -2:
		                        	Console.Println("Invalid starting frame name on line " + script.filename + ":" + script.getlinum(animtokptr), OSDTEXT_RED);
		                            model_ok = 0;
		                            break;
		                        case -3:
		                        	Console.Println("Invalid ending frame name on line " + script.filename + ":" + script.getlinum(animtokptr), OSDTEXT_RED);
		                            model_ok = 0;
		                            break;
	                        }
	                    }
	                    break;
	                    case SKIN: case DETAIL: case GLOW: case SPECULAR: case NORMAL:
	                    {
	                    	int skintokptr = script.ltextptr;
	                        int skinend;
	                        String skinfn = null;
	                        int palnum = 0, surfnum = 0;
	                        double param = 1.0, specpower = 1.0, specfactor = 1.0;

	    	    			if ((skinend = script.getbraces()) == -1) break;
	                        while (script.textptr < skinend)
	                        {
	                            switch (gettoken(script,modelskintokens))
	                            {
	                            default: break;
	                            case PAL:
	                            	palnum = script.getsymbol(); break;
	                            case PARAM:
	                            	if((dvalue = script.getdouble()) != null)
	                            	param = dvalue; break;
	                            case SPECPOWER:
	                            	if((dvalue = script.getdouble()) != null)
	                            	specpower = dvalue; break;
	                            case SPECFACTOR:
	                            	if((dvalue = script.getdouble()) != null)
	                            	specfactor = dvalue; break;
	                            case FILE:
	                            	skinfn = getFile(script);
	                            	break; //skin filename
	                            case SURF:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	surfnum = ivalue; break; //getnumber
	                            }
	                        }
		    				
	                        if (skinfn == null)
	                        {
	                        	Console.Println("Error: missing 'skin filename' for skin definition near line " + script.filename + ":" + script.getlinum(skintokptr), OSDTEXT_RED);
	                            model_ok = 0;
	                            break;
	                        }
	                        
	                        if (seenframe != 0) { modelskin = ++lastmodelskin; }
	                        seenframe = 0;
	                        
	                        switch (token)
	                        {
	                        	default: break;
		                        case DETAIL:
		                        	palnum = DETAILPAL;
		                        	param = 1.0f / param;
		                            break;
		                        case GLOW:
		                        	palnum = GLOWPAL;
		                            break;
		                        case SPECULAR:
		                        	palnum = SPECULARPAL;
		                            break;
		                        case NORMAL:
		                        	palnum = NORMALPAL;
		                            break;
	                        }
	                        
	                        if (!BuildGdx.cache.contains(skinfn, 0) || m.mdnum < 2)
	                            break;

	                        switch (((MDModel) m).setSkin(skinfn, palnum, Math.max(0,modelskin), surfnum, param, specpower, specfactor))
	                        {
		                        case -2:
		                        	Console.Println("Invalid skin filename on line " + script.filename + ":" + script.getlinum(skintokptr), OSDTEXT_RED);
		                            model_ok = 0;
		                            break;
		                        case -3:
		                        	Console.Println("Invalid palette number on line " + script.filename + ":" + script.getlinum(skintokptr), OSDTEXT_RED);
		                            model_ok = 0;
		                            break;
	                        }
	                    }
	                    break;
	                    case HUD:
	                    {
	                    	int hudtokptr = script.ltextptr;
	                        int happy=1, frameend;
	                        int ftilenume = -1, ltilenume = -1, tilex = 0, flags = 0, fov = -1;
	                        double xadd = 0.0, yadd = 0.0, zadd = 0.0, angadd = 0.0;

	                        if ((frameend = script.getbraces()) == -1) break;
	                        while (script.textptr < frameend)
	                        {
	                        	switch (gettoken(script,modelhudtokens))
	                            {
	                            default: break;
	                            case TILE:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	ftilenume = ivalue; ltilenume = ftilenume; break;
	                            case TILE0:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	ftilenume = ivalue; break; //first tile number
	                            case TILE1:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	ltilenume = ivalue; break; //last tile number (inclusive)
	                            case XADD:
	                            	if((dvalue = script.getdouble()) != null)
	                            	xadd = dvalue; break;
	                            case YADD:
	                            	if((dvalue = script.getdouble()) != null)
	                            	yadd = dvalue; break;
	                            case ZADD:
	                            	if((dvalue = script.getdouble()) != null)
	                            	zadd = dvalue; break;
	                            case ANGADD:
	                            	if((dvalue = script.getdouble()) != null)
	                            	angadd = dvalue; break;
	                            case FOV:
	                            	if((ivalue = script.getsymbol()) != null)
	                            	fov = ivalue; break;
	                            case HIDE:
	                                flags |= 1; break;
	                            case NOBOB:
	                                flags |= 2; break;
	                            case FLIPPED:
	                                flags |= 4; break;
	                            case NODEPTH:
	                                flags |= 8; break;
	                            }
	                        }
	                        
	                        if (check_tile_range("hud", ftilenume, ltilenume, script, hudtokptr))
	                        {
	                            model_ok = 0;
	                            break;
	                        }

	                        for (tilex = ftilenume; tilex <= ltilenume && happy != 0; tilex++)
	                        {
	                            if(mdInfo.addHudInfo(tilex, xadd, yadd, zadd, (short) angadd, flags, fov) == -2) {
	                            	Console.Println("Invalid tile number on line " + script.filename + ":" + script.getlinum(hudtokptr), OSDTEXT_RED);
	                                happy = 0;
	                            }

	                            model_ok &= happy;
	                        }
	                    }
	                    break;
                    }
                }
                
                if (model_ok == 0)
                {
                    if (m != null)
                    {
                    	Console.Println("Removing model " + modelfn + " due to errors.", OSDTEXT_YELLOW);
                    	mdInfo.removeModelInfo(m);
                    }
                    break;
                }
                
                m.setMisc((float)mdscale,shadeoffs,(float)mzadd,(float)myoffset,mdflags);

                modelskin = lastmodelskin = 0;
                seenframe = 0;
				break;
			case TEXTURE:
				int textureend;
    			Integer ttile = -1;

    			if ((ttile = script.getsymbol()) == null) break;
                if ((textureend = script.getbraces()) == -1) break;
                
                while (script.textptr < textureend)
                {
                	token = gettoken(script,texturetokens);
                    switch (token)
                    {
                    default: break;
                    case PAL:
                    case DETAIL: 
                    case GLOW: 
                    case SPECULAR: 
                    case NORMAL:
                    	Integer tpal = -1;
                    	String tfn = null;
                    	double alphacut = -1.0, xscale = 1.0, yscale = 1.0, specpower = 1.0, specfactor = 1.0;
                    	int flags = 0;
                    	int palend;
                    	
                    	if (token == Token.PAL && (tpal = script.getsymbol()) == null) break;
                    	if ((palend = script.getbraces()) == -1) break;
                        while (script.textptr < palend)
                        {
                        	switch (gettoken(script, texturetokens))
                            {
                            	default: break;
	                            case FILE:
	                            	tfn = getFile(script);
	                            	break;
	                            case ALPHACUT:
	                            	if(token != Token.PAL)
	                            		break;
	                            	if((dvalue = script.getdouble()) != null)
	                            		alphacut = dvalue;
	                            	break;
	                            case XSCALE:
	                            	if((dvalue = script.getdouble()) != null)
	                            		xscale = dvalue; 
	                            	break;
	                            case YSCALE:
	                            	if((dvalue = script.getdouble()) != null)
	                            		yscale = dvalue; 
	                            	break;
	                            case SPECPOWER:
	                            	if((dvalue = script.getdouble()) != null)
	                            		specpower = dvalue; 
	                            	break;
	                            case SPECFACTOR:
	                            	if((dvalue = script.getdouble()) != null)
	                            		specfactor = dvalue; 
	                            	break;
	                            case NOCOMPRESS:
	                                flags |= 1; break;
	                            case NODOWNSIZE:
	                                flags |= 16; break;
                            }
                        }
                        
                        switch (token)
                        {
                        default: break;
                        case PAL:
                        	xscale = 1.0f / xscale;
	                        yscale = 1.0f / yscale;
                        	break;
                        case DETAIL:
                            tpal = DETAILPAL;
                            xscale = 1.0f / xscale;
                            yscale = 1.0f / yscale;
                            break;
                        case GLOW:
                            tpal = GLOWPAL;
                            break;
                        case SPECULAR:
                            tpal = SPECULARPAL;
                            break;
                        case NORMAL:
                            tpal = NORMALPAL;
                            break;
                        }
                        
                        if (ttile >= MAXTILES) break;	// message is printed later
                        if (token == Token.PAL && tpal >= MAXPALOOKUPS - RESERVEDPALS)
                        {
                        	Console.Println("Error: missing or invalid 'palette number' for texture definition near line " + script.filename + ":" + script.getlinum(script.ltextptr), OSDTEXT_RED);
                            break;
                        }
                        if (tfn == null) 
                        {
                        	Console.Println("Error: missing 'file name' for texture definition near line " + script.filename + ":" + script.getlinum(script.ltextptr), OSDTEXT_RED);
                            break;
                        }

                        if (!BuildGdx.cache.contains(tfn, 0))
                            break;
//                      Console.Println("Loading hires texture \"" + tfn + "\"");
                        
                        texInfo.addTexture(ttile.intValue(),tpal.intValue(),tfn,(float)alphacut,(float)xscale,(float)yscale, (float)specpower, (float)specfactor,flags);
                    	break;
                    }
                }

                if (ttile >= MAXTILES)
                {
                	Console.Println("Error: missing or invalid 'tile number' for texture definition near line " + script.filename + ":" + script.getlinum(script.ltextptr), OSDTEXT_RED);
                    break;
                }
				break;
			case VOXEL:
				int vmodelend;
				double vscale = 1.0;
    	        int tile0 = MAXTILES, tile1 = -1, tilex = -1;
    	        boolean vrotate = false;

    	        if ((fn = getFile(script)) == null) break; //voxel filename

                if ((vmodelend = script.getbraces()) == -1) break;
                
                buffer = BuildGdx.cache.getData(fn, 0);
        		if(buffer == null) {
        			Console.Println("Warning: File not found" + fn, OSDTEXT_YELLOW);
                    script.textptr = vmodelend+1;
                    break;
        		}
        		Voxel vox = KVXLoader.load(buffer); 
                if (vox == null)
                {
                	Console.Println("Warning: Failed loading MD2/MD3 model " + fn, OSDTEXT_YELLOW);
                    script.textptr = vmodelend+1;
                    break;
                }
               
                while (script.textptr < vmodelend)
                {
                    switch (gettoken(script, voxeltokens))
                    {
                    	case TILE:
                    		tilex = script.getsymbol();
                            if (check_tile("voxel", tilex, script, script.ltextptr))
                                break;

                            mdInfo.addVoxelInfo(vox, tilex);
                    		break;
                    	case TILE0:
                    		if((ivalue = script.getsymbol()) != null)
                    			tile0 = ivalue;
                            break; //1st tile #

                        case TILE1:
                        	if((ivalue = script.getsymbol()) != null)
                        		tile1 = ivalue;
                        	
                        	if (check_tile_range("voxel", tile0, tile1, script, script.ltextptr))
                        		break;
                            for (tilex=tile0; tilex<=tile1; tilex++) 
                            	mdInfo.addVoxelInfo(vox, tilex);
                            break; //last tile number (inclusive)
                        case SCALE:
                        	if((dvalue = script.getdouble()) != null)
                        		vscale = dvalue;
                            break;
                        case ROTATE:
                        	vrotate = true;
                        	break;
                        default:
                        	break;
                    }
                }
                vox.getModel().setMisc((float)vscale,0,0,0,vrotate ? MD_ROTATE : 0);
				break;
			case SKYBOX:
				int sskyend, stile = -1, spal = 0;
    			String[] sfn = new String[6];
    			
    			if ((sskyend = script.getbraces()) == -1) break;
    			while (script.textptr < sskyend)
                {
    				try {
    					switch (gettoken(script,skyboxtokens))
    					{
	                    case TILE:
	                    	if((ivalue = script.getsymbol()) != null)
	                    		stile = ivalue; break;
	                    case PAL:
	                    	if((ivalue = script.getsymbol()) != null)
	                    		spal = ivalue; break;
	                    case FRONT:
	                    	sfn[0] = getFile(script); break;
	                    case RIGHT:
	                    	sfn[1] = getFile(script); break;
	                    case BACK:
	                    	sfn[2] = getFile(script); break;
	                    case LEFT:
	                    	sfn[3] = getFile(script); break;
	                    case TOP:
	                    	sfn[4] = getFile(script); break;
	                    case BOTTOM:
	                    	sfn[5] = getFile(script); break;
	                    default: break;
	                    }
    				} catch(Exception e) { }
                }
    			
    			if (stile < 0) {
    				Console.Println("Error: skybox: missing 'tile number' near line " + script.filename + ":" + script.getlinum(script.ltextptr), OSDTEXT_RED);
    				break;
    			}

    			boolean error = false;
                for (int i=0; i<6; i++)
                {
                    if (sfn[i] == null) {
                    	Console.Println("Error: skybox: missing " + skyfaces[i] + " filename' near line " + script.filename + ":" + script.getlinum(script.ltextptr), OSDTEXT_RED);
                    	error = true;
                    }

                    if(!BuildGdx.cache.contains(sfn[i], 0))
            		{
            			Console.Println("Error: file \"" + sfn[i] + "\" does not exist", OSDTEXT_RED);
            			error = true;
            		}
                }

                if(!error)
                	texInfo.addSkybox(stile,spal,sfn);
    			
    			break;
			case DEFINETINT:
				Integer pal,r,g,b,f;

	            if ((pal = script.getsymbol()) == null) break;
	            if ((r = script.getsymbol()) == null) break;
	            if ((g = script.getsymbol()) == null) break;
	            if ((b = script.getsymbol()) == null) break;
	            if ((f = script.getsymbol()) == null) break; //effects
	            
	            texInfo.setPaletteTint(pal.intValue(),r.intValue(),g.intValue(),b.intValue(),f.intValue());
	          
    	        break;
			case MUSIC:
			case SOUND:
				int dummy;
                String t_id = null, t_file = null;
 
    	        if ((dummy = script.getbraces()) == -1) break;
                while (script.textptr < dummy)
                {
                	switch (gettoken(script,sound_musictokens))
                    {
                	default: break;
                    case ID:
                    	String t = script.getstring();
                    	if(t != null)
                    		t_id = t.trim(); break;
                    case FILE:
                    	t_file = getFile(script);
                    	break;
                    }
                }

                audInfo.addDigitalInfo(t_id, t_file);
				break;

			case ERROR:
				break;
			case EOF:
				return;
			}
        }
    }
	
	private void include(String fn, Scriptfile script, int cmdtokptr)
	{
		byte[] data = BuildGdx.cache.getBytes(fn, 0);
		if(data == null)
		{
			if (cmdtokptr == 0)
	        	Console.Println("Warning: Failed including " + fn + " as module", OSDTEXT_YELLOW);
	        else
	        	Console.Println("Warning: Failed including " + fn + " on line " + script.filename + ":" + script.getlinum(cmdtokptr), OSDTEXT_YELLOW);
			return;
		}

		Scriptfile included = new Scriptfile(fn, data);
		included.path = script.path;
	    defsparser(included);
	}
	
	private boolean check_tile_range(String defcmd, int tilebeg, int tileend, Scriptfile script, int cmdtokptr)
	{
		if (tileend < tilebeg)
		{
			Console.Println("Warning: " + defcmd + ": backwards tile range on line " + script.filename + ":"+ script.getlinum(cmdtokptr), OSDTEXT_YELLOW);
			int tmp = tilebeg;
			tilebeg = tileend;
			tileend = tmp;
		}
		
		if (tilebeg >= MAXTILES || tileend >= MAXTILES)
		{
			Console.Println("Error: " + defcmd + ": Invalid tile range on line " + script.filename + ":"+ script.getlinum(cmdtokptr), OSDTEXT_RED);
			return true;
		}
		
		return false;
	}
	
	private boolean check_tile(String defcmd, int tile, Scriptfile script, int cmdtokptr)
	{
		if (tile >= MAXTILES)
		{
			Console.Println("Error: " + defcmd + ": Invalid tile number on line " + script.filename + ":"+ script.getlinum(cmdtokptr), OSDTEXT_RED);
			return true;
		}
		
		return false;
	}
	
	public void apply()
	{
		for(int i = 0; i < MAXTILES; i++)
		{
			if(tiles[i] == null) continue;

			texInfo.remove(i, 0);

			DefTile tile = tiles[i];
	        if(tile.crc32 != 0)
	        {
	        	byte[] data = waloff[i];
	        	if(data == null)
	        		data = engine.loadtile(i);

	        	long crc32 = data != null ? CRC32.getChecksum(data) : -1;
	        	if(crc32 != tile.crc32)
				{
					boolean found = false;
					while(tile.next != null)
					{
						tile = tile.next;
						if(tile.crc32 == 0 || crc32 == tile.crc32) {
							found = true;
							break;
						}	
					}
					
					if(!found) continue;
				}
	        }

			waloff[i] = new byte[tile.waloff.length];
			System.arraycopy(tile.waloff, 0, waloff[i], 0, tile.waloff.length);

			tilesizx[i] = tile.sizx;
			tilesizy[i] = tile.sizy;
			picanm[i] = tile.picanm;
			engine.setpicsiz(i);
			
			//replace hrp info
			texInfo.addTexture(i, 0, tile.hrp, (float)(0xFF - (tile.alphacut & 0xFF)) * (1.0f / 255.0f), 1.0f, 1.0f, 1.0f, 1.0f, 0);
		}
	}

	@Override
	public void dispose()
	{
		if(!disposable) return;
		
		for(int i = 0; i < MAXTILES; i++)
		{
			if(tiles[i] == null) continue;
			
			texInfo.remove(i, 0);
			
			tilesizx[i] = tiles[i].oldx;
			tilesizy[i] = tiles[i].oldy;
			picanm[i] = tiles[i].oldanm;
			engine.setpicsiz(i);
			
			waloff[i] = null;
			tiles[i] = null;
		}

		mdInfo.dispose();
	}
}
