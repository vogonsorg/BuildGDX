package ru.m210projects.Build.android;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import ru.m210projects.Build.Render.Types.GL10;

public class AndroidGL10 extends GL10 {
	
	private int[] ints = new int[1];

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		GLES11.glGetFloatv(pname, params);
	}
	
	@Override
	public String glGetString(int name) {
		return GLES11.glGetString(name);
	}
	
	@Override
	public void glShadeModel(int mode) {
		GLES11.glShadeModel(mode);
	}
	
	@Override
	public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GLES11.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glClear(int mask) {
		GLES11.glClear(mask);
	}
	
	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		GLES10.glGenTextures(n, textures);
	}
	
	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
		if (pixels == null)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GLES11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}
	
	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GLES11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GLES11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GLES11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GLES11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GLES11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	@Override
	public void glDepthFunc(int func) {
		GLES11.glDepthFunc(func);
	}

	@Override
	public void glFogi(int pname, int params) {
		GLES11.glFogf(pname, params);
	}
	
	@Override
	public void glFogf(int pname, float params) {
		GLES11.glFogf(pname, params);
	}

	@Override
	public void glScalef(float x, float y, float z) {
		GLES11.glScalef(x, y, z);
	}
	
	@Override
	public void glAlphaFunc(int pname, float alpha) { 
		GLES11.glAlphaFunc(pname, alpha);
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		GLES11.glBlendFunc(sfactor, dfactor);
	}
	
	@Override
	public void glTexParameteri(int target, int pname, int param) {
		GLES11.glTexParameteri(target, pname, param);
	}
	
	@Override
	public void glEnable(int cap) {
		GLES11.glEnable(cap);
	}

	@Override
	public void glDisable(int cap) {
		GLES11.glDisable(cap);
	}
	
	@Override
	public void glHint(int target, int mode) {
		GLES11.glHint(target, mode);
		
	}
	
	@Override
	public void glColor4f(float r, float g, float b, float a) {
		GLES11.glColor4f(r, g, b, a);
	}
	
	@Override
	public void glViewport(int x, int y, int width, int height) {
		GLES11.glViewport(x, y, width, height);
	}
	
	@Override
	public void glPushMatrix() {
		GLES11.glPushMatrix();
	}
	
	@Override
	public void glPopMatrix() {
		GLES11.glPopMatrix();
	}
	
	@Override
	public void glPolygonOffset(float factor, float units) {
		GLES11.glPolygonOffset(factor, units);
	}
	
	@Override
	public void glPolygonMode(int face, int mode) {
		//GLES11.glPolygonMode(face, mode); XXX
	}

	@Override
	public void glLoadIdentity() { 
		GLES11.glLoadIdentity();
	}
	
	@Override
	public void glMatrixMode(int mode) {
		GLES11.glMatrixMode(mode);
	}
	
	
	private static final FloatBuffer matrixBuffer = ByteBuffer.allocateDirect(16*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	@Override
	public void glLoadMatrixf(float[][] m) { 
		matrixBuffer.clear();
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m[i].length; j++) {
				matrixBuffer.put(m[i][j]);	
			}
		}
		matrixBuffer.rewind();
		GLES11.glLoadMatrixf(matrixBuffer);
	}
	
	@Override
	public void glLoadMatrix(Matrix4 m) { 
		matrixBuffer.clear();
		matrixBuffer.put(m.getValues());
		matrixBuffer.rewind();
		GLES11.glLoadMatrixf(matrixBuffer);
	}
	
	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		GLES11.glLoadMatrixf(m);
	}
	
	@Override
	public void glTexEnvf(int type, int pname, float param) {
		GLES11.glTexEnvf(type, pname, param);
	}

	@Override
	public void glBegin(int type) {
		begin(type);
	}
	
	@Override
	public void glEnd() {
		end();
	}
	
	@Override
	public void glVertex2i(int x, int y) { 
		glVertex3d(x, y ,0);
	}
	
	@Override
	public void glVertex2f(float x, float y) { 
		glVertex3d(x, y ,0);
	}
	
	@Override
	public void glVertex2d(double x, double y) {
		glVertex3d(x, y ,0);
	}
	
	@Override
	public void glVertex3d(double x, double y, double z) {
		vertex((float)x, (float)y, (float)z);
	}
	
	@Override
	public void glTexCoord2f(float s, float t) { 
		texCoord(s,t);
		
	}
	
	@Override
	public void glTexCoord2d(double s, double t) {
		texCoord((float)s,(float)t);
	}

	@Override
	public void glColor4ub(int red, int green, int blue, int alpha) {
		GLES11.glColor4f((red&0xFF)/255f, (green&0xFF)/255f, (blue&0xFF)/255f, (alpha&0xFF)/255f);
	}
	
	@Override
	public void glPopAttrib() {
//		GLES11.glPopAttrib(); XXX
	}
	
	@Override
	public void glPushAttrib(int mask) {
//		GLES11.glPushAttrib(mask); XXX
	}
	
	@Override
	public void glMultiTexCoord2d(int target, double s, double t) {
//		GL13.glMultiTexCoord2d(target, s, t); XXX
	}
	
	@Override
	public void glActiveTexture(int texture) {
		GLES11.glActiveTexture (texture);
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
		
		colorsDefined = false;
		normalsDefined = false;
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
		colors[idxCols] = r;
		colors[idxCols+1] = g;
		colors[idxCols+2] = b;
		colors[idxCols+3] = a;
		colorsDefined = true;
	}
	
	/**
	 * Specifies the normal of the current vertex
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void normal( float x, float y, float z )
	{
		normals[idxNors] = x;
		normals[idxNors+1] = y;
		normals[idxNors+2] = z;
		normalsDefined = true;
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
		
		idxCols += 4;
		idxNors += 3;
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
			glClientActiveTexture( GLES10.GL_TEXTURE0);
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
	public void glFrontFace(int mode) {
		GLES11.glFrontFace(mode);
	}

	@Override
	public void glCullFace(int mode) {
		GLES11.glCullFace(mode);
	}

	@Override
	public void glFogfv(int pname, FloatBuffer params) {
		GLES11.glFogfv(pname, params);
	}

	@Override
	public void glEnableClientState(int cap) {
		GLES11.glEnableClientState(cap);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer texture) {
		GLES11.glDeleteTextures(n, texture);
	}

	@Override
	public void glBindTexture(int target, IntBuffer texture) {
		GLES11.glBindTexture(target, texture.get(0));
	}

	@Override
	public void glRotatef(float angle, float x, float y, float z) {
		GLES11.glRotatef(angle, x, y, z);
	}

	@Override
	public void glTranslatef(float x, float y, float z) {
		GLES11.glTranslatef(x, y, z);
	}

	@Override
	public void glDisableClientState(int cap) {
		GLES11.glDisableClientState(cap);
	}

	@Override
	public void glFlush() {
		GLES11.glFlush();
		
	}

	@Override
	public void glFinish() {
		GLES11.glFinish();
	}

	@Override
	public void glBindBuffer(int arg0, int arg1) {
		GLES11.glBindBuffer (arg0, arg1);
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		GLES20.glBindFramebuffer (target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		GLES20.glBindRenderbuffer (target, renderbuffer);
	}

	@Override
	public void glBindTexture (int target, int texture) {
		GLES11.glBindTexture(target, texture);
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		GLES20.glBlendColor (red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation (int mode) {
		GLES20.glBlendEquation (mode);
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLES20.glBlendFuncSeparate (srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		GLES20.glBufferData (target, size, data, usage);
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		GLES20.glBufferSubData (target, offset, size, data);
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		return GLES20.glCheckFramebufferStatus (target);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		GLES11.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf(float depth) {
		GLES11.glClearDepthf(depth);
	}

	@Override
	public void glClearStencil(int s) {
		GLES11.glClearStencil(s);
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
		GLES11.glCompressedTexImage2D (target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
		GLES11.glCompressedTexSubImage2D (target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GLES11.glCopyTexImage2D (target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GLES11.glCopyTexSubImage2D (target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GLES11.glDeleteBuffers (n, buffers);
	}
	
	@Override
	public void glDeleteBuffer (int buffer) {
		ints[0] = buffer;
		GLES11.glDeleteBuffers (1, ints, 0);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GLES20.glDeleteFramebuffers (n, framebuffers);
	}
	
	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		ints[0] = framebuffer;
		GLES20.glDeleteFramebuffers (1, ints, 0);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GLES20.glDeleteRenderbuffers (n, renderbuffers);
	}
	
	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		ints[0] = renderbuffer;
		GLES20.glDeleteRenderbuffers (1, ints, 0);
	}

	@Override
	public void glDeleteTexture(int texture) {
		ints[0] = texture;
		GLES11.glDeleteTextures (1, ints, 0);
	}

	@Override
	public void glDepthMask(boolean flag) {
		GLES11.glDepthMask(flag);
	}

	@Override
	public void glDepthRangef(float near_val, float far_val) {
		GLES11.glDepthRangef(near_val, far_val);
	}
	
	@Override
	public void glDrawArrays (int mode, int first, int count) {
		GLES11.glDrawArrays (mode, first, count);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		GLES11.glDrawElements (mode, count, type, indices);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		GLES11.glDrawElements (mode, count, type, indices);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GLES20.glFramebufferTexture2D (target, attachment, textarget, texture, level);
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		GLES11.glGenBuffers (n, buffers);
	}
	
	@Override
	public int glGenBuffer () {
		GLES11.glGenBuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GLES20.glGenFramebuffers (n, framebuffers);
	}
	
	@Override
	public int glGenFramebuffer () {
		GLES20.glGenFramebuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GLES20.glGenRenderbuffers (n, renderbuffers);
	}
	
	@Override
	public int glGenRenderbuffer () {
		GLES20.glGenRenderbuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public int glGenTexture() {
		GLES11.glGenTextures(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenerateMipmap(int target) {
		GLES20.glGenerateMipmap (target);
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		GLES11.glGetBooleanv (pname, (IntBuffer)params);
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GLES11.glGetBufferParameteriv (target, pname, params);
	}

	@Override
	public int glGetError() {
		return GLES10.glGetError();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		GLES20.glGetFramebufferAttachmentParameteriv (target, attachment, pname, params);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		params.rewind();
        GLES11.glGetIntegerv(pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glGetRenderbufferParameteriv (target, pname, params);
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GLES20.glGetTexParameterfv (target, pname, params);
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glGetTexParameteriv (target, pname, params);
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		// FIXME won't implement this shit
//		GLES20.glGetVertexAttribPointerv(index, pname, pointer);
	}

	@Override
	public boolean glIsBuffer(int arg0) {
		return GLES11.glIsBuffer (arg0);
	}

	@Override
	public boolean glIsEnabled (int cap) {
		return GLES11.glIsEnabled (cap);
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		return GLES20.glIsFramebuffer (framebuffer);
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return GLES20.glIsRenderbuffer (renderbuffer);
	}

	@Override
	public boolean glIsTexture (int texture) {
		return GLES11.glIsTexture (texture);
	}

	@Override
	public void glLineWidth(float width) {
		GLES11.glLineWidth (width);
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GLES11.glReadPixels (x, y, width, height, format, type, pixels);
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GLES20.glRenderbufferStorage (target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		GLES11.glSampleCoverage(value, invert);
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		GLES11.glScissor(x, y, width, height);
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		GLES11.glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilMask (int mask) {
		GLES11.glStencilMask(mask);
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		GLES11.glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		GLES11.glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GLES11.glTexParameterfv(target, pname, params);
	}
	
	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GLES11.glTexParameteriv(target, pname, params);
	}

	@Override
	public void glClientActiveTexture (int texture) {
//		GL13.glClientActiveTexture(texture); XXX
	}

	@Override
	public void glColorPointer (int size, int type, int stride, Buffer pointer) {
		GLES11.glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) {
		GLES11.glFrustumf(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public void glLightModelf (int pname, float param) {
		GLES11.glLightModelf(pname, param);
	}

	@Override
	public void glLightModelfv (int pname, FloatBuffer params) {
		GLES11.glLightModelfv(pname, params);
	}

	@Override
	public void glLightf (int light, int pname, float param) {
		GLES11.glLightf(light, pname, param);
	}

	@Override
	public void glLightfv (int light, int pname, FloatBuffer params) {
		GLES11.glLightfv(light, pname, params);
	}

	@Override
	public void glLogicOp (int opcode) {
		GLES11.glLogicOp(opcode);
	}

	@Override
	public void glMaterialf (int face, int pname, float param) {
		GLES11.glMaterialf(face, pname, param);
	}

	@Override
	public void glMaterialfv (int face, int pname, FloatBuffer params) {
		GLES11.glMaterialfv(face, pname, params);
	}

	@Override
	public void glMultMatrixf (FloatBuffer m) {
		GLES11.glMultMatrixf(m);
	}

	@Override
	public void glMultiTexCoord4f (int target, float s, float t, float r, float q) {
//		GL13.glMultiTexCoord4f(target, s, t, r, q); XXX
	}

	@Override
	public void glNormal3f (float nx, float ny, float nz) {
		GLES11.glNormal3f(nx, ny, nz);
	}

	@Override
	public void glNormalPointer (int type, int stride, Buffer pointer) {
		GLES11.glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) {
		GLES11.glOrthof(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public void glPointSize (float size) {
		GLES11.glPointSize(size);
	}

	@Override
	public void glTexCoordPointer (int size, int type, int stride, Buffer pointer) {
		GLES11.glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glTexEnvfv (int target, int pname, FloatBuffer params) {
		GLES11.glTexEnvfv(target, pname, params);
	}

	@Override
	public void glVertexPointer (int size, int type, int stride, Buffer pointer) {
		GLES11.glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glDeleteTextures (int n, int[] textures, int offset) {
		GLES11.glDeleteTextures(n, toBuffer(n, textures, offset));
	}

	@Override
	public void glFogfv (int pname, float[] params, int offset) {
		GLES11.glFogfv(pname, toBuffer(params, offset));
	}

	@Override
	public void glGenTextures (int n, int[] textures, int offset) {
		for (int i = offset; i < offset + n; i++)
			textures[i] = glGenTexture();
	}

	IntBuffer getBuffer = BufferUtils.newIntBuffer(100);
	@Override
	public void glGetIntegerv (int pname, int[] params, int offset) {
		glGetIntegerv(pname, getBuffer);
		// FIXME Yeah, so. This sucks as well :D LWJGL does not set pos/lim.
		for (int i = offset, j = 0; i < params.length; i++, j++) {
			if (j == getBuffer.capacity()) return;
			params[i] = getBuffer.get(j);
		}
	}

	@Override
	public void glLightModelfv (int pname, float[] params, int offset) {
		GLES11.glLightModelfv(pname, toBuffer(params, offset));
	}

	@Override
	public void glLightfv (int light, int pname, float[] params, int offset) {
		GLES11.glLightfv(light, pname, toBuffer(params, offset));
	}

	@Override
	public void glLoadMatrixf (float[] m, int offset) {
		GLES11.glLoadMatrixf(toBuffer(m, offset));
	}

	@Override
	public void glMaterialfv (int face, int pname, float[] params, int offset) {
		GLES11.glMaterialfv(face, pname, toBuffer(params, offset));
	}

	@Override
	public void glMultMatrixf (float[] m, int offset) {
		GLES11.glMultMatrixf(toBuffer(m, offset));
	}

	@Override
	public void glTexEnvfv (int target, int pname, float[] params, int offset) {
		GLES11.glTexEnvfv(target, pname, toBuffer(params, offset));
	}

	@Override
	public void glDeleteBuffers (int n, int[] buffers, int offset) {
//		GL15.glDeleteBuffers(toBuffer(n, buffers, offset)); XXX
	}

	@Override
	public void glGenBuffers (int n, int[] buffers, int offset) {
		for (int i = offset; i < offset + n; i++)
			buffers[i] = glGenBuffer();
	}

	@Override
	public void glGetLightfv (int light, int pname, FloatBuffer params) {
		GLES11.glGetLightfv(light, pname, params);
	}

	@Override
	public void glGetMaterialfv (int face, int pname, FloatBuffer params) {
		GLES11.glGetMaterialfv(face, pname, params);
	}

	@Override
	public void glGetTexEnviv (int env, int pname, IntBuffer params) {
		GLES11.glGetTexEnviv(env, pname, params);
	}

	@Override
	public void glPointParameterf (int pname, float param) {
//		GL14.glPointParameterfs(pname, param); XXX
	}

	@Override
	public void glPointParameterfv (int pname, FloatBuffer params) {
//		GL14.glPointParameter(pname, params); XXX
	}

	@Override
	public void glTexEnvi (int target, int pname, int param) {
		GLES11.glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnviv (int target, int pname, int[] params, int offset) {
		GLES11.glTexEnviv(target, pname, toBuffer(params, offset));
	}

	@Override
	public void glTexEnviv (int target, int pname, IntBuffer params) {
		GLES11.glTexEnviv(target, pname, params);
	}

	@Override
	public void glTexParameterfv (int target, int pname, float[] params, int offset) {
		GLES11.glTexParameterfv(target, pname, toBuffer(params, offset));
	}

	@Override
	public void glTexParameteriv (int target, int pname, int[] params, int offset) {
		GLES11.glTexParameteriv(target, pname, toBuffer(params, offset));
	}

	@Override
	public void glColorPointer (int size, int type, int stride, int pointer) {
		GLES11.glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glNormalPointer (int type, int stride, int pointer) {
		GLES11.glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glTexCoordPointer (int size, int type, int stride, int pointer) {
		GLES11.glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glVertexPointer (int size, int type, int stride, int pointer) {
		GLES11.glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glReadBuffer(int mode) {
//		GLES11.glReadBuffer(mode); XXX
	}

	@Override
	public void glDepthRange(double near_val, double far_val) {
		GLES11.glDepthRangef((float)near_val, (float)far_val);
	}
	
	@Override
	public void glDepthMask(int param) {
		GLES11.glDepthMask(param != GLES11.GL_FALSE);
	}

	@Override
	public void glGetTexImage(int target, int level, int format, int type, Buffer pixels) {
//		if (pixels instanceof ByteBuffer) XXX
//			GLES11.glGetTexImage(target, level, format, type, (ByteBuffer)pixels);
//		else if (pixels instanceof IntBuffer)
//			GLES11.glGetTexImage(target, level, format, type, (IntBuffer)pixels);
//		else if (pixels instanceof FloatBuffer)
//			GLES11.glGetTexImage(target, level, format, type, (FloatBuffer)pixels);
//		else if (pixels instanceof DoubleBuffer)
//			GLES11.glGetTexImage(target, level, format, type, (DoubleBuffer)pixels);
//		else if (pixels instanceof ShortBuffer) 
//			GLES11.glGetTexImage(target, level, format, type, (ShortBuffer)pixels);
	}
	
	@Override
	public void glClipPlanef(int plane, float a, float b, float c, float d) {
		GLES11.glClipPlanef(plane, toPlaneBufferf(a,b,c,d));
	}

	@Override
	public int glGetInteger(int pname) {
		tempInt.clear();
		GLES11.glGetIntegerv(pname, tempInt);
		return tempInt.get();
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLES20.glFramebufferRenderbuffer (target, attachment, renderbuffertarget, renderbuffer);
	}
}
