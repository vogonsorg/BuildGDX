package ru.m210projects.Build.Render;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Engine.curbrightness;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.gammabrightness;
import static ru.m210projects.Build.Engine.globalshade;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Render.TextureUtils.tmpArray;
import ru.m210projects.Build.Types.Palette;

public class ImageUtils {

	public static class PicInfo {
		public final byte[] pic;
		public final boolean hasalpha;

		public PicInfo(byte[] pic, boolean hasalpha) {
			this.pic = pic;
			this.hasalpha = hasalpha;
		}
	}

	public static PicInfo loadPic(int xsiz, int ysiz, int tsizx, int tsizy, byte[] data, int dapal, boolean clamped, boolean alphaMode) {
		byte[] pic = tmpArray(xsiz, ysiz);
		boolean hasalpha = false;

		if (data == null) {
			// Force invalid textures to draw something - an almost purely
			// transparency texture
			// This allows the Z-buffer to be updated for mirrors (which are
			// invalidated textures)
			pic[0] = pic[1] = pic[2] = 0;
			pic[3] = 1;
			tsizx = tsizy = 1;
			hasalpha = true;
		} else {
			for (int y = 0; y < ysiz; y++) {
				int y2 = (y < tsizy) ? y : y - tsizy;
				int wpptr = y * xsiz;
				for (int x = 0; x < xsiz; x++, wpptr++) {
					int wp = 4 * wpptr;

					if (clamped && ((x >= tsizx) || (y >= tsizy))) { // Clamp texture
						pic[wp + 0] = pic[wp + 1] = pic[wp + 2] = pic[wp + 3] = 0;
						continue;
					}
					int x2 = (x < tsizx) ? x : x - tsizx;
					if (x2 * tsizy + y2 >= data.length)
						break;

					int dacol = data[x2 * tsizy + y2] & 0xFF;

					pic[wp + 3] = (byte) 255;

					if (alphaMode && dacol == 255) {
						pic[wp + 3] = 0;
						dacol = 0;
						hasalpha = true;
					} else {
						if(dapal == 1) //Blood's pal 1
						{
							int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
							dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
						} else
						dacol = palookup[dapal][dacol] & 0xFF;
					}

					Palette color = curpalette[dacol];
					if (gammabrightness != 0) {
						pic[wp + 0] = (byte) (color.r);
						pic[wp + 1] = (byte) (color.g);
						pic[wp + 2] = (byte) (color.b);
					} else {
						int[] brighttable = britable[curbrightness];
						pic[wp + 0] = (byte) brighttable[color.r];
						pic[wp + 1] = (byte) brighttable[color.g];
						pic[wp + 2] = (byte) brighttable[color.b];
					}
				}
			}
		}
		return new PicInfo(pic, hasalpha);
	}

	public static void fixtransparency(byte[] dapic, int daxsiz, int daysiz, int daxsiz2, int daysiz2, boolean clamping) {
		int dox = daxsiz2 - 1;
		int doy = daysiz2 - 1;
		if (clamping) {
			dox = min(dox, daxsiz);
			doy = min(doy, daysiz);
		} else {
			daxsiz = daxsiz2;
			daysiz = daysiz2;
		} // Make repeating textures duplicate top/left parts

		daxsiz--;
		daysiz--;
		int naxsiz2 = -daxsiz2; // Hacks for optimization inside loop

		// Set transparent pixels to average color of neighboring opaque pixels
		// Doing this makes bilinear filtering look much better for masked
		// textures (I.E. sprites)
		for (int y = doy; y >= 0; y--) {
			int wpptr = y * daxsiz2 + dox;
			for (int x = dox; x >= 0; x--, wpptr--) {
				int wp = 4 * wpptr;
				if (dapic[wp + 3] != 0)
					continue;
				int r = 0, g = 0, b = 0, j = 0;
				if ((x > 0) && (dapic[wp - 4 + 3] != 0)) {
					r += dapic[wp - 4 + 0];
					g += dapic[wp - 4 + 1];
					b += dapic[wp - 4 + 2];
					j++;
				}
				if ((x < daxsiz) && (dapic[wp + 4 + 3] != 0)) {
					r += dapic[wp + 4 + 0];
					g += dapic[wp + 4 + 1];
					b += dapic[wp + 4 + 2];
					j++;
				}
				int offset = 4 * naxsiz2;
				if ((y > 0) && (dapic[wp + offset + 3] != 0)) {
					r += dapic[wp + offset + 0];
					g += dapic[wp + offset + 1];
					b += dapic[wp + offset + 2];
					j++;
				}
				offset = 4 * daxsiz2;
				if ((y < daysiz) && (dapic[wp + offset + 3] != 0)) {
					r += dapic[wp + offset + 0];
					g += dapic[wp + offset + 1];
					b += dapic[wp + offset + 2];
					j++;
				}
				switch (j) {
				case 1:
					dapic[wp + 0] = (byte) r;
					dapic[wp + 1] = (byte) g;
					dapic[wp + 2] = (byte) b;
					break;
				case 2:
					dapic[wp + 0] = (byte) ((r + 1) >> 1);
					dapic[wp + 1] = (byte) ((g + 1) >> 1);
					dapic[wp + 2] = (byte) ((b + 1) >> 1);
					break;
				case 3:
					dapic[wp + 0] = (byte) ((r * 85 + 128) >> 8);
					dapic[wp + 1] = (byte) ((g * 85 + 128) >> 8);
					dapic[wp + 2] = (byte) ((b * 85 + 128) >> 8);
					break;
				case 4:
					dapic[wp + 0] = (byte) ((r + 2) >> 2);
					dapic[wp + 1] = (byte) ((g + 2) >> 2);
					dapic[wp + 2] = (byte) ((b + 2) >> 2);
					break;
				default:
					break;
				}
			}
		}
	}

}
