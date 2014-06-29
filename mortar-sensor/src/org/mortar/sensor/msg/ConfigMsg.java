package org.mortar.sensor.msg;

import org.mortar.common.Config;
import org.mortar.common.Utils;
import org.mortar.sensor.App;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ConfigMsg extends org.mortar.common.msg.ConfigMsg implements ReceivedMessage {

	@Override
	public void onReceive(Context context) {
		try {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
			Editor edit = shared.edit();
			for (Config key : Config.values()) {
				if (key.type.equals(Integer.class)) {
					edit.putInt(key.name(), (int) values.get(key));
				} else if (key.type.equals(Boolean.class)) {
					edit.putBoolean(key.name(), (boolean) values.get(key));
				} else if (key.type.equals(String.class)) {
					edit.putString(key.name(), (String) values.get(key));
				} else {
					throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
				}
			}
			edit.commit();
			((App) context).setupParse();
			Utils.toast("Config received", context);
		} catch (Exception ex) {
			Utils.handle(ex, context);
		}

	}
}
