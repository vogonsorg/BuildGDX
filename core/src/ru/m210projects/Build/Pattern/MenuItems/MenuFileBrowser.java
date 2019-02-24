package ru.m210projects.Build.Pattern.MenuItems;

import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Strhandler.*;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Net.Mmulti.*;
import static ru.m210projects.Build.FileHandle.Compat.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.FileHandle.FileEntry;
import ru.m210projects.Build.Gameutils.ConvertType;
import ru.m210projects.Build.Pattern.BuildEngine;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;
import ru.m210projects.Build.FileHandle.DirectoryEntry;

public class MenuFileBrowser extends MenuItem {

	private final int DIRECTORY = 0;
	private final int FILE = 1;
	private String back = "..";
	private char[] dirs = "Directories".toCharArray();
	private char[] ffs = "Files".toCharArray();
	private class StringList extends LinkedList<String> { private static final long serialVersionUID = 1L; }

	private int touchY;
	private int[] scrollX = new int[2];
	public boolean scrollTouch[] = new boolean[2];
	public boolean showmain;
	
	int[] l_nMin;
	int[] l_nFocus;
	final int nListItems;
	final MenuProc specialCall;
	final int nItemHeight;
	
	DirectoryEntry currDir;
	public FileEntry currFile;

	StringList[] list = new StringList[2];
	
	String path;
	int currColumn;
	
	private BuildEngine draw;
	private SliderDrawable slider;
	private int nBackground = 0;
	private int scrollerHeight;
	
	public MenuFileBrowser(BuildEngine draw, SliderDrawable slider, BuildFont font, int x, int y, int width,
			int nItemHeight, MenuProc specialCall,
			int nListItems) {
		
		super(null, font);
		
		this.flags = 3 | 4;
		this.draw = draw;
		this.slider = slider;

		this.x = x;
		this.y = y;
		this.width = width;
		this.nItemHeight = nItemHeight;
		this.nListItems = nListItems;
		this.specialCall = specialCall;
		
		this.l_nMin = new int[2];
		this.l_nFocus = new int[2];
		this.currColumn = FILE;

		changeDir(cache);
	}
	
	public int mFontOffset() {
		return font.nHeight + nItemHeight;
	}

	List<String> tmpList;
	private void changeDir(DirectoryEntry dir)
	{
		if(list[DIRECTORY] == null)
			list[DIRECTORY] = new StringList();
		else list[DIRECTORY].clear();
		
		if(list[FILE] == null)
			list[FILE] = new StringList();
		else list[FILE].clear();
		
		if(tmpList == null)
			tmpList = new ArrayList<String>();
		else tmpList.clear();
		
		if(currDir == dir)
			return;

		if(dir.getParent() != null)
			list[DIRECTORY].add(back);
		
		for (Iterator<DirectoryEntry> it = dir.getDirectories().values().iterator(); it.hasNext(); ) {
			DirectoryEntry sdir = it.next();
			if(!sdir.getName().equals("<userdir>")) 
				tmpList.add(toLowerCase(sdir.getName()));
		}
		
		Collections.sort(tmpList);
		list[DIRECTORY].addAll(tmpList);
		tmpList.clear();
		
		for (Iterator<FileEntry> it = dir.getFiles().values().iterator(); it.hasNext(); ) {
			FileEntry file = it.next();
			String name = file.getFile().getName();
			if(file.getExtension().equals("map"))
				tmpList.add(toLowerCase(name));
		}
		
		Collections.sort(tmpList);
		list[FILE].addAll(tmpList);
		tmpList.clear();

		currDir = dir;
		path = File.separator;
		if(dir.getRelativePath() != null)
			path += currDir.getRelativePath();
	}

