package org.mortar.common;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;

public abstract class SateliteListener implements GpsStatus.Listener {
	private final LocationManager gps;
	private GpsStatus status;
	
	public SateliteListener(LocationManager gps) {
		this.gps = gps;		
	}
	
	@Override
	public final void onGpsStatusChanged(int event) {
		status = gps.getGpsStatus(status);
		int used = 0;
		int max = 0;
		for (GpsSatellite sat : status.getSatellites()) {
			if (sat.usedInFix())
				used++;
			max++;
		}
		onSatelitesChanged(used,max);
	}

	protected abstract void onSatelitesChanged(int used, int max);
}
