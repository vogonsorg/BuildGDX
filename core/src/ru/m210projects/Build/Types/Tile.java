package ru.m210projects.Build.Types;

public class Tile {

	public enum AnimType {
		Oscil(64), Forward(128), Backward(192), None(0);

		private byte bit;

		AnimType(int bit) {
			this.bit = (byte) bit;
		}

		public int getBit() {
			return bit & 0xFF;
		}

		public boolean hasBit(int picanm) {
			return ((picanm & 192) == (bit & 0xFF));
		}
	};

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

	public Tile clear() {
		data = null;
		width = heigth = 0;
		anm = 0;

		return this;
	}

	public byte getOffsetX() {
		return (byte) ((anm >> 8) & 0xFF);
	}

	public byte getOffsetY() {
		return (byte) ((anm >> 16) & 0xFF);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return heigth;
	}

	public int getFrames() {
		return anm & 0x3F;
	}

	public int getSpeed() {
		return (anm >> 24) & 15;
	}

	public AnimType getType() {
		switch (anm & 192) {
		case 64:
			return AnimType.Oscil;
		case 128:
			return AnimType.Forward;
		case 192:
			return AnimType.Backward;
		}
		return AnimType.None;
	}
}
