package ru.m210projects.Build.Loader.Voxels;

import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

public class FVoxel {

	int MAXVOXMIPS = 5;
	int NumMips;
	int VoxelIndex;
	byte[] Palette;
	FVoxelMipLevel[] Mips; 
	
	final static int vertexSize = 9; //pos * 3 + tex * 2 + unpackedColor * 4
	FloatArray vertices;
	ShortArray indicies;
	
	public FVoxel(String filename)
	{
		Mips = new FVoxelMipLevel[MAXVOXMIPS];
		for(int i = 0; i < MAXVOXMIPS; i++)
			Mips[i] = new FVoxelMipLevel();
		
		vertices = new FloatArray();
		indicies = new ShortArray();
		
		R_LoadKVX(filename);

		FVoxelMipLevel mip = Mips[0];
		for (int x = 0; x < mip.SizeX; x++)
		{
			for (int y = 0; y < mip.SizeY; y++)
			{
				int voxptr = mip.OffsetX[x] + mip.OffsetXY[x][y];
				int voxend = mip.OffsetX[x] + mip.OffsetXY[x][y + 1];

				while(voxptr < voxend)
				{
					MakeSlabPolys(x, y, mip, voxptr);
					int zleng = mip.SlabData[voxptr + 1];
					voxptr += zleng + 3; //3 - slab header
				}
			}
		}
	}
	
	public void MakeSlabPolys(int x, int y, FVoxelMipLevel mip, int voxptr)
	{
		int zleng = mip.SlabData[voxptr + 1];
		int ztop = mip.SlabData[voxptr];
		int cull = mip.SlabData[voxptr + 2];
		int col = voxptr + 3;
		
		if ((cull & 16) != 0)
			AddFace(x, y, ztop, x+1, y, ztop, x, y+1, ztop, x+1, y+1, ztop, mip.SlabData[col]);

		int z = ztop;
		while (z < ztop + zleng)
		{
			int c = 0;
			while (z + c < ztop + zleng && mip.SlabData[col + c] == mip.SlabData[col]) c++;
			
			if ((cull & 1) != 0)
				AddFace(x, y, z, x, y+1, z, x, y, z+c, x, y+1, z+c, mip.SlabData[col]);
			if ((cull & 2) != 0)
				AddFace(x+1, y+1, z, x+1, y, z, x+1, y+1, z+c, x+1, y, z+c, mip.SlabData[col]);
			if ((cull & 4) != 0)
				AddFace(x+1, y, z, x, y, z, x+1, y, z+c, x, y, z+c, mip.SlabData[col]);
			if ((cull & 8) != 0)
				AddFace(x, y+1, z, x+1, y+1, z, x, y+1, z+c, x+1, y+1, z+c, mip.SlabData[col]);
			z += c;
			col += c;
		}
		if ((cull & 32) != 0)
		{
			z = ztop + zleng - 1;
			AddFace(x+1, y, z+1, x, y, z+1, x+1, y+1, z+1, x, y+1, z+1, mip.SlabData[col + zleng-1]);
		}
	}
	
	public void AddFace(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int x4, int y4, int z4, int col)
	{
		float PivotX = Mips[0].Pivot.x;
		float PivotY = Mips[0].Pivot.y;
		float PivotZ = Mips[0].Pivot.z;
		col &= 0xFF;
		
		float u = (((col & 15) * 255 / 16) + 7) / 255.f;
		float v = (((col / 16) * 255 / 16) + 7) / 255.f;
		float x, y, z; 
		
		int vertexOffset = vertices.size / vertexSize;
		
		x =  x1 - PivotX;
		z = -y1 + PivotY;
		y = -z1 + PivotZ;
		vertices.addAll(x, y, z, u, v, 1, 1, 1, 1);
		x =  x2 - PivotX;
		z = -y2 + PivotY;
		y = -z2 + PivotZ;
		vertices.addAll(x, y, z, u, v, 1, 1, 1, 1);
		x =  x4 - PivotX;
		z = -y4 + PivotY;
		y = -z4 + PivotZ;
		vertices.addAll(x, y, z, u, v, 1, 1, 1, 1);
		x =  x3 - PivotX;
		z = -y3 + PivotY;
		y = -z3 + PivotZ;
		vertices.addAll(x, y, z, u, v, 1, 1, 1, 1);
		
		indicies.addAll(new short[] {(short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset) });
	}

