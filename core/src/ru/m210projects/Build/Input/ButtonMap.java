package ru.m210projects.Build.Input;

import static ru.m210projects.Build.Input.GPManager.MAXBUTTONS;
import com.badlogic.gdx.utils.ObjectIntMap;

public class ButtonMap {
	private static ObjectIntMap<String> butNames;

	public static int valueOf (String keyname) {
		if (butNames == null) initializeKeyNames();
		return butNames.get(keyname, -1);
	}

	private static void initializeKeyNames () {
		butNames = new ObjectIntMap<String>();
		for (int i = 0; i < MAXBUTTONS; i++) {
			String name = "JOY" + i;
			if (name != null) butNames.put(name, i);
		}
	}
	
	public static String buttonName(int num)
	{
		if (butNames == null) initializeKeyNames();
		if(num < 0) return "N/A";
		return butNames.findKey(num);
	}
}
