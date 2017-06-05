package com.example.android.fitassistant.message;

import com.google.android.gms.awareness.state.Weather;

/**
 * Created by Clara Matos on 14/05/2017.
 */
public class MessageWeather implements iMessage {

    /**
     * Weather object provided by the google awareness api
     */
    private Weather mWeatherInfo;
    private String mCityName;

    public MessageWeather(Weather weather, String cityName) {
        mWeatherInfo = weather;
        mCityName = cityName;
    }

    public int getTemperature(int units) {
        if (units == Weather.FAHRENHEIT || units == Weather.CELSIUS) {
            return Math.round(mWeatherInfo.getTemperature(units));
        } else return 0;
    }

    public int getHumidity() {
        return mWeatherInfo.getHumidity();
    }

    public int getCondition() {
        if (mWeatherInfo.getConditions().length > 0) {
            return mWeatherInfo.getConditions()[0];
        } else return Weather.CONDITION_UNKNOWN;
    }

    public String getCity() {
        return mCityName;
    }


    @Override
    public int getListItemType() {
        return WEATHER_MESSAGE;
    }
}
