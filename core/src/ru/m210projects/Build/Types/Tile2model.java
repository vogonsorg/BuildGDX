// Copyright (C) EDuke32 developers and contributors

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
