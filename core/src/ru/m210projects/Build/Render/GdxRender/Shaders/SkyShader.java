package ru.m210projects.Build.Render.GdxRender.Shaders;

public class SkyShader {

	public static final String vertex = "attribute vec4 a_position;\n" //
			+ "\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "uniform mat4 u_transform;\n" //
			+ "uniform bool u_mirror;\n" //
			+ "varying vec4 v_pos;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "    v_pos = u_transform * a_position;\n" //
			+ "    gl_Position = u_projTrans * v_pos;\n" //
			+ "    if(u_mirror)\n" //
			+ "        gl_Position.x *= -1.0;\n" //
			+ "};\n"; //

	public static final String fragment = "uniform sampler2D u_sky;\n" //
			+ "uniform sampler2D u_palette;\n" //
			+ "uniform sampler2D u_palookup;\n" //
			+ "uniform float u_alpha;\n" //
			+ "uniform vec3 u_camera;\n" //
			+ "varying vec4 v_pos;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "    vec4 pix = normalize(v_pos - vec4(u_camera, 1.0));\n" //
			+ "    vec2 uv = vec2(atan(pix.y, pix.x), asin(pix.z) * 2.0);\n" //
			+ "    uv /= 3.1415926;\n" //
			+ "    uv = uv * 0.5 + 0.5;\n" //
			+ "    float fi = texture2D(u_sky, uv).r;\n" //
			+ "    if(fi == 1.0) fi -= 0.5 / 256.0;\n" //
			+ "    float index = texture2D(u_palookup, vec2(fi, 1)).r;\n" //
			+ "    if(index == 1.0) index -= 0.5 / 256.0;\n" //
			+ "    gl_FragColor = vec4(texture2D(u_palette, vec2(index, 0.0)).rgb, u_alpha);\n" //
			+ "};";

	public static final String fragmentRGB = "uniform sampler2D u_sky;\n" //
			+ "uniform vec3 u_camera;\n" //
			+ "varying vec4 v_pos;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "    vec4 pix = normalize(v_pos - vec4(u_camera, 1.0));\n" //
			+ "    vec2 uv = vec2(atan(pix.y, pix.x), asin(pix.z) * 2.0);\n" //
			+ "    uv /= 3.1415926;\n" //
			+ "    uv = uv * 0.5 + 0.5;\n" //
			+ "    gl_FragColor = texture2D(u_sky, uv);\n" //
			+ "};";
}
