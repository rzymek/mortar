package org.mortar.client.data;

import org.mortar.common.CoordinateConversion;
import org.mortar.common.CoordinateConversion.UTM;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, "GpsLog", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table log(utm text, timestamp integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void put(Location location) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		UTM utm = CoordinateConversion.INST.latLon2UTM(location.getLatitude(), location.getLongitude());
		values.put("utm", utm.toString());
		values.put("timestamp", location.getTime());
		db.insert("log", null, values);
	}

	public Cursor getAll() {
		SQLiteDatabase db = getReadableDatabase();
		String[] columns ={"utm","timestamp"};
		return db.query("log", columns, null, null, null, null, "timestamp desc");		
	}
}
