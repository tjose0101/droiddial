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
package net.xpdeveloper.dialer.common.service;

import net.xpdeveloper.dialer.common.R;
import net.xpdeveloper.dialer.common.ToneDialActivity;
import net.xpdeveloper.dialer.common.model.DialMemento;
import net.xpdeveloper.dialer.common.model.IToneDialModel;
import net.xpdeveloper.dialer.common.model.ToneDialModel;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * I am started by the Launcher Activity when tone dialing is enabled.
 * 
 * I register a NewOutgoingCallBroadcastReciver to notify me when a number is
 * dialed. I capture this iteration and send a dial command to my model
 */
public class ToneDialService extends Service {
	public static final String ACTION_DIAL = "net.xpdeveloper.dialer.DIAL";
	public static final String ACTION_SERVICE_STATE_CHANGE = "net.xpdeveloper.dialer.SERVICE_STATE_CHANGE";

	private static final int TONE_DIAL_SERVICE_TICKER_ID = 1;

	private NewOutgoingCallBroadcastReceiver _receiver;
	private IToneDialModel _model;
	private boolean _stopped = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int flags) {

		if (intent != null && _stopped == false) {
			String action = intent.getAction();
			if (ACTION_DIAL.equals(action)) {
				Uri data = Uri.parse(intent.getDataString());
				String originalDestination = data
						.getEncodedSchemeSpecificPart();
				toneDial(originalDestination);

				if (shouldDialOnlyOnce()) {
					_stopped = true;

					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(this);
					Editor editor = prefs.edit();
					editor
							.putBoolean(ToneDialActivity.PREF_ENABLE_TONES,
									false);
					editor.commit();

					stopSelf();
				}
			} else if (ACTION_SERVICE_STATE_CHANGE.equals(action)) {
				displayNotification(getText(R.string.ticker_tone_dial_on),
						getText(R.string.notification_text));
			} else {
				// ignore it
				_stopped = true;
				stopSelf();
			}
		}
	}

	private boolean shouldDialOnlyOnce() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getBoolean(ToneDialActivity.PREF_ENABLE_TONES_ONCE, false);
	}

	/**
	 * 
	 * @param originalDestination
	 * @return the number actually dialled
	 */
	public void toneDial(String originalDestination) {
		try {
			DialMemento memento= _model.localise(originalDestination);
			String dialString = memento.getDialString();
			
			displayNotification(
					getText(R.string.ticker_tone_dial) + dialString,
					getText(R.string.notification_text_tone_dial) + dialString);
			
			// Now we can make the noise, or the notification is delayed
			memento.dial();

		} catch (InterruptedException e) {
			Log.e(ToneDialActivity.TAG, "Unable to generate DTMF tones", e);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_model = new ToneDialModel(PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));
		manageRegistration(true);
	}

	@Override
	public void onDestroy() {
		cancelNotification();
		manageRegistration(false);
		_model.release();
		super.onDestroy();
	}

	public void setModel(IToneDialModel model) {
		_model = model;
	}

	/**
	 * http://developer.android.com/reference/android/content/Intent.html#
	 * ACTION_NEW_OUTGOING_CALL Register/Unregister for
	 * android.intent.action.NEW_OUTGOING_CALL Category:
	 * android.intent.category.ALTERNATIVE
	 * 
	 * @param enableTones
	 */
	private void manageRegistration(boolean enableTones) {
		if (enableTones) {
			if (_receiver == null) {
				_receiver = new NewOutgoingCallBroadcastReceiver();
			}

			registerReceiver(_receiver, new IntentFilter(
					Intent.ACTION_NEW_OUTGOING_CALL));
		} else {
			if (_receiver != null) {
				unregisterReceiver(_receiver);
				_receiver = null; // It's not usable anymore
			}
		}
	}

	/**
	 * http://developer.android.com/guide/topics/ui/notifiers/notifications.html
	 * 
	 * @param contentText
	 *            TODO
	 * @param contentText
	 *            TODO
	 */
	private void displayNotification(CharSequence tickerText,
			CharSequence contentText) {
		int icon = R.drawable.stat_service;

		Notification notification = new Notification(icon, tickerText, 0);
		// We will cancel this notification
		notification.flags |= Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;

		// Setup the pending intent to launch the UI
		Intent launchToneDialActivity = new Intent(ToneDialActivity.ACTION_TONE_DIAL);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				launchToneDialActivity, 0);

		// Build the notifications
		Context context = getApplicationContext();
		CharSequence contentTitle = getText(R.string.notification_title);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		// Raise the notification
		NotificationManager notificationManager = notificationManager();
		notificationManager.notify(TONE_DIAL_SERVICE_TICKER_ID, notification);
	}

	private NotificationManager notificationManager() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
		return notificationManager;
	}

	private void cancelNotification() {
		notificationManager().cancelAll();
	}
}
