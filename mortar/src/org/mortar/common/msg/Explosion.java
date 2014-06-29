package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mortar.client.App;
import org.mortar.common.MortarMessage;

import android.content.Context;
import android.location.Location;

public class Explosion extends MortarMessage {
	private float latitude;
	private float longitude;
	public short killZoneDiameter;
	public short warrningDiameter;
	
	@Override
	public void onReceive(Context context) {
		App app = (App) context.getApplicationContext();
		app.explosionEvent(this);		
	}
	
	@Override
	protected void serialize(DataOutputStream out) throws IOException {
		out.writeFloat(latitude);
		out.writeFloat(longitude);
		out.writeShort(killZoneDiameter);
		out.writeShort(warrningDiameter);
	}
	@Override
	protected void deserialize(DataInputStream in) throws IOException {
		latitude = in.readFloat();
		longitude = in.readFloat();
		killZoneDiameter = in.readShort();
		warrningDiameter = in.readShort();
	}

	public Location getLocation() {		
		Location location = new Location("msg");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}

	public void setLocation(Location location) {
		latitude = (float) location.getLatitude();
		longitude = (float) location.getLongitude();
	}
	
	
}
