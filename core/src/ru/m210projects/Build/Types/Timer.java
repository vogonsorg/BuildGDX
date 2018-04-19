package ru.m210projects.Build.Types;

public class Timer {
	public static long startTime;
	public static long spentTime;
	
	public static void start() {
		startTime = System.nanoTime();
	}
	
	public static long result() {
		spentTime = System.nanoTime() - startTime;
		System.out.println(spentTime / 1000f +" nsec");
		return spentTime;
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
