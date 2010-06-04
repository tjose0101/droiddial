/*
 * Copyright (c) Oliver Bye 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.xpdeveloper.dialer.common;

import java.text.MessageFormat;

import net.xpdeveloper.android.IIntentHelper;
import net.xpdeveloper.android.IntentHelper;
import net.xpdeveloper.dialer.common.service.ToneDialService;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

/**
 * I am the UI. Since my persistence is simple preferences these are managed by
 * my super class.
 * 
 * Reference: http://developer.android.com/reference/android/app/Activity.html#
 * ActivityLifecycle
 * 
 * @author byeo
 * 
 */
public class ToneDialActivity extends PreferenceActivity {
	public static final String ACTION_PREFERENCE_CHANGE = "net.xpdeveloper.dialer.PREFERENCE_CHANGE";
	public static final String ACTION_TONE_DIAL = "net.xpdeveloper.dialer.TONE_DIAL";

	public static final String EXTRA_COUNTRY_CODE = "net.xpdeveloper.dialer.EXTRA_COUNTRY_CODE";
	public static final String EXTRA_TRUNK_CODE = "net.xpdeveloper.dialer.EXTRA_TRUNK_CODE";
	public static final String PREF_CONTACTS = "net.xpdeveloper.dialer.PREF_CONTACTS";

	public static final String PREF_ENABLE_TONES = "enableTones";
	public static final String PREF_ENABLE_TONES_ONCE = "enableTonesOnce";
	public static final String TAG = "ToneDial";

	private MessageFormat _countrySummaryFormat, _trunkSummaryFormat;
	private CheckBoxPreference _enabledPreference;
	private IIntentHelper _intentHelper;
	private UpgradeStrategy _upgradeStrategy;
	public static final String PREF_UPGRADE = "net.xpdeveloper.dialer.PREF_UPGRADE";

	public ToneDialActivity(UpgradeStrategy upgradeStrategy) {
		_intentHelper = new IntentHelper(this);
		_upgradeStrategy = upgradeStrategy;
	}

	public void enableService(boolean enableTones) {
		Intent toneDial = new Intent(
				ToneDialService.ACTION_SERVICE_STATE_CHANGE);

		if (enableTones) {
			_intentHelper.startService(toneDial);
		} else {
			_intentHelper.stopService(toneDial);
		}
	}

	public boolean isServiceEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_ENABLE_TONES, false);
	}

	private void onCodeChange(Preference pref, MessageFormat format, Object code) {
		if (code.toString().length() == 0) {
			code = "nothing";
		}
		pref.setSummary(format.format(new Object[] { code }));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		_countrySummaryFormat = new MessageFormat(getText(
				R.string.summary_country_code_preference).toString());
		_trunkSummaryFormat = new MessageFormat(getText(
				R.string.summary_trunk_code_preference).toString());

		_enabledPreference = (CheckBoxPreference) findPreference(PREF_ENABLE_TONES);

		// Add listeners
		_enabledPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object value) {
						enableService((Boolean) value);
						return true; // means persist the value
					}
				});

		
		// Selectively disable preferences
		_upgradeStrategy.summarise(findPreference(PREF_ENABLE_TONES_ONCE), this);
		_upgradeStrategy.summarise(findPreference(PREF_UPGRADE), this);
		
		// Wire up the country and trunk codes
		OnPreferenceChangeListener countryCodeChange = new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object value) {
				onCodeChange(preference, _countrySummaryFormat, value);
				return true; // means persist the value
			}
		};

		OnPreferenceChangeListener trunkCodeChange = new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object value) {
				onCodeChange(preference, _trunkSummaryFormat, value);
				return true; // means persist the value
			}
		};

		EditTextPreference countryPreference = (EditTextPreference) findPreference(EXTRA_COUNTRY_CODE);
		countryPreference.setOnPreferenceChangeListener(countryCodeChange);
		onCodeChange(countryPreference, _countrySummaryFormat,
				countryPreference.getText());

		EditTextPreference trunkPreference = (EditTextPreference) findPreference(EXTRA_TRUNK_CODE);
		trunkPreference.setOnPreferenceChangeListener(trunkCodeChange);
		onCodeChange(trunkPreference, _trunkSummaryFormat, trunkPreference
				.getText());

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	@Override
	protected void onResume() {
		boolean enabled = isServiceEnabled();
		// If Service disabled tone dial after dial-once
		// TODO find better way for model to raise events for UI changes
		_enabledPreference.setChecked(enabled);
		enableService(enabled);

		super.onResume();
	}

	public void setIIntentHelper(IIntentHelper intentHelper) {
		_intentHelper = intentHelper;
	}
}