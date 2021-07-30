package ru.m210projects.Build.Render.GdxRender.Shaders;

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

	protected ShaderProgram skyshader;
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
		IndexedWorldShader, RGBWorldShader, IndexedSkyShader, RGBSkyShader, BitmapShader, FadeShader
	}

	public void init(TextureManager textureCache) {
		skyshader = allocSkyShader();
		skyshader32 = null;
		texshader = allocIndexedShader(textureCache);
		texshader32 = null;
		bitmapShader = allocBitmapShader();
		fadeshader = allocFadeShader();
	}

	public void mirror(Shader shader, boolean mirror) {
		if (shader == Shader.IndexedWorldShader) {
			if (currentShader != texshader)
				texshader.begin();
			texshader.setUniformi(world_mirror, mirror ? 1 : 0);
		} else if (shader == Shader.RGBWorldShader) {
			if (currentShader != texshader32)
				texshader32.begin();

			// XXX
		}
	}

	public void frustum(Shader shader, Plane[] clipPlane) {
		if (shader == Shader.IndexedWorldShader) {
			if (currentShader != texshader)
				texshader.begin();

			if (clipPlane == null) {
				texshader.setUniformi(world_planeClipping, 0);
				return;
			}

			texshader.setUniformi(world_planeClipping, 1);
			texshader.setUniformf(world_plane0, clipPlane[0].normal.x, clipPlane[0].normal.y, clipPlane[0].normal.z,
					clipPlane[0].d);
			texshader.setUniformf(world_plane1, clipPlane[1].normal.x, clipPlane[1].normal.y, clipPlane[1].normal.z,
					clipPlane[1].d);
		}
	}

	public void transform(Shader shader, Matrix4 transform) {
		if (shader == Shader.IndexedWorldShader) {
			if (currentShader != texshader)
				texshader.begin();

			texshader.setUniformMatrix(world_transform, transform);
		}
	}

	public void prepare(Shader shader, BuildCamera cam) {
		if (shader == Shader.IndexedWorldShader) {
			if (currentShader != texshader)
				texshader.begin();

			texshader.setUniformMatrix(world_projTrans, cam.combined);
			texshader.setUniformMatrix(world_modelView, cam.view);
			texshader.setUniformMatrix(world_invProjectionView, cam.invProjectionView);
			texshader.setUniformf(world_viewport, windowx1, windowy1, windowx2 - windowx1 + 1, windowy2 - windowy1 + 1);
			texshader.setUniformi(world_planeClipping, 0);
		} else if (shader == Shader.RGBWorldShader) {
			if (currentShader != texshader32)
				texshader32.begin();
			// XXX
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

	public void bind(Shader shader) {
		if (currentShader != null)
			currentShader.end();

		switch (shader) {
		case IndexedWorldShader:
			texshader.begin();
			break;
		case RGBWorldShader:
			texshader32.begin();
			break;
		case IndexedSkyShader:
			skyshader.begin();
			break;
		case RGBSkyShader:
			skyshader32.begin();
			break;
		case BitmapShader:
			bitmapShader.begin();
			break;
		case FadeShader:
			fadeshader.begin();
			break;
		}
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

	public ShaderProgram allocSkyShader() {
		try {
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
			this.world_plane0 = shader.getUniformLocation("u_plane0");
			this.world_plane1 = shader.getUniformLocation("u_plane1");
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
}
