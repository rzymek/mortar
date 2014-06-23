package org.mortar.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class Config implements Serializable {
	private static final long serialVersionUID = 1L;
	public long locationMinInterval = 1 * 60 * 1000;
	public short locationMinDistance = 5;
	public long maxGpsUptime = 3 * 60 * 1000;// 1 * 60 * 1000;
	public long maxGpsDowntime = 5 * 60 * 1000;// 5 * 60 * 1000;

	public void serialize(OutputStream buf) throws IOException {
		DataOutputStream out = new DataOutputStream(buf);
		out.writeLong(locationMinInterval);
		out.writeShort(locationMinDistance);
		out.writeLong(maxGpsUptime);
		out.writeLong(maxGpsDowntime);
	}

	public static Config deserialize(InputStream buf) throws IOException {
		DataInputStream in = new DataInputStream(buf);
		Config config = new Config();
		config.locationMinInterval = in.readLong();
		config.locationMinDistance = in.readShort();
		config.maxGpsUptime = in.readLong();
		config.maxGpsDowntime = in.readLong();
		return config;
	}
}