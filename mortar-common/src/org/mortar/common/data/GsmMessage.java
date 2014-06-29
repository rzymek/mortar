package org.mortar.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.os.Bundle;
import android.telephony.SmsMessage;

public class GsmMessage implements Serializable { 
	private static final long serialVersionUID = 1L;
	
	public byte[] contents;
	public String from="";
	public long timestamp=0;
	
	public static GsmMessage createFromBundle(Bundle bundle) throws IOException {
		Object[] pdus = (Object[]) bundle.get("pdus");
		if(pdus == null)
			return null;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		GsmMessage msg = null;
		for (Object pdu : pdus) {
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
			buf.write(sms.getUserData());
			if(msg == null) {
				msg = new GsmMessage();
				msg.from = sms.getOriginatingAddress();
				msg.timestamp = sms.getTimestampMillis();
			}
		}
		msg.contents = buf.toByteArray(); 
		return msg;
	}
}