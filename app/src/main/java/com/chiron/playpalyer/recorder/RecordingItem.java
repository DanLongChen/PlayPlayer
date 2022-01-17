package com.chiron.playpalyer.recorder;

import android.os.Parcel;
import android.os.Parcelable;

public class RecordingItem implements Parcelable{
    private String mName; // file name
    private String mFilePath; //file path

    public RecordingItem()
    {
    }

    public RecordingItem(String mName,String mFilePath){
        this.mName = mName;
        this.mFilePath = mFilePath;
    }

    public RecordingItem(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }


    public static final Parcelable.Creator<RecordingItem> CREATOR = new Parcelable.Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFilePath);
        dest.writeString(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
