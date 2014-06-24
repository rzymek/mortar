package org.mortar.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config1 implements Serializable {
	private static final long serialVersionUID = 1L;
	private Context ctx;
	private SharedPreferences shared;
	public static enum Key {
		LOCATION_MIN_INVERAL
	}
	

	private int locationMinInterval = 5 * 60 * 1000;
	public short locationMinDistance = 5;
	public int maxGpsUptime = 3 * 60 * 1000;// 1 * 60 * 1000;
	public int maxGpsDowntime = 5 * 60 * 1000;// 5 * 60 * 1000;
	public int passiveLocationMinInterval = 1 * 60 * 1000;
	// public int minGpsUptime = 1 * 60 * 1000;
	// public int minGpsDowntime = 1 * 60 * 1000;
	public boolean screenGpsControl = true;

	public void serialize(OutputStream buf) throws IOException {
		DataOutputStream out = new DataOutputStream(buf);
		out.writeInt(getLocationMinInterval());
		out.writeShort(locationMinDistance);
		out.writeInt(maxGpsUptime);
		out.writeInt(maxGpsDowntime);
	}

	public static Config1 deserialize(InputStream buf) throws IOException {
		DataInputStream in = new DataInputStream(buf);
		Config1 config = new Config1();
		config.setLocationMinInterval(in.readInt());
		config.locationMinDistance = in.readShort();
		config.maxGpsUptime = in.readInt();
		config.maxGpsDowntime = in.readInt();
		return config;
	}
	
	public int get(Key key) {
		return shared.getInt(key.name(), 0);
	}

	public long getGpsUptime() {
		return maxGpsUptime;
	}

	public long getGpsDowntime() {
		return maxGpsDowntime;
	}

	public int getLocationMinInterval() {		
		return locationMinInterval;
	}

	public void setLocationMinInterval(int locationMinInterval) {
		this.locationMinInterval = locationMinInterval;
	}
}