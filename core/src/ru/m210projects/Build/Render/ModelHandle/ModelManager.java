package ru.m210projects.Build.Render.ModelHandle;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.util.Iterator;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MD3.DefMD3;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MD3.MD3ModelGL10;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelGL10;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelGL20;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Render.Types.Tile2model;
import ru.m210projects.Build.Script.ModelsInfo;

public class ModelManager {

	public enum ModelType {
		GL10, GL20
	};

	protected ModelsInfo mdInfo;
	protected ModelType type;
	protected GLModel[] models = new GLModel[MAXTILES];

	public ModelManager setModelsInfo(ModelsInfo mdInfo, ModelType type) {
		this.dispose();

		this.mdInfo = mdInfo;
		this.type = type;
		return this;
	}

	public Iterator<GLTile[]> getSkins(int tile) {
		GLModel model = models[tile];
		if (model != null) {
			return model.getSkins();
		}

		return null;
	}

	public void setTextureFilter(GLFilter filter, int anisotropy) {
		if (mdInfo == null)
			return;

		for (int i = MAXTILES - 1; i >= 0; i--) {
			GLModel model = models[i];
			if (model == null || model.getType() == Type.Voxel)
				continue;

			Iterator<GLTile[]> it = model.getSkins();
			while (it.hasNext()) {
				for (GLTile tex : it.next()) {
					if (tex == null)
						continue;

					tex.bind();
					tex.setupTextureFilter(filter, anisotropy);
				}
			}
		}
	}

	public void clearSkins(int tile, boolean bit8only) {
		if (models[tile] != null && !bit8only)
			models[tile].clearSkins();
	}

	public GLModel getModel(int tile) {
		if (mdInfo == null)
			return null;

		Model model = null;
		if ((model = mdInfo.getModel(tile)) != null && model.getType() != Type.Voxel) {
			if (models[tile] != null)
				return models[tile];

			try {
				GLModel out = null;
				long startticks = System.nanoTime();
				Type type = model.getType();

				switch (type) {
				case Md2:

					break;
				case Md3:
					DefMD3 md3 = (DefMD3) model;
					if (this.type == ModelType.GL10)
						out = new MD3ModelGL10(md3);
					else if (this.type == ModelType.GL20) {
						// out = new VoxelGL20(model.getData(), 0, model.getFlags());
					}
					break;
				default:
					break;
				}

				long etime = System.nanoTime() - startticks;
				System.out.println("Load " + type + " model: " + tile + "... " + (etime / 1000000.0f) + " ms");

				return models[tile] = out;
			} catch (Exception e) {
				e.printStackTrace();
				Console.Println("Removing model of tile " + tile + " due to errors.", OSDTEXT_RED);
				mdInfo.removeModelInfo(model);
			}
		}

		return null;
	}

	public GLModel getVoxel(int tile) {
		if (mdInfo == null)
			return null;

		DefVoxel model = null;
		if ((model = mdInfo.getVoxel(tile)) != null) {
			if (models[tile] != null && models[tile].getType() == Type.Voxel)
				return models[tile];

			if (models[tile] != null)
				return null;

			GLModel out = null;
			long startticks = System.nanoTime();
			if (type == ModelType.GL10)
				out = new VoxelGL10(model.getData(), 0, model.getFlags(), true);
			else if (type == ModelType.GL20)
				out = new VoxelGL20(model.getData(), 0, model.getFlags());

			long etime = System.nanoTime() - startticks;
			System.out.println("Load voxel model: " + tile + "... " + (etime / 1000000.0f) + " ms");

			return models[tile] = out;
		}

		return null;
	}

	public void dispose() {
		for (int i = MAXTILES - 1; i >= 0; i--) {
			if (models[i] != null)
				models[i].dispose();
			models[i] = null;
		}
		mdInfo = null;
	}
}
