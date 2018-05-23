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

package ru.m210projects.Build.Loader.MD2;

import java.nio.ByteBuffer;

import ru.m210projects.Build.Loader.Model;

import com.badlogic.gdx.utils.BufferUtils;

public class MD2Loader {	

	public static Model load(ByteBuffer bb) {
		MD2Header header = loadHeader(bb);
		
		if ((header.ident != 0x32504449) || (header.version != 8)) return null; //"IDP2"

		MD2Triangle[] triangles = loadTriangles(header, bb);
		float[] texCoords = loadTexCoords(header, bb);
		MD2Frame[] frames = loadFrames(header, bb);
		int[] glcmds = loadGLCommands(header, bb);

		MD2Model out = new MD2Model();
		out.mdnum = 2;
		out.scale = 0.01f;
		out.numframes = header.numFrames;
		out.numverts = header.numVertices;
		out.numglcmds = header.numGLCommands;
		out.frames = frames;
		out.tris = triangles;
//		out.uv = texCoords;
		out.glcmds = glcmds;
		
		out.indicesBuffer = BufferUtils.newShortBuffer(header.numTriangles * 3);
    	for (int i = 0; i < header.numTriangles; i++)
    		for( int j = 0; j < 3; j++) 
    			out.indicesBuffer.put((short) (i * 3 + j)); //m.tris[i].vertices[j]);
    	out.indicesBuffer.flip();
    	
		out.verticesBuffer = BufferUtils.newFloatBuffer(header.numTriangles * 3 * 3); //header.numVertices * 3
		out.uv = BufferUtils.newFloatBuffer(header.numTriangles * 3 * 2);
		for (int i = 0; i < header.numTriangles; i++)
    		for( int j = 0; j < 3; j++) 
    		{
    			int idx = triangles[i].texCoords[j];
    			out.uv.put(texCoords[2 * idx]);
    			out.uv.put(texCoords[2 * idx + 1]);
    		}
		out.uv.flip();

		return out; //buildModel(header, triangles, texCoords, frames);
	}

	private static final byte[] charBuffer = new byte[16];
	private static MD2Frame[] loadFrames (MD2Header header, ByteBuffer bb) {
		bb.position(header.offsetFrames);
		MD2Frame[] frames = new MD2Frame[header.numFrames];
		
		for (int i = 0; i < header.numFrames; i++) {
			MD2Frame frame = new MD2Frame();
			frame.vertices = new float[header.numVertices][3];
	
			float scaleX = bb.getFloat(), scaleY = bb.getFloat(), scaleZ = bb.getFloat();
			float transX = bb.getFloat(), transY = bb.getFloat(), transZ = bb.getFloat();
			bb.get(charBuffer);
	
			int len;
			for (len = 0; len < charBuffer.length; len++)
				if (charBuffer[len] == 0) 
					break;
	
			frame.name = new String(charBuffer, 0, len);
			for (int j = 0; j < header.numVertices; j++) {
				float x = (bb.get() & 0xFF) * scaleX + transX;
				float y = (bb.get() & 0xFF) * scaleY + transY;
				float z = (bb.get() & 0xFF) * scaleZ + transZ;
				bb.get(); // normal index
				
				frame.vertices[j][0] = x;
				frame.vertices[j][1] = y;
				frame.vertices[j][2] = z;
			}

			frames[i] = frame;
		}
		return frames;
	}

	private static MD2Triangle[] loadTriangles (MD2Header header, ByteBuffer bb) {
		
		bb.position(header.offsetTriangles);
		MD2Triangle[] triangles = new MD2Triangle[header.numTriangles];

		for (int i = 0; i < header.numTriangles; i++) {
			MD2Triangle triangle = new MD2Triangle();
			triangle.vertices[0] = bb.getShort();
			triangle.vertices[1] = bb.getShort();
			triangle.vertices[2] = bb.getShort();
			triangle.texCoords[0] = bb.getShort();
			triangle.texCoords[1] = bb.getShort();
			triangle.texCoords[2] = bb.getShort();
			triangles[i] = triangle;
		}

		return triangles;
	}

