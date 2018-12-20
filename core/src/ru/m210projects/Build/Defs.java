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

package ru.m210projects.Build;

import static ru.m210projects.Build.Audio.BMusic.Highmusic.*;
import static ru.m210projects.Build.Common.T_EOF;
import static ru.m210projects.Build.Common.T_ERROR;
import static ru.m210projects.Build.Common.check_file_exist;
import static ru.m210projects.Build.Common.getatoken;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Loader.MDSprite.*;
import static ru.m210projects.Build.Render.Types.Hightile.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.MemLog;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

public class Defs {
	
	public static final int MAXUSERTRACKS = 64;
	public static String usertrack[] = new String[MAXUSERTRACKS];

	private static final int T_INCLUDE = 0;
	private static final int T_DEFINE = 1;
	private static final int T_DEFINETEXTURE = 2;
	private static final int T_DEFINESKYBOX = 3;
	private static final int T_DEFINETINT = 4;
	private static final int T_DEFINEMODEL = 5;
	private static final int T_DEFINEMODELANIM = 6;
	private static final int T_DEFINEMODELSKIN = 7;
	private static final int T_SELECTMODELSKIN = 8;
	private static final int T_DEFINEVOXEL = 9;
	private static final int T_DEFINEVOXELTILES = 10;
	private static final int T_MODEL = 11;
	private static final int T_FILE = 12;
	private static final int T_SCALE = 13;
	private static final int T_SHADE = 14;
	private static final int T_FRAME = 15;
	private static final int T_SMOOTHDURATION = 16;
	private static final int T_ANIM = 17;
	private static final int T_SKIN = 18;
	private static final int T_SURF = 19;
	private static final int T_TILE = 20;
	private static final int T_TILE0 = 21;
	private static final int T_TILE1 = 22;
	private static final int T_FRAME0 = 23;
	private static final int T_FRAME1 = 24;
	private static final int T_FPS = 25;
	private static final int T_FLAGS = 26;
	private static final int T_PAL = 27;
	private static final int T_BASEPAL = 28;
	private static final int T_DETAIL = 29;
	private static final int T_GLOW = 30;
	private static final int T_SPECULAR = 31;
	private static final int T_NORMAL = 32;
	private static final int T_PARAM = 33;
	private static final int T_HUD = 34;
	private static final int T_XADD = 35;
	private static final int T_YADD = 36;
	private static final int T_ZADD = 37;
	private static final int T_ANGADD = 38;
	private static final int T_FOV = 39;
	private static final int T_FLIPPED = 40;
	private static final int T_HIDE = 41;
	private static final int T_NOBOB = 42;
	private static final int T_NODEPTH = 43;
	private static final int T_VOXEL = 44;
	private static final int T_SKYBOX = 45;
	private static final int T_FRONT = 46;
	private static final int T_RIGHT = 47;
	private static final int T_BACK = 48;
	private static final int T_LEFT = 49;
	private static final int T_TOP = 50;
	private static final int T_BOTTOM = 51;
	private static final int T_HIGHPALOOKUP = 52;
	private static final int T_TINT = 53;
	private static final int T_MAKEPALOOKUP = 54 ;
	private static final int T_REMAPPAL = 55;
	private static final int T_REMAPSELF = 56;
	private static final int T_RED = 57;
	private static final int T_GREEN = 58;
	private static final int T_BLUE = 59;
	private static final int T_TEXTURE = 60;
	private static final int T_ALPHACUT = 61;
	private static final int T_XSCALE = 62;
	private static final int T_YSCALE = 63;
	private static final int T_SPECPOWER = 64;
	private static final int T_SPECFACTOR = 65;
	private static final int T_NOCOMPRESS = 66;
	private static final int T_NODOWNSIZE = 67;
	private static final int T_UNDEFMODEL = 68;
	private static final int T_UNDEFMODELRANGE = 69;
	private static final int T_UNDEFMODELOF = 70;
	private static final int T_UNDEFTEXTURE = 71;
	private static final int T_UNDEFTEXTURERANGE = 72;
	private static final int T_ALPHAHACK = 73;
	private static final int T_ALPHAHACKRANGE = 74;
	private static final int T_SPRITECOL = 75;
	private static final int T_2DCOL = 76;
	private static final int T_FOGPAL = 77;
	private static final int T_LOADGRP = 78;
	private static final int T_DUMMYTILE = 79;
	private static final int T_DUMMYTILERANGE = 80;
	private static final int T_SETUPTILE = 81;
	private static final int T_SETUPTILERANGE = 82;
	private static final int T_ANIMTILERANGE = 83;
	private static final int T_CACHESIZE = 84;
	private static final int T_IMPORTTILE = 85;
	private static final int T_MUSIC = 86;
	private static final int T_ID = 87;
	private static final int T_SOUND = 88;
	private static final int T_TILEFROMTEXTURE = 89;
	private static final int T_XOFFSET = 90;
	private static final int T_YOFFSET = 91;
	private static final int T_TEXHITSCAN = 92;
	private static final int T_INCLUDEDEFAULT = 93;
	private static final int T_ANIMSOUNDS = 94;
	private static final int T_NOFLOORPALRANGE = 95;
	private static final int T_TEXHITSCANRANGE = 96;
	private static final int T_ECHO = 97;
	private static final int T_ROTATE = 98;
	private static final int T_CDTRACK = 99;
	
