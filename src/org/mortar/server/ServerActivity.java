package org.mortar.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.mortar.client.App;
import org.mortar.client.R;
import org.mortar.client.SMSReceiver;
import org.mortar.utils.AttackSerializer;
import org.mortar.utils.Utils;

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
					byte[] data = AttackSerializer.write(getAttackLocation());

					PendingIntent sent = createPendingResult(1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
					PendingIntent delivered = createPendingResult(2, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
					smsManager.sendDataMessage(phone, null, (short) R.integer.sms_port, data, sent, delivered);
				} catch (IOException ex) {
					Utils.handle(ex, getApplicationContext());
				}
			}
		});
		LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location lastKnownLocation = location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (lastKnownLocation != null) {
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
