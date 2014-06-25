package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortar.client.Config;
import org.mortar.common.MortarMessage;
import org.mortar.common.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ConfigMessage extends MortarMessage {
	private Map<Config, Object> values = new HashMap<>();

	public ConfigMessage() {
	}

	public ConfigMessage(Context ctx) {
		Config.Read reader = new Config.Read(ctx);
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				values.put(key, reader.get(key));
			} else if (key.type.equals(Boolean.class)) {
				values.put(key, reader.is(key));
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}
	}

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
				} else {
					throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
				}
			}
			edit.commit();
			Utils.toast("Config received", context);
		} catch (Exception ex) {
			Utils.handle(ex, context);
		}

	}

	@Override
	protected void serialize(DataOutputStream out) throws IOException {
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				out.writeInt((int) values.get(key));
			} else if (key.type.equals(Boolean.class)) {
				out.writeBoolean((boolean) values.get(key));
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}

	}

	@Override
	protected void deserialize(DataInputStream in) throws IOException {
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				values.put(key, in.readInt());
			} else if (key.type.equals(Boolean.class)) {
				values.put(key, in.readBoolean());
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}
	}

}
