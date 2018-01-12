package ru.m210projects.Build.desktop;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBTextureEnvCombine;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.NVMultisampleFilterHint;

import ru.m210projects.Build.Render.GL10;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;

public class DesktopGL10 extends GL10 {

	public DesktopGL10()
	{
		GL_LESS = GL11.GL_LESS;
		GL_GREATER = GL11.GL_GREATER;
		GL_TEXTURE = GL11.GL_TEXTURE;
		GL_TEXTURE_2D = GL11.GL_TEXTURE_2D;
		GL_BLEND = GL11.GL_BLEND;
		GL_DEPTH_TEST = GL11.GL_DEPTH_TEST;
		GL_TRIANGLE_FAN = GL11.GL_TRIANGLE_FAN;
		GL_NICEST = GL11.GL_NICEST;
		GL_FASTEST = GL11.GL_FASTEST;
		GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S;
		GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T;
		GL_CLAMP_TO_EDGE = GL12.GL_CLAMP_TO_EDGE;
		GL_SRC_ALPHA = GL11.GL_SRC_ALPHA;
		GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA;
		GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER;
		GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER;
		GL_TEXTURE_MAX_ANISOTROPY_EXT = EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
		GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
		GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT;
		GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT;
		GL_REPEAT = GL11.GL_REPEAT;
		GL_MAX_TEXTURE_SIZE = GL11.GL_MAX_TEXTURE_SIZE;
		GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE;
		GL_RGBA = GL11.GL_RGBA;
		GL_RGB = GL11.GL_RGB;
		GL_NEAREST = GL11.GL_NEAREST;
		GL_ALPHA = GL11.GL_ALPHA;
		GL_FALSE = GL11.GL_FALSE;
		GL_TRUE = GL11.GL_TRUE;
		GL_FRONT_AND_BACK = GL11.GL_FRONT_AND_BACK;
		GL_ALWAYS = GL11.GL_ALWAYS;
		GL_LEQUAL = GL11.GL_LEQUAL;
		GL_LINE_STRIP = GL11.GL_LINE_STRIP;
		GL_LINES = GL11.GL_LINES;
		GL_TRIANGLES = GL11.GL_TRIANGLES;
		GL_SMOOTH = GL11.GL_SMOOTH;
		GL_POINTS = GL11.GL_POINTS;
		GL_LINEAR = GL11.GL_LINEAR;
		GL_NEAREST_MIPMAP_NEAREST = GL11.GL_NEAREST_MIPMAP_NEAREST;
		GL_LINEAR_MIPMAP_NEAREST = GL11.GL_LINEAR_MIPMAP_NEAREST;
		GL_NEAREST_MIPMAP_LINEAR = GL11.GL_NEAREST_MIPMAP_LINEAR;
		GL_LINEAR_MIPMAP_LINEAR = GL11.GL_LINEAR_MIPMAP_LINEAR;
		GL_ONE = GL11.GL_ONE;
		GL_DEST_COLOR = GL11.GL_DST_COLOR;
		GL_POLYGON_OFFSET_FILL = GL11.GL_POLYGON_OFFSET_FILL;
		GL_FOG_HINT = GL11.GL_FOG_HINT;
		GL_DONT_CARE = GL11.GL_DONT_CARE;
		GL_FOG_MODE = GL11.GL_FOG_MODE;
		GL_EXP = GL11.GL_EXP;
		GL_EXP2 = GL11.GL_EXP2;
		GL_FOG_DENSITY = GL11.GL_FOG_DENSITY;
		GL_FOG_COLOR = GL11.GL_FOG_COLOR;
		GL_TEXTURE0_ARB = GL13.GL_TEXTURE0;
		GL_TEXTURE_ENV = GL11.GL_TEXTURE_ENV;
		GL_RGB_SCALE_ARB = GL13.GL_RGB_SCALE;
		GL_MULTISAMPLE_ARB = GL13.GL_MULTISAMPLE;
		GL_MULTISAMPLE_FILTER_HINT_NV = NVMultisampleFilterHint.GL_MULTISAMPLE_FILTER_HINT_NV;
		GL_CLAMP = GL11.GL_CLAMP;
		GL_PROJECTION = GL11.GL_PROJECTION; 
		GL_MODELVIEW = GL11.GL_MODELVIEW; 
		GL_FOG = GL11.GL_FOG;
		GL_ALPHA_TEST = GL11.GL_ALPHA_TEST;
		GL_POLYGON_BIT = GL11.GL_POLYGON_BIT;
		GL_FILL = GL11.GL_FILL;
		GL_LINE = GL11.GL_LINE;
		GL_POINT = GL11.GL_POINT;
		GL_PERSPECTIVE_CORRECTION_HINT = GL11.GL_PERSPECTIVE_CORRECTION_HINT;
		GL_LINE_SMOOTH_HINT = GL11.GL_LINE_SMOOTH_HINT;
		GL_DITHER = GL11.GL_DITHER;
		GL_VENDOR = GL11.GL_VENDOR;
		GL_RENDERER = GL11.GL_RENDERER;
		GL_VERSION = GL11.GL_VERSION;
		GL_EXTENSIONS = GL11.GL_EXTENSIONS;
		GL_COMBINE_ARB = ARBTextureEnvCombine.GL_COMBINE_ARB;
		GL_TEXTURE_ENV_MODE = GL11.GL_TEXTURE_ENV_MODE;
		GL_COMBINE_RGB_ARB = ARBTextureEnvCombine.GL_COMBINE_RGB_ARB;
		GL_MODULATE = GL11.GL_MODULATE;
		GL_SOURCE0_RGB_ARB = ARBTextureEnvCombine.GL_SOURCE0_RGB_ARB;
		GL_OPERAND0_RGB_ARB = ARBTextureEnvCombine.GL_SOURCE0_RGB_ARB;
		GL_SRC_COLOR = GL11.GL_SRC_COLOR;
		GL_SOURCE1_RGB_ARB = ARBTextureEnvCombine.GL_SOURCE1_RGB_ARB;
		GL_OPERAND1_RGB_ARB = ARBTextureEnvCombine.GL_OPERAND1_RGB_ARB;
		GL_COMBINE_ALPHA_ARB = ARBTextureEnvCombine.GL_COMBINE_ALPHA_ARB;
		GL_REPLACE = GL11.GL_REPLACE;
		GL_SOURCE0_ALPHA_ARB = ARBTextureEnvCombine.GL_SOURCE0_ALPHA_ARB;
		GL_PREVIOUS_ARB = ARBTextureEnvCombine.GL_PREVIOUS_ARB;
		GL_OPERAND0_ALPHA_ARB = ARBTextureEnvCombine.GL_OPERAND0_ALPHA_ARB;
		GL_QUADS = GL11.GL_QUADS;
		GL_PACK_ALIGNMENT = GL11.GL_PACK_ALIGNMENT;
		GL_BACK = GL11.GL_BACK;
		GL_FRONT = GL11.GL_FRONT;
		GL_CULL_FACE = GL11.GL_CULL_FACE;
		GL_CCW = GL11.GL_CCW;
		GL_CW = GL11.GL_CW;
		GL_INTERPOLATE_ARB = ARBTextureEnvCombine.GL_INTERPOLATE_ARB;
		GL_SOURCE2_RGB_ARB = ARBTextureEnvCombine.GL_SOURCE2_RGB_ARB;
		GL_OPERAND2_RGB_ARB = ARBTextureEnvCombine.GL_OPERAND2_RGB_ARB;
		GL_VERTEX_ARRAY = GL11.GL_VERTEX_ARRAY;
		GL_FOG_START = GL11.GL_FOG_START;
		GL_FOG_END = GL11.GL_FOG_END;
		GL_TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
		GL_ARRAY_BUFFER_ARB = ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
		GL_STATIC_DRAW_ARB = ARBBufferObject.GL_STATIC_DRAW_ARB;
		GL_ELEMENT_ARRAY_BUFFER_ARB = ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB;
		GL_STREAM_DRAW_ARB = ARBBufferObject.GL_STREAM_DRAW_ARB;
		GL_TEXTURE_COORD_ARRAY = GL11.GL_TEXTURE_COORD_ARRAY;
		GL_DST_COLOR = GL11.GL_DST_COLOR;
	}

