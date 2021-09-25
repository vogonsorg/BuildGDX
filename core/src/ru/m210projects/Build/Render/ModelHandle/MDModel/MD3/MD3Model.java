package ru.m210projects.Build.Render.ModelHandle.MDModel.MD3;

import com.badlogic.gdx.math.Vector3;

import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.FileHandle.Resource.Whence;
import ru.m210projects.Build.Render.ModelHandle.MDModel.MDModel;

public class MD3Model extends MDModel {

	public final MD3Header header;
	public final MD3Frame[] frames;

	public MD3Model(Resource res, String file) {
		super(file, Type.Md3);

		header = loadHeader(res);

		res.seek(header.offsetFrames, Whence.Set);
		frames = new MD3Frame[header.numFrames];
        for(int i = 0; i < header.numFrames; i++) {
        	MD3Frame frame = new MD3Frame();
        	frame.min = new Vector3(res.readFloat(), res.readFloat(), res.readFloat());
        	frame.max = new Vector3(res.readFloat(), res.readFloat(), res.readFloat());
        	frame.origin = new Vector3(res.readFloat(), res.readFloat(), res.readFloat());
        	frame.radius = res.readFloat();
        	frame.name = readString(res, 16);
        	frames[i] = frame;
        }
	}

	@Override
	public int getFrameIndex(String framename) {
		for (int i = 0; i < numframes; i++) {
			MD3Frame fr = frames[i];
			if (fr != null && fr.name.equalsIgnoreCase(framename)) {
				return i;
			}
		}

		return (-3); // frame name invalid
	}

	protected MD3Header loadHeader (Resource res) {
		MD3Header header = new MD3Header();

		header.ident = res.readInt();
		header.version = res.readInt();
		header.filename = readString(res, 64);
		header.flags = res.readInt();
		header.numFrames = res.readInt();
		header.numTags = res.readInt();
		header.numSurfaces = res.readInt();
		header.numSkins = res.readInt();
		header.offsetFrames = res.readInt();
		header.offsetTags = res.readInt();
		header.offsetSurfaces = res.readInt();
		header.offsetEnd = res.readInt();

		return header;
	}
}
