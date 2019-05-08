package ru.m210projects.Build.Render.GdxRender;

import static com.badlogic.gdx.graphics.GL20.GL_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.headspritesect;
import static ru.m210projects.Build.Engine.nextspritesect;
import static ru.m210projects.Build.Engine.numsectors;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Engine.picanm;
import static ru.m210projects.Build.Engine.pow2char;
import static ru.m210projects.Build.Engine.sector;
import static ru.m210projects.Build.Engine.show2dsector;
import static ru.m210projects.Build.Engine.sintable;
import static ru.m210projects.Build.Engine.smalltextfont;
import static ru.m210projects.Build.Engine.textfont;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.wall;
import static ru.m210projects.Build.Engine.waloff;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Engine.yxaspect;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.mulscale;
import static ru.m210projects.Build.Render.GLInfo.anisotropy;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.setupBoundTexture;
import static ru.m210projects.Build.Render.Types.GL10.GL_ALPHA_TEST;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.Renderer;
import ru.m210projects.Build.Render.TextureHandle.BTexture;
import ru.m210projects.Build.Render.TextureHandle.Pthtyp;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;
import ru.m210projects.Build.Render.TextureHandle.ValueResolver;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.Timer;
import ru.m210projects.Build.Types.WALL;

public class GdxRenderer implements Renderer {
	
	protected final TextureCache textureCache;
	protected final Engine engine;
	protected final GdxBatch batch;
	protected final ShapeRenderer shape;
	protected BTexture textAtlas;

	protected DefScript defs;
	
	public GdxRenderer(Engine engine) {
		BuildGdx.app.setFrame(FrameType.GL);
		this.engine = engine;
		this.textureCache = createTextureCache();
		
		this.batch = new GdxBatch();
		this.shape = new ShapeRenderer();

		init();
	}
	
	private BTexture getFont()
	{
		if(textAtlas == null)
		{
			// construct a 256x128 8-bit alpha-only texture for the font glyph matrix
			Timer.start();
			byte[] tbuf = new byte[256 * 128];
			int tptr, i, j;
			for (int h = 0; h < 256; h++) {
				tptr = (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 0; i < 8; i++) {
					for (j = 0; j < 8; j++) {
						if ((textfont[h * 8 + i] & pow2char[7 - j]) != 0) 
							tbuf[tptr + j] = -1; //byte 255
					}
					tptr += 256;
				}
			}

			for (int h = 0; h < 256; h++) {
				tptr = 256 * 64 + (h % 32) * 8 + (h / 32) * 256 * 8;
				for (i = 1; i < 7; i++) {
					for (j = 2; j < 6; j++) {
						if ((smalltextfont[h * 8 + i] & pow2char[7 - j]) != 0) 
							tbuf[tptr + j- 2] = -1; //byte 255
					}
					tptr += 256;
				}
			}
			
			ByteBuffer fbuf = BufferUtils.newByteBuffer(tbuf.length);
			fbuf.put(tbuf).rewind();

			textAtlas = new BTexture(256, 128);
			textAtlas.bind();
			Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, textAtlas.getWidth(), textAtlas.getHeight(), 0, GL_ALPHA, GL_UNSIGNED_BYTE, fbuf);
			setupBoundTexture(0, 0);
			Timer.result();
		}
		
