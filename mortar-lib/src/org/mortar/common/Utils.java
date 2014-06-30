package org.mortar.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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

	public static void handle(Throwable ex, Context context) {
		Log.e("!!!", "error", ex);
		Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
	}

	public static void toast(String string, Context ctx) {
		Toast.makeText(ctx, string, Toast.LENGTH_LONG).show();
	}

	public static String n(String string) {
		return string == null ? "" : string;
	}

	@SuppressLint("DefaultLocale")
	public static String getSystemInfo() {
		return String.format("Android %s-%s (SDK: %d), kernel %s\n%s %s %s (%s)[%s]",
				android.os.Build.VERSION.CODENAME, android.os.Build.VERSION.RELEASE, android.os.Build.VERSION.SDK_INT,
				System.getProperty("os.version"), android.os.Build.MANUFACTURER, android.os.Build.BRAND,
				android.os.Build.MODEL, android.os.Build.PRODUCT, android.os.Build.HARDWARE);
	}

	public static String getStackString(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.toString();
	}
	public static String toString(Bundle bundle) {
		StringBuilder buf = new StringBuilder();
		Set<String> keys = bundle.keySet();
		for (String key : keys) {
			buf.append(key).append(":").append(bundle.get(key)).append(", ");
		}
		return buf.toString();
	}
}
