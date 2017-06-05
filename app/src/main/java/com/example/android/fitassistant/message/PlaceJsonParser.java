package com.example.android.fitassistant.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to parse the places information provided in json format
 *
 * Created by Clara Matos on 29/04/2017.
 */
public class PlaceJsonParser {

    /**
     * Parse the results array of the jsonObject
     *
     * @param jsonObject
     * @return
     */
    public List<Place> parse(JSONObject jsonObject) {
        JSONArray placesArray = null;

        try {
            placesArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(placesArray);
    }

    /**
     * Parse each places object of the results array
     *
     * @param placesArray
     * @return list of the parsed places
     */
    private List<Place> getPlaces(JSONArray placesArray) {

        List<Place> placesList = new ArrayList<>();

        for (int i = 0; i < placesArray.length(); i++) {
            try {
                Place place = getPlace(placesArray.getJSONObject(i));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /**
     * Parse the information of each place object
     *
     * @param placeObject
     * @return Place instance created with the information of placeObject
     */
    private Place getPlace(JSONObject placeObject) {

        String placeName = "ND";
        String vicinity = "ND";
        String latitude = "";
        String longitude = "";
        String type = "";

        Place place = null;

        try {
            if (!placeObject.isNull("name")) {
                placeName = placeObject.getString("name");
            }

            if (!placeObject.isNull("vicinity")) {
                vicinity = placeObject.getString("vicinity");
            }

            latitude = placeObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = placeObject.getJSONObject("geometry").getJSONObject("location").getString("lng");

            if (placeObject.getJSONArray("types").length() > 0) {
                type = placeObject.getJSONArray("types").get(0).toString();
            }

            place = new Place.PlaceBuilder()
                    .placeName(placeName)
                    .vicinity(vicinity)
                    .latitude(latitude)
                    .longitude(longitude)
                    .placeType(type)
                    .build();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place;
    }
}