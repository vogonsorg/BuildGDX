package ru.m210projects.Build.Render.Software;

public interface A {
	
	public void setframeplace(byte[] newframeplace);
	
	public byte[] getframeplace();
	
	public void clearframe(byte col);

	// Global variable functions
	public void setvlinebpl(int dabpl);

	public void fixtransluscence(byte[] datrans);

	public void settransnormal();

	public void settransreverse();
	
	public void drawpixel(int ptr, byte col);

	// Ceiling/floor horizontal line functions

	public void sethlinesizes(int logx, int logy, byte[] bufplc);

	public void setpalookupaddress(byte[] paladdr);

	public void setuphlineasm4(int bxinc, int byinc);

	public void hlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p);

	// Sloped ceiling/floor vertical line functions

	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc, int bzinc);

	public void slopevlin(int p, byte[] pal, int slopaloffs, int cnt, int bx, int by, int x3, int y3, int[] slopalookup, int bz);

	// Wall,face sprite/wall sprite vertical line functions

	public void setupvlineasm(int neglogy);

	public void vlineasm1(int vinc, byte[] pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	public void setupmvlineasm(int neglogy);

	public void mvlineasm1(int vinc, byte[] pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	public void setuptvlineasm(int neglogy);

	public void tvlineasm1(int vinc, byte[] pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	// Floor sprite horizontal line functions

	public void sethlineincs(int x, int y);

	public void setuphline(byte[] pal, int shade);

	public void msethlineshift(int logx, int logy);

	public void mhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p);

	public void tsethlineshift(int logx, int logy);

	public void thline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p);

	// Rotatesprite vertical line functions

	public void setupspritevline(byte[] pal, int shade, int bxinc, int byinc, int ysiz);

	public void spritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	public void msetupspritevline(byte[] pal, int shade, int bxinc, int byinc, int ysiz);

	public void mspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	public void tsetupspritevline(byte[] pal, int shade, int bxinc, int byinc, int ysiz);

	public void tspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	// Voxel functions
	
	public void setupdrawslab(int dabpl, byte[] pal, int shade, int trans);

	public void drawslab(int dx, int v, int dy, int vi, byte[] data, int vptr, int p);

}
