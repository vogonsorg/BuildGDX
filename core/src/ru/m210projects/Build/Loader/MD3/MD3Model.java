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

package ru.m210projects.Build.Loader.MD3;

import java.util.HashMap;

import ru.m210projects.Build.Loader.MDModel;

import com.badlogic.gdx.math.Matrix4;

public class MD3Model extends MDModel {
    public MD3Header head;
	public MD3Frame[] frames;
	public HashMap<String, Matrix4>[] tags;
	public MD3Surface[] surfaces;
    
    // polymer VBO names after that, allocated per surface
//    GLuint*             indices;
//    GLuint*             texcoords;
//    GLuint*             geometry;
}
