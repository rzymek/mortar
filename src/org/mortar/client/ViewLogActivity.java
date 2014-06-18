package org.mortar.client;

import java.util.Date;

import org.mortar.client.data.DBHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewLogActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_log);

		if (savedInstanceState == null) {
			refresh();
		}
	}

	private void refresh() {
		DBHelper db = new DBHelper(this);
		Cursor all = db.getAll();
		StringBuilder buf = new StringBuilder();
		while(!all.isLast()) {
			String utm = all.getString(0);
			long timestamp = all.getLong(1);
			buf.append(utm).append(" ").append(new Date(timestamp)).append("\n");
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



}
