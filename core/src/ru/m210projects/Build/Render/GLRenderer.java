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
import ru.m210projects.Build.Render.Types.FadeEffect;

public abstract class GLRenderer extends Renderer {
	
	public GLRenderer()
	{
		BuildGdx.app.setFrame(FrameType.GL);
		GLInfo.init();
		
		this.registerSlider("Gamma", GLSettings.gamma, 0, 4096, 64, 4096);
		this.registerSlider("Brightness", GLSettings.brightness, -4096, 4096, 64, 4096);
		this.registerSlider("Contrast", GLSettings.contrast, 0, 8192, 64, 4096);
		this.registerButton("Reset to default", new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				GLSettings.gamma.set(0);
				GLSettings.brightness.set(0);
				GLSettings.contrast.set(4096);
			}});
		this.registerSeparator();
		
		String[] filters = new String[GLSettings.glfiltermodes.length];
		for(int i = 0; i < filters.length; i++)
			filters[i] = GLSettings.glfiltermodes[i].name;
		this.registerConteiner("Texture mode", GLSettings.textureFilter, GLSettings.glfiltermodes, filters);
		
		int anisotropysize = 0;
		for (int s = (int)maxanisotropy; s > 1; s >>= 1)
			anisotropysize++;
		Integer[] list = new Integer[anisotropysize + 1];
		String[] anisotropies = new String[anisotropysize + 1];
		for (int i = 0; i < list.length; i++) {
			list[i] = pow2long[i];
			anisotropies[i] = i == 0 ? "None" : list[i] + "x";
		}
		this.registerConteiner("Anisotropy", GLSettings.textureAnisotropy, list, anisotropies);

		this.registerSwitch("True color textures", GLSettings.useHighTile);
		this.registerSwitch("3d models", GLSettings.useModels);
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
