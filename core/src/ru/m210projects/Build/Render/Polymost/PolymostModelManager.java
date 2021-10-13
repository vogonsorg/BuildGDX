package ru.m210projects.Build.Render.Polymost;

import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static ru.m210projects.Build.Engine.DETAILPAL;
import static ru.m210projects.Build.Engine.GLOWPAL;
import static ru.m210projects.Build.Engine.MAXPALOOKUPS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.RESERVEDPALS;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Render.Types.GL10.GL_MODELVIEW;
import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE0;

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
import ru.m210projects.Build.Render.TextureHandle.Hicreplctyp;
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
						if (palnum == DETAILPAL || palnum == GLOWPAL)
							texidx.setHighTile(new Hicreplctyp(palnum));
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

				@Override
				public int bindSkin(int pal, int skinnum, int surfnum) {
					int effectnum = parent.defs.texInfo.getPaletteEffect(pal);

					int texunits = -1;
					GLTile texid = getSkin(pal, skinnum, surfnum, effectnum);
					if (texid != null) {
						parent.bind(texid);

						texunits = GL_TEXTURE0;
//						if (Console.Geti("r_detailmapping") != 0) { XXX Doesn't work
//							if ((texid = getSkin(DETAILPAL, skinnum, surfnum, effectnum)) != null) {
//								BuildGdx.gl.glActiveTexture(++texunits);
//								BuildGdx.gl.glEnable(GL_TEXTURE_2D);
//								parent.setupTextureDetail(texid);
//								MDSkinmap sk = getSkin(DETAILPAL, skinnum, surfnum);
//								if (sk != null) {
//									float f = sk.param;
//									BuildGdx.gl.glMatrixMode(GL_TEXTURE);
//									BuildGdx.gl.glLoadIdentity();
//									BuildGdx.gl.glScalef(f, f, 1.0f);
//									BuildGdx.gl.glMatrixMode(GL_MODELVIEW);
//								}
//							}
//						}
//
//						if (Console.Geti("r_glowmapping") != 0) {
//							if ((texid = getSkin(GLOWPAL, skinnum, surfnum, effectnum)) != null) {
//								BuildGdx.gl.glActiveTexture(++texunits);
//								BuildGdx.gl.glEnable(GL_TEXTURE_2D);
//								parent.setupTextureGlow(texid);
//							}
//						}
					}

					return texunits;
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
