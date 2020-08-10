// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Render.GdxRender;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.palookup;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLRenderer;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.TileFont;

public class GdxRenderer implements GLRenderer {

	protected final TextureManager textureCache;
	protected final GdxOrphoRen orphoRen;
	protected GLTile textAtlas;
	protected DefScript defs;
	protected final Engine engine;

	public GdxRenderer(Engine engine) {
		this.engine = engine;

		BuildGdx.app.setFrame(FrameType.GL);
		this.textureCache = newTextureManager(engine);

		this.orphoRen = new GdxOrphoRen(engine, textureCache);
	}

	@Override
	public void enableShader(boolean enable) {
		textureCache.enableShader(enable);
	}

	@Override
	public void init() {
		orphoRen.init();
	}

	@Override
	public void uninit() {
		orphoRen.uninit();
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
		BuildGdx.gl.glClearColor(
				curpalette.getRed(dacol) / 255.0f,
				curpalette.getGreen(dacol) / 255.0f,
				curpalette.getBlue(dacol) / 255.0f,
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
		// dapicnum and dapalnum are like you'd expect
		// datype is 0 for a wall/floor/ceiling and 1 for a sprite
		// basically this just means walls are repeating
		// while sprites are clamped

		if ((palookup[dapalnum] == null)
				&& (dapalnum < (MAXPALOOKUPS - RESERVEDPALS)))
			return;

		textureCache.bind(dapicnum, dapalnum, 0, 0, (datype & 1) << 2);
	}

	@Override
	public void nextpage() {
		orphoRen.nextpage();
	}

	@Override
	public void gltexapplyprops() {
		GLFilter filter = GLSettings.textureFilter.get();
		textureCache.setFilter(filter);

		if(defs == null)
			return;

		int anisotropy = GLSettings.textureAnisotropy.get();
		for (int i=MAXTILES-1; i>=0; i--) {
			Model m = defs.mdInfo.getModel(i);
	        if(m != null) {
	        	Iterator<GLTile[]> it = m.getSkins();
	        	while(it.hasNext()) {
	        		for (GLTile tex : it.next()) {
	    				if (tex == null)
	    					continue;

	    				textureCache.bind(tex);
	    				tex.setupTextureFilter(filter, anisotropy);
	    			}
	        	}
	        }
	    }
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {
		orphoRen.rotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2);
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		orphoRen.drawmapview(dax, day, zoome, ang);
	}

	@Override
	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		orphoRen.drawoverheadmap(cposx, cposy, czoom, cang);
	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		orphoRen.printext(font, xpos, ypos, text, col, shade, bit, scale);
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		orphoRen.printext(xpos, ypos, col, backcol, text, fontsize, scale);
	}

	@Override
	public void gltexinvalidateall(GLInvalidateFlag... flags) {
		for(int i = 0; i < flags.length; i++) {
			switch(flags[i])
			{
			case Uninit:
				textureCache.uninit();
				Console.Println("TextureCache uninited!", Console.OSDTEXT_RED);
				break;
			case SkinsOnly:
				break;
			case TexturesOnly:
			case IndexedTexturesOnly:
				break;
			case All:
				gltexinvalidateall();
				break;
			}
		}
	}

	public void gltexinvalidateall() {
		textureCache.invalidateall();
	}

	@Override
	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth) {
		textureCache.invalidate(dapicnum, dapalnum, textureCache.clampingMode(dameth));
	}

	@Override
	public ByteBuffer getFrame(PixelFormat format, int xsiz, int ysiz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		orphoRen.drawline256(x1, y1, x2, y2, col);
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

	@Override
	public RenderType getType() {
		return null;
	}

	@Override
	public void changepalette(byte[] palette) {
		// TODO Auto-generated method stub

	}

	@Override
	public void completemirror() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInited() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PixelFormat getTexFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	public TextureManager newTextureManager(Engine engine) {
		return new TextureManager(engine);
	}

	@Override
	public TextureManager getTextureManager() {
		if(textureCache == null)
			return newTextureManager(engine);
		return textureCache;
	}
}
