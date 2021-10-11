package ru.m210projects.Build.Render.ModelHandle.MDModel.MD3;

import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_FLOAT;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT;
import static ru.m210projects.Build.Engine.DETAILPAL;
import static ru.m210projects.Build.Engine.GLOWPAL;
import static ru.m210projects.Build.Render.Types.GL10.GL_ALPHA_TEST;
import static ru.m210projects.Build.Render.Types.GL10.GL_MODELVIEW;
import static ru.m210projects.Build.Render.Types.GL10.GL_RGB_SCALE;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE0;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_COORD_ARRAY;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_ENV;
import static ru.m210projects.Build.Render.Types.GL10.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.CRC32;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDSkinmap;
import ru.m210projects.Build.Render.TextureHandle.GLTile;

public abstract class MD3ModelGL10 extends MDModel {

	private ShortBuffer indices;
	private FloatBuffer vertices;

	private final MD3Surface[] surfaces;
	private final int numSurfaces;

	private Vector3 cScale = new Vector3(1, 1, 1);
	private Vector3 nScale = new Vector3(1, 1, 1);

	public MD3ModelGL10(DefMD3 md) {
		super(md);

		MD3Builder builder = new MD3Builder(md);

		this.surfaces = builder.surfaces;
		this.numSurfaces = builder.head.numSurfaces;

		int maxtris = 0;
		int maxverts = 0;
		for (int i = 0; i < this.numSurfaces; i++) {
			MD3Surface surf = surfaces[i];
			maxtris = Math.max(maxtris, surf.numtris);
			maxverts = Math.max(maxtris, surf.numverts);
		}

		this.indices = BufferUtils.newShortBuffer(maxtris * 3);
		this.vertices = BufferUtils.newFloatBuffer(maxverts * 3);
	}

	public abstract void setupTextureDetail(GLTile detail);

	public abstract void setupTextureGlow(GLTile detail);

	public MD3ModelGL10 setScale(Vector3 cScale, Vector3 nScale) {
		this.cScale.set(cScale);
		this.nScale.set(nScale);

		return this;
	}

	@Override
	public boolean render(ShaderProgram shader, int pal, int shade, int skinnum, int effectnum, int visibility,
			float alpha) {
		boolean isRendered = false;

		for (int surfi = 0; surfi < numSurfaces; surfi++) {
			MD3Surface s = surfaces[surfi];

			vertices.clear();
			for (int i = 0; i < s.numverts; i++) {
				MD3Vertice v0 = s.xyzn[cframe * s.numverts + i];
				MD3Vertice v1 = s.xyzn[nframe * s.numverts + i];

				vertices.put(v0.x * cScale.x + v1.x * nScale.x);
				vertices.put(v0.z * cScale.z + v1.z * nScale.z);
				vertices.put(v0.y * cScale.y + v1.y * nScale.y);
			}
			vertices.flip();

			GLTile texid = getSkin(pal, skinnum, surfi, effectnum);
			if (texid != null) {
				texid.bind();

				if (Console.Geti("r_detailmapping") != 0)
					texid = getSkin(DETAILPAL, skinnum, surfi, effectnum);
				else
					texid = null;

				int texunits = GL_TEXTURE0;
				if (texid != null) {
					BuildGdx.gl.glActiveTexture(++texunits);
					BuildGdx.gl.glEnable(GL_TEXTURE_2D);
					setupTextureDetail(texid);

					MDSkinmap sk = getSkin(DETAILPAL, skinnum, surfi);
					if (sk != null) {
						float f = sk.param;
						BuildGdx.gl.glMatrixMode(GL_TEXTURE);
						BuildGdx.gl.glLoadIdentity();
						BuildGdx.gl.glScalef(f, f, 1.0f);
						BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
					}
				}

				if (Console.Geti("r_glowmapping") != 0)
					texid = getSkin(GLOWPAL, skinnum, surfi, effectnum);
				else
					texid = null;

				if (texid != null) {
					BuildGdx.gl.glActiveTexture(++texunits);
					BuildGdx.gl.glEnable(GL_TEXTURE_2D);
					setupTextureGlow(texid);
				}

				indices.clear();
				for (int i = s.numtris - 1; i >= 0; i--)
					for (int j = 0; j < 3; j++)
						indices.put((short) s.tris[i][j]);
				indices.flip();

				int l = GL_TEXTURE0;
				do {
					BuildGdx.gl.glClientActiveTexture(l++);
					BuildGdx.gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
					BuildGdx.gl.glTexCoordPointer(2, GL_FLOAT, 0, s.uv);
				} while (l <= texunits);

				BuildGdx.gl.glEnableClientState(GL_VERTEX_ARRAY);
				BuildGdx.gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
				BuildGdx.gl.glDrawElements(GL_TRIANGLES, 0, GL_UNSIGNED_SHORT, indices);

				while (texunits > GL_TEXTURE0) {
					BuildGdx.gl.glMatrixMode(GL_TEXTURE);
					BuildGdx.gl.glLoadIdentity();
					BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
					BuildGdx.gl.glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, 1.0f);
					BuildGdx.gl.glDisable(GL_TEXTURE_2D);

					BuildGdx.gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
					BuildGdx.gl.glClientActiveTexture(texunits - 1);

					BuildGdx.gl.glActiveTexture(--texunits);
				}
				BuildGdx.gl.glDisableClientState(GL_VERTEX_ARRAY);
				isRendered = true;
			} else
				break;
		}

		if (usesalpha)
			BuildGdx.gl.glDisable(GL_ALPHA_TEST);
		BuildGdx.gl.glDisable(GL_CULL_FACE);

		return isRendered;
	}

	@Override
	public void dispose() {
		clearSkins();
	}

	@Override
	public Iterator<GLTile[]> getSkins() {
		Iterator<GLTile[]> it = new Iterator<GLTile[]>() {
			private MDSkinmap current = skinmap;

			@Override
			public boolean hasNext() {
				return current != null && current.next != null;
			}

			@Override
			public GLTile[] next() {
				MDSkinmap sk = current;
				current = sk.next;
				return sk.texid;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	@Override
	public void clearSkins() {
		for (MDSkinmap sk = skinmap; sk != null; sk = sk.next) {
			for (int j = 0; j < sk.texid.length; j++) {
				GLTile tex = sk.texid[j];
				if (tex == null)
					continue;

				tex.delete();
				sk.texid[j] = null;
			}
		}
	}

	@Override
	public float getScale() {
		return 0.01f;
	}

	@Override
	public Type getType() {
		return Type.Md3;
	}

}
