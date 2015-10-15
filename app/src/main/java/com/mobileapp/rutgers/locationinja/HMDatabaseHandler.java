package com.mobileapp.rutgers.locationinja;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malabika on 10/15/2015.
 */
public class HMDatabaseHandler  extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static String HMDATABASE_NAME = "MyHMLocations.db",
            TABLE_HM ="heatmaplocations",
            KEY_HMID = "id",
            KEY_HMNAME = "nameofplace",
            KEY_HMLATI = "latitude",
            KEY_HMLONG = "longitude";



    public HMDatabaseHandler(Context context, String name,
                           SQLiteDatabase.CursorFactory factory, int version) {
        super(context, HMDATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HEATMAP_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_HM + "(" + KEY_HMID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_HMNAME + " TEXT NOT NULL," + KEY_HMLATI + " TEXT NOT NULL UNIQUE," + KEY_HMLONG + " TEXT NOT NULL UNIQUE" + ")";

//        Log.e("DATABSE HELPER", "the query is: " + CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_HEATMAP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HM);
        onCreate(db);
    }


    public void createHMLocation(checkedInLocation _checkedInLocation){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues _values = new ContentValues();
        _values.put(KEY_HMLATI, _checkedInLocation.get_latitude());
        _values.put(KEY_HMLONG, _checkedInLocation.get_longitude());
        db.insert(TABLE_HM, null, _values);

        db.close();
    }

    public void clearDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HM, null, null);
    }

    public int getLocationsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HM, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public List<checkedInLocation> getAllMyHMLocations(){

        List<checkedInLocation> myLocationList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_HM, null);
        if(cursor.moveToFirst())
        {
            do{
                checkedInLocation _cLocation = new checkedInLocation(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3));
                myLocationList.add(_cLocation);
            }
            while(cursor.moveToNext());
        }

        db.close();
        return myLocationList;
    }
}


