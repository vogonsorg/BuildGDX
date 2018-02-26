package ru.m210projects.Build.android;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import ru.m210projects.Build.Types.Message;
//import ru.m210projects.BuildEngine.android.R;

public class AndroidMessage implements Message {
	AndroidApplication app;
	public AndroidMessage(AndroidApplication app)
	{
		this.app = app;
	}
	
	@Override
	public boolean show(final String header, final String text, boolean send) {
		app.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				AlertDialog.Builder bld;
				bld = new AlertDialog.Builder(app);
//				bld.setIcon(R.drawable.ic_launcher);
				bld.setTitle(header);
				bld.setMessage(text);
				bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						app.finish();
					}
				});
				bld.setCancelable(false);
				bld.create().show();
			}
		});
		return true;
	}

	@Override
	public void dispose() {

	}
}
