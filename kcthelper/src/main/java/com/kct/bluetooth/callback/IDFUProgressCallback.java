package com.kct.bluetooth.callback;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
 * Author: ZGH
 * Version:
 * Creation date: 2018/4/10
 * Description: ${VERSION}
 * revise history:
 */

public interface IDFUProgressCallback {

    void onDeviceConnecting(String deviceAddress);

    void onDeviceConnected(String deviceAddress);

    void onDfuProcessStarting(String deviceAddress);

    void onDfuProcessStarted(String deviceAddress);

    void onEnablingDfuMode(String deviceAddress);

    void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal);

    void onFirmwareValidating(String deviceAddress);

    void onDeviceDisconnecting(String deviceAddress);

    void onDeviceDisconnected(String deviceAddress);

    void onDfuCompleted(String deviceAddress);

    void onDfuAborted(String deviceAddress);

    void onError(String deviceAddress, int error, int errorType, String message);
}
