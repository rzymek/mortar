package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.location.Location;

public class Explosion implements MortarMessage {
	private float latitude;
	private float longitude;
	private long timestamp;
	public short killZoneDiameter;
	public short warrningDiameter;
	
	
	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.writeFloat(latitude);
		out.writeFloat(longitude);
		out.writeShort(killZoneDiameter);
		out.writeShort(warrningDiameter);
	}
	@Override
	public void deserialize(DataInputStream in) throws IOException {
		latitude = in.readFloat();
		longitude = in.readFloat();
		killZoneDiameter = in.readShort();
		warrningDiameter = in.readShort();
	}

	public Location getLocation() {		
		Location location = new Location("msg");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setTime(timestamp);
		return location;
	}

	public void setLocation(Location location) {
		latitude = (float) location.getLatitude();
		longitude = (float) location.getLongitude();
		timestamp = location.getTime();
	}
	
	
}
