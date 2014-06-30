package org.mortar.cannon.activities;

import org.mortar.cannon.R;
import org.mortar.common.App;
import org.mortar.common.Config;
import org.mortar.common.Config.Read;
import org.mortar.common.Utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

@SuppressWarnings("deprecation")
public class ConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private Read config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler((App)getApplication());
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		config = new Config.Read(this);
		for (final Config key : Config.values()) {
			Preference item = createPreference(key);
			item.setKey(key.name());
			item.setTitle(key.name());
			screen.addPreference(item);
		}
		setPreferenceScreen(screen);
	}

	private Preference createPreference(final Config key) {
		if (key.type.equals(Boolean.class)) {
			CheckBoxPreference item = new CheckBoxPreference(this) {
				@Override
				protected Object onGetDefaultValue(TypedArray a, int index) {
					return a.getBoolean(index, (boolean) key.defValue);
				}
			};
			item.setChecked(config.is(key));
			return item;
		} else if (key.type.equals(Integer.class)) {
			EditTextPreference item = new EditTextPreference(this) {
				@Override
				protected boolean persistString(String value) {
					try {
						return persistInt(Integer.parseInt(value));
					} catch (NumberFormatException ex) {
						return false;
					}
				}

				@Override
				protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
					setText(restoreValue ? "" + getPersistedInt(0) : (String) defaultValue);
				}

				@Override
				protected Object onGetDefaultValue(TypedArray a, int index) {
					return a.getInteger(index, (int) key.defValue);
				}
			};
			item.getEditText().setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			item.setSummary("" + config.get(key));
			return item;
		} else if (key.type.equals(String.class)) {
			EditTextPreference item = new EditTextPreference(this);
			item.setSummary("" + config.getString(key));
			return item;
		} else {
			throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String keyName) {
		Config key = Config.valueOf(keyName);
		Preference pref = findPreference(keyName);
		if (key.type.equals(Integer.class)) {
			pref.setSummary("" + config.get(key));
		} else if (key.type.equals(String.class)) {
			pref.setSummary("" + config.getString(key));
		} else if (key.type.equals(Boolean.class)) {
			CheckBoxPreference check = (CheckBoxPreference) pref;
			boolean val = config.is(key);
			check.setChecked(val);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_config_reset) {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
			Editor edit = shared.edit();
			edit.clear();
			edit.commit();
			Config[] values = Config.values();
			for (Config key : values) {
				onSharedPreferenceChanged(shared, key.name());
			}
			Utils.toast("Reset", this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
