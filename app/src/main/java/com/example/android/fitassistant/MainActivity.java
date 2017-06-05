package com.example.android.fitassistant;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.fitassistant.activities.ActivitiesFragment;
import com.example.android.fitassistant.activities.EditorActivity;
import com.example.android.fitassistant.data.ActivityContract;
import com.example.android.fitassistant.activities.SingleChoiceDialogFragment;
import com.example.android.fitassistant.message.ChatFragment;


public class MainActivity extends AppCompatActivity implements ActivitiesFragment.OnButtonSelectedListener {

    private static final String TAG = "MainActivity";

    private static final String ACTIVITIES_FRAGMENT_TAG = "ActivitiesFragment";
    private static final String CHAT_FRAGMENT_TAG = "ChatFragment";
    private static final String SINGLE_CHOICE_FRAGMENT_TAG = "SingleChoiceFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            ActivitiesFragment activitiesFragment = new ActivitiesFragment();
            activitiesFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, activitiesFragment, ACTIVITIES_FRAGMENT_TAG)
                    .commit();
        }


    }

    /**
     * when the "talk" button is clicked start the chat fragment to begin the interaction with the
     * assistant
     */
    @Override
    public void onChatButtonClick() {

        Fragment chatFragment = getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG);

        // only create a new chat fragment if one has not been created
        if (chatFragment == null) {
            Log.i(TAG, "replace fragment");

            ChatFragment cf = new ChatFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, cf, CHAT_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.i(TAG, "show fragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatFragment, CHAT_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * when the "+" button is clicked show dialog to choose activity type and start MapActivity
     */
    @Override
    public void onStartButtonClick() {
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment();
        dialog.show(getSupportFragmentManager(), SINGLE_CHOICE_FRAGMENT_TAG);
    }

    /**
     * when the "pencil" button is clicked start EditorActivity in add new activity mode
     */
    @Override
    public void onAddButtonClick() {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ChatFragment) {
            Fragment activitiesFragment = getSupportFragmentManager().findFragmentByTag(ACTIVITIES_FRAGMENT_TAG);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, activitiesFragment, ACTIVITIES_FRAGMENT_TAG)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.insert_dummy_data:
                insertActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  To insert a dummy activity into to db (for visualization purposes)
     */
    private void insertActivity() {
        Log.i(TAG, "insertActivity");

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_NAME, "Morning walk");
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_TYPE, ActivityContract.ActivityEntry.RUNNING);
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_DISTANCE, 1000);
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_DURATION, 16000);
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_TIME, Utils.getCurrentDate());
        values.put(ActivityContract.ActivityEntry.COLUMN_ACTIVITY_WEATHER, ActivityContract.ActivityEntry.CONDITION_CLOUDY);

        Uri newUri = getContentResolver().insert(ActivityContract.ActivityEntry.CONTENT_URI, values);
    }

}
