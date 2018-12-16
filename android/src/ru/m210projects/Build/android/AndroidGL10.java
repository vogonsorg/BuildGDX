package ru.m210projects.Build.android;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

import android.opengl.GLES10;
import android.opengl.GLES11;
import ru.m210projects.Build.Render.Types.GL10;

public class AndroidGL10 extends GL10 {

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
		//GLES11.glPolygonMode(face, mode);
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
//		GLES11.glPopAttrib();
	}
	
	@Override
	public void glPushAttrib(int mask) {
//		GLES11.glPushAttrib(mask);
	}
	
	@Override
	public void glMultiTexCoord2d(int target, double s, double t) {
//		GL13.glMultiTexCoord2d(target, s, t);
	}
	
	@Override
	public void glActiveTexture(int texture) {
//		GL13.glActiveTexture(texture);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glBindFramebuffer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glBindRenderbuffer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glBindTexture (int target, int texture) {
		GLES11.glBindTexture(target, texture);
	}

	@Override
	public void glBlendColor(float arg0, float arg1, float arg2, float arg3) {
		
	}

	@Override
	public void glBlendEquation(int arg0) {
		
	}

	@Override
	public void glBlendFuncSeparate(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void glBufferData(int arg0, int arg1, Buffer arg2, int arg3) {
		
	}

	@Override
	public void glBufferSubData(int arg0, int arg1, int arg2, Buffer arg3) {
		
	}

	@Override
	public int glCheckFramebufferStatus(int arg0) {
		
		return 0;
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
	public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, Buffer arg7) {
	}

	@Override
	public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
			int arg7, Buffer arg8) {

	}

	@Override
	public void glCopyTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {

	}

	@Override
	public void glCopyTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {

	}

	@Override
	public void glDeleteBuffer(int arg0) {
	
	}

	@Override
	public void glDeleteBuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public void glDeleteFramebuffer(int arg0) {
		
	}

	@Override
	public void glDeleteFramebuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public void glDeleteRenderbuffer(int arg0) {
		
	}

	@Override
	public void glDeleteRenderbuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public void glDeleteTexture(int texture) {
		
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
	public void glDrawArrays(int arg0, int arg1, int arg2) {
		
	}

	@Override
	public void glDrawElements(int arg0, int arg1, int arg2, Buffer arg3) {
		
	}

	@Override
	public void glDrawElements(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void glFramebufferRenderbuffer(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void glFramebufferTexture2D(int arg0, int arg1, int arg2, int arg3, int arg4) {
		
	}

	@Override
	public int glGenBuffer() {

		return 0;
	}

	@Override
	public void glGenBuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public int glGenFramebuffer() {
		
		return 0;
	}

	@Override
	public void glGenFramebuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public int glGenRenderbuffer() {
		
		return 0;
	}

	@Override
	public void glGenRenderbuffers(int arg0, IntBuffer arg1) {
		
	}

	@Override
	public int glGenTexture() {
//		return GLES11.glGenTextures(arg0, arg1);
		return 0;
	}

	@Override
	public void glGenerateMipmap(int arg0) {
		
	}

	@Override
	public void glGetBooleanv(int arg0, Buffer arg1) {
//		GLES11.glGetBooleanv(arg0, arg1);
	}

	@Override
	public void glGetBufferParameteriv(int arg0, int arg1, IntBuffer arg2) {
		
	}

	@Override
	public int glGetError() {
		return GLES10.glGetError();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int arg0, int arg1, int arg2, IntBuffer arg3) {
		
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		params.rewind();
        GLES11.glGetIntegerv(pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv(int arg0, int arg1, IntBuffer arg2) {
		
	}

	@Override
	public void glGetTexParameterfv(int arg0, int arg1, FloatBuffer arg2) {
		
	}

	@Override
	public void glGetTexParameteriv(int arg0, int arg1, IntBuffer arg2) {
		
	}

	@Override
	public void glGetVertexAttribPointerv(int arg0, int arg1, Buffer arg2) {
		
	}

	@Override
	public boolean glIsBuffer(int arg0) {

		return false;
	}

	@Override
	public boolean glIsEnabled(int arg0) {

		return false;
	}

	@Override
	public boolean glIsFramebuffer(int arg0) {

		return false;
	}

	@Override
	public boolean glIsRenderbuffer(int arg0) {

		return false;
	}

	@Override
	public boolean glIsTexture(int arg0) {

		return false;
	}

	@Override
	public void glLineWidth(float arg0) {
		
	}

	@Override
	public void glReadPixels(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, Buffer arg6) {
		
	}

	@Override
	public void glRenderbufferStorage(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void glSampleCoverage(float arg0, boolean arg1) {
		
	}

	@Override
	public void glScissor(int arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void glShaderBinary(int arg0, IntBuffer arg1, int arg2, Buffer arg3, int arg4) {
		
	}

	@Override
	public void glStencilFunc(int arg0, int arg1, int arg2) {
		
	}

	@Override
	public void glStencilMask(int arg0) {
		
	}

	@Override
	public void glStencilOp(int arg0, int arg1, int arg2) {
		
	}

	@Override
	public void glTexParameterf(int arg0, int arg1, float arg2) {
		
	}

	@Override
	public void glTexParameterfv(int arg0, int arg1, FloatBuffer arg2) {
		
	}

	@Override
	public void glTexParameteriv(int arg0, int arg1, IntBuffer arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glClientActiveTexture(int texture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glColorPointer(int size, int type, int stride, Buffer pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLightModelf(int pname, float param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLightModelfv(int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLightf(int light, int pname, float param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLightfv(int light, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLogicOp(int opcode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMaterialf(int face, int pname, float param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMaterialfv(int face, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMultMatrixf(FloatBuffer m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glNormal3f(float nx, float ny, float nz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glNormalPointer(int type, int stride, Buffer pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar) {
		GLES11.glOrthof(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public void glPointSize(float size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexEnvfv(int target, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glDeleteTextures(int n, int[] textures, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glFogfv(int pname, float[] params, int offset) {
		GLES11.glFogfv(pname, params, offset);
	}

	@Override
	public void glGenTextures(int n, int[] textures, int offset) {
		GLES11.glGenTextures(n, textures, offset);
	}

	@Override
	public void glGetIntegerv(int pname, int[] params, int offset) {
		GLES11.glGetIntegerv(pname, params, offset);
	}

	@Override
	public void glLightModelfv(int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLightfv(int light, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glLoadMatrixf(float[] m, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMaterialfv(int face, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glMultMatrixf(float[] m, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexEnvfv(int target, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetLightfv(int light, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetTexEnviv(int env, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glPointParameterf(int pname, float param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexEnvi(int target, int pname, int param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexEnviv(int target, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexEnviv(int target, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexParameterfv(int target, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexParameteriv(int target, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glColorPointer(int size, int type, int stride, int pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glNormalPointer(int type, int stride, int pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, int pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, int pointer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glReadBuffer(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glDepthMask(int param) {
		GLES11.glDepthMask(param != GL_FALSE);
	}

	@Override
	public void glDepthRange(double near_val, double far_val) {
		GLES11.glDepthRangef((float)near_val, (float)far_val);
	}

	@Override
	public void glGetTexImage(int target, int level, int format, int type, Buffer pixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertex2d(double x, double y) {
		
	}

	@Override
	public void glClipPlanef(int plane, float a, float b, float c, float d) {
		GLES11.glClipPlanef(plane, toPlaneBufferf(a,b,c,d));
	}

	@Override
	public int glGetInteger(int pname) {
		GLES11.glGetIntegerv(pname, tempInt);
		return tempInt.get(0);
	}

}
