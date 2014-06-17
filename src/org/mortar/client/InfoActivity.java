package org.mortar.client;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;

public class InfoActivity extends Activity {

	private static final int STREAM = AudioManager.STREAM_MUSIC;
	private Vibrator vibrator;
	private int userVolume;
	private AudioManager audioManager;
	private MediaPlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		View infoText = findViewById(R.id.infoText);
		infoText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		player = MediaPlayer.create(this, R.raw.beep);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		start();
	}

	protected void start() {
		long[] pattern = {
				0,
				1500,
				300
		};
		vibrator.vibrate(pattern, 0);
		player.setLooping(true);
		player.start();
		userVolume = audioManager.getStreamVolume(STREAM);
		int maxVolume = audioManager.getStreamMaxVolume(STREAM);
		audioManager.setStreamVolume(STREAM, maxVolume, 0);
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
