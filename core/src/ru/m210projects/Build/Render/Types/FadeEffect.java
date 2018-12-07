//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Render.Types;

import static ru.m210projects.Build.Render.Types.GL10.GL_TRIANGLES;

public abstract class FadeEffect {
	public int sfactor; 
	public int dfactor;
	public int r, g, b, a; 

	public FadeEffect(int sfactor, int dfactor)
	{
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}

	public abstract void update(int intensive);

	public void draw(GL10 gl) {
		
		gl.glBlendFunc(sfactor, dfactor);
		gl.glColor4ub(r, g, b, a);

		gl.glBegin(GL_TRIANGLES);
		gl.glVertex2f(-2.5f, 1.f);
		gl.glVertex2f(2.5f, 1.f);
		gl.glVertex2f(.0f, -2.5f);
		gl.glEnd();
	}
}








