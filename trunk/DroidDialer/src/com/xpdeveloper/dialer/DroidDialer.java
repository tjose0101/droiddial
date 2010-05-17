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
package com.xpdeveloper.dialer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * 
 * @author byeo
 * 
 */
public class DroidDialer extends Activity {
	private static final String PREF_ENABLE_TONES = "enableTones";
	private static boolean _tonesEnabled = false; // TODO move this into a
													// service
	public static String PREFS_NAME = "DroidDailer";
	private RadioGroup _enableGroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_enableGroup = (RadioGroup) findViewById(R.id.RadioGroupEnable);
		_enableGroup.check(checkedButtonId(getTonesEnabled()));
		_enableGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				if (checkedId == R.id.RadioButtonTone) {
					setTonesEnabled(true);
				}
				setTonesEnabled(false);
			}
		});

		Button contactsButton = (Button) findViewById(R.id.button_contacts);
		contactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Contacts.CONTENT_URI);
				startActivity(intent);
			}
		});
	}

	private int checkedButtonId(boolean tonesEnabled) {
		if (tonesEnabled) {
			return R.id.RadioButtonTone;
		}
		return R.id.RadioButtonPhone;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void setTonesEnabled(boolean enableTones) {
		_tonesEnabled = enableTones;
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_ENABLE_TONES, enableTones);
		editor.commit();
	}

	public boolean getTonesEnabled() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		_tonesEnabled = settings.getBoolean(PREF_ENABLE_TONES, true);
		return _tonesEnabled;
	}

	public static boolean areTonesEnabled() {
		return _tonesEnabled;
	}
}