package org.mortar.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortar.client.App;
import org.mortar.client.R;
import org.mortar.common.CoordinateConversion;
import org.mortar.common.CoordinateConversion.UTM;
import org.mortar.common.MortarMessage;
import org.mortar.common.MortarMessage.Type;
import org.mortar.common.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ServerActivity extends Activity {
	private static final int RC_SENT = 100;
	private static final int RC_DELIVERY_REPORT = 101;
	private List<String> clients;
	public Map<String, String> clientStatus = new HashMap<>();
	private TextView statusView;
	private TextView utmZoneText;
	private TextView eastingText;
	private TextView northingText;
	private TextView killZoneDiameterText;
	private TextView warrningDiameterText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

		statusView = (TextView) findViewById(R.id.statusView);
		utmZoneText = (TextView) findViewById(R.id.utmZoneText);
		eastingText = (TextView) findViewById(R.id.eastingText);
		northingText = (TextView) findViewById(R.id.northingText);
		killZoneDiameterText = (TextView) findViewById(R.id.killZoneDiameterText);
		warrningDiameterText = (TextView) findViewById(R.id.warrningDiameterText);
		
		View sendButton = findViewById(R.id.fireButton);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(clients.isEmpty())
					testHit();
				else
					fire();
			}
		});
		clients = loadClientNumbers();
		updateStatus();
		
		LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location lastKnownLocation = location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (lastKnownLocation != null) {
			UTM utm = CoordinateConversion.INST.latLon2UTM(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
			utmZoneText.setText(utm.longZone+" "+utm.latZone);
			eastingText.setText(""+utm.easting);
			northingText.setText(""+utm.northing);
		}
	}

	protected void testHit() {		
		try {
			App app = (App) getApplication();
			MortarMessage msg = createMortarMessage();
			app.explosionEvent(msg, "");
		}catch(Exception ex){
			Utils.handle(ex, this);
		}
	}

	private List<String> loadClientNumbers() {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);		
		String[] raw = shared.getString(NumberListActivity.DataKey.NUMBERS.name(), "").split("\n");
		List<String> values = new ArrayList<>(Arrays.asList(raw));
		Iterator<String> iterator = values.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			if(TextUtils.isEmpty(string.trim()))
				iterator.remove();
		}
		return values;
	}

	protected Location getAttackLocation() {
		String utmText = utmZoneText.getText()+" "+eastingText.getText()+" "+northingText.getText();
		try {
			double[] latLon = CoordinateConversion.INST.utm2LatLon(utmText);
			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(latLon[0]);
			location.setLongitude(latLon[1]);
			location.setTime(System.currentTimeMillis());
			return location;
		}catch(Exception ex) {
			throw new IllegalArgumentException(utmText, ex);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String number = data.getStringExtra("number");
		switch(requestCode) {
		case RC_SENT:
			clientStatus.put(number, "sent("+resultCode+")");
			break;
		case RC_DELIVERY_REPORT:
			byte[] pdu = data.getByteArrayExtra("pdu");
			if(pdu != null) {
				SmsMessage reportSms = SmsMessage.createFromPdu(pdu);
				String body = reportSms.getMessageBody();
				clientStatus.put(number, "delivered("+resultCode+"):"+body);
			}else{
				clientStatus.put(number, "delivered("+resultCode+")");
			}
			break;
		default:
			return;
		}
		updateStatus();
	}

	private void updateStatus() {
		StringBuilder text = new StringBuilder();
		for (String number : clients) {
			text.append(number).append(" ").append(Utils.n(clientStatus.get(number))).append("\n");
		}
		statusView.setText(text);		
	}

	private void fire() {
		try {
			final SmsManager smsManager = SmsManager.getDefault();
			final MortarMessage message = createMortarMessage();
			final byte[] userData = message.serialize();
			for (String phone: clients) {
				Intent data = new Intent();
				data.putExtra("number", phone);
				PendingIntent sent = createPendingResult(RC_SENT, data, PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent delivered = createPendingResult(RC_DELIVERY_REPORT, data, PendingIntent.FLAG_UPDATE_CURRENT);
				short smsPort = (short) R.integer.sms_port;
				smsManager.sendDataMessage(phone, null, smsPort, userData, sent, delivered);
				clientStatus.put(phone, "sending");
				updateStatus();
			}
		}catch(Exception ex){
			Utils.handle(ex, this);
		}
	}

	private MortarMessage createMortarMessage() {
		final MortarMessage message = new MortarMessage(Type.EXPLOSION, getAttackLocation());
		message.killZoneDiameter = Integer.parseInt(killZoneDiameterText.getText().toString());
		message.warrningDiameter = Integer.parseInt(warrningDiameterText.getText().toString());
		return message;
	}
}
