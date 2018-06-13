package com.ssmc.sensorrecord.io;

import com.ssmc.sensordesc.SensorRecord;

import java.io.IOException;

/**
 * 处理传感器的写入功能
 */
public interface ISensorStorage {
    /**
     * 将传感器记录写入到本地文件
     */
    void writeSensorData(SensorRecord sensorRecord) throws IOException;

    /**
     * 释放资源
     */
    void close() throws IOException;
}
