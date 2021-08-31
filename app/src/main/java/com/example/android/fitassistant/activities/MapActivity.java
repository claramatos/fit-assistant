package com.example.android.fitassistant.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Monitors the user activity and present he's location on map on real time
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    public static final String ACTIVITY_TYPE = "ACTIVITY_TYPE";

    private static final String TAG = "MapActivity";
    private static final int UPDATE_MILLIS = 1000;

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1000;
    private static final int REQUEST_LOCATION = 2;

    private static final float DEFAULT_ZOOM = 17;

    /**
     * location request parameters
     */
    private long UPDATE_INTERVAL = 15 * 1000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 sec */
    private float SMALLEST_DISPLACEMENT = 1.0f; /* 1m */

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Location mPreviousLocation;

    private long mTravelledDistance = 0;
    private TextView mDistanceTextView;
    private TextView mDurationTextView;
    private String mStartTimeStr;
    private long mStartTime;

    private boolean mIsButtonOnPlay = false;

    private int mCurrentActivityType;
    private int mCurrentWeather;

    private Handler mTimerHandler;

    // to listen to headphone state change
    private PendingIntent mPendingIntent;
    private HeadphoneFenceReceiver mHeadphoneFenceReceiver;
    private static final String HEADPHONE_FENCE_KEY = "HEADPHONE_FENCE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPlayServiceAvailable()) {
            setContentView(R.layout.activity_map);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Awareness.API)
                    .addConnectionCallbacks(this)
                    .build();

            Intent intent = getIntent();
            mCurrentActivityType = intent.getExtras().getInt(ACTIVITY_TYPE);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mDistanceTextView = (TextView) findViewById(R.id.distance);
            mDurationTextView = (TextView) findViewById(R.id.duration);

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_play);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!mIsButtonOnPlay) {
                        Log.i(TAG, "mIsButtonOnPlay: " + mIsButtonOnPlay);
                        mIsButtonOnPlay = true;

                        mStartTimeStr = Utils.getCurrentDate();
                        mStartTime = System.currentTimeMillis();
                        updateDuration();
                        fab.setImageResource(R.drawable.ic_stop);

                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showSettingsAlert();
                        }

                        updateWeather();
                        updateDistance();
                    } else {
                        mIsButtonOnPlay = false;
                        Log.i(TAG, "mIsButtonOnPlay: " + mIsButtonOnPlay);
                        fab.setImageResource(R.drawable.ic_play_arrow);

                        mTimerHandler.removeCallbacks(updater);
                        saveCurrentActivity();

                        mTravelledDistance = 0;
                        updateDistance();
                        resetWidgets();
                    }
                }
            });

            // create the intent to be notified when the headphone status changes
            mHeadphoneFenceReceiver = new HeadphoneFenceReceiver();
            Intent headphoneIntent = new Intent(HeadphoneFenceReceiver.FENCE_RECEIVER_ACTION);
            mPendingIntent = PendingIntent.getBroadcast(this, 1, headphoneIntent, 0);

        } else {
            setContentView(R.layout.activity_main);
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "Google mMap ready");
        this.mMap = map;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permission is granted start receiving location updates
                startLocationUpdates();
            } else {
                // permission was denied or request was cancelled
                Toast.makeText(this, "Location should be granted.", Toast.LENGTH_SHORT).show();
                finish();

            }
        }
    }

    private boolean isPlayServiceAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, status, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        // connect
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        // register for headphone state changes
        registerFence();
        registerReceiver(mHeadphoneFenceReceiver, new IntentFilter(HeadphoneFenceReceiver.FENCE_RECEIVER_ACTION));
    }

    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        // unregister for headphone state changes
        unregisterFence();
        unregisterReceiver(mHeadphoneFenceReceiver);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        // update widget ui
        resetWidgets();

        // romove location changes updates
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        // only stop if it's connected
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        // only remove if handler was created
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacks(updater);
        }
    }


    @Override
    public void onBackPressed() {
        // If the activity hasn't been started, continue with handling back button press
        if (!mIsButtonOnPlay) {
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

        // Show dialog asking if the current activity is to be discarded
        showDiscardActivityDialog(discardButtonClickListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;

        // if activity is being monitored update travelled distance
        if (mIsButtonOnPlay) {
            if (mCurrentLocation != null && mPreviousLocation != null) {
                mTravelledDistance += Math.round(mCurrentLocation.distanceTo(mPreviousLocation));
                updateDistance();
            }
        }

        zoomOnCurrentLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION
            );
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates");

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL).
                        setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        // check if fine location permission is available
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION
            );
        } else {
            // Request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mCurrentLocation = location;

            if (mMap != null) {
                // display current location on map
                mMap.setMyLocationEnabled(true);
            }

            zoomOnCurrentLocation();
        }
    }

    private void zoomOnCurrentLocation() {
        if (mGoogleApiClient.isConnected() && mCurrentLocation != null) {
            Log.i(TAG, "Lat: " + mCurrentLocation.getLatitude() + ", Lon: " + mCurrentLocation.getLongitude());
            LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULT_ZOOM);

            if (mMap != null)
                mMap.moveCamera(update);
        }
    }

    private void drawLine() {
        PolylineOptions options = new PolylineOptions()
                .add(new LatLng(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude()))
                .add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .color(R.color.colorSecondaryDark)
                .width(5);
        mMap.addPolyline(options);
    }

    private void updateDistance() {
        mDistanceTextView.setText(String.valueOf(mTravelledDistance));
    }

    private void updateWeather() {
        Log.i(TAG, "updateWeather");

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION
            );
        } else {

            Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<WeatherResult>() {
                        @Override
                        public void onResult(@NonNull WeatherResult weatherResult) {
                            if (!weatherResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not detect weather info");
                                return;
                            }
                            Weather weather = weatherResult.getWeather();
                            mCurrentWeather = weather.getConditions()[0];
                            Log.i(TAG, "mCurrentWeather: " + mCurrentWeather);
                        }
                    });
        }
    }

    /**
     * Duration text view is updated each second
     */
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - mStartTime;

            mTimerHandler.postDelayed(updater, UPDATE_MILLIS);
            String formattedTime = Utils.getTimeFormattedFromMillis(elapsedTime);
            mDurationTextView.setText(formattedTime);
            updateWidgets(formattedTime, String.valueOf(mTravelledDistance) + " m", mCurrentActivityType);
        }
    };

    private void updateDuration() {
        Log.i(TAG, "updateDuration");

        mTimerHandler = new Handler();
        mTimerHandler.post(updater);
    }

    /**
     * Check if any widget is available and if so update them with the information from the current activity
     */
    private void updateWidgets(String updatedDuration, String updateDistance, int activityType) {
        Log.i("CurrentActivityWidget", "updateWidgets");
        Intent intent = new Intent(this, CurrentActivityWidget.class);

        intent.putExtra(CurrentActivityWidget.ACTIVITY_TYPE_KEY, activityType);
        intent.putExtra(CurrentActivityWidget.DISTANCE_KEY, String.valueOf(updateDistance));
        intent.putExtra(CurrentActivityWidget.DURATION_KEY, updatedDuration);

        // get id of all availabe widgets
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), CurrentActivityWidget.class));

        intent.putExtra(CurrentActivityWidget.WIDGETID_KEY, ids);
        intent.setAction(CurrentActivityWidget.UPDATE_INFO_LABEL);

        sendBroadcast(intent);
    }

    /**
     * When activity monitoring is stopped, reset the info presented on the available widgets
     */
    private void resetWidgets() {
        updateWidgets(getString(R.string.duration_holder), getString(R.string.distance_holder), ActivityEntry.RUNNING);
    }

    /**
     * When activity monitoring is stopped, save the current info and send it to the EditorActivity to be reviewed
     */
    private void saveCurrentActivity() {
        Log.i(TAG, "saveCurrentActivity");
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(ActivityEntry.COLUMN_ACTIVITY_TYPE, mCurrentActivityType);
        intent.putExtra(ActivityEntry.COLUMN_ACTIVITY_TIME, mStartTimeStr);
        intent.putExtra(ActivityEntry.COLUMN_ACTIVITY_WEATHER, mCurrentWeather);
        intent.putExtra(ActivityEntry.COLUMN_ACTIVITY_DISTANCE, mTravelledDistance);
        intent.putExtra(ActivityEntry.COLUMN_ACTIVITY_DURATION, System.currentTimeMillis() - mStartTime);
        startActivity(intent);
    }

    /**
     * When a headphone plugged status is detected start music app if the user request it
     */
    private void startMusicApp() {
        Log.i(TAG, "startMusicApp");

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.music");
        startActivity(intent);
    }

    /**
     * When the activity monitoring starts and the gps is not on prompt the user to enable GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.settings_title);
        builder.setMessage(R.string.settings_dialog_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert SimpleTextMessage
        builder.show();
    }

    /**
     * If the user presses back after the activity monitoring starts ask if the user wants to
     * discard the current activity
     */
    private void showDiscardActivityDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.discard_activity_dialog_title);
        builder.setMessage(R.string.discard_activity_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.continue_activity, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Continue" button, so dismiss the dialog
                // and continue tracking the current activity.
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
     * When the headphone plugged in status is detected as the user if he wants to start the google
     * play app
     */
    public void showStartMusicAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.play_music_dialog_title);
        builder.setMessage(R.string.play_music_dialog_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startMusicApp();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert SimpleTextMessage
        builder.show();
    }

    private void registerFence() {
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(HEADPHONE_FENCE_KEY, headphoneFence, mPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence Registered");
                        } else {
                            Log.i(TAG, "Fence Not Registered");

                        }
                    }
                });
    }

    private void unregisterFence() {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(HEADPHONE_FENCE_KEY)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence Removed");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence Not Removed");
            }
        });
    }

    class HeadphoneFenceReceiver extends BroadcastReceiver {

        public static final String FENCE_RECEIVER_ACTION =
                "com.example.android.fitassistant.map.HeadphoneFenceReceiver.FENCE_RECEIVER_ACTION";

        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), HEADPHONE_FENCE_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        showStartMusicAppDialog();
                        break;
                    case FenceState.FALSE:
                        // do nothing
                        break;
                    case FenceState.UNKNOWN:
                        // do nothing
                        break;
                }
            }
        }
    }
}