	@Override
	public void draw(MenuHandler handler) {
		
		int yColNames = y + 3;
		int yPath = yColNames + font.nHeight + 2;
		int yList = yPath + font.nHeight + 2;
		int scrollerWidth = slider.getScrollerWidth();
	
		nBackground = 321; //XXX
		draw.rotatesprite(x << 16, y << 16, 65536, 0, nBackground, 128, 0, 10 | 16 | 1, 0, 0, coordsConvertXScaled(x+width, ConvertType.Normal), coordsConvertYScaled(yList + nListItems * mFontOffset() + 6));

		int px = x + 3;
		/*directories*/ font.drawText(px, yColNames, dirs, -32, 10, TextAlign.Left, 0, false); //pal = 10 XXX
		/*files*/ font.drawText(x - 3 + width - font.getWidth(ffs), yColNames, ffs, -32, 10, TextAlign.Left, 0, false); //pal = 10 XXX
		
		px += scrollerWidth;
		
		/*path*/ brDrawText(font, toCharArray("path: " + path), px, yPath, -32, 7, 0, this.x + this.width); //XXX font0

		int py = yList;
		for(int i = l_nMin[DIRECTORY]; i >= 0 && i < l_nMin[DIRECTORY] + nListItems && i < list[DIRECTORY].size(); i++) {	
			int pal = 0;
			int shade = 16;
			if ( currColumn == DIRECTORY && i == l_nFocus[DIRECTORY] ) {
				if(m_pMenu.mGetFocusedItem(this))
					shade = 16 - (totalclock & 0x3F);
				else shade = 0;
			}
			text = toCharArray(list[DIRECTORY].get(i));
			if(list[DIRECTORY].get(i).equals(back))
				pal = 7; //XXX
			brDrawText(font, text, px + 3, py, shade, pal, 0, this.x + this.width / 2 - 4); //XXX font0
			py += mFontOffset();
		}
		
		py = yList;
		for(int i = l_nMin[FILE]; i >= 0 && i < l_nMin[FILE] + nListItems && i < list[FILE].size(); i++) {	
			int pal = 0;
			int shade = 16;
			if ( currColumn == FILE && i == l_nFocus[FILE] ) {
				if(m_pMenu.mGetFocusedItem(this))
					shade = 16 - (totalclock & 0x3F);
				else shade = 0;
			}

			String filename = list[FILE].get(i);
			text = toCharArray(filename);
			
	        px = x + width - font.getWidth(text) - scrollerWidth - 5; 
	        brDrawText(font, text, px, py, shade, pal, this.x + this.width / 2 + 4, this.x + this.width); // XXX font 0
			py += mFontOffset();
		}
		
		scrollerHeight = nListItems * mFontOffset();

		//Files scroll
		int nList = BClipLow(list[FILE].size() - nListItems, 1);
		int posy = yList + (scrollerHeight - slider.getScrollerHeight()) * l_nMin[FILE] / nList;
		
		scrollX[FILE] = x + width - scrollerWidth - 1;
		slider.drawScrollerBackground(scrollX[FILE], yList, scrollerHeight, 0, 0);
		slider.drawScroller(scrollX[FILE], posy, handler.getShade(currColumn == FILE ? m_pMenu.m_pItems[m_pMenu.m_nFocus] : null), 0);
		
		//Directory scroll
		nList = BClipLow(list[DIRECTORY].size() - nListItems, 1);
		posy = yList + (scrollerHeight - slider.getScrollerHeight()) * l_nMin[DIRECTORY] / nList;
		
		scrollX[DIRECTORY] = x + 2;
		slider.drawScrollerBackground(scrollX[DIRECTORY], yList, scrollerHeight, 0, 0);
		slider.drawScroller(scrollX[DIRECTORY], posy, handler.getShade(currColumn == DIRECTORY ? m_pMenu.m_pItems[m_pMenu.m_nFocus] : null), 0);
	}
	
