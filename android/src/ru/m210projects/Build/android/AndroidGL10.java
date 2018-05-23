// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import ru.m210projects.Build.Render.Types.BTexture;
import ru.m210projects.Build.Render.Types.GL10;

import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;

import com.badlogic.gdx.math.Matrix4;

public class AndroidGL10 extends GL10 {
	
	public AndroidGL10()
	{
		GL_LESS = GLES10.GL_LESS;
		GL_GREATER = GLES10.GL_GREATER;
		GL_TEXTURE = GLES10.GL_TEXTURE;
		GL_TEXTURE_2D = GLES10.GL_TEXTURE_2D;
		GL_BLEND = GLES10.GL_BLEND;
		GL_DEPTH_TEST = GLES10.GL_DEPTH_TEST;
		GL_TRIANGLE_FAN = GLES10.GL_TRIANGLE_FAN;
		GL_NICEST = GLES10.GL_NICEST;
		GL_FASTEST = GLES10.GL_FASTEST;
		GL_TEXTURE_WRAP_S = GLES10.GL_TEXTURE_WRAP_S;
		GL_TEXTURE_WRAP_T = GLES10.GL_TEXTURE_WRAP_T;
		GL_CLAMP_TO_EDGE = GLES11.GL_CLAMP_TO_EDGE;
		GL_SRC_ALPHA = GLES10.GL_SRC_ALPHA;
		GL_ONE_MINUS_SRC_ALPHA = GLES10.GL_ONE_MINUS_SRC_ALPHA;
		GL_TEXTURE_MAG_FILTER = GLES10.GL_TEXTURE_MAG_FILTER;
		GL_TEXTURE_MIN_FILTER = GLES10.GL_TEXTURE_MIN_FILTER;
		GL_TEXTURE_MAX_ANISOTROPY_EXT = GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT;
		GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
		GL_COLOR_BUFFER_BIT = GLES10.GL_COLOR_BUFFER_BIT;
		GL_DEPTH_BUFFER_BIT = GLES10.GL_DEPTH_BUFFER_BIT;
		GL_REPEAT = GLES10.GL_REPEAT;
		GL_MAX_TEXTURE_SIZE = GLES10.GL_MAX_TEXTURE_SIZE;
		GL_UNSIGNED_BYTE = GLES10.GL_UNSIGNED_BYTE;
		GL_RGBA = GLES10.GL_RGBA;
		GL_RGB = GLES10.GL_RGB;
		GL_NEAREST = GLES10.GL_NEAREST;
		GL_ALPHA = GLES10.GL_ALPHA;
		GL_FALSE = GLES10.GL_FALSE;
		GL_TRUE = GLES10.GL_TRUE;
		GL_FRONT_AND_BACK = GLES10.GL_FRONT_AND_BACK;
		GL_ALWAYS = GLES10.GL_ALWAYS;
		GL_LEQUAL = GLES10.GL_LEQUAL;
		GL_LINE_STRIP = GLES10.GL_LINE_STRIP;
		GL_LINES = GLES10.GL_LINES;
		GL_TRIANGLES = GLES10.GL_TRIANGLES;
		GL_SMOOTH = GLES10.GL_SMOOTH;
		GL_POINTS = GLES10.GL_POINTS;
		GL_LINEAR = GLES10.GL_LINEAR;
		GL_NEAREST_MIPMAP_NEAREST = GLES10.GL_NEAREST_MIPMAP_NEAREST;
		GL_LINEAR_MIPMAP_NEAREST = GLES10.GL_LINEAR_MIPMAP_NEAREST;
		GL_NEAREST_MIPMAP_LINEAR = GLES10.GL_NEAREST_MIPMAP_LINEAR;
		GL_LINEAR_MIPMAP_LINEAR = GLES10.GL_LINEAR_MIPMAP_LINEAR;
		GL_ONE = GLES10.GL_ONE;
		GL_DEST_COLOR = GLES10.GL_DST_COLOR;
		GL_POLYGON_OFFSET_FILL = GLES10.GL_POLYGON_OFFSET_FILL;
		GL_FOG_HINT = GLES10.GL_FOG_HINT;
		GL_DONT_CARE = GLES10.GL_DONT_CARE;
		GL_FOG_MODE = GLES10.GL_FOG_MODE;
		GL_EXP = GLES10.GL_EXP;
		GL_EXP2 = GLES10.GL_EXP2;
		GL_FOG_DENSITY = GLES10.GL_FOG_DENSITY;
		GL_FOG_COLOR = GLES10.GL_FOG_COLOR;
		GL_TEXTURE0_ARB = GLES10.GL_TEXTURE0;
		GL_TEXTURE_ENV = GLES10.GL_TEXTURE_ENV;
		GL_RGB_SCALE_ARB = GLES11.GL_RGB_SCALE;
		GL_MULTISAMPLE_ARB = GLES10.GL_MULTISAMPLE;
		GL_MULTISAMPLE_FILTER_HINT_NV = 0; //GLES10.GL_MULTISAMPLE_FILTER_HINT_NV;
		GL_CLAMP = 0; //GLES11.GL_CLAMP;
		GL_PROJECTION = GLES10.GL_PROJECTION; 
		GL_MODELVIEW = GLES10.GL_MODELVIEW; 
		GL_FOG = GLES10.GL_FOG;
		GL_ALPHA_TEST = GLES10.GL_ALPHA_TEST;
		GL_POLYGON_BIT = 8; //GLES10.GL_POLYGON_BIT;
		GL_FILL = 0; //GLES11.GL_FILL;
		GL_LINE = GLES11.GL_LINES;
		GL_POINT = GLES11.GL_POINTS;
		GL_PERSPECTIVE_CORRECTION_HINT = GLES10.GL_PERSPECTIVE_CORRECTION_HINT;
		GL_LINE_SMOOTH_HINT = GLES10.GL_LINE_SMOOTH_HINT;
		GL_DITHER = GLES10.GL_DITHER;
		GL_VENDOR = GLES10.GL_VENDOR;
		GL_RENDERER = GLES10.GL_RENDERER;
		GL_VERSION = GLES10.GL_VERSION;
		GL_EXTENSIONS = GLES10.GL_EXTENSIONS;
		GL_COMBINE_ARB = 34160;
		GL_TEXTURE_ENV_MODE = GLES11.GL_TEXTURE_ENV_MODE;
		GL_COMBINE_RGB_ARB = 34161;
		GL_MODULATE = GLES11.GL_MODULATE;
		GL_SOURCE0_RGB_ARB = 0; //GLES10Ext.GL_SOURCE0_RGB_ARB;
		GL_OPERAND0_RGB_ARB = 0; //GLES10Ext.GL_SOURCE0_RGB_ARB;
		GL_SRC_COLOR = GLES10.GL_SRC_COLOR;
		GL_SOURCE1_RGB_ARB = 0; //GLES10Ext.GL_SOURCE1_RGB_ARB;
		GL_OPERAND1_RGB_ARB = 0; //GLES10Ext.GL_OPERAND1_RGB_ARB;
		GL_COMBINE_ALPHA_ARB = 0; //GLES10Ext.GL_COMBINE_ALPHA_ARB;
		GL_REPLACE = GLES10.GL_REPLACE;
		GL_SOURCE0_ALPHA_ARB = 0; //GLES10Ext.GL_SOURCE0_ALPHA_ARB;
		GL_PREVIOUS_ARB = 0; //GLES10Ext.GL_PREVIOUS_ARB;
		GL_OPERAND0_ALPHA_ARB = 0; //GLES10Ext.GL_OPERAND0_ALPHA_ARB;
		GL_QUADS = 0; //GLES10.GL_QUADS;
		GL_PACK_ALIGNMENT = GLES10.GL_PACK_ALIGNMENT;
		GL_FRONT = GLES10.GL_FRONT;
		GL_INTERPOLATE_ARB = 0; //GLES10.GL_INTERPOLATE_ARB;
		GL_SOURCE2_RGB_ARB = 0; //GLES10.GL_SOURCE2_RGB_ARB;
		GL_OPERAND2_RGB_ARB = 0; //GLES10.GL_OPERAND2_RGB_ARB;
		GL_VERTEX_ARRAY = GLES10.GL_VERTEX_ARRAY;
		GL_FOG_START = GLES10.GL_FOG_START;
		GL_FOG_END = GLES10.GL_FOG_END;
	}
	
