
package ru.m210projects.Build.Types;

public interface Message {

	public boolean show(String header, String text, boolean send);
	public void dispose();
	
}
