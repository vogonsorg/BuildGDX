package ru.m210projects.Build.Input;

import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;

public interface BController extends Controller {

	public int getControlCount(ControlType type);
	
}
