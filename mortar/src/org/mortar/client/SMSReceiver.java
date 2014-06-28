package org.mortar.client;

import java.util.Date;

import org.json.JSONObject;
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
		LocationLogger logger = new LocationLogger(context);
		try {
			String channel = intent.getStringExtra("com.parse.Channel");
			if (channel == null) {
				onReceiveSMS(context, intent, logger);
			} else {
				onReceivePush(context, intent, logger);
			}
		} catch (Exception ex) {
			Utils.handle(ex, context);
		} finally {
			logger.close();
		}
	}

	private void onReceivePush(Context context, Intent intent, LocationLogger logger) throws Exception {
		String action = intent.getAction();
		String channel = intent.getExtras().getString("com.parse.Channel");
		JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
		String msg = "push: " + action + " on " + channel + "\n" + json;
		logger.log(msg);
		Utils.toast(msg, context);
	}

	private void onReceiveSMS(Context context, Intent intent, LocationLogger logger) throws Exception {
		Bundle extras = intent.getExtras();
		GsmMessage sms = GsmMessage.createFromBundle(extras);
		if (sms == null) {
			sms = (GsmMessage) extras.getSerializable("mortar");
		}
		MortarMessage message = MortarMessage.deserialize(sms.contents);
		String msg = message.getClass().getSimpleName() + " from " + sms.from + " (" + new Date(sms.timestamp) + ")";
		logger.log("sms: " + msg);
		message.onReceive(context);
	}
}
