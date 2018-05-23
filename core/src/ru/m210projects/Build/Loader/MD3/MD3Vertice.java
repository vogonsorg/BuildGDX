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

public class MD3Vertice {
	public short x, y, z, nlat, nlng;
	
	/*
	surface.vertices.put((float)var1.getShort() / 64.0F); //x
    surface.vertices.put((float)var1.getShort() / 64.0F); //y
    surface.vertices.put((float)var1.getShort() / 64.0F); //z
    double var15 = (double)(var1.get() & 255) * Math.PI * 2.0D / 255.0D; //nlat
    double var17 = (double)(var1.get() & 255) * Math.PI * 2.0D / 255.0D; //nlng
    
    float var19 = (float)(Math.cos(var17) * Math.sin(var15));
    float var21 = (float)(Math.sin(var17) * Math.sin(var15));
    float var22 = (float)Math.cos(var15);
    surface.normals.put(var19);
    surface.normals.put(var21);
    surface.normals.put(var22);
	*/
}
