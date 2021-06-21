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

import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.xdimen;
import static ru.m210projects.Build.Engine.xdimenscale;
import static ru.m210projects.Build.Engine.xyaspect;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Engine.yxaspect;
import static ru.m210projects.Build.Pragmas.mulscale;
import static ru.m210projects.Build.Pragmas.scale;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;

import ru.m210projects.Build.Architecture.BuildGdx;

public class GdxBatch {

	private Mesh mesh;

	final float[] vertices;
	int idx = 0;
	GLTexture lastTexture = null;
	float invTexWidth = 0, invTexHeight = 0;

	boolean drawing = false;

//	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
//	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL20.GL_SRC_ALPHA;
	private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
	private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
	private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;
	private ShaderProgram customShader = null;
	private boolean ownsShader;

	float color = Color.WHITE_FLOAT_BITS;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	/**
	 * Constructs a new GdxBatch with a size of 1000, one buffer, and the default
	 * shader.
	 * 
	 * @see GdxBatch#GdxBatch(int, ShaderProgram)
	 */
	public GdxBatch() {
		this(32, null);
	}

	/**
	 * Constructs a new GdxBatch. Sets the projection matrix to an orthographic
	 * projection with y-axis point upwards, x-axis point to the right and the
	 * origin being in the bottom left corner of the screen. The projection will be
	 * pixel perfect with respect to the current screen resolution.
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for
	 * uniforms for this default shader are different than the ones expect for
	 * shaders set with {@link #setShader(ShaderProgram)}. See
	 * {@link #createDefaultShader()}.
	 * 
	 * @param size          The max number of sprites in a single batch. Max of
	 *                      8191.
	 * @param defaultShader The default shader to use. This is not owned by the
	 *                      GdxBatch and must be disposed separately.
	 */
	public GdxBatch(int size, ShaderProgram defaultShader) {
		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites
		// max.
		if (size > 8191)
			throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		mesh = new Mesh(false, size * 4, size * 6,
				new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		projectionMatrix.setToOrtho(0, BuildGdx.graphics.getWidth() - 1, BuildGdx.graphics.getHeight() - 1, 0, 0, 1);

		int VERTEX_SIZE = 2 + 1 + 2;
		int SPRITE_SIZE = 4 * VERTEX_SIZE;
		vertices = new float[size * SPRITE_SIZE];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short) (j + 1);
			indices[i + 2] = (short) (j + 2);
			indices[i + 3] = (short) (j + 2);
			indices[i + 4] = (short) (j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);

		if (defaultShader == null) {
			shader = createDefaultShader();
			ownsShader = true;
		} else
			shader = defaultShader;
	}

	public void resize(int width, int height) {
		projectionMatrix.setToOrtho(0, width, height, 0, 0, 1);
	}

