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
		buffer.clear();
		
		if(data != null && tsizx * tsizy > data.length)
			data = null;
		
		boolean hasalpha = false;
		if (data == null) {
			buffer.putInt(0, 0);
			tsizx = tsizy = 1;
			hasalpha = true;
		} else {
			if(type != PixelFormat.Pal8 && alphaMode) {
				for(int i = 0; i < data.length; i++) {
					if(data[i] == (byte) 255) {
						hasalpha = true;
						break;
					}
				}
			}

			int dptr = 0;
			int sptr = 0;
			int xoffs = xsiz << 2;
			if(clamped) {
				for (int i = 0, j; i < tsizx << 2; i += 4) {
					dptr = i;
					for (j = 0; j < tsizy; j++) {
						buffer.putInt(dptr, getColor(data[sptr++], dapal, alphaMode, type));
						dptr += xoffs;
					}
				}

				if(tsizx != xsiz)  {
					int pos = (tsizx - 1) << 2;
					for (int i = 0; i < tsizy; i++) {
						buffer.putInt(pos + 4, buffer.getInt(pos));
						pos += xoffs;
					}
				}

				if(tsizy != ysiz) {
					int pos = (xsiz * (tsizy - 1)) << 2;
					for (int i = 0; i <= tsizx; i++) {
						buffer.putInt(pos + xoffs, buffer.getInt(pos));
						pos += 4;
					}
				}
			}
			else
			{
				int p = 0;
				int len = data.length;
				for (int i = 0, j; i < xoffs; i += 4) {
					p = 0;
					dptr = i;
					for (j = 0; j < ysiz; j++) {
						buffer.putInt(dptr, getColor(data[sptr + p++], dapal, alphaMode, type));
						dptr += xoffs;
						if(p >= tsizy) p = 0;
					}
					if((sptr += tsizy) >= len) sptr = 0;
				}
			}
		}
		if(type == PixelFormat.Rgb && data != null && hasalpha) 
			fixtransparency(buffer, tsizx, tsizy, xsiz, ysiz, clamped);

		return new PicInfo(buffer, hasalpha);
		
	}
	
	private static int getColor(int dacol, int dapal, boolean alphaMode, PixelFormat type) {
		dacol &= 0xFF;
		if(type == PixelFormat.Pal8)
			return dacol;
		
		if(type == PixelFormat.Pal8A)
		{
			if (alphaMode && dacol == 255) 
				return dacol;

			return dacol | 0xFF000000;
		}
		
		if (alphaMode && dacol == 255) 
			return curpalette.getRGBA(0, (byte) 0);
		
		if(UseBloodPal && dapal == 1) //Blood's pal 1
		{
			int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
			dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
		} else
			dacol = palookup[dapal][dacol] & 0xFF;
		
		return curpalette.getRGBA(dacol, (byte) 0xFF);
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

				switch (j) {
				case 0:
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
