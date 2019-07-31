package ru.m210projects.Build.desktop.software;

import com.badlogic.gdx.InputProcessor;

public interface MouseInterface {

	public void reset();

	public int getX();

	public int getY();
	
	public int getDeltaX();

	public int getDeltaY();

	public boolean isTouched();
	
	public boolean justTouched();
	
	public boolean isButtonPressed(int button);
	
	public int getDWheel();

	public long processEvents(InputProcessor processor);

	public void setCursorCatched(boolean catched);

	public boolean isCursorCatched();

	public void setCursorPosition(int x, int y);

	public void showCursor(boolean b);

	public boolean isInsideWindow();

}
