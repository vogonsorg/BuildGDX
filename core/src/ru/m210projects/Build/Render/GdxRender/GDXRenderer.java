// This file is part of BuildGDX.
// Copyright (C) 2017-2021  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

import static com.badlogic.gdx.graphics.GL20.GL_BACK;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_CW;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_TEST;
import static com.badlogic.gdx.graphics.GL20.GL_FRONT;
import static com.badlogic.gdx.graphics.GL20.GL_LESS;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_PACK_ALIGNMENT;
import static com.badlogic.gdx.graphics.GL20.GL_RGB;
import static com.badlogic.gdx.graphics.GL20.GL_RGBA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static com.badlogic.gdx.graphics.GL20.GL_VERSION;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.Pragmas.divscale;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.mulscale;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Architecture.BuildApplication.Platform;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.GLRenderer;
import ru.m210projects.Build.Render.IOverheadMapSettings;
import ru.m210projects.Build.Render.GdxRender.WorldMesh.GLSurface;
import ru.m210projects.Build.Render.GdxRender.WorldMesh.Heinum;
import ru.m210projects.Build.Render.GdxRender.Scanner.SectorScanner;
import ru.m210projects.Build.Render.GdxRender.Scanner.VisibleSector;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager.Shader;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.GLTileArray;
import ru.m210projects.Build.Render.TextureHandle.IndexedShader;
import ru.m210projects.Build.Render.TextureHandle.IndexedTileData;
import ru.m210projects.Build.Render.TextureHandle.RGBTileData;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.TextureHandle.TextureManager.ExpandTexture;
import ru.m210projects.Build.Render.TextureHandle.TileAtlas;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.FadeEffect.FadeShader;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.Tile.AnimType;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.WALL;

public class GDXRenderer implements GLRenderer {

//	TODO:
//	SpriteRenderer common matrix4 method
//	Scansectors memory leak (WallFrustum)
//	Maskwall sorts
//	Gamma doesn't work

//	Hires + models
//  Blood E1M1 floor sprite invisible
//	Blood drunk effect
//	Skyboxes
//  Duke E2L7 wall vis bug (scanner bug)
//  Duke E4L11 wall vis bug (scanner bug)
//  RGB shader fog

	public Rendering rendering = Rendering.Nothing;

	protected TextureManager textureCache;
	protected final Engine engine;
	protected boolean isInited = false;
	protected GL20 gl;
	protected float defznear = 0.001f;
	protected float defzfar = 1.0f;
	protected float fov = 90;

	protected float gtang = 0.0f;

	protected WorldMesh world;
	protected SectorScanner scanner;
	protected BuildCamera cam;
	protected SpriteRenderer sprR;
	protected GDXOrtho orphoRen; // GdxOrphoRen
	protected DefScript defs;

	protected ShaderManager manager;
	protected boolean isUseIndexedTextures;

	private ByteBuffer pix32buffer;
	private ByteBuffer pix8buffer;
	protected Matrix4 transform = new Matrix4();
	protected Matrix4 identity = new Matrix4();

	private boolean clearStatus = false;
	private float glox1, gloy1, glox2, gloy2;
	private boolean drunk;
	private float drunkIntensive = 1.0f;

	private GLTile frameTexture;
	private int framew;
	private int frameh;

	protected ArrayList<VisibleSector> sectors = new ArrayList<VisibleSector>();
	private ArrayList<GLSurface> bunchfirst = new ArrayList<GLSurface>();
	protected boolean[] mirrorTextures = new boolean[MAXTILES];

	public GDXRenderer(Engine engine, IOverheadMapSettings settings) {
		if (BuildGdx.graphics.getFrameType() != FrameType.GL)
			BuildGdx.app.setFrame(FrameType.GL);

		this.engine = engine;
		this.textureCache = getTextureManager();
		this.manager = new ShaderManager();

		this.sprR = new SpriteRenderer(engine, this);
		this.orphoRen = allocOrphoRenderer(settings);
		this.scanner = new SectorScanner(engine) {
			@Override
			protected Matrix4 getSpriteMatrix(SPRITE tspr) {
				return sprR.getMatrix(tspr, 1, 1);
			}
		};

		Arrays.fill(mirrorTextures, false);
		int[] mirrors = getMirrorTextures();
		if (mirrors != null) {
			for (int i = 0; i < mirrors.length; i++)
				mirrorTextures[mirrors[i]] = true;
		}

		System.err.println("create");
	}

	@Override
	public void init() {
		GLInfo.init();
		this.gl = BuildGdx.graphics.getGL20();

		enableIndexedShader(GLSettings.usePaletteShader.get());

		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		this.cam = new BuildCamera(fov, xdim, ydim, 512, 8192);
		this.manager.init(textureCache);
		this.textureCache.changePalette(curpalette.getBytes());

		Console.Println("Polygdx renderer is initialized", OSDTEXT_GOLD);
		Console.Println(BuildGdx.graphics.getGLVersion().getRendererString() + " " + gl.glGetString(GL_VERSION),
				OSDTEXT_GOLD);

		orphoRen.init();

		if (world != null && world.isInvalid()) {
			world = new WorldMesh(engine);
		}

		System.err.println("init");
		isInited = true;
	}

