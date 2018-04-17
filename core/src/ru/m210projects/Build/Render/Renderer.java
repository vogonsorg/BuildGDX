package ru.m210projects.Build.Render;

import java.nio.ByteBuffer;
import java.util.HashMap;

import ru.m210projects.Build.Render.Types.FadeEffect;

public interface Renderer {
	
	public void init();
	
	public void uninit();
	
	public void drawsprite(int snum);
	
	public void drawmasks();
	
	public void drawrooms();
	
	public void clearview(int dacol);
	
	public void palfade(HashMap<String, FadeEffect> fades);
	
	public void preload();
	
	public void precache(int dapicnum, int dapalnum, int datype);
	
	public void nextpage();
	
	public void gltexapplyprops();
	
	public void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat,
            int cx1, int cy1, int cx2, int cy2);
	
	public abstract void drawoverheadmap(int cposx, int cposy, int czoom, short cang);
	
	public abstract void drawmapview(int dax, int day, int zoome, int ang);
	
	public int printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize);
	
	public int printchar(int xpos, int ypos, int col, int backcol, char ch, int fontsize);
	
	public void gltexinvalidateall(int flags);

	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth);

	public void getFrameBuffer(int x, int y, int w, int h, int format, ByteBuffer pixels);
	
	public void drawline256(int x1, int y1, int x2, int y2, int col);
	
	public void settiltang(int tilt);
	
	public void setdrunk(float intensive);
	
	public float getdrunk();
}
