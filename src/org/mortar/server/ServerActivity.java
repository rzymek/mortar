package org.mortar.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.mortar.client.App;
import org.mortar.client.R;
import org.mortar.client.SMSReceiver;
import org.mortar.common.MortarMessage;
import org.mortar.common.MortarMessage.Type;
import org.mortar.common.Utils;

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
	private static final int RC_SENT = 100;
	private static final int RC_DELIVERY_REPORT = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		View sendButton = findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				send();
			}
		});
		LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location lastKnownLocation = location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (lastKnownLocation != null) {
			final TextView message = (TextView) findViewById(R.id.message);
			message.setText(String.format(Locale.US, "%2.4f %2.4f", lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
		}
	}

	protected Location getAttackLocation() {
		final TextView message = (TextView) findViewById(R.id.message);
		String[] split = message.getText().toString().split(" ");
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(Double.parseDouble(split[0]));
		location.setLongitude(Double.parseDouble(split[1]));
		location.setTime(System.currentTimeMillis());
		return location;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case RC_SENT:
			return;
		case RC_DELIVERY_REPORT:
			return;
		}
		String toast = requestCode + ":" + requestCode + ":" + SMSReceiver.getExtras(data);
		Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
	}

	private void send() {
		final TextView phoneNumberText = (TextView) findViewById(R.id.phoneNumberText);
		String phone = phoneNumberText.getText().toString();
		// TEST CODE {
		if (TextUtils.isEmpty(phone)) {
			App app = (App) getApplication();
			Location location = getAttackLocation();
			app.explosionEvent(location, "");
			return;
		}
		// } TEST CODE
		try {
			SmsManager smsManager = SmsManager.getDefault();
			MortarMessage message = new MortarMessage(Type.EXPLOSION, getAttackLocation());

			PendingIntent sent = createPendingResult(RC_SENT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent delivered = createPendingResult(RC_DELIVERY_REPORT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

			smsManager.sendDataMessage(phone, null, (short) R.integer.sms_port, message.serialize(), sent, delivered);
		} catch (IOException ex) {
			Utils.handle(ex, getApplicationContext());
		}
	}
}
