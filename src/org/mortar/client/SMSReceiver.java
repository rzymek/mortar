package org.mortar.client;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			StringBuilder message = new StringBuilder();
			Object[] pdus = (Object[]) bundle.get("pdus");
			String from = null;
			for (Object pdu : pdus) {
				SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
				message.append(toUTF8(msg.getUserData()));
				if (from == null) {
					from = msg.getOriginatingAddress();
				}
			}

			Intent result = new Intent(context, InfoActivity.class);
			result.putExtra(InfoActivity.Key.MESSAGE.name(), message.toString());
			result.putExtra(InfoActivity.Key.FROM.name(), from);
			result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(result);
		}
	}

	private String toUTF8(byte[] userData) {
		try {
			return new String(userData,"utf-8");
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