		return textAtlas;
	}
	
	private void bindBatch()
	{
		if(!batch.isDrawing()) 
			batch.begin();
	}
	
	private void unbindBatch()
	{
		if(batch.isDrawing()) 
			batch.end();
	}
	
	private TextureCache createTextureCache() {
		return new TextureCache(new ValueResolver<Integer>() {
			@Override
			public Integer get() {
				return anisotropy();
			}
		});
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uninit() {
		if (textAtlas != null) 
			textAtlas.dispose();
	}

	@Override
	public void drawmasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawrooms() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearview(int dacol) {
		BuildGdx.gl.glClearColor( (curpalette[3*dacol]&0xFF) / 255.0f,
				(curpalette[3*dacol+1]&0xFF) / 255.0f,
				(curpalette[3*dacol+2]&0xFF) / 255.0f,
				0);
		BuildGdx.gl.glClear(GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void palfade(HashMap<String, FadeEffect> fades) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void precache(int dapicnum, int dapalnum, int datype) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextpage() {
		unbindBatch();
		engine.faketimerhandler();
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

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {

		if (picnum >= MAXTILES) return;
		if ((cx1 > cx2) || (cy1 > cy2)) return;
		if (z <= 16) return;

		if ((picanm[picnum] & 192) != 0)
			picnum += engine.animateoffs((short) picnum, (short) 0xc000);
		
		if ((tilesizx[picnum] <= 0) || (tilesizy[picnum] <= 0))
			return;
		
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
		
		int xsiz = tilesizx[picnum];
		int ysiz = tilesizy[picnum];

		int xoff = 0, yoff = 0;
		if ((dastat & 16) == 0) {
			xoff = (int) ((byte) ((picanm[picnum] >> 8) & 255)) + (xsiz >> 1);
			yoff = (int) ((byte) ((picanm[picnum] >> 16) & 255)) + (ysiz >> 1);
		}

		if ((dastat & 4) != 0)
			yoff = ysiz - yoff;

		if (picnum >= MAXTILES) picnum = 0;
		if (palookup[dapalnum & 0xFF] == null)
			dapalnum = 0;
		
		engine.setgotpic(picnum);
		if (waloff[picnum] == null) 
			engine.loadtile(picnum);

		Pthtyp pth = textureCache.cache(picnum, dapalnum, (short) 0, textureCache.clampingMode(method), textureCache.alphaMode(method));
		if(pth == null) return;

		if (((method & 3) == 0))
			batch.disableBlending();
		else batch.enableBlending();

		float shade = (numshades - min(max(dashade, 0), numshades)) / (float) numshades;
		float alpha = 1.0f;
		switch (method & 3) {
			case 2: alpha = TRANSLUSCENT1; break;
			case 3: alpha = TRANSLUSCENT2; break;
		}
	
		bindBatch();
		batch.setColor(shade, shade, shade, alpha);
		batch.draw(pth.glpic, sx, sy, xsiz, ysiz, xoff, yoff, a, z, dastat, cx1, cy1, cx2, cy2);
	}

	@Override
	public String getname() {
		return "GdxRenderer";
	}

	@Override
	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		int i, j, k, x1, y1, x2 = 0, y2 = 0, ox, oy;
		int z1, z2, startwall, endwall;
		int xvect, yvect, xvect2, yvect2;

		WALL wal, wal2;

		xvect = sintable[(-cang) & 2047] * czoom;
		yvect = sintable[(1536 - cang) & 2047] * czoom;
		xvect2 = mulscale(xvect, yxaspect, 16);
		yvect2 = mulscale(yvect, yxaspect, 16);

		// Draw red lines
		for (i = 0; i < numsectors; i++) {
			if ((show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			startwall = sector[i].wallptr;
			endwall = sector[i].wallptr + sector[i].wallnum;

			z1 = sector[i].ceilingz;
			z2 = sector[i].floorz;

			if (startwall < 0 || endwall < 0)
				continue;

			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if (wal == null)
					continue;
				k = wal.nextwall;
				if (k < 0 || k > j)
					continue;
				if (wal.nextsector < 0)
					continue;

				if (sector[wal.nextsector] != null
						&& ((sector[wal.nextsector].ceilingz != z1 || sector[wal.nextsector].floorz != z2
								|| (wall[wal.nextwall] != null
										&& ((wal.cstat | wall[wal.nextwall].cstat) & (16 + 32)) != 0)))) {
					ox = wal.x - cposx;
					oy = wal.y - cposy;
					x1 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y1 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

					wal2 = wall[wal.point2];
					ox = wal2.x - cposx;
					oy = wal2.y - cposy;
					x2 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y2 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

					drawline256(x1, y1, x2, y2, 24);
				}
			}
		}

		// Draw sprites
		for (i = 0; i < numsectors; i++) {
			if ((show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			for (j = headspritesect[i]; j >= 0; j = nextspritesect[j]) {

			}

		}

		// Draw white lines
		for (i = 0; i < numsectors; i++) {

			if ((show2dsector[i >> 3] & (1 << (i & 7))) == 0)
				continue;

			startwall = sector[i].wallptr;
			endwall = sector[i].wallptr + sector[i].wallnum;

			if (startwall < 0 || endwall < 0)
				continue;

			k = -1;
			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if (wal == null)
					continue;
				if (wal.nextwall >= 0)
					continue;
				if (tilesizx[wal.picnum] == 0)
					continue;
				if (tilesizy[wal.picnum] == 0)
					continue;

				if (j == k) {
					x1 = x2;
					y1 = y2;
				} else {
					ox = wal.x - cposx;
					oy = wal.y - cposy;
					x1 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
					y1 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);
				}

				k = wal.point2;
				wal2 = wall[k];
				if (wal2 == null)
					continue;

				ox = wal2.x - cposx;
				oy = wal2.y - cposy;
				x2 = dmulscale(ox, xvect, -oy, yvect, 16) + (xdim << 11);
				y2 = dmulscale(oy, xvect2, ox, yvect2, 16) + (ydim << 11);

				drawline256(x1, y1, x2, y2, 24);
			}
		}
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		int oxpos = xpos;
		
		Gdx.gl.glDisable(GL_ALPHA_TEST);
		Gdx.gl.glDepthMask(false); // disable writing to the z-buffer
		Gdx.gl.glEnable(GL_BLEND);

		bindBatch();
		
		int xsiz = (fontsize != 0 ? 4 : 8);
		int ysiz = (fontsize != 0 ? 6 : 8);
		
		xpos <<= 16;
		ypos <<= 16;

		int c = 0, line = 0;
		int x, y, yoffs;
		while (c < text.length && text[c] != '\0') {
			if (text[c] == '\n') {
				text[c] = 0;
				line += 1;
				xpos = oxpos - (int) (scale * (8 >> fontsize));
			}
			if (text[c] == '\r')
				text[c] = 0;
	
			x = xpos + (int) (8 >> fontsize);
			y = ypos + (int) (fontsize != 0 ? 6 : 8);
			
			yoffs = (int) (scale * line * (8 >> fontsize));
	
			batch.setColor((curpalette[3 * col] & 0xFF) / 255.0f, (curpalette[3 * col + 1] & 0xFF) / 255.0f, (curpalette[3 * col + 2] & 0xFF) / 255.0f, 1.0f);
			batch.draw(getFont(), x, y, xsiz, ysiz, 0, -yoffs, (text[c] % 32) * xsiz, (text[c] / 32) * ysiz, xsiz, ysiz, 0, 65536, 8, 0, 0, xdim - 1, ydim - 1);

			xpos += xsiz << 16;
			c++;
		}
		Gdx.gl.glDepthMask(true); // re-enable writing to the z-buffer
	}

	@Override
	public void gltexinvalidateall(int flags) {
		if ((flags & 1) == 1)
			textureCache.uninit();
		if ((flags & 2) == 0)
			gltexinvalidateall();
	}
	
	public void gltexinvalidateall() {
		textureCache.invalidateall();
		textureCache.changePalette(curpalette);
	}

	@Override
	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth) {
		textureCache.invalidate(dapicnum, dapalnum, textureCache.clampingMode(dameth));
	}

	@Override
	public ByteBuffer getframebuffer(int x, int y, int w, int h, int format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		col = palookup[0][col] & 0xFF;
		
		shape.begin(ShapeType.Line);
		shape.setColor(curpalette[3 * col] & 0xFF, curpalette[3 * col + 1] & 0xFF, curpalette[3 * col + 2] & 0xFF, 255);
		shape.line(x1 / 4096.0f, ydim - y1 / 4096.0f, x2 / 4096.0f, ydim - y2 / 4096.0f);
		shape.end();
	}

	@Override
	public void settiltang(int tilt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setdrunk(float intensive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getdrunk() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addSpriteCorr(int snum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSpriteCorr(int snum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefs(DefScript defs) {
		this.textureCache.setTextureInfo(defs != null ? defs.texInfo : null);
		if(this.defs != null)
			gltexinvalidateall();
		this.defs = defs;
	}

}
