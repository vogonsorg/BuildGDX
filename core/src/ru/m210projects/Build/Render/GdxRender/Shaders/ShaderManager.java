package ru.m210projects.Build.Render.GdxRender.Shaders;

import static ru.m210projects.Build.Engine.inpreparemirror;
import static ru.m210projects.Build.Engine.windowx1;
import static ru.m210projects.Build.Engine.windowx2;
import static ru.m210projects.Build.Engine.windowy1;
import static ru.m210projects.Build.Engine.windowy2;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;

import ru.m210projects.Build.Render.GdxRender.BuildCamera;
import ru.m210projects.Build.Render.TextureHandle.IndexedShader;
import ru.m210projects.Build.Render.TextureHandle.TextureManager;
import ru.m210projects.Build.Render.Types.FadeEffect.FadeShader;

public class ShaderManager {

	protected IndexedSkyShaderProgram skyshader;
	protected IndexedShader texshader;
	protected ShaderProgram skyshader32;
	protected ShaderProgram texshader32;

	protected ShaderProgram bitmapShader;
	protected FadeShader fadeshader;

	public ShaderProgram currentShader;

	private int world_projTrans;
	private int world_modelView;
	private int world_invProjectionView;
	private int world_viewport;
	private int world_mirror;
	private int world_planeClipping;
	private int world_plane0;
	private int world_plane1;
	private int world_transform;

	public enum Shader {
		IndexedWorldShader, RGBWorldShader, IndexedSkyShader, RGBSkyShader, BitmapShader, FadeShader;

		ShaderProgram shader;

		public void set(ShaderProgram shader) {
			this.shader = shader;
		}

		public ShaderProgram get() {
			return shader;
		}
	}

	public void init(TextureManager textureCache) {
		skyshader = allocIndexedSkyShader(textureCache);
		Shader.IndexedSkyShader.set(skyshader);
		skyshader32 = null;
		Shader.RGBSkyShader.set(skyshader32);
		texshader = allocIndexedShader(textureCache);
		Shader.IndexedWorldShader.set(texshader);
		texshader32 = null;
		Shader.RGBWorldShader.set(texshader32);
		bitmapShader = allocBitmapShader();
		Shader.BitmapShader.set(bitmapShader);
		fadeshader = allocFadeShader();
		Shader.FadeShader.set(fadeshader);
	}

	public void mirror(Shader shader, boolean mirror) {
		ShaderProgram sh = shader.get();
		if (currentShader != sh)
			sh.begin();

		switch (shader) {
		case IndexedWorldShader:
			texshader.setUniformi(world_mirror, mirror ? 1 : 0);
			break;
		case RGBWorldShader:
			// XXX
			break;
		case IndexedSkyShader:
			skyshader.mirror(mirror);
			break;
		}
	}

	public void frustum(Shader shader, Plane[] clipPlane) {
		ShaderProgram sh = shader.get();
		if (currentShader != sh)
			sh.begin();

		if (shader == Shader.IndexedWorldShader) {
			if (clipPlane == null) {
				texshader.setUniformi(world_planeClipping, 0);
				return;
			}

			texshader.setUniformi(world_planeClipping, 1);
			texshader.setUniformf(world_plane0, clipPlane[0].normal.x, clipPlane[0].normal.y, clipPlane[0].normal.z,
					clipPlane[0].d);

			//XXX world_plane1 doesn't find
			texshader.setUniformf("u_plane[1]", clipPlane[1].normal.x, clipPlane[1].normal.y, clipPlane[1].normal.z,
					clipPlane[1].d);
		}
	}

	public void transform(Shader shader, Matrix4 transform) {
		ShaderProgram sh = shader.get();
		if (currentShader != sh)
			sh.begin();

		switch (shader) {
		case IndexedWorldShader:
			texshader.setUniformMatrix(world_transform, transform);
			break;
		case IndexedSkyShader:
			skyshader.transform(transform);
			break;
		}
	}

