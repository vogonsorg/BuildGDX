package ru.m210projects.Build.Render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;

public abstract class GL10 {
	
	public static int GL_LESS = 0;
	public static int GL_GREATER = 1;
	public static int GL_TEXTURE = 2;
	public static int GL_TEXTURE_2D = 3;
	public static int GL_BLEND = 4;
	public static int GL_DEPTH_TEST = 5;
	public static int GL_TRIANGLE_FAN = 6;
	public static int GL_NICEST = 7;
	public static int GL_FASTEST = 8;
	public static int GL_TEXTURE_WRAP_S = 9;
	public static int GL_TEXTURE_WRAP_T = 10;
	public static int GL_CLAMP_TO_EDGE = 11;
	public static int GL_SRC_ALPHA = 12;
	public static int GL_ONE_MINUS_SRC_ALPHA = 13;
	public static int GL_TEXTURE_MAG_FILTER = 14;
	public static int GL_TEXTURE_MIN_FILTER = 15;
	public static int GL_TEXTURE_MAX_ANISOTROPY_EXT = 16;
	public static int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 17;
	public static int GL_COLOR_BUFFER_BIT = 18;
	public static int GL_DEPTH_BUFFER_BIT = 19;
	public static int GL_REPEAT = 20;
	public static int GL_MAX_TEXTURE_SIZE = 21;
	public static int GL_UNSIGNED_BYTE = 22;
	public static int GL_RGBA = 23;
	public static int GL_RGB = 24;
	public static int GL_NEAREST = 25;
	public static int GL_ALPHA = 26;
	public static int GL_FALSE = 27;
	public static int GL_TRUE = 28;
	public static int GL_FRONT_AND_BACK = 29;
	public static int GL_ALWAYS = 30;
	public static int GL_LEQUAL = 31;
	public static int GL_LINE_STRIP = 32;
	public static int GL_LINES = 33;
	public static int GL_TRIANGLES = 34;
	public static int GL_SMOOTH = 35;
	public static int GL_POINTS = 36;
	public static int GL_LINEAR = 37;
	public static int GL_NEAREST_MIPMAP_NEAREST = 38;
	public static int GL_LINEAR_MIPMAP_NEAREST = 39;
	public static int GL_NEAREST_MIPMAP_LINEAR = 40;
	public static int GL_LINEAR_MIPMAP_LINEAR = 41;
	public static int GL_ONE = 42;
	public static int GL_DEST_COLOR = 43;
	public static int GL_POLYGON_OFFSET_FILL = 44;
	public static int GL_FOG_HINT = 45;
	public static int GL_DONT_CARE = 46;
	public static int GL_FOG_MODE = 47;
	public static int GL_EXP = 48;
	public static int GL_EXP2 = 49;
	public static int GL_FOG_DENSITY = 50;
	public static int GL_FOG_COLOR = 51;
	public static int GL_TEXTURE0_ARB = 52;
	public static int GL_TEXTURE_ENV = 53;
	public static int GL_RGB_SCALE_ARB = 54;
	public static int GL_MULTISAMPLE_ARB = 55;
	public static int GL_MULTISAMPLE_FILTER_HINT_NV = 56;
	public static int GL_CLAMP = 57;
	public static int GL_PROJECTION = 58;
	public static int GL_MODELVIEW = 59;
	public static int GL_FOG = 60;
	public static int GL_ALPHA_TEST = 61;
	public static int GL_CW = 62;
	public static int GL_CCW = 63;
	public static int GL_BACK = 64;
	public static int GL_CULL_FACE = 65;
	public static int GL_POLYGON_BIT = 66;
	public static int GL_FILL = 67;
	public static int GL_LINE = 68;
	public static int GL_POINT = 69;
	public static int GL_PERSPECTIVE_CORRECTION_HINT = 70;
	public static int GL_LINE_SMOOTH_HINT = 71;
	public static int GL_DITHER = 72;
	public static int GL_VENDOR = 73;
	public static int GL_RENDERER = 74; 
	public static int GL_VERSION = 75;
	public static int GL_EXTENSIONS = 76;
	public static int GL_COMBINE_ARB = 77;
	public static int GL_TEXTURE_ENV_MODE = 78;
	public static int GL_COMBINE_RGB_ARB = 79;
	public static int GL_MODULATE = 80;
	public static int GL_SOURCE0_RGB_ARB = 81;
	public static int GL_OPERAND0_RGB_ARB = 82;
	public static int GL_SRC_COLOR = 83;
	public static int GL_SOURCE1_RGB_ARB = 84;
	public static int GL_OPERAND1_RGB_ARB = 85;
	public static int GL_COMBINE_ALPHA_ARB = 86;
	public static int GL_REPLACE = 87;
	public static int GL_SOURCE0_ALPHA_ARB = 88;
	public static int GL_PREVIOUS_ARB = 89;
	public static int GL_OPERAND0_ALPHA_ARB = 90;
	public static int GL_QUADS = 91;
	public static int GL_PACK_ALIGNMENT = 92;
	public static int GL_FRONT = 93;
	public static int GL_INTERPOLATE_ARB = 94;
	public static int GL_SOURCE2_RGB_ARB = 95;
	public static int GL_OPERAND2_RGB_ARB = 96;
	public static int GL_VERTEX_ARRAY = 97;
	public static int GL_FOG_START = 98;
	public static int GL_FOG_END = 99;
	public static int GL_TRIANGLE_STRIP = 100;
	public static int GL_ARRAY_BUFFER_ARB = 101;
	public static int GL_STATIC_DRAW_ARB = 102;
	public static int GL_ELEMENT_ARRAY_BUFFER_ARB = 103;
	public static int GL_STREAM_DRAW_ARB = 104;
	public static int GL_TEXTURE_COORD_ARRAY = 105;
	public static int GL_DST_COLOR = 106;
	
