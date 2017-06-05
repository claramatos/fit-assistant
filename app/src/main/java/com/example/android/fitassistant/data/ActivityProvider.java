package com.example.android.fitassistant.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;

/**
 * Created by Clara Matos on 01/05/2017.
 */

public class ActivityProvider extends ContentProvider {

    public static final String TAG = ActivityProvider.class.getSimpleName();
    private ActivityDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ActivityDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case ACTIVITIES:
                cursor = database.query(ActivityEntry.TABLE_NAME, projection, selection, selectionArgs
                        , null, null, sortOrder);
                break;
            case ACTIVITY_ID:

                selection = ActivityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ActivityEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ACTIVITIES:
                return ActivityContract.CONTENT_LIST_TYPE;
            case ACTIVITY_ID:
                return ActivityContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES:
                return insertActivity(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a activity into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertActivity(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ActivityEntry.COLUMN_ACTIVITY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Activity requires a name");
        }

        // Check that the activity type is valid
        Integer type = values.getAsInteger(ActivityEntry.COLUMN_ACTIVITY_TYPE);
        if (type == null || !ActivityEntry.isValidActivityType(type)) {
            throw new IllegalArgumentException("Activity requires a valid type");
        }

        // Check that the weather type is valid
        Integer weather = values.getAsInteger(ActivityEntry.COLUMN_ACTIVITY_WEATHER);
        if (weather == null || !ActivityEntry.isValidWeather(weather)) {
            throw new IllegalArgumentException("Activity requires a valid weather");
        }

        // Check that the distance is valid
        Long distance = values.getAsLong(ActivityEntry.COLUMN_ACTIVITY_DISTANCE);
        if (distance == null || distance < 0) {
            throw new IllegalArgumentException("Activity requires a valid distance");
        }

        // Check that the duration is valid
        Long duration = values.getAsLong(ActivityEntry.COLUMN_ACTIVITY_DURATION);
        if (duration == null || duration < 0) {
            throw new IllegalArgumentException("Activity requires a valid duration");
        }

        // Check that the time is not null
        String time = values.getAsString(ActivityEntry.COLUMN_ACTIVITY_TIME);
        if (time == null) {
            throw new IllegalArgumentException("Activity requires a valid time");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(ActivityEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ActivityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ACTIVITY_ID:
                // Delete a single row given by the ID in the URI
                selection = ActivityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ActivityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ACTIVITIES:
                return updateActivity(uri, values, selection, selectionArgs);
            case ACTIVITY_ID: // update specific row
                selection = ActivityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateActivity(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update activities in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments.
     * Return the number of rows that were successfully updated.
     */
    private int updateActivity(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ActivityEntry.COLUMN_ACTIVITY_NAME)) {
            String name = values.getAsString(ActivityEntry.COLUMN_ACTIVITY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Activity requires a name");
            }
        }

        if (values.containsKey(ActivityEntry.COLUMN_ACTIVITY_TYPE)) {
            Integer type = values.getAsInteger(ActivityEntry.COLUMN_ACTIVITY_TYPE);
            if (type == null || !ActivityEntry.isValidActivityType(type)) {
                throw new IllegalArgumentException("Activity requires a valid type");
            }
        }

        if (values.containsKey(ActivityEntry.COLUMN_ACTIVITY_DISTANCE)) {
            Long distance = values.getAsLong(ActivityEntry.COLUMN_ACTIVITY_DISTANCE);
            if (distance == null || distance < 0) {
                throw new IllegalArgumentException("Activity requires a valid distance");
            }
        }

        if (values.containsKey(ActivityEntry.COLUMN_ACTIVITY_DURATION)) {
            Long duration = values.getAsLong(ActivityEntry.COLUMN_ACTIVITY_DURATION);
            Log.i(TAG, "duration: " + duration);
            if (duration == null || duration < 0) {
                throw new IllegalArgumentException("Activity requires a valid duration");
            }
        }

        if (values.containsKey(ActivityEntry.COLUMN_ACTIVITY_TIME)) {
            String time = values.getAsString(ActivityEntry.COLUMN_ACTIVITY_TIME);
            if (time == null) {
                throw new IllegalArgumentException("Activity requires a time");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsUpdated = db.update(ActivityEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }


    /**
     * URI matcher code for the content URI for the activities table
     */
    private static final int ACTIVITIES = 100;

    /**
     * URI matcher code for the content URI for a single activity in the activities table
     */
    private static final int ACTIVITY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ActivityContract.CONTENT_AUTHORITY, ActivityContract.PATH_ACTIVITIES, ACTIVITIES);
        sUriMatcher.addURI(ActivityContract.CONTENT_AUTHORITY, ActivityContract.PATH_ACTIVITIES + "/#", ACTIVITY_ID);
    }
}
