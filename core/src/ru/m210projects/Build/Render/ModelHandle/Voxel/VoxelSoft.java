package ru.m210projects.Build.Render.ModelHandle.Voxel;

import ru.m210projects.Build.Render.ModelHandle.Model;

public class VoxelSoft extends Model {

	protected VoxelData vox;
	public VoxelSoft(VoxelData vox) {
		super(Type.Voxel);

		this.vox = vox;
	}

}
