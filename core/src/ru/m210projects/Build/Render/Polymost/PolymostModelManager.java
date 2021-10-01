package ru.m210projects.Build.Render.Polymost;

import static ru.m210projects.Build.Engine.palookup;

import com.badlogic.gdx.graphics.Texture.TextureFilter;

import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model;
import ru.m210projects.Build.Render.ModelHandle.ModelManager;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelData;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelGL10;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelSkin;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;

public class PolymostModelManager extends ModelManager {

	protected Polymost parent;

	public PolymostModelManager(Polymost parent) {
		this.parent = parent;
	}

	@Override
	public GLVoxel allocateVoxel(VoxelData vox, int voxmip, int flags) {
		return new VoxelGL10(vox, voxmip, flags, true) {

			@Override
			public GLTile getSkin(int pal) {
				PixelFormat fmt = parent.getTextureFormat();
				if (palookup[pal] == null || fmt == PixelFormat.Pal8)
					pal = 0;

				if (texid[pal] == null) {
					long startticks = System.nanoTime();
					TileData dat = new VoxelSkin(fmt, skinData, pal);
					GLTile dst = parent.textureCache.newTile(dat, pal, false);

					dst.unsafeSetFilter(TextureFilter.Nearest, TextureFilter.Nearest, true);
					dst.unsafeSetAnisotropicFilter(1, true);
					texid[pal] = dst;
					long etime = System.nanoTime() - startticks;
					System.out.println("Load voxskin: pal" + pal + " for tile " + getTile(this) + "... "
							+ (etime / 1000000.0f) + " ms");
				}

				return texid[pal];
			}

			@Override
			public void setTextureParameters(GLTile tile, int pal, int shade, int visibility, float alpha) {
				if (tile.getPixelFormat() == PixelFormat.Pal8) {
					parent.getShader().setTextureParams(pal, shade);
					parent.getShader().setDrawLastIndex(true);
					parent.getShader().setTransparent(alpha);
					parent.getShader().setVisibility(visibility);
				}
			}
		};
	}

	@Override
	public GLModel allocateModel(Model modelInfo) {
		// TODO Auto-generated method stub
		return null;
	}

}