	/**
	 * Returns a new instance of the default shader used by GdxBatch for GL2 when no
	 * shader is specified.
	 */
	static public ShaderProgram createDefaultShader() {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "varying vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
				+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";

		String fragmentShader = "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "varying LOWP vec4 v_color;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "\n" //
				+ "uniform float cx1;\n" //
				+ "uniform float cy1;\n" //
				+ "uniform float cx2;\n" //
				+ "uniform float cy2;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "	//rotatesprite clipping\n" //
				+ "	if( gl_FragCoord.x < cx1 || gl_FragCoord.x > cx2 + 1.0\n" //
				+ "		|| gl_FragCoord.y > cy1 || gl_FragCoord.y < cy2 - 1.0 ) \n" //
				+ "		discard;\n" //
				+ "	gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
				+ "}\n"; //

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	public void begin() {
		if (drawing)
			throw new IllegalStateException("GdxBatch.end must be called before begin.");

		BuildGdx.gl20.glDepthMask(false);
		if (customShader != null)
			customShader.begin();
		else
			shader.begin();
		setupMatrices();

		drawing = true;
	}

	public void end() {
		if (!drawing)
			throw new IllegalStateException("GdxBatch.begin must be called before end.");
		if (idx > 0)
			flush();
		lastTexture = null;
		drawing = false;

		GL20 gl = BuildGdx.gl20;
		gl.glDepthMask(true);
		if (isBlendingEnabled())
			gl.glDisable(GL20.GL_BLEND);

		if (customShader != null)
			customShader.end();
		else
			shader.end();
	}

	public void setColor(float r, float g, float b, float a) {
		int intBits = (int) (255 * a) << 24 | (int) (255 * b) << 16 | (int) (255 * g) << 8 | (int) (255 * r);
		color = NumberUtils.intToFloatColor(intBits);
	}

	private float[] currClipBounds = new float[4];
	private float[] lastClipBounds = new float[4];

	public void draw(GLTexture tex, int sx, int sy, int sizx, int sizy, int xoffset, int yoffset, int angle, int z,
			int dastat, int cx1, int cy1, int cx2, int cy2) {
		this.draw(tex, sx, sy, sizx, sizy, xoffset, yoffset, 0.0f, 0.0f, sizx, sizy, angle, z, dastat, cx1, cy1, cx2,
				cy2);
	}

	public void drawFade() {
		if (!drawing)
			throw new IllegalStateException("GdxBatch.begin must be called before draw.");

		if (idx != 0)
			flush();

		currClipBounds[0] = 0;
		currClipBounds[1] = ydim;
		currClipBounds[2] = xdim;
		currClipBounds[3] = 0;

		shader.setUniformf("cx1", currClipBounds[0]);
		shader.setUniformf("cy1", currClipBounds[1]);
		shader.setUniformf("cx2", currClipBounds[2]);
		shader.setUniformf("cy2", currClipBounds[3]);

		System.arraycopy(currClipBounds, 0, lastClipBounds, 0, 4);

		float color = this.color;
		int idx = this.idx;
		vertices[idx + 0] = 0;
		vertices[idx + 1] = 0;
		vertices[idx + 2] = color;
		vertices[idx + 3] = 0;
		vertices[idx + 4] = 0;

		vertices[idx + 5] = 0;
		vertices[idx + 6] = ydim;
		vertices[idx + 7] = color;
		vertices[idx + 8] = 0;
		vertices[idx + 9] = 1;

		vertices[idx + 10] = xdim;
		vertices[idx + 11] = ydim;
		vertices[idx + 12] = color;
		vertices[idx + 13] = 1;
		vertices[idx + 14] = 1;

		vertices[idx + 15] = xdim;
		vertices[idx + 16] = 0;
		vertices[idx + 17] = color;
		vertices[idx + 18] = 1;
		vertices[idx + 19] = 0;
		this.idx = idx + 20;

		flush();
	}

	public void draw(GLTexture tex, int sx, int sy, int sizx, int sizy, int xoffset, int yoffset, float srcX,
			float srcY, float srcWidth, float srcHeight, int angle, int z, int dastat, int cx1, int cy1, int cx2,
			int cy2) {
		if (!drawing)
			throw new IllegalStateException("GdxBatch.begin must be called before draw.");

		if (tex != lastTexture)
			switchTexture(tex);
		else if (idx == vertices.length)
			flush();

		currClipBounds[0] = cx1;
		currClipBounds[1] = ydim - cy1;
		currClipBounds[2] = cx2;
		currClipBounds[3] = ydim - cy2;

		if (currClipBounds[0] != lastClipBounds[0] || currClipBounds[1] != lastClipBounds[1]
				|| currClipBounds[2] != lastClipBounds[2] || currClipBounds[3] != lastClipBounds[3]) {
			flush();

			shader.setUniformf("cx1", currClipBounds[0]);
			shader.setUniformf("cy1", currClipBounds[1]);
			shader.setUniformf("cx2", currClipBounds[2]);
			shader.setUniformf("cy2", currClipBounds[3]);

			System.arraycopy(currClipBounds, 0, lastClipBounds, 0, 4);
		}

		int ourxyaspect = xyaspect;
		if ((dastat & 2) == 0) {
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim)
				ourxyaspect = (10 << 16) / 12;
		} else {
			// dastat&2: Auto window size scaling
			int oxdim = xdim, zoomsc;
			int xdim = oxdim; // SHADOWS global

			int ouryxaspect = yxaspect;
			ourxyaspect = xyaspect;

			// screen center to s[xy], 320<<16 coords.
			int normxofs = sx - (320 << 15), normyofs = sy - (200 << 15);
			if ((dastat & 1024) == 0 && 4 * ydim <= 3 * xdim) {
				xdim = (4 * ydim) / 3;

				ouryxaspect = (12 << 16) / 10;
				ourxyaspect = (10 << 16) / 12;
			}

			// nasty hacks go here
			if ((dastat & 8) == 0) {
				int twice_midcx = (cx1 + cx2) + 2;

				// screen x center to sx1, scaled to viewport
				int scaledxofs = scale(normxofs, scale(xdimen, xdim, oxdim), 320);
				int xbord = 0;
				if ((dastat & (256 | 512)) != 0) {
					xbord = scale(oxdim - xdim, twice_midcx, oxdim);
					if ((dastat & 512) == 0)
						xbord = -xbord;
				}

				sx = ((twice_midcx + xbord) << 15) + scaledxofs;
				zoomsc = xdimenscale;
				sy = (((cy1 + cy2) + 2) << 15) + mulscale(normyofs, zoomsc, 16);
			} else {
				// If not clipping to startmosts, & auto-scaling on, as a
				// hard-coded bonus, scale to full screen instead
				sx = (xdim << 15) + scale(normxofs, xdim, 320);
				if ((dastat & 512) != 0)
					sx += (oxdim - xdim) << 16;
				else if ((dastat & 256) == 0)
					sx += (oxdim - xdim) << 15;

				zoomsc = scale(xdim, ouryxaspect, 320);
				sy = (ydim << 15) + mulscale(normyofs, zoomsc, 16);
			}

			z = mulscale(z, zoomsc, 16);
		}

		final float aspectFix = ((dastat & 2) != 0) || ((dastat & 8) == 0) ? ourxyaspect / 65536.0f : 1.0f;
		final float scale = z / 65536.0f;
		final float xoffs = xoffset * scale;
		final float yoffs = yoffset * scale;
		final float width = scale * sizx;
		final float height = scale * sizy;

		float[] vertices = this.vertices;
		final float OriginX = sx / 65536.0f;
		final float OriginY = sy / 65536.0f;
		float x1, y1, x2, y2, x3, y3, x4, y4;

		// rotate
		if (angle != 0) {
			final float rotation = 360.0f * angle / 2048.0f;
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = OriginX + (sin * yoffs - cos * xoffs) * aspectFix;
			y1 = OriginY - xoffs * sin - yoffs * cos;

			x4 = x1 + width * cos * aspectFix;
			y4 = y1 + width * sin;

			x2 = x1 - height * sin * aspectFix;
			y2 = y1 + height * cos;

			x3 = x2 + (x4 - x1);
			y3 = y2 + (y4 - y1);
		} else {
			x1 = x2 = OriginX - xoffs * aspectFix;
			y1 = y4 = OriginY - yoffs;

			x3 = x4 = x1 + width * aspectFix;
			y2 = y3 = y1 + height;
		}

		float v, u = srcX * invTexWidth;
		float v2, u2 = (srcX + srcWidth) * invTexWidth;
		if ((dastat & 4) == 0) {
			v = srcY * invTexHeight;
			v2 = (srcY + srcHeight) * invTexHeight;
		} else {
			v = (srcY + srcHeight) * invTexHeight;
			v2 = srcY * invTexHeight;
		}

		float color = this.color;
		int idx = this.idx;
		vertices[idx + 0] = x1;
		vertices[idx + 1] = y1;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x2;
		vertices[idx + 6] = y2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = x3;
		vertices[idx + 11] = y3;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = x4;
		vertices[idx + 16] = y4;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	public void flush() {
		if (idx == 0)
			return;

		int spritesInBatch = idx / 20;
		if (spritesInBatch > maxSpritesInBatch)
			maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		lastTexture.bind();
		Mesh mesh = this.mesh;
		mesh.setVertices(vertices, 0, idx);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(count);

		if (blendingDisabled) {
			BuildGdx.gl20.glDisable(GL20.GL_BLEND);
		} else {
			BuildGdx.gl20.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1)
				BuildGdx.gl20.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
		}

		mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, count);

