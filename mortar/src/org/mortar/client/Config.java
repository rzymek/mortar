package org.mortar.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Config {
	//@formatter:off
	SMS_SEND(false),
	HIGH_ALERT_SEC(5*60);
	//@formatter:on

	public final Class<?> type;
	public final Object defValue;

	private Config(int defValue) {
		this.type = Integer.class;
		this.defValue = defValue;
	}

	private Config(boolean defValue) {
		this.type = Boolean.class;
		this.defValue = defValue;
	}

	public static class Read {
		private SharedPreferences shared;

		public Read(Context ctx) {
			shared = PreferenceManager.getDefaultSharedPreferences(ctx);
		}

		public int get(Config key) {
			return shared.getInt(key.name(), (int) key.defValue);
		}

		public long milis(Config key) {
			return get(key) * 1000L;
		}

		public boolean is(Config key) {
			return shared.getBoolean(key.name(), (boolean) key.defValue);
		}

	}

}
