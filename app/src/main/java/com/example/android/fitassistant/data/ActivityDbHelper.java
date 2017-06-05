package com.example.android.fitassistant.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;


/**
 * Manages a local database for activities data.
 *
 * Created by Clara Matos on 01/05/2017.
 */
public class ActivityDbHelper extends SQLiteOpenHelper {

    // If the database schema changes, increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Activities.db";

    public ActivityDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // database schema
        String SQL_CREATE_ACT_TABLE = "CREATE TABLE " + ActivityEntry.TABLE_NAME + " ("
                + ActivityEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ActivityEntry.COLUMN_ACTIVITY_NAME + " TEXT NOT NULL, "
                + ActivityEntry.COLUMN_ACTIVITY_TYPE + " INTEGER NOT NULL, "
                + ActivityEntry.COLUMN_ACTIVITY_WEATHER + " INTEGER, "
                + ActivityEntry.COLUMN_ACTIVITY_TIME + " TEXT NOT NULL,"
                + ActivityEntry.COLUMN_ACTIVITY_DISTANCE + " INTEGER NOT NULL, "
                + ActivityEntry.COLUMN_ACTIVITY_DURATION + " INTEGER NOT NULL); ";
        db.execSQL(SQL_CREATE_ACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
