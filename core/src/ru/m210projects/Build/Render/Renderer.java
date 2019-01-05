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

import java.nio.ByteBuffer;
import java.util.HashMap;

import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Script.DefScript;

public interface Renderer {
	
	public void init();
	
	public void uninit();
	
	public void drawmasks();
	
	public void drawrooms();
	
	public void clearview(int dacol);
	
	public void palfade(HashMap<String, FadeEffect> fades);
	
	public void preload();
	
	public void precache(int dapicnum, int dapalnum, int datype);
	
	public void nextpage();
	
	public void gltexapplyprops();
	
	public void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat,
            int cx1, int cy1, int cx2, int cy2);
	
	public String getname();
	
	public abstract void drawoverheadmap(int cposx, int cposy, int czoom, short cang);
	
	public abstract void drawmapview(int dax, int day, int zoome, int ang);
	
	public void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale);
	
	public void gltexinvalidateall(int flags);

	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth);

	public ByteBuffer getframebuffer(int x, int y, int w, int h, int format);
	
	public void drawline256(int x1, int y1, int x2, int y2, int col);
	
	public void settiltang(int tilt);
	
	public void setdrunk(float intensive);
	
	public float getdrunk();
	
	public void addSpriteCorr(int snum);
	
	public void removeSpriteCorr(int snum);
	
	public void setDefs(DefScript defs);
}
