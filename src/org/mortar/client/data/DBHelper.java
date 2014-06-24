package org.mortar.client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, "GpsLog", null, 6);
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
		String q = "SELECT substr(lat,0,10)||' '||substr(lon,0,10)||';'||sat,"
				+ " timestamp FROM location UNION SELECT msg,timestamp FROM log ORDER BY timestamp desc LIMIT " + limit;
		return db.rawQuery(q, null);
	}

	public void log(String string) {
		ContentValues values = new ContentValues();
		values.put("msg", string);
		values.put("timestamp", System.currentTimeMillis());
		getWritableDatabase().insert("log", null, values);
	}

	public void reset() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("location", null, null);
		db.delete("log", null, null);
	}

	public Cursor getLocations() {
		String[] columns = { "lat", "lon", "timestamp", "accuracy", "altitude", "speed", "bearing","provider" };
		SQLiteDatabase db = getReadableDatabase();
		return db.query("location", columns, null, null, null, null, "timestamp");
	}
}
