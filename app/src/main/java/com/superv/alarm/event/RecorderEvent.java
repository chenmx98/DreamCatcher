package com.superv.alarm.event;

public class RecorderEvent {

    //声音分贝
    public String type="";

    //声音分贝
    public int  soundSize = 0;

    //录音时长
    public long  timeCounter = 0L;

    public RecorderEvent(String type,int mSoundSize, long timeCounter) {
        this.type = type;
        this.soundSize = mSoundSize;
        this.timeCounter = timeCounter;
    }


}
