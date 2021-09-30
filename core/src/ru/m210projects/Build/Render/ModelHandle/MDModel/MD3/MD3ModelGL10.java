package ru.m210projects.Build.Render.ModelHandle.MDModel.MD3;

import java.util.Iterator;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.Model.Type;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;
import ru.m210projects.Build.Render.TextureHandle.GLTile;

public class MD3ModelGL10 extends MDModel implements GLModel {

	public MD3ModelGL10(DefMD3 m, Type type) {
		super("", Type.Md3);


	}

	@Override
	public void render(ShaderProgram shader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<GLTile[]> getSkins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearSkins() {
		// TODO Auto-generated method stub

	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRotating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getScale() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFrameIndex(String framename) {
		// TODO Auto-generated method stub
		return 0;
	}

}
