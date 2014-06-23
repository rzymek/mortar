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

import org.mortar.client.R;
import org.mortar.client.data.DBHelper;
import org.mortar.common.Utils;

import android.annotation.SuppressLint;
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

	private DBHelper db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_log);

		if (savedInstanceState == null) {
			db = new DBHelper(this);
			refresh();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}

	private void refresh() {
		int limit = 500;
		Cursor all = db.getLog(limit);
		try {
			StringBuilder buf = new StringBuilder();
			SimpleDateFormat fmt = new SimpleDateFormat("MM.dd HH:mm:ss", Locale.ENGLISH);
			while (all.moveToNext()) {
				String loc = all.getString(0);
				long timestamp = all.getLong(1);
				buf.append(fmt.format(new Date(timestamp))).append(" ").append(loc).append("\n");
			}
			TextView logView = (TextView) findViewById(R.id.logView);
			logView.setText(buf);
		} finally {
			all.close();
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
		if (id == R.id.action_refresh) {
			refresh();
		} else if (id == R.id.action_export) {
			export();
		} else if (id == R.id.action_reset) {
			db.reset();
			refresh();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void export() {
		new AsyncTask<Void, Exception, File>() {

			@Override
			protected File doInBackground(Void... params) {
				try {
					return exportGPX();
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
			protected void onPostExecute(File gpx) {
				if (gpx == null)
					return;
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[] { "rzymek+mortar@gmail.com" });
				i.putExtra(Intent.EXTRA_SUBJECT, "mortar client log");
				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(gpx));
				startActivity(Intent.createChooser(i, "Send GPX..."));
			}
		}.execute();
	}

	@SuppressLint("WorldReadableFiles")
	private File exportGPX() throws IOException {
		Cursor all = db.getLocations();
		File outDir = new File(Environment.getExternalStorageDirectory(),"mortar");
		outDir.mkdirs();
		try {
			final SimpleDateFormat filenameFmt = new SimpleDateFormat("yyyyMMdd_HHmm'.gpx'", Locale.ENGLISH);
			final SimpleDateFormat nameFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm'.gpx'", Locale.ENGLISH);
			
			final SimpleDateFormat dateTimeFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
			final Date now = new Date();
			String filename = filenameFmt.format(now);
						
			File file = new File(outDir, filename);
			FileOutputStream stream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(stream);
			try {
				copyRawTemplateTo(R.raw.gpx_head, out, nameFmt.format(now));
				String trkpt = getRawTemplate(R.raw.gpx_trkpt);
				while (all.moveToNext()) {
					double lat = all.getDouble(0);
					double lon = all.getDouble(1);
					long timestamp = all.getLong(2);

					String dateTime = dateTimeFmt.format(new Date(timestamp));
					out.append(String.format(Locale.ENGLISH, trkpt, lat, lon, dateTime));
				}
				copyRawTemplateTo(R.raw.gpx_tail, out);
			} finally {
				out.close();
				stream.close();
			}
			return file;
		} finally {
			all.close();
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
			out.append(line);
		}
	}

}
