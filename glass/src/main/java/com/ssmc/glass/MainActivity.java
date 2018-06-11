package com.ssmc.glass;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private static final String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    File file = new File(PATH + File.separator + "test.txt");
    BufferedWriter bufferedWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            bufferedWrite = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SensorManager mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        mSensorManager.registerListener(new SensorEventListener() {
                                            @Override
                                            public void onSensorChanged(SensorEvent event) {
                                                try {
                                                    for (float value : event.values) {
                                                        bufferedWrite.write(value + " ");
                                                    }
                                                    bufferedWrite.write("\n");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onAccuracyChanged(Sensor sensor, int accuracy) {

                                            }
                                        },
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
}
