package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortar.common.Config;

import android.content.Context;

public class ConfigMsg implements MortarMessage {
	protected Map<Config, Object> values = new HashMap<>();

	public ConfigMsg() {
	}

	public ConfigMsg(Context ctx) {
		Config.Read reader = new Config.Read(ctx);
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				values.put(key, reader.get(key));
			} else if (key.type.equals(Boolean.class)) {
				values.put(key, reader.is(key));
			} else if (key.type.equals(String.class)) {
				values.put(key, reader.getString(key));
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}
	}


	@Override
	public void serialize(DataOutputStream out) throws IOException {
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				out.writeInt((int) values.get(key));
			} else if (key.type.equals(Boolean.class)) {
				out.writeBoolean((boolean) values.get(key));
			} else if (key.type.equals(String.class)) {
				out.writeUTF((String) values.get(key));
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}

	}

	@Override
	public void deserialize(DataInputStream in) throws IOException {
		for (Config key : Config.values()) {
			if (key.type.equals(Integer.class)) {
				values.put(key, in.readInt());
			} else if (key.type.equals(Boolean.class)) {
				values.put(key, in.readBoolean());
			} else if (key.type.equals(String.class)) {
				values.put(key, in.readUTF());
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
		}
	}

}
