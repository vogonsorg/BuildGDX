package ru.m210projects.Build.Render.ModelHandle.MDModel.MD3;

import static com.badlogic.gdx.graphics.GL20.GL_FLOAT;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT;
import static ru.m210projects.Build.Render.ModelHandle.Model.MD_ROTATE;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE0;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_COORD_ARRAY;
import static ru.m210projects.Build.Render.Types.GL10.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDSkinmap;
import ru.m210projects.Build.Render.TextureHandle.GLTile;

public class MD3ModelGL10 extends MDModel implements GLModel {

	private ShortBuffer indices;
	private FloatBuffer vertices;
	private FloatBuffer uv;

	// Current parameters
	private Vector3 cScale = new Vector3();
	private Vector3 nScale = new Vector3();
	private MD3Surface s;
	private int texunits;

	public MD3ModelGL10(DefMD3 md) {
		super(md);

		MD3Builder builder = new MD3Builder(md);
	}

	public MD3ModelGL10 setParameters(MD3Surface s, int texunits) {
		this.s = s;
		this.texunits = texunits;

		return this;
	}

	public MD3ModelGL10 setScale(Vector3 cScale, Vector3 nScale) {
		this.cScale.set(cScale);
		this.nScale.set(nScale);

		return this;
	}

	@Override
	public void render(ShaderProgram shader) {
		vertices.clear();
		for (int i = 0; i < s.numverts; i++) {
			MD3Vertice v0 = s.xyzn[cframe * s.numverts + i];
			MD3Vertice v1 = s.xyzn[nframe * s.numverts + i];

			vertices.put(v0.x * cScale.x + v1.x * nScale.x);
			vertices.put(v0.z * cScale.z + v1.z * nScale.z);
			vertices.put(v0.y * cScale.y + v1.y * nScale.y);
		}
		vertices.flip();

		indices.clear();
		for (int i = s.numtris - 1; i >= 0; i--)
			for (int j = 0; j < 3; j++)
				indices.put((short) s.tris[i][j]);
		indices.flip();

		int l = GL_TEXTURE0;
		do {
			BuildGdx.gl.glClientActiveTexture(l++);
			BuildGdx.gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			BuildGdx.gl.glTexCoordPointer(2, GL_FLOAT, 0, uv);
		} while (l <= texunits);

		BuildGdx.gl.glEnableClientState(GL_VERTEX_ARRAY);
		BuildGdx.gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
		BuildGdx.gl.glDrawElements(GL_TRIANGLES, 0, GL_UNSIGNED_SHORT, indices);

		while (texunits > GL_TEXTURE0) {
			BuildGdx.gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			BuildGdx.gl.glClientActiveTexture(texunits - 1);
			BuildGdx.gl.glActiveTexture(--texunits);
		}
		BuildGdx.gl.glDisableClientState(GL_VERTEX_ARRAY);
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
	public boolean isRotating() {
		return (flags & MD_ROTATE) != 0;
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
