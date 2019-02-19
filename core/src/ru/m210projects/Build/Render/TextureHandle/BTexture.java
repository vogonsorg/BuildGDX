// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Kirill Klimenko-KLIMaka 
// and Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Render.TextureHandle;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ru.m210projects.Build.Render.Types.GL10;

import static ru.m210projects.Build.Render.Types.GL10.GL_TEXTURE_2D;

public class BTexture extends GLTexture {

	TextureData data;
	final static Map<Application, Array<BTexture>> managedTextures = new HashMap<Application, Array<BTexture>>();
	final int width, height;
	
	public BTexture (Pixmap pixmap, boolean useMipMaps) {
		super(GL_TEXTURE_2D, Gdx.gl.glGenTexture());
		this.width = 0;
		this.height = 0;
		data = new PixmapTextureData(pixmap, null, useMipMaps, false);
		load(data);
		if (data.isManaged()) addManagedTexture(Gdx.app, this);
	}
	
	public BTexture() {
		super(GL_TEXTURE_2D);
		this.width = 0;
		this.height = 0;
	}
	
	public BTexture(int width, int height) {
		super(GL_TEXTURE_2D);
		this.width = width;
		this.height = height;
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
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
