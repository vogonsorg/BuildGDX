package ru.m210projects.Build.Architecture;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Render.Types.GL10;

public class BuildGDX {
	public static BuildApplication app;
	public static BuildGraphics graphics;
//	public static Audio audio;
	public static BuildInput input;
	public static BuildMessage message;
	public static Files files;
	public static Net net;

	public static GL10 gl;
	public static GL20 gl20;
	public static GL30 gl30;
	
	public static Engine engine;
}
