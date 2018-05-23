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

package ru.m210projects.Build.Loader.MD2;

import java.nio.FloatBuffer;

import ru.m210projects.Build.Loader.MDModel;

public class MD2Model extends MDModel {
    public int numverts, numglcmds, framebytes;
    public int[] glcmds;
    public MD2Frame[] frames;
    public String basepath;   // pointer to string of base path
    public String skinfn;   // pointer to first of numskins 64-char strings
    public FloatBuffer uv;
    public MD2Triangle[] tris;
}
