package ru.m210projects.Build.Render.GdxRender.Shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Render.TextureHandle.IndexedShader;
import ru.m210projects.Build.Render.Types.FadeEffect.FadeShader;

public class ShaderManager {

	protected ShaderProgram skyshader;
	protected IndexedShader texshader;
	protected FadeShader fadeshader;
	public ShaderProgram currentShader;

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
