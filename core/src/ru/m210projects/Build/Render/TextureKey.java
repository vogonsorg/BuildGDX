package ru.m210projects.Build.Render;

public abstract class TextureKey {

    abstract int picnum();

    abstract int palnum();
    
    abstract int surfnum();
    
    abstract int effects();

    abstract boolean clamped();

    @Override
    public int hashCode() {
        return (this.clamped() ? 31 : 0) ^ this.picnum() ^ this.palnum() ^ this.surfnum();
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof TextureKey) {
			TextureKey other = (TextureKey) obj;
			return this.picnum() == other.picnum()
					&& this.palnum() == other.palnum()
					&& this.clamped() == other.clamped()
					&& this.surfnum() == other.surfnum();
					// ++  == effects
		}
		return false;
    }
}