	@Override
	public void uninit() {
		System.err.println("uninit");
		isInited = false;
		if (world != null)
			world.dispose();
		orphoRen.uninit();
		manager.dispose();
		FadeEffect.uninit();
		texturesUninit();
	}

	private void texturesUninit() {
		textureCache.uninit();
		for (int i = MAXTILES - 1; i >= 0; i--) {
			skycache.dispose(i);
		}
	}

	@Override
	public void drawrooms() {
		if (orphoRen.isDrawing())
			orphoRen.end();

		// Temporaly code (Tekwar issue)
		else if (!clearStatus) { // once at frame
			gl.glClearColor(0.0f, 0.5f, 0.5f, 1);
			gl.glClear(GL_COLOR_BUFFER_BIT);
			clearStatus = true;
		}
		gl.glClear(GL_DEPTH_BUFFER_BIT);

//		if (shape == null) {
//			shape = new ShapeRenderer();
//			shape.setProjectionMatrix(shape.getProjectionMatrix().setToOrtho(0, xdim, ydim, 0, -1, 1));
//		}
//		shape.begin(ShapeType.Line);

		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glDepthFunc(GL_LESS);
		gl.glDepthRangef(defznear, defzfar);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		resizeglcheck();

		cam.setPosition(globalposx, globalposy, globalposz);
		cam.setDirection(globalang, globalhoriz, gtang);
		cam.update(true);

		globalvisibility = visibility << 2;
		if (globalcursectnum >= MAXSECTORS) {
			globalcursectnum -= MAXSECTORS;
		} else {
			short i = globalcursectnum;
			globalcursectnum = engine.updatesectorz(globalposx, globalposy, globalposz, globalcursectnum);
			if (globalcursectnum < 0)
				globalcursectnum = i;
		}

		sectors.clear();
		scanner.clear();
		scanner.process(sectors, cam, world, globalcursectnum);

		rendering = Rendering.Nothing;
		if (inpreparemirror)
			gl.glCullFace(GL_FRONT);
		else
			gl.glCullFace(GL_BACK);

		prerender(sectors);
		drawbackground();
		for (int i = inpreparemirror ? 1 : 0; i < sectors.size(); i++) {
			drawSector(sectors.get(i));
		}

		spritesortcnt = scanner.getSpriteCount();
		tsprite = scanner.getSprites();

		manager.unbind();
	}

	@Override
	public void drawmasks() {
		int maskwallcnt = scanner.getMaskwallCount();
		sprR.sort(scanner.getSprites(), spritesortcnt);

		while ((spritesortcnt > 0) && (maskwallcnt > 0)) { // While BOTH > 0
			int j = scanner.getMaskwalls()[maskwallcnt - 1];
			if (!spritewallfront(scanner.getSprites()[spritesortcnt - 1], j))
				drawsprite(--spritesortcnt);
			else {
				// Check to see if any sprites behind the masked wall...
				for (int i = spritesortcnt - 2; i >= 0; i--) {
					if (!spritewallfront(scanner.getSprites()[i], j)) {
						drawsprite(i);
						scanner.getSprites()[i] = null;
					}
				}
				// finally safe to draw the masked wall
				drawmaskwall(--maskwallcnt);
			}
		}

		while (spritesortcnt != 0) {
			spritesortcnt--;
			if (scanner.getSprites()[spritesortcnt] != null) {
				drawsprite(spritesortcnt);
			}
		}

		while (maskwallcnt > 0)
			drawmaskwall(--maskwallcnt);

		renderDrunkEffect();
		manager.unbind();
	}

	private void drawMask(int w) {
		gl.glDepthFunc(GL20.GL_LESS);
		gl.glDepthRangef(0.0001f, 0.99999f);

		drawSurf(world.getMaskedWall(w), 0, null, null);

		gl.glDepthFunc(GL20.GL_LESS);
		gl.glDepthRangef(defznear, defzfar);
	}

