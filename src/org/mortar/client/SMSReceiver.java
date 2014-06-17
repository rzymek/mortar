package org.mortar.client;

import java.io.IOException;
import java.util.Set;

import org.mortar.client.data.MergedMessage;
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
			try {
				MergedMessage msg = MergedMessage.createFromBundle(bundle);
				App app = (App) context;
				MortarMessage info = MortarMessage.deserialize(msg.contents);
				switch(info.type) {
				case EXPLOSION:
					app.explosionEvent(info, msg.from);
					break;
				case PREPARE:
					context.startService(new Intent(context, ListenerService.class));
					break;
				}
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