	public void textureParams8(Shader shader, int pal, int shade, float alpha, boolean lastIndex) {
		ShaderProgram sh = shader.get();
		if (currentShader != sh)
			sh.begin();

		switch (shader) {
		case IndexedWorldShader:
			texshader.setTextureParams(pal, shade);
			texshader.setDrawLastIndex(lastIndex);
			texshader.setTransparent(alpha);
			break;
		case IndexedSkyShader:
			skyshader.setTextureParams(pal, shade);
			skyshader.setDrawLastIndex(lastIndex);
			skyshader.setTransparent(alpha);
			break;
		}
	}

	public void prepare(Shader shader, BuildCamera cam) {
		ShaderProgram sh = shader.get();
		if (currentShader != sh)
			sh.begin();

		switch (shader) {
		case IndexedWorldShader:
			texshader.setUniformMatrix(world_projTrans, cam.combined);
			texshader.setUniformMatrix(world_modelView, cam.view);
			texshader.setUniformMatrix(world_invProjectionView, cam.invProjectionView);
			texshader.setUniformf(world_viewport, windowx1, windowy1, windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
			texshader.setUniformi(world_planeClipping, 0);
			break;
		case RGBWorldShader:
			// XXX
			break;
		case IndexedSkyShader:
			skyshader.prepare(cam);
			break;
		}
	}

	public ShaderProgram get(Shader shader) {
		switch (shader) {
		case IndexedWorldShader:
			return texshader;
		case RGBWorldShader:
			return texshader32;
		case IndexedSkyShader:
			return skyshader;
		case RGBSkyShader:
			return skyshader32;
		case BitmapShader:
			return bitmapShader;
		case FadeShader:
			return fadeshader;
		}
		return null;
	}

	public ShaderProgram bind(Shader shader) {
		if (currentShader != null)
			currentShader.end();

		ShaderProgram sh = shader.get();
		if(sh != null)
			sh.begin();
		return sh;
	}

	public void unbind() {
		if (currentShader != null)
			currentShader.end();
		currentShader = null;
	}

	public FadeShader allocFadeShader() {
		return new FadeShader() {
			@Override
			public void begin() {
				super.begin();
				currentShader = this;
			}
		};
	}

	public IndexedSkyShaderProgram allocIndexedSkyShader(final TextureManager textureCache) {
		try {
			IndexedSkyShaderProgram skyshader = new IndexedSkyShaderProgram() {
				@Override
				public void begin() {
					super.begin();
					currentShader = this;
				}

				@Override
				public void bindPalette() {
					textureCache.getPalette().bind();
				}

				@Override
				public void bindPalookup(int pal) {
					textureCache.getPalookup(pal).bind();
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

	public IndexedShader allocIndexedShader(final TextureManager textureCache) {
		try {
			IndexedShader shader = new IndexedShader(WorldShader.vertex, WorldShader.fragment) {
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

			this.world_projTrans = shader.getUniformLocation("u_projTrans");
			this.world_modelView = shader.getUniformLocation("u_modelView");
			this.world_invProjectionView = shader.getUniformLocation("u_invProjectionView");
			this.world_viewport = shader.getUniformLocation("u_viewport");
			this.world_mirror = shader.getUniformLocation("u_mirror");
			this.world_planeClipping = shader.getUniformLocation("u_planeClipping");
			this.world_plane0 = shader.getUniformLocation("u_plane[0]");
			this.world_plane1 = shader.getUniformLocation("u_plane[1]");
			this.world_transform = shader.getUniformLocation("u_transform");

			return shader;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public ShaderProgram allocBitmapShader() { // OrthoShader
		ShaderProgram shader = new ShaderProgram(BitmapShader.vertex, BitmapShader.fragment) {
			@Override
			public void begin() {
				super.begin();
				currentShader = this;
			}
		};
		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	@Override
	public String toString() {
		String out = "Current shader: ";
		Shader current = null;
		for (Shader sh : Shader.values()) {
			if (sh.get() == currentShader) {
				current = sh;
				break;
			}
		}
		if (current != null)
			out += current.name();
		else
			out += "NULL";
		return out;
	}
}
