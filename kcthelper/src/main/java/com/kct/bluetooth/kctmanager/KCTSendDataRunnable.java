package com.kct.bluetooth.kctmanager;


import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import com.kct.bluetooth.KCTReceiveCommand;
import com.kct.bluetooth.KCTSendCommand;

import static com.kct.bluetooth.bean.KCTGattAttributes.RX_SERVICE_872_UUID;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
 * Author: ZGH
 * Version:
 * Creation date: 2018/3/23
 * Description: ${VERSION}
 * revise history:
 */

public class KCTSendDataRunnable implements Runnable {

    private KCTSendCommand kctSendCommand;
    private KCTReceiveCommand kctReceiveCommand;
    private Handler handler;
    private BluetoothGattService RxService;

    public KCTSendDataRunnable(KCTSendCommand kctSendCommand, KCTReceiveCommand kctReceiveCommand
            , Handler handler, BluetoothGattService RxService){
        this.kctSendCommand = kctSendCommand;
        this.kctReceiveCommand = kctReceiveCommand;
        this.handler = handler;
        this.RxService = RxService;
    }

    @Override
    public void run() {
        if (null != kctSendCommand && !kctReceiveCommand.getDataBuffer().burDataBegin) {
            kctSendCommand.reCancel(false);
            if(RxService.getUuid().equals(RX_SERVICE_872_UUID)){
                handler.postDelayed(this,6000);
            }else {
                handler.postDelayed(this,5000);
            }
        }

    }
}
