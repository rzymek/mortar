package org.mortar.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Config {
	//@formatter:off
	LOC_MIN_INTERVAL(5), 
	LOC_MIN_DISTANCE(5), 
	MAX_GPS_UPTIME(3), 
	MAX_GPS_DOWNTIME(5), 
	PASSIVE_LOC_MIN_INTERVAL(1), 
	MIN_GPS_UPTIME(1), 
	MIN_GPS_DOWNTIME(1), 
	SCREEN_GPS_CONTROL(true);
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
			return get(key) * 60L * 1000L;
		}

		public boolean is(Config key) {
			return shared.getBoolean(key.name(), (boolean) key.defValue);
		}

		public long getGpsUptime() {
			return milis(Config.MAX_GPS_UPTIME);
		}

		public long getGpsDowntime() {
			return milis(Config.MAX_GPS_DOWNTIME);
		}
	}

}
