package org.mortar.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.location.Location;
import android.location.LocationManager;

public class MortarMessage {
	public static enum Type {
		PREPARE, EXPLOSION, CONFIG
	}

	public final Type type;
	public Location location;
	public int killZoneDiameter;
	public int warrningDiameter;

	public MortarMessage(Type type) {
		super();
		this.type = type;
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);

		out.writeByte(type.ordinal());
		if (type == Type.EXPLOSION) {
			out.writeDouble(location.getLatitude());
			out.writeDouble(location.getLongitude());
			out.writeLong(location.getTime());
			out.writeShort(killZoneDiameter);
			out.writeShort(warrningDiameter);
		}
		return buf.toByteArray();
	}

	public static MortarMessage deserialize(byte[] data) throws IOException {
		ByteArrayInputStream buf = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(buf);

		Type type = Type.values()[in.readByte()];
		MortarMessage message = new MortarMessage(type);
		if (type == Type.EXPLOSION) {
			Location location = new Location(LocationManager.GPS_PROVIDER);

			location.setLatitude(in.readDouble());
			location.setLongitude(in.readDouble());
			location.setTime(in.readLong());
			message.killZoneDiameter = in.readShort();
			message.warrningDiameter = in.readShort();
		}
		return message;
	}
}
