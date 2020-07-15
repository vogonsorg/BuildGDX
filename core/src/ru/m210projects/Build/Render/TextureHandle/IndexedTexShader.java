package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.*;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Settings.GLSettings.glfiltermodes;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public class IndexedTexShader {

	private class PaletteData extends TileData {
		public final TextureBuffer data;

		public PaletteData(byte[] paldata, int shade) {
			TextureBuffer buffer = getTmpBuffer(getWidth() * getHeight() * 3);
			buffer.clear();
			for (int p = 0; p < MAXPALOOKUPS; p++) {
				int pal = p;
				if (palookup[pal] == null)
					pal = 0;

				for (int i = 0; i < 256; i++) {
					int dacol = palookup[pal][i + (shade << 8)] & 0xFF;
					buffer.putBytes(paldata, 3 * dacol, 3);
				}
			}

			this.data = buffer;
		}

		@Override
		public int getWidth() {
			return 256;
		}

		@Override
		public int getHeight() {
			return MAXPALOOKUPS;
		}

		@Override
		public ByteBuffer getPixels() {
			return data.getBuffer();
		}

		@Override
		public int getGLType() {
			return GL_UNSIGNED_BYTE;
		}

		@Override
		public int getGLInternalFormat() {
			return GL_RGB;
		}

		@Override
		public int getGLFormat() {
			return GL_RGB;
		}

		@Override
		public PixelFormat getPixelFormat() {
			return PixelFormat.Rgb;
		}

		@Override
		public boolean hasAlpha() {
			return false;
		}

		@Override
		public boolean isClamped() {
			return false;
		}

		@Override
		public boolean isHighTile() {
			return false;
		}
	}

	private GLTile palette[];
	private ShaderProgram shaderProg;
	private TextureManager cache;

	public IndexedTexShader(TextureManager cache) throws Exception {
		String fragment = "uniform sampler2D u_texture;\r\n" + "uniform sampler2D u_colorTable;\r\n"
				+ "uniform float u_pal;\r\n" + "uniform float u_alpha;\r\n" + "\r\n" + "void main()\r\n" + "{	\r\n"
				+ "	float index = texture2D(u_texture, gl_TexCoord[0].xy).r;\r\n" + "	if(index == 1.0) discard;\r\n"
				+ "	\r\n" + "	vec3 color = texture2D(u_colorTable, vec2(index, u_pal / 256.0)).rgb;	\r\n"
				+ "	gl_FragColor = vec4(color, u_alpha);\r\n" + "}";

		String vertex = "void main()\r\n" + "{\r\n"
				+ "	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; //ftransform();\r\n"
				+ "	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;\r\n"
				+ "	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;\r\n" + "}";

		shaderProg = new ShaderProgram(vertex, fragment);
		if (!shaderProg.isCompiled())
			throw new Exception("Shader compile error: " + shaderProg.getLog());

		palette = new GLTile[numshades];
		this.cache = cache;
	}

	private GLTile createPalette(byte[] paldata, int shade) {
		TileData dat = new PaletteData(paldata, shade);
		GLTile palette = cache.newTile(dat, 0, false);
		palette.setupTextureFilter(glfiltermodes[0], 1); //GL_NEAREST

		return palette;
	}

	public void changePalette(byte[] pal) {
		for (int i = 0; i < numshades; i++) {
			if (palette[i] != null)
				palette[i].delete();
			palette[i] = createPalette(pal, i);
		}
	}

	public void bind() {
		shaderProg.begin();
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void unbind() {
		shaderProg.end();
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void setShaderParams(int pal, int shade) {
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		shade = BClipRange(shade, 0, numshades - 1);
		palette[shade].bind();
		shaderProg.setUniformi("u_colorTable", 1);
		shaderProg.setUniformf("u_pal", pal);
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void shaderTransparent(float alpha) {
		shaderProg.setUniformf("u_alpha", alpha);
	}
}
