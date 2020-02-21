/*
 * TextureUtils by Kirill Klimenko-KLIMaka 
 * Based on parts of "Polymost" by Ken Silverman
 * 
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.TextureHandle;

import static ru.m210projects.Build.Render.Types.GL10.*;
import static ru.m210projects.Build.Render.GLInfo.*;

import java.nio.ByteBuffer;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Settings.GLSettings;

public class TextureUtils {

	public static int calcSize(int size) {
		int nsize = 1;
		if (GLInfo.texnpot == 0) {
			for (; nsize < size; nsize *= 2)
				;
			return nsize;
		}
		return size == 0 ? 1 : size;
	}

	public static void bindTexture(BTexture tex) {
		tex.bind(); //XXX bind(0) ломает detail текстуры, но без это не работает палитра в шейдере
	}

	public static void deleteTexture(BTexture tex) {
		tex.dispose();
	}

	public static void uploadBoundTexture(boolean doalloc, int xsiz, int ysiz, int intexfmt, int texfmt, ByteBuffer pic) {
		int mipLevel = calcMipLevel(xsiz, ysiz, gltexmaxsize);
		if (mipLevel == 0) {
			if (doalloc) {
				BuildGdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, intexfmt, xsiz, ysiz, 0, texfmt, GL_UNSIGNED_BYTE, pic); // loading 1st time
			} else {
				BuildGdx.gl.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, xsiz, ysiz, texfmt, GL_UNSIGNED_BYTE, pic); // overwrite old texture
			}
		} else {
			System.err.println("Uploading non-zero mipmap level textures is unimplemented");
		}
		
		if(GLSettings.textureFilter.get().mipmaps) {
			//Build 2D Mipmaps
			if (supportsGenerateMipmaps) 
				BuildGdx.gl.glGenerateMipmap(GL_TEXTURE_2D);
			else generateMipMapCPU(doalloc, mipLevel, xsiz, ysiz, intexfmt, texfmt, pic);
		}
	}
	
	private static void generateMipMapCPU(boolean doalloc, int mipLevel, int xsiz, int ysiz, int intexfmt, int texfmt, ByteBuffer pic)
	{
		int x2 = xsiz, x3; 
		int y2 = ysiz, y3;
		int r, g, b, a, k, wpptr, rpptr, wp, rp, index, rgb;
	    for (int j = 1, x, y; (x2 > 1) || (y2 > 1); j++)
	    {
	        x3 = Math.max(1, x2 >> 1); 
	        y3 = Math.max(1, y2 >> 1);		// this came from the GL_ARB_texture_non_power_of_two spec
	        for (y = 0; y < y3; y++)
	        {
	            wpptr = y * x3; 
	            rpptr = (y << 1) * x2;
	            for (x = 0; x < x3; x++, wpptr++, rpptr += 2)
	            {
	            	wp = wpptr << 2;
	            	rp = rpptr << 2;
	            	r = g = b = a = k = 0;
	            	
	            	index = rp;
	                if (pic.get(index + 3) != 0) 
	                { 
	                	r += pic.get(index + 0) & 0xFF;
						g += pic.get(index + 1) & 0xFF;
						b += pic.get(index + 2) & 0xFF;
						a += pic.get(index + 3) & 0xFF;
	                	k++; 
	                }
	                index = rp + 4;
	                if (((x << 1) + 1 < x2) && (pic.get(index + 3) != 0)) 
	                { 
	                	r += pic.get(index + 0) & 0xFF;
						g += pic.get(index + 1) & 0xFF;
						b += pic.get(index + 2) & 0xFF;
						a += pic.get(index + 3) & 0xFF;
	                	k++; 
	                }
	                if ((y << 1) + 1 < y2)
	                {
	                	index = rp + (x2 << 2);
	                    if (pic.get(index + 3) != 0) 
	                    { 
	                    	r += pic.get(index + 0) & 0xFF;
							g += pic.get(index + 1) & 0xFF;
							b += pic.get(index + 2) & 0xFF;
							a += pic.get(index + 3) & 0xFF;
	                    	k++; 
	                    }
	                    
	                    index = rp + ((x2 + 1) << 2);
	                    if (((x << 1) + 1 < x2) && pic.get(index + 3) != 0) 
	                    { 
	                    	r += pic.get(index + 0) & 0xFF;
							g += pic.get(index + 1) & 0xFF;
							b += pic.get(index + 2) & 0xFF;
							a += pic.get(index + 3) & 0xFF;
	                    	k++; 
	                    }
	                }
	                switch (k)
	                {
		                case 0:
		                case 1:
					        rgb = ( (a) << 24 ) + ( (b) << 16 ) + ( (g) << 8 ) + ( (r) << 0 );
							break;
						case 2:
							rgb = ( ((a + 1) >> 1) << 24 ) + ( ((b + 1) >> 1) << 16 ) + ( ((g + 1) >> 1) << 8 ) + ( ((r + 1) >> 1) << 0 );
							break;
						case 3:
							rgb = ( ((a * 85 + 128) >> 8) << 24 ) + ( ((b * 85 + 128) >> 8) << 16 ) + ( ((g * 85 + 128) >> 8) << 8 ) + ( ((r * 85 + 128) >> 8) << 0 );
							break;
						case 4:
							rgb = ( ((a + 2) >> 2) << 24 ) + ( ((b + 2) >> 2) << 16 ) + ( ((g + 2) >> 2) << 8 ) + ( ((r + 2) >> 2) << 0 );
							break;
						default:
							continue;
	                }
	                
	                pic.putInt(wp, rgb);	
	            }
	        }
	        
	        if (j >= mipLevel)
	        {
	        	if (doalloc) {
	        		BuildGdx.gl.glTexImage2D(GL_TEXTURE_2D, j - mipLevel, intexfmt, x3, y3, 0, texfmt, GL_UNSIGNED_BYTE, pic); // loading 1st time
				} else {
					BuildGdx.gl.glTexSubImage2D(GL_TEXTURE_2D, j - mipLevel, 0, 0, x3, y3, texfmt, GL_UNSIGNED_BYTE, pic); // overwrite old texture
				}
	        }
	        x2 = x3; y2 = y3;
	    }
	}

	private static int calcMipLevel(int xsiz, int ysiz, int maxsize) {
		int mipLevel = 0;
		while ((xsiz >> mipLevel) > (1 << maxsize)
				|| (ysiz >> mipLevel) > (1 << maxsize))
			mipLevel++;
		return mipLevel;
	}

	public static void setupBoundTexture(GLFilter filter, int anisotropy) {
		filter.apply();
		if (anisotropy >= 1) { // 1 if you want to disable anisotropy
			BuildGdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy);
		}
	}

	public static void setupBoundTextureWrap(int wrap) {
		BuildGdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		BuildGdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
	}
}
