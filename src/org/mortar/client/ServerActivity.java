package org.mortar.client;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		final ServerActivity thiz = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		final TextView phoneNumberText = (TextView) findViewById(R.id.phoneNumberText);
		final TextView message = (TextView) findViewById(R.id.message);
		View sendButton = findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = phoneNumberText.getText().toString();
				SmsManager smsManager = SmsManager.getDefault();
				byte[] data = getBytes(message.getText().toString());

				PendingIntent sent = createPendingResult(1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent delivered = createPendingResult(2, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
				smsManager.sendDataMessage(phone, null, (short) R.integer.sms_port, data, sent, delivered);
				
			}
		});
		

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getLine1Number();
		phoneNumberText.setText(phoneNumber);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String toast = requestCode + ":" + requestCode + ":" + SMSReceiver.getExtras(data);
		Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
	}

	protected byte[] getBytes(String string) {
		try {
			return string.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			return string.getBytes();
		}
	}
}
