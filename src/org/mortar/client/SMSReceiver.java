package org.mortar.client;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			StringBuilder message = new StringBuilder();
			Object[] pdus = (Object[]) bundle.get("pdus");
			String from="";
			long timestamp=0;
			for (Object pdu : pdus) {
				SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
				message.append(toUTF8(msg.getUserData()));
				from = msg.getOriginatingAddress();
				timestamp = msg.getTimestampMillis();
			}
			App app = (App) context;
			Location location = parserLocation(message.toString());
			location.setTime(timestamp);
			app.explosionEvent(location, from);
		}
	}

	public static Location parserLocation(String s) {
		String[] coords = s.split(" ");
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(Location.convert(coords[0]));
		location.setLongitude(Location.convert(coords[1]));
		return location;
	}

	private String toUTF8(byte[] userData) {
		try {
			return new String(userData, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return new String(userData);
		}
	}

	public static String getExtras(Intent intent) {
		if (intent == null) {
			return "intent==null";
		}
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return "extras==null";
		}
		Set<String> keySet = extras.keySet();
		String s = "";
		for (String key : keySet) {
			s += key + ":" + extras.get(key) + "\n";
		}
		return s;
	}

}
