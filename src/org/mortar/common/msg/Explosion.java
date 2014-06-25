package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mortar.client.App;
import org.mortar.common.MortarMessage;

import android.content.Context;
import android.location.Location;

public class Explosion extends MortarMessage {
	public Location location;
	public short killZoneDiameter;
	public short warrningDiameter;
	
	@Override
	public void onReceive(Context context) {
		App app = (App) context.getApplicationContext();
		app.explosionEvent(this);		
	}
	
	@Override
	protected void serialize(DataOutputStream out) throws IOException {
		out.writeFloat((float) location.getLatitude());
		out.writeFloat((float) location.getLongitude());
		out.writeShort(killZoneDiameter);
		out.writeShort(warrningDiameter);
	}
	@Override
	protected void deserialize(DataInputStream in) throws IOException {
		location = new Location("Message");
		location.setLatitude(in.readFloat());
		location.setLongitude(in.readFloat());
		killZoneDiameter = in.readShort();
		warrningDiameter = in.readShort();
	}
	
	
}
