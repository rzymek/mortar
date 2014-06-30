package org.mortar.cannon.activities;

import org.mortar.cannon.R;
import org.mortar.common.App;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NumbersActivity extends ActionBarActivity {
	public static final String EXTRA_NUMBERS = "numbers";
	private TextView numbersText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler((App)getApplication());
		setContentView(R.layout.activity_number_list);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		numbersText = (TextView) findViewById(R.id.numbersText);
		numbersText.setText(shared.getString(EXTRA_NUMBERS, ""));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.numbers, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.number_list_save) {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
			Editor edit = shared.edit();
			edit.putString(EXTRA_NUMBERS, numbersText.getText().toString());
			edit.commit();
			setResult(RESULT_OK);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
