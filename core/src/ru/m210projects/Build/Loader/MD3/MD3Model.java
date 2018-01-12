package ru.m210projects.Build.Loader.MD3;

import java.util.HashMap;

import ru.m210projects.Build.Loader.MDModel;

import com.badlogic.gdx.math.Matrix4;

public class MD3Model extends MDModel {
    public MD3Header head;
	public MD3Frame[] frames;
	public HashMap<String, Matrix4>[] tags;
	public MD3Surface[] surfaces;
    
    // polymer VBO names after that, allocated per surface
//    GLuint*             indices;
//    GLuint*             texcoords;
//    GLuint*             geometry;
}
