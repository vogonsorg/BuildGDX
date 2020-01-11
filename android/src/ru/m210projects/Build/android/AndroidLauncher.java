package ru.m210projects.Build.android;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import ru.m210projects.Blood.Config;
import ru.m210projects.Blood.Main;
import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.Cache1D;
import ru.m210projects.Build.FileHandle.Compat;
import ru.m210projects.Build.FileHandle.Compat.Path;
import ru.m210projects.Build.Settings.BuildConfig;
import ru.m210projects.Build.android.AndroidFactory;

public class AndroidLauncher extends Activity {

	public static final String appname = "BuildGDX";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BuildGDX"
				+ File.separator;
		File folder = new File(filepath);
		if (!folder.exists() && !folder.mkdir()) 
			System.err.println("Folder isn't created: " + folder);

		BuildGdx.compat = new Compat(filepath, filepath);
		BuildGdx.cache = new Cache1D(BuildGdx.compat);

		BuildConfig cfg = new Config(Path.Game.getPath(), appname + ".ini");

		BuildConfiguration appcfg = new BuildConfiguration();
		appcfg.fullscreen = true;
		appcfg.width = (cfg.ScreenWidth);
		appcfg.height = (cfg.ScreenHeight);

		ApplicationFactory factory = new AndroidFactory(this, appcfg);

		new BuildApplication(new Main(cfg, appname, "?.??", false, false), factory, cfg.renderType);
	}
}
