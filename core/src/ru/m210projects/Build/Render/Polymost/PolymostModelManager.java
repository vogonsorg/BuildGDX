package ru.m210projects.Build.Render.Polymost;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.palookup;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model;
import ru.m210projects.Build.Render.ModelHandle.ModelManager;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDSkinmap;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MD3.DefMD3;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MD3.MD3ModelGL10;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelData;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelGL10;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelSkin;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.PixmapTileData;
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
		switch (modelInfo.getType()) {
		case Md3:
			return new MD3ModelGL10((DefMD3) modelInfo) {

				@Override
				public void bindSkin(GLTile skin) {
					parent.bind(skin);
				}

				@Override
				public void setupTextureDetail(GLTile detail) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setupTextureGlow(GLTile detail) {
					// TODO Auto-generated method stub

				}

				@Override
				public GLTile loadTexture(String skinfile, int palnum, int effectnum) {
					// possibly fetch an already loaded multitexture :_)
					if (palnum >= (MAXPALOOKUPS - RESERVEDPALS)) {
						for (int i = MAXTILES - 1; i >= 0; i--) {
							GLModel m = models[i];
							if (m == null || !(m instanceof MDModel))
								continue;

							MDModel mi = (MDModel) m;
							for (MDSkinmap skzero = mi.skinmap; skzero != null; skzero = skzero.next)
								if (skzero.fn.equalsIgnoreCase(skinfile) && skzero.texid[effectnum] != null)
									return skzero.texid[effectnum];
						}
					}

					Resource res = BuildGdx.cache.open(skinfile, 0);
					if (res == null) {
						Console.Println("Skin " + skinfile + " not found.", Console.OSDTEXT_YELLOW);
						return null;
					}

					GLTile texidx;
//					startticks = System.currentTimeMillis();
					try {
						byte[] data = res.getBytes();
						Pixmap pix = new Pixmap(data, 0, data.length);
						texidx = parent.textureCache.newTile(new PixmapTileData(pix, true, 0), 0, true);
						usesalpha = true;
					} catch (Exception e) {
						Console.Println("Couldn't load file: " + skinfile, Console.OSDTEXT_YELLOW);
						return null;
					} finally {
						res.close();
					}
					texidx.setupTextureWrap(TextureWrap.Repeat);

//					long etime = System.currentTimeMillis() - startticks;
//					System.out.println("Load skin: p" + pal + "-e" + defs.texInfo.getPaletteEffect(pal) + " \"" + skinfile
//							+ "\"... " + etime + " ms");

					return texidx;
				}
			};
		case Md2:
			break;
		default:
			return null;
		}

		return null;
	}

}
