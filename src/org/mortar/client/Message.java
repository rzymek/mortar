package org.mortar.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.telephony.SmsMessage;

public class Message { 
	public byte[] contents;
	public String from="";
	public long timestamp=0;
	
	public static Message createFromBundle(Bundle bundle) throws IOException {
		Object[] pdus = (Object[]) bundle.get("pdus");
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Message msg = null;
		for (Object pdu : pdus) {
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
			buf.write(sms.getUserData());
			if(msg == null) {
				msg = new Message();
				msg.from = sms.getOriginatingAddress();
				msg.timestamp = sms.getTimestampMillis();
			}
		}
		msg.contents = buf.toByteArray(); 
		return msg;
	}
}