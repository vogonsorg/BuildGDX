// Copyright (C) EDuke32 developers and contributors

package ru.m210projects.Build;

import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.util.Map;

import ru.m210projects.Build.OnSceenDisplay.Console;

import static ru.m210projects.Build.OnSceenDisplay.Console.*;

public class Common {
	public static final int T_ERROR = -1;
	public static final int T_EOF = -2;
	
	public static int getatoken(Scriptfile sf, Map<String , Integer> tl)
	{
	    int tok;
	    if (sf == null) return T_ERROR;
	    tok = sf.gettoken();
	    if (tok == -2) return T_EOF;

	    Integer out = tl.get(toLowerCase(sf.textbuf.substring(tok, sf.textptr)));
	    if(out != null)
	    	return out;

	    sf.errorptr = sf.textptr;
	    return T_ERROR;
	}
	
	// checks from path and in ZIPs, returns 1 if NOT found
	public static boolean check_file_exist(String fn)
	{
		if(!kExist(fn, 0))
		{
			Console.Println("Error: file \"" + fn + "\" does not exist", OSDTEXT_RED);
			return true;
		}
	    return false;
	}
}
