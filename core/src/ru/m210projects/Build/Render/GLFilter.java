
package ru.m210projects.Build.Render;

public class GLFilter {
	public String name;
	public int min,mag;
	

	public GLFilter(String name, int min, int mag) {
		this.name = name;
		this.min = min;
		this.mag = mag;
	}
}
