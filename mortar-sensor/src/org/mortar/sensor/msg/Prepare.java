package org.mortar.sensor.msg;

import org.mortar.sensor.services.GPSListenerService;

import android.content.Context;
import android.content.Intent;

public class Prepare extends org.mortar.common.msg.Prepare implements ReceivedMessage {
	@Override
	public void onReceive(Context context) {
		Intent prepare = new Intent(context, GPSListenerService.class);
		prepare.putExtra(GPSListenerService.EXTRA_HIGH_ALERT, seconds);
		context.startService(prepare);
	}

}
