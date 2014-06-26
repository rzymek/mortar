package org.mortar.client;

import org.mortar.client.data.GsmMessage;
import org.mortar.common.MortarMessage;
import org.mortar.common.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			try {
				GsmMessage sms = GsmMessage.createFromBundle(bundle);
				if (sms == null) {
					sms = (GsmMessage) bundle.getSerializable("mortar");
				}
				MortarMessage message = MortarMessage.deserialize(sms.contents);
				Log.i("SMS","received "+message.getClass().getSimpleName());
				message.onReceive(context);
			} catch (Exception ex) {
				Utils.handle(ex, context);
			}
		}
	}
}
