package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mortar.common.MortarMessage;

import android.content.Context;

public class ConfigMessage extends MortarMessage {

	@Override
	public void onReceive(Context context) {
	}
	
	@Override
	protected void serialize(DataOutputStream out) throws IOException {		
	}

	@Override
	protected void deserialize(DataInputStream in) throws IOException {
	}

}