	protected void renderDrunkEffect() { // TODO: to shader
		/*
		 * if (drunk) { set2dview();
		 * 
		 * gl.glActiveTexture(GL_TEXTURE0); boolean hasShader = texshader != null &&
		 * texshader.isBinded(); if (hasShader) texshader.end();
		 * 
		 * if (frameTexture == null || framew != xdim || frameh != ydim) { int size = 1;
		 * for (size = 1; size < Math.max(xdim, ydim); size <<= 1) ;
		 * 
		 * if (frameTexture != null) frameTexture.dispose(); else frameTexture = new
		 * GLTile(PixelFormat.Rgb, size, size);
		 * 
		 * frameTexture.bind(); gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB,
		 * frameTexture.getWidth(), frameTexture.getHeight(), 0, GL_RGB,
		 * GL_UNSIGNED_BYTE, null); frameTexture.unsafeSetFilter(TextureFilter.Linear,
		 * TextureFilter.Linear); framew = xdim; frameh = ydim; }
		 * 
		 * textureCache.bind(frameTexture); gl.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0,
		 * 0, 0, 0, frameTexture.getWidth(), frameTexture.getHeight());
		 * 
		 * gl.glDisable(GL_DEPTH_TEST); gl.glDisable(GL_CULL_FACE);
		 * 
		 * float tiltang = (drunkIntensive * 360) / 2048f; float tilt = min(max(tiltang,
		 * -MAXDRUNKANGLE), MAXDRUNKANGLE); float u = (float) xdim /
		 * frameTexture.getWidth(); float v = (float) ydim / frameTexture.getHeight();
		 * 
		 * int originX = xdim / 2; int originY = ydim / 2; float width = xdim * 1.05f;
		 * float height = ydim * 1.05f;
		 * 
		 * float xoffs = width / 2; float yoffs = height / 2;
		 * 
		 * final float rotation = 360.0f * tiltang / 2048.0f; final float cos =
		 * MathUtils.cosDeg(rotation); final float sin = MathUtils.sinDeg(rotation);
		 * 
		 * float x1 = originX + (sin * yoffs - cos * xoffs); float y1 = originY - xoffs
		 * * sin - yoffs * cos;
		 * 
		 * float x4 = x1 + width * cos; float y4 = y1 + width * sin;
		 * 
		 * float x2 = x1 - height * sin; float y2 = y1 + height * cos;
		 * 
		 * float x3 = x2 + (x4 - x1); float y3 = y2 + (y4 - y1);
		 * 
		 * orphoRen.begin(); // XXX // orphoRen.setColor(1, 1, 1, abs(tilt) / (2 *
		 * MAXDRUNKANGLE)); // orphoRen.setTexture(frameTexture); //
		 * orphoRen.addVertex(x1, ydim - y1, 0, 0); // orphoRen.addVertex(x2, ydim - y2,
		 * 0, v); // orphoRen.addVertex(x3, ydim - y3, u, v); // orphoRen.addVertex(x4,
		 * ydim - y4, u, 0); orphoRen.end();
		 * 
		 * gl.glEnable(GL_DEPTH_TEST); gl.glEnable(GL_CULL_FACE);
		 * 
		 * if (hasShader) texshader.begin(); }
		 */
	}

	public void drawsprite(int i) {
		sprR.begin(cam);
		SPRITE tspr = scanner.getSprites()[i];
		if (tspr != null) {
			rendering = Rendering.Sprite.setIndex(i);
			sprR.draw(tspr);
		}
		sprR.end();
	}

	private void drawmaskwall(int i) {
		rendering = Rendering.MaskWall.setIndex(i);
		drawMask(scanner.getMaskwalls()[i]);
	}

	protected void drawbackground() {
		rendering = Rendering.Skybox;
		drawSkyPlanes();
		for (int i = inpreparemirror ? 1 : 0; i < sectors.size(); i++)
			drawSkySector(sectors.get(i));

	}

	private void prerender(ArrayList<VisibleSector> sectors) {
		if (inpreparemirror)
			return;

		bunchfirst.clear();

		for (int i = 0; i < sectors.size(); i++) {
			VisibleSector sec = sectors.get(i);

			int sectnum = sec.index;
			if ((sec.secflags & 1) != 0)
				checkMirror(world.getFloor(sectnum));

			if ((sec.secflags & 2) != 0)
				checkMirror(world.getCeiling(sectnum));

			for (int w = 0; w < sec.walls.size; w++) {
				int z = sec.walls.get(w);
				int flags = sec.wallflags.get(w);

				checkMirror(world.getWall(z, sectnum));
				if ((flags & 1) != 0)
					checkMirror(world.getLower(z, sectnum));
				if ((flags & 2) != 0)
					checkMirror(world.getUpper(z, sectnum));
				checkMirror(world.getMaskedWall(z));
			}

			for (int w = 0; w < sec.skywalls.size; w++) {
				int z = sec.skywalls.get(w);
				checkMirror(world.getParallaxCeiling(z));
				checkMirror(world.getParallaxFloor(z));
			}
		}

		for (int i = 0; i < bunchfirst.size(); i++) {
			drawSurf(bunchfirst.get(i), 0, null, null);
		}
	}

	private void checkMirror(GLSurface surf) {
		if (surf == null)
			return;

		int picnum = surf.picnum;
		if (mirrorTextures[picnum]) {
			bunchfirst.add(surf);
		}
	}

