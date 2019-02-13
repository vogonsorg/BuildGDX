package ru.m210projects.Build.Pattern.CommonMenus;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Audio.BuildAudio.Driver;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSlider;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;
import ru.m210projects.Build.Pattern.MenuItems.SliderDrawable;

public abstract class MenuSound extends BuildMenu {

	protected BuildFont textStyle;
	protected BuildGame app;
	
	public MenuSound(final BuildGame app, BuildFont textStyle, final SliderDrawable slider)
	{
		this.app = app;
		this.textStyle = textStyle;
		
		MenuTitle title = title("Sound setup");

		final MenuSlider sSound = new MenuSlider(slider, "Sound volume:", textStyle, 46, 90, 240, (int) (app.cfg.soundVolume * 256),
				0, 256, 16, new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuSlider slider = (MenuSlider) pItem;
						app.cfg.soundVolume = slider.value / 256.0f;
						BuildGdx.audio.setVolume(Driver.Sound, app.cfg.soundVolume);
						soundProcess();
					}
				}, false) {

				@Override
				public void open() {
					mCheckEnableItem(!app.cfg.noSound && BuildGdx.audio.IsInited(Driver.Sound));
				}
		};
		
		addItem(title, false);
		
		addItem(sSound, true);
	}
	
	public abstract MenuTitle title(String text);

	public abstract void soundProcess();
}
