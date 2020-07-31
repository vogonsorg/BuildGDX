package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.GL_LUMINANCE;
import static com.badlogic.gdx.graphics.GL20.GL_RGB;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.numshades;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;

public class IndexedTexShader {

	private abstract class ShaderData extends DummyTileData {

		public ShaderData(byte[] buf, int w, int h, int bytes) {
			super(w, h);
			int len = w * h * bytes;

			data.clear();
			data.putBytes(buf, 0, len);
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
	}

	private class PaletteData extends ShaderData {
		public PaletteData(byte[] data) {
			super(data, 256, 1, 3);
		}
	}

	private class LookupData extends ShaderData {
		public LookupData(byte[] data) {
			super(data, 256, 64, 1);
		}

		@Override
		public int getGLFormat() {
			return GL_LUMINANCE;
		}
	}

	private GLTile palette;
	private GLTile palookup[];
	private ShaderProgram shaderProg;
	private TextureManager cache;
	private boolean glfog = false;
	private boolean isBinded;

	private int paletteloc;
	private int numshadesloc;
	private int visibilityloc;
	private int palookuploc;
	private int shadeloc;
	private int alphaloc;
	private int draw255loc;
	private int fogenableloc;
	private int fogstartloc;
	private int fogendloc;
	private int fogcolourloc;

	public IndexedTexShader(TextureManager cache) throws Exception {
		String fragment =
				"uniform sampler2D u_texture;" +
				"uniform sampler2D u_palette;" +
				"uniform sampler2D u_palookup;" +
				"uniform int u_shade;" +
				"uniform float u_numshades;" +
				"uniform float u_visibility;" +
				"uniform float u_alpha;" +
				"uniform int u_draw255;" +
				"uniform int u_fogenable;" +
				"uniform vec4 u_fogcolour;" +
				"uniform float u_fogstart;" +
				"uniform float u_fogend;" +
				"varying float v_dist;" +
				"float fog(float dist) {" +
				"	if(u_fogenable == 1)" +
				"		return clamp(1.0 - (u_fogend - dist) / (u_fogend - u_fogstart), 0.0, 1.0);" +
				"	else return 0.0;" +
				"}" +
				"float getpalookup(int dashade) {" +
				"	float davis = v_dist * u_visibility;" +
				"   if(u_fogenable != -1) davis = u_visibility / 64.0;" +
				"	float shade = (min(max(float(dashade) + davis, 0.0), u_numshades - 1.0));" +
				"	return shade / 64.0;" +
				"}" +
				"void main()" +
				"{" +
				"	float fi = texture2D(u_texture, gl_TexCoord[0].xy).r;" +
				"	if(fi == 1.0)" +
				"	{" +
				"		if(u_draw255 == 0) discard;" +
				"		fi -= 0.5 / 256.0;" +
				"	}" +
				"	float index = texture2D(u_palookup, vec2(fi, getpalookup(u_shade))).r;" +
				"	if(index == 1.0) index -= 0.5 / 256.0;" +
				"	vec4 src = vec4(texture2D(u_palette, vec2(index, 0.0)).rgb, u_alpha);" +
				"   if(u_fogenable == -1) " +
				"		gl_FragColor = src; " +
				"	else gl_FragColor = mix(src, u_fogcolour, fog(v_dist));" +
				"}";

		String vertex =
				" varying float v_dist;"
				+ "void main()"
				+ "{"
				+ "	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; /*ftransform();*/"
				+ "	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;"
				+ " v_dist = gl_ClipVertex.z / gl_ClipVertex.w;"
				+ "	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;"
				+ "}"
				;

		shaderProg = new ShaderProgram(vertex, fragment);
		if (!shaderProg.isCompiled())
			throw new Exception("Shader compile error: " + shaderProg.getLog());

		this.palookup = new GLTile[MAXPALOOKUPS];
		this.cache = cache;

		this.paletteloc = shaderProg.getUniformLocation("u_palette");
		this.numshadesloc = shaderProg.getUniformLocation("u_numshades");
		this.visibilityloc = shaderProg.getUniformLocation("u_visibility");
		this.palookuploc = shaderProg.getUniformLocation("u_palookup");
		this.shadeloc = shaderProg.getUniformLocation("u_shade");
		this.alphaloc = shaderProg.getUniformLocation("u_alpha");
		this.draw255loc = shaderProg.getUniformLocation("u_draw255");
		this.fogenableloc = shaderProg.getUniformLocation("u_fogenable");
		this.fogstartloc = shaderProg.getUniformLocation("u_fogstart");
		this.fogendloc = shaderProg.getUniformLocation("u_fogend");
		this.fogcolourloc = shaderProg.getUniformLocation("u_fogcolour");
	}

	public void dispose() {
		for (int i = 0; i < MAXPALOOKUPS; i++)
			if (palookup[i] != null)
				palookup[i].delete();
		if(palette != null)
			palette.delete();

		shaderProg.dispose();
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void changePalette(byte[] pal) {
		TileData dat = new PaletteData(pal);

		if(palette != null)
			palette.update(dat, false);
		else palette = cache.newTile(dat, 0, false);

		palette.unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
	}

	protected GLTile getpalookup(int pal) {
		if(palookup[pal] == null || palookup[pal].isInvalidated()) {
			if(Engine.palookup[pal] == null)
				return palookup[0];

			TileData dat = new LookupData(Engine.palookup[pal]);

			if(palookup[pal] != null) {
				palookup[pal].setInvalidated(false);
				palookup[pal].update(dat, false);
			} else palookup[pal] = cache.newTile(dat, 0, false);

			palookup[pal].unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
		}

		return palookup[pal];
	}

	public void invalidatepalookup(int pal) {
		if(palookup[pal] != null)
			palookup[pal].setInvalidated(true);
	}

	public void bind() {
		shaderProg.begin();
		isBinded = true;
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void unbind() {
		shaderProg.end();
		isBinded = false;
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public boolean isBinded() {
		return isBinded;
	}

	public void setShaderParams(int pal, int shade) {
		shaderProg.setUniformf(numshadesloc, numshades);

		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		palette.bind();
		shaderProg.setUniformi(paletteloc, 1);

		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
		getpalookup(pal).bind();
		shaderProg.setUniformi(palookuploc, 2);

		shaderProg.setUniformi(shadeloc, shade);
		BuildGdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void shaderTransparent(float alpha) {
		shaderProg.setUniformf(alphaloc, alpha);
	}

	public void shaderDrawLastIndex(boolean draw) {
		shaderProg.setUniformi(draw255loc, draw ? 1 : 0);
	}

	public void setVisibility(int vis) {
		shaderProg.setUniformf(visibilityloc, vis / 64.0f);
	}

	public void setFogParams(boolean enable, float start, float end, float[] fogcolor) {
		boolean binded = isBinded();
		if(!binded) bind();
		if(!glfog) {
			shaderProg.setUniformi(fogenableloc, -1);
		} else {
			shaderProg.setUniformi(fogenableloc, enable ? 1 : 0);
			if(enable) {
				shaderProg.setUniformf(fogstartloc, start);
				shaderProg.setUniformf(fogendloc, end);
				shaderProg.setUniform4fv(fogcolourloc, fogcolor, 0, 4);
			}
		}
		if(!binded) unbind();
	}

	public ShaderProgram getShaderProgram() { //this necessary for mesh.draw();
		return shaderProg;
	}
}
