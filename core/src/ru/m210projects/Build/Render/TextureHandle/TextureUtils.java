/*
 * TextureUtils by Kirill Klimenko-KLIMaka 
 * Based on parts of "Polymost" by Ken Silverman
 * 
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.TextureHandle;

import static ru.m210projects.Build.FileHandle.Cache1D.kGetBytes;
import static ru.m210projects.Build.Render.Types.GL10.*;

import java.nio.ByteBuffer;

import ru.m210projects.Build.Architecture.BuildGDX;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Render.GLInfo;
import ru.m210projects.Build.Render.Types.GLFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

public class TextureUtils {

	private static final int TEX_MAX_SIZE = 1024;

	private static ByteBuffer tmp_buffer;
	private static int gltexmaxsize = 0;
	
	private static ShaderProgram shader;

	private static GLFilter[] glfiltermodes = {
			new GLFilter("GL_NEAREST", GL_NEAREST, GL_NEAREST), // 0
			new GLFilter("GL_LINEAR", GL_LINEAR, GL_LINEAR), // 1
			new GLFilter("GL_NEAREST_MIPMAP_NEAREST", GL_NEAREST_MIPMAP_NEAREST, GL_NEAREST), // 2
			new GLFilter("GL_LINEAR_MIPMAP_NEAREST", GL_LINEAR_MIPMAP_NEAREST, GL_LINEAR), // 3
			new GLFilter("GL_NEAREST_MIPMAP_LINEAR", GL_NEAREST_MIPMAP_LINEAR, GL_NEAREST), // 4
			new GLFilter("GL_LINEAR_MIPMAP_LINEAR", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR) }; // 5

	public static GLFilter getGlFilter(int mode) {
		mode = mode < 0 ? 0 : mode >= glfiltermodes.length ? glfiltermodes.length - 1 : mode;
		return glfiltermodes[mode];
	}

	public static ByteBuffer getTmpBuffer() {
		if (tmp_buffer == null) {
			tmp_buffer = BufferUtils.newByteBuffer(TEX_MAX_SIZE * TEX_MAX_SIZE * 4);
		}
		return tmp_buffer;
	}

	private static int getTextureMaxSize() {
		if (gltexmaxsize <= 0) {
			int i = BuildGDX.gl.glGetInteger(GL_MAX_TEXTURE_SIZE);
			if (i == 0) {
				gltexmaxsize = 6; // 2^6 = 64 == default GL max texture size
			} else {
				gltexmaxsize = 0;
				for (; i > 1; i >>= 1)
					gltexmaxsize++;
			}
		}
		return gltexmaxsize;
	}
	
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
		tex.bind();
	}

	public static void deleteTexture(BTexture tex) {
		tex.dispose();
	}
	
	public static void createShader()
	{
		byte[] file = kGetBytes("fragment.glsl", 0);
	    String fragment = new String(file);
	    shader = new ShaderProgram(SpriteBatch.createDefaultShader().getVertexShaderSource(), fragment);
        if(!shader.isCompiled())
        	Console.Println("Shader compile error: " + shader.getLog());
	}
	
	public static void bindShader()
	{
		shader.begin();
	}
	
	public static void unbindShader()
	{
		shader.end();
	}

	public static void uploadBoundTexture(boolean doalloc, int xsiz, int ysiz, int intexfmt, int texfmt, ByteBuffer pic, int tsizx, int tsizy) {
		int mipLevel = calcMipLevel(xsiz, ysiz, getTextureMaxSize());
		if (mipLevel == 0) {
			if (doalloc) {
				Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, intexfmt, xsiz, ysiz, 0, texfmt, GL_UNSIGNED_BYTE, pic); // loading 1st time
			} else {
				Gdx.gl.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, xsiz, ysiz, texfmt, GL_UNSIGNED_BYTE, pic); // overwrite old texture
			}
		} else {
			System.err.println("Uploading non-zero mipmap level textures is unimplemented");
		}

		//Build 2D Mipmaps
		if (Gdx.graphics.supportsExtension("GL_ARB_framebuffer_object") 
				|| Gdx.graphics.supportsExtension("GL_EXT_framebuffer_object") 
				|| Gdx.gl30 != null 
				|| Gdx.app.getType() == ApplicationType.Android 
				|| Gdx.app.getType() == ApplicationType.WebGL
				|| Gdx.app.getType() == ApplicationType.iOS)
			Gdx.gl.glGenerateMipmap(GL_TEXTURE_2D);
		else
			generateMipMapCPU(doalloc, mipLevel, xsiz, ysiz, intexfmt, texfmt, pic);
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
					Gdx.gl.glTexImage2D(GL_TEXTURE_2D, j - mipLevel, intexfmt, x3, y3, 0, texfmt, GL_UNSIGNED_BYTE, pic); // loading 1st time
				} else {
					Gdx.gl.glTexSubImage2D(GL_TEXTURE_2D, j - mipLevel, 0, 0, x3, y3, texfmt, GL_UNSIGNED_BYTE, pic); // overwrite old texture
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

	public static void setupBoundTexture(int filterMode, int anisotropy) {
		GLFilter filter = getGlFilter(filterMode);
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter.mag);
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter.min);
		if (anisotropy >= 1) { // 1 if you want to disable anisotropy
			Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy);
		}
	}

	public static void setupBoundTextureWrap(int wrap) {
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
	}
}
