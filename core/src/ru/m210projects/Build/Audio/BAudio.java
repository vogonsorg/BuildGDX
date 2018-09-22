//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Audio;

import java.nio.ByteBuffer;
import com.badlogic.gdx.utils.Disposable;

import ru.m210projects.Build.Audio.BMusic.DummyMusic;
import ru.m210projects.Build.Audio.BMusic.Music;
import ru.m210projects.Build.Audio.BMusic.MusicSource;

public class BAudio implements Disposable {
	
	public static final int MUSICDRV = 1;
	public static final int SOUNDDRV = 2;
	public static final int MIDITYPE = 3;
	public static final int DIGITYPE = 4;
	
	private Sound fx;
	private Music mx;
	public BAudio(Sound fx, Music mx) {
		setDriver(SOUNDDRV, fx);
		setDriver(MUSICDRV, mx);
	}

	public boolean IsInited(int type)
	{
		if(type == SOUNDDRV) 
			return fx.isInited();
		if(type == MUSICDRV) 
			return mx.isInited();
		
		return false;
	}
	
	public void setDriver(int type, Object drv)
	{
		if(type == SOUNDDRV) 
		{
			if(this.fx != null) {
				this.fx.stopAllSounds();	
				this.fx.uninit();
			}
			if(drv == null) drv = new DummySound();
			if(drv instanceof Sound)
				this.fx = (Sound) drv;
		}
		
		if(type == MUSICDRV) 
		{
			if(this.mx != null) 
				this.mx.dispose();
			if(drv == null) drv = new DummyMusic();
			if(drv instanceof Music)
				this.mx = (Music) drv;
		}
	}

	public void setVolume(int type, float volume)
	{
		switch(type)
		{
		case SOUNDDRV:
			if(fx.isInited()) 
				fx.setVolume(volume);
			break;
		case MUSICDRV:
			if(fx.getDigitalMusic() != null && fx.getDigitalMusic().isInited()) 
				fx.getDigitalMusic().setVolume(volume);
			if(mx.isInited()) 
				mx.setVolume(volume);
			break;
		}
	}
	
	public void dispose() {
		fx.destroy();
		mx.dispose();
	}
	
	public void update()
	{
		if(fx.isInited()) 
			fx.update();
		if(mx.isInited()) 
			mx.update();
	}
	
	public Sound getSound()
	{
		return fx;
	}
	
	public Music getMusic()
	{
		return mx;
	}
	
	public String getName(int type) {
		switch(type)
		{
		case SOUNDDRV:
			return fx.getName();
		case MUSICDRV:
			return mx.getName();
		}
		
		return null;
	}

	public Source newSound(ByteBuffer data, int rate, int bits, int priority)
	{
		if(priority == 0) priority = 1;
		return fx.newSound(data, rate, bits, priority);
	}
	
	public MusicSource newMusic(int type, String file) {
		if(type == MIDITYPE && mx.isInited()) 
			return mx.newMusic(file);
		if(type == DIGITYPE && fx.getDigitalMusic() != null && fx.getDigitalMusic().isInited())
			return fx.getDigitalMusic().newMusic(file);
		
		return null;
	}
	
	public MusicSource newMusic(int type, byte[] data) {
		if(type == MIDITYPE && mx.isInited()) 
			return mx.newMusic(data);
		if(type == DIGITYPE && fx.getDigitalMusic() != null && fx.getDigitalMusic().isInited())
			return fx.getDigitalMusic().newMusic(data);
		
		return null;
	}
}
