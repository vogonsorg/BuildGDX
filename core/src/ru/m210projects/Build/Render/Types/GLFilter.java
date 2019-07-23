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

import static com.badlogic.gdx.graphics.GL20.*;

import com.badlogic.gdx.Gdx;

public class GLFilter {
	public String name;
	public int min,mag;
	public boolean mipmaps;
	
	public GLFilter(String name, int min, int mag) {
		this.name = name;
		this.min = min;
		this.mag = mag;
		this.mipmaps = (min >= GL_NEAREST_MIPMAP_NEAREST || mag >= GL_NEAREST_MIPMAP_NEAREST);
	}
	
	public void apply()
	{
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, this.mag);
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, this.min);
	}
}
