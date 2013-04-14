package com.cleriotsimon.webtools;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pixellostudio.qqdroid.MyApplication;

public class WebRequest {
	public static long NEVER = 0;
	public static long ONE_SECOND = 1000;
	public static long ONE_MINUTE = 60 * ONE_SECOND;
	public static long ONE_HOUR = 60 * ONE_MINUTE;
	public static long ONE_DAY = 24 * ONE_HOUR;
	public static long ONE_WEEK = 7 * ONE_DAY;

	private static AsyncHttpClient client = new AsyncHttpClient();
	private static DBHandler db = new DBHandler(MyApplication.getAppContext());

	public static void get(String url, long maxTime,
			AsyncHttpResponseHandler responseHandler) {

		String content = db.getContent(url);
		if (content == null) {
			DefaultRequestListener listener = new DefaultRequestListener(
					responseHandler, maxTime, url);
			client.get(url, null, listener);
		} else {
			Log.d("QQDROID", "LOLILOL");
			responseHandler.onSuccess(content);
		}
	}

}
