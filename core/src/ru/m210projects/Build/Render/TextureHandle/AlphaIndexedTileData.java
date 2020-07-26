package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.*;

import ru.m210projects.Build.Types.Tile;

public class AlphaIndexedTileData extends RGBTileData {

	public AlphaIndexedTileData(Tile tile, boolean clamped, boolean alpha, int expflag) {
		super(tile, 0, clamped, alpha, expflag);
	}

	@Override
	public int getGLInternalFormat() {
		return GL_LUMINANCE_ALPHA;
	}

	@Override
	public int getGLFormat() {
		return GL_RGB;
	}

	@Override
	public PixelFormat getPixelFormat() {
		return PixelFormat.Pal8A;
	}

	@Override
	protected int getColor(int dacol, int dapal, boolean alphaMode) {
		dacol &= 0xFF;
		if (alphaMode && dacol == 255)
			return dacol;

		return dacol | 0xFF000000;
	}

}
