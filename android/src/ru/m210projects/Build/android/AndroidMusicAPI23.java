//This file is part of BuildGDX.
//Copyright (C) 2017-2020  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.android;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.os.Build;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Audio.Music;
import ru.m210projects.Build.Audio.MusicSource;
import ru.m210projects.Build.FileHandle.Resource;

public class AndroidMusicAPI23 implements Music {
	
	//check https://github.com/KyoSherlock/MidiDriver

	private String name = "Android Midi Module";
	private boolean inited;
	private MediaPlayer mediaPlayer;

	@Override
	public MusicSource newMusic(byte[] data) {
		try {
			return new MidiMusicSource(mediaPlayer, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public MusicSource newMusic(String name) {
		Resource res = BuildGdx.cache.open(name, 0);
		if (res == null)
			return null;

		byte[] data = res.getBytes();
		res.close();
		return newMusic(data);
	}

	@Override
	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	@Override
	public boolean init() {
		this.mediaPlayer = new MediaPlayer();
		inited = true;
		return true;
	}

	@Override
	public boolean isInited() {
		return inited;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void update() {
		/* nothing */ }

	@Override
	public void dispose() {
		mediaPlayer.release();
	}

	
	@TargetApi(Build.VERSION_CODES.M)
	public class MidiMusicSource extends MusicSource {

		private MediaPlayer mediaPlayer;

		public MidiMusicSource(MediaPlayer player, byte[] data) {
			this.mediaPlayer = player;
			this.mediaPlayer.reset();

			try {
				mediaPlayer.setDataSource(new ByteDataSource(data));
				mediaPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void play(boolean loop) {
			mediaPlayer.setLooping(loop);
			mediaPlayer.start();
		}

		@Override
		public void stop() {
			mediaPlayer.stop();
		}

		@Override
		public void pause() {
			mediaPlayer.pause();
		}

		@Override
		public void resume() {
			mediaPlayer.start();
		}

		@Override
		public void dispose() {
			/* nothing */ }

		@Override
		public void update() {
			/* nothing */ }

		@Override
		public boolean isLooping() {
			return mediaPlayer.isLooping();
		}

		@Override
		public boolean isPlaying() {
			return mediaPlayer.isPlaying();
		}
	}
}
