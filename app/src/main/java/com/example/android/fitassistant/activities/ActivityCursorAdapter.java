package com.example.android.fitassistant.activities;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;

/**
 * Exposes a list of previous recorded activities
 *
 * Created by Clara Matos on 01/05/2017.
 */

public class ActivityCursorAdapter extends CursorAdapter {

    public ActivityCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the activity data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvActivityName = (TextView) view.findViewById(R.id.name);
        TextView tvDate = (TextView) view.findViewById(R.id.date);
        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
        TextView tvDistance = (TextView) view.findViewById(R.id.distance);
        ImageView ivActivityType = (ImageView) view.findViewById(R.id.act_icon);
        ImageView ivWeather = (ImageView) view.findViewById(R.id.iv_weather);

        String activityName = cursor.getString(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_NAME));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_TIME));
        Long duration = cursor.getLong(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_DURATION));
        long distance = cursor.getLong(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_DISTANCE));
        int type = cursor.getInt(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_TYPE));
        int weather = cursor.getInt(cursor.getColumnIndexOrThrow(ActivityEntry.COLUMN_ACTIVITY_WEATHER));

        tvActivityName.setText(activityName);
        tvDate.setText(Utils.getDateTimeFormatted(date));
        tvDuration.setText(Utils.getTimeFormattedFromMillis(duration));
        tvDistance.setText(Utils.getDistanceFormatted(distance));
        ivActivityType.setImageResource(Utils.getActivityImageId(type));
        ivWeather.setImageResource(Utils.getConditionImageId(weather));
    }
}
