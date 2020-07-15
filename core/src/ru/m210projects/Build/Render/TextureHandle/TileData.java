package ru.m210projects.Build.Render.TextureHandle;

import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Format;

import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.Types.DirectTextureBuffer;
import ru.m210projects.Build.Render.Types.FastTextureBuffer;
import ru.m210projects.Build.Render.Types.TextureBuffer;

public abstract class TileData {

	public enum PixelFormat {
		Rgb(3), Rgba(4), Pal8(1), Pal8A(3);

		private final int bytes;
		PixelFormat(int bytes) {
			this.bytes = bytes;
		}

		public int getLength() {
			return bytes;
		}
	}

	private static final int TEX_MAX_SIZE = 1024;
	private static TextureBuffer tmp_buffer;

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract ByteBuffer getPixels();

	public abstract int getGLType();

	public abstract int getGLInternalFormat();

	public abstract int getGLFormat();

	public abstract PixelFormat getPixelFormat();

	public abstract boolean hasAlpha();

	public abstract boolean isClamped();

	public abstract boolean isHighTile();

	public void save(String name) {
		int width = getWidth();
		int height = getHeight();

		Pixmap pixmap = new Pixmap(width, height, Format.RGB888);
		ByteBuffer pixels = getPixels();
		int bytes = getPixelFormat().getLength();

		float[] color = new float[4];
		for (int i = 0; i < (width * height); i++) {
			for(int c = 0; c < bytes; c++)
				color[c] = (pixels.get((i * bytes) + c) & 0xFF) / 255.f;

			pixmap.setColor(color[0], color[1], color[2], 1.0f);
			int row = (int) Math.floor(i / width);
			int col = i % width;
			pixmap.drawPixel(col, row);
		}

		PixmapIO.writePNG(new FileHandle(name), pixmap);

		System.out.println(name + " saved!");
		pixmap.dispose();
	}

	protected int calcSize(int size) {
		int nsize = 1;
		if (GLInfo.texnpot == 0) {
			for (; nsize < size; nsize *= 2);
			return nsize;
		}
		return size == 0 ? 1 : size;
	}

	protected static TextureBuffer getTmpBuffer(int size) {
		if(tmp_buffer == null) {
			size = TEX_MAX_SIZE * TEX_MAX_SIZE * 4;
			try {
	//			if(BuildGdx.app.getPlatform() != Platform.Android) XXX
					tmp_buffer = new FastTextureBuffer(size);
	//			else tmp_buffer = new AndroidTextureBuffer(size);
			} catch (Exception e) {
				e.printStackTrace();
				tmp_buffer = new DirectTextureBuffer(size);
			}
		}
		return tmp_buffer;

//		return ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
	}
}
