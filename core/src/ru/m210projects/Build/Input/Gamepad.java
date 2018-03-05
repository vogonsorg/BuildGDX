package ru.m210projects.Build.Input;

//import com.badlogic.gdx.controllers.Controller;
//import com.badlogic.gdx.controllers.ControllerListener;
//import com.badlogic.gdx.controllers.Controllers;
//import com.badlogic.gdx.controllers.PovDirection;
//import com.badlogic.gdx.math.Vector3;

public class Gamepad implements IGamepad {

//	private ControllerListener listiner;
	public String controllerName;
	public boolean hasControllers;
	public int buttonPressed;
	public int axisCode;
	public float axisValue;
	
	public Gamepad()
	{
		/*
		listiner = new ControllerListener() {

			@Override
			public void connected(Controller controller) {
				hasControllers = true;
				controllerName = controller.getName();
				System.out.println(controller.getName());
			}

			@Override
			public void disconnected(Controller controller) {
				System.out.println(controller.getName());
				hasControllers = false;
				controllerName = null;
			}

			@Override
			public boolean buttonDown(Controller controller, int buttonCode) {
				System.out.println(buttonCode);
				buttonPressed = buttonCode;
				return false;
			}

			@Override
			public boolean buttonUp(Controller controller, int buttonCode) {
				buttonPressed = -1;
				return false;
			}

			@Override
			public boolean axisMoved(Controller controller, int axis, float value) {
				System.out.println(controller.getName() + " " + axis);
				System.out.println(value);
				
				axisCode = axis;
				axisValue = value;
				return false;
			}

			@Override
			public boolean povMoved(Controller controller, int povCode,
					PovDirection value) {
				return false;
			}

			@Override
			public boolean xSliderMoved(Controller controller, int sliderCode,
					boolean value) {
				return false;
			}

			@Override
			public boolean ySliderMoved(Controller controller, int sliderCode,
					boolean value) {
				return false;
			}

			@Override
			public boolean accelerometerMoved(Controller controller,
					int accelerometerCode, Vector3 value) {
				return false;
			}
			
		};
	
		Controllers.addListener(listiner);
		if(Controllers.getControllers().size == 0)
			hasControllers = false;
		*/
	}
	
	@Override
	public boolean isKeyPressed(int buttonCode) {
		return buttonCode == buttonPressed; //XXX
	}

	@Override
	public float axisMoved(int axisCode) {
		
		return 0;
	}

	@Override
	public String getName() {
		if(hasControllers)
			return controllerName;
		return null;
	}
}
