package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.Input;

public interface BuildInput extends Input {
	
	void update();
	
	void processEvents();
	
	public void processMessages();
	
	public boolean cursorHandler();
	
	public int getDWheel(); 

}
