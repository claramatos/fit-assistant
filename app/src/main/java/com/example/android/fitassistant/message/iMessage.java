package com.example.android.fitassistant.message;

/**
 * Created by Clara Matos on 20/05/2017.
 */
public interface iMessage {

    int MY_MESSAGE = 0;
    int OTHER_MESSAGE = 1;
    int WEATHER_MESSAGE = 2;
    int PLACE_MESSAGE = 3;

    int getListItemType();
}
