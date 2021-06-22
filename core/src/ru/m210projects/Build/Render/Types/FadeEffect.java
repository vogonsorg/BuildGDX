//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Render.Types;

import static ru.m210projects.Build.Render.Types.GL10.GL_TRIANGLES;

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import ru.m210projects.Build.Architecture.BuildGdx;

public abstract class FadeEffect {
	public int sfactor;
	public int dfactor;
	public int r, g, b, a;

	public static class FadeShader extends ShaderProgram {
		public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   gl_Position =  " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";

		public static final String fragmentShader = "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "uniform vec4 u_color;\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "	gl_FragColor = u_color;\n" //
				+ "}\n"; //

		public final int color;

		public FadeShader() {
			super(vertexShader, fragmentShader);
			color = getUniformLocation("u_color");
		}

		public void setColor(int r, int g, int b, int a) {
			setUniformf(color, r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
		}
	};

	public static final Mesh mesh = new Mesh(true, 3, 0,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE)) {
		@Override
		public void render(ShaderProgram shader, int primitiveType) {
			if (shader == null) {
				FloatBuffer vertices = getVerticesBuffer();
				BuildGdx.gl.glBegin(primitiveType);
				for (int i = 0; i < 6; i += 2)
					BuildGdx.gl.glVertex2f(vertices.get(i), vertices.get(i + 1));
				BuildGdx.gl.glEnd();
				return;
			}

			super.render(shader, primitiveType);
		}
	}.setVertices(new float[] { -2.5f, 1.0f, 2.5f, 1.0f, 0.0f, -2.5f });

	public FadeEffect(int sfactor, int dfactor) {
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}

	public abstract void update(int intensive);

	public static void setParams(FadeShader shader, int r, int g, int b, int a, int sfactor, int dfactor) {
		BuildGdx.gl.glBlendFunc(sfactor, dfactor);
		if (shader == null)
			BuildGdx.gl.glColor4ub(r, g, b, a);
		else
			shader.setColor(r, g, b, a);
	}

	public static void render(FadeShader shader) {
		mesh.render(shader, GL_TRIANGLES);
	}

	public void draw(FadeShader shader) {
		setParams(shader, r, g, b, a, sfactor, dfactor);
		render(shader);
	}
}
