// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Render;

import java.util.HashMap;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;
import ru.m210projects.Build.Render.Types.FadeEffect;

public abstract class GLRenderer extends Renderer {

	public enum GLInvalidateFlag {
		Uninit,
		SkinsOnly,
		TexturesOnly,
		IndexedTexturesOnly,
		All
	}

	protected final TextureCache textureCache;
	protected final Engine engine;

	public GLRenderer(Engine engine) {
		if (BuildGdx.graphics.getFrameType() != FrameType.GL)
			BuildGdx.app.setFrame(FrameType.GL);
		GLInfo.init();
		this.textureCache = new TextureCache(engine);
		this.engine = engine;
	}

	@Override
	public PixelFormat getTexFormat() {
		return textureCache.getFormat();
	}

	public abstract void palfade(HashMap<String, FadeEffect> fades);

	public abstract void preload();

	public abstract void precache(int dapicnum, int dapalnum, int datype);

	public abstract void gltexapplyprops();

	public abstract void gltexinvalidateall(GLInvalidateFlag... flags);

	public abstract void gltexinvalidate(int dapicnum, int dapalnum, int dameth);

	public abstract void setdrunk(float intensive);

	public abstract float getdrunk();

	public abstract void addSpriteCorr(int snum);

	public abstract void removeSpriteCorr(int snum);

}
