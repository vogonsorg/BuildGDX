package ru.m210projects.Build.Render.ModelHandle.MDModel.MD2;

import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.FileHandle.Resource.Whence;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;

public class DefMD2 extends MDModel {

	public final MD2Frame[] frames;
	public final MD2Header header;

	public DefMD2(Resource res, String file) {
		super(file, Type.Md2);

		this.header = loadHeader(res);
		res.seek(header.offsetFrames, Whence.Set);
		this.frames = new MD2Frame[header.numFrames];
		this.numframes = header.numFrames;
		for (int i = 0; i < header.numFrames; i++) {
			MD2Frame frame = new MD2Frame();
			frame.vertices = new float[header.numVertices][3];

			float scaleX = res.readFloat(), scaleY = res.readFloat(), scaleZ = res.readFloat();
			float transX = res.readFloat(), transY = res.readFloat(), transZ = res.readFloat();
			frame.name = readString(res, 16);

			for (int j = 0; j < header.numVertices; j++) {
				float x = (res.readByte() & 0xFF) * scaleX + transX;
				float y = (res.readByte() & 0xFF) * scaleY + transY;
				float z = (res.readByte() & 0xFF) * scaleZ + transZ;
				res.readByte(); // normal index

				frame.vertices[j][0] = x;
				frame.vertices[j][1] = y;
				frame.vertices[j][2] = z;
			}

			frames[i] = frame;
		}
	}

	@Override
	public int getFrameIndex(String framename) {
		for (int i = 0; i < numframes; i++) {
			MD2Frame fr = frames[i];
			if (fr != null && fr.name.equalsIgnoreCase(framename)) {
				return i;
			}
		}

		return (-3); // frame name invalid
	}

	protected MD2Header loadHeader(Resource res) {
		MD2Header header = new MD2Header();

		header.ident = res.readInt();
		header.version = res.readInt();
		header.skinWidth = res.readInt();
		header.skinHeight = res.readInt();
		header.frameSize = res.readInt();
		header.numSkins = res.readInt();
		header.numVertices = res.readInt();
		header.numTexCoords = res.readInt();
		header.numTriangles = res.readInt();
		header.numGLCommands = res.readInt();
		header.numFrames = res.readInt();
		header.offsetSkin = res.readInt();
		header.offsetTexCoords = res.readInt();
		header.offsetTriangles = res.readInt();
		header.offsetFrames = res.readInt();
		header.offsetGLCommands = res.readInt();
		header.offsetEnd = res.readInt();

		return header;
	}
}
