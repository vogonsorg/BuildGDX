package ru.m210projects.Build.Render.GdxRender;

import static com.badlogic.gdx.graphics.GL20.GL_CCW;
import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_CW;
import static ru.m210projects.Build.Engine.TRANSLUSCENT1;
import static ru.m210projects.Build.Engine.TRANSLUSCENT2;
import static ru.m210projects.Build.Engine.globalvisibility;
import static ru.m210projects.Build.Engine.sector;
import static ru.m210projects.Build.Engine.sprite;
import static ru.m210projects.Build.Engine.totalclock;
import static ru.m210projects.Build.Pragmas.mulscale;

import com.badlogic.gdx.math.Matrix4;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager;
import ru.m210projects.Build.Render.GdxRender.Shaders.ShaderManager.Shader;
import ru.m210projects.Build.Render.ModelHandle.GLModel;
import ru.m210projects.Build.Render.ModelHandle.ModelInfo.Type;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;
import ru.m210projects.Build.Render.ModelHandle.Voxel.GLVoxel;
import ru.m210projects.Build.Render.TextureHandle.TileData.PixelFormat;
import ru.m210projects.Build.Script.DefScript;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.Tile;

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

		Tile pic = engine.getTile(tspr.picnum);
		int orientation = tspr.cstat;

		boolean xflip = (orientation & 4) != 0;
		boolean yflip = (orientation & 8) != 0;
		float xoff = tspr.xoffset;
		float yoff = yflip ? -tspr.yoffset : tspr.yoffset;
		float posx = tspr.x;
		float posy = tspr.y;
		float posz = tspr.z;

		if ((orientation & 128) == 0)
			posz -= ((m.zsiz * tspr.yrepeat) << 1);
		if (yflip && (orientation & 16) == 0)
			posz += ((pic.getHeight() * 0.5f) - m.zpiv) * tspr.yrepeat * 8.0f;

		float f = (tspr.xrepeat / 32.0f) * m.getScale();
		if ((sprite[tspr.owner].cstat & 48) != 16 && (sprite[tspr.owner].cstat & 48) != 32)
			f /= 1.25f;
		float g = (tspr.yrepeat / 32.0f) * m.getScale();

		transform.setToTranslation(posx / cam.xscale, posy / cam.xscale, posz / cam.yscale);
		transform.scale(xflip ? -f : f, f, yflip ? -g : g);
		transform.translate(xoff / 64.0f, 0, -yoff / 64.0f);
		transform.rotate(0, 0, 1, (float) Gameutils.AngleToDegrees(tspr.ang) - 90.0f);
		if (m.isRotating())
			transform.rotate(0, 0, -1, totalclock % 360);
		transform.translate(-m.xpiv / 64.0f, -m.ypiv / 64.0f, -m.zpiv / 64.0f);

		BuildGdx.gl.glEnable(GL_CULL_FACE);
		if (yflip ^ xflip)
			BuildGdx.gl.glFrontFace(GL_CCW);
		else
			BuildGdx.gl.glFrontFace(GL_CW);

		int vis = globalvisibility;
		if (sector[tspr.sectnum].visibility != 0)
			vis = mulscale(globalvisibility, (sector[tspr.sectnum].visibility + 16) & 0xFF, 4);

		parent.switchShader(
				parent.getTexFormat() != PixelFormat.Pal8 ? Shader.RGBWorldShader : Shader.IndexedWorldShader);
		manager.transform(transform);
		manager.frustum(null);

		float alpha = 1.0f;
		if ((tspr.cstat & 2) != 0) {
			if ((tspr.cstat & 512) == 0)
				alpha = TRANSLUSCENT1;
			else
				alpha = TRANSLUSCENT2;
		}

		m.render(tspr.pal & 0xFF, tspr.shade, 0, vis, alpha);

		BuildGdx.gl.glFrontFace(GL_CW);
		return 1;
	}

	public int mddraw(GLModel md, SPRITE tspr) {
		if (md == null)
			return 0;

		if (md.getType() == Type.Voxel)
			return voxdraw((GLVoxel) md, tspr);

		if (md.getType() == Type.Md2 || md.getType() == Type.Md3)
			return mddraw((MDModel) md, tspr) ? 1 : 0;

		return 0;
	}

	public boolean mddraw(MDModel m, SPRITE tspr) {
		if (m == null)
			return false;

		DefScript defs = parent.defs;
		m.updateanimation(defs.mdInfo, tspr);

		modelPrepare(m, tspr);

		int vis = globalvisibility;
		if (sector[tspr.sectnum].visibility != 0)
			vis = mulscale(globalvisibility, (sector[tspr.sectnum].visibility + 16) & 0xFF, 4);
		m.render(tspr.pal & 0xFF, tspr.shade, defs.mdInfo.getParams(tspr.picnum).skinnum, vis, 1.0f);

		BuildGdx.gl.glFrontFace(GL_CW);
		return true;
	}

	private void modelPrepare(MDModel m, SPRITE tspr) {
		ShaderManager manager = parent.manager;
		BuildCamera cam = parent.cam;

		Tile pic = engine.getTile(tspr.picnum);
		int orientation = tspr.cstat;

		boolean xflip = (orientation & 4) != 0;
		boolean yflip = (orientation & 8) != 0;
		float xoff = tspr.xoffset;
		float yoff = yflip ? -tspr.yoffset : tspr.yoffset;
		float posx = tspr.x;
		float posy = tspr.y;
		float posz = tspr.z;

		if ((orientation & 128) != 0 && (orientation & 48) != 32)
			posz += (pic.getHeight() * tspr.yrepeat) << 1;

		if (yflip)
			posz -= (pic.getHeight() * tspr.yrepeat) << 2;

		float f = (tspr.xrepeat / 32.0f) * m.getScale();
//		if ((sprite[tspr.owner].cstat & 48) != 16 && (sprite[tspr.owner].cstat & 48) != 32)
//			f /= 1.25f;
		float g = (tspr.yrepeat / 32.0f) * m.getScale();

		transform.setToTranslation(posx / cam.xscale, posy / cam.xscale, posz / cam.yscale);
		transform.translate(0, 0, -m.getYOffset(false) * g);
		transform.scale(f, xflip ? -f : f, yflip ? -g : g);
		transform.translate((xoff / 64.0f), 0, (-yoff / 64.0f) - m.getYOffset(true));
		transform.rotate(0, 0, 1, (float) Gameutils.AngleToDegrees(tspr.ang));
		if (m.isRotating())
			transform.rotate(0, 0, -1, totalclock % 360);

		transform.rotate(1, 0, 0, -90.0f);
		transform.scale(0.01f, 0.01f, 0.01f);

		BuildGdx.gl.glEnable(GL_CULL_FACE);
		if (yflip ^ xflip)
			BuildGdx.gl.glFrontFace(GL_CW);
		else
			BuildGdx.gl.glFrontFace(GL_CCW);

		parent.switchShader(
				parent.getTexFormat() != PixelFormat.Pal8 ? Shader.RGBWorldShader : Shader.IndexedWorldShader);
		manager.transform(transform);
		manager.frustum(null);

		float alpha = 1.0f;
		if ((tspr.cstat & 2) != 0) {
			if ((tspr.cstat & 512) == 0)
				alpha = TRANSLUSCENT1;
			else
				alpha = TRANSLUSCENT2;
		}

		manager.color(1.0f, 1.0f, 1.0f, alpha);
	}

}
