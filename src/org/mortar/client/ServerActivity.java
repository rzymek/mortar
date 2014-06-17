package org.mortar.client;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		final TextView phoneNumberText = (TextView) findViewById(R.id.phoneNumberText);
		final TextView message = (TextView) findViewById(R.id.message);
		View sendButton = findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = phoneNumberText.getText().toString();
				if (TextUtils.isEmpty(phone)) {
					App app = (App) getApplication();
					Location location = SMSReceiver.parserLocation(message.getText().toString());
					location.setTime(System.currentTimeMillis());
					app.explosionEvent(location, "");
					return;
				}
				SmsManager smsManager = SmsManager.getDefault();
				byte[] data = getBytes(message.getText().toString());

				PendingIntent sent = createPendingResult(1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent delivered = createPendingResult(2, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
				smsManager.sendDataMessage(phone, null, (short) R.integer.sms_port, data, sent, delivered);
			}
		});
		LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location lastKnownLocation = location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if(lastKnownLocation != null) {
			message.setText(String.format(Locale.US, "%2.4f %2.4f", 
					lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()));
		}
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
