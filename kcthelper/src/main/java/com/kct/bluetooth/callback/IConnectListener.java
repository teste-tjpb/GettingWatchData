package com.kct.bluetooth.callback;

import android.bluetooth.BluetoothDevice;

import com.kct.bluetooth.bean.BluetoothLeDevice;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
         * Author: ZGH
         * Version:
         * Creation date: 2017/10/21
         * Description: ${VERSION}
         * revise history:
**/
public interface IConnectListener {

    void onConnectState(int state);

    void onConnectDevice(BluetoothDevice device);

    void onScanDevice(BluetoothLeDevice device);

    void onCommand_d2a(byte[] bytes);
}
