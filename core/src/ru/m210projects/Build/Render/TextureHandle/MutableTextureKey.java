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
