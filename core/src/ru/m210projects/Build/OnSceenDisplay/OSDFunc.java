package ru.m210projects.Build.OnSceenDisplay;

public interface OSDFunc {
	public void drawchar(int x, int y, char ch, int shade, int pal, int scale);
	public void drawosdstr(int x, int y, int ptr, int len, int shade, int pal, int scale);
	public void drawstr(int x, int y, char[] text, int len, int shade, int pal, int scale);
	public void drawcursor(int x, int y, int type, int lastkeypress, int scale);
	public void clearbg(int col, int row);
	public void showosd(int shown);
	public int gettime();
	public long getticksfunc();
	public int getcolumnwidth(int width);
	public int getrowheight(int height);
	public boolean textHandler(String text);
}
