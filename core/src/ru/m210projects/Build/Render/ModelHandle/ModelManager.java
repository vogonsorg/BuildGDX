package ru.m210projects.Build.Render.ModelHandle;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;

import java.util.Iterator;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.ModelHandle.Voxel.VoxelData;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Script.ModelsInfo;

public abstract class ModelManager {

	protected ModelsInfo mdInfo;
	protected GLModel[] models = new GLModel[MAXTILES];

	public ModelManager setModelsInfo(ModelsInfo mdInfo) {
		this.dispose();

		this.mdInfo = mdInfo;
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

	public boolean hasModelInfo(int tile) {
		if (mdInfo == null)
			return false;

		return mdInfo.getModelInfo(tile) != null;
	}

	public GLModel getModel(int tile) {
		if (mdInfo == null)
			return null;

		Model model = null;
		if ((model = mdInfo.getModelInfo(tile)) != null && model.getType() != Type.Voxel) {
			if (models[tile] != null)
				return models[tile];

			try {
				long startticks = System.nanoTime();
				GLModel out = allocateModel(model);
				long etime = System.nanoTime() - startticks;
				System.out
						.println("Load " + model.getType() + " model: " + tile + "... " + (etime / 1000000.0f) + " ms");

				if (out != null)
					return models[tile] = out;
			} catch (Exception e) {
				e.printStackTrace();
			}
			Console.Println("Removing model of tile " + tile + " due to errors.", OSDTEXT_RED);
			mdInfo.removeModelInfo(model);
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

			long startticks = System.nanoTime();
			GLModel out = allocateVoxel(model.getData(), 0, model.getFlags());
			long etime = System.nanoTime() - startticks;
			System.out.println("Load voxel model: " + tile + "... " + (etime / 1000000.0f) + " ms");

			if (out != null)
				return models[tile] = out;
			else {
				Console.Println("Removing voxel of tile " + tile + " due to errors.", OSDTEXT_RED);
				mdInfo.removeModelInfo(model);
			}
		}

		return null;
	}

	public abstract GLVoxel allocateVoxel(VoxelData vox, int voxmip, int flags);

	public abstract GLModel allocateModel(Model modelInfo);

	public void dispose() {
		for (int i = MAXTILES - 1; i >= 0; i--) {
			if (models[i] != null)
				models[i].dispose();
			models[i] = null;
		}
		mdInfo = null;
	}

	public int getTile(GLModel model) {
		for (int i = MAXTILES - 1; i >= 0; i--) {
			if (models[i] == null)
				continue;

			if (models[i] == model) {
				return i;
			}
		}

		return -1;
	}
}
