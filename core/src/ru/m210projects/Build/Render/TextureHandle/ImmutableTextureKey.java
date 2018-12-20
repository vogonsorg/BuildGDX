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
