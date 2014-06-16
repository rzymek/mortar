package org.mortar.client;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;

public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		long[] pattern = {500,100};
		vibrator.vibrate(pattern, 100);
	}
}