	private void drawSkyPlanes() {
		gl.glDisable(GL_CULL_FACE);
		gl.glDepthMask(false);

		SECTOR skysector;
		if ((skysector = scanner.getLastSkySector(Heinum.SkyUpper)) != null) {
			int pal = skysector.ceilingpal;
			int shade = skysector.ceilingshade;
			int picnum = skysector.ceilingpicnum;

			drawSky(world.getQuad(), picnum, shade, pal, 0,
					transform.setToTranslation(cam.position.x, cam.position.y, cam.position.z - 100).scale(cam.far,
							cam.far, 1.0f));
		}

		if ((skysector = scanner.getLastSkySector(Heinum.SkyLower)) != null) {
			int pal = skysector.floorpal;
			int shade = skysector.floorshade;
			int picnum = skysector.floorpicnum;

			drawSky(world.getQuad(), picnum, shade, pal, 0,
					transform.setToTranslation(cam.position.x, cam.position.y, cam.position.z + 100).scale(cam.far,
							cam.far, 1.0f));
		}

		gl.glDepthMask(true);
		gl.glEnable(GL_CULL_FACE);
	}

	private void drawSector(VisibleSector sec) {
		int sectnum = sec.index;
		gotsector[sectnum >> 3] |= pow2char[sectnum & 7];

		if ((sec.secflags & 1) != 0) {
			rendering = Rendering.Floor.setIndex(sectnum);
			drawSurf(world.getFloor(sectnum), 0, null, sec.clipPlane);
		}

		if ((sec.secflags & 2) != 0) {
			rendering = Rendering.Ceiling.setIndex(sectnum);
			drawSurf(world.getCeiling(sectnum), 0, null, sec.clipPlane);
		}

		for (int w = 0; w < sec.walls.size; w++) {
			int flags = sec.wallflags.get(w);
			int z = sec.walls.get(w);
			rendering = Rendering.Wall.setIndex(z);
			drawSurf(world.getWall(z, sectnum), flags, null, sec.clipPlane);
			drawSurf(world.getUpper(z, sectnum), flags, null, sec.clipPlane);
			drawSurf(world.getLower(z, sectnum), flags, null, sec.clipPlane);
		}
	}

	public void drawSkySector(VisibleSector sec) {
		for (int w = 0; w < sec.skywalls.size; w++) {
			int z = sec.skywalls.get(w);
			GLSurface ceil = world.getParallaxCeiling(z);
			if (ceil != null) {
				drawSky(ceil, ceil.picnum, ceil.getShade(), ceil.getPal(), ceil.getMethod(), identity);
			}

			GLSurface floor = world.getParallaxFloor(z);
			if (floor != null) {
				drawSky(floor, floor.picnum, floor.getShade(), floor.getPal(), floor.getMethod(), identity);
			}
		}
	}

	private void drawSky(GLSurface surf, int picnum, int shade, int palnum, int method, Matrix4 worldTransform) {
		if (surf.count == 0)
			return;

		if (engine.getTile(picnum).getType() != AnimType.None)
			picnum += engine.animateoffs(picnum, 0);

		Tile pic = engine.getTile(picnum);
		if (!pic.isLoaded())
			engine.loadtile(picnum);

		if (!pic.isLoaded())
			method = 1; // invalid data, HOM

		engine.setgotpic(picnum);
		if (palookup[palnum] == null)
			palnum = 0;

		GLTile pth = bindSky(picnum, palnum, shade, method);
		if (pth != null) {
			Gdx.gl.glDisable(GL_BLEND);
			if ((method & 3) != 0)
				Gdx.gl.glEnable(GL_BLEND);

			manager.transform(worldTransform);
			manager.frustum(null);

			surf.render(manager.getProgram());
		}
	}

	protected void drawSurf(GLSurface surf, int flags, Matrix4 worldTransform, Plane[] clipPlane) {
		if (surf == null)
			return;

		if (surf.count != 0 && (flags == 0 || (surf.visflag & flags) != 0)) {
			int picnum = surf.picnum;

			if (engine.getTile(picnum).getType() != AnimType.None)
				picnum += engine.animateoffs(picnum, 0);

			Tile pic = engine.getTile(picnum);
			if (!pic.isLoaded())
				engine.loadtile(picnum);

			int method = surf.getMethod();
			if (!pic.isLoaded()) {
				method = 1; // invalid data, HOM
			}

			engine.setgotpic(picnum);
			GLTile pth = bind(picnum, surf.getPal(), surf.getShade(), 0, method);
			if (pth != null) {
				int combvis = globalvisibility;
				int vis = surf.getVisibility();
				if (vis != 0)
					combvis = mulscale(globalvisibility, (vis + 16) & 0xFF, 4);

				if (pth.getPixelFormat() == PixelFormat.Pal8)
					// TODO: set FOG ?
					((IndexedShader) manager.getProgram()).setVisibility((int) (-combvis / 64.0f));

				if (worldTransform == null)
					manager.transform(identity);
				else
					manager.transform(worldTransform);

				if (clipPlane != null && !inpreparemirror)
					manager.frustum(clipPlane);
				else
					manager.frustum(null);

				if ((method & 3) == 0)
					Gdx.gl.glDisable(GL_BLEND);
				else
					Gdx.gl.glEnable(GL_BLEND);

				surf.render(manager.getProgram());
			}
		}
	}

