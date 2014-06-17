package org.mortar.client;

import org.mortar.client.activities.InfoActivity;

import android.app.Application;
import android.content.Intent;
import android.location.Location;

public class App extends Application {

	private static final float KILL_ZONE = 30;
	private static final float OBSERVE_ZONE = 300;
	private static final long TIMEOUT = 7 * 60 * 1000;
	private Location currentBestLocation;
	private Location explosionAt;

	private void checkDistance() {
		if (currentBestLocation == null || explosionAt == null) {
			return;
		}
		if (Math.abs(currentBestLocation.getTime() - explosionAt.getTime()) > TIMEOUT) {
			return;
		}
		float distance = currentBestLocation.distanceTo(explosionAt);
		if (distance < KILL_ZONE) {
			Intent result = new Intent(this, InfoActivity.class);
			result.putExtra(InfoActivity.Key.MESSAGE.name(), "KIA\nEpicentrum: " + (int) distance + "m");
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.hit);
			result.putExtra(InfoActivity.Key.BEEP.name(), true);
			result.putExtra(InfoActivity.Key.CONTINIOUS_VIBRATION.name(), true);
			show(result);
		} else if (distance < OBSERVE_ZONE) {
			Intent result = new Intent(this, InfoActivity.class);
			result.putExtra(InfoActivity.Key.MESSAGE.name(), "Ostrzał w okolicy!\nEpicentrum: "
					+ (int) distance + "m");
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.warrning);
			show(result);
		}
	}

	private void show(Intent result) {
		result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(result);
		explosionAt = null;
	}

	public Location getCurrentBestLocation() {
		return currentBestLocation;
	}

	public void setCurrentBestLocation(Location currentBestLocation) {
		this.currentBestLocation = currentBestLocation;
		checkDistance();
	}

	public void explosionEvent(Location location, String from) {
		explosionAt = location;
		startService(new Intent(this, ListenerService.class));
		checkDistance();
	}

}
