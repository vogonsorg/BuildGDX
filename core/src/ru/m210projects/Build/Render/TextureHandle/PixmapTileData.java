package ru.m210projects.Build.Render.TextureHandle;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class PixmapTileData extends TileData {

	private Pixmap pixmap;
	private boolean clamped;

	public PixmapTileData(Pixmap pixmap, boolean clamped, int expflag) {
		this.pixmap = pixmap;
		this.clamped = clamped;

		int tsizx = pixmap.getWidth();
		int tsizy = pixmap.getHeight();

		int xsiz = tsizx;
		int ysiz = tsizy;
		if ((expflag & 1) != 0)
			xsiz = calcSize(tsizx);
		if ((expflag & 2) != 0)
			ysiz = calcSize(tsizy);

		if (xsiz != tsizx || ysiz != tsizy) {
			Pixmap npix = new Pixmap(xsiz, ysiz, pixmap.getFormat());
			npix.setFilter(Filter.NearestNeighbour);

			if (!clamped) {
				for (int x = 0, y; x < xsiz; x += tsizx) {
					for (y = 0; y < ysiz; y += tsizy) {
						npix.drawPixmap(pixmap, x, y);
					}
				}
			} else
				npix.drawPixmap(pixmap, 0, 0);

			pixmap.dispose();
			this.pixmap = npix;
		}
	}

	@Override
	public boolean hasAlpha() {
		return pixmap.getFormat() == Format.RGBA4444 || pixmap.getFormat() == Format.RGBA8888;
	}

	@Override
	public boolean isClamped() {
		return clamped;
	}

	@Override
	public int getWidth() {
		return pixmap.getWidth();
	}

	@Override
	public int getHeight() {
		return pixmap.getHeight();
	}

	@Override
	public ByteBuffer getPixels() {
		return pixmap.getPixels();
	}

	@Override
	public int getGLType() {
		return pixmap.getGLType();
	}

	@Override
	public int getGLInternalFormat() {
		return pixmap.getGLInternalFormat();
	}

	@Override
	public int getGLFormat() {
		return pixmap.getGLFormat();
	}

	@Override
	public boolean isHighTile() {
		return true;
	}

	@Override
	public PixelFormat getPixelFormat() {
		return PixelFormat.Rgb;
	}
}
