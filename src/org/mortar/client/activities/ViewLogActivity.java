package org.mortar.client.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mortar.client.App;
import org.mortar.client.GPSListenerService;
import org.mortar.client.R;
import org.mortar.client.data.LocationLogger;
import org.mortar.common.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewLogActivity extends ActionBarActivity {
	/**
	 * Approximation factor for calculating Horizontal Dilution of Precision
	 * from location.getAccuracy(). location.getAccuracy() returns an accuracy
	 * measured in meters, and HDOP is obtained by dividing accuracy by this
	 * factor. The value is totally false (!), but is still useful for certain
	 * use case like track display in JOSM. See:
	 * http://code.google.com/p/osmtracker-android/issues/detail?id=15
	 */
	public final static int HDOP_APPROXIMATION_FACTOR = 4;
	private LocationLogger logger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_log);

		if (savedInstanceState == null) {
			logger = ((App) getApplication()).logger;
			refresh();
		}
	}

	private void refresh() {
		int limit = 500;
		Cursor cursor = logger.getLog(limit);
		try {
			StringBuilder buf = new StringBuilder();
			SimpleDateFormat fmt = new SimpleDateFormat("MM.dd HH:mm:ss", Locale.ENGLISH);
			while (cursor.moveToNext()) {
				String msg = cursor.getString(0);
				long timestamp = cursor.getLong(1);
				String date = fmt.format(new Date(timestamp));
				buf.append(date).append(" ").append(msg).append("\n");
			}
			TextView logView = (TextView) findViewById(R.id.logView);
			logView.setText(buf);
		} finally {
			cursor.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_log, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_refresh:
				refresh();
				break;
			case R.id.action_export:
				export();
				break;
			case R.id.action_reset:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Mortar");
				builder.setMessage(R.string.reset_log);
				builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						logger.reset();
						refresh();
					}
				});
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.create().show();
				break;
			case R.id.menu_view_quit:
				stopService(new Intent(this, GPSListenerService.class));
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private class Logs {
		File gpx;
		CharSequence log;
	}

	private void export() {
		new AsyncTask<Void, Exception, Logs>() {
			@Override
			protected Logs doInBackground(Void... params) {
				try {
					Logs logs = new Logs();
					logs.gpx = exportGPX();
					logs.log = getFullMsgLog();
					return logs;
				} catch (Exception ex) {
					publishProgress(ex);
					return null;
				}
			}

			@Override
			protected void onProgressUpdate(Exception... values) {
				Utils.handle(values[0], ViewLogActivity.this);
			}

			@Override
			protected void onPostExecute(Logs logs) {
				if (logs == null) {
					return;
				}
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[] {
					"rzymek+mortar@gmail.com"
				});
				i.putExtra(Intent.EXTRA_SUBJECT, "mortar client log");
				i.putExtra(Intent.EXTRA_TEXT, logs.log);
				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logs.gpx));
				startActivity(Intent.createChooser(i, "Send GPX..."));
			}
		}.execute();
	}

	protected CharSequence getFullMsgLog() throws IOException {
		Cursor cursor = logger.getMessages();
		StringBuilder out = new StringBuilder(cursor.getCount() * 50);
		try {
			final SimpleDateFormat dateTimeFmt = new SimpleDateFormat("HH:mm:ss'('MM.dd')' ", Locale.ENGLISH);
			while (cursor.moveToNext()) {
				String msg = cursor.getString(0);
				long timestamp = cursor.getLong(1);
				String dateTime = dateTimeFmt.format(new Date(timestamp));
				out.append(dateTime).append(msg).append('\n');
			}
			return out;
		} finally {
			cursor.close();
		}
	}

	@SuppressLint("WorldReadableFiles")
	private File exportGPX() throws IOException {
		Cursor cursor = logger.getLocations();
		File outDir = new File(Environment.getExternalStorageDirectory(), "mortar");
		outDir.mkdirs();
		try {
			final SimpleDateFormat filenameFmt = new SimpleDateFormat("yyyyMMdd_HHmm'.gpx'", Locale.ENGLISH);
			final SimpleDateFormat nameFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ENGLISH);

			final SimpleDateFormat dateTimeFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
			final Date now = new Date();
			String filename = filenameFmt.format(now);

			File file = new File(outDir, filename);
			FileOutputStream stream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(stream);
			try {
				copyRawTemplateTo(R.raw.gpx_head, out, nameFmt.format(now));
				String trkpt = getRawTemplate(R.raw.gpx_trkpt);
				while (cursor.moveToNext()) {
					double lat = cursor.getDouble(0);
					double lon = cursor.getDouble(1);
					long timestamp = cursor.getLong(2);
					float accuracy = cursor.getFloat(3);
					float altitude = cursor.getFloat(4);
					float speed = cursor.getFloat(5);
					float bearing = cursor.getFloat(6);
					int sat = cursor.getInt(7);
					String provider = cursor.getString(8);

					String dateTime = dateTimeFmt.format(new Date(timestamp));
					out.append(String.format(Locale.ENGLISH, trkpt, lat, lon, dateTime, altitude, speed, bearing,
							accuracy, sat, provider));
				}
				copyRawTemplateTo(R.raw.gpx_tail, out);
			} finally {
				out.close();
				stream.close();
			}
			return file;
		} finally {
			cursor.close();
		}
	}

	private String getRawTemplate(int resId) throws IOException {
		StringWriter out = new StringWriter();
		copyRawTemplateTo(resId, out);
		return out.toString();
	}

	private void copyRawTemplateTo(int resId, Writer out, Object... args) throws IOException {
		InputStream in = getResources().openRawResource(resId);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			if (args.length > 0) {
				line = String.format(line, args);
			}
			out.append(line).append("\n");
		}
	}

}
