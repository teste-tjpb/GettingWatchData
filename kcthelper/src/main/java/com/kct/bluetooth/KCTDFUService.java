package com.kct.bluetooth;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
 * Author: ZGH
 * Version:
 * Creation date: 2018/4/9
 * Description: ${VERSION}
 * revise history:
 */

public class KCTDFUService extends DfuBaseService{

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return KCTDfuNotificationActivity.class;
    }
}
