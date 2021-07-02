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
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;
import static com.badlogic.gdx.graphics.GL20.GL_VERSION;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.Pragmas.dmulscale;
import static ru.m210projects.Build.Pragmas.mulscale;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
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
import ru.m210projects.Build.Render.GdxRender.WorldMesh.GLSurface;
import ru.m210projects.Build.Render.GdxRender.WorldMesh.Heinum;
import ru.m210projects.Build.Render.GdxRender.Scanner.SectorScanner;
import ru.m210projects.Build.Render.GdxRender.Scanner.VisibleSector;
import ru.m210projects.Build.Render.GdxRender.Shaders.SkyShader;
import ru.m210projects.Build.Render.GdxRender.Shaders.WorldShader;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.IndexedShader;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.TextureHandle.TextureManager.ExpandTexture;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.FadeEffect.FadeShader;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile;
import ru.m210projects.Build.Types.Tile.AnimType;
import ru.m210projects.Build.Types.TileFont;
import ru.m210projects.Build.Types.WALL;

public class GDXRenderer implements GLRenderer {

//	TODO:
//  DukeDC6 train wall update bug

//  Setviewtotile bug (tekwar)
//  Fullscreen change
//	Overheadmap
//	Scansectors memory leak (WallFrustum)
//	Maskwall sort
//  enable/ disable rgb shader
//	Orpho renderer 8bit textures
//	Hires + models
//	Skyboxes
//	Sky texture

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
	protected GdxOrphoRen orphoRen;
	protected DefScript defs;
	protected ShaderProgram skyshader;
	protected IndexedShader texshader;
	protected FadeShader fadeshader;
	public static ShaderProgram currentShader;

	private ByteBuffer pix32buffer;
	private ByteBuffer pix8buffer;
	private Matrix4 transform = new Matrix4();
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

	public GDXRenderer(Engine engine) {
		if (BuildGdx.graphics.getFrameType() != FrameType.GL)
			BuildGdx.app.setFrame(FrameType.GL);
		GLInfo.init();
		this.engine = engine;
		this.textureCache = getTextureManager();

		this.gl = BuildGdx.graphics.getGL20();
		this.sprR = new SpriteRenderer(engine, this);
		this.scanner = new SectorScanner(engine) {
			@Override
			protected Matrix4 getSpriteMatrix(SPRITE tspr) {
				return sprR.getMatrix(tspr);
			}
		};

		this.orphoRen = new GdxOrphoRen(engine, textureCache);

		Arrays.fill(mirrorTextures, false);
		int[] mirrors = getMirrorTextures();
		if (mirrors != null) {
			for (int i = 0; i < mirrors.length; i++)
				mirrorTextures[mirrors[i]] = true;
		}

		Console.Println(BuildGdx.graphics.getGLVersion().getRendererString() + " " + gl.glGetString(GL_VERSION)
				+ " initialized", OSDTEXT_GOLD);
	}

	protected int[] getMirrorTextures() {
		return null;
	}

	protected IndexedShader getTextureShader() {
		return texshader;
	}

