package org.mortar.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class Config implements Serializable {
	private static final long serialVersionUID = 1L;

	public int locationMinInterval = 5 * 60 * 1000;
	public short locationMinDistance = 5;
	public int maxGpsUptime = 3 * 60 * 1000;// 1 * 60 * 1000;
	public int maxGpsDowntime = 5 * 60 * 1000;// 5 * 60 * 1000;
	public int passiveLocationMinInterval = 1 * 60 * 1000;
	// public int minGpsUptime = 1 * 60 * 1000;
	// public int minGpsDowntime = 1 * 60 * 1000;
	public boolean screenGpsControl = true;

	public void serialize(OutputStream buf) throws IOException {
		DataOutputStream out = new DataOutputStream(buf);
		out.writeInt(locationMinInterval);
		out.writeShort(locationMinDistance);
		out.writeInt(maxGpsUptime);
		out.writeInt(maxGpsDowntime);
	}

	public static Config deserialize(InputStream buf) throws IOException {
		DataInputStream in = new DataInputStream(buf);
		Config config = new Config();
		config.locationMinInterval = in.readInt();
		config.locationMinDistance = in.readShort();
		config.maxGpsUptime = in.readInt();
		config.maxGpsDowntime = in.readInt();
		return config;
	}

	public long getGpsUptime() {
		return maxGpsUptime;
	}

	public long getGpsDowntime() {
		return maxGpsDowntime;
	}
}