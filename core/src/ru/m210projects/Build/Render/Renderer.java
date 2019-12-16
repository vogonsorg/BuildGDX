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
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Settings.BuildSettings;
import ru.m210projects.Build.Types.ParamLinker;
import ru.m210projects.Build.Types.TileFont;

public abstract class Renderer extends ParamLinker {
	
	public Renderer() {
		this.params.add(new SliderItem<Integer>("Field of view", BuildSettings.fov, 60, 140, 5, null));
		this.params.add(new SwitchItem<Boolean>("Vsync", BuildSettings.vsync));
		String[] limits = new String[BuildSettings.fpslimits.length];
		for(int i = 0; i < limits.length; i++)
			limits[i] = i == 0 ? "None" : BuildSettings.fpslimits[i] + " fps";
		this.params.add(new ConteinerItem<Integer>("Framerate limit", BuildSettings.fpsLimit, BuildSettings.fpslimits, limits));
		this.params.add(new SwitchItem<Boolean>("Voxels", BuildSettings.useVoxels));
	}
	
	public enum RenderType { 
		Software(FrameType.Canvas, "Classic"), Polymost(FrameType.GL, "Polymost");
		
		FrameType type;
		String name;
		RenderType(FrameType type, String name)
		{
			this.type = type;
			this.name = name;
		}
		
		public FrameType getFrameType() { return type; }
		public String getName() { return name; }
	};
	
	public enum Transparent { None, Bit1, Bit2 }
	
	public enum PixelFormat { Rgb, Pal8, Pal8A }
	
	public abstract PixelFormat getTexFormat();
	
	public abstract void init();
	
	public abstract void uninit();
	
	public abstract boolean isInited();
	
	public abstract void drawmasks();
	
	public abstract void drawrooms();
	
	public abstract void clearview(int dacol);
	
	public abstract void changepalette(byte[] palette);

	public abstract void nextpage();
	
	public abstract void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat,
            int cx1, int cy1, int cx2, int cy2);

	public abstract void completemirror();
	
	public abstract void drawoverheadmap(int cposx, int cposy, int czoom, short cang);
	
	public abstract void drawmapview(int dax, int day, int zoome, int ang);
	
	public abstract void printext(TileFont font, int xpos, int ypos, char[] text, int col, int shade, Transparent bit, float scale);
	
	public abstract void printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize, float scale);

	public abstract ByteBuffer getFrame(PixelFormat format, int xsiz, int ysiz);
	
	public abstract void drawline256(int x1, int y1, int x2, int y2, int col);
	
	public abstract void settiltang(int tilt);

	public abstract void setDefs(DefScript defs);
	
	public abstract RenderType getType();
}
