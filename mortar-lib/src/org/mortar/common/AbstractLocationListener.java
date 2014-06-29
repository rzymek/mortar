package org.mortar.common;

import android.location.LocationListener;
import android.os.Bundle;

public abstract class AbstractLocationListener implements LocationListener {

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

}
