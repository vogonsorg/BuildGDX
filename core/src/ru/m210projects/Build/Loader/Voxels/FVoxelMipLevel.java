package ru.m210projects.Build.Loader.Voxels;

import com.badlogic.gdx.math.Vector3;

public class FVoxelMipLevel {
	int			SizeX;
	int			SizeY;
	int			SizeZ;
	Vector3		Pivot;
	int[]		OffsetX;
	short[][]	OffsetXY;
	byte[]		SlabData;
	int[] 		SlabDataBgra;
	
	public FVoxelMipLevel()
	{
		Pivot = new Vector3();
	}
}
