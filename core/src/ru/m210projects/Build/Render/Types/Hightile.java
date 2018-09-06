/*
* High-colour textures support for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Render.Types;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.USERTILES;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.Palette;

public class Hightile {

	public static Hicreplctyp[] hicreplc = new Hicreplctyp[MAXTILES];
	public static Palette[] hictinting = new Palette[MAXPALOOKUPS];
	public static boolean hicfirstinit;
	public static final int  HICEFFECTMASK = (1|2|4|8);
	public static int first = 1;
	
	//
	// find the index into hicreplc[] which contains the replacement tile particulars
	//
	public static Hicreplctyp hicfindsubst(int picnum, int palnum, int skybox)
	{
	    if (!hicfirstinit || picnum >= MAXTILES) return null;

	    do
	    {
	        if (skybox != 0)
	        {
	            Hicreplctyp hr = hicreplc[picnum];
	            for (; hr != null; hr = hr.next)
	                if (hr.palnum == palnum && hr.skybox != null && hr.skybox.ignore == 0)
	                    return hr;
	        }
	        else
	        {
	            Hicreplctyp hr = hicreplc[picnum];
	            for (; hr != null; hr = hr.next)
	                if (hr.palnum == palnum && hr.ignore == 0)
	                    return hr;
	        }

	        if (palnum == 0 || palnum >= (MAXPALOOKUPS - RESERVEDPALS)) break;
	        palnum = 0;
	    }
	    while (true);

	    return null;	// no replacement found
	}
	
	//
	// hicinit()
	//   Initialize the high-colour stuff to default.
	//
	public static void hicinit()
	{
	    for (int i=0; i<MAXPALOOKUPS; i++)  	// all tints should be 100%
	    {
	    	hictinting[i] = new Palette(0xff, 0xff, 0xff, 0);
	    }

	    if (hicfirstinit)
	        for (int i=MAXTILES-1; i>=0; i--)
	        {
	            for (Hicreplctyp hr=hicreplc[i]; hr != null;)
	            {
	            	Hicreplctyp next = hr.next;

	                if (hr.skybox != null)
	                {
	                    for (int j=5; j>=0; j--)
	                        if (hr.skybox.face[j] != null)
	                            hr.skybox.face[j] = null;
	                    hr.skybox = null;
	                }

	                if (hr.filename != null)
	                	hr.filename = null;

	                hr = null;

	                hr = next;
	            }
	        }

	    hicfirstinit = true;
	}
	
	//
	// hicsetpalettetint(pal,r,g,b,effect)
	//   The tinting values represent a mechanism for emulating the effect of global sector
	//   palette shifts on true-colour textures and only true-colour textures.
	//   effect bitset: 1 = greyscale, 2 = invert
	//
	public static void hicsetpalettetint(int palnum, int r, int g, int b, int effect)
	{
	    if (palnum >= MAXPALOOKUPS) return;
	    if (!hicfirstinit) hicinit();

	    hictinting[palnum].r = r;
	    hictinting[palnum].g = g;
	    hictinting[palnum].b = b;
	    hictinting[palnum].f = effect & HICEFFECTMASK;
	}
	
	//
	// hicsetsubsttex(picnum,pal,filen,alphacut)
	//   Specifies a replacement graphic file for an ART tile.
	//
	public static int hicsetsubsttex(int picnum, int palnum, String filen, float alphacut, float xscale, float yscale, float specpower, float specfactor, int flags)
	{
	    Hicreplctyp hr, hrn;

	    if (picnum >= MAXTILES) return -1;
	    if (palnum >= MAXPALOOKUPS) return -1;
	    if (!hicfirstinit) hicinit();

	    for (hr = hicreplc[picnum]; hr != null; hr = hr.next)
	    {
	        if (hr.palnum == palnum)
	            break;
	    }

	    if (hr == null)
	    {
	        // no replacement yet defined
	        hrn = new Hicreplctyp();
	        hrn.palnum = palnum;
	    }
	    else hrn = hr;

	    // store into hicreplc the details for this replacement
	    hrn.filename = filen;
	    
	    if (hrn.filename == null)
	    {
	    	if (hrn.skybox != null) return -1;	// don't free the base structure if there's a skybox defined
	        if (hr == null) hrn = null;	// not yet a link in the chain
	        return -1;
	    }
	    hrn.ignore = 0;
	    hrn.alphacut = (float) Math.min(alphacut,1.0);
	    hrn.xscale = xscale;
	    hrn.yscale = yscale;
	    hrn.specpower = specpower;
	    hrn.specfactor = specfactor;
	    hrn.flags = flags;
	    if (hr == null)
	    {
	        hrn.next = hicreplc[picnum];
	        hicreplc[picnum] = hrn;
	    }

	    //user tiles will load later
	    if (picnum < (MAXTILES - USERTILES) && (tilesizx[picnum]<=0 || tilesizy[picnum]<=0))
	    {
	        if (first != 0)
	        {
	        	Console.Println("Warning: defined hightile replacement for empty tile " + picnum, OSDTEXT_YELLOW);
	        	Console.Println(" Maybe some tilesXXX.art are not loaded?", OSDTEXT_YELLOW);
	            first = 0;
	        }
	    }

	    return 0;
	}
	
	//
	// hicsetskybox(picnum,pal,faces[6])
	//   Specifies a graphic files making up a skybox.
	//
	public static int hicsetskybox(int picnum, int palnum, String[] faces)
	{
		Hicreplctyp hr, hrn;
		 
	    if (picnum >= MAXTILES) return -1;
	    if (palnum >= MAXPALOOKUPS) return -1;
	    for (int j=5; j>=0; j--) if (faces[j] == null) return -1;
	    if (!hicfirstinit) hicinit();

	    for (hr = hicreplc[picnum]; hr != null; hr = hr.next)
	    {
	        if (hr.palnum == palnum)
	            break;
	    }

	    if (hr == null)
	    {
	        // no replacement yet defined
	    	hrn = new Hicreplctyp();
	        hrn.palnum = palnum;
	    }
	    else hrn = hr;

	    if (hrn.skybox == null)
	        hrn.skybox = new Hicskybox();
	    else
	    {
	        for (int j=5; j>=0; j--)
	        {
	            if (hrn.skybox.face[j] != null)
	            	hrn.skybox.face[j] = null;
	        }
	    }

	    // store each face's filename
	    for (int j=0; j<6; j++)
	    {
	        hrn.skybox.face[j] = faces[j];
	        if (hrn.skybox.face[j] == null)
	        {
	            for (--j; j>=0; --j)	// free any previous faces
	               hrn.skybox.face[j] = null;
	            hrn.skybox = null;
	            if (hr == null) hrn = null;
	            return -1;
	        }
	    }
	    hrn.skybox.ignore = 0;
	    if (hr == null)
	    {
	        hrn.next = hicreplc[picnum];
	        hicreplc[picnum] = hrn;
	    }

	    return 0;
	}
	
	//
	// hicclearsubst(picnum,pal)
	//   Clears a replacement for an ART tile, including skybox faces.
	//
	public int hicclearsubst(int picnum, int palnum)
	{
	    Hicreplctyp hr, hrn = null;

	    if (picnum >= MAXTILES) return -1;
	    if (palnum >= MAXPALOOKUPS) return -1;
	    if (!hicfirstinit) return 0;

	    for (hr = hicreplc[picnum]; hr != null; hrn = hr, hr = hr.next)
	    {
	        if (hr.palnum == palnum)
	            break;
	    }

	    if (hr == null) return 0;

	    if (hr.filename != null) hr.filename = null;
	    if (hr.skybox != null)
	    {
	        for (int i=5; i>=0; i--)
	            if (hr.skybox.face[i] != null)
	                hr.skybox.face[i] = null;
	        hr.skybox = null;
	    } 
	    if (hrn != null) hrn.next = hr.next;
	    else hicreplc[picnum] = hr.next;
	    hr = null;

	    return 0;
	}
}
