// VOXModel by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import static com.badlogic.gdx.graphics.GL20.GL_RGBA;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.UseBloodPal;
import static ru.m210projects.Build.Engine.britable;
import static ru.m210projects.Build.Engine.curbrightness;
import static ru.m210projects.Build.Engine.curpalette;
import static ru.m210projects.Build.Engine.gammabrightness;
import static ru.m210projects.Build.Engine.globalshade;
import static ru.m210projects.Build.Engine.numshades;
import static ru.m210projects.Build.Engine.palookup;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.bindTexture;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.getTmpBuffer;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.setupBoundTexture;
import static ru.m210projects.Build.Render.TextureHandle.TextureUtils.uploadBoundTexture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Render.TextureHandle.BTexture;

public class VOXModel extends Model {
	
	public class voxrect_t {
		public vert_t[] v = new vert_t[4];
		public voxrect_t() {
			for(int i = 0; i < 4; i++)
				v[i] = new vert_t();
		}
	}
	
	public class vert_t { public int x, y, z, u, v; }

	public void initQuads()
	{
		for(int vx = 0; vx < qcnt; vx++)
			quad[vx] = new voxrect_t();
	}
	public voxrect_t[] quad; 
	public int qcnt, qfacind[] = new int[7];
	public int mytexx, mytexy;
	public byte[] mytex;
	public int xsiz, ysiz, zsiz;
	public float xpiv, ypiv, zpiv;
	public int is8bit;
	public FloatBuffer uv;

	public BTexture gloadtex(int dapal) {
		ByteBuffer buffer = getTmpBuffer();

		if (palookup[dapal] == null)
			dapal = 0;

		int rgb = 0, r, g, b, wpptr, wp, dacol;
		for (int x, y = 0; y < mytexy; y++) {
			wpptr = y * mytexx;
			for (x = 0; x < mytexx; x++, wpptr++) {
				wp = wpptr << 2;

				dacol = mytex[wpptr] & 0xFF;
				if(UseBloodPal && dapal == 1) //Blood's pal 1
				{
					int shade = (min(max(globalshade/*+(davis>>8)*/,0),numshades-1));
					dacol = palookup[dapal][dacol + (shade << 8)] & 0xFF;
				} else
					dacol = palookup[dapal][dacol] & 0xFF; 

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
				rgb = ( 255 << 24 ) + ( b << 16 ) + ( g << 8 ) + ( r << 0 );
				buffer.putInt(wp, rgb);
			}
		}

		BTexture rtexid = new BTexture();
		bindTexture(rtexid);
		uploadBoundTexture(true, mytexx, mytexy, GL_RGBA, GL_RGBA, buffer, mytexx, mytexy);
		setupBoundTexture(0, 0);
		return rtexid;
	}
}
