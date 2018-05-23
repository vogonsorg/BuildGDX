/*
 * TextureKey by Kirill Klimenko-KLIMaka 
 * Based on parts of "Polymost" by Ken Silverman
 * 
 * Ken Silverman's official web site: http://www.advsys.net/ken
 * See the included license file "BUILDLIC.TXT" for license info.
 */

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