		idx = 0;
	}

	public void disableBlending() {
		if (blendingDisabled)
			return;
		flush();
		blendingDisabled = true;
	}

	public void enableBlending() {
		if (!blendingDisabled)
			return;
		flush();
		blendingDisabled = false;
	}

	public void setBlendFunction(int srcFunc, int dstFunc) {
		setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
	}

	public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
		if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha
				&& blendDstFuncAlpha == dstFuncAlpha)
			return;
		flush();
		blendSrcFunc = srcFuncColor;
		blendDstFunc = dstFuncColor;
		blendSrcFuncAlpha = srcFuncAlpha;
		blendDstFuncAlpha = dstFuncAlpha;
	}

	public void dispose() {
		mesh.dispose();
		if (ownsShader && shader != null)
			shader.dispose();
	}

	private void setupMatrices() {
		if (customShader != null) {
			customShader.setUniformMatrix("u_projTrans", projectionMatrix);
			customShader.setUniformi("u_texture", 0);
		} else {
			shader.setUniformMatrix("u_projTrans", projectionMatrix);
			shader.setUniformi("u_texture", 0);
		}
	}

	protected void switchTexture(GLTexture texture) {
		flush();
		lastTexture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	public void setShader(ShaderProgram shader) {
		if (drawing) {
			flush();
			if (customShader != null)
				customShader.end();
			else
				this.shader.end();
		}
		customShader = shader;
		if (drawing) {
			if (customShader != null)
				customShader.begin();
			else
				this.shader.begin();
			setupMatrices();
		}
	}

	public ShaderProgram getShader() {
		if (customShader == null) {
			return shader;
		}
		return customShader;
	}

	public boolean isBlendingEnabled() {
		return !blendingDisabled;
	}

	public boolean isDrawing() {
		return drawing;
	}
}
