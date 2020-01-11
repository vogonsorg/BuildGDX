// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.android;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import ru.m210projects.Build.Architecture.BuildFrame;
import ru.m210projects.Build.Architecture.BuildMessage;

public class AndroidMessage implements BuildMessage {
	private Activity app;
	private BuildFrame frame;
	private boolean update;
	
	public AndroidMessage(Activity app)
	{
		this.app = app;
	}
	
	@Override
	public void setFrame(BuildFrame frame) {
		this.frame = frame;
	}
	
	@Override
	public boolean show(final String header, final String text, MessageType type) {
		app.runOnUiThread(new Runnable() {
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
