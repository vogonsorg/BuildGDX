/*
* MDModel for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been ported to Java by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.Engine.MAXPALOOKUPS;

import ru.m210projects.Build.Types.SPRITE;

public abstract class MDModel extends Model {
	public MDSkinmap skinmap;
	public int numskins, skinloaded; // set to 1+numofskin when a skin is loaded and the tex coords are modified,
	
	public int numframes, cframe, nframe, fpssc;
	public boolean usesalpha;
    public float oldtime, curtime, interpol;
    public MDAnimation animations; 
    
    public abstract int getFrameIndex(String framename);
    
    public abstract void updateanimation(SPRITE tspr);
    
    public MDSkinmap getSkin(int palnum, int skinnum, int surfnum) {
    	for (MDSkinmap sk = skinmap; sk != null; sk = sk.next)
	        if (sk.palette == palnum && skinnum == sk.skinnum && surfnum == sk.surfnum)
	            return sk;

		return null;
	}
    
    private void addSkin(MDSkinmap sk) {
    	sk.next = skinmap;
    	skinmap = sk;
	}
    
    public int setSkin(String skinfn, int palnum, int skinnum, int surfnum, double param, double specpower, double specfactor)
	{
		if (skinfn == null) return -2;
	    if (palnum >= MAXPALOOKUPS) return -3;

	    if (mdnum == 2) surfnum = 0;
	    
	    MDSkinmap sk = getSkin(palnum, skinnum, surfnum);
	    if(sk == null)  // no replacement yet defined
	    	addSkin(sk = new MDSkinmap());

	    sk.palette = palnum;
	    sk.skinnum = skinnum;
	    sk.surfnum = surfnum;
	    sk.param = (float) param;
	    sk.specpower = (float) specpower;
	    sk.specfactor = (float) specfactor;
	    sk.fn = skinfn;
	    
	    return 0;
	}
    
    public int setAnimation(String framestart, String frameend, int fpssc, int flags)
	{
		MDAnimation ma = new MDAnimation();
	    int i = 0;

	    //find index of start frame
	    i = getFrameIndex(framestart);
	    if (i == numframes) return -2;
	    ma.startframe = i;

	    //find index of finish frame which must trail start frame
	    i = getFrameIndex(frameend);
	    if (i == numframes) return -3;
	    ma.endframe = i;

	    ma.fpssc = fpssc;
	    ma.flags = flags;

	    ma.next = animations;
	    animations = ma;

	    return 0;
	}
}
