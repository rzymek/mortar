package org.mortar.client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, "GpsLog", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table location(lat real, lon real, timestamp integer)");
		db.execSQL("create table log(msg text, timestamp integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table location");
		db.execSQL("drop table log");
		onCreate(db);
	}

	public void put(Location location) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("lat", location.getLatitude());
		values.put("lon", location.getLongitude());
		values.put("timestamp", location.getTime());
		db.insert("location", null, values);
	}

	public Cursor getLog(int limit) {
		SQLiteDatabase db = getReadableDatabase();
		String q = "SELECT lat||' '||lon, timestamp FROM location UNION SELECT msg,timestamp FROM log ORDER BY timestamp desc LIMIT " + limit;
		return db.rawQuery(q, null);
	}

	public void put(String string) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("msg", string);
		values.put("timestamp", System.currentTimeMillis());
		db.insert("log", null, values);
	}

	public void reset() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("location", null, null);
		db.delete("log", null, null);
	}

	public Cursor getLocations() {
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = { "lat", "lon", "timestamp" };
		return db.query("location", columns, null, null, null, null, "timestamp");
	}
}
