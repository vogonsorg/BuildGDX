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

package ru.m210projects.Build.Types;

public class Timer {
	public static long startTime;
	public static long spentTime;
	
	public static long summTime;
	public static long count;
	
	public static void start() {
		startTime = System.nanoTime();
	}
	
	public static long result() {
		spentTime = System.nanoTime() - startTime;
		System.out.println(spentTime / 1000f +" nsec");
		return spentTime;
	}
	
	public static long resultAverage() {
		spentTime = System.nanoTime() - startTime;
		count++;
		summTime += (spentTime / 1000f);
		long result = (summTime / count);
		if((count % 255) == 1)
		System.out.println(result +" nsec");
		return result;
	}
	
	public static long result(String comment) {
		spentTime = System.nanoTime() - startTime;
		
		System.out.println(comment + " : " + spentTime / 1000f +" nsec");
		return spentTime;
	}
	
	public static void startFPS() {
		startTime = System.nanoTime();
	}
	
	public static int FPSresult() {
		spentTime = (long) ((System.nanoTime() - startTime));
		long fps = (long) (1000000000.0/spentTime);
		System.out.println(fps +" fps");
		return (int) fps;
	}
}
