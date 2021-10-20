package ru.m210projects.Build.Render.ModelHandle;

import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.TextureHandle.GLTile;

public interface GLModel {

	public boolean render(ShaderProgram shader, int pal, int shade, int surfnum, int visibility, float alpha);

	public void dispose();

	public Iterator<GLTile> getSkins();

	public void clearSkins();

	public Type getType();

	public boolean isRotating();

	public boolean isTintAffected();

	public float getScale();

}
