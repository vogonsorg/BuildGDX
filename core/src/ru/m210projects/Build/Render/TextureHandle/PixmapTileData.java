package ru.m210projects.Build.Render.TextureHandle;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class PixmapTileData extends TileData {

	private Pixmap pixmap;
	private boolean clamped;
	private int width, height;

	public PixmapTileData(Pixmap pixmap, boolean clamped, int expflag) {
		this.pixmap = pixmap;
		this.clamped = clamped;

		width = pixmap.getWidth();
		height = pixmap.getHeight();

		int xsiz = width;
		int ysiz = height;
		if ((expflag & 1) != 0)
			xsiz = calcSize(width);
		if ((expflag & 2) != 0)
			ysiz = calcSize(height);

		if (xsiz != width || ysiz != height) {
			Pixmap npix = new Pixmap(xsiz, ysiz, pixmap.getFormat());
			npix.setFilter(Filter.NearestNeighbour);

			if (!clamped) {
				for (int x = 0, y; x < xsiz; x += width) {
					for (y = 0; y < ysiz; y += height) {
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

	public int getTileWidth() {
		return width;
	}

	public int getTileHeight() {
		return height;
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
