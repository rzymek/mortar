package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Prepare implements MortarMessage {
	public int seconds;

	public Prepare() {
		this(5 * 60);
	}

	public Prepare(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.writeInt(seconds);
	}

	@Override
	public void deserialize(DataInputStream in) throws IOException {
		seconds = in.readInt();
	}

}
