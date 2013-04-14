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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cleriotsimon.webtools.DBHandler;
import com.cleriotsimon.webtools.WebRequest;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pixellostudio.qqdroid.R.drawable;

/**
 * 
 * @author Cleriot Simon
 * 
 */
public class HomeActivity extends BaseActivity {
	List<String> items;
	List<String> descrs;
	List<Integer> icons;
	List<String> tags;
	List<Boolean> typesTitle;

	int nb = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, "913255144104");
		} else {
			Log.v(TAG, "Already registered : " + regId);
			/*
			 * pushidRequest = new SimpleTextRequest( );
			 * getSpiceManager().execute(pushidRequest, "register_id",
			 * DurationInMillis.NEVER, );
			 */
			WebRequest.get("http://qqdroid.mobi/api/register.php?gcm=" + regId,
					WebRequest.NEVER, new PushidRequestListener());
		}

		items = new ArrayList<String>();
		descrs = new ArrayList<String>();
		tags = new ArrayList<String>();
		icons = new ArrayList<Integer>();
		typesTitle = new ArrayList<Boolean>();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings.getBoolean("clear", false)) {
			Log.d("QQDROID", "REMOVE DATA FROM CACHE");
			DBHandler db = new DBHandler(this);
			db.removeCache();

			settings.edit().putBoolean("clear", false).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);

		WebRequest.get("http://qqdroid.mobi/api/websites.json",
				WebRequest.ONE_WEEK, new WebsitesRequestListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.settings: // SETTINGS
			i = new Intent(this, SettingsActivity.class);

			startActivity(i);
			return true;
		case R.id.share: // SHARE APPLICATION
			i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_TEXT, getText(R.string.sharetext));
			i.setType("text/plain");
			startActivity(Intent.createChooser(i,
					this.getText(R.string.sharemenu)));
		}

		return false;
	}

	public class WebsitesRequestListener extends AsyncHttpResponseHandler {

		@Override
		public void onFailure(Throwable e, String response) {
			Toast.makeText(HomeActivity.this, "failure", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onSuccess(String result) {
			Log.d("QQDROID", "Success 2");

			items.clear();
			typesTitle.clear();
			icons.clear();
			descrs.clear();
			tags.clear();
			nb = 0;

			JSONObject resp = null;
			try {
				resp = new JSONObject((String) result);

				JSONArray j = resp.getJSONArray("websites");
				for (int i = 0; i < j.length(); i++) {
					JSONObject obj = j.getJSONObject(i);

					int drawableId = 0;
					try {
						Class<drawable> res = R.drawable.class;
						Field field = res.getField(obj.getString("drawable"));
						drawableId = field.getInt(null);
					} catch (Exception e) {
						Log.e("FLIRTY", "Failure to get drawable id.", e);
					}

					addButton(obj.getString("name"), obj.getString("descr"),
							obj.getString("setting"), drawableId,
							obj.getString("lang"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (nb == 0) {
				items.add(String.valueOf(getText(R.string.nowebsite)));
				typesTitle.add(false);
				icons.add(0);
				descrs.add("");
				tags.add("settings");
			}

			ListView lv = (ListView) findViewById(R.id.list_websites);
			lv.setBackgroundColor(getResources()
					.getColor(android.R.color.black));
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if (tags.get(arg2).equals("settings")) {
						Intent i = new Intent(HomeActivity.this,
								SettingsActivity.class);
						startActivity(i);
					} else {
						if (!typesTitle.get(arg2)) {
							Intent a = new Intent(HomeActivity.this,
									DisplayActivity.class);
							a.putExtra("tag", tags.get(arg2));
							startActivity(a);
						}
					}
				}
			});
			lv.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
					R.layout.list_websites_item, items) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					LayoutInflater inflater = (LayoutInflater) HomeActivity.this
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.list_websites_item,
							null);

					if (!typesTitle.get(position)) {
						ImageView image = (ImageView) view
								.findViewById(R.id.icon);
						image.setImageResource(icons.get(position));

						TextView title = (TextView) view
								.findViewById(R.id.text1);
						title.setText(items.get(position));
						TextView descr = (TextView) view
								.findViewById(R.id.text2);
						descr.setText(descrs.get(position));

						title.setTextSize(20.0f);
						descr.setTextSize(15.0f);

						LinearLayout layout = (LinearLayout) view
								.findViewById(R.id.linearlayout);
						layout.setPadding(10, 10, 10, 10);
					} else {
						TextView title = (TextView) view
								.findViewById(R.id.text1);
						title.setText(items.get(position));
						TextView descr = (TextView) view
								.findViewById(R.id.text2);
						descr.setTextSize(0.0f);
						descr.setHeight(1);
						view.setBackgroundColor(Color.GRAY);
						title.setTextColor(Color.WHITE);
						title.setTextSize(15.0f);
						LinearLayout layout = (LinearLayout) view
								.findViewById(R.id.linearlayout);
						layout.setPadding(10, 5, 10, 5);
					}

					return view;
				}

			});

			setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
		}
	}

	private void addButton(String name, String descr, String setting,
			int resimg, String lang) {
		if (!lang.equals("")) {
			items.add(lang);
			typesTitle.add(true);
			icons.add(0);
			descrs.add("");
			tags.add("");
		}

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings.getBoolean(setting, false)) {

			items.add(name);
			icons.add(resimg);
			descrs.add(descr);
			typesTitle.add(false);
			tags.add(setting);

			nb++;
		}
	}

	public class PushidRequestListener extends JsonHttpResponseHandler {
		@Override
		public void onFailure(Throwable e, String response) {
			Toast.makeText(HomeActivity.this, "failure", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
