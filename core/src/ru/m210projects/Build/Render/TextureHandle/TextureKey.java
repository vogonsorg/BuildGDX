// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Kirill Klimenko-KLIMaka 
// 
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Render.TextureHandle;

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
					&& this.surfnum() == other.surfnum()
					&& this.effects() == other.effects();
		}
		return false;
    }
}
