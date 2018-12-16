/*
 * ImageUtils for "POLYMOST" code written by Ken Silverman
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * 
 * See the included license file "BUILDLIC.TXT" for license info.
 */

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
			int wpptr, wp, dacol;
			for (int y = 0, x2, y2, x; y < ysiz; y++) {
				y2 = (y < tsizy) ? y : y - tsizy;
				wpptr = y * xsiz;
				for (x = 0; x < xsiz; x++, wpptr++) {
					wp = wpptr << 2;

					if (clamped && ((x >= tsizx) || (y >= tsizy))) { // Clamp texture
						pic[wp + 0] = pic[wp + 1] = pic[wp + 2] = pic[wp + 3] = 0;
						continue;
					}
					x2 = (x < tsizx) ? x : x - tsizx;
					if (x2 * tsizy + y2 >= data.length)
						break;

					dacol = data[x2 * tsizy + y2] & 0xFF;

					pic[wp + 3] = (byte) 255;

					if (alphaMode && dacol == 255) {
						pic[wp + 3] = 0;
						dacol = 0;
						hasalpha = true;
					} else {
						if(UseBloodPal && dapal == 1) //Blood's pal 1
						{
							int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
							dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
						} else dacol = palookup[dapal][dacol] & 0xFF;
					}

					dacol *= 3;
					if (gammabrightness != 0) {
						pic[wp + 0] = curpalette[dacol];
						pic[wp + 1] = curpalette[dacol + 1];
						pic[wp + 2] = curpalette[dacol + 2];
					} else {
						byte[] brighttable = britable[curbrightness];
						pic[wp + 0] = brighttable[curpalette[dacol] & 0xFF];
						pic[wp + 1] = brighttable[curpalette[dacol + 1] & 0xFF];
						pic[wp + 2] = brighttable[curpalette[dacol + 2] & 0xFF];
					}
				}
			}
		}
		
		if(hasalpha)
			fixtransparency(pic, tsizx, tsizy, xsiz, ysiz, clamped);
		
		return new PicInfo(pic, hasalpha);
	}

	private static void fixtransparency(byte[] dapic, int daxsiz, int daysiz, int daxsiz2, int daysiz2, boolean clamping) {
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
	
		// Set transparent pixels to average color of neighboring opaque pixels
		// Doing this makes bilinear filtering look much better for masked
		// textures (I.E. sprites)
		int r, g, b, j, index, wp, wpptr;
		for (int y = doy, x; y >= 0; y--) {
			wpptr = y * daxsiz2 + dox;
			for (x = dox; x >= 0; x--, wpptr--) {
				wp = (wpptr << 2);
				if (dapic[wp + 3] != 0) continue;
				
				r = g = b = j = 0;
				index = wp - 4;
				if ((x > 0) && (dapic[index + 3] != 0)) {
					r += dapic[index + 0] & 0xFF;
					g += dapic[index + 1] & 0xFF;
					b += dapic[index + 2] & 0xFF;
					j++;
				}
				index = wp + 4;
				if ((x < daxsiz) && (dapic[index + 3] != 0)) {
					r += dapic[index + 0] & 0xFF;
					g += dapic[index + 1] & 0xFF;
					b += dapic[index + 2] & 0xFF;
					j++;
				}
				index = wp - (daxsiz2 << 2);
				if ((y > 0) && (dapic[index + 3] != 0)) {
					r += dapic[index + 0] & 0xFF;
					g += dapic[index + 1] & 0xFF;
					b += dapic[index + 2] & 0xFF;
					j++;
				}
				index = wp + (daxsiz2 << 2);
				if ((y < daysiz) && (dapic[index + 3] != 0)) {
					r += dapic[index + 0] & 0xFF;
					g += dapic[index + 1] & 0xFF;
					b += dapic[index + 2] & 0xFF;
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
