package org.mortar.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortar.common.msg.ConfigMessage;
import org.mortar.common.msg.Explosion;
import org.mortar.common.msg.Prepare;

import android.content.Context;

public abstract class MortarMessage {
	private static final Map<Integer, Class<? extends MortarMessage>> registry = new HashMap<>();
	static {
		register(Explosion.class);
		register(Prepare.class);
		register(ConfigMessage.class);
	}

	public abstract void onReceive(Context context);

	protected abstract void serialize(DataOutputStream out) throws IOException;

	protected abstract void deserialize(DataInputStream in) throws IOException;

	public byte[] serialize() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);

		int type = getTypeCode(getClass());
		out.writeInt(type);
		serialize(out);
		return buf.toByteArray();
	}

	public static MortarMessage deserialize(byte[] data) throws IOException, ReflectiveOperationException {
		ByteArrayInputStream buf = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(buf);
		int type = in.readInt();
		Class<? extends MortarMessage> clazz = registry.get(type);
		if (clazz == null) {
			throw new IllegalArgumentException("Unregistered type: " + type + ". Known types: " + registry);
		}
		MortarMessage message = clazz.newInstance();
		message.deserialize(in);
		return message;
	}

	public static void register(Class<? extends MortarMessage> clazz) {
		registry.put(getTypeCode(clazz), clazz);
	}

	protected static int getTypeCode(Class<? extends MortarMessage> clazz) {
		return clazz.getName().hashCode();
	}
}
