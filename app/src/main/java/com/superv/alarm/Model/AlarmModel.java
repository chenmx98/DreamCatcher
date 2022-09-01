package com.superv.alarm.Model;

public class AlarmModel {

    private int mID;
    private String mTitle;
    private String mTime;
    private String mRepeatType;
    private String mRepeatCode;
    private String mActive;
    private String mRing;

    public AlarmModel(int ID, String Title,  String Time,
                      String RepeatType,String RepeatCode,String ring, String Active){
        mID = ID;
        mTitle = Title;
        mTime = Time;
        mRepeatType = RepeatType;
        mRepeatCode = RepeatCode;
        mRing = ring;
        mActive = Active;
    }

    public AlarmModel(String Title, String Time, String RepeatType,
                      String RepeatCode, String ring,String Active){
        mTitle = Title;
        mTime = Time;
        mRepeatType = RepeatType;
        mRepeatCode = RepeatCode;
        mRing = ring;
        mActive = Active;
    }

    public AlarmModel(){};

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }


    public String getActive() {
        return mActive;
    }

    public void setActive(String mActive) {
        this.mActive = mActive;
    }


    public String getRepeatType() {
        return mRepeatType;
    }

    public void setRepeatType(String mRepeatType) {
        this.mRepeatType = mRepeatType;
    }

    public String getRepeatCode() {
        return mRepeatCode;
    }

    public void setRepeatCode(String mRepeatCode) {
        this.mRepeatCode = mRepeatCode;
    }

    public String getRing() {
        return mRing;
    }

    public void setRing(String ring) {
        mRing = ring;
    }
}
