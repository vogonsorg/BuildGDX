package ru.m210projects.Build.android;

import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;

import android.app.Activity;
import ru.m210projects.Build.Architecture.BuildConfiguration;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildGraphics;
import ru.m210projects.Build.Architecture.BuildInput;

public class AndroidFrame extends BuildFrame {
	
	protected final Activity activity;
	public AndroidFrame(Activity activity, BuildConfiguration config, FrameType type) {
		super(config);
		this.activity = activity;
	}

	@Override
	public BuildGraphics getGraphics(FrameType type) {
		return new AndroidGraphics(this, new FillResolutionStrategy());
	}

	@Override
	public BuildInput getInput(FrameType type) {
		return new AndroidInput();
	}

}
