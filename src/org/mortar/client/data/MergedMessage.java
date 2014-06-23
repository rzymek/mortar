package org.mortar.client.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.os.Bundle;
import android.telephony.SmsMessage;

public class MergedMessage implements Serializable { 
	public byte[] contents;
	public String from="";
	public long timestamp=0;
	
	public static MergedMessage createFromBundle(Bundle bundle) throws IOException {
		Object[] pdus = (Object[]) bundle.get("pdus");
		if(pdus == null)
			return null;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		MergedMessage msg = null;
		for (Object pdu : pdus) {
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
			buf.write(sms.getUserData());
			if(msg == null) {
				msg = new MergedMessage();
				msg.from = sms.getOriginatingAddress();
				msg.timestamp = sms.getTimestampMillis();
			}
		}
		msg.contents = buf.toByteArray(); 
		return msg;
	}
}