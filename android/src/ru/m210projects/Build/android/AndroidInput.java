package ru.m210projects.Build.android;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidInputFactory;

import android.content.Context;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildInput;

public class AndroidInput implements BuildInput {

	private com.badlogic.gdx.backends.android.AndroidInput input;
	@Override
	public void init(BuildFrame frame) {
		Application activity = BuildGdx.app;
		Context context = ((AndroidFrame) frame).activity;
		Object view = ((AndroidGraphics) frame.getGraphics()).view;
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		input = AndroidInputFactory.newAndroidInput(activity, context, view, config);
		Gdx.input = BuildGdx.input = this;
		
		input.setCatchBackKey(true);
	}
	
	@Override
	public void cancelVibrate() {
		input.cancelVibrate();
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
	public float getAzimuth() {
		return input.getAzimuth();
	}

	@Override
	public long getCurrentEventTime() {
		return input.getCurrentEventTime();
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
	public int getDeltaY() {
		return input.getDeltaY();
	}

	@Override
	public int getDeltaY(int pointer) {
		return input.getDeltaY(pointer);
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
	public int getMaxPointers() {
		return 0; //input.getMaxPointers();
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
	public int getY() {
		return input.getY();
	}

	@Override
	public int getY(int pointer) {
		return input.getY(pointer);
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
	public float getPressure() {
		return 0; // input.getPressure();
	}

	@Override
	public float getPressure(int pointer) {
		return 0; //input.getPressure(pointer);
	}

	@Override
	public boolean isButtonPressed(int button) {
		return input.isButtonPressed(button);
	}

	@Override
	public boolean isButtonJustPressed(int button) {
		return false; //input.isButtonJustPressed(button);
	}

	@Override
	public boolean isKeyPressed(int key) {
		return input.isKeyPressed(key);
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
	public void setCatchBackKey(boolean catchBack) {
		input.setCatchBackKey(catchBack);
	}

	@Override
	public boolean isCatchBackKey() {
		return input.isCatchBackKey();
	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {
		input.setCatchBackKey(catchMenu);
	}

	@Override
	public boolean isCatchMenuKey() {
		return input.isCatchMenuKey();
	}

	@Override
	public void setCatchKey(int keycode, boolean catchKey) {
//		input.setCatchKey(keycode, catchKey);
	}

	@Override
	public boolean isCatchKey(int keycode) {
		return false; //input.isCatchKey(keycode);
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

	@Override
	public void dispose() {}
	

	@Override
	public void update() {}

	@Override
	public void processEvents() {
		
	}

	@Override
	public void processMessages() {
		
	}

	@Override
	public boolean cursorHandler() {
		return false;
	}

	@Override
	public int getDWheel() {
		return 0;
	}
}