	public void bglGetFloatv(int pname, FloatBuffer params) {
		GL11.glGetFloat(pname, params);
	}
	
	public String bglGetString(int name) {
		return GL11.glGetString(name);
	}
	
	public void bglShadeModel(int mode) {
		GL11.glShadeModel(mode);
	}
	
	public void bglColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}
	
	public void bglClearColor(double red, double green, double blue, double alpha) {
		GL11.glClearColor((float)red, (float)green, (float)blue, (float)alpha);
	}
	
	public void bglClear(int mask) {
		GL11.glClear(mask);
	}
	
//	public void bglGenTextures(int n, IntBuffer textures) {
//		GL11.glGenTextures(n, textures);
//	}
	
	public int bglGetIntegerv(int pname, IntBuffer params) {
		params.rewind();
        GL11.glGetInteger(pname, params);
        return params.get();
	}
	
	public void bglTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	public void bglTexSubImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GL11.glTexSubImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	public void bglDepthMask(int param) {
		GL11.glDepthMask(param != GL11.GL_FALSE);
	}
	
	public void bglDepthFunc(int func) {
		GL11.glDepthFunc(func);
	}
	
	public void bglDepthRange(double near_val, double far_val) {
		GL11.glDepthRange(near_val, far_val);
	}
	
	public void bglFogi(int pname, int params) {
		GL11.glFogf(pname, params);
	}
	
	public void bglFogf(int pname, float params) {
		GL11.glFogf(pname, params);
	}
	
	public void bglFogfv(int pname, FloatBuffer params) {
		GL11.glFog(pname, params);
	}
	
	public void bglScalef(float x, float y, float z) {
		GL11.glScalef(x, y, z);
	}
	
	public void bglAlphaFunc(int pname, float alpha) { 
		GL11.glAlphaFunc(pname, alpha);
	}
	
	public void bglDeleteTextures(int n, Texture texture) {
		texture.dispose();
	}
	
	public void bglBlendFunc(int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}
	
	public void bglTexParameteri(int target, int pname, int param) {
		GL11.glTexParameterf(target, pname, param);
	}
	
	public void bglEnable(int cap) {
		try
		{
			GL11.glEnable(cap);
		} catch(Exception e) {
			bglGetError("glEnable ", cap);
		}
	}
	
	public void bglBindTexture(int target, Texture texture) {
		texture.bind();
	}

	public void bglDisable(int cap) {
		GL11.glDisable(cap);
	}
	
	public void bglHint(int target, int mode) {
		GL11.glHint(target, mode);
		
	}
	
	public void bglColor4f(float r, float g, float b, float a) {
		GL11.glColor4f(r, g, b, a);
	}
	
	public void bglViewport(int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}
	
	public void bglPushMatrix() {
		GL11.glPushMatrix();
	}
	
	public void bglPopMatrix() {
		GL11.glPopMatrix();
	}
	
	public void bglPolygonOffset(float factor, float units) {
		GL11.glPolygonOffset(factor, units);
	}
	
	public void bglPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}

	public void bglLoadIdentity() { 
		GL11.glLoadIdentity();
	}
	
	public void bglMatrixMode(int mode) {
		GL11.glMatrixMode(mode);
	}
	
	private final FloatBuffer matrixBuffer = BufferUtils.newFloatBuffer(4 * 4);
	public void bglLoadMatrixf(float[][] m) { 
		matrixBuffer.clear();
		for(int i = 0; i < m.length; i++) 
			matrixBuffer.put(m[i]);	
		matrixBuffer.rewind();
		GL11.glLoadMatrix(matrixBuffer);
	}
	
	public void bglLoadMatrix(Matrix4 m) { 
		matrixBuffer.clear();
		matrixBuffer.put(m.getValues());
		matrixBuffer.rewind();
		GL11.glLoadMatrix(matrixBuffer);
	}
	
	public void bglTexEnvf(int type, int pname, float param) {
		GL11.glTexEnvf(type, pname, param);
	}

	public void bglOrtho(int left, int right, int bottom, int top, int zNear, int zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}
	
	public void bglBegin(int type) {
		GL11.glBegin(type);
	}
	
	public void bglEnd() {
		GL11.glEnd();
	}
	
	public void bglVertex2i(int x, int y) { 
		GL11.glVertex2i(x, y);
	}
	
	public void bglVertex2f(float x, float y) { 
		GL11.glVertex2f(x, y);
	}
	
	public void bglVertex3d(double x, double y, double z) {
		GL11.glVertex3d(x, y, z);
	}
	
	public void bglTexCoord2f(float s, float t) { 
		GL11.glTexCoord2f(s, t);
		
	}
	
	public void bglTexCoord2d(double s, double t) {
		GL11.glTexCoord2d(s, t);
	}

	public void bglColor4ub(int red, int green, int blue, int alpha) {
		GL11.glColor4f((red&0xFF)/255f, (green&0xFF)/255f, (blue&0xFF)/255f, (alpha&0xFF)/255f);
	}
	
	public void bglPopAttrib() {
		GL11.glPopAttrib();
	}
	
	public void bglPushAttrib(int mask) {
		GL11.glPushAttrib(mask);
	}
	
	public void bglMultiTexCoord2dARB(int target, double s, double t) {
		GL13.glMultiTexCoord2d(target, s, t);
	}
	
	public void bglActiveTextureARB(int texture) {
		GL13.glActiveTexture(texture);
	}
	
	public void bglGetError(String name, int var) {
		int error = GL11.glGetError();
		if(error != 0)
			System.out.println(name + " " + error + " " + var);
	}
	
	@Override
	public void bglFrontFace(int mode) {
		GL11.glFrontFace(mode);
	}

	@Override
	public void bglCullFace(int mode) {
		GL11.glCullFace(mode);
	}

	@Override
	public void glPixelStorei(int pname, int param) {
		GL11.glPixelStorei(pname, param);
		
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format,
			int type, ByteBuffer pixels) {
		GL11.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void bglEnableClientState(int cap) {
		GL11.glEnableClientState(cap);
		
	}

	@Override
	public void bglVertexPointer(int size, int stride, FloatBuffer pointer) {
		GL11.glVertexPointer(size, stride, pointer);
	}

	@Override
	public void bglTexCoordPointer(int size, int stride, FloatBuffer pointer) {
		GL11.glTexCoordPointer(size, stride, pointer);
	}

	@Override
	public void bglDrawElements(int mode, ShortBuffer indices) {
		GL11.glDrawElements(mode, indices);
	}
	
	@Override
	public void bglDrawElements(int mode, IntBuffer indices) {
		GL11.glDrawElements(mode, indices);
	}

	@Override
	public void bglRotatef(float angle, float x, float y, float z) {
		GL11.glRotatef(angle, x, y, z);
	}

	@Override
	public void bglTranslatef(float x, float y, float z) {
		GL11.glTranslatef(x, y, z);
	}
	
	@Override
	public void bglDeleteBuffersARB(IntBuffer buffers)
	{
		GL15.glDeleteBuffers(buffers);
	}
	
	@Override
	public void bglGenBuffersARB(IntBuffer buffers)
	{
		GL15.glGenBuffers(buffers);
	}
	
	@Override
	public void bglBindBufferARB(int target, int buffer)
	{
		GL15.glBindBuffer(target, buffer);
	}
	
	@Override
	public void bglBufferDataARB(int target, IntBuffer data, int usage)
	{
		GL15.glBufferData(target, data, usage);
	}
	
	@Override
	public void bglClientActiveTextureARB(int texture)
	{
		GL13.glClientActiveTexture(texture);
	}
	
	@Override
	public void bglDisableClientState(int cap)
	{
		GL11.glDisableClientState(cap);
	}

	@Override
	public void bglDeleteTextures(int n, IntBuffer texture) {
		GL11.glDeleteTextures(texture);
	}

	@Override
	public void bglBindTexture(int target, IntBuffer texture) {
		GL11.glBindTexture(target, texture.get(0));
	}
}
