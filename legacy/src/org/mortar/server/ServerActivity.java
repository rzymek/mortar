package org.mortar.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortar.client.AbstractLocationListener;
import org.mortar.client.R;
import org.mortar.client.data.GsmMessage;
import org.mortar.common.CoordinateConversion;
import org.mortar.common.CoordinateConversion.UTM;
import org.mortar.common.MortarMessage;
import org.mortar.common.Utils;
import org.mortar.common.msg.ConfigMessage;
import org.mortar.common.msg.Explosion;
import org.mortar.common.msg.Prepare;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ServerActivity extends Activity {
	private static final int RC_SENT = 100;
	private static final int RC_DELIVERY_REPORT = 101;
	protected static final int PREPARE_SECONDS = 10;//TODO: increase

	private List<String> clients;
	public Map<String, String> clientStatus = new HashMap<>();

	private TextView statusView;
	private TextView utmZoneText;
	private TextView eastingText;
	private TextView northingText;
	private TextView killZoneDiameterText;
	private TextView warrningDiameterText;

	private LocationManager gps;
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

		findViewById(R.id.fireButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					broadcast(createFireMessage());
				} catch (Exception ex) {
					Utils.handle(ex, getApplicationContext());
				}
			}
		});
		findViewById(R.id.prepareButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcast(new Prepare(PREPARE_SECONDS));
			}
		});
		clients = loadClientNumbers();
		updateStatus();

		gps = (LocationManager) getSystemService(LOCATION_SERVICE);
		setLocation(gps.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
	}

	private void setLocation(Location Location) {
		if (Location != null) {
			UTM utm = CoordinateConversion.INST.latLon2UTM(Location.getLatitude(), Location.getLongitude());
			utmZoneText.setText(utm.longZone + " " + utm.latZone);
			eastingText.setText("" + utm.easting);
			northingText.setText("" + utm.northing);
		}
	}

	private List<String> loadClientNumbers() {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		String[] raw = shared.getString(NumbersActivity.EXTRA_NUMBERS, "").split("\n");
		List<String> values = new ArrayList<>(Arrays.asList(raw));
		Iterator<String> iterator = values.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			if (TextUtils.isEmpty(string.trim()))
				iterator.remove();
		}
		return values;
	}

	protected Location getAttackLocation() {
		String utmText = utmZoneText.getText() + " " + eastingText.getText() + " " + northingText.getText();
		try {
			double[] latLon = CoordinateConversion.INST.utm2LatLon(utmText);
			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(latLon[0]);
			location.setLongitude(latLon[1]);
			location.setTime(System.currentTimeMillis());
			return location;
		} catch (Exception ex) {
			throw new IllegalArgumentException(utmText, ex);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String number = data.getStringExtra("number");
		switch (requestCode) {
		case RC_SENT:
			clientStatus.put(number, clientStatus.get(number) + " sent(" + resultCodeDesc(resultCode) + ")");
			break;
		case RC_DELIVERY_REPORT:
			byte[] pdu = data.getByteArrayExtra("pdu");
			String body = "";
			if (pdu != null) {
				SmsMessage reportSms = SmsMessage.createFromPdu(pdu);
				body = reportSms.getMessageBody();
			}
			clientStatus.put(number, clientStatus.get(number) + " delivered(" + resultCodeDesc(resultCode) + "):" + body);
			break;
		default:
			return;
		}
		updateStatus();
	}

	private String resultCodeDesc(int resultCode) {
		switch (resultCode) {
		case RESULT_OK:
			return "OK";
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			return "GENERIC_FAILURE";
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			return "NO_SERVICE";
		case SmsManager.RESULT_ERROR_NULL_PDU:
			return "NULL_PDU";
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			return "RADIO_OFF";
		}
		return "" + resultCode;
	}

	private void updateStatus() {
		StringBuilder text = new StringBuilder();
		for (String number : clients) {
			text.append(number).append(" ").append(Utils.n(clientStatus.get(number))).append("\n");
		}
		statusView.setText(text);
	}

	private void broadcast(final MortarMessage message) {
		try {
			if (clients.isEmpty()) {
				Intent intent = new Intent("android.intent.action.DATA_SMS_RECEIVED");
				intent.setData(Uri.parse("sms://0:" + R.integer.sms_port));
				GsmMessage msg = new GsmMessage();
				msg.contents = message.serialize();
				msg.from = "me";
				msg.timestamp = new Date().getTime();
				intent.putExtra("mortar", msg);
				sendBroadcast(intent);
			}
			final byte[] userData = message.serialize();
			final SmsManager smsManager = SmsManager.getDefault();
			for (String phone : clients) {
				clientStatus.put(phone, "sending");
				updateStatus();
				Intent data = new Intent();
				data.putExtra("number", phone);
				PendingIntent sent = createPendingResult(RC_SENT, data, PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent delivered = createPendingResult(RC_DELIVERY_REPORT, data, PendingIntent.FLAG_UPDATE_CURRENT);
				short smsPort = (short) R.integer.sms_port;
				smsManager.sendDataMessage(phone, null, smsPort, userData, sent, delivered);
			}
		} catch (Exception ex) {
			Utils.handle(ex, this);
		}
	}

	private Explosion createFireMessage() {
		final Explosion message = new Explosion();
		message.location = getAttackLocation();
		message.killZoneDiameter = Short.parseShort(killZoneDiameterText.getText().toString());
		message.warrningDiameter = Short.parseShort(warrningDiameterText.getText().toString());
		return message;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server, menu);
		return true;
	}

	private final AbstractLocationListener currentLocation = new AbstractLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			setLocation(location);
			stopGps();
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			Utils.toast("Accuracy: " + (int) location.getAccuracy() + "m", getApplicationContext());
		}
	};
	private Listener satelitelistener = new GpsStatus.Listener() {
		private GpsStatus status;

		@Override
		public void onGpsStatusChanged(int event) {
			status = gps.getGpsStatus(status);
			int used = 0;
			int max = 0;
			for (GpsSatellite sat : status.getSatellites()) {
				if (sat.usedInFix())
					used++;
				max++;
			}
			progressDialog.setProgress(used);
			progressDialog.setMessage("Acquiring location "+used+"/"+max);
		}
	};

	private ProgressDialog progressDialog = null;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_server_config:
			startActivity(new Intent(this, ConfigActivity.class));
			return true;
		case R.id.menu_server_send:
			broadcast(new ConfigMessage(this));
			return true;
		case R.id.menu_server_current_location: {
			gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, currentLocation);
			gps.addGpsStatusListener(satelitelistener);
			progressDialog = ProgressDialog.show(this, "GPS", "Acquiring location", false, true, new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					stopGps();
				}
			});
			progressDialog.setMax(5);
			return true;
		}
		case R.id.menu_server_numbers:
			startActivity(new Intent(this, NumbersActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void stopGps() {
		gps.removeUpdates(currentLocation);
		gps.removeGpsStatusListener(satelitelistener);
	}

}
