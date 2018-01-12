package ru.m210projects.Build.Render;

public class MutableTextureKey extends TextureKey {
	
	private int picnum;
    private int palnum;
    private int surfnum;
    private boolean clamped;

    MutableTextureKey() {
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

    /**
     * Factory method for immutable keys.
     */
    public TextureKey toImmutable() {
        return new ImmutableTextureKey(picnum, palnum, clamped, surfnum);
    }

    public MutableTextureKey picnum(int i) {
        this.picnum = i;
        return this;
    }

    public MutableTextureKey palnum(int i) {
        this.palnum = i;
        return this;
    }

    public MutableTextureKey clamped(boolean b) {
        this.clamped = b;
        return this;
    }
    
    public MutableTextureKey surfnum(int i) {
        this.surfnum = i;
        return this;
    }
}
