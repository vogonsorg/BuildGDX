package ru.m210projects.Build.Types;

public class MemLog {
	public static void log(String id) {
		int mb = 1024 * 1024; 
		System.out.println("***** Heap utilization statistics [MB] *****\n on id : " + id);
		
		// get Runtime instance
		Runtime instance = Runtime.getRuntime();
		// available memory
		System.out.println("Total Memory: " + instance.totalMemory() / mb);
		// free memory
		System.out.println("Free Memory: " + instance.freeMemory() / mb);
		// used memory
		System.out.println("Used Memory: "
				+ (instance.totalMemory() - instance.freeMemory()) / mb);
		// Maximum available memory
		System.out.println("Max Memory: " + instance.maxMemory() / mb);
	}
	
	public static void logTotal(String id) {
		int mb = 1024 * 1024; 
		System.out.println("***** Heap utilization statistics [MB] *****\n on id : " + id);
		
		// get Runtime instance
		Runtime instance = Runtime.getRuntime();
		// available memory
		System.out.println("Total Memory: " + instance.totalMemory() / mb);
	}
	
	public static long startMem;
	public static void start()
	{
		Runtime instance = Runtime.getRuntime();
		startMem = (instance.totalMemory() - instance.freeMemory());
	}
	
	public static void result(String txt)
	{
		int kb = 1024; 
		Runtime instance = Runtime.getRuntime();
		long mem = (instance.totalMemory() - instance.freeMemory()) - startMem;
		
		System.out.println(txt + " : " + mem / kb +" kb");
	}
}
