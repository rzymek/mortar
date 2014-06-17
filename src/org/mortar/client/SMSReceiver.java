package org.mortar.client;

import java.io.IOException;
import java.util.Set;

import org.mortar.utils.AttackSerializer;
import org.mortar.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

public class SMSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			try {
				Message msg = Message.createFromBundle(bundle);
				App app = (App) context;
				Location location = AttackSerializer.read(msg.contents);
				app.explosionEvent(location, msg.from);
			} catch (IOException ex) {
				Utils.handle(ex, context);
			}
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
