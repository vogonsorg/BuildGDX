package ru.m210projects.Build.desktop.gl;

import static ru.m210projects.Build.Input.Keymap.*;

import java.lang.reflect.Method;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglInput;

import ru.m210projects.Build.Architecture.BuildInput;

public class GLInput implements BuildInput {
	
	protected Cursor emptyCursor;
	protected Cursor defCursor = Mouse.getNativeCursor();
	protected LwjglInput input;
	private Method processEventsMethod;
	
	public GLInput()
	{
		input = new LwjglInput();

		try {
			processEventsMethod = input.getClass().getDeclaredMethod("processEvents");  
			processEventsMethod.setAccessible(true);   
		} catch (Exception e) {}
	}

	@Override
	public float getAccelerometerX() {
		return input.getAccelerometerX();
	}

	@Override
	public float getAccelerometerY() {
		return input.getAccelerometerY();
	}

	@Override
	public float getAccelerometerZ() {
		return input.getAccelerometerZ();
	}

	@Override
	public float getGyroscopeX() {
		return input.getGyroscopeX();
	}

	@Override
	public float getGyroscopeY() {
		return input.getGyroscopeY();
	}

	@Override
	public float getGyroscopeZ() {
		return input.getGyroscopeZ();
	}

	@Override
	public int getX() {
		return input.getX();
	}

	@Override
	public int getX(int pointer) {
		return input.getX(pointer);
	}

	@Override
	public int getDeltaX() {
		return input.getDeltaX();
	}

	@Override
	public int getDeltaX(int pointer) {
		return input.getDeltaX(pointer);
	}

	@Override
	public int getY() {
		return input.getY();
	}

	@Override
	public int getY(int pointer) {
		return input.getY(pointer);
	}

	@Override
	public int getDeltaY() {
		return input.getDeltaY();
	}

	@Override
	public int getDeltaY(int pointer) {
		return input.getDeltaY(pointer);
	}

	@Override
	public boolean isTouched() {
		return input.isTouched();
	}

	@Override
	public boolean justTouched() {
		return input.justTouched();
	}

	@Override
	public boolean isTouched(int pointer) {
		return input.isTouched(pointer);
	}

	@Override
	public boolean isButtonPressed(int button) {
		return input.isButtonPressed(button);
	}

	@Override
	public boolean isKeyPressed(int key) {
//		if (!Keyboard.isCreated()) return false;  // isReady must be enough
		return Keyboard.isKeyDown(getLwjglKeyCode(key));
	}

	@Override
	public boolean isKeyJustPressed(int key) {
		return input.isKeyJustPressed(key);
	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint) {
		input.getTextInput(listener, title, text, hint);
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
		input.setOnscreenKeyboardVisible(visible);
	}

	@Override
	public void vibrate(int milliseconds) {
		input.vibrate(milliseconds);
	}

	@Override
	public void vibrate(long[] pattern, int repeat) {
		input.vibrate(pattern, repeat);
	}

	@Override
	public void cancelVibrate() {
		input.cancelVibrate();
	}

	@Override
	public float getAzimuth() {
		return input.getAzimuth();
	}

	@Override
	public float getPitch() {
		return input.getPitch();
	}

	@Override
	public float getRoll() {
		return input.getRoll();
	}

	@Override
	public void getRotationMatrix(float[] matrix) {
		input.getRotationMatrix(matrix);
	}

	@Override
	public long getCurrentEventTime() {
		return input.getCurrentEventTime();
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {
		input.setCatchBackKey(catchBack);
	}

	@Override
	public boolean isCatchBackKey() {
		return input.isCatchBackKey();
	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {
		input.setCatchMenuKey(catchMenu);
	}

	@Override
	public boolean isCatchMenuKey() {
		return input.isCatchMenuKey();
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
		input.setInputProcessor(processor);
	}

	@Override
	public InputProcessor getInputProcessor() {
		return input.getInputProcessor();
	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		return input.isPeripheralAvailable(peripheral);
	}

	@Override
	public int getRotation() {
		return input.getRotation();
	}

	@Override
	public Orientation getNativeOrientation() {
		return input.getNativeOrientation();
	}

	@Override
	public void setCursorCatched(boolean catched) {
		input.setCursorCatched(catched);
	}

	@Override
	public boolean isCursorCatched() {
		return input.isCursorCatched();
	}

	@Override
	public void setCursorPosition(int x, int y) {
		input.setCursorPosition(x, y);
	}

	public void update() {
		input.update();
	}
	
	public void processEvents()
	{
		if(processEventsMethod != null) try {
			processEventsMethod.invoke(input);
		} catch (Exception e) { }
	}
	
	@Override
	public void processMessages() {
		Display.processMessages();
	}

	@Override
	public boolean cursorHandler() {
		try {
			if(emptyCursor == null)
				emptyCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
			
			if (emptyCursor != null && Mouse.isInsideWindow() && Display.isActive()) 
				Mouse.setNativeCursor(emptyCursor);
			else Mouse.setNativeCursor(defCursor);
		} catch (Exception e) {}
		
		return false;
	}

	@Override
	public int getDWheel() {
		return Mouse.getDWheel();
	}
	
	protected static int getLwjglKeyCode (int gdxKeyCode) {
		switch (gdxKeyCode) {
		case KEY_PAUSE:
			return Keyboard.KEY_PAUSE;
		case KEY_CAPSLOCK:
			return Keyboard.KEY_CAPITAL;
		case KEY_SCROLLOCK:
			return Keyboard.KEY_SCROLL;
		case KEY_NUMDECIMAL:
			return Keyboard.KEY_DECIMAL;
		default:
			return LwjglInput.getLwjglKeyCode(gdxKeyCode);
		}
	}
}
