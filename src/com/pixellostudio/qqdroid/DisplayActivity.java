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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;

/**
 * 
 * @author Cleriot Simon
 * 
 */
public class DisplayActivity extends BaseActivity {
	private QuotesAdapter viewPagerAdapter;

	private SimpleTextRequest infosRequest;
	private SimpleTextRequest quotesRequest;

	String url = "";
	int minPage = 0;
	String[][] menus = new String[10][3];

	int menu = 0;
	int currentPage;
	Integer firstPage = null;

	ViewPager vp;

	String tag = "";
	String title = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slider);

		tag = getIntent().getExtras().getString("tag");

		infosRequest = new SimpleTextRequest(
				"http://qqdroid.mobi/api/infos.php?site=" + tag);
		quotesRequest = new SimpleTextRequest("");

		vp = (ViewPager) findViewById(R.id.vp);
		vp.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				currentPage = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
		getSpiceManager().execute(infosRequest, "infos" + tag,
				DurationInMillis.ONE_WEEK, new InfosRequestListener());
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menuquote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.refresh: // REFRESH
			setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
			vp.setAdapter(viewPagerAdapter);
			return true;
		}

		return false;
	}

	public class InfosRequestListener implements RequestListener<String> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(DisplayActivity.this, "failure", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onRequestSuccess(final String result) {
			JSONObject resp = null;
			List<String> titles = null;
			try {
				resp = new JSONObject((String) result);

				title = resp.getString("title");
				getSupportActionBar().setTitle(title);
				url = resp.getString("url");
				minPage = resp.getInt("minpage");

				titles = new ArrayList<String>();

				JSONArray j = resp.getJSONArray("pages");
				for (int i = 0; i < j.length(); i++) {
					JSONObject obj = j.getJSONObject(i);

					menus[i][0] = obj.getString("title");
					menus[i][1] = obj.getString("url");
					menus[i][2] = obj.getString("random");
					titles.add(obj.getString("title"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			SpinnerAdapter spinner = new ArrayAdapter<String>(
					DisplayActivity.this, android.R.layout.simple_list_item_1,
					titles);
			OnNavigationListener onNavigationListener = new OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int position,
						long itemId) {
					menu = position;
					vp.setAdapter(viewPagerAdapter);
					setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
					return true;
				}
			};

			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			getSupportActionBar().setListNavigationCallbacks(spinner,
					onNavigationListener);

			viewPagerAdapter = new QuotesAdapter();

			vp.setAdapter(viewPagerAdapter);
			vp.setOffscreenPageLimit(6);
		}
	}

	private class QuotesAdapter extends PagerAdapter {
		LayoutInflater li = (LayoutInflater) DisplayActivity.this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		@Override
		public int getCount() {
			if (menus[menu][2].equals("1"))
				return 1;
			else
				return 2000;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = li.inflate(R.layout.view_slider, null);

			ListView lv = (ListView) v.findViewById(R.id.list_quotes);

			quotesRequest = new SimpleTextRequest(parseUrl(
					url + menus[menu][1], position));
			long time = DurationInMillis.ONE_HOUR;
			if (menus[menu][2].equals("1"))
				time = DurationInMillis.NEVER;

			getSpiceManager().execute(quotesRequest,
					tag + menus[menu][0] + position, time,
					new QuotesRequestListener(lv));
			container.addView(v);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public class QuotesRequestListener implements RequestListener<String> {
		ListView lv;

		QuotesRequestListener(ListView v) {
			lv = v;
		}

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(DisplayActivity.this, "failure", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onRequestSuccess(final String result) {
			final List<String> quotes = new ArrayList<String>();

			String tab[] = result.split("<quote>");

			for (int i = 1; i < tab.length; i++) {
				String content = tab[i].split("</quote>")[0] + "\n";

				quotes.add(content);
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					DisplayActivity.this, android.R.layout.simple_list_item_1,
					quotes) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					SharedPreferences pref = PreferenceManager
							.getDefaultSharedPreferences(DisplayActivity.this);

					TextView txt = new TextView(this.getContext());

					txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float
							.parseFloat(pref.getString("policesize", "18")));

					txt.setText(Html.fromHtml(quotes.get(position)));

					if (pref.getString("design", "blackonwhite").equals(
							"blackonwhite")) {
						txt.setTextColor(Color.BLACK);
						txt.setBackgroundColor(Color.WHITE);
						/*
						 * txt.setBackgroundDrawable(this.getContext().getResources
						 * () .getDrawable(R.drawable.quote_gradient_white));
						 */
					} else if (pref.getString("design", "blackonwhite").equals(
							"whiteonblack")) {
						txt.setTextColor(Color.WHITE);
						txt.setBackgroundColor(Color.BLACK);
						/*
						 * txt.setBackgroundDrawable(this.getContext().getResources
						 * () .getDrawable(R.drawable.quote_gradient_black));
						 */
					}

					txt.setPadding(20, 20, 20, 20);

					return txt;
				}
			};

			lv.setAdapter(adapter);
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenu.ContextMenuInfo menuInfo) {
					menu.add(0, 1, 0, R.string.sharequote);
				}
			});

			setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
		}
	}

	public String parseUrl(String url, int page) {
		if (minPage == 1)
			page++;
		url = url.replace("*page*", String.valueOf(page));

		return url;
	}

	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			Spanned quote = Html.fromHtml(((String) ((ListView) vp.getChildAt(
					currentPage).findViewById(R.id.list_quotes))
					.getItemAtPosition(menuInfo.position)));

			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_TEXT, getText(R.string.quotefrom) + " "
					+ title + " : " + quote);
			i.setType("text/plain");
			startActivity(Intent.createChooser(i, this.getText(R.string.share)));

			break;
		default:
			// return super.onContextItemSelected(item);
		}
		return true;
	}
}
