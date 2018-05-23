// VOXModel by Alexander Makarov-[M210] (m210-2007@mail.ru) based
// on code originally written by Ken Silverman
// Ken Silverman's official web site: http://www.advsys.net/ken
//
// See the included license file "BUILDLIC.TXT" for license info.

package ru.m210projects.Build.Loader.Voxels;

import java.nio.FloatBuffer;

import ru.m210projects.Build.Loader.Model;

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
	public int mytex[], mytexx, mytexy;
	public int xsiz, ysiz, zsiz;
	public float xpiv, ypiv, zpiv;
	public int is8bit;
	public FloatBuffer uv;
}
