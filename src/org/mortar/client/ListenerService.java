package org.mortar.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mortar.client.activities.LuncherActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ListenerService extends Service implements LocationListener {
	private static final int NOTIFIFACTION_ID = 1337;
	private static final long LOC_MIN_TIME = 1 * 60 * 1000;
	private static final long MAX_GPS_TIME = 3 * 60;
	private static final long SPIN_GPS_EVERY = 15 * 60;
	private static final float LOC_MIN_DIST = 0;
	private LocationManager locationManager;
	private Runnable gpsOn = new Runnable() {
		@Override
		public void run() {
			startGPS();
			scheduler.schedule(gpsOff, MAX_GPS_TIME, TimeUnit.SECONDS);
		}
	};
	private Runnable gpsOff = new Runnable() {
		@Override
		public void run() {
			App app = (App) getApplication();
			if(app.isCurrentLocationValid()) { 
				stopGPS();
			}
		}
	};
	private ScheduledExecutorService scheduler;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent resultIntent = new Intent(this, LuncherActivity.class);
		resultIntent.putExtra(LuncherActivity.Cmd.class.getSimpleName(), LuncherActivity.Cmd.EXIT.ordinal());
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0/* flags */);
		Notification notification = new NotificationCompat.Builder(this)
			.setContentIntent(resultPendingIntent)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("Mortar Client")
			.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFIFACTION_ID, notification);
		
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(gpsOn, 0, SPIN_GPS_EVERY, TimeUnit.SECONDS);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return START_NOT_STICKY;
	}

	private void startGPS() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOC_MIN_TIME, LOC_MIN_DIST, this);
	}

	public void onLocationChanged(Location location) {
		App app = (App) getApplication();
		if(isBetterLocation(location, app.getCurrentBestLocation())) {
			app.setCurrentBestLocation(location);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		stopGPS();
		scheduler.shutdown();
	}

	private void stopGPS() {
		locationManager.removeUpdates(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (null);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		final int TWO_MINUTES = 1000 * 60 * 2;
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
