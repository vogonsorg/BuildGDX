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

import static ru.m210projects.Build.Engine.pow2long;
import static ru.m210projects.Build.Render.GLInfo.maxanisotropy;

import java.util.HashMap;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Settings.BuildSettings;
import ru.m210projects.Build.Settings.GLSettings;

public abstract class GLRenderer extends Renderer {
	
	protected final TextureCache textureCache;
	
	public GLRenderer()
	{
		BuildGdx.app.setFrame(FrameType.GL);
		GLInfo.init();
		
		this.params.add(0, new SliderItem<Integer>("Gamma", BuildSettings.paletteGamma, 0, 15, 1, null) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.Indexed; }
		});
		this.params.add(1, new ParamItem<Boolean>(ItemType.Separator) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.Indexed; }
		});
		
		this.params.add(0, new SliderItem<Integer>("Gamma", GLSettings.gamma, 0, 4096, 64, 4096) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.RGB; }
		});
		this.params.add(1, new SliderItem<Integer>("Brightness", GLSettings.brightness, -4096, 4096, 64, 4096) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.RGB; }
		});
		this.params.add(2, new SliderItem<Integer>("Contrast", GLSettings.contrast, 0, 8192, 64, 4096) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.RGB; }
		});
		this.params.add(3, new ButtonItem<Boolean>("Reset to default", new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				GLSettings.gamma.set(0);
				GLSettings.brightness.set(0);
				GLSettings.contrast.set(4096);
			}}) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.RGB; }
		});
		this.params.add(4, new ParamItem<Boolean>(ItemType.Separator) {
			@Override
			public boolean checkItem() { return getTexFormat() == PixelFormat.RGB; }
		});
		
		String[] filters = new String[GLSettings.glfiltermodes.length];
		for(int i = 0; i < filters.length; i++)
			filters[i] = GLSettings.glfiltermodes[i].name;
		this.params.add(this.params.size() - 1, new ConteinerItem<GLFilter>("Texture mode", GLSettings.textureFilter, GLSettings.glfiltermodes, filters));
		
		int anisotropysize = 0;
		for (int s = (int)maxanisotropy; s > 1; s >>= 1)
			anisotropysize++;
		Integer[] list = new Integer[anisotropysize + 1];
		String[] anisotropies = new String[anisotropysize + 1];
		for (int i = 0; i < list.length; i++) {
			list[i] = pow2long[i];
			anisotropies[i] = i == 0 ? "None" : list[i] + "x";
		}
		this.params.add(this.params.size() - 1, new ConteinerItem<Integer>("Anisotropy", GLSettings.textureAnisotropy, list, anisotropies));

		this.params.add(new SwitchItem<Boolean>("True color textures", GLSettings.useHighTile));
		this.params.add(new SwitchItem<Boolean>("3d models", GLSettings.useModels));
		
		this.textureCache = new TextureCache();
	}
	
	@Override
	public PixelFormat getTexFormat() {
		return textureCache.getFormat();
	}
	
	public abstract void palfade(HashMap<String, FadeEffect> fades);
	
	public abstract void preload();
	
	public abstract void precache(int dapicnum, int dapalnum, int datype);
	
	public abstract void gltexapplyprops();
	
	public abstract void gltexinvalidateall(int flags);

	public abstract void gltexinvalidate(int dapicnum, int dapalnum, int dameth);

	public abstract void setdrunk(float intensive);
	
	public abstract float getdrunk();
	
	public abstract void addSpriteCorr(int snum);
	
	public abstract void removeSpriteCorr(int snum);

}
