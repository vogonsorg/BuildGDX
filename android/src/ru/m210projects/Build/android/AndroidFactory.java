package ru.m210projects.Build.android;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.android.AndroidClipboard;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.utils.Clipboard;

import android.app.Activity;
import ru.m210projects.Build.Architecture.ApplicationFactory;
import ru.m210projects.Build.Architecture.BuildApplication.Platform;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildFrame.FrameType;
import ru.m210projects.Build.Architecture.BuildMessage;
import ru.m210projects.Build.Audio.BuildAudio;
import ru.m210projects.Build.Input.BuildControllers;

public class AndroidFactory implements ApplicationFactory {

	private Activity activity;
	private BuildConfiguration cfg;
	public AndroidFactory(Activity activity, BuildConfiguration cfg)
	{
		this.cfg = cfg;
		this.activity = activity;
	}
	
	@Override
	public BuildConfiguration getConfiguration() {
		return cfg;
	}

	@Override
	public BuildMessage getMessage() {
		return new AndroidMessage(activity);
	}

	@Override
	public BuildAudio getAudio() {
		return new BuildAudio();
	}

	@Override
	public Files getFiles() {
		activity.getFilesDir(); // workaround for Android bug #10515463
		return new AndroidFiles(activity.getAssets(), activity.getFilesDir().getAbsolutePath());
	}

	@Override
	public BuildControllers getControllers() {
		return new AndroidControllers();
	}

	@Override
	public Platform getPlatform() {
		return Platform.Android;
	}

	@Override
	public BuildFrame getFrame(BuildConfiguration config, FrameType type) {
		return new AndroidFrame(activity, config, type);
	}

	@Override
	public ApplicationType getApplicationType() {
		return ApplicationType.Android;
	}

	@Override
	public Clipboard getClipboard() {
		return new AndroidClipboard(activity);
	}
}
