package com.example.android.fitassistant.activities;

import android.app.DatePickerDialog;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.fitassistant.MainActivity;
import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;

/**
 * Allows user to create a new activity, edit an existing one or review one that has just been performed
 */
public class EditorActivity extends AppCompatActivity implements
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditorActivity";
    private static final int URL_LOADER = 0;

    private EditText mNameEditText;
    private Spinner mActivityTypeSpinner;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private EditText mDurationEditText;
    private EditText mDistanceEditText;
    private Spinner mWeatherSpinner;
    private RelativeLayout mBottomLayout;

    private int mActivityType = 0;
    private int mActivityWeather = 0;

    Uri mCurrentActivityUri;

    /**
     * true if the current instance is on "review" mode
     */
    boolean mReviewActivity = false;

    /**
     * if any of the input fields was changed mActivityHasChanged is set to true
     */
    private boolean mActivityHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mActivityHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentActivityUri = intent.getData();

        mBottomLayout = (RelativeLayout) findViewById(R.id.save_activity);
        mNameEditText = (EditText) findViewById(R.id.activity_name);
        mDateTextView = (TextView) findViewById(R.id.txt_date);
        mTimeTextView = (TextView) findViewById(R.id.txt_time);
        mActivityTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        mDurationEditText = (EditText) findViewById(R.id.ed_txt_duration);
        mDistanceEditText = (EditText) findViewById(R.id.ed_txt_distance);
        mWeatherSpinner = (Spinner) findViewById(R.id.weather_spinner);

        setupTypeSpinner();
        setupWeatherSpinner();

        if (mCurrentActivityUri == null) {

            if (intent.getExtras() != null) { // review mode
                mReviewActivity = true;
                setTitle(getString(R.string.editor_activity_title_review_activity));
                Log.i(TAG, "review");
                fillActivityInfo(intent);
            } else { // add mode
                Log.i(TAG, "add");
                setTitle(getString(R.string.editor_activity_title_new_activity));
                invalidateOptionsMenu();
            }
        } else { // edit mode
            Log.i(TAG, "edit");
            setTitle(getString(R.string.editor_activity_title_edit_activity));
            getLoaderManager().initLoader(URL_LOADER, null, this);
        }

        mBottomLayout.setOnClickListener(this);
        mDateTextView.setOnClickListener(this);
        mTimeTextView.setOnClickListener(this);
        mDurationEditText.setOnClickListener(this);

        mNameEditText.setOnTouchListener(mTouchListener);
        mDateTextView.setOnTouchListener(mTouchListener);
        mTimeTextView.setOnTouchListener(mTouchListener);
        mActivityTypeSpinner.setOnTouchListener(mTouchListener);
        mDurationEditText.setOnTouchListener(mTouchListener);
        mDistanceEditText.setOnTouchListener(mTouchListener);
    }

    private void setupTypeSpinner() {
        ArrayAdapter activityTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.activities_array, android.R.layout.simple_spinner_item);

        activityTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mActivityTypeSpinner.setAdapter(activityTypeSpinnerAdapter);

        mActivityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_running))) {
                        mActivityType = ActivityEntry.RUNNING; // Running
                    } else if (selection.equals(getString(R.string.type_walking))) {
                        mActivityType = ActivityEntry.WALKING; // Walking
                    } else if (selection.equals(getString(R.string.type_bicycling))) {
                        mActivityType = ActivityEntry.BICYCLING; // Bicycling
                    } else {
                        mActivityType = ActivityEntry.UNKNOWN; // Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mActivityType = 0; // Unknown
            }
        });
    }

    private void setupTypeSpinner(int type) {
        switch (type) {
            case ActivityEntry.WALKING:
                mActivityTypeSpinner.setSelection(0);
                break;
            case ActivityEntry.RUNNING:
                mActivityTypeSpinner.setSelection(1);
                break;
            case ActivityEntry.BICYCLING:
                mActivityTypeSpinner.setSelection(2);
                break;
            default:
                mActivityTypeSpinner.setSelection(0);
                break;
        }
    }

    private void setupWeatherSpinner() {
        ArrayAdapter weatherSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.weather_array, android.R.layout.simple_spinner_item);
        weatherSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mWeatherSpinner.setAdapter(weatherSpinnerAdapter);
        mWeatherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_clear))) {
                        mActivityWeather = ActivityEntry.CONDITION_CLEAR; // Clear
                    } else if (selection.equals(getString(R.string.type_cloudy))) {
                        mActivityWeather = ActivityEntry.CONDITION_CLOUDY; // Cloudy
                    } else if (selection.equals(getString(R.string.type_foggy))) {
                        mActivityWeather = ActivityEntry.CONDITION_FOGGY; // Foggy
                    } else if (selection.equals(getString(R.string.type_hazy))) {
                        mActivityWeather = ActivityEntry.CONDITION_HAZY; // Hazy
                    } else if (selection.equals(getString(R.string.type_icy))) {
                        mActivityWeather = ActivityEntry.CONDITION_ICY; // Icy
                    } else if (selection.equals(getString(R.string.type_rainy))) {
                        mActivityWeather = ActivityEntry.CONDITION_RAINY; // Rainy
                    } else if (selection.equals(getString(R.string.type_snowy))) {
                        mActivityWeather = ActivityEntry.CONDITION_SNOWY; // Snowy
                    } else if (selection.equals(getString(R.string.type_stormy))) {
                        mActivityWeather = ActivityEntry.CONDITION_STORMY; // Stormy
                    } else if (selection.equals(getString(R.string.type_windy))) {
                        mActivityWeather = ActivityEntry.CONDITION_WINDY; // Windy
                    } else {
                        mActivityWeather = ActivityEntry.CONDITION_UNKNOWN; // Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mActivityType = 0; // Unknown
            }
        });
    }

    private void setupWeatherSpinner(int weather) {
        switch (weather) {
            case ActivityEntry.CONDITION_CLEAR:
                mWeatherSpinner.setSelection(0);
                break;
            case ActivityEntry.CONDITION_CLOUDY:
                mWeatherSpinner.setSelection(1);
                break;
            case ActivityEntry.CONDITION_FOGGY:
                mWeatherSpinner.setSelection(2);
                break;
            case ActivityEntry.CONDITION_HAZY:
                mWeatherSpinner.setSelection(3);
                break;
            case ActivityEntry.CONDITION_ICY:
                mWeatherSpinner.setSelection(4);
                break;
            case ActivityEntry.CONDITION_RAINY:
                mWeatherSpinner.setSelection(5);
                break;
            case ActivityEntry.CONDITION_SNOWY:
                mWeatherSpinner.setSelection(6);
                break;
            case ActivityEntry.CONDITION_STORMY:
                mWeatherSpinner.setSelection(7);
                break;
            case ActivityEntry.CONDITION_WINDY:
                mWeatherSpinner.setSelection(8);
                break;
            default:
                mWeatherSpinner.setSelection(0);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // If the activity hasn't changed, continue with handling back button press
        if (!mActivityHasChanged && !mReviewActivity) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public void finish() {
        if (mReviewActivity) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            super.finish();
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");

        if (v == mDateTextView) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            mDateTextView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        }

                    }, year, month, day);
            datePickerDialog.show();
        }

        if (v == mTimeTextView) {
            final Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            String AM_PM;
                            if (hourOfDay < 12) {
                                AM_PM = "AM";
                            } else {
                                AM_PM = "PM";
                            }

                            mTimeTextView.setText(hourOfDay + ":" + minute + " " + AM_PM);
                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        }

        if (v == mBottomLayout) {
            saveActivity();
            finish();
        }

        if (v == mDurationEditText) {
            showInsertDurationDialog();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new activity, hide the "Delete" menu item.
        if (mCurrentActivityUri == null && !mReviewActivity) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // confirm if the user wants to delete current activity
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mActivityHasChanged && !mReviewActivity) {
                    // Navigate back to parent activity
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }

                };
                // if changes were done or if the activity is on review mode ask the user if
                // the current changes should be discarded
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Loader manager methods
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = new String[]{ActivityEntry.COLUMN_ACTIVITY_ID,
                ActivityEntry.COLUMN_ACTIVITY_NAME,
                ActivityEntry.COLUMN_ACTIVITY_TIME,
                ActivityEntry.COLUMN_ACTIVITY_TYPE,
                ActivityEntry.COLUMN_ACTIVITY_DURATION,
                ActivityEntry.COLUMN_ACTIVITY_DISTANCE,
                ActivityEntry.COLUMN_ACTIVITY_WEATHER};


        return new CursorLoader(
                this,   // Parent activity context
                mCurrentActivityUri,        // Table to query
                projection,     // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_NAME);
            int timeColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_TIME);
            int typeColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_TYPE);
            int durationColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_DURATION);
            int distanceColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_DISTANCE);
            int weatherColumnIndex = cursor.getColumnIndex(ActivityEntry.COLUMN_ACTIVITY_WEATHER);

            String name = cursor.getString(nameColumnIndex);
            String time = cursor.getString(timeColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int weather = cursor.getInt(weatherColumnIndex);
            long duration = cursor.getLong(durationColumnIndex);
            long distance = cursor.getLong(distanceColumnIndex);

            mNameEditText.setText(name);
            mTimeTextView.setText(Utils.getTimeFormatted(time));
            mDateTextView.setText(Utils.getDateFormatted(time));
            mDurationEditText.setText(Utils.getTimeFormattedFromMillis(duration));
            mDistanceEditText.setText(String.valueOf(distance));

            setupTypeSpinner(type);
            setupWeatherSpinner(weather);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mTimeTextView.setText(getString(R.string.time_holder));
        mDateTextView.setText(getString(R.string.date_holder));
        mDurationEditText.setText(getString(R.string.duration_holder));
        mDistanceEditText.setText(getString(R.string.distance_holder));
        mActivityTypeSpinner.setSelection(0);
        mWeatherSpinner.setSelection(0);
    }

    /**
     * If the activity is on review mode fill the edit fields with the information sent by the MapActivity
     */
    private void fillActivityInfo(Intent intent) {
        String dateTimeStr = intent.getExtras().getString(ActivityEntry.COLUMN_ACTIVITY_TIME);
        String date = Utils.getDateFormatted(dateTimeStr);
        String time = Utils.getTimeFormatted(dateTimeStr);
        int type = intent.getExtras().getInt(ActivityEntry.COLUMN_ACTIVITY_TYPE);
        int weather = intent.getExtras().getInt(ActivityEntry.COLUMN_ACTIVITY_WEATHER);
        long duration = intent.getExtras().getLong(ActivityEntry.COLUMN_ACTIVITY_DURATION);
        long distance = intent.getExtras().getLong(ActivityEntry.COLUMN_ACTIVITY_DISTANCE);

        Log.i(TAG, "type: " + type);
        setupTypeSpinner(type);
        setupWeatherSpinner(weather);
        mNameEditText.setText(getSuggestedActivityName());
        mDateTextView.setText(date);
        mTimeTextView.setText(time);
        mDurationEditText.setText(Utils.getTimeFormattedFromMillis(duration));
        mDistanceEditText.setText(Long.toString(distance));
    }

    /**
     * Database operations
     */
    private void saveActivity() {
        String actName = mNameEditText.getText().toString().trim();
        String date = mDateTextView.getText().toString().trim();
        String time = mTimeTextView.getText().toString().trim();
        String durationStr = mDurationEditText.getText().toString().trim();
        String distanceStr = mDistanceEditText.getText().toString().trim();

        if (mCurrentActivityUri == null && TextUtils.isEmpty(actName) && TextUtils.isEmpty(date)
                && TextUtils.isEmpty(time) && TextUtils.isEmpty(durationStr)
                && TextUtils.isEmpty(distanceStr) && mActivityType == ActivityEntry.UNKNOWN) {
            return;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ActivityEntry.COLUMN_ACTIVITY_NAME, actName);
        values.put(ActivityEntry.COLUMN_ACTIVITY_TYPE, mActivityType);

        long duration = 0;
        if (!TextUtils.isEmpty(durationStr)) {
            duration = Utils.getDurationInMillis(durationStr);
        }
        values.put(ActivityEntry.COLUMN_ACTIVITY_DURATION, duration);


        long distance = 0;
        if (!TextUtils.isEmpty(distanceStr)) {
            distance = Long.parseLong(distanceStr);
        }
        values.put(ActivityEntry.COLUMN_ACTIVITY_DISTANCE, distance);

        String dateAndTime = Utils.getStringFromTimeAndDate(time, date);

        values.put(ActivityEntry.COLUMN_ACTIVITY_TIME, dateAndTime);

        values.put(ActivityEntry.COLUMN_ACTIVITY_WEATHER, mActivityWeather);

        if (mCurrentActivityUri == null) {
            Uri newUri = getContentResolver().insert(ActivityEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.act_not_saved), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.act_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentActivityUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_act_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_act_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteActivity() {
        if (mCurrentActivityUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentActivityUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_act_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_act_successful), Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    /**
     * Get suggested activity name to fill the activity name edit text
     */
    private String getSuggestedActivityName() {
        Calendar cal = Calendar.getInstance();

        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        Log.i(TAG, "hourOfDay: " + hourOfDay);

        String partOfDay = "";

        if (hourOfDay <= 12 && hourOfDay > 0) {
            partOfDay = getString(R.string.morning);
        } else if (hourOfDay > 12 && hourOfDay < 20) {
            partOfDay = getString(R.string.afternoon);
        } else {
            partOfDay = getString(R.string.night);
        }

        switch (mActivityType) {
            case ActivityEntry.WALKING:
                return partOfDay + " " + getString(R.string.walk);
            case ActivityEntry.RUNNING:
                return partOfDay + " " + getString(R.string.run);
            case ActivityEntry.BICYCLING:
                return partOfDay + " " + getString(R.string.bike);
            default:
                return null;
        }

    }


    /**
     * If the user presses back a discard dialog is presented to prevent information loss
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // continue editing the activity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * When the user clicks the delete button on the options menu a confirm delete
     * dialog is presented
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // delete the activity.
                deleteActivity();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // continue editing the activity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * To edit duration a custom dialog was created to ensure that the duration was inserted
     * on the correct format
     */
    private void showInsertDurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_insert_duration, null);
        final EditText hoursEt = (EditText) view.findViewById(R.id.hh);
        final EditText minutesEt = (EditText) view.findViewById(R.id.mm);
        final EditText secondsEt = (EditText) view.findViewById(R.id.ss);

        String durationStr = mDurationEditText.getText().toString();
        if (!durationStr.isEmpty()) {
            Calendar cal = Utils.getCalendarFromDuration(durationStr);

            hoursEt.setText(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
            minutesEt.setText(String.valueOf(cal.get(Calendar.MINUTE)));
            secondsEt.setText(String.valueOf(cal.get(Calendar.SECOND)));
        }

        builder.setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String hours = hoursEt.getText().toString();
                        String minutes = minutesEt.getText().toString();
                        String seconds = secondsEt.getText().toString();

                        if (minutes.isEmpty()) {
                            minutes = "00";
                        }

                        if (seconds.isEmpty()) {
                            seconds = "00";
                        }

                        if (minutes.length() == 1) {
                            minutes = "0" + minutes;
                        }

                        if (seconds.length() == 1) {
                            seconds = "0" + seconds;
                        }

                        if (hours.isEmpty()) {
                            mDurationEditText.setText(minutes + ":" + seconds);
                        } else {

                            if (hours.length() == 1) {
                                hours = "0" + hours;
                            }

                            mDurationEditText.setText(hours + ":" + minutes + ":" + seconds);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
