package ru.m210projects.Build.Render.Types;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_2D;

public class BTexture extends GLTexture {

	
	TextureData data;
	final static Map<Application, Array<BTexture>> managedTextures = new HashMap<Application, Array<BTexture>>();
	
	public BTexture (Pixmap pixmap, boolean useMipMaps) {
		super(GL10.GL_TEXTURE_2D, createGLHandle());
		
		data = new PixmapTextureData(pixmap, null, useMipMaps, false);
		load(data);
		if (data.isManaged()) addManagedTexture(Gdx.app, this);
	}
	
	public BTexture() {
		super(GL_TEXTURE_2D);
	}
	
	private static void addManagedTexture (Application app, BTexture texture) {
		Array<BTexture> managedTextureArray = managedTextures.get(app);
		if (managedTextureArray == null) managedTextureArray = new Array<BTexture>();
		managedTextureArray.add(texture);
		managedTextures.put(app, managedTextureArray);
	}
	
	public void load (TextureData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;

		if (!data.isPrepared()) data.prepare();

		bind();
		uploadImageData(GL10.GL_TEXTURE_2D, data);

		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		Gdx.gl.glBindTexture(glTarget, 0);
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public boolean isManaged() {
		return false;
	}

	@Override
	protected void reload() {
	}
	
	public void dispose () {
		if (glHandle == 0) return;
		delete();
		if (data != null && data.isManaged())
			if (managedTextures.get(Gdx.app) != null) managedTextures.get(Gdx.app).removeValue(this, true);
	}
}
