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
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.globalshade;
import static ru.m210projects.Build.Engine.palookup;

import java.nio.ByteBuffer;

import ru.m210projects.Build.Render.Renderer.PixelFormat;
import ru.m210projects.Build.Types.UnsafeDirectBuffer;

public class ImageUtils {
	
	private static final int TEX_MAX_SIZE = 1024;
	private static UnsafeDirectBuffer tmp_buffer;

	public static class PicInfo {
		public final ByteBuffer pic;
		public final boolean hasalpha;

		public PicInfo(UnsafeDirectBuffer unsafe, boolean hasalpha) {
			this.pic = unsafe.getBuffer();
			this.hasalpha = hasalpha;
		}
	}

	public static PicInfo loadPic(int xsiz, int ysiz, int tsizx, int tsizy, byte[] data, int dapal, boolean clamped, boolean alphaMode, PixelFormat type) {
		UnsafeDirectBuffer buffer = getTmpBuffer();
		boolean hasalpha = false;
		buffer.clear();
		
		if(data != null && tsizx * tsizy > data.length)
			data = null;
		
		switch(type)
		{
			case Pal8:
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
			default:
			if (data == null) {
				buffer.putInt(0, 0);
				tsizx = tsizy = 1;
				hasalpha = true;
			} else {
//				int wpptr, wp, dacol;
//				for (int y = 0, x2, y2, x; y < ysiz; y++) {
//					y2 = (y < tsizy) ? y : y - tsizy;
//					wpptr = y * xsiz;
//					for (x = 0; x < xsiz; x++, wpptr++) {
//						wp = wpptr << 2;
//
//						if (clamped && ((x >= tsizx) || (y >= tsizy))) { // Clamp texture
//							buffer.putInt(wp, 0);
//							continue;
//						}
//						x2 = (x < tsizx) ? x : x - tsizx;
//						dacol = data[x2 * tsizy + y2] & 0xFF;
//						if (alphaMode && dacol == 255) 
//							hasalpha = true;
//
//						buffer.putInt(wp, getColor(dacol, dapal, alphaMode, type));
//					}
//				}
				
				int dacol;
				int dptr = 0;
				int sptr = 0;
				if(clamped) {
					for (int i = 0, j; i < tsizx; i++) {
						dptr = i << 2;
						for (j = 0; j < tsizy; j++) {
							dacol = data[sptr++] & 0xFF;
							if (alphaMode && dacol == 255) 
								hasalpha = true;
							buffer.putInt(dptr, getColor(dacol, dapal, alphaMode, type));
							dptr += (xsiz << 2);
						}
					}
				}
				else
				{
//					for (int i = 0, j; i < xsiz; i++) {
//						dptr = i << 2;
//						sptr = (i % tsizx) * tsizy;
//						for (j = 0; j < ysiz; j++) {
//							dacol = data[sptr + (j & (tsizy - 1))] & 0xFF;
//							if (alphaMode && dacol == 255) 
//								hasalpha = true;
//							buffer.putInt(dptr, getColor(dacol, dapal, alphaMode, type));
//							dptr += (xsiz << 2);
//						}
//					}
					
					int wpptr, wp;
					for (int y = 0, x2, y2, x; y < ysiz; y++) {
						y2 = (y < tsizy) ? y : y - tsizy;
						wpptr = y * xsiz;
						for (x = 0; x < xsiz; x++, wpptr++) {
							wp = wpptr << 2;
							x2 = (x < tsizx) ? x : x - tsizx;
							dacol = data[x2 * tsizy + y2] & 0xFF;
							if (alphaMode && dacol == 255) 
								hasalpha = true;
	
							buffer.putInt(wp, getColor(dacol, dapal, alphaMode, type));
						}
					}
				}
			}
			if(data != null && hasalpha) 
				fixtransparency(buffer, tsizx, tsizy, xsiz, ysiz, clamped);

			return new PicInfo(buffer, hasalpha);
		}
	}
	
	private static int getColor(int dacol, int dapal, boolean alphaMode, PixelFormat type)
	{
		if(type == PixelFormat.Pal8A)
		{
			byte a = -1;
			if (alphaMode && dacol == 255) {
				a = 0;
			}
			
			return dacol | ((a & 0xFF) << 24);
		}
		else {
			byte a = -1;
			if (alphaMode && dacol == 255) {
				a = 0;
				dacol = 0;
			} else {
				if(UseBloodPal && dapal == 1) //Blood's pal 1
				{
					int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
					dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
				} else
					dacol = palookup[dapal][dacol] & 0xFF;
			}
			
			return curpalette.getRGBA(dacol, a);
		}
	}
	
	private static void fixtransparency(UnsafeDirectBuffer dapic, int daxsiz, int daysiz, int daxsiz2, int daysiz2, boolean clamping) {
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
	
	public static UnsafeDirectBuffer getTmpBuffer() {
		if (tmp_buffer == null) {
			tmp_buffer = new UnsafeDirectBuffer(TEX_MAX_SIZE * TEX_MAX_SIZE * 4);
		}
		return tmp_buffer;
	}

}
