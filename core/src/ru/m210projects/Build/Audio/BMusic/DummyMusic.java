// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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


package ru.m210projects.Build.Audio.BMusic;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import ru.m210projects.Build.OnSceenDisplay.Console;

public class DummyMusic implements Music {

	@Override
	public void setVolume(float volume) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean init() {
		Console.Println(getName() + " initialized", OSDTEXT_GOLD);
		return true;
	}

	@Override
	public String getName() {
		return "Dummy midi synth";
	}

	@Override
	public boolean isInited() {
		return true;
	}

	@Override
	public MusicSource newMusic(byte[] data) {
		
		return null;
	}

	@Override
	public MusicSource newMusic(String name) {
		return null;
	}

	@Override
	public void update() {
	}
}