package ru.m210projects.Build.android.launcher;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.BuildMessage.MessageType;
import ru.m210projects.Build.FileHandle.Cache1D;
import ru.m210projects.Build.FileHandle.Compat;
import ru.m210projects.Build.FileHandle.Compat.Path;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Settings.BuildConfig;
import ru.m210projects.Build.android.AndroidFactory;
import ru.m210projects.Build.android.AndroidMessage;

public class AndroidLauncher extends Activity {

	public static String appversion = "v1.07";
	public static final String appname = "BuildGDX";

	public enum Game {
		Blood, Duke3d, RR, Powerslave, Tekwar, Witchaven, LSP
	};
	
	private BuildGame getApplication(Game game, BuildConfig cfg)
	{
		switch (game) {
		case Blood:
			return new ru.m210projects.Blood.Main(cfg, "BloodGDX", appversion, false, false);
		case Duke3d:
			return new ru.m210projects.Duke3D.Main(cfg, "DukeGDX", appversion, false, false);
		case RR:
			return new ru.m210projects.Redneck.Main(cfg, "RedneckGDX", appversion, false, false);
		case Powerslave:
			return new ru.m210projects.Powerslave.Main(cfg, "PowerslaveGDX", appversion, false);
		case Tekwar:
			return new ru.m210projects.Tekwar.Main(cfg, "TekwarGDX", appversion, false, false);
		case Witchaven:
			return new ru.m210projects.Witchaven.Main(cfg, "WitchavenGDX", appversion, false, false);
		case LSP:
			return new ru.m210projects.LSP.Main(cfg, "LSPGDX", appversion, false);
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
		case RR:
			return new ru.m210projects.Redneck.Config(Path.Game.getPath(), "RedneckGDX" + ".ini");
		case Powerslave:
			return new ru.m210projects.Powerslave.Config(Path.Game.getPath(), "PowerslaveGDX" + ".ini");
		case Tekwar:
			return new ru.m210projects.Tekwar.Config(Path.Game.getPath(), "TekwarGDX" + ".ini");
		case Witchaven:
			return new ru.m210projects.Witchaven.Config(Path.Game.getPath(), "WitchavenGDX" + ".ini");
		case LSP:
			return new ru.m210projects.LSP.Config(Path.Game.getPath(), "LSPGDX" + ".ini");
		}
		return null;
	}
	
	private String getPath(Game game, String gdxPath)
	{
		switch (game) {
		case Blood:
			gdxPath += "Blood" + File.separator;
			break;
		case Duke3d:
			gdxPath += "Duke3D" + File.separator;
			break;
		case RR:
			gdxPath += "RR" + File.separator;
			break;
		case Powerslave:
			gdxPath += "Powerslave" + File.separator;
			break;
		case Tekwar:
			gdxPath += "Tekwar" + File.separator;
			break;
		case Witchaven:
			gdxPath += "Witchaven" + File.separator;
			break;
		case LSP:
			gdxPath += "LSP" + File.separator;
			break;
		}
		
		return gdxPath;
	}
	
	private boolean checkPermission(Context context)
	{
	    return context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;            
	}
	
	private void launchPort(String path, Game game)
	{
		BuildGdx.compat = new Compat(path, path);
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Game game = Game.Blood;
		
		if (checkPermission(this)) {
			String filepath = getPath(game, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BuildGDX" + File.separator);
			File folder = new File(filepath);
			if (!folder.exists() && !folder.mkdir())
				System.err.println("Folder isn't created: " + folder);

			launchPort(filepath, game);
		} else {
			new AndroidMessage(this).show("Permission denied!", "You have no write permissions for the external storage", MessageType.Crash);
		}
	}
}
