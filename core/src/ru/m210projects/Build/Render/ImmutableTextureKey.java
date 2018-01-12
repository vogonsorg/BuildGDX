package ru.m210projects.Build.Render;

public class ImmutableTextureKey extends TextureKey {
	
	final int picnum;
    final int palnum;
    final int surfnum;
    final boolean clamped;

    ImmutableTextureKey(int picnum, int palnum, boolean clamped, int surfnum) {
        this.picnum = picnum;
        this.palnum = palnum;
        this.clamped = clamped;
        this.surfnum = surfnum;
    }

    @Override
    boolean clamped() {
        return this.clamped;
    }

    @Override
    int palnum() {
        return this.palnum;
    }

    @Override
    int picnum() {
        return this.picnum;
    }
    
    @Override
   	int surfnum() {
   		return this.surfnum;
   	}

   	@Override
   	int effects() {
   		return 0;
   	}
}
