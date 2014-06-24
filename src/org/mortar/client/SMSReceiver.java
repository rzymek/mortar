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
				App app = (App) context.getApplicationContext();
				
				MergedMessage msg = MergedMessage.createFromBundle(bundle);
				if(msg == null)
					msg = (MergedMessage) bundle.getSerializable("mortar");
				
				MortarMessage.Type type = getType(msg.contents);
				Utils.toast(type.name(), context);
				switch (type) {
					case EXPLOSION:
						MortarMessage info = MortarMessage.deserialize(msg.contents);
						app.explosionEvent(info, msg.from);
						break;
					case PREPARE:
						Intent prepare = new Intent(context, ListenerService.class);
						prepare.putExtra(ListenerService.EXTRA_HIGH_ALERT, 10);
						context.startService(prepare);
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
