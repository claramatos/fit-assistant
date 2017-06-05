package com.example.android.fitassistant.message;

import android.graphics.Bitmap;

/**
 * Created by Clara Matos on 29/04/2017.
 */
public class Place implements iMessage {

    private String mPlaceName;
    private String mVicinity;
    private String mLatitude;
    private String mLongitude;
    private Bitmap mBitMap;
    private String mType;

    private Place(PlaceBuilder builder) {
        this.mPlaceName = builder.placeName;
        this.mVicinity = builder.vicinity;
        this.mLatitude = builder.latitude;
        this.mLongitude = builder.longitude;
        this.mType = builder.type;
    }

    public static class PlaceBuilder {
        private String placeName;
        private String vicinity;
        private String latitude;
        private String longitude;
        private String type;

        public PlaceBuilder placeName(String placeName) {
            this.placeName = placeName;
            return this;
        }

        public PlaceBuilder vicinity(String vicinity) {
            this.vicinity = vicinity;
            return this;
        }

        public PlaceBuilder latitude(String latitude) {
            this.latitude = latitude;
            return this;
        }

        public PlaceBuilder longitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public PlaceBuilder placeType(String type) {
            this.type = type;
            return this;
        }

        public Place build() {
            return new Place(this);
        }
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("mPlaceName: " + mPlaceName);
        sb.append(", mVicinity: " + mVicinity);
        sb.append(", mLatitude: " + mLatitude);
        sb.append(", mLongitude: " + mLongitude);
        sb.append(", nType: " + mType);

        return sb.toString();
    }

    public void setBitMap(Bitmap image) {
        mBitMap = image;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public String getVicinity() {
        return mVicinity;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public Bitmap getBitMap() {
        return mBitMap;
    }

    public String getType() {
        return mType;
    }

    @Override
    public int getListItemType() {
        return PLACE_MESSAGE;
    }

}
