package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.Input;

public interface BuildInput extends Input {
	
	public void processMessages();
	
	public boolean cursorHandler();
	
	public int getDWheel(); 

}
