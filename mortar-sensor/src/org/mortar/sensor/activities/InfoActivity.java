package org.mortar.sensor.activities;

import org.mortar.sensor.App;
import org.mortar.sensor.R;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class InfoActivity extends Activity {

	private static final int STREAM = AudioManager.STREAM_MUSIC;
	public static enum Key {
		MESSAGE,FROM, COLOR, BEEP, CONTINIOUS_VIBRATION
	}
	private Vibrator vibrator;
	private int userVolume;
	private AudioManager audioManager;
	private MediaPlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler((App)getApplication());
		setContentView(R.layout.activity_info);
		final TextView infoText = (TextView) findViewById(R.id.infoText);
		final Intent intent = getIntent();
		infoText.setText(intent.getStringExtra(Key.MESSAGE.name()));
		infoText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		findViewById(R.id.infoPane).setBackgroundResource(intent.getIntExtra(Key.COLOR.name(), R.color.hit));
				
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		player = MediaPlayer.create(this, R.raw.beep_long);
		player.setLooping(true);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		startVibrations(intent.getBooleanExtra(Key.CONTINIOUS_VIBRATION.name(), false));
		if(intent.getBooleanExtra(Key.BEEP.name(), false)) {
			startSound();
		}
	}

	protected void startSound() {
		player.start();
		userVolume = audioManager.getStreamVolume(STREAM);
		int maxVolume = audioManager.getStreamMaxVolume(STREAM);
		audioManager.setStreamVolume(STREAM, maxVolume, 0);
	}

	protected void startVibrations(boolean repeat) {
		long[] pattern = {
				0,
				1500,
				300,
				1500,
				300,
				1500,
				300,
		};
		vibrator.vibrate(pattern, repeat ? 0 : -1);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		stop();
	}

	protected void stop() {
		vibrator.cancel();
		player.stop();
		audioManager.setStreamVolume(STREAM, userVolume, 0);
		finish();
	}
}
