/*
* High-colour textures support for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*/

package ru.m210projects.Build.Render.TextureHandle;

public class Hicreplctyp {
	
	public static class Hicskybox {
		public int ignore;
	    public final String[] face;
	    
	    public Hicskybox()
	    {
	    	face = new String[6];
	    }
	}

	public String filename;
	public Hicskybox skybox;
    public int palnum, ignore, flags;
    public float alphacut, xscale, yscale, specpower, specfactor;
    public Hicreplctyp next;
    
    public Hicreplctyp(Hicreplctyp src)
    {
    	this.filename = src.filename;
    	this.skybox = src.skybox;
    	this.palnum = src.palnum;
    	this.ignore = src.ignore;
    	this.flags = src.flags;
    	this.alphacut = src.alphacut;
    	this.xscale = src.xscale;
    	this.yscale = src.yscale;
    	this.specpower = src.specpower;
    	this.specfactor = src.specfactor;
    	this.next = src.next;
    }
    
    public Hicreplctyp(int palnum)
    {
    	this.palnum = palnum;
    }
    
    public Hicreplctyp(int palnum, boolean skybox)
    {
    	this.palnum = palnum;
    	this.skybox = new Hicskybox();
    }
}
