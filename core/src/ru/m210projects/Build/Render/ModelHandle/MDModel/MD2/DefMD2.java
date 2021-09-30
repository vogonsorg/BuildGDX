package ru.m210projects.Build.Render.ModelHandle.MDModel.MD2;

import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.FileHandle.Resource.Whence;
import ru.m210projects.Build.Render.ModelHandle.DefMD;

public class DefMD2 extends DefMD {

	public final MD2Header header;

	public DefMD2(Resource res, String file) {
		super(file, Type.Md2);

		this.header = loadHeader(res);
		res.seek(header.offsetFrames, Whence.Set);
		this.frames = new String[header.numFrames];
		this.numframes = header.numFrames;

		for (int i = 0; i < header.numFrames; i++) {
			res.seek(6, Whence.Current);
			frames[i] = readString(res, 16);
			res.seek(header.numVertices * 4, Whence.Current);
		}
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
