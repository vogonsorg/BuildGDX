package ru.m210projects.Build.desktop.software;

import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Input.Keymap.KEY_CAPSLOCK;
import static ru.m210projects.Build.Input.Keymap.KEY_PAUSE;
import static ru.m210projects.Build.Input.Keymap.KEY_SCROLLOCK;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import ru.m210projects.Build.Architecture.BuildInput;

public class SoftInput implements BuildInput {

	protected Cursor emptyCursor;
	protected Cursor defCursor = Cursor.getDefaultCursor();
	
	protected JDisplay display;

	protected int omouse_x;
	protected int omouse_y;
	protected int mouse_x;
	protected int mouse_y;
	protected Robot robot;
	protected boolean cursorCatched;

	protected boolean[] pressedKeys = new boolean[256];
	protected boolean[] justPressedKeys = new boolean[256];

	public SoftInput()
	{
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public int getKeyCode(KeyEvent ke) {
		switch (ke.getKeyCode()) {
		case KeyEvent.VK_MULTIPLY:
			return Keys.STAR;
		case KeyEvent.VK_PAUSE:
			return KEY_PAUSE;
		case KeyEvent.VK_CAPS_LOCK:
			return KEY_CAPSLOCK;
		case KeyEvent.VK_SCROLL_LOCK:
			return KEY_SCROLLOCK;
		case KeyEvent.VK_BACK_SPACE:
			return Keys.BACKSPACE;
		case KeyEvent.VK_LEFT:
			return Keys.LEFT;
		case KeyEvent.VK_RIGHT:
			return Keys.RIGHT;
		case KeyEvent.VK_UP:
			return Keys.UP;
		case KeyEvent.VK_DOWN:
			return Keys.DOWN;
		case KeyEvent.VK_QUOTE:
			return Keys.APOSTROPHE;
		case KeyEvent.VK_OPEN_BRACKET:
			return Keys.LEFT_BRACKET;
		case KeyEvent.VK_CLOSE_BRACKET:
			return Keys.LEFT_BRACKET;
		case KeyEvent.VK_BACK_QUOTE:
			return Keys.GRAVE;
		case KeyEvent.VK_NUM_LOCK:
			return Keys.NUM;
		case KeyEvent.VK_EQUALS:
			return Keys.EQUALS;
		case KeyEvent.VK_0:
			return Keys.NUM_0;
		case KeyEvent.VK_1:
			return Keys.NUM_1;
		case KeyEvent.VK_2:
			return Keys.NUM_2;
		case KeyEvent.VK_3:
			return Keys.NUM_3;
		case KeyEvent.VK_4:
			return Keys.NUM_4;
		case KeyEvent.VK_5:
			return Keys.NUM_5;
		case KeyEvent.VK_6:
			return Keys.NUM_6;
		case KeyEvent.VK_7:
			return Keys.NUM_7;
		case KeyEvent.VK_8:
			return Keys.NUM_8;
		case KeyEvent.VK_9:
			return Keys.NUM_9;
		case KeyEvent.VK_A:
			return Keys.A;
		case KeyEvent.VK_B:
			return Keys.B;
		case KeyEvent.VK_C:
			return Keys.C;
		case KeyEvent.VK_D:
			return Keys.D;
		case KeyEvent.VK_E:
			return Keys.E;
		case KeyEvent.VK_F:
			return Keys.F;
		case KeyEvent.VK_G:
			return Keys.G;
		case KeyEvent.VK_H:
			return Keys.H;
		case KeyEvent.VK_I:
			return Keys.I;
		case KeyEvent.VK_J:
			return Keys.J;
		case KeyEvent.VK_K:
			return Keys.K;
		case KeyEvent.VK_L:
			return Keys.L;
		case KeyEvent.VK_M:
			return Keys.M;
		case KeyEvent.VK_N:
			return Keys.N;
		case KeyEvent.VK_O:
			return Keys.O;
		case KeyEvent.VK_P:
			return Keys.P;
		case KeyEvent.VK_Q:
			return Keys.Q;
		case KeyEvent.VK_R:
			return Keys.R;
		case KeyEvent.VK_S:
			return Keys.S;
		case KeyEvent.VK_T:
			return Keys.T;
		case KeyEvent.VK_U:
			return Keys.U;
		case KeyEvent.VK_V:
			return Keys.V;
		case KeyEvent.VK_W:
			return Keys.W;
		case KeyEvent.VK_X:
			return Keys.X;
		case KeyEvent.VK_Y:
			return Keys.Y;
		case KeyEvent.VK_Z:
			return Keys.Z;
		case KeyEvent.VK_ALT:
			if(ke.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT)
				return Keys.ALT_LEFT;
			return Keys.ALT_RIGHT;
		case KeyEvent.VK_BACK_SLASH:
			return Keys.BACKSLASH;
		case KeyEvent.VK_COMMA:
			return Keys.COMMA;
		case KeyEvent.VK_DELETE:
			return Keys.FORWARD_DEL;
		case KeyEvent.VK_ENTER:
			return Keys.ENTER;
		case KeyEvent.VK_HOME:
			return Keys.HOME;
		case KeyEvent.VK_END:
			return Keys.END;
		case KeyEvent.VK_PAGE_DOWN:
			return Keys.PAGE_DOWN;
		case KeyEvent.VK_PAGE_UP:
			return Keys.PAGE_UP;
		case KeyEvent.VK_INSERT:
			return Keys.INSERT;
		case KeyEvent.VK_SUBTRACT:
		case KeyEvent.VK_MINUS:
			return Keys.MINUS;
		case KeyEvent.VK_PERIOD:
			return Keys.PERIOD;
		case KeyEvent.VK_ADD:
		case KeyEvent.VK_PLUS:
			return Keys.PLUS;
		case KeyEvent.VK_SEMICOLON:
			return Keys.SEMICOLON;
		case KeyEvent.VK_SHIFT:
			if(ke.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT)
				return Keys.SHIFT_LEFT;
			return Keys.SHIFT_RIGHT;
		case KeyEvent.VK_SLASH:
		case KeyEvent.VK_DIVIDE:
			return Keys.SLASH;
		case KeyEvent.VK_SPACE:
			return Keys.SPACE;
		case KeyEvent.VK_TAB:
			return Keys.TAB;
		case KeyEvent.VK_DECIMAL:
			return Keys.FORWARD_DEL; // Keys.DEL;
		case KeyEvent.VK_CONTROL:
			if(ke.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT)
				return Keys.CONTROL_LEFT;
			return Keys.CONTROL_RIGHT;
		case KeyEvent.VK_ESCAPE:
			return Keys.ESCAPE;
		case KeyEvent.VK_F1:
			return Keys.F1;
		case KeyEvent.VK_F2:
			return Keys.F2;
		case KeyEvent.VK_F3:
			return Keys.F3;
		case KeyEvent.VK_F4:
			return Keys.F4;
		case KeyEvent.VK_F5:
			return Keys.F5;
		case KeyEvent.VK_F6:
			return Keys.F6;
		case KeyEvent.VK_F7:
			return Keys.F7;
		case KeyEvent.VK_F8:
			return Keys.F8;
		case KeyEvent.VK_F9:
			return Keys.F9;
		case KeyEvent.VK_F10:
			return Keys.F10;
		case KeyEvent.VK_F11:
			return Keys.F11;
		case KeyEvent.VK_F12:
			return Keys.F12;
		case KeyEvent.VK_COLON:
			return Keys.COLON;
		case KeyEvent.VK_NUMPAD0:
			return Keys.NUMPAD_0;
		case KeyEvent.VK_NUMPAD1:
			return Keys.NUMPAD_1;
		case KeyEvent.VK_NUMPAD2:
			return Keys.NUMPAD_2;
		case KeyEvent.VK_NUMPAD3:
			return Keys.NUMPAD_3;
		case KeyEvent.VK_NUMPAD4:
			return Keys.NUMPAD_4;
		case KeyEvent.VK_NUMPAD5:
			return Keys.NUMPAD_5;
		case KeyEvent.VK_NUMPAD6:
			return Keys.NUMPAD_6;
		case KeyEvent.VK_NUMPAD7:
			return Keys.NUMPAD_7;
		case KeyEvent.VK_NUMPAD8:
			return Keys.NUMPAD_8;
		case KeyEvent.VK_NUMPAD9:
			return Keys.NUMPAD_9;
		default:
			return Keys.UNKNOWN;
		}
	}

	public void init(final JDisplay display) {
		this.display = display;

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
//				synchronized (SoftInput.class) {
					int code;
					switch (ke.getID()) {
					case KeyEvent.KEY_PRESSED:
						code = getKeyCode(ke);
						justPressedKeys[code] = !pressedKeys[code];
						pressedKeys[code] = true;
						break;
					case KeyEvent.KEY_RELEASED:
						code = getKeyCode(ke);
						justPressedKeys[code] = pressedKeys[code] = false;
						break;
					}
					return false;
//				}
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
		
		
		display.getCanvas().addMouseListener(adapter);
		display.getCanvas().addMouseMotionListener(adapter);
	}

	@Override
	public int getX() {
		return (int) (MouseInfo.getPointerInfo().getLocation().getX() - display.getCanvas().getLocationOnScreen().getX());
	}

	@Override
	public int getX(int pointer) {
		if(pointer != 0)
			return 0;
		
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
		if(pointer != 0)
			return 0;
		
		return getDeltaX();
	}

	@Override
	public int getY() {
		return (int) (MouseInfo.getPointerInfo().getLocation().getY() - display.getCanvas().getLocationOnScreen().getY());	
	}

	@Override
	public int getY(int pointer) {
		if(pointer != 0)
			return 0;
		
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
		if(pointer != 0)
			return 0;
		
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
		if(pointer != 0)
			return false;
		
		return isTouched();
	}
	
	@Override
	public boolean isButtonPressed(int button) {
		return false;
	}
	
	@Override
	public void setCursorCatched(boolean catched) {
		this.cursorCatched = catched;
	}

	@Override
	public boolean isCursorCatched() {
		return cursorCatched;
	}

	@Override
	public void setCursorPosition(int x, int y) {
		if(!display.m_frame.isActive() || !display.m_frame.isFocused())
			return;
		
		Point p = display.getCanvas().getLocationOnScreen();
		robot.mouseMove(p.x + x, p.y + y);
	}

	@Override
	public int getDWheel() {
		return 0;
	}

	@Override
	public boolean isKeyPressed(int key) {
		//synchronized (SoftInput.class) {
			return pressedKeys[key];
		//}
	}

	@Override
	public boolean isKeyJustPressed(int key) {
		//synchronized (SoftInput.class) {
			boolean pressed = justPressedKeys[key];
			if(pressed) 
				justPressedKeys[key] = false;
			return pressed;
		//}
	}

	@Override
	public Orientation getNativeOrientation() {
		return Input.Orientation.Landscape;
	}

	@Override
	public void processMessages() {
		if(isCursorCatched())
		{
			int x = BClipRange(getX(), 0, Gdx.graphics.getWidth() - 1);
			int y = BClipRange(getY(), 0, Gdx.graphics.getHeight() - 1);
			setCursorPosition(x, y);
		}
	}

	@Override
	public boolean cursorHandler() {
		if(emptyCursor == null) {
			emptyCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "emptyCursor");
		}
		
		if (emptyCursor != null && display.m_frame.isActive() && display.m_frame.isFocused()) {
			if(display.m_frame.getContentPane().getCursor() != emptyCursor)
				display.m_frame.getContentPane().setCursor(emptyCursor);
		}
		else { 
			if(display.m_frame.getContentPane().getCursor() != defCursor) 
				display.m_frame.getContentPane().setCursor(defCursor);
		}

		return false;
	}

