package com.ssmc.sensorrecord;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.ssmc.sensordesc.SensorRecord;
import com.ssmc.sensorrecord.io.ISensorStorage;

import java.io.IOException;

/**
 * 与手表的数据通信处理，接收手表的数据并保存到本地
 */
public class WearTransfer implements DataApi.DataListener {

    //private static final String TAG = "WearTransfer";
    private ISensorStorage mSensorDataWriter;
    private GoogleApiClient mGoogleApiClient;

    public WearTransfer(Context context, ISensorStorage sensorStorage) {
        mSensorDataWriter = sensorStorage;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        //Log.d(TAG, "onConnected: " + connectionHint);
                        Wearable.DataApi.addListener(mGoogleApiClient, WearTransfer.this);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        //Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        //Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    /**
     * 开启连接
     */
    public void start() {
        mGoogleApiClient.connect();
    }

    /**
     * 关闭连接和文件存储功能
     */
    public void stop() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        if (mSensorDataWriter != null) {
            try {
                mSensorDataWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSensorData(SensorRecord sensorRecord) {
        try {
            mSensorDataWriter.writeSensorData(sensorRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                ///这里请添加一条逻辑用来检测Uri
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                ///有待改进，不要直接用字符串，这样以后改要同时改手机的Transfer部分和Wear的Transfer部分
                String stringType = dataMap.get("stringType");
                float[] values = dataMap.get("values");
                float timeToBeginSecond = dataMap.get("timeToBeginSecond");
                long timeStamp = dataMap.get("timeStamp");
                int type = dataMap.get("type");
                SensorRecord sensorRecord = new SensorRecord(stringType, values,
                        timeToBeginSecond, timeStamp, type);
                //Log.d(TAG, "onDataChanged: " + sensorRecord.toString());
                saveSensorData(sensorRecord);
            }
        }
    }
}
