package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Render.GL10.GL_TRIANGLES;
import ru.m210projects.Build.Render.GL10;

public abstract class FadeEffect {
	public int sfactor; 
	public int dfactor;
	public int r, g, b, a; 
	
	public FadeEffect(int sfactor, int dfactor)
	{
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}
	
	public abstract void update(int intensive);

	public void draw(GL10 gl) {
		gl.bglBlendFunc(sfactor, dfactor);
		gl.bglColor4ub(r, g, b, a);

		gl.bglBegin(GL_TRIANGLES);
		gl.bglVertex2f(-2.5f, 1.f);
		gl.bglVertex2f(2.5f, 1.f);
		gl.bglVertex2f(.0f, -2.5f);
		gl.bglEnd();
	}
}








