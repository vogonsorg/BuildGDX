package ru.m210projects.Build.android.launcher;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.Cache1D;
import ru.m210projects.Build.FileHandle.Compat;
import ru.m210projects.Build.FileHandle.Compat.Path;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Settings.BuildConfig;
import ru.m210projects.Build.android.AndroidFactory;

public class AndroidLauncher extends Activity {

	public static String appversion = "v1.06";
	public static final String appname = "BuildGDX";

	public enum Game {
		Blood, Duke3d
	};
	
	private BuildGame getApplication(Game game, BuildConfig cfg)
	{
		switch (game) {
		case Blood:
			return new ru.m210projects.Blood.Main(cfg, "BloodGDX", appversion, false, false);
		case Duke3d:
			return new ru.m210projects.Duke3D.Main(cfg, "DukeGDX", appversion, false, false);
		}
		return null;
	}
	
	private BuildConfig getConfig(Game game)
	{
		switch (game) {
		case Blood:
			return new ru.m210projects.Blood.Config(Path.Game.getPath(), "BloodGDX" + ".ini");
		case Duke3d:
			return new ru.m210projects.Duke3D.Config(Path.Game.getPath(), "DukeGDX" + ".ini");
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Game game = Game.Duke3d;
		String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BuildGDX"
				+ File.separator;

		switch (game) {
		case Blood:
			filepath += "Blood" + File.separator;
			break;
		case Duke3d:
			filepath += "Duke3D" + File.separator;
			break;
		}

		File folder = new File(filepath);
		if (!folder.exists() && !folder.mkdir())
			System.err.println("Folder isn't created: " + folder);

		BuildGdx.compat = new Compat(filepath, filepath);
		BuildGdx.cache = new Cache1D(BuildGdx.compat);

		BuildConfig cfg = getConfig(game);
		BuildGame ga = getApplication(game, cfg);
		if(ga != null) {
			BuildConfiguration appcfg = new BuildConfiguration();
			appcfg.fullscreen = true;
			appcfg.width = (cfg.ScreenWidth);
			appcfg.height = (cfg.ScreenHeight);
	
			ApplicationFactory factory = new AndroidFactory(this, appcfg);
	
			new BuildApplication(ga, factory, cfg.renderType);
		}
	}
}
