package org.mortar.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import org.mortar.client.activities.InfoActivity;
import org.mortar.client.data.LocationLogger;
import org.mortar.common.Utils;
import org.mortar.common.msg.Explosion;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class App extends Application implements UncaughtExceptionHandler {
	private static final long CURRENT_LOCATION_TIMEOUT = 7 * 60 * 1000;
	private static final int HIGH_ALERT_ON_EXPLOSION_SEC = 3 * 60;

	private Location currentBestLocation;
	private Explosion explosion;
	public LocationLogger logger;
	public Config.Read config;

	@Override
	public void onCreate() {
		super.onCreate();
		logger = new LocationLogger(this);
		Parse.initialize(this, "zh0WoCJmlUEZcyUcfcfJxsV0LzlWogRxT9eskueX", "sUr8eVDrVmKbf3vgkfBQ15O6eRPfT2nPGnekLVP2");
		config = new Config.Read(this);
		setupParse();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void setupParse() {
		ParseInstallation inst = ParseInstallation.getCurrentInstallation();
		inst.put("channel", config.getString(Config.PUSH_CHANNEL));
		inst.saveInBackground();
	}

	private void checkDistance() {
		Log.i("APP", "checkDistance");
		if (currentBestLocation == null || explosion == null) {
			return;
		}
		if (!isCurrentLocationValid()) {
			return;
		}
		float distance = currentBestLocation.distanceTo(explosion.getLocation());
		logger.log(String.format("distance: %.0f", distance));
		Log.i("APP", "distance: " + distance);
		if (distance < explosion.killZoneDiameter) {
			Intent result = new Intent(this, InfoActivity.class);
			String text = "KIA\n" + getString(R.string.epicenter) + ": " + (int) distance + "m" + "\nStrefa KIA:"
					+ explosion.killZoneDiameter + "m" + "\nSłychać na:" + explosion.warrningDiameter;
			result.putExtra(InfoActivity.Key.MESSAGE.name(), text);
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.hit);
			result.putExtra(InfoActivity.Key.BEEP.name(), true);
			result.putExtra(InfoActivity.Key.CONTINIOUS_VIBRATION.name(), true);
			logger.log("KIA");
			show(result);
		} else if (distance < explosion.warrningDiameter) {
			Intent result = new Intent(this, InfoActivity.class);
			String text = "Ostrzał w okolicy!\nEpicentrum: " + (int) distance + "m" + "\nStrefa KIA:"
					+ explosion.killZoneDiameter + "m" + "\nSłychać na:" + explosion.warrningDiameter;
			result.putExtra(InfoActivity.Key.MESSAGE.name(), text);
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.warrning);
			show(result);
		}
	}

	public boolean isCurrentLocationValid() {
		if (currentBestLocation == null)
			return false;
		if (explosion == null || explosion.getLocation() == null)
			return true;
		long timeSpan = Math.abs(currentBestLocation.getTime() - explosion.getLocation().getTime());
		boolean isValid = timeSpan > CURRENT_LOCATION_TIMEOUT;
		Log.i("APP", "timeSpan: " + timeSpan / 1000.0 + "sec - " + (isValid ? "valid" : "obsolete"));
		return isValid;
	}

	private void show(Intent result) {
		result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(result);
		explosion = null;
	}

	public Location getCurrentBestLocation() {
		return currentBestLocation;
	}

	public void setCurrentBestLocation(Location currentBestLocation) {
		this.currentBestLocation = currentBestLocation;
		checkDistance();
	}

	public void explosionEvent(Explosion explosion) {
		this.explosion = explosion;
		Intent intent = new Intent(this, GPSListenerService.class);
		intent.putExtra(GPSListenerService.EXTRA_HIGH_ALERT, HIGH_ALERT_ON_EXPLOSION_SEC);
		startService(intent);
		checkDistance();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("APP", "unhandled", ex);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		logger.log(sw.toString());
		Utils.handle(ex, this);
	}

}
