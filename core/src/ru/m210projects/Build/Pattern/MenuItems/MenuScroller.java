package ru.m210projects.Build.Pattern.MenuItems;

import static ru.m210projects.Build.Gameutils.*;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuScroller extends MenuItem {

	protected MenuList parent;
	protected int touchY;
	protected boolean isTouched;
	protected static MenuScroller touchedObj;
	
	protected SliderDrawable slider;

	protected int max;
	protected int height;
	
	public MenuScroller(SliderDrawable slider, MenuList parent, int x, int width) {
		super(null, null);
		
		this.flags = 2;
		this.parent = parent;
		this.slider = slider;
		this.x = x;
		this.y = parent.y;

		this.max = parent.len;
		this.height = parent.nListItems * parent.mFontOffset();
		this.width = width;
	}

	@Override
	public void draw(MenuHandler handler) {
		int nList = BClipLow(max - parent.nListItems, 1);
		int nRange = height - slider.getScrollerHeight();
		int posy = y + nRange * parent.l_nMin / nList;
		
		slider.drawScrollerBackground(x, y, height, 0, 0);
		
		slider.drawScroller(x, posy, 0, pal);

		if(touchedObj == this) {
			parent.l_nFocus = -1;
			parent.l_nMin = BClipRange(((touchY - y) * nList) / nRange, 0, nList);
		}
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		return m_pMenu.mNavigation(opt);
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		touchY = my;
		if(!Gdx.input.isTouched())
			touchedObj = null;

		if(mx > x && mx < x + width)
		{
			if(my > y && my < y + height) {
				if(Gdx.input.isTouched())
					touchedObj = this;
			}
		}

		return touchedObj != null;
	}

	@Override
	public void open() {
	}

	@Override
	public void close() {
	}

}
