/*
* High-colour textures support for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
*/

package ru.m210projects.Build.Render.Types;

public class Hicreplctyp {
	public Hicreplctyp next;
	public String filename;
	public Hicskybox skybox;
    public int palnum, ignore, flags, filler;
    public float alphacut, xscale, yscale, specpower, specfactor;
}
