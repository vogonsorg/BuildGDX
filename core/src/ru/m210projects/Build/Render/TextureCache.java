/*
 * TextureCache by Kirill Klimenko-KLIMaka 
 * Based on parts of "Polymost" by Ken Silverman
 * 
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.waloff;
import static ru.m210projects.Build.Engine.usehightile;
import static ru.m210projects.Build.Render.ImageUtils.loadPic;
import static ru.m210projects.Build.Render.TextureUtils.bindTexture;
import static ru.m210projects.Build.Render.TextureUtils.setupBoundTexture;
import static ru.m210projects.Build.Render.TextureUtils.setupBoundTextureWrap;
import static ru.m210projects.Build.Render.TextureUtils.uploadBoundTexture;
import static ru.m210projects.Build.Render.Types.GL10.GL_CLAMP;
import static ru.m210projects.Build.Render.Types.GL10.GL_CLAMP_TO_EDGE;
import static ru.m210projects.Build.Render.Types.GL10.GL_REPEAT;
import static ru.m210projects.Build.Render.Types.GL10.GL_RGB;
import static ru.m210projects.Build.Render.Types.GL10.GL_RGBA;
import static ru.m210projects.Build.Render.Types.Hightile.hicfindsubst;
import static ru.m210projects.Build.Render.Types.Hightile.hictinting;
import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.util.HashMap;
import java.util.Map;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ImageUtils.PicInfo;
import ru.m210projects.Build.Render.Types.BTexture;
import ru.m210projects.Build.Render.Types.Hicreplctyp;
import ru.m210projects.Build.Render.Types.Pthtyp;
import ru.m210projects.Build.Render.Types.ValueResolver;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

public class TextureCache {
	
	/*
	private static class TextureKey {
		public final int picnum;
		public final int palnum;
		public final boolean clamped;
		public final int surfnum;

		public TextureKey(int picnum, int palnum, boolean clamped, int surfnum) {
			this.picnum = picnum;
			this.palnum = palnum;
			this.clamped = clamped;
			this.surfnum = surfnum;
		}

		@Override
		public int hashCode() {
			return (clamped ? 31 : 0) ^ picnum ^ palnum ^ surfnum;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TextureKey) {
				TextureKey key = (TextureKey) obj;
				return picnum == key.picnum 
						&& palnum == key.palnum 
						&& clamped == key.clamped 
						&& surfnum == key.surfnum;
						// ++  == effects
			}
			return false;
		}
	}
	*/

	private final ValueResolver<Integer> anisotropy;

	private final Map<TextureKey, Pthtyp> cache = new HashMap<TextureKey, Pthtyp>();

    private final MutableTextureKey mutableTextureKey = new MutableTextureKey(); // <----- reusable mutable key

	public TextureCache(ValueResolver<Integer> anisotropy) {
		this.anisotropy = anisotropy;
	}

	private Pthtyp get(int picnum, int palnum, boolean clamped, int surfnum) {
		return this.cache.get(this.mutableTextureKey.picnum(picnum).palnum(palnum).clamped(clamped).surfnum(surfnum));   // <----- not creating new instances
//		return cache.get(new TextureKey(picnum, palnum, clamped, surfnum));
	}

	private void add(int picnum, Pthtyp tex, int surfnum) {
//		cache.put(new TextureKey(picnum, tex.palnum, tex.isClamped(), surfnum), tex);
		this.cache.put(this.mutableTextureKey
                .picnum(picnum)
                .palnum(tex.palnum)
                .clamped(tex.isClamped())
                .toImmutable(), // <----- creates immutable key only when necessary, i.e. when adding a texture to the cache
                tex
        );
	}

	public void invalidate(int dapicnum, int dapalnum, boolean clamped) {
		invalidate(get(dapicnum, dapalnum, clamped, 0));
	}

	public void invalidateall() {
		for (Pthtyp pth : cache.values()) {
			invalidate(pth);
		}
	}

	private Pthtyp gloadTileArtAlloc(int dapic, int dapal, boolean clamping, boolean alpha, Pthtyp pth) {
		return loadTileArt(dapic, dapal, clamping, alpha, pth, true);
	}

	private Pthtyp loadTileArtNoAlloc(int dapic, int dapal, boolean clamping, boolean alpha, Pthtyp pth) {
		return loadTileArt(dapic, dapal, clamping, alpha, pth, false);
	}
	
	private Pthtyp gloadHighTileAlloc(int dapic, int dapal, boolean clamping, boolean alpha, int facen, Hicreplctyp hicr, Pthtyp pth, int effect) {
		return loadHighTile(dapic, dapal, clamping, alpha, facen, hicr, pth, effect, true);
	}

	private Pthtyp loadHighTileNoAlloc(int dapic, int dapal, boolean clamping, boolean alpha, int facen, Hicreplctyp hicr, Pthtyp pth, int effect) {
		return loadHighTile(dapic, dapal, clamping, alpha, facen, hicr, pth, effect, false);
	}

	public static int calcSize(int size) {
		int nsize = 1;
		if (GLInfo.texnpot == 0) {
			for (; nsize < size; nsize *= 2)
				;
			return nsize;
		}
		return size == 0 ? 1 : size;
	}

	private Pthtyp loadTileArt(int dapic, int dapal, boolean clamping, boolean alpha, Pthtyp pth, boolean doalloc) {
		int tsizx = tilesizx[dapic];
		int tsizy = tilesizy[dapic];
		int xsiz = calcSize(tsizx);
		int ysiz = calcSize(tsizy);

		if (palookup[dapal] == null)
			dapal = 0;

		PicInfo picInfo = loadPic(xsiz, ysiz, tsizx, tsizy, waloff[dapic], dapal, clamping, alpha);

		//Realloc for user tiles
		if (pth.glpic != null && (pth.glpic.getWidth() != xsiz || pth.glpic.getHeight() != ysiz)) {
			pth.glpic.dispose(); 
			doalloc = true;
		}

		if (doalloc) {
			try {
				pth.glpic = new BTexture();
			} catch(Exception e) { return null; }
		}
		
		bindTexture(pth.glpic);
		int intexfmt = picInfo.hasalpha ? GL_RGBA : GL_RGB;

		if (Gdx.app.getType() == ApplicationType.Android)
			intexfmt = GL_RGBA; // android bug? black textures fix

		uploadBoundTexture(doalloc, xsiz, ysiz, intexfmt, GL_RGBA, picInfo.pic, tsizx, tsizy);
		int gltexfiltermode = Console.Geti("r_texturemode");
		setupBoundTexture(gltexfiltermode, anisotropy.get());
		int wrap = !clamping ? GL_REPEAT : GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP;
		setupBoundTextureWrap(wrap);

		pth.picnum = (short) dapic;
		pth.palnum = (short) dapal;
		pth.effects = 0;
		pth.setClamped(clamping);
		pth.setHasAlpha(picInfo.hasalpha);
		pth.hicr = null;
		return pth;
	}
	
	private Pthtyp loadHighTile(int dapic, int dapal, boolean clamping, boolean alpha, int facen, Hicreplctyp hicr, Pthtyp pth, int effect, boolean doalloc) {
		
		if (hicr == null) return null;
		
		String fn = null;
		if (facen > 0)
	    {
	        if (hicr.skybox == null) return null;
	        if (facen > 6) return null;
	        if (hicr.skybox.face[facen-1] == null) return null;
	        fn = hicr.skybox.face[facen-1];
	    }
	    else
	    {
	        if (hicr.filename == null) return null;
	        fn = hicr.filename;
	    }
		
		if (!kExist(fn, 0)) 
	    {
			Console.Print("hightile: " + fn + "(pic " + dapic + ") not found");
	        if (facen > 0)
	            hicr.skybox.ignore = 1;
	        else
	            hicr.ignore = 1;
	        return null;
	    }
	    
	    //int cachefil = polymost_trytexcache(fn, picfillen+(dapalnum<<8), dameth, effect, &cachead, 0);
	    //if (cachefil >= 0) { ... }

		if (doalloc) {
			byte[] data = kGetBytes(fn, 0);
			if(data == null) return null;
			try {
				Pixmap pix = new Pixmap(data, 0, data.length);
				
				int psizx = calcSize(pix.getWidth());
				int psizy = calcSize(pix.getHeight());
				
				pth.sizx = (short) pix.getWidth();
				pth.sizy = (short) pix.getHeight();
				
				//Texture width and height must be powers of two
				if(psizx != pix.getWidth() || psizy != pix.getHeight())
				{
					Pixmap npix = new Pixmap(psizx, psizy, pix.getFormat());
					npix.drawPixmap(pix,
					        0, 0, pix.getWidth(), pix.getHeight(),
					        0, 0, psizx, psizy
					);
					pix.dispose();
					pix = npix;
					
					pth.sizx = (short) psizx;
					pth.sizy = (short) psizy;
				}
				pth.glpic = new BTexture(pix, true); 
				pix.dispose();
			} catch(Exception e) { 
				if (facen > 0)
					hicr.skybox.ignore = 1;
				else
					hicr.ignore = 1; return null; 
			} 
		}

		int tsizx = pth.sizx;
		int tsizy = pth.sizy;

		bindTexture(pth.glpic);
		int gltexfiltermode = Console.Geti("r_texturemode");
		
		setupBoundTexture(gltexfiltermode, anisotropy.get());
		int wrap = !clamping ? GL_REPEAT : GLInfo.clamptoedge ? GL_CLAMP_TO_EDGE : GL_CLAMP;
		setupBoundTextureWrap(wrap);

		pth.picnum = (short) dapic;
		pth.palnum = (short) dapal;
		pth.effects = 0;
		pth.setClamped(clamping);
		pth.setHasAlpha(alpha);
		pth.setHighTile(true);
		if(facen > 0)
			pth.setSkyboxFace(true);
		pth.hicr = hicr;
		
		if (facen > 0)
		{
		 	pth.scalex = ((float)tsizx) / 64.0f;
		    pth.scaley = ((float)tsizy) / 64.0f;
		}
		else
		{
		 	pth.scalex = ((float)tsizx) / ((float)tilesizx[dapic]);
		    pth.scaley = ((float)tsizy) / ((float)tilesizy[dapic]);
		}

		return pth;
	}

	public Pthtyp cache_tryart(int dapicnum, int dapalnum, boolean clamping, boolean alpha) {
		// load from art
		Pthtyp pth = get(dapicnum, dapalnum, clamping, 0);
		if (pth != null) {
			if (pth.isInvalidated()) {
				pth.setInvalidated(false);
				pth = loadTileArtNoAlloc(dapicnum, dapalnum, clamping, alpha, pth);
			}
		} else {
			pth = gloadTileArtAlloc(dapicnum, dapalnum, clamping, alpha, new Pthtyp());
			if (pth != null) {
				add(dapicnum, pth, 0);
			}
		}
		return pth;
	}
	
	public Pthtyp cache(int dapicnum, int dapalnum, int skybox, boolean clamping, boolean alpha)
	{
		Hicreplctyp si = usehightile ? hicfindsubst(dapicnum,dapalnum,skybox) : null;

		if (si == null)
	    {
	        if (skybox != 0 || dapalnum >= (MAXPALOOKUPS - RESERVEDPALS)) return null;
	        return cache_tryart(dapicnum, dapalnum, clamping, alpha);
	    }

		/* if palette > 0 && replacement found
	     *    no effects are applied to the texture
	     * else if palette > 0 && no replacement found
	     *    effects are applied to the palette 0 texture if it exists
	     */

	    // load a replacement
		Pthtyp pth = get(dapicnum, dapalnum, clamping, skybox);
		
		if (pth != null) {
			if (pth.isInvalidated()) {
				pth.setInvalidated(false);
				if((pth = loadHighTileNoAlloc(dapicnum, dapalnum, clamping, alpha, skybox, si, pth, (si.palnum>0) ? 0 : hictinting[dapalnum].f)) == null) // reload tile
				{
					if (skybox != 0) return null;
					return cache_tryart(dapicnum, dapalnum, clamping, alpha);
				}
			}
		} else {
			// possibly fetch an already loaded multitexture :_)
			//
			// { ... }  if (dapalnum >= (MAXPALOOKUPS - RESERVEDPALS))
			//

			pth = gloadHighTileAlloc(dapicnum, dapalnum, clamping, alpha, skybox, si, new Pthtyp(), (si.palnum>0) ? 0 : hictinting[dapalnum].f);
			if (pth != null) {
				add(dapicnum, pth, skybox);
			} else // failed, so try for ART
				return cache_tryart(dapicnum, dapalnum, clamping, alpha);
		}
		return pth;
	}

	public void updateSettings(int gltexfiltermode) {
		for (Pthtyp pth : cache.values()) {
			bindTexture(pth.glpic);
			setupBoundTexture(gltexfiltermode, anisotropy.get());
		}
	}

	private static void invalidate(Pthtyp pth) {
		if (pth == null)
			return;
		if (pth.hicr == null) {
			pth.setInvalidated(true);
		}
	}
	
	public boolean gltexmayhavealpha(int dapicnum, int dapalnum)
	{
		for (Pthtyp pth : cache.values()) 
			if ((pth.picnum == dapicnum) && (pth.palnum == dapalnum))
		    	return((pth.flags&8) != 0);
		return(true);
	}

	public void uninit() {
		for (Pthtyp pth : cache.values()) {
			pth.glpic.dispose();
		}
		cache.clear();
	}
}
