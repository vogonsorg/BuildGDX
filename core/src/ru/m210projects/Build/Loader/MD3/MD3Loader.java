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

package ru.m210projects.Build.Loader.MD3;

import java.nio.FloatBuffer;
import java.util.HashMap;

import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.FileHandle.Resource.Whence;
import ru.m210projects.Build.Loader.Model;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;

public class MD3Loader {
	private static int maxtris = 0;
	private static int maxverts = 0;	
	
	public static Model load(Resource bb) {
		MD3Header header = loadHeader(bb);
		
		if ((header.ident != 0x33504449) || (header.version != 15)) return null; //"IDP3"
		MD3Frame[] frames = loadFrames(header, bb);
		HashMap<String, Matrix4>[] tags = loadTags(header, bb);
		MD3Surface[] surfaces = loadSurfaces(header, bb);

		MD3Model m = new MD3Model();

		m.mdnum = 3;
		m.scale = 0.01f;
		m.head = header;
		m.numskins = header.numSkins;
	    m.numframes = header.numFrames;
	    m.frames = frames;
		m.tags = tags;
		m.surfaces = surfaces;
		m.indicesBuffer = BufferUtils.newShortBuffer(maxtris * 3);
		m.verticesBuffer = BufferUtils.newFloatBuffer(maxverts * 3);

		return m;
	}

	private static MD3Header loadHeader (Resource bb) {
		MD3Header header = new MD3Header();

		header.ident = bb.readInt();
		header.version = bb.readInt();
		header.filename = readString(bb, 64);
		header.flags = bb.readInt();
		header.numFrames = bb.readInt();
		header.numTags = bb.readInt();
		header.numSurfaces = bb.readInt();
		header.numSkins = bb.readInt();
		header.offsetFrames = bb.readInt();
		header.offsetTags = bb.readInt();
		header.offsetSurfaces = bb.readInt();
		header.offsetEnd = bb.readInt();

		return header;
	}
	
	private static MD3Frame[] loadFrames (MD3Header header, Resource bb) {
		bb.seek(header.offsetFrames, Whence.Set);
		MD3Frame[] out = new MD3Frame[header.numFrames];
        for(int i = 0; i < header.numFrames; i++) {
        	MD3Frame frame = new MD3Frame();
        	frame.min = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
        	frame.max = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
        	frame.origin = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
        	frame.radius = bb.readFloat();
        	frame.name = readString(bb, 16);
        	out[i] = frame;
        }
        return out;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Matrix4>[] loadTags (MD3Header header, Resource bb) {
		bb.seek(header.offsetTags, Whence.Set);
		HashMap<String, Matrix4>[] out = (HashMap<String, Matrix4>[]) new HashMap[header.numFrames];
		for (int k = 0; k < header.numFrames; k++) {
			out[k] = new HashMap<String, Matrix4>();
		    for (int i = 0; i < header.numTags; i++) {
		    	String tagName = readString(bb, 64);

		        Vector3 pos = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
		        Vector3 xAxis = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
		        Vector3 yAxis = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
		        Vector3 zAxis = new Vector3(bb.readFloat(), bb.readFloat(), bb.readFloat());
		        Matrix4 mat = new Matrix4();
		        mat.set(xAxis, yAxis, zAxis, pos);

		        out[k].put(tagName, mat);
			}
		}
		return out;
	}
	
	private static MD3Surface[] loadSurfaces(MD3Header header, Resource bb) {
		int offsetSurfaces = header.offsetSurfaces;
		MD3Surface[] out = new MD3Surface[header.numSurfaces];
        for(int i = 0; i < header.numSurfaces; i++) {
        	bb.seek(offsetSurfaces, Whence.Set);
        	MD3Surface surf = new MD3Surface();
        	surf.id = bb.readInt();
        	surf.nam = readString(bb, 64);
        	surf.flags = bb.readInt();
        	surf.numframes = bb.readInt();
        	surf.numshaders = bb.readInt();
        	surf.numverts = bb.readInt();
        	surf.numtris = bb.readInt();
        	surf.ofstris = bb.readInt();
        	surf.ofsshaders = bb.readInt();
        	surf.ofsuv = bb.readInt();
        	surf.ofsxyzn = bb.readInt();
        	surf.ofsend = bb.readInt();
        	
        	surf.tris = loadTriangles(surf, offsetSurfaces, bb);
        	surf.shaders = loadShaders(surf, offsetSurfaces, bb);
        	surf.uv = loadUVs(surf, offsetSurfaces, bb);
        	surf.xyzn = loadVertices(surf, offsetSurfaces, bb);
        	maxtris = Math.max(maxtris, surf.numtris);
        	maxverts = Math.max(maxtris, surf.numverts);
        	offsetSurfaces += surf.ofsend;
        	
        	out[i] = surf;
        }
        return out;
	}
	
	private static int[][] loadTriangles(MD3Surface surf, int offsetSurfaces, Resource bb)
	{
		bb.seek(offsetSurfaces + surf.ofstris, Whence.Set);
		int[][] out = new int[surf.numtris][3];
		for(int i = 0; i < surf.numtris; i++) {
			out[i][0] = bb.readInt();
			out[i][1] = bb.readInt();
			out[i][2] = bb.readInt();
		}
		return out;
	}
	
	private static FloatBuffer loadUVs(MD3Surface surf, int offsetSurfaces, Resource bb)
	{
		bb.seek(offsetSurfaces +  surf.ofsuv, Whence.Set);
		FloatBuffer out = BufferUtils.newFloatBuffer(2 * surf.numverts);
		for(int i = 0; i < surf.numverts; i++) {
			out.put(bb.readFloat());
			out.put(bb.readFloat());
		}
		out.flip();
		return out;
	}
	
	private static MD3Vertice[] loadVertices(MD3Surface surf, int offsetSurfaces, Resource bb)
	{
		bb.seek(offsetSurfaces +  surf.ofsxyzn, Whence.Set);
		MD3Vertice[] out = new MD3Vertice[surf.numframes * surf.numverts];
		for(int i = 0; i < out.length; i++)
		{
			MD3Vertice xyzn = new MD3Vertice();
			xyzn.x = bb.readShort();
			xyzn.y = bb.readShort();
			xyzn.z = bb.readShort();
			xyzn.nlat = (short) (bb.readByte() & 0xFF);
			xyzn.nlng = (short) (bb.readByte() & 0xFF);
			out[i] = xyzn;
		}
		return out;
	}
	
	private static MD3Shader[] loadShaders(MD3Surface surf, int offsetSurfaces, Resource bb)
	{
		bb.seek(offsetSurfaces + surf.ofsshaders, Whence.Set);
		MD3Shader[] out = new MD3Shader[surf.numshaders];
		for(int i = 0; i < surf.numshaders; i++) {
			MD3Shader shader = new MD3Shader();
			shader.name = readString(bb, 64);
			shader.index = bb.readInt();
			out[i] = shader;
		}
		return out;
	}
	
	private static String readString(Resource bb, int len) {
		byte[] buf = new byte[len];
		bb.read(buf);

		for(int i = 0; i < buf.length; i++) {
        	if(buf[i] == 0) 
        		return new String(buf, 0, i);
		}
		return new String(buf);
	}
}
