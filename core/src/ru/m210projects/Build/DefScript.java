package ru.m210projects.Build;

import static ru.m210projects.Build.Engine.DETAILPAL;
import static ru.m210projects.Build.Engine.GLOWPAL;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXVOXELS;
import static ru.m210projects.Build.Engine.NORMALPAL;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.SPECULARPAL;
import static ru.m210projects.Build.FileHandle.Cache1D.kExist;
import static ru.m210projects.Build.FileHandle.Compat.toLowerCase;
import static ru.m210projects.Build.Loader.MDSprite.md_defineanimation;
import static ru.m210projects.Build.Loader.MDSprite.md_defineframe;
import static ru.m210projects.Build.Loader.MDSprite.md_definehud;
import static ru.m210projects.Build.Loader.MDSprite.md_defineskin;
import static ru.m210projects.Build.Loader.MDSprite.md_loadmodel;
import static ru.m210projects.Build.Loader.MDSprite.md_setmisc;
import static ru.m210projects.Build.Loader.MDSprite.md_undefinemodel;
import static ru.m210projects.Build.Loader.MDSprite.qloadkvx;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_YELLOW;
import static ru.m210projects.Build.Render.Types.Hightile.hicsetpalettetint;
import static ru.m210projects.Build.Render.Types.Hightile.hicsetskybox;
import static ru.m210projects.Build.Render.Types.Hightile.hicsetsubsttex;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ru.m210projects.Build.Loader.Voxels.VOXModel;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.Types.Hicreplctyp;
import ru.m210projects.Build.Types.Palette;
import ru.m210projects.Build.Types.Tile2model;

public class DefScript {

	private Hicreplctyp[] hicreplc = new Hicreplctyp[MAXTILES];
	private Palette[] hictinting = new Palette[MAXPALOOKUPS];
	
	private int[] tiletovox;
	private int nextvoxid, lastvoxid = -1;
	private VOXModel[] voxmodels = new VOXModel[MAXVOXELS];
	private boolean[] voxrotate;
	
	private Tile2model[] tile2model;
	private int lastmodelid = -1, modelskin = -1, lastmodelskin = -1, seenframe = 0;
	
	private HashMap<String, String> midToMusic = new HashMap<String, String>();
	
	private final String skyfaces[] = 
	{ 
		"front face", 
		"right face", 
		"back face", 
		"left face", 
		"top face",
		"bottom face" 
	};