	private void brDrawText( BuildFont font, char[] text, int x, int y, int shade, int pal, int x1, int x2 )
	{
		int tptr = 0, tx = 0;
	    while(tptr < text.length && text[tptr] != 0)
	    {
        	if(tx + x > x1 && tx + x <= x2) 
        		x += font.drawChar(x, y, text[tptr], shade, pal, 0, false);
        	else break;

	        tptr++;
	    }
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		switch(opt)
		{
			case MWUP:
				if(l_nMin[currColumn] > 0)
					l_nMin[currColumn]--;
				return false;
			case MWDW:
				if(l_nMin[currColumn] < list[currColumn].size() - nListItems)
					l_nMin[currColumn]++;
				return false;
			case UP:
				l_nFocus[currColumn]--;
				if(l_nFocus[currColumn] >= 0 && l_nFocus[currColumn] < l_nMin[currColumn])
					if(l_nMin[currColumn] > 0) l_nMin[currColumn]--;
				if(l_nFocus[currColumn] < 0) {
					l_nFocus[currColumn] = list[currColumn].size() - 1;
					l_nMin[currColumn] = list[currColumn].size() - nListItems;
					if(l_nMin[currColumn] < 0) l_nMin[currColumn] = 0;
				}
				return false;
			case DW:
				l_nFocus[currColumn]++;
				if(l_nFocus[currColumn] >= l_nMin[currColumn] + nListItems && l_nFocus[currColumn] < list[currColumn].size())
					l_nMin[currColumn]++;
				if(l_nFocus[currColumn] >= list[currColumn].size()) {
					l_nFocus[currColumn] = 0;
					l_nMin[currColumn] = 0;
				}
				return false;
			case LEFT:
				if(list[DIRECTORY].size() > 0)
					currColumn = DIRECTORY;
				return false;
			case RIGHT:
				if(list[FILE].size() > 0)
					currColumn = FILE;
				return false;
			case ENTER:
			case LMB:
				if(opt == MenuOpt.LMB && scrollTouch[FILE] || scrollTouch[DIRECTORY])
				{
					if(list[currColumn].size() <= nListItems)
						return false;

					int nList = BClipLow(list[currColumn].size() - nListItems, 1);
					int nRange = scrollerHeight;
					int py = y;

					l_nFocus[currColumn] = -1;
					l_nMin[currColumn] = BClipRange(((touchY - py) * nList) / nRange, 0, nList);
					
					return false;
				}
				if(list[DIRECTORY].size() > 0 && currColumn == DIRECTORY)
				{
					if(l_nFocus[DIRECTORY] == -1) return false;
					String dirName = list[DIRECTORY].get(l_nFocus[DIRECTORY]);
					if(dirName.equals(back))
						changeDir(currDir.getParent());
					else changeDir(currDir.checkDirectory(dirName));
					l_nFocus[DIRECTORY] = l_nMin[DIRECTORY] = 0;
					l_nFocus[FILE] = l_nMin[FILE] = 0;
				} else if(list[FILE].size() > 0 && currColumn == FILE) {
					String filename = null;

					if(l_nFocus[FILE] == -1) return false;
					filename = list[FILE].get(l_nFocus[FILE]);
					currFile = currDir.checkFile(filename);
					specialCall.run(handler, this);
				}
				getInput().resetKeyStatus();
				return false;
			case ESC: 
			case RMB:
				//l_nFocus = l_nMin = 0;
				return true;
			case BSPACE:
				if(currDir.getParent() != null)
				{
					changeDir(currDir.getParent());
					
					l_nFocus[DIRECTORY] = l_nMin[DIRECTORY] = 0;
					l_nFocus[FILE] = l_nMin[FILE] = 0;
				}
				return false;
			case PGUP:
				l_nFocus[currColumn] -= (nListItems - 1);
				if(l_nFocus[currColumn] >= 0 && l_nFocus[currColumn] < l_nMin[currColumn])
					if(l_nMin[currColumn] > 0) l_nMin[currColumn] -= (nListItems - 1);
				if(l_nFocus[currColumn] < 0 || l_nMin[currColumn] < 0) {
					l_nFocus[currColumn] = 0;
					l_nMin[currColumn] = 0;
				}
				return false;
			case PGDW:
				l_nFocus[currColumn] += (nListItems - 1);
				if(l_nFocus[currColumn] >= l_nMin[currColumn] + nListItems && l_nFocus[currColumn] < list[currColumn].size())
					l_nMin[currColumn] += (nListItems - 1);
				if(l_nFocus[currColumn] >= list[currColumn].size() || l_nMin[currColumn] > list[currColumn].size() - nListItems) {
					l_nFocus[currColumn] = list[currColumn].size() - 1;
					if(list[currColumn].size() >= nListItems)
						l_nMin[currColumn] = list[currColumn].size() - nListItems;
					else l_nMin[currColumn] = list[currColumn].size() - 1;
				}
				return false;
			case HOME:
				l_nFocus[currColumn] = 0;
				l_nMin[currColumn] = 0;
				return false;
			case END:
				l_nFocus[currColumn] = list[currColumn].size() - 1;
				if(list[currColumn].size() >= nListItems)
					l_nMin[currColumn] = list[currColumn].size() - nListItems;
				else l_nMin[currColumn] = list[currColumn].size() - 1;
				return false;
			default:
				return false;
		}
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		if(mx >= x + width / 2) currColumn = 1;
		else currColumn = 0;

		if(!Gdx.input.isTouched()) {
			scrollTouch[DIRECTORY] = false;
			scrollTouch[FILE] = false;
		}
		
		touchY = my;
		if(mx > scrollX[currColumn] && mx < scrollX[currColumn] + slider.getScrollerWidth()) 
		{
			if(Gdx.input.isTouched())
				scrollTouch[currColumn] = true;
			else scrollTouch[currColumn] = false;
			return true;
		}

		if((!scrollTouch[DIRECTORY] && !scrollTouch[FILE]) && list[currColumn].size() > 0)
		{
			int py = y + font.nHeight + 3;

			for(int i = l_nMin[currColumn]; i >= 0 && i < l_nMin[currColumn] + nListItems && i < list[currColumn].size(); i++) {	
			    if(mx > x && mx < scrollX[FILE])
					if(my > py && my < py + font.nHeight)
					{
						l_nFocus[currColumn] = i;
						return true;
					}
			    
				py += mFontOffset();
			}
		}
		return false;
	}

	@Override
	public void open() {
		boolean ostate = showmain;
		if(numplayers > 1) 
			showmain = true;
		else showmain = false;

		if(currDir == cache && ostate != showmain)
		{
			currDir = null; //force to update filelist
			changeDir(cache);
		}
	}

	@Override
	public void close() {
		for(int i = 0; i < 2; i++)
			l_nFocus[i] = l_nMin[i] = 0;
	}

}


