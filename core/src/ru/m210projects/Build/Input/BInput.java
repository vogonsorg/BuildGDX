package ru.m210projects.Build.Input;

import com.badlogic.gdx.Input;

public interface BInput extends Input {
	
	public void updateRequest();
	
	public boolean cursorHandler();
	
	public int getDWheel(); 

}
