package com.ssmc.sensorrecord.mobileUI;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ssmc.sensorrecord.MobileSensorRecordService;
import com.ssmc.sensorrecord.io.SensorDataWriter;
import com.ssmc.sensorrecord.WearTransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 这个Activity逻辑过于复杂，需要分模块解耦合
 * 建议分成本地Sensor、WearTransfer部分
 */
@RuntimePermissions
public class MobileActivity extends AppCompatActivity {

    private static final String TAG = "MobileActivity";

    //配置项
    //保存 CheckBox 和 Sensor 的映射
    private List<SensorCheckBox> mCheckboxToSensorMapping = new ArrayList<>(Arrays.asList(
            new SensorCheckBox(Sensor.STRING_TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER, this),
            new SensorCheckBox(Sensor.STRING_TYPE_GRAVITY, Sensor.TYPE_GRAVITY, this),
            new SensorCheckBox(Sensor.STRING_TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE, this),
            new SensorCheckBox(Sensor.STRING_TYPE_LINEAR_ACCELERATION, Sensor.TYPE_LINEAR_ACCELERATION, this),
            new SensorCheckBox(Sensor.STRING_TYPE_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR, this),
            new SensorCheckBox(Sensor.STRING_TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD, this),
            new SensorCheckBox(Sensor.STRING_TYPE_ORIENTATION, Sensor.TYPE_ORIENTATION, this)));
    //设置手表的传输服务
    private WearTransfer mWearTransfer =
            new WearTransfer(MobileActivity.this,
                    new SensorDataWriter("wear"));

    private Button btRecord;
    private MobileSensorRecordService.SensorRecordBinder mService;
    private List<Integer> sensorNeedRecord;

    //通过ServiceConnection来监听与service的连接
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (MobileSensorRecordService.SensorRecordBinder) service;
            mService.start(sensorNeedRecord);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intiViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWearTransfer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWearTransfer.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }

    /**
     * 动态加载布局
     */
    private void intiViews() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        for (SensorCheckBox sensorCheckBox : mCheckboxToSensorMapping) {
            sensorCheckBox.loadCheckBox();//加载文字到checkbox上
            layout.addView(sensorCheckBox.checkBox);
        }
        btRecord = new Button(this);
        btRecord.setText("确定");
        btRecord.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        btRecord.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobileActivityPermissionsDispatcher.recordSensorWithPermissionCheck(MobileActivity.this);
            }
        });
        layout.addView(btRecord);
        setContentView(layout);
    }

    /**
     * 传感器记录
     */
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BODY_SENSORS})
    void recordSensor() {
        if (btRecord.getText().equals("确定")) {
            StringBuilder dialogMessage = new StringBuilder();
            final List<Integer> sensorNeedRecord = new LinkedList<>();
            dialogMessage.append("已经选中:\n");
            //从SensorCheckBox里找出选中的要监听的传感器发给Service监听
            boolean isAtLeastCheckOne = false;
            for (SensorCheckBox sensorCheckBox : mCheckboxToSensorMapping) {
                if (sensorCheckBox.checkBox.isChecked()) {
                    dialogMessage.append(sensorCheckBox.checkBox.getText().toString());
                    dialogMessage.append("\n");
                    sensorNeedRecord.add(sensorCheckBox.sensor);
                    isAtLeastCheckOne = true;
                }
            }
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MobileActivity.this);
            if (isAtLeastCheckOne) {
                alertDialogBuilder
                        .setMessage(dialogMessage.toString())
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startRecord(sensorNeedRecord);
                            }
                        });
                btRecord.setText("正在写入");
            } else {
                //如果所有CheckBox都没有被选中
                alertDialogBuilder
                        .setMessage("您没有选中任何要监听的传感器")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
            }
            alertDialogBuilder.create().show();

        } else if (btRecord.getText().equals("正在写入")) {
            stopRecord();
            btRecord.setText("确定");
        }
    }

    private void stopRecord() {
        if (mService != null) {
            mService.stop();
            unbindService(mConnection);
        }
    }

    private void startRecord(List<Integer> sensorNeedRecord) {
        this.sensorNeedRecord = sensorNeedRecord;
        Intent intent = new Intent(MobileActivity.this, MobileSensorRecordService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }
}
