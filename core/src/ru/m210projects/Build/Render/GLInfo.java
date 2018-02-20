package ru.m210projects.Build.Render;

import static ru.m210projects.Build.Engine.glanisotropy;
import static ru.m210projects.Build.Render.GL10.GL_DITHER;
import static ru.m210projects.Build.Render.GL10.GL_EXTENSIONS;
import static ru.m210projects.Build.Render.GL10.GL_LINE_SMOOTH_HINT;
import static ru.m210projects.Build.Render.GL10.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static ru.m210projects.Build.Render.GL10.GL_NICEST;
import static ru.m210projects.Build.Render.GL10.GL_PERSPECTIVE_CORRECTION_HINT;
import static ru.m210projects.Build.Render.GL10.GL_RENDERER;
import static ru.m210projects.Build.Render.GL10.GL_SMOOTH;
import static ru.m210projects.Build.Render.GL10.GL_TEXTURE_2D;
import static ru.m210projects.Build.Render.GL10.GL_VENDOR;
import static ru.m210projects.Build.Render.GL10.GL_VERSION;
import static ru.m210projects.Build.Strhandler.Bstrtoken;

import java.nio.FloatBuffer;


import com.badlogic.gdx.utils.BufferUtils;

public class GLInfo {
	public static String vendor;
	public static String renderer;
	public static String version;
	public static String extensions;

	public static float maxanisotropy;
	public static byte bgra;
	public static boolean clamptoedge;
	public static byte texcompr;
	public static byte texnpot;
	public static byte multisample;
	public static byte nvmultisamplehint;
	public static byte arbfp;
	public static byte depthtex;
	public static byte shadow;
	public static byte fbos;
	public static byte rect;
	public static byte multitex;
	public static byte envcombine;
	public static byte vbos;
	public static byte vsync;
	public static byte sm4;
	public static byte occlusionqueries;
	public static byte glsl;
	public static byte dumped;
	public static int hack_nofog;

	public static void init(GL10 gl) {
		gl.bglEnable(GL_TEXTURE_2D);
		gl.bglShadeModel(GL_SMOOTH); // GL_FLAT
		gl.bglClearColor(0, 0, 0, 0.5); // Black Background
		gl.bglHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // Use FASTEST for ortho!
		gl.bglHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		gl.bglDisable(GL_DITHER);

		GLInfo.vendor = gl.bglGetString(GL_VENDOR);
		GLInfo.renderer = gl.bglGetString(GL_RENDERER);
		GLInfo.version = gl.bglGetString(GL_VERSION);
		GLInfo.extensions = gl.bglGetString(GL_EXTENSIONS);

		GLInfo.maxanisotropy = 1.0f;
		GLInfo.bgra = 0;
		GLInfo.texcompr = 0;

		// process the extensions string and flag stuff we recognize
		String p = GLInfo.extensions.toUpperCase(), p2;

		int start = 0;
		int offset = Bstrtoken(p, ' ', start);

		while (offset != p.length()) {
			p2 = p.substring(start, offset);

			if (p2.compareToIgnoreCase("GL_EXT_texture_filter_anisotropic") == 0) {
				// supports anisotropy. get the maximum anisotropy level
				FloatBuffer buf = BufferUtils.newFloatBuffer(16);
				gl.bglGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buf);
				GLInfo.maxanisotropy = buf.get();
			} else if (p2.compareToIgnoreCase("GL_EXT_texture_edge_clamp") == 0 ||
					p2.compareToIgnoreCase("GL_SGIS_texture_edge_clamp") == 0) {
				// supports GL_CLAMP_TO_EDGE or GL_CLAMP_TO_EDGE_SGIS
				GLInfo.clamptoedge = true;
			} else if (p2.compareToIgnoreCase("GL_EXT_bgra") == 0) {
				// support bgra textures
				GLInfo.bgra = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_texture_compression") == 0) {
				// support texture compression
				GLInfo.texcompr = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_texture_non_power_of_two") == 0) {
				// support non-power-of-two texture sizes
				GLInfo.texnpot = 1;
			} else if (p2.compareToIgnoreCase("WGL_3DFX_gamma_control") == 0) {
				// 3dfx cards have issues with fog
				GLInfo.hack_nofog = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_multisample") == 0) {
				// supports multisampling
				GLInfo.multisample = 1;
			} else if (p2.compareToIgnoreCase("GL_NV_multisample_filter_hint") == 0) {
				// supports nvidia's multisample hint extension
				GLInfo.nvmultisamplehint = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_multitexture") == 0) {
				GLInfo.multitex = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_texture_env_combine") == 0) {
				GLInfo.envcombine = 1;
			} else if (p2.compareToIgnoreCase("GL_ARB_VERTEX_BUFFER_OBJECT") == 0) {
				GLInfo.vbos = 1;
			}

			start = offset + 1;
			offset = Bstrtoken(p, ' ', start);
		}
	}

	public static int anisotropy() {
		if (GLInfo.maxanisotropy > 1.0) {
			if (glanisotropy <= 0 || glanisotropy > GLInfo.maxanisotropy)
				glanisotropy = (int) GLInfo.maxanisotropy;
		}
		return glanisotropy;
	}
}
