package ru.m210projects.Build.Script;

import static ru.m210projects.Build.FileHandle.Compat.toLowerCase;

import java.util.HashMap;

public class AudioInfo {

	private HashMap<String, String> midToMusic;
	public AudioInfo()
	{
		midToMusic = new HashMap<String, String>();
	}
	
	public void addDigitalInfo(String midiname, String oggfile) {
		midToMusic.put(midiname, oggfile);
	}
	
	public String getDigitalInfo(String midi)
	{
		if(midi != null)
			return midToMusic.get(toLowerCase(midi));
		
		return null;
	}
}
