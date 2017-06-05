package com.example.android.fitassistant.activities;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.example.android.fitassistant.data.ActivityContract;

/**
 *
 * Created by Clara Matos on 21/05/2017.
 */

public class CurrentActivityWidget extends AppWidgetProvider {
    private static final String TAG = "CurrentActivityWidget";

    public final static String UPDATE_INFO_LABEL = "com.example.android.fitassistant.UPDATE_INFO_LABEL";

    public final static String WIDGETID_KEY = "com.example.android.fitassistant.WIDGETID";
    public final static String DISTANCE_KEY = "com.example.android.fitassistant.DURATION";
    public final static String DURATION_KEY = "com.example.android.fitassistant.DISTANCE";
    public final static String ACTIVITY_TYPE_KEY = "com.example.android.fitassistant.ACTIVITY_TYPE";

    private String DURATION_DEFAULT = "--:--";
    private String DISTANCE_DEFAULT = "-:--";
    private int ACTIVITY_ID_DEFAULT = ActivityContract.ActivityEntry.RUNNING;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate");
        for (int i = 0; i < appWidgetIds.length; i++) {
            setWidgetData(context, appWidgetManager, appWidgetIds[i], DURATION_DEFAULT, DISTANCE_DEFAULT, ACTIVITY_ID_DEFAULT);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Update widget info each time a brodcast is received
     * The broadcasts are sent by the MapActivity
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(UPDATE_INFO_LABEL)) {
            int[] widgets = intent.getIntArrayExtra(WIDGETID_KEY);
            for (int i = 0; i < widgets.length; i++) {
                int thisWidget = widgets[i];
                Log.i(TAG, "thisWidget: " + thisWidget);

                if (thisWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    Log.i(TAG, "onReceive: thisWidget: " + thisWidget);
                    String duration = intent.getStringExtra(DURATION_KEY);
                    String distance = intent.getStringExtra(DISTANCE_KEY);
                    int activityType = intent.getIntExtra(ACTIVITY_TYPE_KEY, ACTIVITY_ID_DEFAULT);
                    setWidgetData(context, appWidgetManager, thisWidget, duration, distance, activityType);
                }
            }
        }
    }

    /**
     * Set the provided info into the current id widget
     */
    private void setWidgetData(Context context, AppWidgetManager appWidgetManager, int thisWidget, String duration, String distance, int activityType) {
        Log.i(TAG, "setWidgetData, thisWidget:" + thisWidget);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        remoteViews.setTextViewText(R.id.duration_textview, duration);
        remoteViews.setTextViewText(R.id.distance_textview, distance);

        remoteViews.setImageViewResource(R.id.actvity_icon_imageview, Utils.getActivityImageId(activityType));

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }


}
