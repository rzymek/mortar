package org.mortar.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.mortar.client.data.MergedMessage;
import org.mortar.common.MortarMessage;
import org.mortar.common.MortarMessage.Type;
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

				MortarMessage.Type type = getType(msg.contents);
				switch (type) {
					case EXPLOSION:
						MortarMessage info = MortarMessage.deserialize(msg.contents);
						app.explosionEvent(info, msg.from);
						break;
					case PREPARE:
						context.startService(new Intent(context, ListenerService.class));
						break;
					case CONFIG:
						ByteArrayInputStream buf = new ByteArrayInputStream(msg.contents, 1, msg.contents.length - 1);
						Intent cmd = new Intent(context, ListenerService.class);
						cmd.putExtra(ListenerService.EXTRA_CONFIG, Config.deserialize(buf));
						context.startService(cmd);
						break;
				}
			} catch (IOException ex) {
				Utils.handle(ex, context);
			}
		}
	}

	private Type getType(byte[] contents) {
		return Type.values()[contents[0]];
	}
}
