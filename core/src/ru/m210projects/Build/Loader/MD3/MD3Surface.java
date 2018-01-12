package ru.m210projects.Build.Loader.MD3;

import java.nio.FloatBuffer;

public class MD3Surface {
	public int id; //IDP3(0x33806873)
	public String nam; //ascz surface name
    public int flags; //?
    public int numframes, numshaders; //numframes same as md3head,max shade=~256,vert=~4096,tri=~8192
	public int numverts;
	public int numtris;
	public int ofstris;
	public int ofsshaders;
	public int ofsuv;
	public int ofsxyzn;
	public int ofsend;

    public int[][] tris;
    public FloatBuffer uv;
    public MD3Shader[] shaders;
    public MD3Vertice[] xyzn;
}
