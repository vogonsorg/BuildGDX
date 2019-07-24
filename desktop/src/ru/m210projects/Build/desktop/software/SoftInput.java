package ru.m210projects.Build.desktop.software;

import static ru.m210projects.Build.Input.Keymap.KEY_CAPSLOCK;
import static ru.m210projects.Build.Input.Keymap.KEY_PAUSE;
import static ru.m210projects.Build.Input.Keymap.KEY_SCROLLOCK;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;

import ru.m210projects.Build.Architecture.BuildInput;

public class SoftInput implements BuildInput, MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;
		static final int TOUCH_MOVED = 3;
		static final int TOUCH_SCROLLED = 4;

		long timeStamp;
		int type;
		int x;
		int y;
		int pointer;
		int button;
		int scrollAmount;
	}

	Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
		protected KeyEvent newObject () {
			return new KeyEvent();
		}
	};

	Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
		protected TouchEvent newObject () {
			return new TouchEvent();
		}
	};

	
	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	int touchX = 0;
	int touchY = 0;
	int deltaX = 0;
	int deltaY = 0;
	int wheel = 0;
	boolean touchDown = false;
	boolean justTouched = false;
	int keyCount = 0;
	boolean[] keys = new boolean[256];
	boolean keyJustPressed = false;
	boolean[] justPressedKeys = new boolean[256];
	IntSet pressedButtons = new IntSet();
	InputProcessor processor;
	Canvas canvas;
	boolean catched = false;
	Robot robot = null;
	long currentEventTimeStamp;
	JDisplay display;
	
	protected Cursor noCursor;
	protected Cursor defCursor = Cursor.getDefaultCursor();
	private boolean mouseInside;
	
	protected void reset()
	{
		keyJustPressed = false;
		Arrays.fill(justPressedKeys, false);
		touchDown = false;
		touchX = 0;
		touchY = 0;
		deltaX = 0;
		deltaY = 0;
		wheel = 0;
		justTouched = false;
		keyCount = 0;
		Arrays.fill(keys, false);
	}

	public SoftInput () {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (Exception e) {}
	}
	
	private void mouseMove(int x, int y)
	{
		x += canvas.getLocationOnScreen().x;
		y += canvas.getLocationOnScreen().y;

		if(robot != null)
			robot.mouseMove(x, y);
	}
	
	public void update() {}
	
	public void init(JDisplay display)
	{
		this.display = display;
		this.setListeners(display.getCanvas());
	}

	public void setListeners (Canvas canvas) {
		if (this.canvas != null) {
			canvas.removeMouseListener(this);
			canvas.removeMouseMotionListener(this);
			canvas.removeMouseWheelListener(this);
			canvas.removeKeyListener(this);
		}
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addKeyListener(this);
		canvas.setFocusTraversalKeysEnabled(false);
		this.canvas = canvas;
	}

	@Override
	public float getAccelerometerX () {
		return 0;
	}

	@Override
	public float getAccelerometerY () {
		return 0;
	}

	@Override
	public float getAccelerometerZ () {
		return 0;
	}

	public void getTextInput (final TextInputListener listener, final String title, final String text, final String hint) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				JPanel panel = new JPanel(new FlowLayout());

				JPanel textPanel = new JPanel() {
					private static final long serialVersionUID = -4257888082210622881L;

					public boolean isOptimizedDrawingEnabled () {
						return false;
					};
				};

				textPanel.setLayout(new OverlayLayout(textPanel));
				panel.add(textPanel);

				final JTextField textField = new JTextField(20);
				textField.setText(text);
				textField.setAlignmentX(0.0f);
				textPanel.add(textField);

				final JLabel placeholderLabel = new JLabel(hint);
				placeholderLabel.setForeground(Color.GRAY);
				placeholderLabel.setAlignmentX(0.0f);
				textPanel.add(placeholderLabel, 0);

				textField.getDocument().addDocumentListener(new DocumentListener() {

					@Override
					public void removeUpdate (DocumentEvent arg0) {
						this.updated();
					}

					@Override
					public void insertUpdate (DocumentEvent arg0) {
						this.updated();
					}

					@Override
					public void changedUpdate (DocumentEvent arg0) {
						this.updated();
					}

					private void updated () {
						if (textField.getText().length() == 0)
							placeholderLabel.setVisible(true);
						else
							placeholderLabel.setVisible(false);
					}
				});

				JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null,
					null);

				pane.setInitialValue(null);
				pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());

				Border border = textField.getBorder();
				placeholderLabel.setBorder(new EmptyBorder(border.getBorderInsets(textField)));

				JDialog dialog = pane.createDialog(null, title);
				pane.selectInitialValue();

				dialog.addWindowFocusListener(new WindowFocusListener() {

					@Override
					public void windowLostFocus (WindowEvent arg0) {
					}

					@Override
					public void windowGainedFocus (WindowEvent arg0) {
						textField.requestFocusInWindow();
					}
				});

				dialog.setVisible(true);
				dialog.dispose();

				Object selectedValue = pane.getValue();

				if (selectedValue != null && (selectedValue instanceof Integer)
					&& ((Integer)selectedValue).intValue() == JOptionPane.OK_OPTION) {
					listener.input(textField.getText());
				} else {
					listener.canceled();
				}

			}
		});
	}

	@Override
	public int getX() {
		return touchX;
	}

	@Override
	public int getX (int pointer) {
		if (pointer == 0)
			return getX();
		else
			return 0;
	}

	@Override
	public int getY() {
		return touchY;
	}

	@Override
	public int getY (int pointer) {
		if (pointer == 0)
			return getY();
		else
			return 0;
	}

	@Override
	public boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyCount > 0;
		}
		if (key < 0 || key > 255) {
			return false;
		}
		return keys[key];
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyJustPressed;
		}
		if (key < 0 || key > 255) {
			return false;
		}
		return justPressedKeys[key];
	}

	@Override
	public boolean isTouched () {
		return touchDown;
	}

	@Override
	public boolean isTouched (int pointer) {
		if (pointer == 0)
			return touchDown;
		else
			return false;
	}

	void processEvents () {
//		synchronized (this) {
			justTouched = false;
			if (keyJustPressed) {
				keyJustPressed = false;
				Arrays.fill(justPressedKeys, false);
			}

			if (processor != null) {
				InputProcessor processor = this.processor;

				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.keyDown(e.keyCode);
						keyJustPressed = true;
						justPressedKeys[e.keyCode] = true;
						break;
					case KeyEvent.KEY_UP:
						processor.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						processor.keyTyped(e.keyChar);
					}
					usedKeyEvents.free(e);
				}

				len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case TouchEvent.TOUCH_DOWN:
						processor.touchDown(e.x, e.y, e.pointer, e.button);
						justTouched = true;
						break;
					case TouchEvent.TOUCH_UP:
						processor.touchUp(e.x, e.y, e.pointer, e.button);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						processor.touchDragged(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_MOVED:
						processor.mouseMoved(e.x, e.y);
						break;
					case TouchEvent.TOUCH_SCROLLED:
						processor.scrolled(e.scrollAmount);
						break;
					}
					usedTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent event = touchEvents.get(i);
					if (event != null && event.type == TouchEvent.TOUCH_DOWN) justTouched = true;
					usedTouchEvents.free(event);
				}

				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					usedKeyEvents.free(keyEvents.get(i));
				}
			}

			if (touchEvents.isEmpty()) {
				deltaX = 0;
				deltaY = 0;
				wheel = 0;
			}

			keyEvents.clear();
			touchEvents.clear();
