package com.example.android.fitassistant.message;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.android.fitassistant.activities.MapActivity;
import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.example.android.fitassistant.data.ActivityContract;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Displays the messages received and sent by the user
 */
public class ChatFragment extends Fragment implements SuggestionsRecyclerAdapter.SuggestionsClickListener,
        MessageRecyclerAdapter.DirectionsButtonClickListener {

    private static final String TAG = "ChatFragment";

    private static final String ZERO_RESULTS = "ZERO_RESULTS";
    private static final int MAX_PLACES_TO_SHOW = 3;
    private static final int REQUEST_LOCATION = 2;

    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;

    private RecyclerView mMessageRecyclerView;
    private RecyclerView.Adapter mMessageRecyclerViewAdapter;
    private RecyclerView.LayoutManager mMessageLayoutManager;
    private ArrayList<iMessage> mMessageList;

    private RecyclerView mSuggestionsRecyclerView;
    private RecyclerView.Adapter mSuggestionsRecyclerViewAdapter;
    private RecyclerView.LayoutManager mSuggestionsLayoutManager;
    private ArrayList<String> mSuggestionsList;

    private GoogleApiClient mGoogleApiClient;

    private double mLatitude = 0;
    private double mLongitude = 0;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .build();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mButtonSend = (FloatingActionButton) view.findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) view.findViewById(R.id.et_message);

        initMessageRecyclerView(view);

        // if there are no messages on the list greet the user
        if (mMessageList.isEmpty()) {
            greet();
        }

        // set the listener for the send button
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();

                // check if message is empty
                if (TextUtils.isEmpty(message)) {
                    return;
                }

                sendMessage(message);
                mEditTextMessage.setText("");
            }
        });

        initSuggestionsRecyclerView(view);

        return view;
    }

    private void initMessageRecyclerView(View view) {
        Log.i(TAG, "initMessageRecyclerView");

        if (mMessageList == null) {
            mMessageList = new ArrayList<iMessage>();
        }

        mMessageLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mMessageRecyclerView = (RecyclerView) view.findViewById(R.id.message_recycler_view);
        mMessageRecyclerView.setLayoutManager(mMessageLayoutManager);
        mMessageRecyclerViewAdapter = new MessageRecyclerAdapter(getContext(), mMessageList, this);
        mMessageRecyclerView.setAdapter(mMessageRecyclerViewAdapter);
    }

    private void initSuggestionsRecyclerView(View view) {
        Log.i(TAG, "initSuggestionsRecyclerView");

        // add a list of predefined questions the user can do to the assistant
        mSuggestionsList = new ArrayList<>();
        mSuggestionsList.add("Show me the current weather");
        mSuggestionsList.add("Show me nearby parks");
        mSuggestionsList.add("Show me nearby Gyms");
        mSuggestionsList.add("Let's go for a run!");
        mSuggestionsList.add("Let's go for a walk!");
        mSuggestionsList.add("Let's go for a bike ride!");

        mSuggestionsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mSuggestionsRecyclerView = (RecyclerView) view.findViewById(R.id.suggestions_recycler_view);
        mSuggestionsRecyclerView.setLayoutManager(mSuggestionsLayoutManager);
        mSuggestionsRecyclerViewAdapter = new SuggestionsRecyclerAdapter(mSuggestionsList, this);
        mSuggestionsRecyclerView.setAdapter(mSuggestionsRecyclerViewAdapter);
    }


    @Override
    public void onPause() {
        //mMessageList = messageAdapter.getValues();
        super.onPause();
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*if (mMessageList != null) {
            mMessageRecyclerViewAdapter = new MessageRecyclerAdapter(getContext(), mMessageList, this);
            mMessageRecyclerView.setAdapter(mMessageRecyclerViewAdapter);
        }*/
    }

    public void onStart() {
        super.onStart();

        // connect
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        super.onStop();

        // only stop if it's connected
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Get the last known location
     */
    private void updateLocation() {
        if (mGoogleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION
                );
            } else {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocation();
            } else {
                // Permission was denied or request was cancelled
                receiveMessage(getContext().getString(R.string.chat_location_error_message));
            }
        }
    }

    /**
     * Parses the message received to prompt the bot to start specific actions
     */
    private void parseMessage(String message) {
        Log.i(TAG, "isNetworkAvailable: " + isNetworkAvailable());
        if (isNetworkAvailable()) {
            if (message.toLowerCase().contains("park")) {
                showNearbyPlaces("park");
            } else if (message.toLowerCase().contains("gym")) {
                showNearbyPlaces("gym");
            } else if (message.toLowerCase().contains("weather")) {
                showWeather();
            } else if (message.toLowerCase().contains("run")) {
                startActivity(ActivityContract.ActivityEntry.RUNNING);
            } else if (message.toLowerCase().contains("walk")) {
                startActivity(ActivityContract.ActivityEntry.WALKING);
            } else if (message.toLowerCase().contains("bike")) {
                startActivity(ActivityContract.ActivityEntry.BICYCLING);
            } else {
                receiveMessage(getContext().getString(R.string.chat_error_message));
            }
        } else {
            // when no internet connection is available notify the user
            receiveMessage(getContext().getString(R.string.chat_no_internet_message));
            receiveMessage(getContext().getString(R.string.chat_connect_internet_message));
        }
    }

    /**
     * Send message to the bot
     */
    private void sendMessage(String message) {
        Log.i(TAG, "sendMessage: " + message);

        SimpleTextMessage chatMessage = new SimpleTextMessage(message, SimpleTextMessage.SENT_MESSAGE);
        addMessageToList(chatMessage);

        parseMessage(message);
    }

    /**
     * Receive the message sent by the bot
     */
    private void receiveMessage(String message) {
        Log.i(TAG, "receiveMessage: " + message);

        SimpleTextMessage chatMessage = new SimpleTextMessage(message, SimpleTextMessage.RECEIVED_MESSAGE);
        addMessageToList(chatMessage);
    }

    private void showLocation() {
        updateLocation();
        Log.i(TAG, "Lat: " + mLatitude + ", Lon: " + mLongitude);
        receiveMessage("Lat: " + mLatitude + ", Lon: " + mLongitude);
    }

    /**
     * Create the url to fetch the places info from google places
     */
    private void showNearbyPlaces(String type) {
        updateLocation();

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&rankby=distance");
        sb.append("&types=" + type);
        sb.append("&sensor=true");
        sb.append("&key=" + getString(R.string.api_key));

        PlacesAsyncTask task = new PlacesAsyncTask(sb.toString(), type);
        task.execute();
    }

    private void greet() {
        String name = Utils.getPreferredName(getContext());
        String salutMessage = "Hello";

        if (name != null && !name.isEmpty()) {
            salutMessage += ", " + name + "!";
        } else {
            salutMessage += "!";
        }

        receiveMessage(salutMessage);
    }

    /**
     * Get the current weather info by using the google awareness api
     */
    private void showWeather() {
        if (ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
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
                            String city = ""; // not implemented for now
                            MessageWeather messageWeather = new MessageWeather(weather, city);
                            receiveMessage(getString(R.string.chat_weather_message));
                            addMessageToList(messageWeather);
                        }
                    });
        }
    }

    /**
     * Start MapActivity on user request
     */
    private void startActivity(int activtyType) {
        receiveMessage(getContext().getString(R.string.chat_great_idea_message));
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.ACTIVITY_TYPE, activtyType);
        startActivity(intent);
    }

    private void addMessageToList(iMessage item) {
        mMessageList.add(item);
        mMessageRecyclerViewAdapter.notifyDataSetChanged();
        mMessageLayoutManager.scrollToPosition(mMessageList.size() - 1);
    }

    /**
     * Check if internet connection is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onItemClick(String message) {
        sendMessage(message);
    }


    @Override
    public void onDirectionsButtonClick(float lat, float lng) {
        launchGoogleMaps(lat, lng);
    }

    /**
     * Start google maps app when the user clicks the get direction on the places card
     *
     * @param lat the place to go latitude
     * @param lng the place to go longitude
     */
    private void launchGoogleMaps(float lat, float lng) {
        String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", lat, lng);
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * Download the places requested by the user from the google places api
     */
    private class PlacesAsyncTask extends AsyncTask<Void, Void, String> {

        private String downloadUrl;
        private String placeType;

        private PlacesAsyncTask(String url, String type) {
            downloadUrl = url;
            placeType = type;
        }

        @Override
        protected String doInBackground(Void... params) {

            URL url = createUrl(downloadUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "start PlacesAsyncTaskParser");
            Log.i(TAG, "result: " + result);

            JSONObject jsonObject = null;
            String status = "";
            try {
                jsonObject = new JSONObject(result);
                status = jsonObject.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (status.equals(ZERO_RESULTS)) {
                receiveMessage(getString(R.string.chat_no_nearby_places_message, placeType));
            } else {
                PlacesAsyncTaskParser parserTask = new PlacesAsyncTaskParser();
                parserTask.execute(result);
            }
        }
    }

    /**
     * Parse the json string returned by the PlacesAsyncTask
     */
    private class PlacesAsyncTaskParser extends AsyncTask<String, Void, List<Place>> {

        @Override
        protected List<Place> doInBackground(String... jsonData) {
            List<Place> placeList = null;
            PlaceJsonParser placeJsonParser = new PlaceJsonParser();

            try {
                JSONObject jsonObject = new JSONObject(jsonData[0]);

                placeList = placeJsonParser.parse(jsonObject);

            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }

            return placeList;
        }

        @Override
        protected void onPostExecute(List<Place> places) {
            if (places.size() > 0) {
                receiveMessage(getString(R.string.chat_nearby_places_message, places.get(0).getType()));
            }

            for (int i = 0; i < places.size() && i < MAX_PLACES_TO_SHOW; i++) {
                Place place = places.get(i);

                Log.i(TAG, place.toString());

                StringBuilder sb = new StringBuilder("http://maps.google.com/maps/api/staticmap?center=");
                sb.append(place.getLatitude() + "," + place.getLongitude());
                sb.append("&zoom=15&size=800x350&sensor=false");
                sb.append("&markers=color:blue%7Clabel:B%7C");
                sb.append(place.getLatitude() + "," + place.getLongitude());
                sb.append("&markers=color:red%7Blabel:A%7C");
                sb.append(mLatitude + "," + mLongitude);

                StaticMapAsyncTask task = new StaticMapAsyncTask(sb.toString(), place);
                task.execute();
            }
        }
    }

    /**
     * Download the static maps of the places requested by the user
     */
    private class StaticMapAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private String downloadUrl;
        private Place currentPlace;

        private StaticMapAsyncTask(String url, Place place) {
            downloadUrl = url;
            currentPlace = place;
        }

        @Override
        protected Bitmap doInBackground(String... stringURL) {
            Bitmap bmp = null;

            try {
                URL url = createUrl(downloadUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();

                bmp = BitmapFactory.decodeStream(is, null, options);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmp;
        }

        protected void onPostExecute(Bitmap bmp) {
            if (bmp != null) {
                Log.i(TAG, "download static maps");
                currentPlace.setBitMap(bmp);
                addMessageToList(currentPlace);
            }
        }
    }

    /**
     * AssyncTask helper methods
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("ERROR", "Error code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e("ERROR", "Error code: " + e.getMessage());
            // TODO: Handle the exception
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
