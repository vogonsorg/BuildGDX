package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.DETAILPAL;
import static ru.m210projects.Build.Engine.GLOWPAL;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Render.Types.GL10.GL_MODELVIEW;
import static ru.m210projects.Build.Render.Types.GL10.GL_RGB_SCALE;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE0;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_ENV;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.Polymost.Polymost.Rendering;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Render.Types.Palette;
import ru.m210projects.Build.Script.TextureHDInfo;
import ru.m210projects.Build.Settings.GLSettings;
import ru.m210projects.Build.Types.Tile;

public class TextureManager {

	protected final Engine engine;
	private final GLTileArray cache;
	private TextureHDInfo info;
	protected GLTile bindedTile;
	protected IndexedTexShader shader;
	protected int texunits;

	public TextureManager(Engine engine) {
		this.engine = engine;
		cache = new GLTileArray(MAXTILES);

		if (GLSettings.usePaletteShader.get())
			enableShader(true);
	}

	public void setTextureInfo(TextureHDInfo info) {
		this.info = info;
	}

	protected GLTile get(int dapicnum, int dapalnum, int skybox, boolean clamping, boolean alpha) {
		Hicreplctyp si = (GLSettings.useHighTile.get() && info != null) ? info.findTexture(dapicnum, dapalnum, skybox)
				: null;

		if (si == null) {
			if (skybox != 0 || dapalnum >= (MAXPALOOKUPS - RESERVEDPALS))
				return null;

			if (shader != null)
				dapalnum = 0; // don't load 8bit texture with pal != 0, it's work of shader
		}

		GLTile tile = cache.get(dapicnum, dapalnum, clamping, skybox);
		if (si != null && tile != null && tile.hicr == null && si.skybox == null) { // GDX 29.05.2020 skybox check added
			// (if you're switching between 8bit and hrp textures, old loaded texture should
			// be disposed. Addon HRP support)
			cache.dispose(dapicnum); // old 8-bit texture
			tile = null;
		}

		boolean useMipMaps = GLSettings.textureFilter.get().mipmaps;
		if (tile != null) {
			if (tile.isInvalidated()) {
				tile.update(loadPic(si, dapicnum, dapalnum, clamping, alpha, skybox), useMipMaps);
				tile.setInvalidated(false);
			}
		} else {
			if (si != null && dapalnum != 0 && info.findTexture(dapicnum, 0, skybox) == si
					&& (tile = cache.get(dapicnum, 0, clamping, skybox)) != null)
				return bind(tile);

			TileData data = loadPic(si, dapicnum, dapalnum, clamping, alpha, skybox);
			if (data == null)
				return null;

			tile = newTile(data, data.isHighTile() ? si.palnum : dapalnum, useMipMaps);
			if (data.isHighTile()) {
				tile.setHighTile(si);
				tile.setHasAlpha(alpha);
				tile.setSkyboxFace(skybox);

				if (skybox > 0) {
					tile.scalex = tile.getWidth() / 64.0f;
					tile.scaley = tile.getHeight() / 64.0f;
				} else {
					Tile pic = engine.getTile(dapicnum);
					tile.scalex = tile.getWidth() / ((float) pic.getWidth());
					tile.scaley = tile.getHeight() / ((float) pic.getHeight());
				}
			}
			cache.add(tile, dapicnum);
		}

		if (GLInfo.multisample != 0 && dapalnum >= (MAXPALOOKUPS - RESERVEDPALS)) {
			BuildGdx.gl.glActiveTexture(++texunits);
			BuildGdx.gl.glEnable(GL_TEXTURE_2D);
		}

		return bind(tile);
	}

	public GLTile bind(GLTile tile) {
		if (bindedTile == tile)
			return tile;

		if (bindedTile != null && shader != null && bindedTile.isRequireShader() && !tile.isRequireShader())
			shader.unbind();

		if (tile.bind()) {
			if (shader != null && tile.isRequireShader() && (bindedTile == null || !bindedTile.isRequireShader()))
				shader.bind();
		}
		bindedTile = tile;

		return tile;
	}

