package org.mortar.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utils {

	public static String toUTF8(byte[] userData) {
		try {
			return new String(userData, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return new String(userData);
		}
	}

	public static void handle(IOException ex, Context context) {
		Log.e("!!!", "error", ex);
		Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
	}

	public static void toast(String string, Context ctx) {
		Toast.makeText(ctx, string, Toast.LENGTH_LONG).show();
	}

}
