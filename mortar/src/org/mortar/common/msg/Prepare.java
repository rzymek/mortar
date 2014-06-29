package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mortar.client.services.GPSListenerService;
import org.mortar.common.MortarMessage;

import android.content.Context;
import android.content.Intent;

public class Prepare extends MortarMessage {
	public int seconds;

	public Prepare() {
		this(5 * 60);
	}

	public Prepare(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public void onReceive(Context context) {
		Intent prepare = new Intent(context, GPSListenerService.class);
		prepare.putExtra(GPSListenerService.EXTRA_HIGH_ALERT, seconds);
		context.startService(prepare);
	}

	@Override
	protected void serialize(DataOutputStream out) throws IOException {
		out.writeInt(seconds);
	}

	@Override
	protected void deserialize(DataInputStream in) throws IOException {
		seconds = in.readInt();
	}

}
