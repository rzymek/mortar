package org.mortar.common.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface MortarMessage {
	void serialize(DataOutputStream out) throws IOException;

	void deserialize(DataInputStream in) throws IOException;

}
