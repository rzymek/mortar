package org.mortar.sensor;

import java.util.Date;

import org.mortar.common.Utils;
import org.mortar.common.msg.Explosion;
import org.mortar.sensor.activities.InfoActivity;
import org.mortar.sensor.activities.ViewLogActivity;
import org.mortar.sensor.services.GPSListenerService;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.parse.Parse;
import com.parse.PushService;

public class App extends org.mortar.common.App {
	private static final long CURRENT_LOCATION_TIMEOUT = 7 * 60 * 1000;
	private static final int HIGH_ALERT_ON_EXPLOSION_SEC = 3 * 60;

	private Location currentBestLocation;
	private Explosion explosion;
	public Logger logger;

	@Override
	public void onCreate() {
		super.onCreate();
		logger = new Logger(this);
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
		boolean isValid = timeSpan < CURRENT_LOCATION_TIMEOUT;
		logger.log("shelling timeSpan: " + timeSpan / 1000.0 + "sec - " + (isValid ? "valid" : "obsolete"));
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
		logger.log(Utils.getStackString(ex));
		super.uncaughtException(thread, ex);
	}
	
	public void simulateHit() {
		currentBestLocation = new Location("mock");
		currentBestLocation.setLatitude(52);
		currentBestLocation.setLongitude(23);
		currentBestLocation.setTime(new Date().getTime());
		explosion = new Explosion();
		explosion.setLocation(currentBestLocation);
		explosion.killZoneDiameter = 30;
		explosion.warrningDiameter = 300;
		checkDistance();
	}

}
