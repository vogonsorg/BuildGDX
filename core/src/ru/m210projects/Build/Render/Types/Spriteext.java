/*
 * 3D models support for Polymost
 * by Jonathon Fowler
 * See the included license file "BUILDLIC.TXT" for license info.
 */

package ru.m210projects.Build.Render.Types;

public class Spriteext {
	public long mdanimtims;
	public short mdanimcur;
	public short angoff, pitch, roll;
	public int xoff, yoff, zoff;
	public short flags;
	public short xpanning, ypanning;
	public float alpha;
	
	public void clear()
	{
		mdanimtims = 0;
		mdanimcur = 0;
		angoff = 0;
		pitch = 0;
		roll = 0;
		xoff = 0;
		yoff = 0;
		zoff = 0;
		flags = 0;
		xpanning = 0;
		ypanning = 0;
		alpha = 0;
	}
}