	public void bglGetFloatv(int pname, FloatBuffer params) {
		GLES11.glGetFloatv(pname, params);
	}
	
	public String bglGetString(int name) {
		return GLES11.glGetString(name);
	}
	public void bglShadeModel(int mode) {
		GLES11.glShadeModel(mode);
	}
	
	public void bglColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GLES11.glColorMask(red, green, blue, alpha);
	}
	
	public void bglClearColor(double red, double green, double blue, double alpha) {
		GLES11.glClearColor((float)red, (float)green, (float)blue, (float)alpha);
	}
	
	public void bglClear(int mask) {
		GLES11.glClear(mask);
	}
	
//	public void bglGenTextures(int n, IntBuffer textures) {
//		Gdx.GLES10.glGenTextures(n, textures);
//	}
	
	public int bglGetIntegerv(int pname, IntBuffer params) {
		params.rewind();
        GLES11.glGetIntegerv(pname, params);
        return params.get();
	}
	
	public void bglTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	public void bglTexSubImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GLES11.glTexSubImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	public void bglDepthMask(int param) {
		GLES11.glDepthMask(param != GL_FALSE);
	}
	
	public void bglDepthFunc(int func) {
		GLES11.glDepthFunc(func);
	}
	
	public void bglDepthRange(double near_val, double far_val) {
		GLES11.glDepthRangef((float)near_val, (float)far_val);
	}
	
	public void bglFogi(int pname, int params) {
		GLES11.glFogf(pname, params);
	}
	
	public void bglFogf(int pname, float params) {
		GLES11.glFogf(pname, params);
	}
	public void bglFogfv(int pname, float[] params) {
		GLES11.glFogfv(pname, params, 0);
	}
	public void bglScalef(float x, float y, float z) {
		GLES11.glScalef(x, y, z);
	}
	
	public void bglAlphaFunc(int pname, float alpha) { 
		GLES11.glAlphaFunc(pname, alpha);
	}
	
	public void bglDeleteTextures(int n, BTexture texture) {
		texture.dispose();
	}
	
	public void bglBlendFunc(int sfactor, int dfactor) {
		GLES11.glBlendFunc(sfactor, dfactor);
	}
	
	public void bglTexParameteri(int target, int pname, int param) {
		GLES11.glTexParameteri(target, pname, param);
	}
	
	public void bglEnable(int cap) {
		GLES11.glEnable(cap);
	}
	
	public void bglBindTexture(int target, BTexture texture) {
		texture.bind();
	}

	public void bglDisable(int cap) {
		GLES11.glDisable(cap);
	}
	
	public void bglHint(int target, int mode) {
		GLES11.glHint(target, mode);
		
	}
	
	public void bglColor4f(float r, float g, float b, float a) {
		GLES11.glColor4f(r, g, b, a);
	}
	
	public void bglViewport(int x, int y, int width, int height) {
		GLES11.glViewport(x, y, width, height);
	}
	
	public void bglPushMatrix() {
		GLES11.glPushMatrix();
	}
	
	public void bglPopMatrix() {
		GLES11.glPopMatrix();
	}
	
	public void bglPolygonOffset(float factor, float units) {
		GLES11.glPolygonOffset(factor, units);
	}
	
	public void bglPolygonMode(int face, int mode) {
		//GLES11.glPolygonMode(face, mode);
	}

	public void bglLoadIdentity() { 
		GLES11.glLoadIdentity();
	}
	
	public void bglMatrixMode(int mode) {
		GLES11.glMatrixMode(mode);
	}
	
	private static final FloatBuffer matrixBuffer = ByteBuffer.allocateDirect(16*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	public void bglLoadMatrixf(float[][] m) { 
		matrixBuffer.clear();
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m[i].length; j++) {
				matrixBuffer.put(m[i][j]);	
			}
		}
		matrixBuffer.rewind();
		GLES11.glLoadMatrixf(matrixBuffer);
	}
	
	public void bglLoadMatrix(Matrix4 m) { 
		matrixBuffer.clear();
		matrixBuffer.put(m.getValues());
		matrixBuffer.rewind();
		GLES11.glLoadMatrixf(matrixBuffer);
	}
	
	public void bglTexEnvf(int type, int pname, float param) {
		GLES11.glTexEnvf(type, pname, param);
	}

	public void bglOrtho(int left, int right, int bottom, int top, int zNear, int zFar) {
		GLES11.glOrthof(left, right, bottom, top, zNear, zFar);
	}
	
	public void bglBegin(int type) {
		begin(type);
	}
	
	public void bglEnd() {
		end();
	}
	
	public void bglVertex2i(int x, int y) { 
		bglVertex3d(x, y ,0);
	}
	
	public void bglVertex2f(float x, float y) { 
		bglVertex3d(x, y ,0);
	}
	
	public void bglVertex3d(double x, double y, double z) {
		vertex((float)x, (float)y, (float)z);
	}
	
	public void bglTexCoord2f(float s, float t) { 
		texCoord(s,t);
		
	}
	
	public void bglTexCoord2d(double s, double t) {
		texCoord((float)s,(float)t);
	}

	public void bglColor4ub(int red, int green, int blue, int alpha) {
		GLES11.glColor4f((red&0xFF)/255f, (green&0xFF)/255f, (blue&0xFF)/255f, (alpha&0xFF)/255f);
	}
	
	public void bglPopAttrib() {
//		GLES11.glPopAttrib();
	}
	
	public void bglPushAttrib(int mask) {
//		GLES11.glPushAttrib(mask);
	}
	
	public void bglMultiTexCoord2dARB(int target, double s, double t) {
//		GL13.glMultiTexCoord2d(target, s, t);
	}
	
	public void bglActiveTextureARB(int texture) {
//		GL13.glActiveTexture(texture);
	}
	
	public void bglGetError(String name, int var) {
		int error = GLES10.glGetError();
		if(error != 0)
			System.out.println(name + " " + error + " " + var);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static final int MAX_VERTICES = 2000 * 3;

	/** the primitive type **/
	private static int primitiveType;
	
	/** the vertex position array and buffer **/
	private static float[] positions = new float[3*MAX_VERTICES];
	private static FloatBuffer positionsBuffer = allocateBuffer( 3 * MAX_VERTICES );
	
	/** the vertex color array and buffer **/
	private static float[] colors = new float[4 * MAX_VERTICES];
	private static FloatBuffer colorsBuffer = allocateBuffer( 4 * MAX_VERTICES );
	
	/** the vertex normal array and buffer **/
	private static float[] normals = new float[3*MAX_VERTICES];
	private static FloatBuffer normalsBuffer = allocateBuffer( 3 * MAX_VERTICES );
	
	/** the texture coordinate array and buffer **/
	private static float[] texCoords = new float[2 * MAX_VERTICES];
	private static FloatBuffer texCoordsBuffer = allocateBuffer( 2 * MAX_VERTICES );
	
	/** the current vertex attribute indices **/
	private static int idxPos = 0;
	private static int idxCols = 0;
	private static int idxNors = 0;
	private static int idxTexCoords = 0;
	
	/** which attributes have been defined **/
	private static boolean colorsDefined = false;
	private static boolean normalsDefined = false;
	private static boolean texCoordsDefined = false;

	private static FloatBuffer allocateBuffer( int numFloats )
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( numFloats * 4 );
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asFloatBuffer();
	}
	
	/**
	 * Starts a new list of primitives. The primitiveType
	 * specifies which primitives to draw. Can be any of
	 * GLES10.GL_TRIANGLES, GLES10.GL_LINES and so on. A maximum
	 * of 6000 vertices can be drawn at once.
	 * 
	 * @param primitiveType the primitive type.
	 */
	public void begin( int primitiveType )
	{
		AndroidGL10.primitiveType = primitiveType;		
		idxPos = 0;
		idxCols = 0;
		idxNors = 0;
		idxTexCoords = 0;
		
//		colorsDefined = false;
//		normalsDefined = false;
		texCoordsDefined = false;
	}
	
	/**
	 * Specifies the color of the current vertex
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 */
	public void color( float r, float g, float b, float a )
	{
//		colors[idxCols] = r;
//		colors[idxCols+1] = g;
//		colors[idxCols+2] = b;
//		colors[idxCols+3] = a;
//		colorsDefined = true;
	}
	
	/**
	 * Specifies the normal of the current vertex
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void normal( float x, float y, float z )
	{
//		normals[idxNors] = x;
//		normals[idxNors+1] = y;
//		normals[idxNors+2] = z;
//		normalsDefined = true;
	}
	
	/**
	 * Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate
	 */
	public void texCoord( float u, float v )
	{
		texCoords[idxTexCoords] = u;
		texCoords[idxTexCoords+1] = v;
		texCoordsDefined = true;
	}
	
	/**
	 * Specifies the position of the current vertex and 
	 * finalizes it. After a call to this method you will
	 * effectively define a new vertex afterwards.
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void vertex( float x, float y, float z )
	{
		positions[idxPos++] = x;
		positions[idxPos++] = y;
		positions[idxPos++] = z;
		
//		idxCols += 4;
//		idxNors += 3;
		idxTexCoords += 2;		
	}
	
	/**
	 * Renders the primitives just defined.
	 */
	public void end( )
	{
		if( idxPos == 0 )
			return;
		
		GLES11.glEnableClientState( GLES10.GL_VERTEX_ARRAY );	
		positionsBuffer.clear();
		positionsBuffer.put( positions, 0, idxPos );
		positionsBuffer.flip();
		GLES11.glVertexPointer( 3, GLES10.GL_FLOAT, 0, positionsBuffer );
		
		if( colorsDefined )
		{
			GLES11.glEnableClientState( GLES10.GL_COLOR_ARRAY );
			colorsBuffer.clear();
			colorsBuffer.put( colors, 0, idxCols );
			colorsBuffer.flip();
			GLES11.glColorPointer( 4, GLES10.GL_FLOAT, 0, colorsBuffer );
		}
		
		if( normalsDefined )
		{
			GLES11.glEnableClientState( GLES10.GL_NORMAL_ARRAY );
			normalsBuffer.clear();
			normalsBuffer.put( normals, 0, idxNors );
			normalsBuffer.flip();
			GLES11.glNormalPointer( GLES10.GL_FLOAT, 0, normalsBuffer );
		}
		
		if( texCoordsDefined )
		{
			//gl.glClientActiveTexture( GLES10.GL_TEXTURE0 );
			GLES11.glEnableClientState( GLES10.GL_TEXTURE_COORD_ARRAY );
			texCoordsBuffer.clear();
			texCoordsBuffer.put( texCoords, 0, idxTexCoords );
			texCoordsBuffer.flip();
			GLES11.glTexCoordPointer( 2, GLES10.GL_FLOAT, 0, texCoordsBuffer );
		}
		
		GLES11.glDrawArrays( primitiveType, 0, idxPos / 3 );
		
		if( colorsDefined )
			GLES11.glDisableClientState( GLES10.GL_COLOR_ARRAY );
		if( normalsDefined )
			GLES11.glDisableClientState( GLES10.GL_NORMAL_ARRAY );
		if( texCoordsDefined )
			GLES11.glDisableClientState( GLES10.GL_TEXTURE_COORD_ARRAY );
	}

	@Override
	public void glPixelStorei(int pname, int param) {
		GLES11.glPixelStorei(pname, param);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format,
			int type, ByteBuffer pixels) {
		GLES11.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void bglFrontFace(int mode) {
		GLES11.glFrontFace(mode);
	}

	@Override
	public void bglCullFace(int mode) {
		GLES11.glCullFace(mode);
	}

	@Override
	public void bglFogfv(int pname, FloatBuffer params) {
		GLES11.glFogfv(pname, params);
	}

	@Override
	public void bglEnableClientState(int cap) {
		GLES11.glEnableClientState(cap);
	}

	@Override
	public void bglVertexPointer(int size, int stride, FloatBuffer pointer) {
		GLES11.glVertexPointer(size, 0, stride, pointer);
	}

	@Override
	public void bglDrawElements(int mode, ShortBuffer indices) {
		GLES11.glDrawElements(mode, 0, 0, indices);
	}

	@Override
	public void bglTexCoordPointer(int size, int stride, FloatBuffer pointer) {
		// TODO Auto-generated method stub
	}

	@Override
	public void bglDrawElements(int mode, IntBuffer indices) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglDeleteTextures(int n, IntBuffer texture) {
		GLES11.glDeleteTextures(n, texture);
	}

	@Override
	public void bglBindTexture(int target, IntBuffer texture) {
		GLES11.glBindTexture(target, texture.get(0));
	}

	@Override
	public void bglRotatef(float angle, float x, float y, float z) {
		GLES11.glRotatef(angle, x, y, z);
	}

	@Override
	public void bglTranslatef(float x, float y, float z) {
		GLES11.glTranslatef(x, y, z);
	}

	@Override
	public void bglDeleteBuffersARB(IntBuffer buffers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglGenBuffersARB(IntBuffer buffers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglBindBufferARB(int target, int buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglBufferDataARB(int target, IntBuffer data, int usage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglClientActiveTextureARB(int texture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglDisableClientState(int cap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bglCopyTexImage2D(int target, int level, int internalFormat,
			int x, int y, int width, int height, int border) {
		// TODO Auto-generated method stub
		
	}

}