	public GLTile bind(int tilenum, int pal, int shade, int skybox, int method) {
		Tile pic = engine.getTile(tilenum);
		if (!pic.isLoaded())
			engine.loadtile(tilenum);

		GLTile tile = get(tilenum, pal, skybox, clampingMode(method), alphaMode(method));
		if (tile == null)
			return null;

		if (tile.isRequireShader()) {
			getShader().setShaderParams(pal, shade);

			float alpha = 1.0f;
			switch (method & 3) {
			case 2:
				alpha = TRANSLUSCENT1;
				break;
			case 3:
				alpha = TRANSLUSCENT2;
				break;
			}
			getShader().shaderTransparent(alpha);
		} else {
			// texture scale by parkar request
			if (tile.getXScale() != 1.0f || (tile.getYScale() != 1.0f) && Rendering.Skybox.getIndex() == 0) {
				BuildGdx.gl.glMatrixMode(GL_TEXTURE);
				BuildGdx.gl.glLoadIdentity();
				BuildGdx.gl.glScalef(tile.getXScale(), tile.getYScale(), 1.0f);
				BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
			}

			if (GLInfo.multisample != 0 && GLSettings.useHighTile.get() && Rendering.Skybox.getIndex() == 0) {
				if (Console.Geti("r_detailmapping") != 0) {
					GLTile detail = get(tilenum, DETAILPAL, 0, clampingMode(method), alphaMode(method));
					if (detail != null) {
						detail.setupTextureDetail();

						BuildGdx.gl.glMatrixMode(GL_TEXTURE);
						BuildGdx.gl.glLoadIdentity();
						if (tile.getXScale() != 1.0f || (tile.getYScale() != 1.0f))
							BuildGdx.gl.glScalef(tile.getXScale(), tile.getYScale(), 1.0f);
						if (detail.getXScale() != 1.0f || (detail.getYScale() != 1.0f))
							BuildGdx.gl.glScalef(detail.getXScale(), detail.getYScale(), 1.0f);
						BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
					}
				}

				if (Console.Geti("r_glowmapping") != 0) {
					GLTile glow = get(tilenum, GLOWPAL, 0, clampingMode(method), alphaMode(method));
					if (glow != null) {
						glow.setupTextureGlow();
					}
				}
			}

			Color c = getshadefactor(shade, method);
			if (tile.isHighTile() && info != null) {
				if (tile.getPal() != pal) {
					// apply tinting for replaced textures

					Palette p = info.getTints(pal);
					c.r *= p.r / 255.0f;
					c.g *= p.g / 255.0f;
					c.b *= p.b / 255.0f;
				}

				Palette pdetail = info.getTints(MAXPALOOKUPS - 1);
				if (pdetail.r != 255 || pdetail.g != 255 || pdetail.b != 255) {
					c.r *= pdetail.r / 255.0f;
					c.g *= pdetail.g / 255.0f;
					c.b *= pdetail.b / 255.0f;
				}
			}

			if (!pic.isLoaded())
				c.a = 0.01f; // Hack to update Z-buffer for invalid mirror textures
			tile.setColor(c.r, c.g, c.b, c.a);
		}

		return tile;
	}

	public void precache(int dapicnum, int dapalnum, boolean clamped) {
		bind(dapicnum, dapalnum, 0, 0, clamped ? 4 : 0);
	}

	public int getTextureUnits() {
		return texunits;
	}

	public void unbind() {
		if (GLInfo.multisample == 0)
			return;

		while (texunits >= GL_TEXTURE0) {
			BuildGdx.gl.glActiveTexture(texunits);
			BuildGdx.gl.glMatrixMode(GL_TEXTURE);
			BuildGdx.gl.glLoadIdentity();
			BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
			if (texunits > GL_TEXTURE0) {
				BuildGdx.gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
				BuildGdx.gl.glDisable(GL_TEXTURE_2D);
			}
			texunits--;
		}
		texunits = GL_TEXTURE0;
	}

