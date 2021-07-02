package ru.m210projects.Build.Render.GdxRender.Shaders;

public class WorldShader {

	public static final String vertex = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "    precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP\n" //
			+ "#endif\n" //
			+ "\n" //
			+ "attribute vec4 a_position;\n" //
			+ "attribute vec2 a_texCoord0;\n" //
			+ "\n" //
			+ "uniform mat4 u_modelView;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "uniform mat4 u_spriteTrans;\n" //
			+ "uniform bool u_drawSprite;\n" //
			+ "uniform bool u_mirror;\n" //
			+ "\n" //
			+ "varying LOWP float v_dist;\n" //
			+ "varying vec3 v_pos;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main() {\n" //
			+ "    v_texCoords = a_texCoord0;\n" //
			+ "  \n" //
			+ "    vec4 mv;\n" //
			+ "    if(!u_drawSprite) {\n" //
			+ "        mv = u_modelView * a_position;\n" //
			+ "        gl_Position = u_projTrans * a_position;\n" //
			+ "    } else {\n" //
			+ "        mv = u_modelView * u_spriteTrans  * a_position;\n" //
			+ "        gl_Position = u_projTrans * u_spriteTrans * a_position;\n" //
			+ "    }\n" //
			+ "    if(u_mirror)\n" //
			+ "        gl_Position.x *= -1.0;\n" //
			+ "    v_dist = mv.z / mv.w;\n" //
			+ "};\n" //
			+ ""; //

	public static final String fragment = "uniform sampler2D u_texture;\n" //
			+ "uniform sampler2D u_palette;\n" //
			+ "uniform sampler2D u_palookup;\n" //
			+ "\n" //
			+ "uniform float u_numshades;\n" //
			+ "uniform float u_visibility;\n" //
			+ "uniform int u_shade;\n" //
			+ "uniform bool u_draw255;\n" //
			+ "uniform float u_alpha;\n" //
			+ "\n" //
			+ "varying float v_dist;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "uniform mat4 u_invProjectionView;\n" //
			+ "uniform vec4 u_plane[2];\n" //
			+ "uniform vec4 u_viewport;\n" //
			+ "uniform bool u_frustumClipping;\n" //
			+ "\n" //
			+ "float getpalookup(int dashade) {\n" //
			+ "    float davis = v_dist * u_visibility;\n" //
			+ "    float shade = (min(max(float(dashade) + davis, 0.0), u_numshades - 1.0));\n" //
			+ "    return shade / 64.0;\n" //
			+ "}\n" //
			+ "\n" //
			+ "vec4 getPos() {\n" //
			+ "    vec4 ndc;\n" //
			+ "	   vec2 xy = gl_FragCoord.xy - vec2(u_viewport.xy);" //
			+ "    ndc.xy = (2.0 * xy) / u_viewport.zw - 1.0;\n" //
			+ "    ndc.z = (2.0 * gl_FragCoord.z) - 1.0;\n" //
			+ "    ndc.w = 1.0;\n" //
			+ "    \n" //
			+ "    vec4 worldCoords = u_invProjectionView * ndc;\n" //
			+ "    worldCoords.xyz /= worldCoords.w;\n" //
			+ "    worldCoords.xyz *= vec3(512.0, 512.0, 8192.0); // BuildEngine coords scale\n" //
			+ "    worldCoords.w = 1.0;\n" //
			+ "    return worldCoords;\n" //
			+ "}\n" //
			+ "\n" //
			+ "bool isvisible() {\n" //
			+ "    vec4 pos = getPos();\n" //
			+ "    for(int i = 0; i < 2; i++) {\n" //
			+ "        if(dot(u_plane[i], pos) < 0.0)\n" //
			+ "            return false;\n" //
			+ "    }\n" //
			+ "    return true;\n" //
			+ "}\n" //
			+ "\n" //
			+ "void main() {  \n" //
			+ "    if((u_frustumClipping && !isvisible()))\n" //
			+ "        discard;\n" //
			+ "\n" //
			+ "    float fi = texture2D(u_texture, v_texCoords).r;\n" //
			+ "    if(fi == 1.0) {\n" //
			+ "        if(!u_draw255) {\n" //
			+ "            gl_FragColor = vec4(0.01);\n" //
			+ "            return;\n" //
			+ "        }\n" //
			+ "        fi -= 0.5 / 256.0;\n" //
			+ "    }\n" //
			+ "    float index = texture2D(u_palookup, vec2(fi, getpalookup(u_shade))).r;\n" //
			+ "    if(index == 1.0) index -= 0.5 / 256.0;\n" //
			+ "\n" //
			+ "    gl_FragColor = vec4(texture2D(u_palette, vec2(index, 0.0)).rgb, u_alpha);\n" //
			+ "}"; //

}
