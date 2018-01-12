package ru.m210projects.Build.Types;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class PropertyIgnoreCase extends Properties {

	private static final long serialVersionUID = 7511088737858527084L;

	public String getPropertyIgnoreCase(String key) {
		return getPropertyIgnoreCase(key, null);
	}

	public String getPropertyIgnoreCase(String key, String defaultV) {
		String value = getProperty(key);
	 	if (value != null)
	 		return value;

	 	// Not matching with the actual key then
		Set<Entry<Object, Object>> s = entrySet();
		Iterator<Entry<Object, Object>> it = s.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			if (key.equalsIgnoreCase((String) entry.getKey()))
				return (String) entry.getValue();
		}
		return defaultV;
	}
}
