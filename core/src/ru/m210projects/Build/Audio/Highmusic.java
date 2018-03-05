package ru.m210projects.Build.Audio;

import static ru.m210projects.Build.FileHandle.Compat.*;

import java.util.HashMap;

public class Highmusic {
	public static HashMap<String, String> midToMusic = new HashMap<String, String>();
	
	public static void addDigitalMusic(String midi, String digital)
	{
		midToMusic.put(toLowerCase(midi), toLowerCase(digital));
	}
	
	public static String checkDigitalMusic(String midi)
	{
		if(midi != null)
			return midToMusic.get(toLowerCase(midi));
		
		return null;
	}
}
