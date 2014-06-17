package org.mortar.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.location.Location;
import android.location.LocationManager;

public class AttackSerializer {

	public static byte[] write(Location location) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		
		out.writeDouble(location.getLatitude());
		out.writeDouble(location.getLongitude());
		out.writeLong(location.getTime());
		return buf.toByteArray();
	}
	
	public static Location read(byte[] data) throws IOException {
		ByteArrayInputStream buf = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(buf);
		
		Location location = new Location(LocationManager.GPS_PROVIDER);		
		location.setLatitude(in.readDouble());
		location.setLongitude(in.readDouble());
		location.setTime(in.readLong());
		return location;
	}
	
	public static void main(String[] args) throws IOException {
		Location location = new Location(LocationManager.GPS_PROVIDER);
		byte[] serialize = AttackSerializer.write(location);
		System.out.println(serialize.length);
	}
}
