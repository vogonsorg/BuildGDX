package ru.m210projects.Build.Render.TextureHandle;

import ru.m210projects.Build.Render.Types.GLFilter;

public class GLTileArray {

	private final GLTile[] array;

	public GLTileArray(int size) {
		array = new GLTile[size];
	}

	public GLTile get(int picnum, int palnum, boolean clamped, int surfnum) {
		for (GLTile pth = array[picnum]; pth != null && pth.palnum <= palnum; pth = pth.next) {
			if (pth.palnum == palnum && pth.isClamped() == clamped && pth.skyface == surfnum)
				return pth;
		}
		return null;
	}

	public void add(GLTile newNode, int dapicnum) {
		int p = newNode.compareTo(array[dapicnum]);
		if (p <= 0) {
			// addFirst
			newNode.next = array[dapicnum];
			array[dapicnum] = newNode;
		} else {
			GLTile prev = null;
			GLTile pth = array[dapicnum];
			do {
				if (newNode.compareTo(pth) < 0) {
					newNode.next = pth;
					if (prev != null)
						prev.next = newNode;
					return;
				}

				prev = pth;
				pth = pth.next;
			} while (pth != null);

			// addLast
			prev.next = newNode;
		}
	}

	public void dispose(int tilenum) {
		for (GLTile pth = array[tilenum]; pth != null;) {
			GLTile next = pth.next;
			pth.delete();
			pth = next;
		}
		array[tilenum] = null;
	}

	public void setFilter(int tilenum, GLFilter filter, int anisotropy) {
		for (GLTile pth = array[tilenum]; pth != null;) {
			GLTile next = pth.next;

			pth.bind();
			pth.setupTextureFilter(filter, anisotropy);
			if(!filter.retro)
				pth.setInvalidated(true);
			pth = next;
		}
	}

	public void invalidate(int tilenum) {
		for (GLTile pth = array[tilenum]; pth != null;) {
			GLTile next = pth.next;
			if (pth.hicr == null && !pth.isRequireShader())
				pth.setInvalidated(true);
			pth = next;
		}
	}
}
