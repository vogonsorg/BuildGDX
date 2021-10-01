package ru.m210projects.Build.Render.GdxRender;

import static ru.m210projects.Build.Engine.*;

import com.badlogic.gdx.graphics.Texture.TextureFilter;

import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model;
import ru.m210projects.Build.Render.ModelHandle.ModelManager;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelData;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelGL20;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelSkin;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.IndexedShader;
import ru.m210projects.Build.Render.TextureHandle.TileData;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;

public class GDXModelManager extends ModelManager {

	private GDXRenderer parent;

	public GDXModelManager(GDXRenderer parent) {
		this.parent = parent;
	}

	@Override
	public GLVoxel allocateVoxel(VoxelData vox, int voxmip, int flags) {
		return new VoxelGL20(vox, voxmip, flags) {

			@Override
			public GLTile getSkin(int pal) {
				PixelFormat fmt = parent.getTexFormat();
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
					System.out.println("Load voxskin: p" + pal + "... " + (etime / 1000000.0f) + " ms");
				}

				return texid[pal];
			}

			@Override
			public void setTextureParameters(GLTile tile, int pal, int shade, int visibility, float alpha) {
				if (tile.getPixelFormat() == TileData.PixelFormat.Pal8) {
					parent.manager.textureTransform(parent.texture_transform.idt(), 0);
					parent.manager.textureParams8(pal, shade, alpha, true);
					((IndexedShader) parent.manager.getProgram()).setVisibility((int) (-visibility / 64.0f));
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
