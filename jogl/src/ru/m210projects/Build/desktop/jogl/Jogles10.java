package ru.m210projects.Build.desktop.jogl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLES1;

import ru.m210projects.Build.Render.Types.GL10;

public class Jogles10 extends GL10 {

	private int[] ints = new int[1];

	@Override
	public void glActiveTexture(int texture) {
		GLContext.getCurrentGL().getGL2ES1().glActiveTexture(texture);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		GLContext.getCurrentGL().getGL2ES1().glBindBuffer(target, buffer);
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		GLContext.getCurrentGL().getGL2ES1().glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer(int target, int renderbuffer) {
		GLContext.getCurrentGL().getGL2ES1().glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		GLContext.getCurrentGL().getGL2ES1().glBindTexture(target, texture);
	}

	@Override
	public void glBlendColor(float red, float green, float blue, float alpha) {
		if (GLContext.getCurrentGL().isGLES2())
			GLContext.getCurrentGL().getGLES2().glBlendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation(int mode) {
		GLContext.getCurrentGL().getGL2ES1().glBlendEquation(mode);
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		GLContext.getCurrentGL().getGL2ES1().glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLContext.getCurrentGL().getGL2ES1().glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage) {
		GLContext.getCurrentGL().getGL2ES1().glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		GLContext.getCurrentGL().getGL2ES1().glBufferSubData(target, offset, size, data);
	}

	@Override
	public int glCheckFramebufferStatus(int target) {
		return GLContext.getCurrentGL().getGL2ES1().glCheckFramebufferStatus(target);
	}

	@Override
	public void glClear(int mask) {
		GLContext.getCurrentGL().getGL2ES1().glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().getGL2ES1().glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf(float depth) {
		GLContext.getCurrentGL().getGL2ES1().glClearDepth(depth);
	}

	@Override
	public void glClearStencil(int s) {
		GLContext.getCurrentGL().getGL2ES1().glClearStencil(s);
	}

	@Override
	public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GLContext.getCurrentGL().getGL2ES1().glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
			int imageSize, Buffer data) {
		GLContext.getCurrentGL().getGL2ES1().glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize,
				data);
	}

	@Override
	public final void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
			int format, int imageSize, Buffer data) {
		GLContext.getCurrentGL().getGL2ES1().glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format,
				imageSize, data);
	}

	@Override
	public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
			int border) {
		GLContext.getCurrentGL().getGL2ES1().glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
			int height) {
		GLContext.getCurrentGL().getGL2ES1().glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace(int mode) {
		GLContext.getCurrentGL().getGL2ES1().glCullFace(mode);
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteBuffers(n, buffers);
	}

	@Override
	public void glDeleteBuffer(int buffer) {
		ints[0] = buffer;
		GLContext.getCurrentGL().getGL2ES1().glDeleteBuffers (1, ints, 0);
	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteFramebuffers(n, framebuffers);
	}

	@Override
	public void glDeleteFramebuffer(int framebuffer) {
		ints[0] = framebuffer;
		GLContext.getCurrentGL().getGL2ES1().glDeleteFramebuffers (1, ints, 0);
	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteRenderbuffers(n, renderbuffers);
	}

	@Override
	public void glDeleteRenderbuffer(int renderbuffer) {
		ints[0] = renderbuffer;
		GLContext.getCurrentGL().getGL2ES1().glDeleteRenderbuffers (1, ints, 0);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteTextures(n, textures);
	}

	@Override
	public void glDeleteTexture(int texture) {
		ints[0] = texture;
		GLContext.getCurrentGL().getGL2ES1().glDeleteTextures (1, ints, 0);
	}

	@Override
	public void glDepthFunc(int func) {
		GLContext.getCurrentGL().getGL2ES1().glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		GLContext.getCurrentGL().getGL2ES1().glDepthMask(flag);
	}

	@Override
	public void glDepthRangef(float zNear, float zFar) {
		GLContext.getCurrentGL().getGL2ES1().glDepthRange(zNear, zFar);
	}

	@Override
	public void glDisable(int cap) {
		GLContext.getCurrentGL().getGL2ES1().glDisable(cap);
	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		GLContext.getCurrentGL().getGL2ES1().glDrawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		GLContext.getCurrentGL().getGL2ES1().glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnable(int cap) {
		GLContext.getCurrentGL().getGL2ES1().glEnable(cap);
	}

	@Override
	public void glFinish() {
		GLContext.getCurrentGL().getGL2ES1().glFinish();
	}

	@Override
	public void glFlush() {
		GLContext.getCurrentGL().getGL2ES1().glFlush();
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLContext.getCurrentGL().getGL2ES1().glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GLContext.getCurrentGL().getGL2ES1().glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFrontFace(int mode) {
		GLContext.getCurrentGL().getGL2ES1().glFrontFace(mode);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		GLContext.getCurrentGL().getGL2ES1().glGenBuffers(n, buffers);
	}

	@Override
	public int glGenBuffer() {
		GLContext.getCurrentGL().getGL2ES1().glGenBuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		GLContext.getCurrentGL().getGL2ES1().glGenFramebuffers(n, framebuffers);
	}

	@Override
	public int glGenFramebuffer() {
		GLContext.getCurrentGL().getGL2ES1().glGenFramebuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		GLContext.getCurrentGL().getGL2ES1().glGenRenderbuffers(n, renderbuffers);
	}

	@Override
	public int glGenRenderbuffer() {
		GLContext.getCurrentGL().getGL2ES1().glGenRenderbuffers(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		GLContext.getCurrentGL().getGL2ES1().glGenTextures(n, textures);
	}

	@Override
	public int glGenTexture() {
		GLContext.getCurrentGL().getGL2ES1().glGenTextures(1, ints, 0);
		return ints[0];
	}

	@Override
	public void glGenerateMipmap(int target) {
		GLContext.getCurrentGL().getGL2ES1().glGenerateMipmap(target);
	}

	@Override
	public void glGetBooleanv(int pname, Buffer params) {
		if (!(params instanceof ByteBuffer))
			throw new GdxRuntimeException("params must be a direct ByteBuffer");
		GLContext.getCurrentGL().getGL2ES1().glGetBooleanv(pname, (ByteBuffer) params);
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public int glGetError() {
		return GLContext.getCurrentGL().getGL2ES1().glGetError();
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetFloatv(pname, params);
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetIntegerv(pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public String glGetString(int name) {
		return GLContext.getCurrentGL().getGL2ES1().glGetString(name);
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetTexParameteriv(target, pname, params);
	}

	@Override
	public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glHint(int target, int mode) {
		GLContext.getCurrentGL().getGL2ES1().glHint(target, mode);
	}

	@Override
	public boolean glIsBuffer(int buffer) {
		return GLContext.getCurrentGL().getGL2ES1().glIsBuffer(buffer);
	}

	@Override
	public boolean glIsEnabled(int cap) {
		return GLContext.getCurrentGL().getGL2ES1().glIsEnabled(cap);
	}

	@Override
	public boolean glIsFramebuffer(int framebuffer) {
		return GLContext.getCurrentGL().getGL2ES1().glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsRenderbuffer(int renderbuffer) {
		return GLContext.getCurrentGL().getGL2ES1().glIsRenderbuffer(renderbuffer);
	}

	@Override
	public boolean glIsTexture(int texture) {
		return GLContext.getCurrentGL().getGL2ES1().glIsTexture(texture);
	}

	@Override
	public void glLineWidth(float width) {
		GLContext.getCurrentGL().getGL2ES1().glLineWidth(width);
	}

	@Override
	public void glPixelStorei(int pname, int param) {
		GLContext.getCurrentGL().getGL2ES1().glPixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset(float factor, float units) {
		GLContext.getCurrentGL().getGL2ES1().glPolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GLContext.getCurrentGL().getGL2ES1().glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
		GLContext.getCurrentGL().getGL2ES1().glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage(float value, boolean invert) {
		GLContext.getCurrentGL().getGL2ES1().glSampleCoverage(value, invert);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		GLContext.getCurrentGL().getGL2ES1().glScissor(x, y, width, height);
	}

	@Override
	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		GLContext.getCurrentGL().getGL2ES1().glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilMask(int mask) {
		GLContext.getCurrentGL().getGL2ES1().glStencilMask(mask);
	}

	@Override
	public void glStencilOp(int fail, int zfail, int zpass) {
		GLContext.getCurrentGL().getGL2ES1().glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
			int type, Buffer pixels) {
		GLContext.getCurrentGL().getGL2ES1().glTexImage2D(target, level, internalformat, width, height, border, format, type,
				pixels);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameterfv(target, pname, params);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameteriv(target, pname, params);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
			int type, Buffer pixels) {
		GLContext.getCurrentGL().getGL2ES1().glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, int indices) {
		GLContext.getCurrentGL().getGL2ES1().glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glAlphaFunc(int func, float ref) {
		GLContext.getCurrentGL().getGL2ES1().glAlphaFunc(func, ref);
	}

	@Override
	public void glClientActiveTexture(int texture) {
		try {
			GLContext.getCurrentGL().getGL2ES1().glClientActiveTexture(texture);
		} catch (Throwable ex) {

		}
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().getGL2ES1().glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glDisableClientState(int array) {
		GLContext.getCurrentGL().getGL2ES1().glDisableClientState(array);
	}

	@Override
	public void glEnableClientState(int array) {
		GLContext.getCurrentGL().getGL2ES1().glEnableClientState(array);
	}

	@Override
	public void glFogf(int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glFogf(pname, param);
	}

	@Override
	public void glFogfv(int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glFogfv(pname, params);
	}

	@Override
	public void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar) {
		GLContext.getCurrentGL().getGL2ES1().glFrustum(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public void glLightModelf(int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelf(pname, param);
	}

	@Override
	public void glLightModelfv(int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelfv(pname, params);
	}

	@Override
	public void glLightf(int light, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glLightf(light, pname, param);
	}

	@Override
	public void glLightfv(int light, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glLightfv(light, pname, params);
	}

	@Override
	public void glLoadIdentity() {
		GLContext.getCurrentGL().getGL2ES1().glLoadIdentity();
	}

	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(m);
	}

	@Override
	public void glLogicOp(int opcode) {
		GLContext.getCurrentGL().getGL2ES1().glLogicOp(opcode);
	}

	@Override
	public void glMaterialf(int face, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialf(face, pname, param);
	}

	@Override
	public void glMaterialfv(int face, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialfv(face, pname, params);
	}

	@Override
	public void glMatrixMode(int mode) {
		GLContext.getCurrentGL().getGL2ES1().glMatrixMode(mode);
	}

	@Override
	public void glMultMatrixf(FloatBuffer m) {
		GLContext.getCurrentGL().getGL2ES1().glMultMatrixf(m);
	}

	@Override
	public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
		GLContext.getCurrentGL().getGL2ES1().glMultiTexCoord4f(target, s, t, r, q);
	}

	@Override
	public void glNormal3f(float nx, float ny, float nz) {
		GLContext.getCurrentGL().getGL2ES1().glNormal3f(nx, ny, nz);
	}

	@Override
	public void glNormalPointer(int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar) {
		GLContext.getCurrentGL().getGL2ES1().glOrtho(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public void glPointSize(float size) {
		GLContext.getCurrentGL().getGL2ES1().glPointSize(size);
	}

	@Override
	public void glPopMatrix() {
		GLContext.getCurrentGL().getGL2ES1().glPopMatrix();
	}

	@Override
	public void glPushMatrix() {
		GLContext.getCurrentGL().getGL2ES1().glPushMatrix();
	}

	@Override
	public void glRotatef(float angle, float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glRotatef(angle, x, y, z);
	}

	@Override
	public void glScalef(float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glScalef(x, y, z);
	}

	@Override
	public void glShadeModel(int mode) {
		GLContext.getCurrentGL().getGL2ES1().glShadeModel(mode);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glTexEnvf(int target, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvf(target, pname, param);
	}

	@Override
	public void glTexEnvfv(int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvfv(target, pname, params);
	}

	@Override
	public void glTranslatef(float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glTranslatef(x, y, z);
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		GLContext.getCurrentGL().getGL2ES1().glViewport(x, y, width, height);
	}

	@Override
	public void glDeleteTextures(int n, int[] textures, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteTextures(n, textures, offset);
	}

	@Override
	public void glFogfv(int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glFogfv(pname, params, offset);
	}

	@Override
	public void glGenTextures(int n, int[] textures, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGenTextures(n, textures, offset);
	}

	@Override
	public void glGetIntegerv(int pname, int[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGetIntegerv(pname, params, offset);
	}

	@Override
	public void glLightModelfv(int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelfv(pname, params, offset);
	}

	@Override
	public void glLightfv(int light, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLightfv(light, pname, params, offset);
	}

	@Override
	public void glLoadMatrixf(float[] m, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(m, offset);
	}

	@Override
	public void glMaterialfv(int face, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialfv(face, pname, params, offset);
	}

	@Override
	public void glMultMatrixf(float[] m, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glMultMatrixf(m, offset);
	}

	@Override
	public void glTexEnvfv(int target, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvfv(target, pname, params, offset);
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glDeleteBuffers(n, buffers, offset);
	}

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGenBuffers(n, buffers, offset);
	}

	@Override
	public void glGetLightfv(int light, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetLightfv(light, pname, params);
	}

	@Override
	public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetMaterialfv(face, pname, params);
	}

	@Override
	public void glGetTexEnviv(int env, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetTexEnviv(env, pname, params);
	}

	@Override
	public void glPointParameterf(int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glPointParameterf(pname, param);
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glPointParameterfv(pname, params);
	}

	@Override
	public void glTexEnvi(int target, int pname, int param) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnviv(int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnviv(target, pname, params, offset);
	}

	@Override
	public void glTexEnviv(int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnviv(target, pname, params);
	}

	@Override
	public void glTexParameterfv(int target, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameterfv(target, pname, params, offset);
	}

	@Override
	public void glTexParameteriv(int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexParameteriv(target, pname, params, offset);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glNormalPointer(int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glVertexPointer(size, type, stride, pointer);
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
	public void glFogi(int pname, int params) {
		GLContext.getCurrentGL().getGL2ES1().glFogf(pname, params);
	}

	@Override
	public void glBindTexture(int target, IntBuffer texture) {
		GLContext.getCurrentGL().getGL2ES1().glBindTexture(target, texture.get(0));
	}

	FloatBuffer matrixBuffer = BufferUtils.newFloatBuffer(4 * 4);

	@Override
	public void glLoadMatrixf(float[][] m) {
		matrixBuffer.clear();
		for (int i = 0; i < m.length; i++)
			matrixBuffer.put(m[i]);
		matrixBuffer.rewind();
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(matrixBuffer);
	}

	@Override
	public void glLoadMatrix(Matrix4 m) {
		matrixBuffer.clear();
		matrixBuffer.put(m.getValues());
		matrixBuffer.rewind();
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(matrixBuffer);
	}

	@Override
	public void glVertex2i(int x, int y) {
		glVertex3d(x, y, 0);
	}

	@Override
	public void glVertex2f(float x, float y) {
		glVertex3d(x, y, 0);
	}

	@Override
	public void glVertex2d(double x, double y) {
		glVertex3d(x, y, 0);
	}

	@Override
	public void glVertex3d(double x, double y, double z) {
		vertex((float) x, (float) y, (float) z);
	}

	@Override
	public void glTexCoord2f(float s, float t) {
		texCoord(s, t);

	}

	@Override
	public void glTexCoord2d(double s, double t) {
		texCoord((float) s, (float) t);
	}

	@Override
	public void glColor4ub(int red, int green, int blue, int alpha) {
		GLContext.getCurrentGL().getGL2ES1().glColor4f((red & 0xFF) / 255f, (green & 0xFF) / 255f, (blue & 0xFF) / 255f,
				(alpha & 0xFF) / 255f);
	}

	@Override
	public void glMultiTexCoord2d(int target, double s, double t) {
		GLContext.getCurrentGL().getGL2ES1().glMultiTexCoord4f(target, (float) s, (float) t, 0, 0);
	}

	@Override
	public void glDepthRange(double near_val, double far_val) {
		GLContext.getCurrentGL().getGL2ES1().glDepthRange(near_val, far_val);
	}

	@Override
	public void glDepthMask(int param) {
		GLContext.getCurrentGL().getGL2ES1().glDepthMask(param != GL.GL_FALSE);
	}

	@Override
	public void glClipPlanef(int plane, float a, float b, float c, float d) {
//		GLContext.getCurrentGL().getGL2ES1().glClipPlanef(plane, toPlaneBufferf(a, b, c, d)); XXX
	}

	@Override
	public int glGetInteger(int pname) {
		tempInt.clear();
		GLContext.getCurrentGL().getGL2ES1().glGetIntegerv(pname, tempInt);
		return tempInt.get();
	}

	private static final int MAX_VERTICES = 2000 * 3;

	/** the primitive type **/
	private static int primitiveType;

	/** the vertex position array and buffer **/
	private static float[] positions = new float[3 * MAX_VERTICES];
	private static FloatBuffer positionsBuffer = allocateBuffer(3 * MAX_VERTICES);

	/** the vertex color array and buffer **/
	private static float[] colors = new float[4 * MAX_VERTICES];
	private static FloatBuffer colorsBuffer = allocateBuffer(4 * MAX_VERTICES);

	/** the vertex normal array and buffer **/
	private static float[] normals = new float[3 * MAX_VERTICES];
	private static FloatBuffer normalsBuffer = allocateBuffer(3 * MAX_VERTICES);

	/** the texture coordinate array and buffer **/
	private static float[] texCoords = new float[2 * MAX_VERTICES];
	private static FloatBuffer texCoordsBuffer = allocateBuffer(2 * MAX_VERTICES);

	/** the current vertex attribute indices **/
	private static int idxPos = 0;
	private static int idxCols = 0;
	private static int idxNors = 0;
	private static int idxTexCoords = 0;

	/** which attributes have been defined **/
	private static boolean colorsDefined = false;
	private static boolean normalsDefined = false;
	private static boolean texCoordsDefined = false;

	private static FloatBuffer allocateBuffer(int numFloats) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numFloats * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asFloatBuffer();
	}

	/**
	 * Starts a new list of primitives. The primitiveType specifies which primitives
	 * to draw. Can be any of GLES1.GL_TRIANGLES, GLES1.GL_LINES and so on. A
	 * maximum of 6000 vertices can be drawn at once.
	 *
	 * @param primitiveType the primitive type.
	 */
	public void begin(int primitiveType) {
		Jogles10.primitiveType = primitiveType;
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
	 *
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 */
	public void color(float r, float g, float b, float a) {
		colors[idxCols] = r;
		colors[idxCols + 1] = g;
		colors[idxCols + 2] = b;
		colors[idxCols + 3] = a;
		colorsDefined = true;
	}

	/**
	 * Specifies the normal of the current vertex
	 *
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void normal(float x, float y, float z) {
		normals[idxNors] = x;
		normals[idxNors + 1] = y;
		normals[idxNors + 2] = z;
		normalsDefined = true;
	}

	/**
	 * Specifies the texture coordinates of the current vertex
	 *
	 * @param u the u coordinate
	 * @param v the v coordinate
	 */
	public void texCoord(float u, float v) {
		texCoords[idxTexCoords] = u;
		texCoords[idxTexCoords + 1] = v;
		texCoordsDefined = true;
	}

	/**
	 * Specifies the position of the current vertex and finalizes it. After a call
	 * to this method you will effectively define a new vertex afterwards.
	 *
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void vertex(float x, float y, float z) {
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
	public void end() {
		if (idxPos == 0)
			return;

		GLContext.getCurrentGL().getGL2ES1().glEnableClientState(GLES1.GL_VERTEX_ARRAY);
		positionsBuffer.clear();
		positionsBuffer.put(positions, 0, idxPos);
		positionsBuffer.flip();
		GLContext.getCurrentGL().getGL2ES1().glVertexPointer(3, GLES1.GL_FLOAT, 0, positionsBuffer);

		if (colorsDefined) {
			GLContext.getCurrentGL().getGL2ES1().glEnableClientState(GLES1.GL_COLOR_ARRAY);
			colorsBuffer.clear();
			colorsBuffer.put(colors, 0, idxCols);
			colorsBuffer.flip();
			GLContext.getCurrentGL().getGL2ES1().glColorPointer(4, GLES1.GL_FLOAT, 0, colorsBuffer);
		}

		if (normalsDefined) {
			GLContext.getCurrentGL().getGL2ES1().glEnableClientState(GLES1.GL_NORMAL_ARRAY);
			normalsBuffer.clear();
			normalsBuffer.put(normals, 0, idxNors);
			normalsBuffer.flip();
			GLContext.getCurrentGL().getGL2ES1().glNormalPointer(GLES1.GL_FLOAT, 0, normalsBuffer);
		}

		if (texCoordsDefined) {
			glClientActiveTexture(GLES1.GL_TEXTURE0);
			GLContext.getCurrentGL().getGL2ES1().glEnableClientState(GLES1.GL_TEXTURE_COORD_ARRAY);
			texCoordsBuffer.clear();
			texCoordsBuffer.put(texCoords, 0, idxTexCoords);
			texCoordsBuffer.flip();
			GLContext.getCurrentGL().getGL2ES1().glTexCoordPointer(2, GLES1.GL_FLOAT, 0, texCoordsBuffer);
		}

		GLContext.getCurrentGL().getGL2ES1().glDrawArrays(primitiveType, 0, idxPos / 3);

		GLContext.getCurrentGL().getGL2ES1().glDisableClientState(GLES1.GL_VERTEX_ARRAY);
		if (colorsDefined)
			GLContext.getCurrentGL().getGL2ES1().glDisableClientState(GLES1.GL_COLOR_ARRAY);
		if (normalsDefined)
			GLContext.getCurrentGL().getGL2ES1().glDisableClientState(GLES1.GL_NORMAL_ARRAY);
		if (texCoordsDefined)
			GLContext.getCurrentGL().getGL2ES1().glDisableClientState(GLES1.GL_TEXTURE_COORD_ARRAY);
	}

	@Override
	public void glReadBuffer(int mode) {
//		GLContext.getCurrentGL().getGL2ES1().glReadBuffer(mode); XXX
	}

	@Override
	public void glPopAttrib() {
//		GLContext.getCurrentGL().getGL2ES1().glPopAttrib(); XXX
	}

	@Override
	public void glPushAttrib(int mask) {
//		GLContext.getCurrentGL().getGL2ES1().glPushAttrib(mask); XXX
	}

	@Override
	public void glPolygonMode(int face, int mode) {
//		GLContext.getCurrentGL().getGL2().glPolygonMode(face, mode); // XXX GL2?
	}
}
