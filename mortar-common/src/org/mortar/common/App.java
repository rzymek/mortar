package org.mortar.common;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class App extends Application implements UncaughtExceptionHandler {
	protected Config.Read config;

	@Override
	public void onCreate() {
		super.onCreate();
		config = new Config.Read(this);

		Parse.initialize(this, "zh0WoCJmlUEZcyUcfcfJxsV0LzlWogRxT9eskueX", "sUr8eVDrVmKbf3vgkfBQ15O6eRPfT2nPGnekLVP2");
		setupParse();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	public void setupParse() {
		ParseInstallation inst = ParseInstallation.getCurrentInstallation();
		inst.put("channel", config.getString(Config.PUSH_CHANNEL));
		inst.saveInBackground();
	}


	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("APP", "unhandled", ex);
		Utils.handle(ex, this);
	}

}
