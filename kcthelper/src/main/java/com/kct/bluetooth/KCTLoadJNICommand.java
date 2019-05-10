package com.kct.bluetooth;

/**
 * Copyright: Shenzhen Jinkant Intelligent Technology Co., Ltd.
 * Author: ZGH
 * Version:
 * Creation date: 2018/4/9
 * Description: ${VERSION}
 * revise history:
 */

public class KCTLoadJNICommand {

    public static final String TAG = "KCTLoadJNICommand";
    public static KCTLoadJNICommand mKCTLoadJNICommand;

    static {
        System.loadLibrary("KCTCommand");
    }

    public static KCTLoadJNICommand getInstance(){
        if(mKCTLoadJNICommand == null){
            synchronized (KCTLoadJNICommand.class){
                mKCTLoadJNICommand = new KCTLoadJNICommand();
            }
        }
        return mKCTLoadJNICommand;
    }

    public native String getDFUCommand();

    public native String getDFUVersion();

    public native String getDFUData();
}
