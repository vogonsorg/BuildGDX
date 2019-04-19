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

package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Engine.*;

import java.util.HashSet;

import ru.m210projects.Build.Render.TextureHandle.Pthtyp;
import ru.m210projects.Build.Render.TextureHandle.TextureCache;

public class TileFont {
	
	public static final HashSet<TileFont> managedFont = new HashSet<TileFont>();
	
	public enum FontType { Tilemap, Bitmap };

	public Pthtyp atlas;
	
	public Object ptr;
	public FontType type;
	public int charsizx;
	public int charsizy;
	public int cols, rows;
	public int sizx = -1, sizy = -1;

	public TileFont(FontType type, Object ptr, int charsizx, int charsizy, int cols, int rows)
	{
		this.ptr = ptr;
		this.type = type;
		this.charsizx = charsizx;
		this.charsizy = charsizy;
		this.cols = cols;
		this.rows = rows;
		
		managedFont.add(this);
	}
	
	public Pthtyp init(TextureCache textureCache, int col) {
		int nTile = (Integer) ptr;
		
		Pthtyp pth = textureCache.cache(nTile, col, (short) 0, false, true);
		this.sizx = tilesizx[nTile];
		this.sizy = tilesizy[nTile];

		return pth;
	}
	
	public Pthtyp getGL(TextureCache textureCache, int col) {
		return init(textureCache, col);
	}
	
	public void dispose()
	{
		if(atlas != null)
			atlas.glpic.dispose();
		
		managedFont.remove(this);
	}
	
}
