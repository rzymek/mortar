package org.mortar.client.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mortar.client.R;
import org.mortar.client.data.DBHelper;

import android.database.Cursor;
import android.os.Bundle;
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
		Cursor all = db.getAll();
		StringBuilder buf = new StringBuilder();
		SimpleDateFormat fmt = new SimpleDateFormat("MM.dd HH:mm:ss", Locale.ENGLISH);
		while (all.moveToNext()) {
			String utm = all.getString(0);
			long timestamp = all.getLong(1);
			buf.append(fmt.format(new Date(timestamp))).append(" ").append(utm).append("\n");
		}
		TextView logView = (TextView) findViewById(R.id.logView);
		logView.setText(buf);
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
		} else if (id == R.id.action_reset) {
			db.reset();
			refresh();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
