package org.mortar.client;

import org.mortar.client.activities.InfoActivity;
import org.mortar.common.MortarMessage;

import android.app.Application;
import android.content.Intent;
import android.location.Location;

public class App extends Application {

	private static final long TIMEOUT = 7 * 60 * 1000;
	private Location currentBestLocation;
	private MortarMessage explosion;

	private void checkDistance() {
		if (currentBestLocation == null || explosion == null) {
			return;
		}
		if (isCurrentLocationValid()) {
			return;
		}
		float distance = currentBestLocation.distanceTo(explosion.location);
		if (distance < explosion.killZoneDiameter) {
			Intent result = new Intent(this, InfoActivity.class);
			String text = "KIA\nEpicentrum: " + (int) distance + "m"
					+ "\nStrefa KIA:"+explosion.killZoneDiameter+"m"
					+ "\nSłychać na:"+explosion.warrningDiameter;
			result.putExtra(InfoActivity.Key.MESSAGE.name(), text);
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.hit);
			result.putExtra(InfoActivity.Key.BEEP.name(), true);
			result.putExtra(InfoActivity.Key.CONTINIOUS_VIBRATION.name(), true);
			show(result);
		} else if (distance < explosion.warrningDiameter) {
			Intent result = new Intent(this, InfoActivity.class);
			String text = "Ostrzał w okolicy!\nEpicentrum: " + (int) distance + "m"
					+ "\nStrefa KIA:"+explosion.killZoneDiameter+"m"
					+ "\nSłychać na:"+explosion.warrningDiameter;
			result.putExtra(InfoActivity.Key.MESSAGE.name(), text);
			result.putExtra(InfoActivity.Key.COLOR.name(), R.color.warrning);
			show(result);
		}
	}

	public boolean isCurrentLocationValid() {
		if(currentBestLocation == null)
			return false;
		if(explosion == null || explosion.location == null)
			return true;
		return Math.abs(currentBestLocation.getTime() - explosion.location.getTime()) > TIMEOUT;
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

	public void explosionEvent(MortarMessage info, String from) {
		explosion = info;
		startService(new Intent(this, ListenerService.class));
		checkDistance();
	}

}
