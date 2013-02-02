/*
 * This file is part of QQDroid.

 * QQDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * QQDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with QQDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pixellostudio.qqdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * 
 * @author Cleriot Simon
 * 
 */
public class GCMIntentService extends GCMBaseIntentService {
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e("GCM", arg1);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d("FLIRTY", "intent : " + intent.getExtras().toString());
		if (intent.getExtras().getString("type").equals("clear")) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			settings.edit().putBoolean("clear", true).commit();
		} else if (intent.getExtras().getString("type").equals("msg")) {
			Intent intent2 = new Intent("new-msg");
			intent2.putExtra("msg", intent.getExtras().getString("msg"));
			intent2.putExtra("url", intent.getExtras().getString("url"));
			intent2.putExtra("time", intent.getExtras().getString("time"));
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
		}

	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.d("GCM", "Registered");
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
	}
}