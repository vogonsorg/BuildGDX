package ru.m210projects.Build.Audio;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class DummyMusic implements Music {

	@Override
	public void open(String name) {
	}

	@Override
	public int open(byte[] data) {
		return 0;
	}

	@Override
	public void close() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void play(boolean looping) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void setVolume(int volume) {
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
	public boolean isPlaying() {
		return false;
	}
}
