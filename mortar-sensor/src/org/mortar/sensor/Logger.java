package org.mortar.sensor;

import java.util.Date;

import org.mortar.common.CoordinateConversion;
import org.mortar.common.CoordinateConversion.UTM;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class Logger extends SQLiteOpenHelper {
	private static final long MIN_INTERVAL = 5/* sec */* 1000L;
	private static final float MIN_DISTANCE = 5/* m */;

	private Location lastSavedLocation = null;

	public Logger(Context context) {
		super(context, "GpsLog", null, 7);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table location(lat real, lon real, timestamp integer, accuracy real,"
				+ "altitude real, speed real, bearing real, sat integer, provider text)");
		db.execSQL("create table log(msg text, timestamp integer)");		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table location");
		db.execSQL("drop table log");
		onCreate(db);
	}

	public void put(Location location) {
		UTM utm = CoordinateConversion.INST.latLon2UTM(location.getLatitude(), location.getLongitude());
		long since = (new Date().getTime() - location.getTime()) / 1000;
		Log.d("LOC", location.getProvider() + ":" + utm + " (" + since + "sec) " + location.getProvider());

		if (lastSavedLocation != null) {
			if (lastSavedLocation.distanceTo(location) < MIN_DISTANCE)
				return;
			if (location.getTime() - lastSavedLocation.getTime() < MIN_INTERVAL)
				return;
		}
		lastSavedLocation = location;
		persist(location);
	}

	private void persist(Location location) {
		ContentValues values = new ContentValues();

		values.put("lat", location.getLatitude());
		values.put("lon", location.getLongitude());
		values.put("timestamp", location.getTime());
		values.put("accuracy", location.getAccuracy());
		values.put("altitude", location.getAltitude());
		values.put("speed", location.getSpeed());
		values.put("bearing", location.getBearing());
		values.put("sat", location.getExtras().getInt("satellites"));
		values.put("provider", location.getProvider());

		getWritableDatabase().insert("location", null, values);
	}

	public Cursor getLog(int limit) {
		SQLiteDatabase db = getReadableDatabase();
		String q = "SELECT 'acc:'||substr(accuracy,0,8)||',spd:'||substr(speed,0,5)||',sat:'||sat,"
				+ " timestamp FROM location UNION SELECT msg,timestamp FROM log ORDER BY timestamp desc LIMIT " + limit;
		return db.rawQuery(q, null);
	}

	public void log(String string) {
		ContentValues values = new ContentValues();
		values.put("msg", string);
		values.put("timestamp", System.currentTimeMillis());
		getWritableDatabase().insert("log", null, values);
		Log.i("LOG", string);
	}

	public void reset() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("location", null, null);
		db.delete("log", null, null);
	}

	public Cursor getLocations() {
		String[] columns = { "lat", "lon", "timestamp", "accuracy", "altitude", "speed", "bearing", "sat", "provider" };
		SQLiteDatabase db = getReadableDatabase();
		return db.query("location", columns, null, null, null, null, "timestamp");
	}

	public Cursor getMessages() {
		String[] columns = { "msg", "timestamp" };
		SQLiteDatabase db = getReadableDatabase();
		return db.query("log", columns, null, null, null, null, "timestamp");
	}
}
