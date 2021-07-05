package ru.m210projects.Build.Render.TextureHandle;

import java.util.HashMap;

public class GLFontAtlas {

	public int atlasWidth, atlasHeight; // maximum one atlas size
	public int gridWidth, gridHeight; // maximum size of each symbol
	public int symbols;
	public GLTile[] atlas;

	public HashMap<Character, GLTile> hash;

	public GLFontAtlas(int atlasWidth, int atlasHeight, int gridWidth, int gridHeight, int symbols) {
		this.atlasWidth = atlasWidth;
		this.atlasHeight = atlasHeight;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.symbols = symbols;

		int cols = atlasWidth / gridWidth;
		int rows = atlasHeight / gridHeight;

		int symbolsPerAtlas = cols * rows;
		int atlasnum = symbols / symbolsPerAtlas;

		this.atlas = new GLTile[atlasnum];
	}

	public class GLSymbol {
		public float u, v;
		public float width, height;
		public GLTile atlasPointer;
	}
}
