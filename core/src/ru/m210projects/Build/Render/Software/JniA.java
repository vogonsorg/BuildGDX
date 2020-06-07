package ru.m210projects.Build.Render.Software;

public class JniA implements A {

	public JniA(byte[][] palookup, int[] reciptable) {
		System.load("C:\\Users\\M210\\IdeaProjects\\BuildGDX\\core\\jni\\a.dll");
		
		jniinit(palookup, reciptable);
	}
	
	public native void jniinit(byte[][] palookup, int[] reciptable);

	public native void jnisetframeplace(byte[] newframeplace);

	public native void jnisetvlinebpl(int dabpl);

	public native void jnifixtransluscence(byte[] datrans);

	public native void jnisettransnormal();

	public native void jnisettransreverse();

	public native void jnidrawpixel(int ptr, byte col);

	public native void jnisethlinesizes(int logx, int logy, byte[] bufplc);

	public native void jnisetpalookupaddress(int paladdr);

	public native void jnisetuphlineasm4(int bxinc, int byinc);

	public native void jnihlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p);

	public native void jnisetupslopevlin(int logylogx, byte[] bufplc, int pinc, int bzinc);

	public native void jnislopevlin(int p, int pal, int slopaloffs, int cnt, int bx, int by, int x3, int y3,
			int[] slopalookup, int bz);

	public native void jnisetupvlineasm(int neglogy);

	public native void jnivlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	public native void jnisetupmvlineasm(int neglogy);

	public native void jnimvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	public native void jnisetuptvlineasm(int neglogy);

	public native void jnitvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p);

	public native void jnisethlineincs(int x, int y);

	public native void jnisetuphline(int pal, int shade);

	public native void jnimsethlineshift(int logx, int logy);

	public native void jnimhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p);

	public native void jnitsethlineshift(int logx, int logy);

	public native void jnithline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p);

	public native void jnisetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz);

	public native void jnispritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	public native void jnimsetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz);

	public native void jnimspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	public native void jnitsetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz);

	public native void jnitspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p);

	public native void jnisetupdrawslab(int dabpl, int pal, int shade, int trans);

	public native void jnidrawslab(int dx, int v, int dy, int vi, byte[] data, int vptr, int p);

	@Override
	public void setframeplace(byte[] newframeplace) {
		jnisetframeplace(newframeplace);
	}

	@Override
	public void setvlinebpl(int dabpl) {
		jnisetvlinebpl(dabpl);
	}

	@Override
	public void fixtransluscence(byte[] datrans) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void settransnormal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void settransreverse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawpixel(int ptr, byte col) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sethlinesizes(int logx, int logy, byte[] bufplc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setpalookupaddress(int paladdr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setuphlineasm4(int bxinc, int byinc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc, int bzinc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void slopevlin(int p, int pal, int slopaloffs, int cnt, int bx, int by, int x3, int y3, int[] slopalookup,
			int bz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupvlineasm(int neglogy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupmvlineasm(int neglogy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setuptvlineasm(int neglogy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sethlineincs(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setuphline(int pal, int shade) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msethlineshift(int logx, int logy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tsethlineshift(int logx, int logy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void thline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tsetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupdrawslab(int dabpl, int pal, int shade, int trans) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawslab(int dx, int v, int dy, int vi, byte[] data, int vptr, int p) {
		// TODO Auto-generated method stub
		
	}
}