	@Override
	public void clearview(int dacol) {
		gl.glClearColor(curpalette.getRed(dacol) / 255.0f, //
				curpalette.getGreen(dacol) / 255.0f, //
				curpalette.getBlue(dacol) / 255.0f, 0); //
		gl.glClear(GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void changepalette(byte[] palette) {
		textureCache.changePalette(palette);
	}

	@Override
	public void nextpage() {
		clearStatus = false;
		if (world != null)
			world.nextpage();
		orphoRen.nextpage();
		manager.unbind();
		textureCache.unbind();
		beforedrawrooms = 1;

//		if (shape != null)
//			shape.end();
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {
		rendering = Rendering.Tile.setIndex(picnum);
		set2dview();
		orphoRen.rotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2);
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		set2dview();
		orphoRen.drawmapview(dax, day, zoome, ang);
	}

	@Override
	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		set2dview();
		orphoRen.drawoverheadmap(cposx, cposy, czoom, cang);
	}

	@Override
	public void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit,
			float scale) {
		rendering = Rendering.Tile.setIndex(0);
		set2dview();
		orphoRen.printext(font, xpos, ypos, text, col, shade, bit, scale);
	}

	@Override
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale) {
		rendering = Rendering.Tile.setIndex(0);
		set2dview();
		orphoRen.printext(xpos, ypos, col, backcol, text, fontsize, scale);
	}

	@Override
	public ByteBuffer getFrame(PixelFormat format, int xsiz, int ysiz) {
		if (pix32buffer != null)
			pix32buffer.clear();

		boolean reverse = false;
		if (ysiz < 0) {
			ysiz *= -1;
			reverse = true;
		}

		int byteperpixel = 3;
		int fmt = GL_RGB;
		if (BuildGdx.app.getPlatform() == Platform.Android) {
			byteperpixel = 4;
			fmt = GL_RGBA;
		}

		if (pix32buffer == null || pix32buffer.capacity() < xsiz * ysiz * byteperpixel)
			pix32buffer = BufferUtils.newByteBuffer(xsiz * ysiz * byteperpixel);
		gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);
		gl.glReadPixels(0, ydim - ysiz, xsiz, ysiz, fmt, GL_UNSIGNED_BYTE, pix32buffer);

		if (format == PixelFormat.Rgb) {
			if (reverse) {
				int b1, b2 = 0;
				for (int p, x, y = 0; y < ysiz / 2; y++) {
					b1 = byteperpixel * (ysiz - y - 1) * xsiz;
					for (x = 0; x < xsiz; x++) {
						for (p = 0; p < byteperpixel; p++) {
							byte tmp = pix32buffer.get(b1 + p);
							pix32buffer.put(b1 + p, pix32buffer.get(b2 + p));
							pix32buffer.put(b2 + p, tmp);
						}
						b1 += byteperpixel;
						b2 += byteperpixel;
					}
				}
			}
			pix32buffer.rewind();
			return pix32buffer;
		} else if (format == PixelFormat.Pal8) {
			if (pix8buffer != null)
				pix8buffer.clear();
			if (pix8buffer == null || pix8buffer.capacity() < xsiz * ysiz)
				pix8buffer = BufferUtils.newByteBuffer(xsiz * ysiz);

			int base = 0, r, g, b;
			if (reverse) {
				for (int x, y = 0; y < ysiz; y++) {
					base = byteperpixel * (ysiz - y - 1) * xsiz;
					for (x = 0; x < xsiz; x++) {
						r = (pix32buffer.get(base++) & 0xFF) >> 2;
						g = (pix32buffer.get(base++) & 0xFF) >> 2;
						b = (pix32buffer.get(base++) & 0xFF) >> 2;
						pix8buffer.put(engine.getclosestcol(palette, r, g, b));
					}
				}
			} else {
				for (int i = 0; i < pix8buffer.capacity(); i++) {
					r = (pix32buffer.get(base++) & 0xFF) >> 2;
					g = (pix32buffer.get(base++) & 0xFF) >> 2;
					b = (pix32buffer.get(base++) & 0xFF) >> 2;
					if (byteperpixel == 4)
						base++; // Android
					pix8buffer.put(engine.getclosestcol(palette, r, g, b));
				}
			}

			pix8buffer.rewind();
			return pix8buffer;
		}

		return null;
	}

	@Override
	public byte[] screencapture(int newwidth, int newheight) {
		byte[] capture = new byte[newwidth * newheight];

		int xf = divscale(xdim, newwidth, 16);
		int yf = divscale(ydim, newheight, 16);

		ByteBuffer frame = getFrame(PixelFormat.Rgb, xdim, -ydim);

		int byteperpixel = 3;
		if (BuildGdx.app.getType() == ApplicationType.Android)
			byteperpixel = 4;

		int base;
		for (int fx, fy = 0; fy < newheight; fy++) {
			base = mulscale(fy, yf, 16) * xdim;
			for (fx = 0; fx < newwidth; fx++) {
				int pos = base + mulscale(fx, xf, 16);
				frame.position(byteperpixel * pos);
				int r = (frame.get() & 0xFF) >> 2;
				int g = (frame.get() & 0xFF) >> 2;
				int b = (frame.get() & 0xFF) >> 2;

				capture[newheight * fx + fy] = engine.getclosestcol(palette, r, g, b);
			}
		}

		return capture;
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		set2dview();
		orphoRen.drawline256(x1, y1, x2, y2, col);
	}

	@Override
	public void settiltang(int tilt) {
		if (tilt == 0)
			gtang = 0.0f;
		else
			gtang = (float) Gameutils.AngleToDegrees(tilt);
	}

	@Override
	public void setDefs(DefScript defs) {
		this.textureCache.setTextureInfo(defs != null ? defs.texInfo : null);
		if (this.defs != null)
			gltexinvalidateall(GLInvalidateFlag.Uninit, GLInvalidateFlag.All);
		this.defs = defs;
	}

	@Override
	public TextureManager getTextureManager() {
		if (textureCache == null)
			textureCache = new TextureManager(engine, ExpandTexture.Vertical);
		return textureCache;
	}

	@Override
	public void enableIndexedShader(boolean enable) {
		if (isUseIndexedTextures != enable) {
			if (isInited)
				texturesUninit();

//			clearskins(false); XXX
			this.isUseIndexedTextures = enable;
		}
	}

	@Override
	public void palfade(HashMap<String, FadeEffect> fades) { // TODO: to shader?
		if (orphoRen.isDrawing())
			orphoRen.end();

		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_TEXTURE_2D);

		gl.glEnable(GL_BLEND);

		set2dview();

		FadeShader shader = (FadeShader) manager.bind(Shader.FadeShader);

		palfadergb.draw(shader);
		if (fades != null) {
			Iterator<FadeEffect> it = fades.values().iterator();
			while (it.hasNext()) {
				FadeEffect obj = it.next();
				obj.draw(shader);
			}
		}

		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void preload() {
		if (world != null)
			world.dispose();
		world = new WorldMesh(engine);
		scanner.init();

		for (int i = 0; i < MAXSPRITES; i++) {
			removeSpriteCorr(i);
			SPRITE spr = sprite[i];
			if (spr == null || ((spr.cstat >> 4) & 3) != 1 || spr.statnum == MAXSTATUS)
				continue;

			addSpriteCorr(i);
		}
	}

	@Override
	public void precache(int dapicnum, int dapalnum, int datype) {
		if ((palookup[dapalnum] == null) && (dapalnum < (MAXPALOOKUPS - RESERVEDPALS)))
			return;

		textureCache.precache(getTexFormat(), dapicnum, dapalnum, datype);
	}

	@Override
	public void gltexapplyprops() {
		GLFilter filter = GLSettings.textureFilter.get();
		textureCache.setFilter(filter);
		int anisotropy = GLSettings.textureAnisotropy.get();
		for (int i = MAXTILES - 1; i >= 0; i--) {
			skycache.setFilter(i, filter, anisotropy);
		}

		if (defs == null)
			return;

		for (int i = MAXTILES - 1; i >= 0; i--) {
			Model m = defs.mdInfo.getModel(i);
			if (m != null) {
				Iterator<GLTile[]> it = m.getSkins();
				while (it.hasNext()) {
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
	public void gltexinvalidateall(GLInvalidateFlag... flags) {
		if (flags.length == 0) {
			textureCache.invalidateall();
//			clearskins(true); XXX
			return;
		}

		for (int i = 0; i < flags.length; i++) {
			switch (flags[i]) {
			case Uninit:
				texturesUninit();
				break;
			case SkinsOnly:
//				clearskins(true); XXX
				break;
			case TexturesOnly:
			case IndexedTexturesOnly:
				textureCache.invalidateall();
				break;
			case Palookup:
				for (int j = 0; j < MAXPALOOKUPS; j++) {
					textureCache.invalidatepalookup(j);
				}
				break;
			case All:
				textureCache.invalidateall();
				break;
			}
		}
	}

	//
	// invalidatetile
	// pal: pass -1 to invalidate all palettes for the tile, or >=0 for a particular
	// palette
	// how: pass -1 to invalidate all instances of the tile in texture memory, or a
	// bitfield
	// bit 0: opaque or masked (non-translucent) texture, using repeating
	// bit 1: ignored
	// bit 2: ignored (33% translucence, using repeating)
	// bit 3: ignored (67% translucence, using repeating)
	// bit 4: opaque or masked (non-translucent) texture, using clamping
	// bit 5: ignored
	// bit 6: ignored (33% translucence, using clamping)
	// bit 7: ignored (67% translucence, using clamping)
	// clamping is for sprites, repeating is for walls
	//

	@Override
	public void invalidatetile(int tilenume, int pal, int how) { // jfBuild
		int numpal, firstpal, np;
		int hp;

		PixelFormat fmt = textureCache.getFmt(tilenume);
		if (fmt == null)
			return;

		if (fmt == PixelFormat.Pal8) {
			numpal = 1;
			firstpal = 0;
		} else {
			if (pal < 0) {
				numpal = MAXPALOOKUPS;
				firstpal = 0;
			} else {
				numpal = 1;
				firstpal = pal % MAXPALOOKUPS;
			}
		}

		for (hp = 0; hp < 8; hp += 4) {
			if ((how & pow2long[hp]) == 0)
				continue;

			for (np = firstpal; np < firstpal + numpal; np++) {
				textureCache.invalidate(tilenume, np, textureCache.clampingMode(hp));
			}
		}
	}

	@Override
	public void setdrunk(float intensive) {
		if (intensive == 0) {
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

	protected GLTile bind(int dapicnum, int dapalnum, int dashade, int skybox, int method) {
		if (palookup[dapalnum] == null)
			dapalnum = 0;

		GLTile pth = textureCache.get(getTexFormat(), dapicnum, dapalnum, skybox, method);
		if (pth == null)
			return null;

		textureCache.bind(pth);
		if (manager.getShader() == null || isSkyShader() || pth.getPixelFormat() != manager.getPixelFormat()) {
			switchShader(pth.getPixelFormat() != PixelFormat.Pal8 ? Shader.RGBWorldShader : Shader.IndexedWorldShader);
		}
		setTextureParameters(pth, dapicnum, dapalnum, dashade, skybox, method);

		return pth;
	}

	protected GLTile bindSky(int dapicnum, int dapalnum, int dashade, int method) {
		if (palookup[dapalnum] == null)
			dapalnum = 0;

		GLTile pth = getSkyTexture(getTexFormat(), dapicnum, dapalnum);
		if (pth == null)
			return null;

		textureCache.bind(pth);
		if (manager.getShader() == null || !isSkyShader() || pth.getPixelFormat() != manager.getPixelFormat()) {
			switchShader(pth.getPixelFormat() != PixelFormat.Pal8 ? Shader.RGBSkyShader : Shader.IndexedSkyShader);
		}
		setTextureParameters(pth, dapicnum, dapalnum, dashade, 0, 0);
		return pth;
	}

	public void setTextureParameters(GLTile tile, int tilenum, int pal, int shade, int skybox, int method) {
		if (tile.getPixelFormat() == TileData.PixelFormat.Pal8) {
			float alpha = 1.0f;
			switch (method & 3) {
			case 2:
				alpha = TRANSLUSCENT1;
				break;
			case 3:
				alpha = TRANSLUSCENT2;
				break;
			}

			if (!engine.getTile(tilenum).isLoaded())
				alpha = 0.01f; // Hack to update Z-buffer for invalid mirror textures

			manager.textureParams8(pal, shade, alpha, (method & 3) == 0 || !textureCache.alphaMode(method));
		} else {
			// XXX
		}
	}

	public void setFieldOfView(final float fov) {
		if (cam != null) {
			cam.setFieldOfView(fov);
		} else {
			BuildGdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					cam.setFieldOfView(fov);
				}
			});
		}
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
	public void completemirror() {
		inpreparemirror = false;
	}

	private boolean spritewallfront(SPRITE s, int w) {
		if (s == null)
			return false;

		WALL wal = wall[w];
		int x1 = wal.x;
		int y1 = wal.y;
		wal = wall[wal.point2];
		return (dmulscale(wal.x - x1, s.y - y1, -(s.x - x1), wal.y - y1, 32) >= 0);
	}

	protected void set2dview() {
		if (gloy1 != -1) {
			gl.glViewport(0, 0, xdim, ydim);
			orphoRen.resize(xdim, ydim);
		}
		gloy1 = -1;
	}

	protected void resizeglcheck() {
		if ((glox1 != windowx1) || (gloy1 != windowy1) || (glox2 != windowx2) || (gloy2 != windowy2)) {
			glox1 = windowx1;
			gloy1 = windowy1;
			glox2 = windowx2;
			gloy2 = windowy2;

			gl.glViewport(windowx1, ydim - (windowy2 + 1), windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);

			cam.viewportWidth = windowx2;
			cam.viewportHeight = windowy2;
		}
	}

	protected GDXOrtho allocOrphoRenderer(IOverheadMapSettings settings) {
		return new GDXOrtho(this, settings);
	}

	protected int[] getMirrorTextures() {
		return null;
	}

	@Override
	public RenderType getType() {
		return RenderType.PolyGDX;
	}

	@Override
	public PixelFormat getTexFormat() {
		return isUseIndexedTextures ? PixelFormat.Pal8 : PixelFormat.Rgba;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	protected void switchShader(Shader shader) {
		manager.bind(shader);
		manager.mirror(inpreparemirror);
		manager.prepare(cam);
	}

	protected final GLTileArray skycache = new GLTileArray(MAXTILES);

	protected GLTile getSkyTexture(PixelFormat fmt, int picnum, int palnum) {
		if (!engine.getTile(picnum).hasSize())
			return textureCache.get(getTexFormat(), picnum, palnum, 0, 0);

		GLTile tile = skycache.get(picnum, palnum, false, 0);
		boolean useMipMaps = GLSettings.textureFilter.get().mipmaps;

		if (tile != null /* && tile.getPixelFormat() == fmt */) {
			if (tile.isInvalidated()) {
				tile.setInvalidated(false);

				TileData data = loadPic(fmt, picnum, palnum);
				tile.update(data, useMipMaps);
			}
		} else {
//			if (tile != null)
//				skycache.dispose(picnum); // old texture

			TileData data = loadPic(fmt, picnum, palnum);
			if (data == null)
				return null;

			skycache.add(textureCache.newTile(data, fmt == PixelFormat.Pal8 ? 0 : palnum, useMipMaps), picnum);
		}

		return tile;
	}

	protected TileData loadPic(PixelFormat fmt, int picnum, int palnum) {
		short[] dapskyoff = zeropskyoff;
		int dapskybits = pskybits;

		if (dapskybits < 0)
			dapskybits = 0;

		Tile tile = engine.getTile(picnum);
		TileAtlas sky = new TileAtlas(fmt, tile.getWidth() * (1 << dapskybits), tile.getHeight(), tile.getWidth(),
				tile.getHeight(), false);
		for (int i = 0; i < (1 << dapskybits); i++) {
			int pic = dapskyoff[i] + picnum;
			TileData dat;
			if (fmt == PixelFormat.Pal8)
				dat = new IndexedTileData(engine.getTile(pic), false, false, 0);
			else
				dat = new RGBTileData(engine.getTile(pic), palnum, false, false, 0);
			sky.addTile(pic, dat);
		}

		return sky.atlas.get(0);
	}

	private boolean isSkyShader() {
		return manager.getShader() == Shader.RGBSkyShader || manager.getShader() == Shader.IndexedSkyShader;
	}

	// Debug 2.5D renderer

//	private boolean WallFacingCheck(WALL wal) {
//		float x1 = wal.x - globalposx;
//		float y1 = wal.y - globalposy;
//		float x2 = wall[wal.point2].x - globalposx;
//		float y2 = wall[wal.point2].y - globalposy;
//
//		return (x1 * y2 - y1 * x2) >= 0;
//	}
//
//	private boolean NearPlaneCheck(BuildCamera cam, ArrayList<? extends Vector3> points) {
//		Plane near = cam.frustum.planes[0];
//		for (int i = 0; i < points.size(); i++) {
//			if (near.testPoint(points.get(i)) == PlaneSide.Back)
//				return true;
//		}
//		return false;
//	}
//
//	private void projectToScreen(BuildCamera cam, ArrayList<Vertex> points) {
//		for (int i = 0; i < points.size(); i++)
//			cam.project(points.get(i));
//	}
//
//	private ArrayList<Vertex> project(BuildCamera cam, int z, int sectnum, Heinum h) {
//		WALL wal = wall[z];
//		if (!WallFacingCheck(wal))
//			return null;
//
//		ArrayList<Vertex> vertex = world.getPoints(h, sectnum, z);
//		if (!cam.polyInCamera(vertex))
//			return null;
//
//		if (NearPlaneCheck(cam, vertex)) {
//			PolygonClipper cl = new PolygonClipper();
//			vertex = cl.ClipPolygon(cam.frustum, vertex);
//			if (vertex.size() < 3)
//				return null;
//		}
//
//		projectToScreen(cam, vertex);
//		return vertex;
//	}
//
//	public ShapeRenderer shape;
//
//	private void draw2dSurface(int z, int sectnum, Heinum heinum) {
//		ArrayList<Vertex> coords = project(cam, z, (short) sectnum, heinum);
//		if (coords != null) {
//			if (heinum == Heinum.MaxWall)
//				shape.setColor(0.8f, 0.8f, 0.8f, 1);
//			else if (heinum == Heinum.Upper || heinum == Heinum.Lower)
//				shape.setColor(0.8f, 0.8f, 0.0f, 1);
//			else if (heinum == Heinum.Portal)
//				shape.setColor(0.8f, 0, 0, 1);
//
//			for (int i = 0; i < coords.size(); i++) {
//				int next = (i + 1) % coords.size();
//				shape.line(coords.get(i).x, coords.get(i).y, coords.get(next).x, coords.get(next).y);
//			}
//		}
//	}

}
