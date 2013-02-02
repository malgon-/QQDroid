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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.octo.android.robospice.SpiceManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 
 * @author Cleriot Simon
 * 
 */
public class BaseActivity extends SherlockActivity {
	protected String TAG = "QQDroid";
	private SpiceManager spiceManager = new SpiceManager(RoboSpiceService.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter("new-msg"));

		getSupportActionBar().setLogo(R.drawable.home);

		spiceManager.start(this);
	}

	@Override
	protected void onStart() {
		if (!spiceManager.isStarted())
			spiceManager.start(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	protected SpiceManager getSpiceManager() {
		return spiceManager;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return (true);

		}
		return true;
	}

	@SuppressLint("ResourceAsColor")
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			Style s = new Style.Builder()
					.setDuration(
							Integer.valueOf(intent.getExtras()
									.getString("time")))
					.setBackgroundColor(android.R.color.white)
					.setTextColor(android.R.color.black).build();

			Crouton.makeText(BaseActivity.this,
					Html.fromHtml(intent.getExtras().getString("msg")), s)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent browserIntent = new Intent(
									Intent.ACTION_VIEW, Uri.parse(intent
											.getExtras().getString("url")));
							startActivity(browserIntent);
							Crouton.cancelAllCroutons();
						}
					}).show();
			Log.d("receiver", "New message");
		}
	};
}
