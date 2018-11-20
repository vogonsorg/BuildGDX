package ru.m210projects.Build.desktop.software;

import java.awt.Canvas;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import ru.m210projects.Build.Architecture.BuildInput;

public class SoftInput implements BuildInput {
	
	protected Canvas canvas;
	
	protected int omouse_x;
	protected int omouse_y;
	protected int mouse_x;
	protected int mouse_y;
	
//	private static volatile boolean wPressed = false;
//    public static boolean isWPressed() {
//        synchronized (IsKeyPressed.class) {
//            return wPressed;
//        }
//    }
	
	public void init(Canvas canvas)
	{
		this.canvas = canvas;
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (SoftInput.class) {
                    switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                    	System.err.println(ke);
//                        if (ke.getKeyCode() == KeyEvent.VK_W) {
//                            wPressed = true;
//                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
//                        if (ke.getKeyCode() == KeyEvent.VK_W) {
//                            wPressed = false;
//                        }
                        break;
                    }
                    return false;
                }
            }
        });

		MouseAdapter adapter = new MouseAdapter() {
			 @Override
		     public void mousePressed(MouseEvent e) {
		    	 
		     }
		     @Override
		     public void mouseMoved(MouseEvent e) {
		    	 omouse_x = mouse_x;
		    	 omouse_y = mouse_y;
		    	 
		    	 mouse_x = e.getX();
		    	 mouse_y = e.getY();
		     }
		};
		
		canvas.addMouseListener(adapter);
		canvas.addMouseMotionListener(adapter);
	}

	@Override
	public int getX() {
		return mouse_x;
	}

	@Override
	public int getX(int pointer) {
		return getX();
	}

	@Override
	public int getDeltaX() {
		int dx = mouse_x - omouse_x;
		omouse_x = mouse_x;
		
		return dx;
	}

	@Override
	public int getDeltaX(int pointer) {
		return getDeltaX();
	}

	@Override
	public int getY() {
		return mouse_y;
	}

	@Override
	public int getY(int pointer) {
		return getY();
	}

	@Override
	public int getDeltaY() {
		int dy = mouse_y - omouse_y;
		omouse_y = mouse_y;
		
		return dy;
	}

	@Override
	public int getDeltaY(int pointer) {
		return getDeltaY();
	}

	@Override
	public boolean isTouched() {
		return false;
	}

	@Override
	public boolean justTouched() {
		return false;
	}

	@Override
	public boolean isTouched(int pointer) {
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		return false;
	}

	@Override
	public boolean isKeyJustPressed(int key) {
		synchronized (SoftInput.class) {
			return false;
		}
	}

	@Override
	public Orientation getNativeOrientation() {
		return Input.Orientation.Landscape;
	}

	@Override
	public void setCursorCatched(boolean catched) {
		
	}

	@Override
	public boolean isCursorCatched() {
		return false;
	}

	@Override
	public void setCursorPosition(int x, int y) {
		
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

	public void update() {
		
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	public float getAccelerometerX() {
		return 0;
	}

	@Override
	public float getAccelerometerY() {
		return 0;
	}

	@Override
	public float getAccelerometerZ() {
		return 0;
	}

	@Override
	public float getGyroscopeX() {
		return 0;
	}

	@Override
	public float getGyroscopeY() {
		return 0;
	}

	@Override
	public float getGyroscopeZ() {
		return 0;
	}
	
	@Override
	public boolean isButtonPressed(int button) {
		return false;
	}
	
	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint) {
		
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
		
	}

	@Override
	public void vibrate(int milliseconds) {
		
	}

	@Override
	public void vibrate(long[] pattern, int repeat) {
		
	}

	@Override
	public void cancelVibrate() {
		
	}

	@Override
	public float getAzimuth() {
		return 0;
	}

	@Override
	public float getPitch() {
		return 0;
	}

	@Override
	public float getRoll() {
		return 0;
	}

	@Override
	public void getRotationMatrix(float[] matrix) {
		
	}

	@Override
	public long getCurrentEventTime() {
		return 0;
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {
	}

	@Override
	public boolean isCatchBackKey() {
		return false;
	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {
	}

	@Override
	public boolean isCatchMenuKey() {
		return false;
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
	}

	@Override
	public InputProcessor getInputProcessor() {
		return null;
	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		return false;
	}

	@Override
	public int getRotation() {
		return 0;
	}

}