	private static float[] loadTexCoords (MD2Header header,ByteBuffer bb) {
		bb.position(header.offsetTexCoords);
		float[] texCoords = new float[header.numTexCoords * 2];
		float width = header.skinWidth;
		float height = header.skinHeight;

		for (int i = 0; i < header.numTexCoords; i++) {
			short u = bb.getShort();
			short v = bb.getShort();
			texCoords[2 * i] = (u / width);
			texCoords[2 * i + 1] =(v / height);
		}
		return texCoords;
	}

	private static MD2Header loadHeader (ByteBuffer bb) {
		MD2Header header = new MD2Header();

		header.ident = bb.getInt();
		header.version = bb.getInt();
		header.skinWidth = bb.getInt();
		header.skinHeight = bb.getInt();
		header.frameSize = bb.getInt();
		header.numSkins = bb.getInt();
		header.numVertices = bb.getInt();
		header.numTexCoords = bb.getInt();
		header.numTriangles = bb.getInt();
		header.numGLCommands = bb.getInt();
		header.numFrames = bb.getInt();
		header.offsetSkin = bb.getInt();
		header.offsetTexCoords = bb.getInt();
		header.offsetTriangles = bb.getInt();
		header.offsetFrames = bb.getInt();
		header.offsetGLCommands = bb.getInt();
		header.offsetEnd = bb.getInt();

		return header;
	}
	
	public static int[] loadGLCommands(MD2Header header, ByteBuffer bb)
	{
		bb.position(header.offsetGLCommands);
		int[] glcmds = new int[header.numGLCommands];
		
		for (int i = 0; i < header.numGLCommands; i++)
			glcmds[i] = bb.getInt();
		return glcmds;
	}

	/*
	private static Model buildModel (MD2Header header, MD2Triangle[] triangles, float[] texCoords, MD2Frame[] frames) {
		ArrayList<VertexIndices> vertCombos = new ArrayList<VertexIndices>();
		short[] indices = new short[triangles.length * 3];
		int idx = 0;
		short vertIdx = 0;
		for (int i = 0; i < triangles.length; i++) {
			MD2Triangle triangle = triangles[i];
			for (int j = 0; j < 3; j++) {
				VertexIndices vert = null;
				boolean contains = false;
				for (int k = 0; k < vertCombos.size(); k++) {
					VertexIndices vIdx = vertCombos.get(k);
					if (vIdx.vIdx == triangle.vertices[j] && vIdx.tIdx == triangle.texCoords[j]) {
						vert = vIdx;
						contains = true;
						break;
					}
				}
				if (!contains) {
//					vert = new VertexIndices(triangle.vertices[j], triangle.texCoords[j], vertIdx);
					vertCombos.add(vert);
					vertIdx++;
				}

				indices[idx++] = vert.nIdx;
			}
		}

		idx = 0;
		float[] uvs = new float[vertCombos.size() * 2];
		for (int i = 0; i < vertCombos.size(); i++) {
			VertexIndices vtI = vertCombos.get(i);
			uvs[idx++] = texCoords[vtI.tIdx * 2];
			uvs[idx++] = texCoords[vtI.tIdx * 2 + 1];
		}

		for (int i = 0; i < frames.length; i++) {
			MD2Frame frame = frames[i];
			idx = 0;
			float[] newVerts = new float[vertCombos.size() * 3];

			for (int j = 0; j < vertCombos.size(); j++) {
				VertexIndices vIdx = vertCombos.get(j);
				newVerts[idx++] = frame.vertices[vIdx.vIdx][0];
				newVerts[idx++] = frame.vertices[vIdx.vIdx][1];
				newVerts[idx++] = frame.vertices[vIdx.vIdx][2];
			}
//			frame.vertices = newVerts;
		}
		header.numVertices = vertCombos.size();

//		subMesh.mesh = new Mesh(false, header.numTriangles * 3, indices.length, new VertexAttribute(Usage.Position, 3, "a_pos"),
//			new VertexAttribute(Usage.TextureCoordinates, 2, "a_tex0"));
//		subMesh.mesh.setIndices(indices);
//		subMesh.animations.put("all", animation);
//		subMesh.primitiveType = GL10.GL_TRIANGLES;		
//		KeyframedModel model = new KeyframedModel();
//		model.setAnimation("all", 0);
		return new Model();
	}
	*/
}