	public void update() {}
	
	

	@Override
	public float getAccelerometerX() { return 0; }

	@Override
	public float getAccelerometerY() { return 0; }

	@Override
	public float getAccelerometerZ() { return 0; }

	@Override
	public float getGyroscopeX() { return 0; }

	@Override
	public float getGyroscopeY() { return 0; }

	@Override
	public float getGyroscopeZ() { return 0; }

	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint) {}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {}

	@Override
	public void vibrate(int milliseconds) {}

	@Override
	public void vibrate(long[] pattern, int repeat) {}

	@Override
	public void cancelVibrate() {}

	@Override
	public float getAzimuth()  { return 0; }

	@Override
	public float getPitch()  { return 0; }

	@Override
	public float getRoll()  { return 0; }

	@Override
	public void getRotationMatrix(float[] matrix) {}

	@Override
	public long getCurrentEventTime()  { return 0; }

	@Override
	public void setCatchBackKey(boolean catchBack) {}

	@Override
	public boolean isCatchBackKey() { return false; }

	@Override
	public void setCatchMenuKey(boolean catchMenu) {}

	@Override
	public boolean isCatchMenuKey() {return false;}

	@Override
	public void setInputProcessor(InputProcessor processor) {}

	@Override
	public InputProcessor getInputProcessor() {return null;}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {return false;}

	@Override
	public int getRotation() {return 0;}

}