	private TileData loadPic(Hicreplctyp hicr, int dapicnum, int dapalnum, boolean clamping, boolean alpha,
			int skybox) {

		int expand = 1 | 2;
		System.err.println("loadPic " + dapicnum);
		if (hicr != null) {
			String fn = checkResource(hicr, dapicnum, skybox);
			byte[] data = BuildGdx.cache.getBytes(fn, 0);
			if (data != null) {
				try {
					return new PixmapTileData(new Pixmap(data, 0, data.length), clamping, expand);
				} catch (Throwable t) {
					t.printStackTrace();
					if (skybox != 0)
						return null;
				}
			}
		}

		if (shader != null)
			return new IndexedTileData(engine.getTile(dapicnum), clamping, alpha, expand);
		return new RGBTileData(engine.getTile(dapicnum), dapalnum, clamping, alpha, expand);
	}

	private String checkResource(Hicreplctyp hicr, int dapic, int facen) {
		if (hicr == null)
			return null;

		String fn = null;
		if (facen > 0) {
			if (hicr.skybox == null || facen > 6 || hicr.skybox.face[facen - 1] == null)
				return null;

			fn = hicr.skybox.face[facen - 1];
		} else
			fn = hicr.filename;

		if (!BuildGdx.cache.contains(fn, 0)) {
			Console.Print("Hightile[" + dapic + "]: File \"" + fn + "\" not found");
			if (facen > 0)
				hicr.skybox.ignore = 1;
			else
				hicr.ignore = 1;
			return null;
		}

		return fn;
	}

	protected final Color polyColor = new Color();
	protected Color getshadefactor(int shade, int method) {
		float fshade = min(max(shade * 1.04f, 0), numshades);
		float f = (numshades - fshade) / numshades;

		polyColor.r = polyColor.g = polyColor.b = f;

		switch (method & 3) {
		default:
		case 0:
		case 1:
			polyColor.a = 1.0f;
			break;
		case 2:
			polyColor.a = TRANSLUSCENT1;
			break;
		case 3:
			polyColor.a = TRANSLUSCENT2;
			break;
		}

		return polyColor;
	}

	public GLTile newTile(TileData pic, int palnum, boolean useMipMaps) {
		return new GLTile(pic, palnum, useMipMaps);
	}

	public void setFilter(GLFilter filter) {
		int anisotropy = GLSettings.textureAnisotropy.get();
		for (int i = MAXTILES - 1; i >= 0; i--) {
			cache.setFilter(i, filter, anisotropy);
		}
	}

	public void invalidate(int dapicnum, int dapalnum, boolean clamped) {
		GLTile tile = cache.get(dapicnum, dapalnum, clamped, 0);
		if (tile == null)
			return;

		if (tile.hicr == null)
			tile.setInvalidated(true);
	}

	public void invalidateall() {
		for (int j = MAXTILES - 1; j >= 0; j--)
			cache.invalidate(j);
	}

	public IndexedTexShader getShader() {
		return shader;
	}

	public boolean clampingMode(int dameth) {
		return ((dameth & 4) >> 2) == 1;
	}

	public boolean alphaMode(int dameth) {
		return (dameth & 256) == 0;
	}

	public void uninit() {
		for (int i = MAXTILES - 1; i >= 0; i--) {
			cache.dispose(i);
		}
	}

	public boolean isUseShader() {
		return shader != null && bindedTile != null && bindedTile.isRequireShader();
	}

	public void enableShader(boolean enable) {
		if (enable && shader == null) {
			try {
				shader = new IndexedTexShader(this);
				shader.changePalette(curpalette.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
				shader = null;
			}
		} else if (shader != null) {
			shader.dispose();
			shader = null;
		}

		uninit();
	}
}
