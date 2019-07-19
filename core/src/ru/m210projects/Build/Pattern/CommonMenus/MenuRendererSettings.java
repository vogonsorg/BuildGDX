package ru.m210projects.Build.Pattern.CommonMenus;

import static ru.m210projects.Build.Render.GLSettings.glfiltermodes;
import static ru.m210projects.Build.Render.GLSettings.textureFilter;

import java.util.List;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuParamList;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;
import ru.m210projects.Build.Render.Renderer;
import ru.m210projects.Build.Types.ParamLinker.ConteinerItem;
import ru.m210projects.Build.Types.ParamLinker.ItemType;
import ru.m210projects.Build.Types.ParamLinker.ParamItem;
import ru.m210projects.Build.Types.ParamLinker.SwitchItem;

public abstract class MenuRendererSettings extends BuildMenu {
	
	private MenuTitle title;
	public MenuParamList list;
	public Engine engine;
	
	public BuildFont style;
	public int posx, posy, width, nHeight;
	
	public abstract MenuTitle getTitle(BuildGame app, String text);
	
	public MenuRendererSettings(final BuildGame app, int posx, int posy, int width, int nHeight, BuildFont style) {
	
		engine = app.pEngine;
		this.style = style;
		this.posx = posx;
		this.posy = posy;
		this.width = width;
		this.nHeight = nHeight;
		
		addItem(title = getTitle(app, "Renderer settings"), false);
		
		MenuItem builder = new MenuItem(null, null) {
			@Override
			public void draw(MenuHandler handler) {}

			@Override
			public boolean callback(MenuHandler handler, MenuOpt opt) {
				return false;
			}

			@Override
			public boolean mouseAction(int mx, int my) {
				return false;
			}

			@Override
			public void open() {
				build();
			}

			@Override
			public void close() {}
		};
		addItem(builder, true);
	}
	
	protected void build()
	{
		m_nItems = 0;
		Renderer ren = engine.getrender();
		title.text = (engine.getrender().getType().getName() + " settings").toCharArray();
		addItem(title, false);
		
		List<ParamItem<?>> list = ren.getList();
		int y = posy;
		for(int i = 0; i < list.size(); i++)
		{
			ParamItem<?> item = list.get(i);
			String text = list.get(i).getName();
			MenuProc callback = null;
			switch(item.getType())
			{
			case Switch:
				SwitchItem<?> sw = (SwitchItem<?>) item;
				addItem(new MenuSwitch(text, style, posx, y += nHeight, width, sw.getState(), callback, null, null), i == 0);
				break;
			case Slider:
				break;
			case Conteiner:
				final ConteinerItem<?> con = (ConteinerItem<?>) item;
				addItem(new MenuConteiner(text, style, posx, y += nHeight, width, con.title, con.getIndex(), new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						con.getVariable().set(con.getObject(item.num));
					}
				}), i == 0);
				break;
			}
		}
	}

}
