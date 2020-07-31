package ru.m210projects.Build.Script;

import static ru.m210projects.Build.Strhandler.*;

import java.util.HashMap;

import ru.m210projects.Build.CRC32;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Render.Types.Spriteext;

public class MapHackInfo {

	protected Maphack maphack;
	protected HashMap<String, String> hacklist;

	public MapHackInfo() {
		hacklist = new HashMap<String, String>();
	}

	public MapHackInfo(MapHackInfo src) {
		hacklist = new HashMap<String, String>(src.hacklist);
	}

	public boolean addMapInfo(String map, String mhkscript) {
		hacklist.put(toLowerCase(map), toLowerCase(mhkscript));
		return true;
	}

	public boolean load(String mapname) {
		unload();
		if(hasMaphack(mapname)) {
			byte[] bytes = BuildGdx.cache.getBytes(mapname, 0);
			if(bytes != null) {
				long crc32 = CRC32.getChecksum(bytes);
				if(load(mapname, crc32))
					return true;
			}
		}

		return false;
	}

	public boolean isLoaded() {
		return maphack != null;
	}

	protected boolean load(String mapname, long crc32) {
		String mhk = hacklist.get(toLowerCase(mapname));
		if(mhk != null) {
			Maphack maphack = new Maphack(mhk);
			if(maphack.getMapCRC() == crc32) {
				this.maphack = maphack;
				return true;
			}
		}

		return false;
	}

	public void load(Maphack info) {
		this.maphack = info;
	}

	public void unload() {
		this.maphack = null;
	}

	public boolean hasMaphack(String mapname) {
		return hacklist.get(toLowerCase(mapname)) != null;
	}

	public Spriteext getSpriteInfo(int spriteid) {
		if(maphack != null)
			return maphack.getSpriteInfo(spriteid);
		return null;
	}

}
