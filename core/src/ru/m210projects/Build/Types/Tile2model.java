/*
* Tile2model for Polymost
* by Jonathon Fowler
* See the included license file "BUILDLIC.TXT" for license info.
* 
* This file has been modified
* by the EDuke32 team (development@voidpoint.com)
* by Alexander Makarov-[M210] (m210-2007@mail.ru)
*/

package ru.m210projects.Build.Types;

public class Tile2model {
	// maps build tiles to particular animation frames of a model
    public int     modelid = -1;
    public int     skinnum;
    public int     framenum;   // calculate the number from the name when declaring
    public float   smoothduration;
    public int     next = -1;
    public int     pal;
}