//		}
	}

	@Override
	public void setCatchBackKey (boolean catchBack) {

	}

	@Override
	public boolean isCatchBackKey () {
		return false;
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {

	}

	@Override
	public boolean isCatchMenuKey () {
		return false;
	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {

	}

	@Override
	public void mouseDragged (MouseEvent e) {
//		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DRAGGED;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			checkCatched(e);
//		}
	}

	@Override
	public void mouseMoved (MouseEvent e) {
//		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_MOVED;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			checkCatched(e);
//		}
	}

	@Override
	public void mouseClicked (MouseEvent arg0) {
	}

	@Override
	public void mouseEntered (MouseEvent e) {
		touchX = e.getX();
		touchY = e.getY();
		mouseInside = true;
		checkCatched(e);
	}

	@Override
	public void mouseExited (MouseEvent e) {
		mouseInside = false;
		checkCatched(e);
	}

	private void checkCatched (MouseEvent e) {
		if(!display.isActive()) 
			return;
		
		if (catched && canvas.isShowing()) {
			if (e.getX() < 0 || e.getX() >= canvas.getWidth() || e.getY() < 0 || e.getY() >= canvas.getHeight()) {
				mouseMove(canvas.getWidth() / 2, canvas.getHeight() / 2);
				showCursor(false);
			}
		}
	}

	private int toGdxButton (int swingButton) {
		if (swingButton == MouseEvent.BUTTON1) return Buttons.LEFT;
		if (swingButton == MouseEvent.BUTTON2) return Buttons.MIDDLE;
		if (swingButton == MouseEvent.BUTTON3) return Buttons.RIGHT;
		return Buttons.LEFT;
	}

	@Override
	public void mousePressed (MouseEvent e) {
//		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DOWN;
			event.button = toGdxButton(e.getButton());
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			touchDown = true;
			pressedButtons.add(event.button);
//		}
	}

	@Override
	public void mouseReleased (MouseEvent e) {
//		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.button = toGdxButton(e.getButton());
			event.type = TouchEvent.TOUCH_UP;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			pressedButtons.remove(event.button);
			if (pressedButtons.size == 0) touchDown = false;
//		}
	}

	@Override
	public void mouseWheelMoved (MouseWheelEvent e) {
//		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.type = TouchEvent.TOUCH_SCROLLED;
			event.scrollAmount = e.getWheelRotation();
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);
			
			wheel = -event.scrollAmount;
//		}
	}

	@Override
	public void keyPressed (java.awt.event.KeyEvent e) {
//		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e);
			event.type = KeyEvent.KEY_DOWN;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			if (!keys[event.keyCode]) {
				keyCount++;
				keys[event.keyCode] = true;
			}
//		}
	}

	@Override
	public void keyReleased (java.awt.event.KeyEvent e) {
//		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e);
			event.type = KeyEvent.KEY_UP;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			if (keys[event.keyCode]) {
				keyCount--;
				keys[event.keyCode] = false;
			}
//		}
	}

	@Override
	public void keyTyped (java.awt.event.KeyEvent e) {
//		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = e.getKeyChar();
			event.keyCode = 0;
			event.type = KeyEvent.KEY_TYPED;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
//		}
	}

	protected static int translateKeyCode (java.awt.event.KeyEvent ke) {
		switch (ke.getKeyCode()) {
		case java.awt.event.KeyEvent.VK_MULTIPLY:
			return Keys.STAR;
		case java.awt.event.KeyEvent.VK_PAUSE:
			return KEY_PAUSE;
		case java.awt.event.KeyEvent.VK_CAPS_LOCK:
			return KEY_CAPSLOCK;
		case java.awt.event.KeyEvent.VK_SCROLL_LOCK:
			return KEY_SCROLLOCK;
		case java.awt.event.KeyEvent.VK_BACK_SPACE:
			return Keys.BACKSPACE;
		case java.awt.event.KeyEvent.VK_LEFT:
			return Keys.LEFT;
		case java.awt.event.KeyEvent.VK_RIGHT:
			return Keys.RIGHT;
		case java.awt.event.KeyEvent.VK_UP:
			return Keys.UP;
		case java.awt.event.KeyEvent.VK_DOWN:
			return Keys.DOWN;
		case java.awt.event.KeyEvent.VK_QUOTE:
			return Keys.APOSTROPHE;
		case java.awt.event.KeyEvent.VK_OPEN_BRACKET:
			return Keys.LEFT_BRACKET;
		case java.awt.event.KeyEvent.VK_CLOSE_BRACKET:
			return Keys.RIGHT_BRACKET;
		case java.awt.event.KeyEvent.VK_BACK_QUOTE:
			return Keys.GRAVE;
		case java.awt.event.KeyEvent.VK_NUM_LOCK:
			return Keys.NUM;
		case java.awt.event.KeyEvent.VK_EQUALS:
			return Keys.EQUALS;
		case java.awt.event.KeyEvent.VK_0:
			return Keys.NUM_0;
		case java.awt.event.KeyEvent.VK_1:
			return Keys.NUM_1;
		case java.awt.event.KeyEvent.VK_2:
			return Keys.NUM_2;
		case java.awt.event.KeyEvent.VK_3:
			return Keys.NUM_3;
		case java.awt.event.KeyEvent.VK_4:
			return Keys.NUM_4;
		case java.awt.event.KeyEvent.VK_5:
			return Keys.NUM_5;
		case java.awt.event.KeyEvent.VK_6:
			return Keys.NUM_6;
		case java.awt.event.KeyEvent.VK_7:
			return Keys.NUM_7;
		case java.awt.event.KeyEvent.VK_8:
			return Keys.NUM_8;
		case java.awt.event.KeyEvent.VK_9:
			return Keys.NUM_9;
		case java.awt.event.KeyEvent.VK_A:
			return Keys.A;
		case java.awt.event.KeyEvent.VK_B:
			return Keys.B;
		case java.awt.event.KeyEvent.VK_C:
			return Keys.C;
		case java.awt.event.KeyEvent.VK_D:
			return Keys.D;
		case java.awt.event.KeyEvent.VK_E:
			return Keys.E;
		case java.awt.event.KeyEvent.VK_F:
			return Keys.F;
		case java.awt.event.KeyEvent.VK_G:
			return Keys.G;
		case java.awt.event.KeyEvent.VK_H:
			return Keys.H;
		case java.awt.event.KeyEvent.VK_I:
			return Keys.I;
		case java.awt.event.KeyEvent.VK_J:
			return Keys.J;
		case java.awt.event.KeyEvent.VK_K:
			return Keys.K;
		case java.awt.event.KeyEvent.VK_L:
			return Keys.L;
		case java.awt.event.KeyEvent.VK_M:
			return Keys.M;
		case java.awt.event.KeyEvent.VK_N:
			return Keys.N;
		case java.awt.event.KeyEvent.VK_O:
			return Keys.O;
		case java.awt.event.KeyEvent.VK_P:
			return Keys.P;
		case java.awt.event.KeyEvent.VK_Q:
			return Keys.Q;
		case java.awt.event.KeyEvent.VK_R:
			return Keys.R;
		case java.awt.event.KeyEvent.VK_S:
			return Keys.S;
		case java.awt.event.KeyEvent.VK_T:
			return Keys.T;
		case java.awt.event.KeyEvent.VK_U:
			return Keys.U;
		case java.awt.event.KeyEvent.VK_V:
			return Keys.V;
		case java.awt.event.KeyEvent.VK_W:
			return Keys.W;
		case java.awt.event.KeyEvent.VK_X:
			return Keys.X;
		case java.awt.event.KeyEvent.VK_Y:
			return Keys.Y;
		case java.awt.event.KeyEvent.VK_Z:
			return Keys.Z;
		case java.awt.event.KeyEvent.VK_ALT:
			if(ke.getKeyLocation() == java.awt.event.KeyEvent.KEY_LOCATION_LEFT)
				return Keys.ALT_LEFT;
			return Keys.ALT_RIGHT;
		case java.awt.event.KeyEvent.VK_BACK_SLASH:
			return Keys.BACKSLASH;
		case java.awt.event.KeyEvent.VK_COMMA:
			return Keys.COMMA;
		case java.awt.event.KeyEvent.VK_DELETE:
			return Keys.FORWARD_DEL;
		case java.awt.event.KeyEvent.VK_ENTER:
			return Keys.ENTER;
		case java.awt.event.KeyEvent.VK_HOME:
			return Keys.HOME;
		case java.awt.event.KeyEvent.VK_END:
			return Keys.END;
		case java.awt.event.KeyEvent.VK_PAGE_DOWN:
			return Keys.PAGE_DOWN;
		case java.awt.event.KeyEvent.VK_PAGE_UP:
			return Keys.PAGE_UP;
		case java.awt.event.KeyEvent.VK_INSERT:
			return Keys.INSERT;
		case java.awt.event.KeyEvent.VK_SUBTRACT:
		case java.awt.event.KeyEvent.VK_MINUS:
			return Keys.MINUS;
		case java.awt.event.KeyEvent.VK_PERIOD:
			return Keys.PERIOD;
		case java.awt.event.KeyEvent.VK_ADD:
		case java.awt.event.KeyEvent.VK_PLUS:
			return Keys.PLUS;
		case java.awt.event.KeyEvent.VK_SEMICOLON:
			return Keys.SEMICOLON;
		case java.awt.event.KeyEvent.VK_SHIFT:
			if(ke.getKeyLocation() == java.awt.event.KeyEvent.KEY_LOCATION_LEFT)
				return Keys.SHIFT_LEFT;
			return Keys.SHIFT_RIGHT;
		case java.awt.event.KeyEvent.VK_SLASH:
		case java.awt.event.KeyEvent.VK_DIVIDE:
			return Keys.SLASH;
		case java.awt.event.KeyEvent.VK_SPACE:
			return Keys.SPACE;
		case java.awt.event.KeyEvent.VK_TAB:
			return Keys.TAB;
		case java.awt.event.KeyEvent.VK_DECIMAL:
			return Keys.FORWARD_DEL; // Keys.DEL;
		case java.awt.event.KeyEvent.VK_CONTROL:
			if(ke.getKeyLocation() == java.awt.event.KeyEvent.KEY_LOCATION_LEFT)
				return Keys.CONTROL_LEFT;
			return Keys.CONTROL_RIGHT;
		case java.awt.event.KeyEvent.VK_ESCAPE:
			return Keys.ESCAPE;
		case java.awt.event.KeyEvent.VK_F1:
			return Keys.F1;
		case java.awt.event.KeyEvent.VK_F2:
			return Keys.F2;
		case java.awt.event.KeyEvent.VK_F3:
			return Keys.F3;
		case java.awt.event.KeyEvent.VK_F4:
			return Keys.F4;
		case java.awt.event.KeyEvent.VK_F5:
			return Keys.F5;
		case java.awt.event.KeyEvent.VK_F6:
			return Keys.F6;
		case java.awt.event.KeyEvent.VK_F7:
			return Keys.F7;
		case java.awt.event.KeyEvent.VK_F8:
			return Keys.F8;
		case java.awt.event.KeyEvent.VK_F9:
			return Keys.F9;
		case java.awt.event.KeyEvent.VK_F10:
			return Keys.F10;
		case java.awt.event.KeyEvent.VK_F11:
			return Keys.F11;
		case java.awt.event.KeyEvent.VK_F12:
			return Keys.F12;
		case java.awt.event.KeyEvent.VK_COLON:
			return Keys.COLON;
		case java.awt.event.KeyEvent.VK_NUMPAD0:
			return Keys.NUMPAD_0;
		case java.awt.event.KeyEvent.VK_NUMPAD1:
			return Keys.NUMPAD_1;
		case java.awt.event.KeyEvent.VK_NUMPAD2:
			return Keys.NUMPAD_2;
		case java.awt.event.KeyEvent.VK_NUMPAD3:
			return Keys.NUMPAD_3;
		case java.awt.event.KeyEvent.VK_NUMPAD4:
			return Keys.NUMPAD_4;
		case java.awt.event.KeyEvent.VK_NUMPAD5:
			return Keys.NUMPAD_5;
		case java.awt.event.KeyEvent.VK_NUMPAD6:
			return Keys.NUMPAD_6;
		case java.awt.event.KeyEvent.VK_NUMPAD7:
			return Keys.NUMPAD_7;
		case java.awt.event.KeyEvent.VK_NUMPAD8:
			return Keys.NUMPAD_8;
		case java.awt.event.KeyEvent.VK_NUMPAD9:
			return Keys.NUMPAD_9;
		}
		return Input.Keys.UNKNOWN;
	}

	@Override
	public void setInputProcessor (InputProcessor processor) {
//		synchronized (this) {
			this.processor = processor;
//		}
	}

	@Override
	public InputProcessor getInputProcessor () {
		return this.processor;
	}

	@Override
	public void vibrate (int milliseconds) {
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isButtonPressed (int button) {
		return pressedButtons.contains(button);
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
	}

	@Override
	public void cancelVibrate () {
	}

	@Override
	public float getAzimuth () {
		return 0;
	}

	@Override
	public float getPitch () {
		return 0;
	}

	@Override
	public float getRoll () {
		return 0;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		if (peripheral == Peripheral.HardwareKeyboard) return true;
		return false;
	}

	@Override
	public int getRotation () {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation () {
		return Orientation.Landscape;
	}

	@Override
	public void setCursorCatched (boolean catched) {
		this.catched = catched;
		showCursor(!catched);

		if(catched)
			mouseMove(canvas.getWidth() / 2, canvas.getHeight() / 2);
	}

	private void showCursor (boolean visible) {
		if (!visible) {
			if(noCursor == null) {
				Toolkit t = Toolkit.getDefaultToolkit();
				Image i = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				noCursor = t.createCustomCursor(i, new Point(0, 0), "none");
			}
			if(display.m_frame.getContentPane().getCursor() != noCursor)
				display.m_frame.getContentPane().setCursor(noCursor);
		} else {
			if(display.m_frame.getContentPane().getCursor() != defCursor) 
				display.m_frame.getContentPane().setCursor(defCursor);
		}
	}

	@Override
	public boolean isCursorCatched () {
		return catched;
	}

	@Override
	public int getDeltaX () {
		return deltaX;
	}

	@Override
	public int getDeltaX (int pointer) {
		if (pointer == 0) return deltaX;
		return 0;
	}

	@Override
	public int getDeltaY () {
		return deltaY;
	}

	@Override
	public int getDeltaY (int pointer) {
		if (pointer == 0) return deltaY;
		return 0;
	}

	@Override
	public void setCursorPosition (int x, int y) {
		if(!isInsideWindow() || !display.isActive()) 
			return;
		
		mouseMove(x, y);
	}

	@Override
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
	}

	@Override
	public float getGyroscopeX () {
		return 0;
	}

	@Override
	public float getGyroscopeY () {
		return 0;
	}

	@Override
	public float getGyroscopeZ () {
		return 0;
	}

	@Override
	public void processMessages() {}

	@Override
	public boolean cursorHandler() {
		if (isInsideWindow() && display.isActive())
			showCursor(false);
		else showCursor(true);

		return false;
	}

	@Override
	public int getDWheel() {
		return wheel;
	}
	
	public boolean isInsideWindow() {
		return mouseInside;
	}
}
