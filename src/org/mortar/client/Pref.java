package org.mortar.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Pref {
	//@formatter:off
	LOCATION_MIN_INTERVAL(5), 
	LOCATION_MIN_DISTANCE(5), 
	MAX_GPS_UPTIME(3), 
	MAX_GPS_DOWNTIME(5), 
	PASSIVE_LOCATION_MIN_INTERVAL(1), 
	MIN_GPS_UPTIME(1), 
	MIN_GPS_DOWNTIME(1), 
	SCREEN_GPS_CONTROL(true);
	//@formatter:on

	public final Class<?> type;
	public final Object defValue;

	private Pref(int defValue) {
		this.type = Integer.class;
		this.defValue = defValue;
	}
	private Pref(boolean defValue) {
		this.type = Boolean.class;
		this.defValue = defValue;
	}

	public static class Read {
		private SharedPreferences shared;

		public Read(Context ctx) {
			shared = PreferenceManager.getDefaultSharedPreferences(ctx);
		}

		public int get(Pref key) {
			return shared.getInt(key.name(), (int) key.defValue);
		}
		
		public long milis(Pref key) {
			return get(key) * 60L * 1000L;
		}

		public boolean is(Pref key) {
			return shared.getBoolean(key.name(), (boolean) key.defValue);
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
			return milis(Pref.MAX_GPS_UPTIME);
		}

		public long getGpsDowntime() {
			return milis(Pref.MAX_GPS_DOWNTIME);
		}

	}

}
