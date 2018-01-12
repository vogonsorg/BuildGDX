package ru.m210projects.Build.Loader.MD2;

import java.nio.FloatBuffer;

import ru.m210projects.Build.Loader.MDModel;

public class MD2Model extends MDModel {
    public int numverts, numglcmds, framebytes;
    public int[] glcmds;
    public MD2Frame[] frames;
    public String basepath;   // pointer to string of base path
    public String skinfn;   // pointer to first of numskins 64-char strings
    public FloatBuffer uv;
    public MD2Triangle[] tris;
}