	public void R_LoadKVX(String filename)
	{
		int mip, maxmipsize, i, j, n;

		ByteBuffer bb = kGetBuffer(filename, 0);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	int voxelsize = bb.capacity();
    	
		for (mip = 0, maxmipsize = voxelsize - 768 - 4;
				 mip < MAXVOXMIPS;
				 mip++)
		{
			int numbytes = bb.getInt();
			if (numbytes > maxmipsize || numbytes < 24)
				break;
			
			FVoxelMipLevel mipl = Mips[mip];

			// Load header data.
			mipl.SizeX = bb.getInt();
			mipl.SizeY = bb.getInt();
			mipl.SizeZ = bb.getInt();
			mipl.Pivot.x = bb.getInt() / 256.0f;
			mipl.Pivot.y = bb.getInt() / 256.0f;
			mipl.Pivot.z = bb.getInt() / 256.0f;
			
			// How much space do we have for voxdata?
			int offsetsize = 4 * (mipl.SizeX + 1) + 2 * (mipl.SizeX * (mipl.SizeY + 1));
			int voxdatasize = numbytes - 24 - offsetsize;

			if (voxdatasize < 0)
			{ // Clearly, not enough.
				break;
			}
			if (voxdatasize != 0)
			{	// This mip level is not empty.
				// Allocate slab data space.
				mipl.OffsetX = new int[mipl.SizeX + 1];
				mipl.OffsetXY = new short[mipl.SizeX][mipl.SizeY + 1];
				mipl.SlabData = new byte[voxdatasize];
				
				// Load x offsets.
				for (i = 0, n = mipl.SizeX; i <= n; ++i)
				{
					// The X offsets stored in the KVX file are relative to the start of the
					// X offsets array. Make them relative to voxdata instead.
					mipl.OffsetX[i] = bb.getInt() - offsetsize;
				}
				
				// The first X offset must be 0 (since we subtracted offsetsize), according to the spec:
				//		NOTE: xoffset[0] = (xsiz+1)*4 + xsiz*(ysiz+1)*2 (ALWAYS)
				if (mipl.OffsetX[0] != 0) 
					break;
				
				// And the final X offset must point just past the end of the voxdata.
				if (mipl.OffsetX[mipl.SizeX] != voxdatasize) 
					break;

				// Load xy offsets.
				
				for (i = 0; i < mipl.SizeX; ++i)
				{
					for (j = 0; j <= mipl.SizeY; ++j)
						mipl.OffsetXY[i][j] = bb.getShort();
				}

				// Ensure all offsets are within bounds.
				for (i = 0; i < mipl.SizeX; ++i)
				{
					int xoff = mipl.OffsetX[i];
					for (j = 0; j < mipl.SizeY; ++j)
					{
						int yoff = mipl.OffsetXY[i][j];
						if ((xoff + yoff) > voxdatasize)
							return;
					}
				}
				
				// Record slab location for the end.
				bb.get(mipl.SlabData);
			}
			maxmipsize -= numbytes + 4;
		}
		
		// Did we get any mip levels, and if so, does the last one leave just
		// enough room for the palette after it?
		if (mip == 0 || bb.position() != voxelsize - 768)
		{
			return;
		}
		
		// Do not count empty mips at the end.
		for (; mip > 0; --mip)
		{
			if (Mips[mip - 1].SlabData != null)
				break;
		}
		NumMips = mip;
		
		// Fix pivot data for submips, since some tools seem to like to just center these.
		for (i = 1; i < mip; ++i)
		{
			Mips[i].Pivot.x = Mips[i - 1].Pivot.x / 2;
			Mips[i].Pivot.y = Mips[i - 1].Pivot.y / 2;
			Mips[i].Pivot.z = Mips[i - 1].Pivot.z / 2;
		}
		
		Palette = new byte[768];
		bb.get(Palette);
	}
}
