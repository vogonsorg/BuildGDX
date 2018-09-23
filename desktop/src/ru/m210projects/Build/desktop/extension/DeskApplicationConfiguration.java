package ru.m210projects.Build.desktop.extension;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Array;

public class DeskApplicationConfiguration extends LwjglApplicationConfiguration {

	public Array<String> iconPaths = new Array<String>();
	public Array<FileType> iconFileTypes = new Array<FileType>();
	
	public DeskGraphics.SetDisplayModeCallback setDisplayModeCallback;
	
	/** Adds a window icon. Icons are tried in the order added, the first one that works is used. Typically three icons should be
	 * provided: 128x128 (for Mac), 32x32 (for Windows and Linux), and 16x16 (for Windows). */
	public void addIcon (String path, FileType fileType) {
		iconPaths.add(path);
		iconFileTypes.add(fileType);
	}
}
