package ru.m210projects.Build.Render.ModelHandle;

import static ru.m210projects.Build.Loader.OldModel.MD_ROTATE;

public class Model {
	public enum Type { Voxel, Md2, Md3 }

	protected int flags;
	protected float scale;
	protected final Type type;

	public Model(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public boolean isRotating() {
		return (flags & MD_ROTATE) != 0;
	}

	public float getScale() {
		return scale;
	}
}
