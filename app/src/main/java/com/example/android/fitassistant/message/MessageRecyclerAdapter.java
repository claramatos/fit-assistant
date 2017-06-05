package com.example.android.fitassistant.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.fitassistant.R;
import com.example.android.fitassistant.Utils;
import com.google.android.gms.awareness.state.Weather;

import java.util.ArrayList;

/**
 * Created by Clara Matos on 20/05/2017.
 *
 * Exposes the messages sent between the user and the bot
 */
public class MessageRecyclerAdapter extends RecyclerView
        .Adapter<MessageRecyclerAdapter.ViewHolder> {

    private static final String TAG = "MessageRecyclerAdapter";

    private final Context mContext;
    private ArrayList<iMessage> mMessageList;
    private static DirectionsButtonClickListener mButtonClickListener;

    public MessageRecyclerAdapter(Context ctx, ArrayList<iMessage> myDataset, DirectionsButtonClickListener clickListener) {
        mContext = ctx;
        mMessageList = myDataset;
        mButtonClickListener = clickListener;
    }

    /**
     * ViewHolder for the simple message type
     */
    public class ViewHolderSimpleMessage extends ViewHolder {
        private final TextView mTextView;

        public ViewHolderSimpleMessage(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.message_textview);
        }

        public void bindType(iMessage item) {
            mTextView.setText(((SimpleTextMessage) item).getContent());
        }
    }

    /**
     * ViewHolder for the weather message type
     */
    public class ViewHolderWeatherMessage extends ViewHolder{
        private final TextView mTempTextView;
        private final TextView mHumidityTextView;
        private final TextView mWeatherTextView;
        private final ImageView mWeatherImageView;

        public ViewHolderWeatherMessage(View itemView) {
            super(itemView);
            mTempTextView = (TextView) itemView.findViewById(R.id.temp_textview);
            mHumidityTextView = (TextView) itemView.findViewById(R.id.humidity_textview);
            mWeatherImageView = (ImageView) itemView.findViewById(R.id.weather_icon);
            mWeatherTextView = (TextView) itemView.findViewById(R.id.weather_textview);
        }

        public void bindType(iMessage item) {
            MessageWeather message = (MessageWeather) item;

            int temperature = 0;

            if (Utils.isMetric(mContext)) {
                temperature = message.getTemperature(Weather.CELSIUS);
            } else {
                temperature = message.getTemperature(Weather.FAHRENHEIT);
            }

            int humidity = message.getHumidity();
            int iconId = message.getCondition();

            mTempTextView.setText(String.valueOf(temperature) + "º");
            mHumidityTextView.setText(mContext.getString(R.string.text_humidity, humidity) + "%");
            mWeatherImageView.setImageResource(Utils.getConditionImageId(iconId));
            mWeatherTextView.setText(mContext.getString(Utils.getConditionString(iconId)));
        }
    }

    /**
     * ViewHolder for the place message type
     */
    public class ViewHolderPlaceMessage extends ViewHolder implements View.OnClickListener {

        private final TextView mPlaceTextView;
        private final ImageView mMapImageView;
        private final Button mDirectionsButton;

        private float mLatitude = 0.0f;
        private float mLongitude = 0.0f;

        public ViewHolderPlaceMessage(View itemView) {
            super(itemView);
            mPlaceTextView = (TextView) itemView.findViewById(R.id.place_textview);
            mMapImageView = (ImageView) itemView.findViewById(R.id.map_imageview);
            mDirectionsButton = (Button) itemView.findViewById(R.id.directions_button);

            mDirectionsButton.setOnClickListener(this);
        }

        public void bindType(iMessage item) {
            Place place = (Place) item;

            mLatitude = Float.parseFloat(place.getLatitude());
            mLongitude = Float.parseFloat(place.getLongitude());

            mPlaceTextView.setText(place.getPlaceName());
            mMapImageView.setImageBitmap(place.getBitMap());
        }

        @Override
        public void onClick(View v) {
            mButtonClickListener.onDirectionsButtonClick(mLatitude, mLongitude);
        }

    }

    /**
     * More than one view holder is possible to be implemented
     * For that the new view holder should extend this one
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindType(iMessage message) {
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view;
        // inflate the view holder according to its type
        switch (type) {
            case iMessage.MY_MESSAGE:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_my_message, viewGroup, false);
                return new ViewHolderSimpleMessage(view);
            case iMessage.OTHER_MESSAGE:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_other_message, viewGroup, false);
                return new ViewHolderSimpleMessage(view);
            case iMessage.WEATHER_MESSAGE:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_weather_message, viewGroup, false);
                return new ViewHolderWeatherMessage(view);
            case iMessage.PLACE_MESSAGE:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_place_message, viewGroup, false);
                return new ViewHolderPlaceMessage(view);
        }

        return null;
    }

    public void onBindViewHolder(ViewHolder holder, int i) {
        iMessage message = mMessageList.get(i);
        holder.bindType(message);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getListItemType();
    }

    /**
     * notify the activity when the directions button is clicked
     */
    public interface DirectionsButtonClickListener {
        void onDirectionsButtonClick(float lat, float lng);
    }
}
