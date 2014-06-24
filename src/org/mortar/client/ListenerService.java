package org.mortar.client;

import org.mortar.client.Pref.Read;
import static org.mortar.client.Pref.*;
import org.mortar.client.activities.LuncherActivity;
import org.mortar.client.data.DBHelper;
import org.mortar.common.Utils;

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
import static android.location.LocationManager.*;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

public class ListenerService extends Service {
	protected static final int GPS_ON = 61;
	protected static final int GPS_OFF = 60;
	protected static final int LOW_ALERT = 66;

	private static final int NOTIFIFACTION_ID = 1337;

	public static final String EXTRA_CONFIG = "config";
	public static final String EXTRA_HIGH_ALERT = "high alert (min)";
	public static final String EXTRA_RELOAD = "reload";

	private LocationManager locationManager;
	private DBHelper db;
	private Handler handler;
	private Location lastSavedLocation;
	private Read config;
	private boolean highAlert = false;

	// ========================================================================================================

	private LocationListener gpsListener = new AbstractLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			db.log("location:" + location.getProvider());
			App app = (App) getApplication();
			if (GPSUtils.isBetterLocation(location, app.getCurrentBestLocation())) {
				app.setCurrentBestLocation(location);

				if (lastSavedLocation != null) {
					if (lastSavedLocation.distanceTo(location) < config.get(LOCATION_MIN_DISTANCE))
						return;
					if (location.getTime() - lastSavedLocation.getTime() < config.get(PASSIVE_LOCATION_MIN_INTERVAL))
						return;
				}
				lastSavedLocation = location;
				db.put(location);
			}
		}
	};

	private LocationListener otherProvidersListener = new AbstractLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			gpsListener.onLocationChanged(location);
		}
	};

	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			db.log(action);
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				stopGPS();
			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
				startGPS();
			}
		}
	};

	// ========================================================================================================

	@Override
	public void onCreate() {
		super.onCreate();

		config = new Pref.Read(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		db = new DBHelper(this);

		handler = new Handler(getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				clearHandlerMessages();
				switch (msg.what) {
				case LOW_ALERT:
					highAlert = false;
					// continue to GPS_OFF:
				case GPS_OFF:
					stopGPS();
					setGpsDelayed(GPS_ON, config.getGpsDowntime());
					return;
				case GPS_ON:
					startGPS();
					return;
				}
			}
		};

		db.log("created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		db.log("onStartCommand");
		if (intent.getBooleanExtra(EXTRA_RELOAD, false)) {
			reload();
			return START_NOT_STICKY;
		}

		int forceActive = intent.getIntExtra(EXTRA_HIGH_ALERT, -1);
		if (forceActive > 0) {
			highAlert = true;
			clearHandlerMessages();
			startGPS();
			handler.sendMessageDelayed(handler.obtainMessage(LOW_ALERT), forceActive * 60 * 1000);
			return START_NOT_STICKY;
		}

		if (config == null) {
			// defaults:
			// setConfig(new Config());
		}

		startNotification();
		return START_NOT_STICKY;
	}

	private void startNotification() {
		Intent resultIntent = new Intent(this, LuncherActivity.class);
		resultIntent.putExtra(LuncherActivity.Cmd.class.getSimpleName(), LuncherActivity.Cmd.EXIT.ordinal());
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0/* flags */);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(resultPendingIntent);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("Mortar Client");

		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFIFACTION_ID, notification);
	}

	// ========================================================================================================

	private void startGPS() {
		db.log("start GPS " + (highAlert ? "!!!" : ""));
		if (!highAlert) {
			setGpsDelayed(GPS_OFF, config.getGpsUptime());
		}

		int minTime = config.get(LOCATION_MIN_INTERVAL);
		int distance = config.get(LOCATION_MIN_DISTANCE);
		locationManager.requestLocationUpdates(GPS_PROVIDER, minTime, distance, gpsListener);
	}

	private void stopGPS() {
		if (highAlert) {
			db.log("ignoring stop GPS (high alert)");
			return;
		}
		db.log("stop GPS");
		clearHandlerMessages();
		locationManager.removeUpdates(gpsListener);
	}

	// ========================================================================================================

	private void registerOtherLocationProviders() {
		try {
			locationManager.removeUpdates(otherProvidersListener);
			long passiveMinTime = config.get(PASSIVE_LOCATION_MIN_INTERVAL);
			long networkMinTime = config.get(LOCATION_MIN_INTERVAL);
			float minDistance = config.get(LOCATION_MIN_DISTANCE);
			locationManager.requestLocationUpdates(PASSIVE_PROVIDER, passiveMinTime, minDistance, otherProvidersListener);
			locationManager.requestLocationUpdates(NETWORK_PROVIDER, networkMinTime, minDistance, otherProvidersListener);
		} catch (Exception ex) {
			// emulator does not support NETWORK_PROVIDER.
			Utils.handle(ex, getApplicationContext());
		}
	}

	private void registerScreenListener() {
		try {
			getApplicationContext().unregisterReceiver(screenReceiver);
		} catch (IllegalArgumentException e) {
			// not registered - ignore
		}
		final IntentFilter theFilter = new IntentFilter();
		theFilter.addAction(Intent.ACTION_SCREEN_ON);
		theFilter.addAction(Intent.ACTION_SCREEN_OFF);

		if (config.is(SCREEN_GPS_CONTROL)) {
			getApplicationContext().registerReceiver(screenReceiver, theFilter);
		}
	}

	public void reload() {
		highAlert = false;
		stopGPS();

		registerScreenListener();
		registerOtherLocationProviders();

		startGPS();
	}

	public void setGpsDelayed(int what, long when) {
		handler.removeMessages(GPS_ON);
		handler.removeMessages(GPS_OFF);
		handler.sendMessageDelayed(handler.obtainMessage(what), when);

	}

	private void clearHandlerMessages() {
		handler.removeMessages(GPS_OFF);
		handler.removeMessages(GPS_ON);
		handler.removeMessages(LOW_ALERT);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.log("listener destroy");
		db.close();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (null);
	}
}
