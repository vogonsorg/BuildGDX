package ru.m210projects.Build.desktop.jogl;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;

public class JoglFrame extends BuildFrame implements GLEventListener {

	public JoglFrame(ApplicationListener listener, BuildConfiguration config) {
		super(listener, config);
	}

	@Override
	public void render() {}

	@Override
	public void create() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void setType(FrameType type) {
		super.setType(type);
		JoglGraphics graphics = (JoglGraphics) getGraphics();
		graphics.canvas.addGLEventListener(this);
		graphics.canvas.setVisible(true);
	}

	@Override
	public BuildGraphics getGraphics(FrameType type) {
		if (type == FrameType.GL) {
			return new JoglGraphics(config);
		}

//			if(type == FrameType.Canvas)
//				return new AWTGraphics(cfg);

		throw new UnsupportedOperationException("Unsupported frame type: " + type);
	}

	@Override
	public BuildInput getInput(FrameType type) {
		if (type == FrameType.GL) {
			return new JoglInput();
		}

//			if(type == FrameType.Canvas)
//				return new AWTInput();

		throw new UnsupportedOperationException("Unsupported frame type: " + type);
	}

	@Override
	public void display(GLAutoDrawable arg0) {
		listener.render();

		System.err.println("aaa");
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		JoglGraphics graphics = (JoglGraphics) getGraphics();
		try {
			graphics.initiateGL(drawable.getGL());
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't initialize GL");
		}
		listener.create();
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

}