	private static int lastmodelid = -1, lastvoxid = -1, modelskin = -1, lastmodelskin = -1, seenframe = 0;
	private static int nextvoxid = 512;
	
	private static final String skyfaces[] =
	{
	    "front face", "right face", "back face",
	    "left face", "top face", "bottom face"
	};
	
	public static final Map<String , Integer> basetokens = new HashMap<String , Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("include",    T_INCLUDE);
			put("model",   T_MODEL);
			put("texture",    T_TEXTURE);
			put("voxel",    T_VOXEL);
			put("skybox",   T_SKYBOX);
			put("definetint",   T_DEFINETINT);
			put("music",   T_MUSIC);
			put("sound",   T_SOUND);
		}
	};
	
	public static void defsparser_include(String fn, Scriptfile script, int cmdtokptr)
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
	
	public static boolean check_tile_range(String defcmd, int tilebeg, int tileend, Scriptfile script, int cmdtokptr)
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
	
	public static boolean check_tile(String defcmd, int tile, Scriptfile script, int cmdtokptr)
	{
		if (tile >= MAXTILES)
		{
			Console.Println("Error: " + defcmd + ": Invalid tile number on line " + script.filename + ":"+ script.getlinum(cmdtokptr), OSDTEXT_RED);
			return true;
		}
		
		return false;
	}
	
	public static void defsparser(Scriptfile script)
    {
		Console.Println("Loading " + script.filename + "...", 0);
    	while (true)
        {
    		int tokn = getatoken(script,basetokens);
    		switch (tokn)
            {
	    		case T_INCLUDE:
	    			String fn;
	    			if ((fn = script.getstring()) == null) break;
	                defsparser_include(fn, script, script.ltextptr);
	    			break;
	    		case T_INCLUDEDEFAULT:
	            {
//	                defsparser_include(G_DefaultDefFile(), script, script.ltextptr);
	                break;
	            }
	    		case T_ERROR:
	    			//Console.Println("Error on line " + script.filename + ":" + script.getlinum(script.ltextptr), true);
	                break;
	    		case T_EOF:
	                return;
	    		case T_MODEL:
	    			int modelend;
	    			String modelfn;
	    			double mdscale=1.0, mzadd=0.0, myoffset=0.0;
	    	        int shadeoffs=0, mdpal=0, mdflags=0;
//	    	        byte[] usedframebitmap = new byte[1024>>3];
	    	        int model_ok = 1;
	    		
	    			final Map<String , Integer> modeltokens = new HashMap<String , Integer>() {
	    				private static final long serialVersionUID = 1L;
	    				{
	    					put( "scale",    T_SCALE    );
	    	                put( "shade",    T_SHADE    );
	    	                put( "zadd",     T_ZADD     );
	    	                put( "yoffset",  T_YOFFSET  );
	    	                put( "frame",    T_FRAME    );
	    	                put( "anim",     T_ANIM     );
	    	                put( "skin",     T_SKIN     );
	    	                put( "detail",   T_DETAIL   );
	    	                put( "glow",     T_GLOW     );
	    	                put( "specular", T_SPECULAR );
	    	                put( "normal",   T_NORMAL   );
	    	                put( "hud",      T_HUD      );
	    	                put( "flags",    T_FLAGS    );
	    				}
	    			};
	    			
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
	                	int mdtoken = getatoken(script,modeltokens);
	                	switch (mdtoken)
	                    {
		                    case T_SCALE:
		                    	mdscale = script.getdouble(); break;
		                    case T_SHADE:
		                    	shadeoffs = script.getsymbol(); break;
		                    case T_ZADD:
		                    	mzadd = script.getdouble(); break;
		                    case T_YOFFSET:
		                    	myoffset = script.getdouble(); break;
		                    case T_FLAGS:
		                    	mdflags = script.getsymbol(); break;
		                    case T_FRAME:
		                    {
		                    	int frametokptr = script.ltextptr;
		                        int frameend, happy=1;
		                        String framename = null;
		                        int ftilenume = -1, ltilenume = -1, tilex = 0, framei;
		                        double smoothduration = 0.1;
		                        
		                        final Map<String , Integer> modelframetokens = new HashMap<String , Integer>() {
		    	    				private static final long serialVersionUID = 1L;
		    	    				{
		    	    					put( "pal",              T_PAL               );
		    	                        put( "frame",            T_FRAME             );
		    	                        put( "name",             T_FRAME             );
		    	                        put( "tile",             T_TILE              );
		    	                        put( "tile0",            T_TILE0             );
		    	                        put( "tile1",            T_TILE1             );
		    	                        put( "smoothduration",   T_SMOOTHDURATION    );
		    	    				}
		    	    			};
		    	    			
		    	    			if ((frameend = script.getbraces()) == -1) break;
		    	    			
		    	    			while (script.textptr < frameend)
	    	                    {
		    	    				switch (getatoken(script,modelframetokens))
	    	                        {
	    	                        case T_PAL:
	    	                            mdpal = script.getsymbol(); break;
	    	                        case T_FRAME:
	    	                        	framename = script.getstring(); break;
	    	                        case T_TILE:
	    	                        	ftilenume = script.getsymbol(); ltilenume = ftilenume; break;
	    	                        case T_TILE0:
	    	                        	ftilenume = script.getsymbol(); break; //first tile number
	    	                        case T_TILE1:
	    	                        	ltilenume = script.getsymbol(); break; //last tile number (inclusive)
	    	                        case T_SMOOTHDURATION:
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
//		                                if (framei >= 0 && framei < 1024)
//		                                    usedframebitmap[framei>>3] |= (1<<(framei&7));
		                            }

		                            model_ok &= happy;
		                        }

		                        seenframe = 1;
		                    }
		                    break;
		                    case T_ANIM:
		                    {
		                    	int animtokptr = script.ltextptr;
		                    	int animend, happy=1;
		                    	String startframe = null, endframe = null;
		                    	int flags = 0;
		                        double dfps = 1.0;
		                        
		                        final Map<String , Integer> modelanimtokens = new HashMap<String , Integer>() {
		    	    				private static final long serialVersionUID = 1L;
		    	    				{
		    	    					put( "frame0", T_FRAME0 );
			                            put( "frame1", T_FRAME1 );
			                            put( "fps",    T_FPS    );
			                            put( "flags",  T_FLAGS  );
		    	    				}
		    	    			};
		    	    			
		    	    			if ((animend = script.getbraces()) == -1) break;
		                        while (script.textptr < animend)
		                        {
		                            switch (getatoken(script,modelanimtokens))
		                            {
		                            case T_FRAME0:
		                            	startframe = script.getstring(); break;
		                            case T_FRAME1:
		                            	endframe = script.getstring(); break;
		                            case T_FPS:
		                            	dfps = script.getdouble(); break; //animation frame rate
		                            case T_FLAGS:
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
		                    case T_SKIN: case T_DETAIL: case T_GLOW: case T_SPECULAR: case T_NORMAL:
		                    {
		                    	int skintokptr = script.ltextptr;
		                        int skinend;
		                        String skinfn = null;
		                        int palnum = 0, surfnum = 0;
		                        double param = 1.0, specpower = 1.0, specfactor = 1.0;
		                        
		                        final Map<String , Integer> modelskintokens = new HashMap<String , Integer>() {
		    	    				private static final long serialVersionUID = 1L;
		    	    				{
		    	    					put( "pal",           T_PAL        );
		                                put( "file",          T_FILE       );
		                                put( "surf",          T_SURF       );
		                                put( "surface",       T_SURF       );
		                                put( "intensity",     T_PARAM      );
		                                put( "scale",         T_PARAM      );
		                                put( "detailscale",   T_PARAM      );
		                                put( "specpower",     T_SPECPOWER  ); put( "specularpower",  T_SPECPOWER  ); put( "parallaxscale", T_SPECPOWER );
		                                put( "specfactor",    T_SPECFACTOR ); put( "specularfactor", T_SPECFACTOR ); put( "parallaxbias", T_SPECFACTOR );
		    	    				}
		    	    			};
		    	    			if ((skinend = script.getbraces()) == -1) break;
		                        while (script.textptr < skinend)
		                        {
		                            switch (getatoken(script,modelskintokens))
		                            {
		                            case T_PAL:
		                            	palnum = script.getsymbol(); break;
		                            case T_PARAM:
		                            	param = script.getdouble(); break;
		                            case T_SPECPOWER:
		                            	specpower = script.getdouble(); break;
		                            case T_SPECFACTOR:
		                            	specfactor = script.getdouble(); break;
		                            case T_FILE:
		                            	skinfn = script.getstring(); break; //skin filename
		                            case T_SURF:
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
		                        
		                        switch (mdtoken)
		                        {
			                        case T_DETAIL:
			                        	palnum = DETAILPAL;
			                        	param = 1.0f / param;
			                            break;
			                        case T_GLOW:
			                        	palnum = GLOWPAL;
			                            break;
			                        case T_SPECULAR:
			                        	palnum = SPECULARPAL;
			                            break;
			                        case T_NORMAL:
			                        	palnum = NORMALPAL;
			                            break;
		                        }
		                        
		                        if(script.path != null)
		                        	skinfn = script.path + File.separator + skinfn;
		                        
		                        if (check_file_exist(skinfn))
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
		                    case T_HUD:
		                    {
		                    	int hudtokptr = script.ltextptr;
		                        int happy=1, frameend;
		                        int ftilenume = -1, ltilenume = -1, tilex = 0, flags = 0, fov = -1;
		                        double xadd = 0.0, yadd = 0.0, zadd = 0.0, angadd = 0.0;
		                        
		                        final Map<String , Integer> modelhudtokens = new HashMap<String , Integer>() {
		    	    				private static final long serialVersionUID = 1L;
		    	    				{
		    	    					put( "tile",   T_TILE   );
		                                put( "tile0",  T_TILE0  );
		                                put( "tile1",  T_TILE1  );
		                                put( "xadd",   T_XADD   );
		                                put( "yadd",   T_YADD   );
		                                put( "zadd",   T_ZADD   );
		                                put( "angadd", T_ANGADD );
		                                put( "fov",    T_FOV    );
		                                put( "hide",   T_HIDE   );
		                                put( "nobob",  T_NOBOB  );
		                                put( "flipped",T_FLIPPED);
		                                put( "nodepth",T_NODEPTH);
		    	    				}
		    	    			};

		                        if ((frameend = script.getbraces()) == -1) break;
		                        while (script.textptr < frameend)
		                        {
		                        	switch (getatoken(script,modelhudtokens))
		                            {
		                            case T_TILE:
		                            	ftilenume = script.getsymbol(); ltilenume = ftilenume; break;
		                            case T_TILE0:
		                            	ftilenume = script.getsymbol(); break; //first tile number
		                            case T_TILE1:
		                            	ltilenume = script.getsymbol(); break; //last tile number (inclusive)
		                            case T_XADD:
		                            	xadd = script.getdouble(); break;
		                            case T_YADD:
		                            	yadd = script.getdouble(); break;
		                            case T_ZADD:
		                            	zadd = script.getdouble(); break;
		                            case T_ANGADD:
		                            	angadd = script.getdouble(); break;
		                            case T_FOV:
		                            	fov = script.getsymbol(); break;
		                            case T_HIDE:
		                                flags |= 1; break;
		                            case T_NOBOB:
		                                flags |= 2; break;
		                            case T_FLIPPED:
		                                flags |= 4; break;
		                            case T_NODEPTH:
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
	    		case T_SKYBOX:
	    			int sskyend, stile = -1, spal = 0;
	    			String[] sfn = new String[6];
	    			final Map<String , Integer> skyboxtokens = new HashMap<String , Integer>() {
	    				private static final long serialVersionUID = 1L;
	    				{
	    					put( "tile"   ,T_TILE   );
	    	                put( "pal"    ,T_PAL    );
	    	                put( "ft"     ,T_FRONT  ); put( "front"  ,T_FRONT  ); put( "forward",T_FRONT  );
	    	                put( "rt"     ,T_RIGHT  ); put( "right"  ,T_RIGHT  );
	    	                put( "bk"     ,T_BACK   ); put( "back"   ,T_BACK   );
	    	                put( "lf"     ,T_LEFT   ); put( "left"   ,T_LEFT   ); put( "lt"     ,T_LEFT   );
	    	                put( "up"     ,T_TOP    ); put( "top"    ,T_TOP    ); put( "ceiling",T_TOP    ); put( "ceil"   ,T_TOP    );
	    	                put( "dn"     ,T_BOTTOM ); put( "bottom" ,T_BOTTOM ); put( "floor"  ,T_BOTTOM ); put( "down"   ,T_BOTTOM );
	    				}
	    			};
	    			if ((sskyend = script.getbraces()) == -1) break;
	    			while (script.textptr < sskyend)
	                {
	    				try {
	    				switch (getatoken(script,skyboxtokens))
	                    {
		                    case T_TILE:
		                    	stile = script.getsymbol(); break;
		                    case T_PAL:
		                    	spal = script.getsymbol(); break;
		                    case T_FRONT:
		                    	sfn[0] = script.getstring(); break;
		                    case T_RIGHT:
		                    	sfn[1] = script.getstring(); break;
		                    case T_BACK:
		                    	sfn[2] = script.getstring(); break;
		                    case T_LEFT:
		                    	sfn[3] = script.getstring(); break;
		                    case T_TOP:
		                    	sfn[4] = script.getstring(); break;
		                    case T_BOTTOM:
		                    	sfn[5] = script.getstring(); break;
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
	                    	
	                    if (check_file_exist(sfn[i]))
	                        error = true;
	                }

	                if(!error)
	                	hicsetskybox(stile,spal,sfn);
	    			
	    			break;
	    		case T_TEXTURE:
	    			int token, textureend;
	    			Integer ttile = -1;
	    			
	    			final Map<String , Integer> texturetokens = new HashMap<String , Integer>() {
	    				private static final long serialVersionUID = 1L;
	    				{
	    					put("pal",    			T_PAL);
	    					put("detail",    		T_DETAIL);
	    					put("glow",    			T_GLOW);
	    					put("specular",    		T_SPECULAR);
	    					put("normal",    		T_NORMAL);

	    					put( "file",            T_FILE );
	    					put( "name", 			T_FILE );
	                        put( "alphacut",        T_ALPHACUT );
	                        put( "detailscale",     T_XSCALE ); 
	                        put( "scale",  			T_XSCALE ); 
	                        put( "xscale", 			T_XSCALE ); 
	                        put( "intensity",  		T_XSCALE );
	                        put( "yscale",          T_YSCALE );
	                        put( "specpower",       T_SPECPOWER ); 
	                        put( "specularpower", 	T_SPECPOWER ); 
	                        put( "parallaxscale", 	T_SPECPOWER );
	                        put( "specfactor",      T_SPECFACTOR ); 
	                        put( "specularfactor", 	T_SPECFACTOR ); 
	                        put( "parallaxbias", 	T_SPECFACTOR );
	                        put( "nocompress",      T_NOCOMPRESS );
	                        put( "nodownsize",      T_NODOWNSIZE );
	    				}
	    			};

	    			
	    			if ((ttile = script.getsymbol()) == null) break;
	                if ((textureend = script.getbraces()) == -1) break;
	                
	                while (script.textptr < textureend)
	                {
	                	token = getatoken(script,texturetokens);
	                    switch (token)
	                    {
	                    case T_PAL:
	                    case T_DETAIL: 
	                    case T_GLOW: 
	                    case T_SPECULAR: 
	                    case T_NORMAL:
	                    	Integer tpal = -1;
	                    	String tfn = null;
	                    	double alphacut = -1.0, xscale = 1.0, yscale = 1.0, specpower = 1.0, specfactor = 1.0;
	                    	int flags = 0;
	                    	int palend;
	                    	
	                    	if (token == T_PAL && (tpal = script.getsymbol()) == null) break;
	                    	if ((palend = script.getbraces()) == -1) break;
	                        while (script.textptr < palend)
	                        {
	                        	switch (getatoken(script, texturetokens))
	                            {
		                            case T_FILE:
		                            	tfn = script.getstring(); break;
		                            case T_ALPHACUT:
		                            	if(token != T_PAL)
		                            		break;
		                            	alphacut = script.getdouble();
		                            	break;
		                            case T_XSCALE:
		                            	xscale = script.getdouble(); 
		                            	break;
		                            case T_YSCALE:
		                            	yscale = script.getdouble(); 
		                            	break;
		                            case T_SPECPOWER:
		                            	specpower = script.getdouble(); 
		                            	break;
		                            case T_SPECFACTOR:
		                            	specfactor = script.getdouble(); 
		                            	break;
		                            case T_NOCOMPRESS:
		                                flags |= 1; break;
		                            case T_NODOWNSIZE:
		                                flags |= 16; break;
	                            }
	                        }
	                        
	                        switch (token)
	                        {
	                        case T_PAL:
	                        	xscale = 1.0f / xscale;
		                        yscale = 1.0f / yscale;
	                        	break;
	                        case T_DETAIL:
	                            tpal = DETAILPAL;
	                            xscale = 1.0f / xscale;
	                            yscale = 1.0f / yscale;
	                            break;
	                        case T_GLOW:
	                            tpal = GLOWPAL;
	                            break;
	                        case T_SPECULAR:
	                            tpal = SPECULARPAL;
	                            break;
	                        case T_NORMAL:
	                            tpal = NORMALPAL;
	                            break;
	                        }
	                        
	                        if (ttile >= MAXTILES) break;	// message is printed later
	                        if (token == T_PAL && tpal >= MAXPALOOKUPS - RESERVEDPALS)
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

	                        if (check_file_exist(tfn))
	                            break;
//	                        Console.Println("Loading hires texture \"" + tfn + "\"", false);
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
	    		 case T_DEFINETINT:
    	            Integer pal,r,g,b,f;

    	            if ((pal = script.getsymbol()) == null) break;
    	            if ((r = script.getsymbol()) == null) break;
    	            if ((g = script.getsymbol()) == null) break;
    	            if ((b = script.getsymbol()) == null) break;
    	            if ((f = script.getsymbol()) == null) break; //effects
    	            hicsetpalettetint(pal.intValue(),r.intValue(),g.intValue(),b.intValue(),f.intValue());
	    	        
	    	        break;
	    		case T_VOXEL:
	    	        String vfn;
	    	        int vmodelend;
	    	        int tile0 = MAXTILES, tile1 = -1, tilex = -1, rotate = -1;

	    	        Map<String , Integer> voxeltokens = new HashMap<String , Integer>() {
	    				private static final long serialVersionUID = 1L;
	    				{
	    					put("tile",     T_TILE);
	    					put("tile0",    T_TILE0);
	    					put("tile1",    T_TILE1);
	    					put("scale",    T_SCALE);
	    					put("rotate",   T_ROTATE);
	    				}
	    	        };

	    	        if ((vfn = script.getstring()) == null) break; //voxel filename
	    	        if(script.path != null)
	    	        	vfn = script.path + File.separator + vfn;
	    	        
	                if (nextvoxid == MAXVOXELS) { Console.Println("Maximum number of voxels already defined.", OSDTEXT_YELLOW); break; }
	                if (qloadkvx(nextvoxid, vfn) == -1) { Console.Println("Failure loading voxel file " + vfn, OSDTEXT_RED); break; }
	                lastvoxid = nextvoxid++;
	                
	                if ((vmodelend = script.getbraces()) == -1) break;

	                while (script.textptr < vmodelend)
	                {
	                    switch (getatoken(script,voxeltokens))
	                    {
	                    	case T_TILE:
	                    		tilex = script.getsymbol();

	                            if (check_tile("voxel", tilex, script, script.ltextptr))
	                                break;

	                            tiletovox[tilex] = lastvoxid;
	                    		break;
	                    	case T_TILE0:
	                    		tile0 = script.getsymbol();
	                            break; //1st tile #

	                        case T_TILE1:
	                        	tile1 = script.getsymbol();

	                        	if (check_tile_range("hud", tile0, tile1, script, script.ltextptr))
	                        		break;

	                            for (tilex=tile0; tilex<=tile1; tilex++) {
	                                 tiletovox[tilex] = lastvoxid;
	                            }
	                            break; //last tile number (inclusive)
	                        case T_SCALE:
	                            double scale=1.0;
	                            scale = script.getdouble();
	                            if (voxmodels[lastvoxid] != null)
	                                voxmodels[lastvoxid].scale = (float) scale;
	                            break;
	                        case T_ROTATE:
	                        	rotate = 1;
	                        	break;
	                        
	                    }
	                }
//	                Console.Println("Voxel model loaded \"" + vfn + "\"", false);
	                if(tilex != -1 && rotate != -1) {
	                	voxrotate[tilex] = true;
	                }
	                lastvoxid = -1;
	    			break;
	    		case T_SOUND:
	            case T_MUSIC:
	            {
	                int dummy;
	                String t_id = null, t_file = null;
	                final Map<String , Integer> sound_musictokens = new HashMap<String , Integer>() {
	    				private static final long serialVersionUID = 1L;
	    				{
	    					put("id",     T_ID);
	    					put("file",    T_FILE);
	    				}
	    	        };

	    	        if ((dummy = script.getbraces()) == -1) break;
	                while (script.textptr < dummy)
	                {
	                	switch (getatoken(script,sound_musictokens))
	                    {
	                    case T_ID:
	                    	t_id = script.getstring(); break;
	                    case T_FILE:
	                    	t_file = script.getstring(); break;
	                    }
	                }
	                
	                if(script.path != null)
	                	t_file = script.path + File.separator + t_file;
	                addDigitalMusic(t_id, t_file);
	            }
	            break;
            }
        }
    }

	public static void loaddefinitionsfile(String filename, String path)
	{
		Scriptfile script = Scriptfile.scriptfile_fromfile(filename);
		if (script == null) return;
		script.path = path;
		defsparser(script);
	}
	
	public static void loaddefinitionsfile(String filename)
	{
		Scriptfile script = Scriptfile.scriptfile_fromfile(filename);
		if (script == null) return;
		defsparser(script);
	}
}

