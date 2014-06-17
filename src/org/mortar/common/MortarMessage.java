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
		PREPARE, EXPLOSION
	}
	
	public final Type type;
	public final Location location;
	
	public MortarMessage(Type type, Location location) {
		super();
		this.type = type;
		this.location = location;
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		
		out.writeByte(type.ordinal());
		out.writeDouble(location.getLatitude());
		out.writeDouble(location.getLongitude());
		out.writeLong(location.getTime());
		return buf.toByteArray();		
	}
	
	public static MortarMessage deserialize(byte[] data) throws IOException {
		ByteArrayInputStream buf = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(buf);

		Location location = new Location(LocationManager.GPS_PROVIDER);

		Type type = Type.values()[in.readByte()];		
		location.setLatitude(in.readDouble());
		location.setLongitude(in.readDouble());
		location.setTime(in.readLong());		
		return new MortarMessage(type, location);
	}
}
