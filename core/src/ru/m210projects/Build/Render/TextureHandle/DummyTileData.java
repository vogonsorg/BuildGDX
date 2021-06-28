package ru.m210projects.Build.Render.TextureHandle;

import static com.badlogic.gdx.graphics.GL20.GL_RGBA;
import static com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE;

import java.nio.ByteBuffer;

import ru.m210projects.Build.Render.Types.DirectTextureBuffer;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public class DummyTileData extends TileData {

	public final TextureBuffer data;
	public final int width, height;

	public DummyTileData(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new DirectTextureBuffer(width * height * 4);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public ByteBuffer getPixels() {
		return data.getBuffer();
	}

	@Override
	public int getGLType() {
		return GL_UNSIGNED_BYTE;
	}

	@Override
	public int getGLInternalFormat() {
		return GL_RGBA;
	}

	@Override
	public int getGLFormat() {
		return GL_RGBA;
	}

	@Override
	public PixelFormat getPixelFormat() {
		return PixelFormat.Rgba;
	}

	@Override
	public boolean hasAlpha() {
		return true;
	}

	@Override
	public boolean isClamped() {
		return false;
	}

	@Override
	public boolean isHighTile() {
		return false;
	}
}
