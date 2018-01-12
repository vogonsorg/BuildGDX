package ru.m210projects.Build.Render;

import static ru.m210projects.Build.Engine.MAXXDIM;
import static ru.m210projects.Build.Engine.MAXYDIM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Graphics.DisplayMode;

public class VideoMode {
	public static final int MAXVALIDMODES = 256;
	public static String[] strvmodes;
	public static List<VideoMode> validmodes = new ArrayList<VideoMode>();
	public DisplayMode mode;
	public int xdim,ydim;
	public byte bpp;
	
	public VideoMode(DisplayMode mode) {
		this.mode = mode;
		this.xdim = mode.width;
		this.ydim = mode.height;
		this.bpp = (byte) mode.bitsPerPixel;
	}
	
	public static void initVideoModes(DisplayMode[] modes, DisplayMode DesktopDisplayMode) {
		if(strvmodes != null)
			return;

		for (int i = 0; i < modes.length; i++) {
			VideoMode mode = new VideoMode(modes[i]);
			boolean exist = false;
			for(VideoMode savedmode: validmodes) {
				if(savedmode.xdim == mode.xdim && savedmode.ydim == mode.ydim) {
					exist = true;
					break;
				}
			}
			if(exist)
				continue;
			if(mode.xdim > MAXXDIM)
				continue;
			if(mode.ydim > MAXYDIM)
				continue;

			validmodes.add(mode);
		}
		
		if(validmodes.size() == 0)
			validmodes.add(new VideoMode(DesktopDisplayMode));

		Collections.sort(validmodes, new Comparator<VideoMode>() {
            public int compare(VideoMode lhs, VideoMode rhs) {
                return lhs.ydim > rhs.ydim ? -1 : (lhs.ydim > rhs.ydim ) ? 1 : 0;
            }
		});
		Collections.sort(validmodes, new Comparator<VideoMode>() {
            public int compare(VideoMode lhs, VideoMode rhs) {
                return lhs.xdim > rhs.xdim ? -1 : (lhs.xdim > rhs.xdim ) ? 1 : 0;
            }
		});
		Collections.reverse(validmodes);
		strvmodes = new String[validmodes.size()];
		for (int i = 0; i < validmodes.size(); i++) {
			VideoMode mode = validmodes.get(i);
			strvmodes[i] = new String(mode.xdim +" x "+mode.ydim + " 32bpp");
		}
	}
	
	public static DisplayMode getmode(int xdim, int ydim) {
		int j = 0;

		for (int i = 0; i < validmodes.size(); i++) {
			if ((validmodes.get(i).xdim == xdim) && (validmodes.get(i).ydim == ydim))
			{
				j = i;
				break;
			}
		}

		return validmodes.get(j).mode;
	}
	
	public static boolean setFullscreen(int xdim, int ydim, boolean fullscreen)
	{
		if(!fullscreen)
			return false;
		else
			if(getmodeindex(xdim, ydim) != -1)
				return true;

		return false;
	}
	
	public static DisplayMode getmode(int index) {
		if(index >= 0 && index < validmodes.size())
			return validmodes.get(index).mode;
		
		return null;
	}
	
	public static int getmodeindex(int xdim, int ydim) {
		int j = -1;

		for (int i = 0; i < validmodes.size(); i++) {
			if ((validmodes.get(i).xdim == xdim) && (validmodes.get(i).ydim == ydim))
			{
				j = i;
				break;
			}
		}

		return j;
	}
}
