package org.mortar.sensor.services;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

import java.util.Date;

import org.mortar.common.AbstractLocationListener;
import org.mortar.common.CoordinateConversion;
import org.mortar.common.CoordinateConversion.UTM;
import org.mortar.common.SateliteListener;
import org.mortar.common.Utils;
import org.mortar.sensor.App;
import org.mortar.sensor.Logger;
import org.mortar.sensor.R;
import org.mortar.sensor.activities.LuncherActivity;
import org.mortar.sensor.activities.ViewLogActivity;
import org.mortar.sensor.utils.GPSUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GPSListenerService extends Service {
	private static enum State {
		HIGH_ALERT, LOW_ALERT, OFF
	}

	private boolean isScreenOn = false;
	private State currentState = State.LOW_ALERT;

	private static final int NOTIFIFACTION_ID = 81263183;
	private static final int STOP_HIGH_ALERT = 100;

	public static final String EXTRA_HIGH_ALERT = "high alert (sec)";

	private static final long LOW_ALERT_INTERVAL = 5 * 60 * 1000L;
	private static final float LOW_ALERT_DISTANCE = 50;

	private static final long HIGH_ALERT_INTERVAL = 1 * 1000L;
	private static final float HIGH_ALERT_DISTANCE = 0;

	protected static final long PASSIVE_INTERVAL = 1 * 60 * 1000L;
	private static final float PASSIVE_DISTANCE = 15;

	private static final long NETWORK_INTERVAL = 5 * 60 * 1000L;;
	private static final float NETWORK_DISTANCE = 100;

	private static final boolean VERBOSE = true;

	private LocationManager gps;

	private SateliteListener sateliteListener;

	private Handler handler;
	private Logger logger;

	// ========================================================================================================

	private LocationListener gpsListener = new AbstractLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			if (Log.isLoggable("LOC", Log.DEBUG)) {
				UTM utm = CoordinateConversion.INST.latLon2UTM(location.getLatitude(), location.getLongitude());
				long since = (new Date().getTime() - location.getTime()) / 1000;
				Log.d("LOC", location.getProvider() + ":" + utm + " " + since);
			}

			App app = (App) getApplication();
			if (GPSUtils.isBetterLocation(location, app.getCurrentBestLocation())) {
				app.setCurrentBestLocation(location);
				logger.put(location);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			logger.log(provider + " ON");
		}

		@Override
		public void onProviderDisabled(String provider) {
			logger.log(provider + " off");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			logger.log(provider + " status=" + getStatusString(status) + "; " + Utils.toString(extras));
		}

		private String getStatusString(int status) {
			switch (status) {
			case LocationProvider.AVAILABLE:
				return "AVAILABLE";
			case LocationProvider.OUT_OF_SERVICE:
				return "OUT_OF_SERVICE";
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				return "TEMPORARILY_UNAVAILABLE";
			}
			return "UNKNOWN_" + status;
		}
	};

	private LocationListener otherProvidersListener = new AbstractLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			gpsListener.onLocationChanged(location);
		}

		@Override
		public void onProviderEnabled(String provider) {
			logger.log(provider + " ON");
		}

		@Override
		public void onProviderDisabled(String provider) {
			logger.log(provider + " off");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			logger.log(provider + " status=" + status + ": " + extras);
		}
	};

	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				isScreenOn = false;
				request(State.OFF);
			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
				isScreenOn = true;
				request(State.LOW_ALERT);
			}
		}
	};
	private CharSequence notificationTitle = "Mortar";
	private CharSequence notificationText = "";

	// ========================================================================================================

	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler((App) getApplication());
		logger = ((App) getApplication()).logger;
		gps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gps.addGpsStatusListener(sateliteListener);

		handler = new Handler(getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == STOP_HIGH_ALERT) {
					if (isScreenOn) {
						transitionTo(State.LOW_ALERT);
					} else {
						transitionTo(State.OFF);
					}
				}
			}
		};
		sateliteListener = new SateliteListener(gps) {
			@Override
			protected void onSatelitesChanged(int used, int max) {
				logger.log("sat: " + used + "/" + max);
			}
		};
		reload();
		logger.log("listener created\n" + Utils.getSystemInfo());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.log("start command");
		int forceActive = intent.getIntExtra(EXTRA_HIGH_ALERT, -1);
		if (forceActive > 0) {
			request(State.HIGH_ALERT);
			handler.removeMessages(STOP_HIGH_ALERT);
			handler.sendMessageDelayed(handler.obtainMessage(STOP_HIGH_ALERT), forceActive * 1000L);
		}
		startForeground(NOTIFIFACTION_ID, createNotification(null, null));
		return START_NOT_STICKY;
	}

	private Notification createNotification(CharSequence title, CharSequence msg) {
		if (title != null) {
			notificationTitle = title;
		}
		if (msg != null) {
			notificationText = msg;
		}
		Intent resultIntent = new Intent(this, ViewLogActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		/*
		 * The stack builder object will contain an artificial back stack for
		 * the started Activity. This ensures that navigating backward from the
		 * Activity leads out of your application to the Home screen.
		 */
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(LuncherActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(resultPendingIntent);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(this.notificationTitle);
		if (VERBOSE) {
			builder.setContentText(this.notificationText);
		}

		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		return notification;
	}

	// ========================================================================================================
	private void request(State requested) {
		if (currentState == State.HIGH_ALERT) {
			if (requested != State.HIGH_ALERT) {
				logger.log(currentState + " -/-> " + requested);
				return;
			}
		}
		transitionTo(requested);
	}

	private void transitionTo(State requested) {
		logger.log(currentState + " => " + requested);
		switch (requested) {
		case HIGH_ALERT:
			gps.removeUpdates(gpsListener);
			gps.requestLocationUpdates(GPS_PROVIDER, HIGH_ALERT_INTERVAL, HIGH_ALERT_DISTANCE, gpsListener);
			break;
		case LOW_ALERT:
			gps.removeUpdates(gpsListener);
			gps.requestLocationUpdates(GPS_PROVIDER, LOW_ALERT_INTERVAL, LOW_ALERT_DISTANCE, gpsListener);
			break;
		case OFF:
			gps.removeUpdates(gpsListener);
			break;
		}
		this.currentState = requested;
	}

	// ========================================================================================================

	private void registerOtherLocationProviders() {
		try {
			gps.removeUpdates(otherProvidersListener);
		} catch (Exception ex) {
			// ignore
		}
		try {
			gps.requestLocationUpdates(PASSIVE_PROVIDER, PASSIVE_INTERVAL, PASSIVE_DISTANCE, otherProvidersListener);
			gps.requestLocationUpdates(NETWORK_PROVIDER, NETWORK_INTERVAL, NETWORK_DISTANCE, otherProvidersListener);
		} catch (Exception ex) {
			// emulator does not support NETWORK_PROVIDER.
			Utils.handle(ex, getApplicationContext());
		}
	}

	private void registerScreenListener() {
		unregisterScreenListener();
		final IntentFilter theFilter = new IntentFilter();
		theFilter.addAction(Intent.ACTION_SCREEN_ON);
		theFilter.addAction(Intent.ACTION_SCREEN_OFF);

		getApplicationContext().registerReceiver(screenReceiver, theFilter);
	}

	protected void unregisterScreenListener() {
		try {
			getApplicationContext().unregisterReceiver(screenReceiver);
		} catch (IllegalArgumentException e) {
			// not registered - ignore
		}
	}

	public void reload() {
		request(currentState);
		registerScreenListener();
		registerOtherLocationProviders();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		logger.log("shutting down");
		transitionTo(State.OFF);
		gps.removeGpsStatusListener(sateliteListener);
		gps.removeUpdates(otherProvidersListener);
		unregisterScreenListener();
		stopForeground(true);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (null);
	}
}
