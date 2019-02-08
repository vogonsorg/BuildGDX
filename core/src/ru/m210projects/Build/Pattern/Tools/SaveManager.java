package ru.m210projects.Build.Pattern.Tools;

import static ru.m210projects.Build.FileHandle.Compat.Bcheck;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SaveManager {

	private List<SaveInfo> SavList = new ArrayList<SaveInfo>();
	private HashMap<String, SaveInfo> SavHash = new HashMap<String, SaveInfo>();
	
	public class SaveInfo implements Comparable<SaveInfo> {
		public String name;
		public long time;
		public String filename;

		public SaveInfo(String name, long time, String filename)
		{
			this.name = name;
			this.time = time;
			this.filename = filename;
		}
		
		public void update(String name, long time, String filename)
		{
			this.name = name;
			this.time = time;
			this.filename = filename;
		}

		@Override
		public int compareTo(SaveInfo obj) {
			return (obj.time < this.time)? -1 : 1;
		}
	}

	public SaveInfo getSlot(int num)
	{
		return SavList.get(num);
	}
	
	public List<SaveInfo> getList()
	{
		return SavList;
	}
	
	public void add(String savname, long time, String filename)
	{
		SaveInfo info;
		if((info = SavHash.get(filename)) == null) {
			info = new SaveInfo(savname, time, filename);
			SavList.add(0, info);
			SavHash.put(filename, info);
		} else {
			SavList.remove(info);
			info.update(savname, time, filename);
			SavList.add(0, info);
		}
	}
	
	public void delete(String filename)
	{
		SaveInfo info;
		if((info = SavHash.get(filename)) != null) {
			File file = Bcheck(filename, "R");
			if(file != null) {
				SavList.remove(info);
				file.delete();
			}
		} 
	}
	
	public String getLast()
	{
		if(SavList.size() > 0)
			return SavList.get(0).filename;
		return null;
	}
	
	public void sort()
	{
		Collections.sort(SavList);
	}
}
