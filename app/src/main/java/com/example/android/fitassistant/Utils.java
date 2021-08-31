package com.example.android.fitassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;
import com.google.android.gms.awareness.state.Weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Clara Matos on 25/04/2017.
 */
public class Utils {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_FORMAT_DISPLAY = "h:mm a";
    private static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    private static final String DATE_TIME_FORMAT_DISPLAY = "dd/MM/yyyy h:mm a";
    private static final String DURATION_FORMAT_DISPLAY_HOURS = "HH:mm:ss";
    private static final String DURATION_FORMAT_DISPLAY = "mm:ss";

    private static final long MAX_METERS = 1000;
    private static final long HOUR_IN_MILLIS = 3600000;

    /**
     * Get user name saved on shared preferences
     * @param context of the current activity
     * @return the user name
     */
    public static String getPreferredName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_name_key),
                context.getString(R.string.pref_name_default));
    }

    /**
     * Get the default temperature units system saved on shared preferences
     * @param context of the current activity
     * @return true if the temperature units system is metric
     */
    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    /**
     * Gets the current date in "yyyy-MM-dd HH:mm:ss" format
     *
     * @return the date on the give format
     */
    public static String getCurrentDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();

        return timeFormat.format(cal.getTime());
    }

    /**
     * Parses a given date in String format to Calendar
     *
     * @param theDate date to be parsed in String format
     * @param format to parse the date
     * @return the calendar instance created based on the date provided
     * @throws ParseException
     */
    public static Calendar stringToCalendar(String theDate, String format) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat(format);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        cal.setTime(timeFormat.parse(theDate));

        Log.i("EditorActivity", "sdf.parse(theDate): " + timeFormat.parse(theDate));

        return cal;
    }

    /**
     * Gets the Calendar instance of the duration in "HH:mm:ss" format or "mm:ss" format
     *
     * @param duration
     * @return the calendar instance
     */
    public static Calendar getCalendarFromDuration(String duration) {
        Calendar cal = Calendar.getInstance();

        try {
            cal = stringToCalendar(duration, Utils.DURATION_FORMAT_DISPLAY_HOURS);
            return cal;
        } catch (Exception e) {

        }

        try {
            cal = Utils.stringToCalendar(duration, Utils.DURATION_FORMAT_DISPLAY);
            return cal;
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    /**
     * Get date and time String formatted in "month, day, h:mm a" format
     *
     * @param rawDate
     * @return the date and time on the given format
     */
    public static String getDateTimeFormatted(String rawDate) {
        Calendar cal = null;
        try {
            cal = stringToCalendar(rawDate, DATE_TIME_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_DISPLAY);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String time = timeFormat.format(cal.getTime());

        return month + " " + Integer.toString(day) + ", " + time;
    }

    /**
     * Get date String formatted in "yyyy-MM-dd HH:mm:ss" format
     *
     * @param rawDate
     * @return the date on the given format
     */
    public static String getDateFormatted(String rawDate) {
        Calendar cal = null;
        try {
            cal = stringToCalendar(rawDate, DATE_TIME_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return timeFormat.format(cal.getTime());
    }

    /**
     * Get time String formatted in "h:mm a" format
     *
     * @param rawDate
     * @return the time on the given format
     */
    public static String getTimeFormatted(String rawDate) {
        Calendar cal = null;
        try {
            cal = stringToCalendar(rawDate, DATE_TIME_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_DISPLAY);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return timeFormat.format(cal.getTime());
    }

    /**
     * Converts time and date String into "yyyy-MM-dd HH:mm:ss"
     *
     * @param time
     * @param date
     * @return the String converted on the given format
     */
    public static String getStringFromTimeAndDate(String time, String date) {
        Calendar cal = null;
        try {
            cal = stringToCalendar(date + " " + time, DATE_TIME_FORMAT_DISPLAY);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        //timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return timeFormat.format(cal.getTime());
    }

    /**
     * Get time formatted in "HH:mm:ss" format or "mm:ss" (depending if the duration also
     * includes hours or not)
     *
     * @param millis
     * @return the String on the correct format
     */
    public static String getTimeFormattedFromMillis(long millis) {
        SimpleDateFormat timeFormat;

        if (millis > HOUR_IN_MILLIS) {
            timeFormat = new SimpleDateFormat(DURATION_FORMAT_DISPLAY_HOURS);
        } else {
            timeFormat = new SimpleDateFormat(DURATION_FORMAT_DISPLAY);
        }

        return timeFormat.format(new Date(millis));
    }

    /**
     * Get distance formatted in km or meters depending if it is above 1 kmm
     *
     * @param dist
     * @return the string on the correct format
     */
    public static String getDistanceFormatted(long dist) {
        if (dist > MAX_METERS) {
            return String.valueOf(Math.round(dist / MAX_METERS)) + " km";
        } else return String.valueOf(dist) + " m";
    }

    /**
     * Convert duration String into long in milliseconds
     *
     * @param duration
     * @return the duration in milliseconds
     */
    public static long getDurationInMillis(String duration) {
        Calendar cal = getCalendarFromDuration(duration);
        long durationInMillis = cal.getTimeInMillis();

        if (durationInMillis > 0){
            return durationInMillis;
        }
        else {
            return 0;
        }
    }

    /**
     * Get the drawable code of the activity type associated to each activity type
     *
     * @param type the activity code
     * @return the drawable code associated with the activity type on the drawable folder
     */
    public static int getActivityImageId(int type) {
        switch (type) {
            case ActivityEntry.RUNNING:
                return R.drawable.ic_directions_run;
            case ActivityEntry.WALKING:
                return R.drawable.ic_directions_walk;
            case ActivityEntry.BICYCLING:
                return R.drawable.ic_directions_bike;
            default:
                return R.drawable.ic_directions_run;
        }
    }

    /**
     * Get the drawable id of the weather condition associated to each weather type
     *
     * @param weather the weather condition id
     * @return the drawable id associated with the weather condition on the drawable folder
     */
    public static int getConditionImageId(int weather) {
        switch (weather) {
            case ActivityEntry.CONDITION_CLEAR:
                return R.drawable.sunny;
            case ActivityEntry.CONDITION_CLOUDY:
                return R.drawable.cloudy;
            case ActivityEntry.CONDITION_FOGGY:
                return R.drawable.haze;
            case ActivityEntry.CONDITION_HAZY:
                return R.drawable.haze;
            case ActivityEntry.CONDITION_ICY:
                return R.drawable.snow;
            case ActivityEntry.CONDITION_RAINY:
                return R.drawable.drizzle;
            case ActivityEntry.CONDITION_SNOWY:
                return R.drawable.snow;
            case ActivityEntry.CONDITION_STORMY:
                return R.drawable.thunderstorms;
            case ActivityEntry.CONDITION_WINDY:
                return R.drawable.slight_drizzle;
            default:
                return R.drawable.sunny;

        }
    }

    /**
     * Get the string id of the weather condition associated to each weather type
     *
     * @param condition the weather condition id
     * @return the string id associated with the weather condition on the strings.xml file
     */
    public static int getConditionString(int condition) {
        switch (condition) {
            case Weather.CONDITION_CLEAR:
                return R.string.type_clear;
            case Weather.CONDITION_CLOUDY:
                return R.string.type_cloudy;
            case Weather.CONDITION_FOGGY:
                return R.string.type_foggy;
            case Weather.CONDITION_HAZY:
                return R.string.type_hazy;
            case Weather.CONDITION_ICY:
                return R.string.type_icy;
            case Weather.CONDITION_RAINY:
                return R.string.type_rainy;
            case Weather.CONDITION_SNOWY:
                return R.string.type_snowy;
            case Weather.CONDITION_STORMY:
                return R.string.type_stormy;
            case Weather.CONDITION_WINDY:
                return R.string.type_windy;
            default:
                return R.string.type_unknown;
        }
    }


}
