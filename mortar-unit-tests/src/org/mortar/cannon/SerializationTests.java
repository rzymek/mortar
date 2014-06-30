package org.mortar.cannon;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortar.common.MessageSerializer;
import org.mortar.common.msg.ConfigMsg;
import org.mortar.common.msg.Explosion;
import org.mortar.common.msg.MortarMessage;
import org.mortar.common.msg.Prepare;
import org.mortar.sensor.msg.ReceivedMessage;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SerializationTests {
	private static final int PDU_SIZE = 134;

	@Test
	public void test() throws Exception {
		MessageSerializer serializer = new MessageSerializer(MortarMessage.class.getPackage());
		MessageSerializer deserializer = new MessageSerializer(ReceivedMessage.class.getPackage());
		MortarMessage[] msgs = { new ConfigMsg(Robolectric.application), new Explosion(), new Prepare() };
		for (MortarMessage msg : msgs) {
			byte[] serialized = serializer.serialize(msg);			
			int length = serialized.length;
			System.out.println(msg.getClass().getSimpleName()+":"+length);
			assertThat(length, lessThanOrEqualTo(PDU_SIZE));
			assertThat(deserializer.deserialize(serialized), instanceOf(ReceivedMessage.class));			
		}
	}
}