	private enum Token {
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
			put("pal",   Token.PAL  );
		}
	};
	
	private final static Map<String , Token> texturetokens_pal = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("file",     Token.FILE );put("name", Token.FILE );
			put("alphacut", Token.ALPHACUT );
		}
	};
	
	public DefScript(Scriptfile script) throws NullPointerException {
		if (script == null)
			throw new NullPointerException("script == null");

		Console.Println("Loading defscript " + script.filename + "...");

		defsparser(script);
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
	
	private void defsparser(Scriptfile script)
    {
		String fn;
		Token token;
		while (true)
        {
			switch(gettoken(script, basetokens))
			{
			case INCLUDE:
    			if ((fn = script.getstring()) == null) break;
                include(fn, script, script.ltextptr);
				break;
			case MODEL:
				int modelend;
    			String modelfn;
    			double mdscale=1.0, mzadd=0.0, myoffset=0.0;
    	        int shadeoffs=0, mdpal=0, mdflags=0;
    	        int model_ok = 1;

    			modelskin = lastmodelskin = 0;
    	        seenframe = 0;
    	        
    	        if ((modelfn = script.getstring()) == null) break;
                if ((modelend = script.getbraces()) == -1) break;
    			
                lastmodelid = md_loadmodel(modelfn);
                if (lastmodelid < 0)
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
	                    	mdscale = script.getdouble(); break;
	                    case SHADE:
	                    	shadeoffs = script.getsymbol(); break;
	                    case ZADD:
	                    	mzadd = script.getdouble(); break;
	                    case YOFFSET:
	                    	myoffset = script.getdouble(); break;
	                    case FLAGS:
	                    	mdflags = script.getsymbol(); break;
	                    case FRAME:
	                    {
	                    	int frametokptr = script.ltextptr;
	                        int frameend, happy=1;
	                        String framename = null;
	                        int ftilenume = -1, ltilenume = -1, tilex = 0, framei;
	                        double smoothduration = 0.1;

	    	    			if ((frameend = script.getbraces()) == -1) break;
	    	    			
	    	    			while (script.textptr < frameend)
    	                    {
	    	    				switch (gettoken(script,modelframetokens))
    	                        {
    	                        default: break;
    	                        case PAL:
    	                            mdpal = script.getsymbol(); break;
    	                        case FRAME:
    	                        	framename = script.getstring(); break;
    	                        case TILE:
    	                        	ftilenume = script.getsymbol(); ltilenume = ftilenume; break;
    	                        case TILE0:
    	                        	ftilenume = script.getsymbol(); break; //first tile number
    	                        case TILE1:
    	                        	ltilenume = script.getsymbol(); break; //last tile number (inclusive)
    	                        case SMOOTHDURATION:
    	                        	smoothduration = script.getdouble(); break;
    	                        }
    	                    }
	    	    			
	    	    			if (check_tile_range("model: frame", ftilenume, ltilenume, script, frametokptr))
	                        {
	                            model_ok = 0;
	                            break;
	                        }

	                        if (lastmodelid < 0)
	                        {
	                        	Console.Println("Warning: Ignoring frame definition.", OSDTEXT_YELLOW);
	                            break;
	                        }

	                        for (tilex = ftilenume; tilex <= ltilenume && happy != 0; tilex++)
	                        {
	                        						
	                            framei = md_defineframe(lastmodelid, framename, tilex, (int)Math.max(0, modelskin), (float) smoothduration,mdpal);
	                            switch (framei)
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
	                            	dfps = script.getdouble(); break; //animation frame rate
	                            case FLAGS:
	                            	flags = script.getsymbol(); break;
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
	                        if (happy == 0) break;

	                        if (lastmodelid < 0)
	                        {
	                        	Console.Println("Warning: Ignoring animation definition.", OSDTEXT_YELLOW);
	                            break;
	                        }
	                        switch (md_defineanimation(lastmodelid, startframe, endframe, (int)(dfps*(65536.0*.001)), flags))
	                        {
		                        case 0:
		                            break;
		                        case -1:
		                            break; // invalid model id!?
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
	                            	param = script.getdouble(); break;
	                            case SPECPOWER:
	                            	specpower = script.getdouble(); break;
	                            case SPECFACTOR:
	                            	specfactor = script.getdouble(); break;
	                            case FILE:
	                            	skinfn = script.getstring(); break; //skin filename
	                            case SURF:
	                            	surfnum = script.getsymbol(); break; //getnumber
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
	                        
	                        if(script.path != null)
	                        	skinfn = script.path + File.separator + skinfn;
	                        
	                        if (!kExist(skinfn, 0))
	                            break;
	                        
	                        switch (md_defineskin(lastmodelid, skinfn, palnum, Math.max(0,modelskin), surfnum, param, specpower, specfactor))
	                        {
		                        case 0:
		                            break;
		                        case -1:
		                            break; // invalid model id!?
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
	                            	ftilenume = script.getsymbol(); ltilenume = ftilenume; break;
	                            case TILE0:
	                            	ftilenume = script.getsymbol(); break; //first tile number
	                            case TILE1:
	                            	ltilenume = script.getsymbol(); break; //last tile number (inclusive)
	                            case XADD:
	                            	xadd = script.getdouble(); break;
	                            case YADD:
	                            	yadd = script.getdouble(); break;
	                            case ZADD:
	                            	zadd = script.getdouble(); break;
	                            case ANGADD:
	                            	angadd = script.getdouble(); break;
	                            case FOV:
	                            	fov = script.getsymbol(); break;
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
	                        
	                        if (lastmodelid < 0)
	                        {
	                        	Console.Println("Warning: Ignoring frame definition.", OSDTEXT_YELLOW);
	                            break;
	                        }
	                        
	                        for (tilex = ftilenume; tilex <= ltilenume && happy != 0; tilex++)
	                        {
	                            switch (md_definehud(lastmodelid, tilex, xadd, yadd, zadd, angadd, flags, fov))
	                            {
	                            case 0:
	                                break;
	                            case -1:
	                                happy = 0; break; // invalid model id!?
	                            case -2:
	                            	Console.Println("Invalid tile number on line " + script.filename + ":" + script.getlinum(hudtokptr), OSDTEXT_RED);
	                                happy = 0;
	                                break;
	                            case -3:
	                                Console.Println("Invalid frame name on line " + script.filename + ":" + script.getlinum(hudtokptr), OSDTEXT_RED);        
	                                happy = 0;
	                                break;
	                            }

	                            model_ok &= happy;
	                        }
	                    }
	                    break;
                    }
                }
                
                if (model_ok == 0)
                {
                    if (lastmodelid >= 0)
                    {
                    	Console.Println("Removing model " + lastmodelid + " due to errors.", OSDTEXT_YELLOW);
                        md_undefinemodel(lastmodelid);
                    }
                    break;
                }

                md_setmisc(lastmodelid,(float)mdscale,shadeoffs,(float)mzadd,(float)myoffset,mdflags);

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
	                            	tfn = script.getstring(); break;
	                            case ALPHACUT:
	                            	if(token != Token.PAL)
	                            		break;
	                            	alphacut = script.getdouble();
	                            	break;
	                            case XSCALE:
	                            	xscale = script.getdouble(); 
	                            	break;
	                            case YSCALE:
	                            	yscale = script.getdouble(); 
	                            	break;
	                            case SPECPOWER:
	                            	specpower = script.getdouble(); 
	                            	break;
	                            case SPECFACTOR:
	                            	specfactor = script.getdouble(); 
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
                        
                        if(script.path != null)
                        	tfn = script.path + File.separator + tfn;

                        if (!kExist(tfn, 0))
                            break;
//                      Console.Println("Loading hires texture \"" + tfn + "\"", false);
                        hicsetsubsttex(ttile.intValue(),tpal.intValue(),tfn,(float)alphacut,(float)xscale,(float)yscale, (float)specpower, (float)specfactor,flags);
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
    	        int tile0 = MAXTILES, tile1 = -1, tilex = -1;
    	        boolean vrotate = false;

    	        if ((fn = script.getstring()) == null) break; //voxel filename
    	        if(script.path != null)
    	        	fn = script.path + File.separator + fn;
    	        
                if (nextvoxid == MAXVOXELS) { Console.Println("Maximum number of voxels already defined.", OSDTEXT_YELLOW); break; }
                if (qloadkvx(nextvoxid, fn) == -1) { Console.Println("Failure loading voxel file " + fn, OSDTEXT_RED); break; }
                lastvoxid = nextvoxid++;
                
                if ((vmodelend = script.getbraces()) == -1) break;

                while (script.textptr < vmodelend)
                {
                    switch (gettoken(script, voxeltokens))
                    {
                    	case TILE:
                    		tilex = script.getsymbol();
                            if (check_tile("voxel", tilex, script, script.ltextptr))
                                break;

                            tiletovox[tilex] = lastvoxid;
                    		break;
                    	case TILE0:
                    		tile0 = script.getsymbol();
                            break; //1st tile #

                        case TILE1:
                        	tile1 = script.getsymbol();
                        	if (check_tile_range("voxel", tile0, tile1, script, script.ltextptr))
                        		break;
                            for (tilex=tile0; tilex<=tile1; tilex++) 
                                 tiletovox[tilex] = lastvoxid;
                            break; //last tile number (inclusive)
                        case SCALE:
                            double scale = script.getdouble();
                            if (voxmodels[lastvoxid] != null)
                                voxmodels[lastvoxid].scale = (float) scale;
                            break;
                        case ROTATE:
                        	vrotate = true;
                        	break;
                        default:
                        	break;
                    }
                }
//              Console.Println("Voxel model loaded \"" + fn + "\"", false);
                if(tilex != -1 && vrotate) 
                	voxrotate[tilex] = true;
                lastvoxid = -1;
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
	                    	stile = script.getsymbol(); break;
	                    case PAL:
	                    	spal = script.getsymbol(); break;
	                    case FRONT:
	                    	sfn[0] = script.getstring(); break;
	                    case RIGHT:
	                    	sfn[1] = script.getstring(); break;
	                    case BACK:
	                    	sfn[2] = script.getstring(); break;
	                    case LEFT:
	                    	sfn[3] = script.getstring(); break;
	                    case TOP:
	                    	sfn[4] = script.getstring(); break;
	                    case BOTTOM:
	                    	sfn[5] = script.getstring(); break;
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
                    
                    if(script.path != null)
                    	sfn[i] = script.path + File.separator + sfn[i];
                    
                    if(!kExist(sfn[i], 0))
            		{
            			Console.Println("Error: file \"" + sfn[i] + "\" does not exist", OSDTEXT_RED);
            			error = true;
            		}
                }

                if(!error)
                	hicsetskybox(stile,spal,sfn);
    			
    			break;
			case DEFINETINT:
				Integer pal,r,g,b,f;

	            if ((pal = script.getsymbol()) == null) break;
	            if ((r = script.getsymbol()) == null) break;
	            if ((g = script.getsymbol()) == null) break;
	            if ((b = script.getsymbol()) == null) break;
	            if ((f = script.getsymbol()) == null) break; //effects
	            hicsetpalettetint(pal.intValue(),r.intValue(),g.intValue(),b.intValue(),f.intValue());
    	        
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
                    	t_id = script.getstring(); break;
                    case FILE:
                    	t_file = script.getstring(); break;
                    }
                }
                
                if(script.path != null)
                	t_file = script.path + File.separator + t_file;
                
                midToMusic.put(toLowerCase(t_id), toLowerCase(t_file));
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
		Scriptfile included = Scriptfile.scriptfile_fromfile(fn);
	    if (included == null)
	    {
	        if (cmdtokptr == 0)
	        	Console.Println("Warning: Failed including " + fn + " as module", OSDTEXT_YELLOW);
	        else
	        	Console.Println("Warning: Failed including " + fn + " on line " + script.filename + ":" + script.getlinum(cmdtokptr), OSDTEXT_YELLOW);
	    }
	    else
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

	public String checkDigitalMusic(String midi)
	{
		if(midi != null)
			return midToMusic.get(toLowerCase(midi));
		
		return null;
	}
}
