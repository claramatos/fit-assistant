package com.example.android.fitassistant.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.android.gms.awareness.state.Weather;

/**
 * Defines table and column names for the activities database.
 *
 * Created by Clara Matos on 01/05/2017.
 */
public class ActivityContract {

    private ActivityContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.fitassistant";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ACTIVITIES = "activities";

    public static final String CONTENT_LIST_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES;

    /* Inner class that defines the table contents of the activities table */
    public static class ActivityEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ACTIVITIES);

        public static final String TABLE_NAME = "activities";

        public static final String COLUMN_ACTIVITY_ID = BaseColumns._ID;

        // Activity name, stored as a string
        public static final String COLUMN_ACTIVITY_NAME = "name";
        // Activity type, stored as an int representing the activity id
        public static final String COLUMN_ACTIVITY_TYPE = "type";
        // Distance, stored as an int representing the distance in meters
        public static final String COLUMN_ACTIVITY_DISTANCE = "distance";
        // Duration, stored as long in milliseconds
        public static final String COLUMN_ACTIVITY_DURATION = "duration";
        // Time, stored as an string representing the date of the current activity
        // in "yyyy-MM-dd HH:mm:ss" format
        public static final String COLUMN_ACTIVITY_TIME = "time";
        // Weather type, stored as an int representing the weather id
        public static final String COLUMN_ACTIVITY_WEATHER = "weather";

        public static final int WALKING = 0;
        public static final int RUNNING = 1;
        public static final int BICYCLING = 2;
        public static final int UNKNOWN = 3;

        public static final int CONDITION_UNKNOWN = Weather.CONDITION_UNKNOWN;
        public static final int CONDITION_CLEAR = Weather.CONDITION_CLEAR;
        public static final int CONDITION_CLOUDY = Weather.CONDITION_CLOUDY;
        public static final int CONDITION_FOGGY = Weather.CONDITION_FOGGY;
        public static final int CONDITION_HAZY = Weather.CONDITION_HAZY;
        public static final int CONDITION_ICY = Weather.CONDITION_ICY;
        public static final int CONDITION_RAINY = Weather.CONDITION_RAINY;
        public static final int CONDITION_SNOWY = Weather.CONDITION_SNOWY;
        public static final int CONDITION_STORMY = Weather.CONDITION_STORMY;
        public static final int CONDITION_WINDY = Weather.CONDITION_WINDY;

        public static boolean isValidActivityType(Integer type) {
            if (type == WALKING || type == RUNNING || type == BICYCLING || type == UNKNOWN) {
                return true;
            }
            else return false;
        }

        public static boolean isValidWeather(Integer type) {
            if (type == CONDITION_UNKNOWN || type == CONDITION_CLEAR || type == CONDITION_CLOUDY
                    || type == CONDITION_FOGGY || type == CONDITION_HAZY || type == CONDITION_ICY
                    || type == CONDITION_RAINY || type == CONDITION_SNOWY || type == CONDITION_STORMY
                    || type == CONDITION_WINDY) {
                return true;
            }
            else return false;
        }
    }

}