	public abstract void bglEnableClientState(int cap);
	
	public abstract void bglVertexPointer(int size, int stride, FloatBuffer pointer);
	
	public abstract void bglTexCoordPointer(int size, int stride, FloatBuffer pointer);
	
	public abstract void bglDrawElements(int mode, ShortBuffer indices);
	
	public abstract void bglDrawElements(int mode, IntBuffer indices);
	
	public abstract void glPixelStorei(int pname, int param);
	
	public abstract void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels);
	
	public abstract void bglGetFloatv(int pname, FloatBuffer params);
	
	public abstract String bglGetString(int name);
	
	public abstract void bglShadeModel(int mode);
	
	public abstract void bglFrontFace(int mode);
	
	public abstract void bglCullFace(int mode);
	
	public abstract void bglColorMask(boolean red, boolean green, boolean blue, boolean alpha);
	
	public abstract void bglClearColor(double red, double green, double blue, double alpha);
	
	public abstract void bglClear(int mask);

	public abstract int bglGetIntegerv(int pname, IntBuffer params);
	
	public abstract void bglTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels);
	
	public abstract void bglTexSubImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels);
	
	public abstract void bglDepthMask(int param);
	
	public abstract void bglDepthFunc(int func);
	
	public abstract void bglDepthRange(double near_val, double far_val);
	
	public abstract void bglFogi(int pname, int params);
	
	public abstract void bglFogf(int pname, float params);
	
	public abstract void bglFogfv(int pname, FloatBuffer params);
	
	public abstract void bglScalef(float x, float y, float z);
	
	public abstract void bglAlphaFunc(int pname, float alpha);
	
	public abstract void bglDeleteTextures(int n, Texture texture);
	
	public abstract void bglDeleteTextures(int n, IntBuffer texture);
	
	public abstract void bglBlendFunc(int sfactor, int dfactor);
	
	public abstract void bglTexParameteri(int target, int pname, int param);
	
	public abstract void bglEnable(int cap);
	
	public abstract void bglBindTexture(int target, Texture texture);
	
	public abstract void bglBindTexture(int target, IntBuffer texture);

	public abstract void bglDisable(int cap);
	
	public abstract void bglHint(int target, int mode);
	
	public abstract void bglColor4f(float r, float g, float b, float a);
	
	public abstract void bglViewport(int x, int y, int width, int height);
	
	public abstract void bglPushMatrix();
	
	public abstract void bglPopMatrix();
	
	public abstract void bglPolygonOffset(float factor, float units);
	
	public abstract void bglPolygonMode(int face, int mode);

	public abstract void bglLoadIdentity();
	
	public abstract void bglMatrixMode(int mode);

	public abstract void bglLoadMatrixf(float[][] m);
	
	public abstract void bglLoadMatrix(Matrix4 m);
	
	public abstract void bglTexEnvf(int type, int pname, float param);

	public abstract void bglOrtho(int left, int right, int bottom, int top, int zNear, int zFar);
	
	public abstract void bglBegin(int type);
	
	public abstract void bglEnd();
	
	public abstract void bglVertex2i(int x, int y);
	
	public abstract void bglVertex2f(float x, float y);
	
	public abstract void bglVertex3d(double x, double y, double z);
	
	public abstract void bglTexCoord2f(float s, float t);
	
	public abstract void bglTexCoord2d(double s, double t);

	public abstract void bglColor4ub(int red, int green, int blue, int alpha);
	
	public abstract void bglPopAttrib();
	
	public abstract void bglPushAttrib(int mask);
	
	public abstract void bglMultiTexCoord2dARB(int target, double s, double t);
	
	public abstract void bglActiveTextureARB(int texture);
	
	public abstract void bglGetError(String name, int var);
	
	public abstract void bglRotatef(float angle, float x, float y, float z);

	public abstract void bglTranslatef(float x, float y, float z);
	
	public abstract void bglDeleteBuffersARB(IntBuffer buffers);
	
	public abstract void bglGenBuffersARB(IntBuffer buffers);
	
	public abstract void bglBindBufferARB(int target, int buffer);
	
	public abstract void bglBufferDataARB(int target, IntBuffer data, int usage);
	
	public abstract void bglClientActiveTextureARB(int texture);
	
	public abstract void bglDisableClientState(int cap);
	
}
