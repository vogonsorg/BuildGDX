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

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.scale;

import ru.m210projects.Build.Types.WALL;

public class Gameutils {
	
	public static float BClampAngle(float angle)
    {
		return angle < 0 ? (angle % 2048) + 2048 : angle % 2048;
    }
	
	public static float BClipRange(float value, float min, float max) {
		if(value < min) return min;
		if(value > max) return max;
		
		return value;
	}
	
	public static int BClipRange(int value, int min, int max) {
		if(value < min) return min;
		if(value > max) return max;
		
		return value;
	}
	
	public static short BClipLow(short value, short min) {
		return value < min ? min : value;
	}
	
	public static short BClipHigh(short value, short max) {
		return value > max ? max : value;
	}
	
	public static int BClipLow(int value, int min) {
		return value < min ? min : value;
	}
	
	public static int BClipHigh(int value, int max) {
		return value > max ? max : value;
	}

	public static float BClipLow(float value, int min) {
		return value < min ? min : value;
	}
	
	public static float BClipHigh(float value, int max) {
		return value > max ? max : value;
	}
	
	public static double BSinAngle(double daang)
	{
		double rad_ang = daang * Math.PI * (1.0/1024.0);
		return (Math.sin(rad_ang) * 16384.0);
	}
	
	public static double BCosAngle(double daang)
	{
		double rad_ang = daang * Math.PI * (1.0/1024.0);
		return (Math.cos(rad_ang) * 16384.0);
	}
	
	public static boolean isValidSector(int i)
	{
		return i >= 0 && i < MAXSECTORS;
	}
	
	public static boolean isValidStat(int i)
	{
		return i >= 0 && i <= MAXSTATUS;
	}
	
	public static boolean isValidSprite(int i)
	{
		return i >= 0 && i < MAXSPRITES;
	}
	
	public static boolean isValidWall(int i)
	{
		return i >= 0 && i < MAXWALLS;
	}
	
	public static boolean isCorruptWall(WALL wal)
	{
		return !isValidWall(wal.point2) || wall[wal.point2] == null;
	}
	
	public enum ConvertType { Normal, AlignLeft, AlignRight, Stretch };
	
	public static int coordsConvertXScaled(int coord, ConvertType type)
	{
		int oxdim = xdim;
		int xdim = (4 * ydim) / 3;
		int offset = oxdim - xdim;
		
		int buildim = 320;
		if(type == ConvertType.Stretch)
			buildim = buildim * xdim / oxdim;
	
		int normxofs = coord - (buildim << 15);
		int wx = (xdim << 15) + scale(normxofs, xdim, buildim);
		
		if(type == ConvertType.Stretch)
			return wx;
		
		wx += (oxdim - xdim) / 2;
		
		if(type == ConvertType.AlignLeft)
			return wx - offset / 2 - 1;
		if(type == ConvertType.AlignRight)
			return wx + offset / 2 - 1;

		return wx - 1;
	}
	
	public static int coordsConvertYScaled(int coord)
	{
		int oydim = ydim;
		int ydim = (3 * xdim) / 4;
		int buildim = 200 * ydim / oydim;
		int normxofs = coord - (buildim << 15);
		int wy = (ydim << 15) + scale(normxofs, ydim, buildim);

		return wy;
	}
}
