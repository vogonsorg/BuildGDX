package ru.m210projects.Build.Render.ModelHandle;

import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.TextureHandle.GLTile;

public interface GLModel {

	public void render(ShaderProgram shader);

	public void dispose();

	public Iterator<GLTile[]> getSkins();

	public void clearSkins();

	public Type getType();

	public boolean isRotating();

	public float getScale();

}
