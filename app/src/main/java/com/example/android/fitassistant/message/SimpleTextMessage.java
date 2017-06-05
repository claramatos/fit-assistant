package com.example.android.fitassistant.message;

/**
 * Created by Clara Matos on 22/04/2017.
 */
public class SimpleTextMessage implements iMessage{

    public static final int MY_MESSAGE = 0;
    public static final int OTHER_MESSAGE = 1;

    private int mMessageType;
    private String mContent;

    public SimpleTextMessage(String message, int type) {
        mContent = message;
        mMessageType = type;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public int getType() {
        return mMessageType;
    }

    @Override
    public String toString() {
        return mContent + ", " + String.valueOf(mMessageType);
    }

    @Override
    public int getListItemType() {
        return mMessageType;
    }
}
