package org.mortar.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mortar.common.msg.MortarMessage;

public class MessageSerializer {
	private String pkg;

	public MessageSerializer(Package pkg) {
		this.pkg = pkg.getName();
	}

	public byte[] serialize(MortarMessage msg) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		out.writeUTF(msg.getClass().getSimpleName());
		msg.serialize(out);
		return buf.toByteArray();
	}

	public MortarMessage deserialize(byte[] data) throws IOException, ReflectiveOperationException {
		ByteArrayInputStream buf = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(buf);
		String type = in.readUTF();
		Class<?> clazz= Class.forName(pkg+'.'+type);
		MortarMessage message = (MortarMessage) clazz.newInstance();
		message.deserialize(in);
		return message;
	}

}
