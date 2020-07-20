package ru.m210projects.Build.Render;

import static com.badlogic.gdx.graphics.GL20.GL_DONT_CARE;
import static com.badlogic.gdx.graphics.GL20.GL_LINEAR;
import static com.badlogic.gdx.graphics.GL20.GL_NICEST;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookupfog;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG_COLOR;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG_END;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG_HINT;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG_MODE;
import static ru.m210projects.Build.Render.Types.GL10.GL_FOG_START;

import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.BufferUtils;

import ru.m210projects.Build.Architecture.BuildGdx;

public class GLFog {

	// For GL_LINEAR fog:
	public final int FOGDISTCONST = 600;
	public final float FULLVIS_BEGIN = (float) 2.9e30;
	public final float FULLVIS_END = (float) 3.0e30;

	public int shade, pal;
	public float combvis;

	public boolean nofog;

	protected final FloatBuffer color = BufferUtils.newFloatBuffer(4);

	public void init() {
		if (BuildGdx.graphics.getGLVersion().getVendorString().compareTo("NVIDIA Corporation") == 0) {
			BuildGdx.gl.glHint(GL_FOG_HINT, GL_NICEST);
		} else {
			BuildGdx.gl.glHint(GL_FOG_HINT, GL_DONT_CARE);
		}
		BuildGdx.gl.glFogi(GL_FOG_MODE, GL_LINEAR); // GL_EXP
	}

	public void copy(GLFog src) {
		this.shade = src.shade;
		this.combvis = src.combvis;
		this.pal = src.pal;
	}

	public void clear() {
		shade = 0;
		combvis = 0;
		pal = 0;
	}

	public void apply() {
		if (nofog)
			return;

		float start, end;
		if (combvis == 0) {
			start = FULLVIS_BEGIN;
			end = FULLVIS_END;
		} else if (shade >= numshades - 1) {
			start = -1;
			end = 0.001f;
		} else {
			start = (shade > 0) ? 0 : -(FOGDISTCONST * shade) / combvis;
			end = (FOGDISTCONST * (numshades - 1 - shade)) / combvis;
		}

		color.clear();
		color.put(palookupfog[pal][0] / 63.f);
		color.put(palookupfog[pal][1] / 63.f);
		color.put(palookupfog[pal][2] / 63.f);
		color.put(0);
		color.flip();

		BuildGdx.gl.glFogfv(GL_FOG_COLOR, color);
		BuildGdx.gl.glFogf(GL_FOG_START, start);
		BuildGdx.gl.glFogf(GL_FOG_END, end);
	}

	public void enable() {
		if (!nofog)
			BuildGdx.gl.glEnable(GL_FOG);
	}

	public void disable() {
		BuildGdx.gl.glDisable(GL_FOG);
	}
}
