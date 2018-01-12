package ru.m210projects.Build.Loader;


public class MDModel extends Model {
	public MDSkinmap skinmap;
	public int numskins, skinloaded; // set to 1+numofskin when a skin is loaded and the tex coords are modified,
	
	public int numframes, cframe, nframe, fpssc;
	public boolean usesalpha;
    public float oldtime, curtime, interpol;
    public MDAnimation animations; 
}
