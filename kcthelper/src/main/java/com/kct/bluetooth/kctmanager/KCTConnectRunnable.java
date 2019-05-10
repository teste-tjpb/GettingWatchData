package com.kct.bluetooth.kctmanager;

import android.util.Log;

import com.kct.bluetooth.KCTBluetoothHelper;
import com.kct.bluetooth.KCTBluetoothManager;
import com.kct.bluetooth.utils.LogUtil;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
 * Author: ZGH
 * Version:
 * Creation date: 2018/3/23
 * Description: ${VERSION}
 * revise history:
 */

public class KCTConnectRunnable implements Runnable{

    private KCTBluetoothHelper helper;

    public KCTConnectRunnable(KCTBluetoothHelper helper){
        this.helper = helper;
    }

    @Override
    public void run() {
        if (helper.getConnectState() != KCTBluetoothManager.STATE_CONNECTED) {
            LogUtil.d("[KCTConnect]", "connect time over");
            helper.disconnect();
            helper.close();
        }
    }
}