	private IndexedShader allocIndexedShader() {
		try {
//			FileInputStream fis = new FileInputStream(new File("worldshader_vert.glsl"));
//			byte[] data = new byte[fis.available()];
//			fis.read(data);
//			String vert = new String(data);
//
//			fis = new FileInputStream(new File("worldshader_frag.glsl"));
//			data = new byte[fis.available()];
//			fis.read(data);
//			String frag = new String(data);

			System.err.println("Allocate sahder");
			return new IndexedShader(WorldShader.vertex, WorldShader.fragment) {
				@Override
				public void bindPalette() {
					textureCache.getPalette().bind();
				}

				@Override
				public void bindPalookup(int pal) {
					textureCache.getPalookup(pal).bind();
				}

				@Override
				public void begin() {
					super.begin();
					currentShader = this;
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ShaderProgram allocSkyShader() {
//		FileInputStream fis = null;
		try {
//			fis = new FileInputStream(new File("skyshader_frag.glsl"));
//			byte[] data = new byte[fis.available()];
//			fis.read(data);
//			String frag = new String(data);
//
//			fis = new FileInputStream(new File("skyshader_vert.glsl"));
//			data = new byte[fis.available()];
//			fis.read(data);
//			String vert = new String(data);

			ShaderProgram skyshader = new ShaderProgram(SkyShader.vertex, SkyShader.fragment) {
				@Override
				public void begin() {
					super.begin();
					currentShader = this;
				}
			};

			if (!skyshader.isCompiled())
				System.err.println("Shader compile error: " + skyshader.getLog());

			return skyshader;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void init() {
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		this.cam = new BuildCamera(fov, xdim, ydim, 512, 8192);

		this.skyshader = allocSkyShader();
		this.fadeshader = new FadeShader() {
			@Override
			public void begin() {
				super.begin();
				currentShader = this;
			}
		};
		this.texshader = allocIndexedShader();
		this.textureCache.changePalette(curpalette.getBytes());

		orphoRen.init();

		isInited = true;
	}

	@Override
	public void uninit() {
		orphoRen.uninit();

		textureCache.uninit();
	}

	@Override
	public RenderType getType() {
		return RenderType.PolyGDX;
	}

	@Override
	public PixelFormat getTexFormat() {
		return PixelFormat.Pal8;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	private void drawMask(int w) {
		gl.glDepthFunc(GL20.GL_LESS);
		gl.glDepthRangef(0.0001f, 0.99999f);

		drawSurf(world.getMaskedWall(w), 0);

		gl.glDepthFunc(GL20.GL_LESS);
		gl.glDepthRangef(defznear, defzfar);
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
	}

	protected void renderDrunkEffect() { // TODO: to shader
		if (drunk) {
			set2dview();

			gl.glActiveTexture(GL_TEXTURE0);
			boolean hasShader = texshader != null && texshader.isBinded();
			if (hasShader)
				texshader.end();

			if (frameTexture == null || framew != xdim || frameh != ydim) {
				int size = 1;
				for (size = 1; size < Math.max(xdim, ydim); size <<= 1)
					;

				if (frameTexture != null)
					frameTexture.dispose();
				else
					frameTexture = new GLTile(PixelFormat.Rgb, size, size);

				frameTexture.bind();
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, frameTexture.getWidth(), frameTexture.getHeight(), 0, GL_RGB,
						GL_UNSIGNED_BYTE, null);
				frameTexture.unsafeSetFilter(TextureFilter.Linear, TextureFilter.Linear);
				framew = xdim;
				frameh = ydim;
			}

			textureCache.bind(frameTexture);
			gl.glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, frameTexture.getWidth(), frameTexture.getHeight());

			gl.glDisable(GL_DEPTH_TEST);
			gl.glDisable(GL_CULL_FACE);

			float tiltang = (drunkIntensive * 360) / 2048f;
			float tilt = min(max(tiltang, -MAXDRUNKANGLE), MAXDRUNKANGLE);
			float u = (float) xdim / frameTexture.getWidth();
			float v = (float) ydim / frameTexture.getHeight();

			int originX = xdim / 2;
			int originY = ydim / 2;
			float width = xdim * 1.05f;
			float height = ydim * 1.05f;

			float xoffs = width / 2;
			float yoffs = height / 2;

			final float rotation = 360.0f * tiltang / 2048.0f;
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			float x1 = originX + (sin * yoffs - cos * xoffs);
			float y1 = originY - xoffs * sin - yoffs * cos;

			float x4 = x1 + width * cos;
			float y4 = y1 + width * sin;

			float x2 = x1 - height * sin;
			float y2 = y1 + height * cos;

			float x3 = x2 + (x4 - x1);
			float y3 = y2 + (y4 - y1);

			orphoRen.begin();
			orphoRen.setColor(1, 1, 1, abs(tilt) / (2 * MAXDRUNKANGLE));
			orphoRen.setTexture(frameTexture);
			orphoRen.addVertex(x1, ydim - y1, 0, 0);
			orphoRen.addVertex(x2, ydim - y2, 0, v);
			orphoRen.addVertex(x3, ydim - y3, u, v);
			orphoRen.addVertex(x4, ydim - y4, u, 0);
			orphoRen.end();

			gl.glEnable(GL_DEPTH_TEST);
			gl.glEnable(GL_CULL_FACE);

			if (hasShader)
				texshader.begin();
		}
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

	@Override
	public void drawrooms() {
		if (!clearStatus) { // once at frame
			gl.glClear(GL_COLOR_BUFFER_BIT);
			gl.glClearColor(0.0f, 0.5f, 0.5f, 1);
			clearStatus = true;
		}
		gl.glClear(GL_DEPTH_BUFFER_BIT);

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
		texshader.begin();
		if (inpreparemirror) {
			gl.glCullFace(GL_FRONT);
			texshader.setUniformi("u_mirror", 1);
		} else {
			gl.glCullFace(GL_BACK);
			texshader.setUniformi("u_mirror", 0);
		}

		texshader.setUniformi("u_drawSprite", 0);
		texshader.setUniformMatrix("u_projTrans", cam.combined);
		texshader.setUniformMatrix("u_modelView", cam.view);
		texshader.setUniformMatrix("u_invProjectionView", cam.invProjectionView);
		texshader.setUniformf("u_viewport", windowx1, windowy1, windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
		texshader.setClip(0, 0, xdim, ydim);

		prerender(sectors);

		for (int i = 0; i < sectors.size(); i++)
			drawSector(sectors.get(i));

		setFrustum(null);

		for (int i = 0; i < sectors.size(); i++)
			drawSkySector(sectors.get(i));
		drawSkyPlanes();

		texshader.end();

		spritesortcnt = scanner.getSpriteCount();
		tsprite = scanner.getSprites();

		inpreparemirror = false;
	}

	private void prerender(ArrayList<VisibleSector> sectors) {
		if (inpreparemirror)
			return;

		bunchfirst.clear();
		setFrustum(null);

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
		}

		for (int i = 0; i < bunchfirst.size(); i++)
			drawSurf(bunchfirst.get(i), 0);
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
//		gl.glDisable(GL_DEPTH_TEST);

		if (scanner.getSkyPicnum(Heinum.SkyUpper) != -1) {
			bind(TileData.PixelFormat.Pal8, scanner.getSkyPicnum(Heinum.SkyUpper), scanner.getSkyPal(Heinum.SkyUpper),
					0, 0, 0);
			transform.idt();
			transform.translate(cam.position.x, cam.position.y, cam.position.z - 100);
			transform.scale(cam.far, cam.far, 1.0f);

			skyshader.begin();
			skyshader.setUniformMatrix("u_transform", transform);
			world.getSkyPlane().render(skyshader);
			skyshader.end();
		}

		if (scanner.getSkyPicnum(Heinum.SkyLower) != -1) {
			bind(TileData.PixelFormat.Pal8, scanner.getSkyPicnum(Heinum.SkyLower), scanner.getSkyPal(Heinum.SkyLower),
					0, 0, 0);
			transform.idt();
			transform.translate(cam.position.x, cam.position.y, cam.position.z + 100);
			transform.scale(cam.far, cam.far, 1.0f);

			skyshader.begin();
			skyshader.setUniformMatrix("u_transform", transform);
			world.getSkyPlane().render(skyshader);
			skyshader.end();
		}
		transform.idt();

		gl.glEnable(GL_CULL_FACE);
		gl.glDepthMask(true);
//		gl.glEnable(GL_DEPTH_TEST);
	}

	private void drawSector(VisibleSector sec) {
		int sectnum = sec.index;
		gotsector[sectnum >> 3] |= pow2char[sectnum & 7];

		if (!inpreparemirror)
			setFrustum(sec.clipPlane);

		if ((sec.secflags & 1) != 0) {
			rendering = Rendering.Floor.setIndex(sectnum);
			drawSurf(world.getFloor(sectnum), 0);
		}

		if ((sec.secflags & 2) != 0) {
			rendering = Rendering.Ceiling.setIndex(sectnum);
			drawSurf(world.getCeiling(sectnum), 0);
		}

		for (int w = 0; w < sec.walls.size; w++) {
			int flags = sec.wallflags.get(w);
			int z = sec.walls.get(w);
			rendering = Rendering.Wall.setIndex(z);
			drawSurf(world.getWall(z, sectnum), flags);
			drawSurf(world.getUpper(z, sectnum), flags);
			drawSurf(world.getLower(z, sectnum), flags);
		}
	}

	public void drawSkySector(VisibleSector sec) {
		for (int w = 0; w < sec.skywalls.size; w++) {
			int z = sec.skywalls.get(w);
			GLSurface ceil = world.getParallaxCeiling(z);
			if (ceil != null) {
				drawSky(ceil, ceil.picnum, ceil.getPal(), ceil.getMethod());
			}

			GLSurface floor = world.getParallaxFloor(z);
			if (floor != null) {
				drawSky(floor, floor.picnum, floor.getPal(), floor.getMethod());
			}
		}
	}

	private void drawSky(GLSurface surf, int picnum, int palnum, int method) {
		if (surf.count == 0)
			return;

		if (engine.getTile(picnum).getType() != AnimType.None)
			picnum += engine.animateoffs(picnum, 0);

		Tile pic = engine.getTile(picnum);
		if (!pic.isLoaded())
			engine.loadtile(picnum);

		engine.setgotpic(picnum);
		GLTile pth = bind(TileData.PixelFormat.Pal8, picnum, palnum, 0, 0, method);
		if (pth != null) {
			skyshader.begin();
			gl.glActiveTexture(GL20.GL_TEXTURE1);
			textureCache.getPalette().bind();
			skyshader.setUniformi("u_palette", 1);

			gl.glActiveTexture(GL20.GL_TEXTURE2);
			textureCache.getPalookup(palnum).bind();
			skyshader.setUniformi("u_palookup", 2);
			gl.glActiveTexture(GL20.GL_TEXTURE0);

			skyshader.setUniformf("u_camera", cam.position.x, cam.position.y, cam.position.z);
			skyshader.setUniformMatrix("u_projTrans", cam.combined);
			skyshader.setUniformMatrix("u_transform", transform);

			if (!pic.isLoaded()) {
				skyshader.setUniformf("u_alpha", 0.0f);
				method = 1;
			} else
				skyshader.setUniformf("u_alpha", 1.0f);

			if ((method & 3) == 0) {
				gl.glDisable(GL_BLEND);
//				gl.glDisable(GL_ALPHA_TEST);
			} else {
				gl.glEnable(GL_BLEND);
//				gl.glEnable(GL_ALPHA_TEST);
			}

			surf.render(skyshader);
			skyshader.end();
		}
	}

	private void drawSurf(GLSurface surf, int flags) {
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
			if (!pic.isLoaded())
				method = 1; // invalid data, HOM

			engine.setgotpic(picnum);
			GLTile pth = bind(PixelFormat.Pal8, picnum, surf.getPal(), surf.getShade(), 0, method);
			if (pth != null) {
				int combvis = globalvisibility;
				int vis = surf.getVisibility();
				if (vis != 0)
					combvis = mulscale(globalvisibility, (vis + 16) & 0xFF, 4);
				texshader.setVisibility((int) (-combvis / 64.0f));

				if ((method & 3) == 0) {
					Gdx.gl.glDisable(GL_BLEND);
				} else {
					Gdx.gl.glEnable(GL_BLEND);
				}

				surf.render(texshader);
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

		beforedrawrooms = 1;
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
	public void enableShader(boolean enable) {
		// TODO: 8bit / rgb switch
	}

	@Override
	public void palfade(HashMap<String, FadeEffect> fades) { // TODO: to shader?
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_TEXTURE_2D);

		gl.glEnable(GL_BLEND);

		set2dview();
		texshader.end();
		fadeshader.begin();

		palfadergb.draw(fadeshader);
		if (fades != null) {
			Iterator<FadeEffect> it = fades.values().iterator();
			while (it.hasNext()) {
				FadeEffect obj = it.next();
				obj.draw(fadeshader);
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

		textureCache.precache(TileData.PixelFormat.Pal8, dapicnum, dapalnum, datype);
	}

	@Override
	public void gltexapplyprops() {
		GLFilter filter = GLSettings.textureFilter.get();
		textureCache.setFilter(filter);

		if (defs == null)
			return;

		int anisotropy = GLSettings.textureAnisotropy.get();
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
				textureCache.uninit();
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
					if (texshader != null)
						textureCache.invalidatepalookup(j);
				}
				break;
			case All:
				textureCache.invalidateall();
				break;
			}
		}
	}

	@Override
	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth) {
		textureCache.invalidate(dapicnum, dapalnum, textureCache.clampingMode(dameth));
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

	private void setFrustum(Plane[] clipPlane) {
		if (!texshader.isBinded())
			texshader.begin();

		if (clipPlane == null) {
			texshader.setUniformi("u_frustumClipping", 0);
			return;
		}

		texshader.setUniformi("u_frustumClipping", 1);
//		texshader.setUniformf("u_plane[0]", clipPlane[0].normal.x, clipPlane[0].normal.y, clipPlane[0].normal.z,
//				clipPlane[0].d);
//		texshader.setUniformf("u_plane[1]", clipPlane[1].normal.x, clipPlane[1].normal.y, clipPlane[1].normal.z,
//				clipPlane[1].d);
//		texshader.setUniformf("u_plane[2]", clipPlane[2].normal.x, clipPlane[2].normal.y, clipPlane[2].normal.z,
//				clipPlane[2].d);
//		texshader.setUniformf("u_plane[3]", clipPlane[3].normal.x, clipPlane[3].normal.y, clipPlane[3].normal.z,
//				clipPlane[3].d);

		texshader.setUniformf("u_plane[0]", clipPlane[0].normal.x, clipPlane[0].normal.y, clipPlane[0].normal.z,
				clipPlane[0].d);
		texshader.setUniformf("u_plane[1]", clipPlane[1].normal.x, clipPlane[1].normal.y, clipPlane[1].normal.z,
				clipPlane[1].d);
	}

	protected GLTile bind(PixelFormat fmt, int dapicnum, int dapalnum, int dashade, int skybox, int method) {
		GLTile pth = textureCache.get(PixelFormat.Pal8, dapicnum, dapalnum, skybox, method);
		if (pth == null)
			return null;

		if (textureCache.bind(pth)) {
			gl.glActiveTexture(GL_TEXTURE0);
			if (pth.getPixelFormat() != PixelFormat.Pal8)
				texshader.end();
			else
				texshader.begin();
		}
		setTextureParameters(pth, dapicnum, dapalnum, dashade, skybox, method);

		return pth;
	}

	public void setTextureParameters(GLTile tile, int tilenum, int pal, int shade, int skybox, int method) {
		if (tile.getPixelFormat() == TileData.PixelFormat.Pal8) {
			if (!texshader.isBinded()) {
				gl.glActiveTexture(GL_TEXTURE0);
				texshader.begin();
			}
			texshader.setTextureParams(pal, shade);

			float alpha = 1.0f;
			switch (method & 3) {
			case 2:
				alpha = TRANSLUSCENT1;
				break;
			case 3:
				alpha = TRANSLUSCENT2;
				break;
			}

			texshader.setDrawLastIndex((method & 3) == 0 || !textureCache.alphaMode(method));
			texshader.setTransparent(alpha);
		}
	}

	protected void bind(GLTile tile) {
		if (textureCache.bind(tile)) {
			gl.glActiveTexture(GL_TEXTURE0);
			if (tile.getPixelFormat() != PixelFormat.Pal8)
				texshader.end();
			else
				texshader.begin();
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
		/* nothing */ }
}
