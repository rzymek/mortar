package org.mortar.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Pref {
	//@formatter:off
	LOCATION_MIN_INTERVAL, 
	LOCATION_MIN_DISTANCE, 
	MAX_GPS_UPTIME, 
	MAX_GPS_DOWNTIME, 
	PASSIVE_LOCATION_MIN_INTERVAL, 
	MIN_GPS_UPTIME, 
	MIN_GPS_DOWNTIME, 
	SCREEN_GPS_CONTROL(Boolean.class);
	//@formatter:on

	public final Class<?> type;

	private Pref() {
		this(Integer.class);
	}

	private Pref(Class<?> type) {
		this.type = type;
	}

	public static class Read {
		private SharedPreferences shared;

		public Read(Context ctx) {
			shared = PreferenceManager.getDefaultSharedPreferences(ctx);
		}

		public int get(Pref key) {
			return shared.getInt(key.name(), 0);
		}

		public boolean is(Pref key) {
			return shared.getBoolean(key.name(), false);
		}

		public void serialize(OutputStream buf) throws IOException {
			DataOutputStream out = new DataOutputStream(buf);
			for (Pref key : values()) {
				if (key.type.equals(Integer.class)) {
					out.writeInt(get(key));
				} else if (key.type.equals(Boolean.class)) {
					out.writeBoolean(is(key));
				} else {
					throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
				}
			}
		}

		public long getGpsUptime() {
			return get(Pref.MAX_GPS_UPTIME);
		}

		public long getGpsDowntime() {
			return get(Pref.MAX_GPS_DOWNTIME);
		}

	}

}
