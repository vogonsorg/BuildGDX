package ru.m210projects.Build.Render.ModelHandle;

import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Render.TextureHandle.GLTile;

public abstract class GLModel extends Model {

	public GLModel(Type type) {
		super(type);
	}

	public abstract void render(ShaderProgram shader);

	public abstract void dispose();

	public abstract Iterator<GLTile[]> getSkins();

	public abstract void clearSkins();

}
