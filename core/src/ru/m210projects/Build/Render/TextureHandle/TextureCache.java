/*
 * TextureCache by Kirill Klimenko-KLIMaka 
 * Based on parts of "Polymost" by Ken Silverman
 * 
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.GL_RGB;
import static com.badlogic.gdx.graphics.GL20.GL_RGBA;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.waloff;
import static ru.m210projects.Build.Engine.usehightile; //TODO: GL settings
import static ru.m210projects.Build.Render.TextureHandle.ImageUtils.*;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.*;
import static ru.m210projects.Build.Render.Types.GL10.*;

import java.nio.ByteBuffer;

import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.TextureHandle.ImageUtils.PicInfo;
import ru.m210projects.Build.Script.TextureHDInfo;
import ru.m210projects.Build.Types.UnsafeBuffer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class TextureCache {
	
	private final ValueResolver<Integer> anisotropy;
	private final Pthtyp[] cache;
    private TextureHDInfo info;
    
    private ShaderProgram shader;
	private BTexture palette[];

	public TextureCache(ValueResolver<Integer> anisotropy) {
		this.anisotropy = anisotropy;
		cache = new Pthtyp[MAXTILES];
	    boolean useShader = false;
	    if(useShader)
	    	shader = createShader();
	    changePalette(Engine.palette);
	}
	
	public void setTextureInfo(TextureHDInfo info)
	{
		this.info = info;
	}

	private Pthtyp get(int picnum, int palnum, boolean clamped, int surfnum) {
		for (Pthtyp pth = cache[picnum]; pth != null; pth = pth.next)
	    {
			if (pth.picnum == picnum
					&& pth.palnum == palnum
					&& pth.isClamped() == clamped
					&& pth.skyface == surfnum)
				return pth;
	    }
		return null;
	}

	private void add(Pthtyp tex) {
		tex.next = cache[tex.picnum];
		cache[tex.picnum] = tex;
	}

	public void invalidate(int dapicnum, int dapalnum, boolean clamped) {
		invalidate(get(dapicnum, dapalnum, clamped, 0));
	}

	public void invalidateall() {
		for(int j=MAXTILES-1;j>=0;j--)
			for(Pthtyp pth = cache[j]; pth != null; pth = pth.next)
				invalidate(pth);
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

	private Pthtyp loadTileArt(int dapic, int dapal, boolean clamping, boolean alpha, Pthtyp pth, boolean doalloc) {
		int tsizx = tilesizx[dapic];
		int tsizy = tilesizy[dapic];
		int xsiz = calcSize(tsizx);
		int ysiz = calcSize(tsizy);

		if (palookup[dapal] == null)
			dapal = 0;

		PicInfo picInfo = loadPic(xsiz, ysiz, tsizx, tsizy, waloff[dapic], dapal, clamping, alpha, shader != null);

		//Realloc for user tiles
		if (pth.glpic != null && (pth.glpic.getWidth() != xsiz || pth.glpic.getHeight() != ysiz)) {
			pth.glpic.dispose(); 
			doalloc = true;
		}

		if (doalloc) {
			try {
				pth.glpic = new BTexture(xsiz, ysiz);
			} catch(Exception e) { return null; }
		}
		
		bindTexture(pth.glpic);
		int intexfmt = (picInfo.hasalpha ? GL_RGBA : GL_RGB);

		if (Gdx.app.getType() == ApplicationType.Android)
			intexfmt = GL_RGBA; // android bug? black textures fix

		uploadBoundTexture(doalloc, xsiz, ysiz, intexfmt, GL_RGBA, picInfo.pic, tsizx, tsizy);
		int gltexfiltermode = Console.Geti("r_texturemode"); //TODO: GL settings
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
					npix.setFilter(Filter.NearestNeighbour);
					
					if(!clamping) {
						for(int i = 0; i < 2; i++)
						{
							npix.drawPixmap(pix,
							        0, 0, pth.sizx, pth.sizy,
							        0, i * pth.sizy, psizx, pth.sizy
							);
						}
						pth.sizx = (short) psizx;

//						npix.drawPixmap(pix,
//						        0, 0, pix.getWidth(), pix.getHeight(),
//						        0, 0, psizx, psizy
//						);
//						pth.sizx = (short) psizx;
//						pth.sizy = (short) psizy;
					} else npix.drawPixmap(pix, 0, 0);
					
					pix.dispose();
					pix = npix;
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

	private Pthtyp cache_tryart(int dapicnum, int dapalnum, boolean clamping, boolean alpha) {
		// load from art
		Pthtyp pth = get(dapicnum, dapalnum, clamping, 0);
		if (pth != null) {
			if (pth.isInvalidated()) {
				pth.setInvalidated(false);
				pth = loadTileArtNoAlloc(dapicnum, dapalnum, clamping, alpha, pth);
			}
		} else {
			pth = gloadTileArtAlloc(dapicnum, dapalnum, clamping, alpha, new Pthtyp());
			if (pth != null) 
				add(pth);
		}
		return pth;
	}
	
	public Pthtyp cache(int dapicnum, int dapalnum, short skybox, boolean clamping, boolean alpha)
	{
		Hicreplctyp si = (usehightile && info != null) ? info.findTexture(dapicnum,dapalnum,skybox) : null;

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
				if((pth = loadHighTileNoAlloc(dapicnum, dapalnum, clamping, alpha, skybox, si, pth, (si.palnum>0 || info == null) ? 0 : info.getPaletteEffect(dapalnum))) == null) // reload tile
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
			
			if(dapalnum != 0 && info.findTexture(dapicnum, 0, skybox) == si && (pth = get(dapicnum, 0, clamping, skybox)) != null)
				return pth;

			pth = gloadHighTileAlloc(dapicnum, dapalnum, clamping, alpha, skybox, si, new Pthtyp(), (si.palnum>0 || info == null) ? 0 : info.getPaletteEffect(dapalnum));
			if (pth != null) {
				pth.skyface = skybox;
				add(pth);
			} else // failed, so try for ART
				return cache_tryart(dapicnum, dapalnum, clamping, alpha);
		}
		return pth;
	}
	
	public void updateSettings(int gltexfiltermode) {
		for (int i=MAXTILES-1; i>=0; i--) {
			for (Pthtyp pth=cache[i]; pth != null; pth = pth.next) {
				bindTexture(pth.glpic);
				setupBoundTexture(gltexfiltermode, anisotropy.get());
			}
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
		for (Pthtyp pth = cache[dapicnum]; pth != null; pth = pth.next)
	    {
			if ((pth.picnum == dapicnum) && (pth.palnum == dapalnum))
		    	return pth.hasAlpha();
	    }
		
		return(true);
	}

	public void uninit() {
		for (int i=MAXTILES-1; i>=0; i--) {
			for (Pthtyp pth=cache[i]; pth != null;) {
				Pthtyp next = pth.next;
				pth.glpic.dispose();
				pth = next;
			}
			cache[i] = null;
		}
	}
	
	public void savetexture(ByteBuffer pixels, int tw, int th, int w, int h, int num) {
		Pixmap pixmap = new Pixmap(w, h, Format.RGB888);

		for (int i = 0; i < (tw * th); i++) {
			int row = (int) Math.floor(i / tw);
			int col = i % tw;
			if (col < w && row < h) {
				pixmap.setColor((pixels.get(4 * i) & 0xFF) / 255.f, (pixels.get(4 * i + 1) & 0xFF) / 255.f, (pixels.get(4 * i + 2) & 0xFF) / 255.f, 1);
				pixmap.drawPixel(col, row);
			}
		}

		PixmapIO.writePNG(new FileHandle("texture" + num + ".png"), pixmap);

		System.out.println("texture" + num + ".png saved!");
		pixmap.dispose();
	}
	
	
	//Shader feature
	
	private BTexture createPalette(byte[] paldata, int shade)
	{
		UnsafeBuffer buffer = getTmpBuffer();
		buffer.clear();
		for(int p = 0; p < MAXPALOOKUPS; p++) {
			int pal = p;
			if(palookup[pal] == null) pal = 0;
			
			for(int i = 0; i < 256; i++)
			{
				int dacol = palookup[pal][i + (shade << 8)] & 0xFF;
				buffer.put(paldata[3 * dacol]);
				buffer.put(paldata[3 * dacol + 1]); 
				buffer.put(paldata[3 * dacol + 2]); 
			}
		}
		buffer.flip();

		BTexture palette = new BTexture(256, MAXPALOOKUPS);
		palette.bind(1);
		Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, palette.getWidth(), palette.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, buffer.getBuffer());
		setupBoundTexture(0, 0);
		
		return palette;
	}
	
	public void changePalette(byte[] pal)
	{
		if(!isUseShader()) return;
		
        for(int i = 0; i < numshades; i++) {
        	if(palette[i] != null)
        		palette[i].dispose();
        	palette[i] = createPalette(pal, i);
        }
	}
	
	private ShaderProgram createShader() 
	{
	    String fragment = new String(kGetBytes("fragment.glsl", 0));
	    String vertex = new String(kGetBytes("vertex.glsl", 0));
	    
	    ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if(!shader.isCompiled())
        	Console.Println("Shader compile error: " + shader.getLog(), OSDTEXT_RED);

        palette = new BTexture[numshades];

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        
        return shader;
	}
	
	public void bindShader()
	{
		if(shader != null) 
			shader.begin();
	}

	public void unbindShader()
	{
		if(shader != null) {
			shader.end();
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		}
	}
	
	public void setShaderParams(int pal, int shade)
	{
		if(shader == null) return;
		
		palette[shade].bind(1);
		shader.setUniformi("u_colorTable", 1);
		shader.setUniformf("u_pal", pal);
	}
	
	public void shaderTransparent(float alpha)
	{
		if(shader != null)
			shader.setUniformf("u_alpha", alpha);
	}
	
	public boolean isUseShader()
	{
		return shader != null;
	}
}
