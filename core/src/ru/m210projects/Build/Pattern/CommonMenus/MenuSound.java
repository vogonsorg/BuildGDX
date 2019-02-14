package ru.m210projects.Build.Pattern.CommonMenus;

import java.util.ArrayList;
import java.util.List;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Audio.BuildAudio.Driver;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSlider;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;

public abstract class MenuSound extends BuildMenu {

	protected BuildFont textStyle;
	protected BuildGame app;
	
	public int snddriver;
	public int middriver;
	public int resampler;
	public int osnddriver;
	public int omiddriver;
	public int oresampler;
	public int voices;
	public int ovoices;
	public int cdaudio;
	public int ocdaudio;
	
	public MenuSound(final BuildGame app,  int posx, int posy, int width, int menuHeight, int separatorHeight, BuildFont menuItems, BuildFont drvStyle, BuildFont applyButton)
	{
		this.app = app;
		this.textStyle = menuItems;
		
		addItem(title("Sound setup"), false);

		final MenuConteiner sSoundDrv = new MenuConteiner("Sound driver:", menuItems, drvStyle, posx, posy += menuHeight, width, null, 0,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						snddriver = item.num;
					}
				}) {
			@Override
			public void open() {
				if (this.list == null) {
					List<String> names = new ArrayList<String>();
					BuildGdx.audio.getDeviceslList(Driver.Sound, names);
					this.list = new char[names.size()][];
					for (int i = 0; i < list.length; i++)
						this.list[i] = names.get(i).toCharArray();
				}
				num = snddriver = osnddriver = app.cfg.snddrv;
				if (BuildGdx.audio.IsInited(Driver.Sound))
					list[num] = BuildGdx.audio.getSound().getName().toCharArray();
				else
					list[num] = "initialization failed".toCharArray();
			}
		};
		
		final MenuConteiner sMusicDrv = new MenuConteiner("Midi driver:", menuItems, drvStyle, posx, posy += menuHeight, width, null, 0,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						middriver = item.num;
					}
				}) {
			@Override
			public void open() {
				if (this.list == null) {
					List<String> names = new ArrayList<String>();
					BuildGdx.audio.getDeviceslList(Driver.Music, names);
					this.list = new char[names.size()][];
					for (int i = 0; i < list.length; i++)
						this.list[i] = names.get(i).toCharArray();
				}
				num = middriver = omiddriver = app.cfg.middrv;
				if (BuildGdx.audio.IsInited(Driver.Music)) {
					list[num] = BuildGdx.audio.getMusic().getName().toCharArray();
				} else
					list[num] = "initialization failed".toCharArray();
			}
		};
		
		final MenuConteiner sResampler = new MenuConteiner("Resampler:", menuItems, posx, posy += menuHeight, width, null, 0,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						resampler = item.num;
					}
				}) {
			@Override
			public void open() {
				if(this.list == null) {
					this.list = new char[BuildGdx.audio.getSound().getNumResamplers()][];
					for (int i = 0; i < list.length; i++)
						this.list[i] = BuildGdx.audio.getSound().getSoftResamplerName(i).toCharArray();
				}
				if(app.cfg.resampler_num < 0 || app.cfg.resampler_num >= BuildGdx.audio.getSound().getNumResamplers())
					app.cfg.resampler_num = 0;
				num = resampler = oresampler = app.cfg.resampler_num;
			}
		};

		posy += separatorHeight;
		int oposy = posy;
		posy += menuHeight;

		final MenuSlider sSound = new MenuSlider(app.slider, "Sound volume:", menuItems, posx, posy += menuHeight, width, (int) (app.cfg.soundVolume * 256),
				0, 256, 16, new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						app.cfg.soundVolume = slider.value / 256.0f;
						BuildGdx.audio.setVolume(Driver.Sound, app.cfg.soundVolume);
						soundVolumeChange();
					}
				}, false) {

				@Override
				public void open() {
					mCheckEnableItem(!app.cfg.noSound && BuildGdx.audio.IsInited(Driver.Sound));
				}
		};
		
		final MenuSlider sVoices = new MenuSlider(app.slider, "Voices:", menuItems, posx, posy += menuHeight, width, 0, 8, 256, 8, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSlider slider = (MenuSlider) pItem;
				voices = slider.value;
			}
		}, true) {
			@Override
			public void open() {
				value = voices = ovoices = app.cfg.maxvoices;
				mCheckEnableItem(!app.cfg.noSound && BuildGdx.audio.IsInited(Driver.Sound));
			}
		};
		
		MenuSwitch sSoundSwitch = new MenuSwitch("Sound:", menuItems, posx, oposy += menuHeight, width, !app.cfg.noSound, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				
				app.cfg.noSound = !sw.value;
				sSound.mCheckEnableItem(!app.cfg.noSound);
				sVoices.mCheckEnableItem(!app.cfg.noSound);
				if(sw.value) {
					soundOn();
				} else {
					soundOff();
				}
			}
		}, null, null);
		
		posy += separatorHeight;
		oposy = posy;
		posy += menuHeight;
		
		final MenuSlider sMusic = new MenuSlider(app.slider, "Music volume:", menuItems, posx, posy += menuHeight, width, (int) (app.cfg.musicVolume * 256), 0, 256, 8,
				new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						app.cfg.musicVolume = slider.value / 256.0f;
						BuildGdx.audio.setVolume(Driver.Music, app.cfg.musicVolume);				
					}
				}, false) {
			@Override
			public void open() {
				mCheckEnableItem(!app.cfg.muteMusic && BuildGdx.audio.IsInited(Driver.Music));
			}
		};
		
		MenuSwitch sMusicSwitch = new MenuSwitch("Music:", menuItems, posx, oposy += menuHeight, width, !app.cfg.muteMusic, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuSwitch sw = (MenuSwitch) pItem;
				app.cfg.muteMusic = !sw.value;
				if (app.cfg.muteMusic)
					BuildGdx.audio.setVolume(Driver.Music, 0);	
				else
					BuildGdx.audio.setVolume(Driver.Music, app.cfg.musicVolume);
				
				sMusic.mCheckEnableItem(!app.cfg.muteMusic);
			}
		}, null, null);

		MenuConteiner sMusicType = new MenuConteiner("Music type:", menuItems, posx, posy += menuHeight, width, null, 0, new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				MenuConteiner item = (MenuConteiner) pItem;
				cdaudio = item.num;
			}
		}) {
			@Override
			public void open() {
				if (this.list == null) {
					this.list = new char[3][];
					this.list[0] = "midi".toCharArray();
					this.list[1] = "external".toCharArray();
					this.list[2] = "cd audio".toCharArray();
				}
				cdaudio = ocdaudio = num = app.cfg.musicType;
			}
			
			@Override
			public void draw(MenuHandler handler) {
				super.draw(handler);
				mCheckEnableItem(!app.cfg.muteMusic);
			}
		};
		
		MenuProc callback = new MenuProc() {
			@Override
			public void run(MenuHandler handler, MenuItem pItem) {
				if (snddriver != osnddriver || voices != ovoices || resampler != oresampler) {
					
					soundPreDrvChange();
					
					int olddrv = BuildGdx.audio.getDriver(Driver.Sound);
					if (snddriver != osnddriver)
						BuildGdx.audio.setDriver(Driver.Sound, snddriver);
					if (voices != ovoices)
						app.cfg.maxvoices = voices;
					if(resampler != oresampler)
						app.cfg.resampler_num = resampler;

					if (soundRestart(app.cfg.maxvoices, app.cfg.resampler_num)) {
						app.cfg.snddrv = osnddriver = snddriver;
						
						sSoundDrv.list[sSoundDrv.num] = BuildGdx.audio.getSound().getName().toCharArray();
						ovoices = voices;
						oresampler = resampler;
						
						sResampler.list = new char[BuildGdx.audio.getSound().getNumResamplers()][];
						for (int i = 0; i < sResampler.list.length; i++)
							sResampler.list[i] = BuildGdx.audio.getSound().getSoftResamplerName(i).toCharArray();
						if(app.cfg.resampler_num < 0 || app.cfg.resampler_num >= BuildGdx.audio.getSound().getNumResamplers())
							app.cfg.resampler_num = 0;
						sResampler.num = resampler = oresampler = app.cfg.resampler_num;
					
					} else {
						sSoundDrv.list[sSoundDrv.num] = "initialization failed".toCharArray();
						BuildGdx.audio.setDriver(Driver.Sound, olddrv);
					}
				}

				if (middriver != omiddriver) {
					int olddrv = BuildGdx.audio.getDriver(Driver.Music);
					BuildGdx.audio.setDriver(Driver.Music, middriver);
					if (musicRestart()) {
						app.cfg.middrv = omiddriver = middriver;
						sMusicDrv.list[sMusicDrv.num] = BuildGdx.audio.getMusic().getName().toCharArray();
					} else {
						sMusicDrv.list[sMusicDrv.num] = "initialization failed".toCharArray();
						BuildGdx.audio.setDriver(Driver.Music, olddrv);
					}
				}

				if (cdaudio != ocdaudio) {
					app.cfg.musicType = cdaudio;
					ocdaudio = cdaudio;
				}

				soundPostDrvChange();
			}
		};
		
		posy += 2 * separatorHeight;
		MenuButton mApplyChanges = new MenuButton("Apply changes", applyButton, 0, posy, 320, 1, 0, null, -1, callback, 0) {
			@Override
			public void draw(MenuHandler handler) {
				super.draw(handler);
				mCheckEnableItem(snddriver != osnddriver || middriver != omiddriver || resampler != oresampler || voices != ovoices || cdaudio != ocdaudio);
			}
		};
		
		addItem(sSoundDrv, true);
		addItem(sMusicDrv, false);
		addItem(sResampler, false);
		addItem(sSoundSwitch, false);
		addItem(sSound, false);
		addItem(sVoices, false);
		addItem(sMusicSwitch, false);
		addItem(sMusic, false);
		addItem(sMusicType, false);
		addItem(mApplyChanges, false);
	}
	
	public abstract MenuTitle title(String text);
	
	public abstract void soundPreDrvChange();
	
	public abstract void soundPostDrvChange();
	
	public abstract boolean soundRestart(int voices, int resampler);
	
	public abstract boolean musicRestart();

	public abstract void soundVolumeChange();
	
	public abstract void soundOn();
	
	public abstract void soundOff();
}
