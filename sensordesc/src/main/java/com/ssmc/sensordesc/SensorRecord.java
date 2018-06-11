package com.ssmc.sensordesc;

import android.hardware.SensorEvent;

/**
 * 传感器数据的对象封装
 */
public class SensorRecord {

    private String stringType;//对应传感器的名字
    private float values[];//待写入的数据
    private float timeToBeginSecond;//计算从任务开始到现在的用时
    private long timeStamp;//系统时间戳
    private int type;

    @Deprecated
    private String currentTime;//当前时间戳

    public SensorRecord(String stringType, float[] values, float timeToBeginSecond, long timeStamp, int type) {
        this.stringType = stringType;
        this.values = values;
        this.timeToBeginSecond = timeToBeginSecond;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    public SensorRecord(SensorEvent event, long timeStamp, float timeToBeginSecond) {
        stringType = event.sensor.getStringType();
        type = event.sensor.getType();
        values = event.values;
        this.timeStamp = timeStamp;
        this.timeToBeginSecond = timeToBeginSecond;
    }

    @Deprecated
    public SensorRecord(String stringType, float[] values) {
        this.stringType = stringType;
        this.values = values;
    }

    @Deprecated
    public SensorRecord(float timeToBeginSecond, String currentTime,
                        int type, float[] values) {
        this.timeToBeginSecond = timeToBeginSecond;
        this.currentTime = currentTime;
        this.type = type;
        this.values = values;
    }

    public String getStringType() {
        return stringType;
    }

    public float[] getValues() {
        return values;
    }

    public float getTimeToBeginSecond() {
        return timeToBeginSecond;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getType() {
        return type;
    }
}
