package ru.m210projects.Build.Render.ModelHandle.Voxel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class VoxelGL20 extends GLVoxel {

	private Mesh mesh;

	public VoxelGL20(VoxelData vox, int voxmip, int flags) {
		super(flags);

		VoxelBuilder builder = new VoxelBuilder(vox, voxmip);
		float[] vertices = builder.getVertices();
		short[] indices = builder.getIndices();

		this.xsiz = builder.xsiz;
		this.ysiz = builder.ysiz;
		this.zsiz = builder.zsiz;

		this.xpiv = vox.xpiv[voxmip] / 256.0f;
		this.ypiv = vox.ypiv[voxmip] / 256.0f;
		this.zpiv = vox.zpiv[voxmip] / 256.0f;

		int size = builder.getVertexSize();
		mesh = new Mesh(true, vertices.length / size, indices.length, builder.getAttributes());
		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		skinData = builder.getTexture();
	}

	@Override
	public void render(ShaderProgram shader) {
		mesh.render(shader, GL20.GL_TRIANGLES);
	}

	@Override
	public void dispose() {
		mesh.dispose();
		clearSkins();
	}
}
