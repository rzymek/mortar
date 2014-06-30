package org.mortar.sensor.activities;

import org.mortar.common.Utils;
import org.mortar.sensor.App;
import org.mortar.sensor.R;
import org.mortar.sensor.services.GPSListenerService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

public class LuncherActivity extends Activity {
	public static final String EXTRA_EXIT = "exit";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler((App) getApplication());
		final LuncherActivity thiz = LuncherActivity.this;
		Intent intent = getIntent();
		if (intent.getBooleanExtra(EXTRA_EXIT, false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Mortar");
			builder.setMessage("Exit?");
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					stopService(new Intent(thiz, GPSListenerService.class));
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.create().show();
		} else {			
			startService(new Intent(this, GPSListenerService.class));
			Utils.toast(getString(R.string.app_started), this);
			finish();
		}
	}
}
