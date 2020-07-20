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
		String fragment =
				  "uniform sampler2D u_texture;"
				+ "uniform sampler2D u_colorTable;"
				+ "uniform float u_pal;"
				+ "uniform float u_alpha;"
				+ "uniform int u_draw255;"
				+ "uniform float u_fogdensity;"
				+ "uniform vec4 u_fogcolour;"
				+ "uniform float u_fogstart;"
				+ "uniform float u_fogend;"
				+ "vec4 fog(vec4 src) {"
				+ "    float dist = gl_FragCoord.z / gl_FragCoord.w;"
				+ "    float z = u_fogdensity * dist;"
				+ "    return mix(src, u_fogcolour, 1.0 - clamp((u_fogend - z) / (u_fogend - u_fogstart), 0.0, 1.0));"
				+ "}"
				+ "void main()"
				+ "{"
				+ "	float index = texture2D(u_texture, gl_TexCoord[0].xy).r;"
				+ " if(index == 1.0)"
				+ " {"
				+ "	 if(u_draw255 == 0) discard;"
				+ "	 index -= 0.5 / 256.0;"
				+ " }"
				+ "	"
				+ "	vec3 color = texture2D(u_colorTable, vec2(index, u_pal / 256.0)).rgb;"
				+ "	vec4 src = vec4(color, u_alpha);"
				+ " gl_FragColor = fog(src);"
				+ "}"
				;

		String vertex =
				  "void main()"
				+ "{"
				+ "	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; /*ftransform();*/"
				+ "	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;"
				+ "	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;"
				+ "}\r\n"
				;

		shaderProg = new ShaderProgram(vertex, fragment);
		if (!shaderProg.isCompiled())
			throw new Exception("Shader compile error: " + shaderProg.getLog());

		palette = new GLTile[numshades];
		this.cache = cache;
	}

	public void dispose() {
		shaderProg.dispose();
		for (int i = 0; i < palette.length; i++) {
			if (palette[i] != null)
				palette[i].delete();
		}
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
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

	public void shaderDrawLastIndex(boolean draw) {
		shaderProg.setUniformi("u_draw255", draw ? 1 : 0);
	}

	public ShaderProgram getShaderProgram() {
		return shaderProg;
	}
}
