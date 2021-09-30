package ru.m210projects.Build.Render.GdxRender;

import static com.badlogic.gdx.graphics.GL20.GL_BACK;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_CCW;
import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_CW;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static ru.m210projects.Build.Engine.inpreparemirror;
import static ru.m210projects.Build.Engine.sprite;
import static ru.m210projects.Build.Engine.totalclock;

import com.badlogic.gdx.math.Matrix4;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager.Shader;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.TextureHandle.GLTile;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Types.SPRITE;

public class GDXModelRenderer {

	private Matrix4 transform;
	private GDXRenderer parent;
	private Engine engine;

	public GDXModelRenderer(Engine engine, GDXRenderer parent) {
		this.transform = new Matrix4();
		this.parent = parent;
		this.engine = engine;
	}

	public int voxdraw(GLVoxel m, SPRITE tspr) {
		if (m == null)
			return 0;

		if ((sprite[tspr.owner].cstat & 48) == 32)
			return 0;

		ShaderManager manager = parent.manager;
		BuildCamera cam = parent.cam;

		int picnum = tspr.picnum;
		int shade = tspr.shade;
		int pal = tspr.pal & 0xFF;
		int orientation = tspr.cstat;
		int spritenum = tspr.owner;

		GLTile skin = m.getSkin(parent.getTexFormat(), pal);
		if (skin == null) {
			if ((skin = m.loadSkin(
					parent.textureCache.newTile(parent.getTexFormat(), m.getSkinWidth(), m.getSkinHeight()),
					pal)) == null)
				return 0;
		}

		boolean xflip = (orientation & 4) != 0;
		boolean yflip = (orientation & 8) != 0;
		float xoff = tspr.xoffset;
		float yoff = tspr.yoffset;
		if (yflip)
			yoff = -yoff;

		float posx = tspr.x;
		float posy = tspr.y;
		float posz = tspr.z;

		if ((orientation & 128) == 0)
			posz -= ((m.zsiz * tspr.yrepeat) << 1);
		if (yflip && (orientation & 16) == 0)
			posz += ((engine.getTile(tspr.picnum).getHeight() * 0.5f) - m.zpiv) * tspr.yrepeat * 8.0f;

		float sx = m.getScale();
		float sy = m.getScale();
		float sz = m.getScale();

		float f = (tspr.xrepeat) * (256.0f / 320.0f) / 32.0f;
		if ((sprite[spritenum].cstat & 48) == 16 || (sprite[spritenum].cstat & 48) == 32)
			f *= 1.25f;
		sx *= xflip ? -f : f;
		sy *= f;
		sz *= (tspr.yrepeat) / 32.0f;

		transform.setToTranslation(posx / cam.xscale, posy / cam.xscale, posz / cam.yscale);
		transform.scale(sx, sy, sz);
		transform.translate(-xoff / cam.xscale, yoff / cam.xscale, 0);
		transform.rotate(0, 0, 1, (float) Gameutils.AngleToDegrees(tspr.ang) - 90.0f);
		if (m.isRotating())
			transform.rotate(0, 0, 1, totalclock % 360);
		transform.translate(-m.xpiv / 64.0f, -m.ypiv / 64.0f, -m.zpiv / 64.0f);

		BuildGdx.gl.glEnable(GL_CULL_FACE);
		if (yflip ^ xflip)
			BuildGdx.gl.glFrontFace(GL_CCW);
		else
			BuildGdx.gl.glFrontFace(GL_CW);

		BuildGdx.gl.glEnable(GL_TEXTURE_2D);
		if ((tspr.cstat & 2) != 0)
			BuildGdx.gl.glEnable(GL_BLEND);

		skin.bind();
		parent.switchShader(
				parent.getTexFormat() != PixelFormat.Pal8 ? Shader.RGBWorldShader : Shader.IndexedWorldShader);
		parent.setTextureParameters(skin, picnum, pal, shade, 0, tspr.cstat & 2);

		manager.transform(transform);
		manager.frustum(null);
		m.render(manager.getProgram());

		BuildGdx.gl.glFrontFace(GL_CW);
		return 1;
	}

}
