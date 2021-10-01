package ru.m210projects.Build.Render.ModelHandle;

import ru.m210projects.Build.Render.TextureHandle.GLTile;

public interface SkinProvider {

	public GLTile getSkin(GLModel md, int pal, int skinnum, int surface);

	public void setupTextureDetail(GLTile detail);

	public void setupTextureGlow(GLTile detail);

}
