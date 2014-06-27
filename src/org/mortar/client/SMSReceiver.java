package org.mortar.client;

import java.util.Date;

import org.mortar.client.data.GsmMessage;
import org.mortar.client.data.LocationLogger;
import org.mortar.common.MortarMessage;
import org.mortar.common.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SMSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			LocationLogger logger = new LocationLogger(context);
			try {
				GsmMessage sms = GsmMessage.createFromBundle(bundle);
				if (sms == null) {
					sms = (GsmMessage) bundle.getSerializable("mortar");
				}
				MortarMessage message = MortarMessage.deserialize(sms.contents);
				logger.log("incomming: " + message.getClass().getSimpleName() + " from " + sms.from + " ("
						+ new Date(sms.timestamp) + ")");
				message.onReceive(context);
			} catch (Exception ex) {
				Utils.handle(ex, context);
			} finally {
				logger.close();
			}
		}
	}
}
