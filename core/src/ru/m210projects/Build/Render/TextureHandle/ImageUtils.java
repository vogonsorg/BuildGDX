/*
 * ImageUtils for "POLYMOST" code written by Ken Silverman
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * 
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.TextureHandle;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Engine.curbrightness;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.gammabrightness;
import static ru.m210projects.Build.Engine.globalshade;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.*;

import java.nio.ByteBuffer;

public class ImageUtils {

	public static class PicInfo {
		public final ByteBuffer pic;
		public final boolean hasalpha;

		public PicInfo(ByteBuffer pic, boolean hasalpha) {
			this.pic = pic;
			this.hasalpha = hasalpha;
		}
	}

	public static PicInfo loadPic(int xsiz, int ysiz, int tsizx, int tsizy, byte[] data, int dapal, boolean clamped, boolean alphaMode, boolean isPaletted) {
		ByteBuffer buffer = getTmpBuffer();
		boolean hasalpha = false;
		buffer.clear();
		
		if(isPaletted)
		{
			if (data == null) {
				buffer.put(0, (byte) 0);
				tsizx = tsizy = 1;
				hasalpha = true;
			} else {
				int wpptr, wp;
				for (int y = 0, x2, y2, x; y < ysiz; y++) {
					y2 = (y < tsizy) ? y : y - tsizy;
					wpptr = y * xsiz;
					for (x = 0; x < xsiz; x++, wpptr++) {
						wp = wpptr << 2;
	
						if (clamped && ((x >= tsizx) || (y >= tsizy))) { // Clamp texture
							buffer.put(wp, (byte) 255);
							continue;
						}
						x2 = (x < tsizx) ? x : x - tsizx;
						buffer.put(wp, data[x2 * tsizy + y2]);
					}
				}
			}
			
			return new PicInfo(buffer, hasalpha);
		}

		if (data == null) {
			buffer.putInt(0, 0);
			tsizx = tsizy = 1;
			hasalpha = true;
		} else {
			int wpptr, wp, dacol;
			int rgb = 0, r, g, b, a;
			for (int y = 0, x2, y2, x; y < ysiz; y++) {
				y2 = (y < tsizy) ? y : y - tsizy;
				wpptr = y * xsiz;
				for (x = 0; x < xsiz; x++, wpptr++) {
					wp = wpptr << 2;

					if (clamped && ((x >= tsizx) || (y >= tsizy))) { // Clamp texture
						buffer.putInt(wp, 0);
						continue;
					}
					x2 = (x < tsizx) ? x : x - tsizx;
					if (x2 * tsizy + y2 >= data.length) //sizx/y = 0 protect
						break;
					
					dacol = data[x2 * tsizy + y2] & 0xFF;

					a = 255;

					if (alphaMode && dacol == 255) {
						a = 0;
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
					if (gammabrightness == 0) {
						r = curpalette[dacol + 0] & 0xFF;
						g = curpalette[dacol + 1] & 0xFF;
						b = curpalette[dacol + 2] & 0xFF;
					} else {
						byte[] brighttable = britable[curbrightness];
						r = brighttable[curpalette[dacol + 0] & 0xFF] & 0xFF;
						g = brighttable[curpalette[dacol + 1] & 0xFF] & 0xFF;
						b = brighttable[curpalette[dacol + 2] & 0xFF] & 0xFF;
					}
					rgb = ( a << 24 ) + ( b << 16 ) + ( g << 8 ) + ( r << 0 );
					buffer.putInt(wp, rgb);
				}
			}
		}

		if(hasalpha) 
			fixtransparency(buffer, tsizx, tsizy, xsiz, ysiz, clamped);

		return new PicInfo(buffer, hasalpha);
	}
	
	private static void fixtransparency(ByteBuffer dapic, int daxsiz, int daysiz, int daxsiz2, int daysiz2, boolean clamping) {
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
		int r, g, b, j, index, wp, wpptr, rgb;
		for (int y = doy, x; y >= 0; y--) {
			wpptr = y * daxsiz2 + dox;
			for (x = dox; x >= 0; x--, wpptr--) {
				wp = (wpptr << 2);
				if(dapic.get(wp + 3) != 0) continue;
			
				r = g = b = j = 0;
				index = wp - 4;
				if ((x > 0) && (dapic.get(index + 3) != 0)) {
					r += dapic.get(index + 0) & 0xFF;
					g += dapic.get(index + 1) & 0xFF;
					b += dapic.get(index + 2) & 0xFF;
					j++;
				}
				index = wp + 4;
				if ((x < daxsiz) && (dapic.get(index + 3) != 0)) {
					r += dapic.get(index + 0) & 0xFF;
					g += dapic.get(index + 1) & 0xFF;
					b += dapic.get(index + 2) & 0xFF;
					j++;
				}
				index = wp - (daxsiz2 << 2);
				if ((y > 0) && (dapic.get(index + 3) != 0)) {
					r += dapic.get(index + 0) & 0xFF;
					g += dapic.get(index + 1) & 0xFF;
					b += dapic.get(index + 2) & 0xFF;
					j++;
				}
				index = wp + (daxsiz2 << 2);
				if ((y < daysiz) && (dapic.get(index + 3) != 0)) {
					r += dapic.get(index + 0) & 0xFF;
					g += dapic.get(index + 1) & 0xFF;
					b += dapic.get(index + 2) & 0xFF;
					j++;
				}
				rgb = 0;
				switch (j) {
				case 1:
			        rgb = ( (dapic.get(wp + 3) & 0xFF) << 24 ) + ( b << 16 ) + ( g << 8 ) + ( r << 0 );
					break;
				case 2:
					rgb = ( (dapic.get(wp + 3) & 0xFF) << 24 ) + ( ((b + 1) >> 1) << 16 ) + ( ((g + 1) >> 1) << 8 ) + ( ((r + 1) >> 1) << 0 );
					break;
				case 3:
					rgb = ( (dapic.get(wp + 3) & 0xFF) << 24 ) + ( ((b * 85 + 128) >> 8) << 16 ) + ( ((g * 85 + 128) >> 8) << 8 ) + ( ((r * 85 + 128) >> 8) << 0 );
					break;
				case 4:
					rgb = ( (dapic.get(wp + 3) & 0xFF) << 24 ) + ( ((b + 2) >> 2) << 16 ) + ( ((g + 2) >> 2) << 8 ) + ( ((r + 2) >> 2) << 0 );
					break;
				default:
					continue;
				}
				
				dapic.putInt(wp, rgb);
			}
		}
	}

}
