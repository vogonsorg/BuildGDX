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

package ru.m210projects.Build;

public class Gameutils {
	public static float BClampAngle(float angle)
    {
        if (angle < 0) angle += 2048f;
        if (angle >= 2048) angle -= 2048f;

        return BClipRange(angle, 0, 2048);
    }
	
	public static float BClipRange(float value, int min, int max) {
		if(value < min) value = min;
		if(value > max) value = max;
		
		return value;
	}
	
	public static double BSinAngle(float daang)
	{
		double rad_ang = daang * Math.PI * (1.0/1024.0);
		return (Math.sin(rad_ang) * 16384.0);
	}
	
	public static double BCosAngle(float daang)
	{
		double rad_ang = daang * Math.PI * (1.0/1024.0);
		return (Math.cos(rad_ang) * 16384.0);
	}
}
