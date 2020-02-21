package ru.m210projects.Build.Pattern.CommonMenus;

import java.util.List;

import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.MenuItems.BuildMenu;
import ru.m210projects.Build.Pattern.MenuItems.MenuButton;
import ru.m210projects.Build.Pattern.MenuItems.MenuConteiner;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;
import ru.m210projects.Build.Pattern.MenuItems.MenuItem;
import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Pattern.MenuItems.MenuSlider;
import ru.m210projects.Build.Pattern.MenuItems.MenuSwitch;
import ru.m210projects.Build.Pattern.MenuItems.MenuTitle;
import ru.m210projects.Build.Render.Renderer;
import ru.m210projects.Build.Render.Renderer.PixelFormat;
import ru.m210projects.Build.Types.ParamLinker.ButtonItem;
import ru.m210projects.Build.Types.ParamLinker.ConteinerItem;
import ru.m210projects.Build.Types.ParamLinker.ParamChoosableItem;
import ru.m210projects.Build.Types.ParamLinker.ParamItem;
import ru.m210projects.Build.Types.ParamLinker.SliderItem;
import ru.m210projects.Build.Types.ParamLinker.SwitchItem;

public abstract class MenuRendererSettings extends BuildMenu {
	
	private MenuTitle title;
	private Renderer currentRenderer;
	private PixelFormat currentFormat;
	public BuildGame app;
	
	public BuildFont style;
	public int posx, posy, width, nHeight;
	public boolean fontShadow = false;
	public boolean listShadow = false;
	
	public abstract MenuTitle getTitle(BuildGame app, String text);
	
	public MenuRendererSettings(final BuildGame app, int posx, int posy, int width, int nHeight, BuildFont style) {
	
		this.app = app;
		this.style = style;
		this.posx = posx;
		this.posy = posy;
		this.width = width;
		this.nHeight = nHeight;
		
		addItem(title = getTitle(app, "Renderer settings"), false);
	}
	
	public void mDraw(MenuHandler handler)
	{
		if(currentFormat != app.pEngine.getrender().getTexFormat())
			rebuild();
		super.mDraw(handler);
	}
	
	public boolean mLoadRes(MenuHandler handler, MenuOpt opt)
	{
		if(opt == MenuOpt.Open && (currentRenderer != app.pEngine.getrender() || currentFormat != app.pEngine.getrender().getTexFormat()))
			rebuild();
		return super.mLoadRes(handler, opt);
	}
	
	protected void rebuild()
	{
		m_nItems = 0;

		currentRenderer = app.pEngine.getrender();
		currentFormat = app.pEngine.getrender().getTexFormat();
		if(title != null)
			title.text = (app.pEngine.getrender().getType().getName() + " settings").toCharArray();
		
		addItem(title, false);
		
		List<ParamItem<?>> list = currentRenderer.getParamList();
		int y = posy;
		for(int i = 0; i < list.size(); i++)
		{
			ParamItem<?> item = list.get(i);
			
			if(!item.checkItem())
				continue;
			
			String text = null;
			if(item instanceof ParamChoosableItem)
				 text = ((ParamChoosableItem<?>) list.get(i)).getName();
			
			switch(item.getType())
			{
			case Separator:
				y += nHeight / 2;
				break;
			case Switch:
				final SwitchItem<?> sw = (SwitchItem<?>) item;
				MenuSwitch obj = new MenuSwitch(text, style, posx, y += nHeight, width, sw.getState(), new MenuProc() {
						@Override
						public void run(MenuHandler handler, MenuItem pItem) {
							MenuSwitch ss = (MenuSwitch) pItem;
							sw.setState(ss.value);
						}
					}, null, null) {
					@Override
					public void draw(MenuHandler handler) {
						this.value = sw.getState();
						super.draw(handler);
					}
				};
				obj.fontShadow = fontShadow;
				addItem(obj, i == 0);
				break;
			case Slider:
				final SliderItem<?> si = (SliderItem<?>) item;
				MenuSlider slider = new MenuSlider(app.pSlider, text, style, posx, y += nHeight, width, si.getValue(),
					si.getMin(), si.getMax(), si.getStep(), new MenuProc() {
						@Override
						public void run(MenuHandler handler, MenuItem pItem) {
							MenuSlider slider = (MenuSlider) pItem;
							if(!si.setValue(slider.value))
								slider.value = si.getValue();
						}
					}, true) {
					
					@Override
					public void draw(MenuHandler handler) {
						this.value = si.getValue();
						super.draw(handler);
					}
				};
				
				if(si.getDigitalMax() != null)
					slider.digitalMax = si.getDigitalMax();
				
				slider.fontShadow = fontShadow;
				addItem(slider, i == 0);
				break;
			case Conteiner:
				final ConteinerItem<?> con = (ConteinerItem<?>) item;
				
				MenuConteiner conteiner = new MenuConteiner(text, style, posx, y += nHeight, width, con.title, con.getIndex(), new MenuProc() {
					@Override
					public void run(MenuHandler handler, MenuItem pItem) {
						MenuConteiner item = (MenuConteiner) pItem;
						con.getVariable().set(con.getObject(item.num));
					}
				} ) {
					@Override
					public void draw(MenuHandler handler) {
						this.num = con.getIndex();
						super.draw(handler);
					}
				};
				conteiner.fontShadow = fontShadow;
				conteiner.listShadow = listShadow;
				addItem(conteiner, i == 0);
				break;
			case Button:
				final ButtonItem<?> b = (ButtonItem<?>) item;
				MenuButton but = new MenuButton(text, style, posx, y += nHeight, width, 0, 0, null, 0, b.getCallback(), 0);
				but.fontShadow = fontShadow;
				addItem(but, i == 0);
				break;
			}
		}
	}

}
