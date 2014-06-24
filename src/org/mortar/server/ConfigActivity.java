package org.mortar.server;

import org.mortar.client.Pref;
import org.mortar.client.Pref.Read;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.inputmethod.EditorInfo;

@SuppressWarnings("deprecation")
public class ConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private Read config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		config = new Pref.Read(this);
		for (Pref key : Pref.values()) {
			Preference item;
			if (key.type.equals(Boolean.class)) {
				item = new CheckBoxPreference(this);
			} else if (key.type.equals(Integer.class)) {
				EditTextPreference textItem = new EditTextPreference(this) {
					@Override
					protected boolean persistString(String value) {
						return persistInt(Integer.parseInt(value));
					}

					@Override
					protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
						setText(restoreValue ? "" + getPersistedInt(0) : (String) defaultValue);
					}
				};
				textItem.getEditText().setInputType(EditorInfo.TYPE_CLASS_NUMBER);
				item = textItem;
				item.setSummary("" + config.get(key));
			} else {
				throw new IllegalArgumentException("Unsupported type:" + key.type + " for " + key);
			}
			item.setKey(key.name());
			item.setTitle(key.name());
			screen.addPreference(item);
		}
		setPreferenceScreen(screen);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Pref cfg = Pref.valueOf(key);
		Preference pref = findPreference(key);
		if (cfg.type.equals(Integer.class))
			pref.setSummary("" + config.get(cfg));
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
}
