package com.mobileapp.rutgers.locationinja;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malabika on 10/10/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "MyLocations.db",
                        TABLE_DOCS = "checkedlocations",
                        KEY_ID = "id",
                        KEY_PLACENAME = "name",
                        KEY_LATITUDE = "latitude",
                        KEY_LONGITUDE = "longitude";


    public DatabaseHandler(Context context, String name,
                           SQLiteDatabase.CursorFactory factory, int version) {
            super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_DOCS + "(" + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+KEY_PLACENAME
                + " TEXT NOT NULL," + KEY_LATITUDE + " TEXT NOT NULL," + KEY_LONGITUDE + " TEXT NOT NULL" + ")";


//        Log.e("DATABSE HELPER", "the query is: " + CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCS);
        onCreate(db);
    }

    public void createLocation(checkedInLocation _checkedInLocation ){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLACENAME,_checkedInLocation.get_nameofplace());
        values.put(KEY_LATITUDE, _checkedInLocation.get_latitude());
        values.put(KEY_LONGITUDE, _checkedInLocation.get_longitude());

        db.insert(TABLE_DOCS, null, values);


    }

    public void clearDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DOCS, null, null);
    }

    public int getLocationsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DOCS, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public List<checkedInLocation> getAllMyLocations(){

        List<checkedInLocation> myLocationList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DOCS, null);
        if(cursor.moveToFirst())
        {
            do{
                checkedInLocation _cLocation = new checkedInLocation(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3));
                myLocationList.add(_cLocation);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return myLocationList;
    }

}
