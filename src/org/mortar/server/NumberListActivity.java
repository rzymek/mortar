package org.mortar.server;

import org.mortar.client.R;
import org.mortar.common.Utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NumberListActivity extends ActionBarActivity {
	public static enum DataKey {
		NUMBERS
	}
	private TextView numbersText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_list);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		numbersText  = (TextView) findViewById(R.id.numbersText);
		numbersText.setText(shared.getString(DataKey.NUMBERS.name(),""));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.number_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.number_list_save) {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
			Editor edit = shared.edit();
			edit.putString(DataKey.NUMBERS.name(), numbersText.getText().toString());
			edit.commit();
			Utils.toast("Saved", this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
