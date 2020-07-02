package ru.m210projects.Build.Types;

public class Tile {

	public int width, heigth;
	public int anm;
	public byte[] data;

	public int getSize() {
		return width * heigth;
	}

	public Tile allocate(int xsiz, int ysiz) {
		int dasiz = xsiz * ysiz;

		data = new byte[dasiz];
		width = xsiz;
		heigth = ysiz;
		anm = 0;

		return this;
	}

	public byte getXOffset() {
		return ((byte) ((anm >> 8) & 0xFF));
	}

	public byte getYOffset() {
		return  ((byte) ((anm >> 16) & 0xFF));
	}
}
