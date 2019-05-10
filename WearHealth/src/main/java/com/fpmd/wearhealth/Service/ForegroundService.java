package com.fpmd.wearhealth.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fpmd.wearhealth.BleConstants;
import com.fpmd.wearhealth.Constants.Constant;
import com.fpmd.wearhealth.HomeActivity;
import com.fpmd.wearhealth.R;
import com.fpmd.wearhealth.Utility.SavedData;
import com.fpmd.wearhealth.Utility.UtilityFunctions;
import com.fpmd.wearhealth.database.DatabaseHelper;
import com.fpmd.wearhealth.modal.WatchData;
import com.kct.bluetooth.KCTBluetoothManager;
import com.kct.bluetooth.bean.BluetoothLeDevice;
import com.kct.bluetooth.callback.IConnectListener;
import com.kct.command.BLEBluetoothManager;
import com.kct.command.IReceiveListener;
import com.kct.command.KCTBluetoothCommand;
import com.nullwire.trace.ExceptionHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.fpmd.wearhealth.Constants.Constant.BLOOD_PRESSURE_OXYGEN;
import static com.fpmd.wearhealth.Constants.Constant.HEART_RATE_ALL;
import static com.fpmd.wearhealth.Constants.Constant.HEART_RATE_REAL;
import static com.fpmd.wearhealth.Constants.Constant.OXYGEN;
import static com.fpmd.wearhealth.Constants.Constant.PEDOMETER_ALL;

/**
 * Created by vikasaggarwal on 08/04/18.
 */

public class ForegroundService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();

    private UtilityFunctions utilityFunctions;
    public static boolean IS_SERVICE_RUNNING = false;
    public static int SERVICE_ID = 23312;
    RequestQueue queue;
    private String step,month,hour,day,year,minute,second,heart,calorie,distance;

    Context ctx;


    //Location work
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 10f;
    //fetch location
    Location mLastLocation;

    private class LocationListener implements android.location.LocationListener {

        @NonNull
        private void L2_Parse(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                String resModebyteslx = UtilsLX.bytesToHexString(bytes);
                //TODO  Single packet data request
                if (bytes.length == 5) {  //todo --- The command sent by the watch   bytes.length == 13     ----- Single package removes the first 8bytes
                    // BA30 0005 0043 0002 0500510000  --- Looking for a mobile phone    // BA30 0005 00BE 0007  0400460000
                    int byte1 = bytes[0];
                    int byte3 = bytes[2];
                    switch (byte1) {
                        case BleConstants.FIRMWARE_UPGRADE_COMMAND:
                            break;
                        case BleConstants.INSTALL_COMMAND:
                            break;
                        case BleConstants.WEATHER_PROPELLING:
                            break;
                        case BleConstants.DEVICE_COMMAND:
                            switch (byte3) {
                                case BleConstants.CAMERA_OPEN:
                                    break;
                                case BleConstants.CAMERA_TAKE:
                                    break;
                                case BleConstants.CAMERA_CLOSE:
                                    break;
                                case BleConstants.KEY_WATCH_LOCK_SCREEN:
                                    break;
                                case BleConstants.DIAL_RETURN:
                                    break;
                                case BleConstants.WATCH_BLUETOOTH_DISCONNECT:
                                    break;
                                case BleConstants.KEY_NOTIFICATION_PUSH:
                                    break;
                                case BleConstants.SYNC_USER_WEIGHT:
                                    break;
                                    




                            }
                    }
                    if (byte1 ==) {          //TODO -- Firmware upgrade command   0x01   byte9 == BleConstants.FIRMWARE_UPGRADE_COMMAND
                        if (byte3 == BleConstants.FIRMWARE_UPGRADE_ECHO) {
                            disConnect();
                            Intent intent = new Intent(ACTION_BLEDISCONNECTED);
                            sContext.sendBroadcast(intent);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.BLE_CLICK_STOP, true);    // false
                            EventBus.getDefault().post(new MessageEvent("goto_updata"));//Enter firmware upgrade mode
                        }
                    }
                    else if (byte1 == BleConstants.INSTALL_COMMAND) {              //TODO -- Setting command        0x02

                    }
                    else if (byte1 == BleConstants.WEATHER_PROPELLING) {           //TODO -- Weather push command     0x03

                    } else if (byte1 == BleConstants.DEVICE_COMMAND) {                    //TODO -- Device command         0x04
                        switch (byte3) {   // -------  key  byte11
                            case BleConstants.CAMERA_OPEN: //  Open photo
                                if(SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")){
                                    if ((Boolean) SharedPreUtil.getParam(sContext, SharedPreUtil.USER, SharedPreUtil.TB_CAMERA_NOTIFY, true)){
                                        Intent intent = new Intent("0x46");   // Send remote photo --- corresponding broadcast
                                        sendBroadcast(intent);
                                    }
                                } else if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
//                                if (RemoteCamera.isSendExitTakephoto) {
////                                    RemoteCamera.isSendExitTakephoto = false;
//                                    return;
//                                } else {
//                                    Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
//                                    sendBroadcast(intent);
//                                }

                                    if(!CameraActivity.isPhoneExitTakephoto){  // Mobile phone actively quits
                                        Intent intentCamera = new Intent(getApplicationContext(), CameraActivity.class);     // todo  --- 打开 拍照              BTNotificationApplication.getInstance()    getApplicationContext()  sContext   getBaseContext
                                        intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intentCamera);

//                                    Intent launchIntent = new Intent();
//                                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    launchIntent.setClass(mContext, RemoteCamera.class);
//                                    mContext.startActivity(launchIntent);
                                    }else{
                                        CameraActivity.isPhoneExitTakephoto = false;
                                    }

                                } else {
                                    Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent);
                                }

//                                Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
//                                sendBroadcast(intent);
//                            }
                                break;

                            case BleConstants.CAMERA_TAKE: // 拍照
                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
                                    sendBroadcast(new Intent(ACTION_REMOTE_CAMERA));
                                }
                                else if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1"))
                                {
                                    boolean b = (boolean) SharedPreUtil.getParam(this, SharedPreUtil.USER, SharedPreUtil.TB_CAMERA_NOTIFY, true);
                                    if(b)
                                    {
                                        Intent intent2 = new Intent("0x47");   // 发送远程拍照 --- 对应的广播
                                        sendBroadcast(intent2);
                                    }
                                }
                                else {
                                    Intent intent2 = new Intent("0x47");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent2);
                                }
                                break;

                            case BleConstants.CAMERA_CLOSE: // 退出拍照
//                            if (!SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH_ASSISTANT_CAMERA).equals("0")) {   //判断是否开启照片按钮
//                            }

                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
                                    sendBroadcast(new Intent(ACTION_REMOTE_CAMERA_EXIT));
                                }else {
                                    Intent intent3 = new Intent("0x48");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent3);
                                    isDeviceSendExitCommand = true;
                                }
                                break;

                            case BleConstants.KEY_WATCH_LOCK_SCREEN: // 手表发送锁屏功能
                                startLock();
                                break;

                            case BleConstants.DIAL_RETURN: // 发送绑定设备后，手表返回  表盘数据返回
                                // 04004F00003032233037233136233137233138233139233230233231233232233233233234233235233236233237233238233239233331233332233333233334233335233336233337233338233339233430233431233432233433233434
                                // 04 00 4F0000 3032233037233136233137233138233139233230233231233232233233233234233235233236233237233238233239233331233332233333233334233335233336233337233338233339233430233431233432233433233434

                                byte[] newByte = new byte[bytes.length - 5];   // 去掉前面5byte 第6byte开始为 手表实际返回数据
                                System.arraycopy(bytes, 5, newByte, 0, bytes.length - 5);
                                String resnewbytes = new String(newByte);
                                Log.e(TAG, "newbytes.toString----" + resnewbytes);
                                // 02#07#16#17#18#19#20#21#22#23#24#25#26#27#28#29#31#32#33#34#35#36#37#38#39#40#41#42#43#44   ---- 表盘序号数据
//                            String[] dialData = resnewbytes.split("#");

                                String ss = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL);
                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL).equals(resnewbytes)) {
                                    return;
                                } else {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    Log.e("readMessagereadcontant", resnewbytes);
                                    Intent intentNew = new Intent();
                                    intentNew.setAction(MainService.ACTION_CLOCK_SKIN_MODEL_CHANGE);

                                    SharedPreUtil.savePre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL, resnewbytes);
                                    // intent.putExtra("type", readMessagereadcontant);
                                    sendBroadcast(intentNew);
                                }
                                break;

                            case BleConstants.WATCH_BLUETOOTH_DISCONNECT: // 手表发送蓝牙断开连接
//                            MainService.Daring = true;
//                            stopChat();     //TODO -----  APP端蓝牙断开连接
                                break;

                            case BleConstants.KEY_NOTIFICATION_PUSH: // 手机通知推送（app端推送手机短信等）

                                break;

                            case BleConstants.SYNC_USER_WEIGHT: // 手机端同步用户体重--- 手表端回复 （同步成功时）

                                break;
                            case BleConstants.KEY_INPUTASSIT_SEND:        //协助输入
                                EventBus.getDefault().post(new MessageEvent("assistInput_success"));
                                break;
                            case BleConstants.SYN_ADDREST_LIST:        //联系人
                                EventBus.getDefault().post(new MessageEvent("constants"));  //发送到
                                break;

                        }
                    } else if (byte1 == BleConstants.FIND_COMMAND) {                        //TODO -- 查找命令   0x05
                        switch (byte3) {   // -------  key
                            case BleConstants.FIND_PHONE: // 找手机
//                            if(!SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH_ASSISTANT_FIND_PHONE).equals("0")){  // 0 --- ???
                                showDialog();      //todo ----    单包找手机

//                            Intent intent2 = new Intent();
//                            intent2.setAction(MainService.ACTION_FINDPHONE);   // 发找手机广播，
//                            sendBroadcast(intent2);
//                            }
                                break;

                            case BleConstants.FIND_DEVICE: // 找手表  ---- 手表端回复命令
                                Intent intent = new Intent();
                                intent.setAction(MainService.ACTION_GETWATCH);   // 发广播，销毁加载框
                                sendBroadcast(intent);
                                break;

                        }
                    } else if (byte1 == BleConstants.REMIND_COMMAND) {                      //TODO -- 提醒命令   0x06

                    } else if (byte1 == BleConstants.RUN_MODE_COMMADN) {                     //TODO -- 运动模式命令   0x07    单包
                        if(bytes[2] == BleConstants.RUN_BASE_RETURN){
                            int l2ValueLength = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            if (l2ValueLength == 0) {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(MainService.ACTION_SYNFINSH_SPORTS); // todo  --- 当没有运动模式数据时，发广播，销毁加载的同步框
                                getApplicationContext().sendBroadcast(broadcastIntent);
                                return;
                            }
                        }
                    } else if (byte1 == BleConstants.SLEEP_COMMAND) {                        //TODO -- 睡眠命令     0x08

                    } else if (byte1 == BleConstants.HEART_COMMAND) {                           //TODO -- 心率命令   0x09

                    } else if (byte1 == BleConstants.SYN_COMMAND) {                      //TODO -- 同步命令   0x0A    ---- 单包
//                    closeDialogNoData();  // TODO  注释 0616
                        if (bytes[2] == BleConstants.BRACELREALSPORT) {                         //TODO 手环运动模式数据返回 (0xA5)
                            int l2ValueLength = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            if (l2ValueLength == 0) {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(MainService.ACTION_SYNFINSH_SPORTS); // todo  --- 当没有运动模式数据时，发广播，销毁加载的同步框
                                getApplicationContext().sendBroadcast(broadcastIntent);
                                return;
                            }
                        }

                        if (bytes[2] == BleConstants.BRACELET_RUN_DATA_RETURN) {
                            if(!BTNotificationApplication.isSyncEnd) {
                                if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                    Log.e(TAG,"needSendDataType = " + needSendDataType + " ; needReceDataNumber = " + needReceDataNumber);
                                    needReceDataNumber = 1;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                    intent.putExtra("step", "1");
                                    sContext.sendBroadcast(intent);
                                } else {

                                    getSyncDataNumInService++;

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "发送5广播");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送5广播");
                                        }
                                    }//////////////////////////////////////////////////////////////////

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                }
                            }

                        } else if (bytes[2] == BleConstants.BRACELET_SLEEP_DATA_RETURN) {
                            if(!BTNotificationApplication.isSyncEnd) {
                                if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                    if (needReceDataNumber == 1) {
                                        needReceDataNumber = 2;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                        intent.putExtra("step", "2");
                                        sContext.sendBroadcast(intent);
                                    }
                                    if (BTNotificationApplication.needSendDataType == needReceDataNumber) {
                                        receiveCountDownTimer.cancel();
                                        receiveCountDownTimer.start();
                                    }


                                } else {
                                    getSyncDataNumInService++;

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "发送5广播");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送5广播");
                                        }
                                    }//////////////////////////////////////////////////////////////////

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                }
                            }
                        } else if (bytes[2] == BleConstants.BRACELET_HEART_DATA_RETURN) {

                            if (!BTNotificationApplication.isSyncEnd) {
                                if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                    Log.e(TAG,"needSendDataType = " + needSendDataType + " ; needReceDataNumber = " + needReceDataNumber);
                                    if (needReceDataNumber == 1) {
                                        needReceDataNumber = 2;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                        intent.putExtra("step", "2");
                                        sContext.sendBroadcast(intent);
                                    } else if (needReceDataNumber == 2) {
                                        needReceDataNumber = 3;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                        intent.putExtra("step", "3");
                                        sContext.sendBroadcast(intent);
                                    }
                                    if (BTNotificationApplication.needSendDataType == needReceDataNumber) {
                                        receiveCountDownTimer.cancel();
                                        receiveCountDownTimer.start();
                                    }
                                } else {
                                    getSyncDataNumInService++;

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }
                                    }//////////////////////////////////////////////////////////////////

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                    if (getSyncDataNumInService == BTNotificationApplication.needReceiveNum) {  //   BTNotificationApplication.bleSyncDataDays = 7;    HomeFragment.getHistoryDataDays
//                                Intent intent = new Intent();      // add 0414
//                                intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播      ACTION_SYNFINSH_SUCCESS
//                                sContext.sendBroadcast(intent);

                                        Intent intent = new Intent();      // add 0414
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                        intent.putExtra("step", "6");
                                        sContext.sendBroadcast(intent);
                                        Log.e("liuxiaodata", "Send 6 broadcasts");

//                                HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize5));

                                        getSyncDataNumInService = 0;
                                        BTNotificationApplication.isSyncEnd = true;

                                        String curMacaddress = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.MAC);

                                        String isFirstSync = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.ISFIRSTSYNCDATA, SharedPreUtil.SYNCED);
                                        if (StringUtils.isEmpty(isFirstSync) || isFirstSync.substring(0, 1).equals("0")) {      // TODO--- 没取过7天的数据了
                                            SharedPreUtil.savePre(BTNotificationApplication.getInstance(), SharedPreUtil.ISFIRSTSYNCDATA, SharedPreUtil.SYNCED, "1#" + curMacaddress);  // todo The synchronization is completed --- Synchronize the data for 7 days for the first time. After 7 days of data, set SYNCED to 1
                                        }
                                    } else { //todo --- The number of corresponding received data has not been reached
                                        mHandler.sendEmptyMessageDelayed(HEART_DATA_FAILOVER, 20000); //TODO ---Delayed 30 seconds to send, broadcast
                                    }
                                }
                            }
                        }else if(bytes[2] == BleConstants.BLOOD_PRESSURE_HIS) {                  //TODO -- 历史血压数据返回 -- 0xAD
                            if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                Log.e(TAG,"needSendDataType = " + needSendDataType + " ; needReceDataNumber = " + needReceDataNumber);
                                if (needReceDataNumber == 1) {
                                    needReceDataNumber = 2;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                    intent.putExtra("step", "2");
                                    sContext.sendBroadcast(intent);
                                } else if (needReceDataNumber == 2) {
                                    needReceDataNumber = 3;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                    intent.putExtra("step", "3");
                                    sContext.sendBroadcast(intent);
                                } else if(needReceDataNumber == 3){
                                    needReceDataNumber = 4;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                    intent.putExtra("step", "4");
                                    sContext.sendBroadcast(intent);
                                }
                                if (BTNotificationApplication.needSendDataType == needReceDataNumber) {
                                    receiveCountDownTimer.cancel();
                                    receiveCountDownTimer.start();
                                }
                            }

                        }

                    } else if (byte1 == BleConstants.CALIBRATION_COMMAND) {                       //TODO -- 校准命令    0x0B

                    } else if (byte1 == BleConstants.FACTORY_COMMAND) {                             //TODO -- 工厂命令   0x0C

                    } else if (byte1 == BleConstants.PUSH_DATA_TO_PHONE_COMMAND) {                        //TODO -- 查找命令   0x0D   推送数据到手机 (接挂电话相关)
                        //    0A00A300 64110909000000004C00000059000000A6000000B0000000E1000000F3000001360000028E00000C1000000EDA0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                        //    0A00A300 641109080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000063300000C2600000E3E00000F1D
                        switch (byte3) {   // -------  key                                      BA300 0080 04F000A0D00010003000101      0A00 AD00 08 1109080101000000
//                        case BleConstants.GESTURE_PUSH_COMMAND: // 手势智控推送    ----    0D0001 00  03 00 0000    0D0001 00  0300 0101
//                            Log.e("phone", "智控开关发命令了");  // TAG     05005000 0100
//
//                            //todo  --- 当开关为开时，将抬手亮屏的开关打开 ----发广播
//
//
//                            break;

                            case BleConstants.REJECT_DIAL_COMMAND: // 拒接电话
                                uart_data_end_call(sContext);
                                Log.e("phone", "Refuse to answer the call");  // TAG
                                break;

                            case BleConstants.ANSWER_DIAL_COMMAND: // 接电话
                                startcall();
                                Log.e("phone", "Answer the call");
                                break;





                            case BleConstants.PLAY_MUSIC_COMMAND: // Play music push
                                controlMusic(KEYCODE_MEDIA_PLAY_PAUSE);
                                break;

                            case BleConstants.PAUSE_MUSIC_COMMAND: // Pause music push
                                controlMusic(KEYCODE_MEDIA_PLAY_PAUSE);
                                break;

                            case BleConstants.LAST_MUSIC_COMMAND: // Previous push
                                controlMusic(KEYCODE_MEDIA_PREVIOUS);
                                break;

                            case BleConstants.NEXT_MUSIC_COMMAND: // Next push
                                controlMusic(KEYCODE_MEDIA_NEXT);
                                break;

                        }
                    } else if (byte1 == (byte) 0x10){              //TODO   ---Next clock movement calibration command...
                        switch (byte3){
                            case (byte)0x02:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.REFUSE_CALIBRATION));
                                break;
                            case (byte)0x03:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.REFUSE_CALIBRATION));
                                break;
                            case (byte)0x04:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.CONFIRM_CALIBRATION));
                                break;
                            case (byte)0x05:
                                if(bytes.length > 5){
                                    int code = bytes[5];
                                    if(code == 0){
                                        EventBus.getDefault().post(new MessageEvent(CalibrationActivity.SEND_CALIBRATION));
                                    }else{
                                        EventBus.getDefault().post(new MessageEvent(CalibrationActivity.CANCEL_CALIBRATION));
                                    }
                                }
                                break;
                        }
                    } else if (byte1 == BleConstants.COMMAND_WEATHER_INDEX) {//TODO -- 表盘推送    0x0E
                        if (bytes[2] == BleConstants.DIAL_PUSH) {   // 表盘推送
                            int ddd = 666;
                        }
                    }
                } else {    //todo ---- 手机端主动 发送命令      //TODO  多包数据请求
                    int byte1 = bytes[0];   // 命令号    0x11,0x03,0x15,0x13,0x10,0x28,0x3D,
                    int byte3 = bytes[2];
                    if (byte1 == BleConstants.FIRMWARE_UPGRADE_COMMAND) {              //TODO -- 固件升级命令   0x01
                        switch (bytes[2]) {
                            case BleConstants.FIRMWARE_UPGRADE_REQURN:                 //TODO -- 固件信息返回
                                String version = "v" + bytes[5] + "." + bytes[6] + "." + bytes[7];          //固件版本
                                Log.i(TAG, "Firmware information version: " + version);
                                SharedPreUtil.savePre(sContext, SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.FIRMEWAREVERSION, version);
                                int braceletType = bytes[8];
                                Log.i(TAG, "升级平台: " + braceletType);         //升级平台  0:nordic  1:dialog  2：MTK  3：智能表
                                SharedPreUtil.savePre(sContext, SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.FIRMEWARETYPE, braceletType + "");
                                int platformCode = (((bytes[9] << 16) & 0xff0000) | ((bytes[10] << 8) & 0xff00) | (bytes[11] & 0xff));                   //手环序列号
                                SharedPreUtil.savePre(sContext, SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.FIRMEWARECODE, platformCode + "");
                                Log.i(TAG, "手环序列号：" + platformCode);
                                EventBus.getDefault().post(new MessageEvent("firmeware_version"));
                                if(bytes.length == 16){
                                    String protocolCode = "V" + bytes[12] + "." + bytes[13] + "." + bytes[14] + bytes[15];
                                    Log.i(TAG,"协议版本：" + protocolCode);
                                    if(DateUtil.versionCompare("V1.1.39",protocolCode) && braceletType == 3){   //现根据协议版本号，大于1.1.40
                                        CONSTANTS = true;
                                    }else {
                                        CONSTANTS = false;
                                    }
                                    SharedPreUtil.savePre(sContext, SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.PROTOCOLCODE, protocolCode + "");
                                }
                                if(db == null) {
                                    db = DBHelper.getInstance(sContext);
                                }
                                HTTPController.SynWatchInfo(sContext,db,platformCode);   //请求型号适配功能
                                break;
                            case BleConstants.FIRMWARE_UPGRADE_ECHO:                   //TODO -- 固件升级回应
                                if("1".equals(SharedPreUtil.readPre(sContext, SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.FIRMEWARETYPE))) {   //dialog升级
                                    KCTBluetoothManager.getInstance().setDilog(true,iDialogCallback);
                                    EventBus.getDefault().post(new MessageEvent("firmWare_start"));
                                    //isSendFile = true;

                                }
                                break;
                        }
                    }
                    else if (byte1 == BleConstants.TEMPERATURE_COMMAND) {
                        if(byte3 == BleConstants.TEMPERATURE_RETURN)
                        {
                            if(bytes.length<12){
                                return;
                            }
                            String year = String.format(Locale.ENGLISH,"20" + "%02d", (bytes[5] & 0xff));   // 第5位 年
                            Log.e(TAG, "sleep year =" + year);
                            String month = String.format(Locale.ENGLISH,"%02d", bytes[6] & 0xff);
                            Log.e(TAG, "sleep mouth =" + month);
                            String day = String.format(Locale.ENGLISH,"%02d", bytes[7] & 0xff);
                            Log.e(TAG, "sleep day =" + day);
                            String hour = (bytes[9] & 0xff)+":"+(bytes[10] & 0xff)+":00";
                            String temperatureValue = (bytes[11] & 0xff)+"."+(bytes[12] & 0xff);
                            String date = year+"-"+month+"-"+day;
                            String binTime = null;
                            try {
                                binTime = mSimpleDateFormat.parse(date + " " + hour).getTime()+"";
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Temperature temperature = new Temperature();
                            temperature.setBinTime(binTime);
                            temperature.setDate(date);
                            temperature.setTemperatureValue(temperatureValue);
                            String mac = "";
                            if (SharedPreUtil.readPre(this, SharedPreUtil.USER, SharedPreUtil.SHOWMAC).equals("")) {
                                mac = SharedPreUtil.readPre(this, SharedPreUtil.USER, SharedPreUtil.MAC);
                            } else {
                                mac = SharedPreUtil.readPre(this, SharedPreUtil.USER, SharedPreUtil.SHOWMAC);
                            }
                            temperature.setMac(mac);
                            try {
                                saveTemperature(temperature);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            EventBus.getDefault().post("updateTemperature");
                        }
                    }
                    else if (byte1 == BleConstants.ECG_COMMAND) {              //TODO -- 心电命令

                        switch (byte3)
                        {
                            case BleConstants.ECG_HISTORY_CONTENT:
                                parseEcg(bytes);
                                break;
                            case BleConstants.ECG_CONFIGURATION:
                                if (bytes.length >= 8) {
                                    int speed = bytes[5];
                                    int gain = bytes[6];
                                    short rate = (short) ((bytes[7]&0xff)<<8|(bytes[8]&0xff));
                                    int dimension = 350;
                                    SharedPreUtil.setParam(BTNotificationApplication.getInstance(),SharedPreUtil.USER,SharedPreUtil.ECG_SPEED,speed);
                                    SharedPreUtil.setParam(BTNotificationApplication.getInstance(),SharedPreUtil.USER,SharedPreUtil.ECG_GAIN,gain);
                                    SharedPreUtil.setParam(BTNotificationApplication.getInstance(),SharedPreUtil.USER,SharedPreUtil.ECG_RATE,rate);
                                    SharedPreUtil.setParam(BTNotificationApplication.getInstance(),SharedPreUtil.USER,SharedPreUtil.ECG_DIMENSION,dimension);
                                }
                                break;
                            case BleConstants.ECG_START:
                            case BleConstants.ECG_FINISH:
                            case BleConstants.ECG_CONTENT: {
                                EventBus.getDefault().post(bytes);
                                break;
                            }
                        }
                    }
                    else if (byte1 == BleConstants.INSTALL_COMMAND) {              //TODO -- Setting command        0x02
                        if (bytes[2] == BleConstants.INSTALL_SETTING_RETURN) {            //TODO -- Read bracelet setting response   0x2F
                            // 02002F 004A
                            // 000000 0000
                            // 000000 0000
                            // 000000 0000
                            // 000000 0000
                            // 000000 0000   --- 25 bytes	Alarm clock (all 5 groups)
                            // 0109 0B7F 001E 0032   --- 8 bytes		Sedentary reminder
                            // 0012 AA3C 8813 0000   --- 8 bytes		User Info
                            // 03                   1 byte		Reminder mode
                            // 0000 0000 00             5 bytes		Do not disturb setting
                            // 0000 0000 0000       6 bytes		Heart rate detection   （6bytes）
                            // 0018 3C0E            4 bytes		System settings   --- Not used
                            // 0000 0000 0000 0000  8 bytes		Drinking water reminder
                            // 0000                  2 bytes		Message push
                            // 8813 0000            4 bytes		Moving target
                            // 000000               3 bytes		Gesture bright screen
                            byte[] clockByte = new byte[25];       //Alarm clock
                            byte[] sedentaryByte = new byte[8];    //Sedentary reminder
                            byte[] userInfoByte = new byte[8];     //User Info
                            byte[] notifyByte = new byte[1];       //Reminder mode
                            byte[] disturbByte = new byte[5];      //Do not disturb setting
                            byte[] heartByte = new byte[6];        //Heart rate detection     byte[] heartByte = new byte[7];  TODO--- Modified to 6 0703 17:41
                            byte[] systemByte = new byte[4];       //System settings
                            byte[] waterByte = new byte[8];        //Drinking water reminder
                            byte[] notificationByte = new byte[2]; //Message push
                            byte[] goalByte = new byte[4];         //Moving target
                            byte[] gestureByte = new byte[3];        //Gesture bright screen

                            System.arraycopy(bytes, 5, clockByte, 0, clockByte.length);  // The alarm clock starts at 5 25
                            System.arraycopy(bytes, 5 + clockByte.length, sedentaryByte, 0, sedentaryByte.length);  // Sedentary
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length, userInfoByte, 0, userInfoByte.length); // User Info
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length, notifyByte, 0, notifyByte.length); // Reminder mode
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length, disturbByte, 0, disturbByte.length); // Do not disturb setting
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length + disturbByte.length, heartByte, 0, heartByte.length); // Heart rate detection
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length + disturbByte.length + heartByte.length, systemByte, 0, systemByte.length); // System settings
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length + disturbByte.length + heartByte.length + systemByte.length, waterByte, 0, waterByte.length); // Drinking water reminder
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length + disturbByte.length + heartByte.length + systemByte.length + 2, goalByte, 0, goalByte.length); // Moving target
                            System.arraycopy(bytes, 5 + clockByte.length + sedentaryByte.length + userInfoByte.length + notifyByte.length + disturbByte.length + heartByte.length + systemByte.length + waterByte.length + 2 + 4, gestureByte, 0, gestureByte.length); // Gesture bright screen

                            ArrayList<AlarmClockData> alarmList = new ArrayList<>();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            for (int i = 0; i < (clockByte.length / 5); i++) {
                                Log.i(TAG, "Alarm clock" + (i + 1) + "  hour:" + clockByte[i * 5]);
                                Log.i(TAG, "Alarm clock" + (i + 1) + "  minute:" + clockByte[(i * 5) + 1]);
                                Log.i(TAG, "Alarm clock" + (i + 1) + "  repeat:" + Utils.getBinaryStrFromByte(clockByte[(i * 5) + 2]));
                                Log.i(TAG, "Alarm clock" + (i + 1) + "  label:" + clockByte[(i * 5) + 3]);
                                Log.i(TAG, "Alarm clock" + (i + 1) + "  switch:" + clockByte[(i * 5) + 4]);
//                            if(clockByte[i*5+4] ==1){ //todo -- No need to switch
                                AlarmClockData alarmClock = new AlarmClockData();
                                if (clockByte[(i * 5) + 3] != 1) { // The tag bit is 1 indicating that the user has set
                                    continue;
                                }
                                int hour = clockByte[i * 5];
                                int minute = clockByte[i * 5 + 1];
                                if (hour > 23 || hour < 0){    //Whether the hour is greater than 23 hours or less than 0 hours, unified as 0 hours
                                    hour = 0;
                                }
                                if (minute > 59 || minute < 0){ //Whether the minute is greater than 59 minutes or less than 0 minutes, unified as 0 minutes
                                    minute = 0;
                                }

                                alarmClock.setRemind(clockByte[(i * 5) + 3] + "");  // label
                                alarmClock.setTime(String.format(Locale.ENGLISH,"%02d", hour) + ":" + String.format(Locale.ENGLISH,"%02d", minute));
                                alarmClock.setCycle(Utils.getBinaryStrFromByte(clockByte[(i * 5) + 2]));
                                alarmClock.setMac(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC));
                                alarmClock.setMid(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MID));
                                alarmClock.setType(clockByte[i * 5 + 4] + "");
                                alarmClock.setAlarm_time(simpleDateFormat.format(new Date()) + " " + String.format(Locale.ENGLISH,"%02d", hour) + ":" + String.format(Locale.ENGLISH,"%02d", minute));
                                alarmClock.setUpload("0");
                                alarmList.add(alarmClock);
//                            }
                            }
                            if (db == null) {
                                db = DBHelper.getInstance(sContext);
                            }
                            Query query = db.getAlarmClockDataDao().queryBuilder()
                                    .where(AlarmClockDataDao.Properties.Mac.eq(SharedPreUtil.readPre(getApplicationContext(), SharedPreUtil.USER, SharedPreUtil.MAC)))
                                    .build();   //  .where(AlarmClockDataDao.Properties.Type.eq("1")) 不用管开关

//                        List ddd =  query.list();
                            while (query.list().size() != 0) {
                                for (int i = 0; i < query.list().size(); i++) {
                                    db.DeleteAlarmClockData((AlarmClockData) query.list().get(i));
                                }
                            }

                            if (alarmList.size() > 0) {
                                for (int i = 0; i < alarmList.size(); i++) {
                                    db.saveAlarmClockData(alarmList.get(i));
                                }
                            }
//                        List ddd2 =  query.list();

                            Log.e(TAG, "Sedentary switch:" + sedentaryByte[0]);
                            Log.e(TAG, "Sedentary start time:" + sedentaryByte[1]);
                            Log.e(TAG, "Sedentary end time:" + sedentaryByte[2]);
                            Log.e(TAG, "Sedentary repetition:" + Utils.getBinaryStrFromByte(sedentaryByte[3]).substring(0, 7));
                            Log.e(TAG, "Sedentary time:" + (((sedentaryByte[4] << 8) & 0xff00) | (sedentaryByte[5] & 0xff)));
                            Log.e(TAG, "Sedentary threshold:" + (((sedentaryByte[6] << 8) & 0xff00) | (sedentaryByte[7] & 0xff)));
                            int sedentaryStart = sedentaryByte[1];   //Sedentary start time
                            int sedentaryEnd = sedentaryByte[2];     //Sedentary end time
                            int sedentaryTime = ((sedentaryByte[4] << 8) & 0xff00) | (sedentaryByte[5] & 0xff);  //Sedentary time
                            int sedentaryStep = ((sedentaryByte[6] << 8) & 0xff00) | (sedentaryByte[7] & 0xff);  //Sedentary threshold
                            if (sedentaryStart > 23 || sedentaryStart < 0){   //Whether the hour is greater than 23 hours or less than 0 hours, unified as 0 hours
                                sedentaryStart = 0;
                            }
                            if (sedentaryEnd > 23 || sedentaryEnd < 0){    //Whether the hour is greater than 23 hours or less than 0 hours, unified as 0 hours
                                sedentaryEnd = 0;
                            }
                            List<String> sedentaryHourList = Utils.getSitList();
                            List<String> sedentaryStepList = Utils.getStepList();
                            int sedentaryTimes = 0;
                            int sedentarySteps = 0;
                            for (int i = 0; i < sedentaryHourList.size(); i++) {    //Sedentary time to determine whether it is 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 360
                                if(sedentaryTime == Integer.parseInt(sedentaryHourList.get(i))){
                                    sedentaryTimes = sedentaryTime;
                                    break;
                                }
                            }
                            if(sedentaryTimes == 0){
                                sedentaryTimes = 30;
                            }
                            for (int i = 0; i < sedentaryStepList.size(); i++) {    //The sedentary threshold is judged whether it is 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
                                if(sedentaryStep == Integer.parseInt(sedentaryStepList.get(i))){
                                    sedentarySteps = sedentaryStep;
                                    break;
                                }
                            }
                            if(sedentarySteps == 0){
                                sedentarySteps = 100;
                            }
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_REPEAT_WEEK, Utils.getBinaryStrFromByte(sedentaryByte[3]).substring(0, 7));
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_SWITCH, sedentaryByte[0] == 1 ? true : false);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_START_TIME, sedentaryStart);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_STOP_TIME, sedentaryEnd);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_TIME, sedentaryTimes);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.SIT_STEP, sedentarySteps);


                            Log.i(TAG, "User gender:" + userInfoByte[0]);
                            Log.i(TAG, "User age:" + userInfoByte[1]);
                            Log.i(TAG, "User height:" + userInfoByte[2]);
                            Log.i(TAG, "User weight:" + userInfoByte[3]);
                            Log.i(TAG, "User steps:" + Utils.getInt(userInfoByte, 4));


                            Log.i(TAG, "Reminder mode:" + notifyByte[0]);
                            int notifyMode = notifyByte[0];
                            if(notifyMode > 3 || notifyMode < 1){    //Reminder mode is greater than 3 or less than 1, unified as 1 mode: 1: bright screen; 2: vibration; 3: bright screen + vibration
                                notifyMode = 3;
                            }
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.ALARM_MODE, notifyMode);


                            Log.i(TAG, "Do not disturb switch:" + disturbByte[0]);
                            Log.i(TAG, "Do not disturb start hour:" + disturbByte[1]);
                            Log.i(TAG, "Do not disturb start minutes:" + disturbByte[2]);
                            Log.i(TAG, "Do not disturb end hour:" + disturbByte[3]);
                            Log.i(TAG, "Do not disturb end minutes:" + disturbByte[4]);
                            int disturbStartHour = disturbByte[1];
                            int disturbStartMin = disturbByte[2];
                            int disturbEndHour = disturbByte[3];
                            int disturbEndMin = disturbByte[4];

                            if(disturbStartHour > 23 || disturbStartHour < 0){
                                disturbStartHour = 0;
                            }

                            if(disturbStartMin != 0 && disturbStartMin != 30){
                                disturbStartMin = 0;
                            }

                            if(disturbEndHour > 23 || disturbEndHour < 0){
                                disturbEndHour = 0;
                            }

                            if(disturbEndMin != 0 && disturbEndMin != 30){
                                disturbEndMin = 0;
                            }

                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.NO_START_TIME, disturbStartHour);//Do not disturb start hours
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.NO_START_TIME_MIN, disturbStartMin);//Do not disturb start minutes
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.NO_STOP_TIME, disturbEndHour);//Do not disturb the end of the hour
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.NO_STOP_TIME_MIN, disturbEndMin);//Do not disturb the minute
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.NO_SWITCH, disturbByte[0] == 1 ? true : false);


                            Log.i(TAG, "Heart rate detection switch:" + heartByte[0]);  // Heart rate detection is now changed to 6
                            Log.i(TAG, "Heart rate detection start hour:" + heartByte[1]);
                            Log.i(TAG, "Heart rate detection starts minutes:" + heartByte[2]);
                            Log.i(TAG, "Heart rate detection end hour:" + heartByte[3]);
                            Log.i(TAG, "Heart rate detection end minutes:" + heartByte[4]);
                            Log.i(TAG, "Heart rate detection end interval:" + heartByte[5]);

                            int heartStartHour = heartByte[1];
                            int heartStartMin = heartByte[2];
                            int heartEndHour = heartByte[3];
                            int heartEndMin = heartByte[4];
                            int heartFrequency = heartByte[5];

                            if(heartStartHour > 23 || heartStartHour < 0){
                                heartStartHour = 0;
                            }
                            if(heartStartMin != 0 && heartStartMin != 30){
                                heartStartMin = 0;
                            }
                            if(heartEndHour > 23 || heartEndHour < 0){
                                heartEndHour = 0;
                            }
                            if(heartEndMin != 0 && heartEndMin != 30){
                                heartEndMin = 0;
                            }
                            int frequency = 0;
                            List<String> frequencyList = Utils.getHeartList();
                            for (int i = 0; i < frequencyList.size(); i++) {
                                if(heartFrequency == Integer.parseInt(frequencyList.get(i))){
                                    frequency = heartFrequency;
                                    break;
                                }
                            }
                            if(frequency == 0){
                                frequency = 10;
                            }
//                        Log.i(TAG,"心率检测结束预留:" + heartByte[6]);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_SWITCH, heartByte[0] == 1 ? true : false);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_START_TIME, heartStartHour); // 开始时间小时
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_START_TIME_MIN, heartStartMin); // 开始时间分钟
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_STOP_TIME, heartEndHour); // 结束时间小时
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_STOP_TIME_MIN, heartEndMin); // 结束时间分钟
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.HEART_FREQUENCY, frequency);  // 心率检测结束间隔


                            Log.i(TAG, "System setting language:" + systemByte[0]);
                            Log.i(TAG, "System setting hourly system:" + systemByte[1]);
                            Log.i(TAG, "System settings bright screen:" + systemByte[2]);
                            Log.i(TAG, "System settings phone pairing:" + systemByte[3]);

                            Log.i(TAG, "Drinking water reminder switch:" + waterByte[0]);
                            Log.i(TAG, "Drink water reminder start hour:" + waterByte[1]);
                            Log.i(TAG, "Drink water reminder to start minutes:" + waterByte[2]);
                            Log.i(TAG, "Drink water reminder end hour:" + waterByte[3]);
                            Log.i(TAG, "Drink water reminder end minutes:" + waterByte[4]);
                            Log.i(TAG, "Drinking water reminder repeat:" + Utils.getBinaryStrFromByte(waterByte[5]).substring(0, 7));
                            Log.i(TAG, "Drinking water reminder interval:" + (((waterByte[6] << 8) & 0xff00) | (waterByte[7] & 0xff)));

                            int waterStartHour = waterByte[1];
                            int waterStartMin = waterByte[2];
                            int waterEndHour = waterByte[3];
                            int waterEndMin = waterByte[4];
                            int waterFrequency = ((waterByte[6] << 8) & 0xff00) | (waterByte[7] & 0xff);

                            if(waterStartHour > 23 || waterStartHour < 0){
                                waterStartHour = 0;
                            }
                            if(waterStartMin != 0 && waterStartMin != 30){
                                waterStartMin = 0;
                            }
                            if(waterEndHour > 23 || waterEndHour < 0){
                                waterEndHour = 0;
                            }
                            if(waterEndMin != 0 && waterEndMin != 30){
                                waterEndMin = 0;
                            }
                            List<String> waterList = Utils.getDrinkList();
                            int waters = 0;
                            for (int i = 0; i < waterList.size(); i++) {
                                if(waterFrequency == Integer.parseInt(waterList.get(i))){
                                    waters = waterFrequency;
                                    break;
                                }
                            }
                            if(waters == 0){
                                waters = 30;
                            }
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_SWITCH, waterByte[0] == 1 ? true : false);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_REPEAT_WEEK, Utils.getBinaryStrFromByte(waterByte[5]).substring(0, 7));
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_START_TIME, waterStartHour);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_START_TIME_MIN, waterStartMin);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_STOP_TIME, waterEndHour);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_STOP_TIME_MIN, waterEndMin);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.DRINK_FREQUENCY, waters);

                            Log.i(TAG, "Gesture intelligence control--right hand----" + gestureByte[0]);
                            Log.i(TAG, "Gesture intelligence control--raise your hand to brighten----:" + gestureByte[1]);
                            Log.i(TAG, "Gesture intelligence control--turning the wrist bright screen----:" + gestureByte[2]);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.GESTURE_HAND, (int)gestureByte[0]);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.RAISE_BRIGHT, gestureByte[1] == 1 ? true : false);
                            SharedPreUtil.setParam(sContext, SharedPreUtil.USER, SharedPreUtil.FANWAN_BRIGHT, gestureByte[2] == 1 ? true : false);
                        }
                    } else if (byte1 == BleConstants.WEATHER_PROPELLING) {           //TODO -- 天气推送命令     0x03

                    } else if (byte1 == BleConstants.DEVICE_COMMAND) {               //TODO -- 设备命令         0x04
                        switch (bytes[2]) {   //todo -----  key类型
                            case BleConstants.CAMERA_OPEN: //  打开拍照
                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")){
                                    if ((Boolean) SharedPreUtil.getParam(sContext, SharedPreUtil.USER, SharedPreUtil.TB_CAMERA_NOTIFY, true)){
                                        Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
                                        sendBroadcast(intent);
                                    }
                                }else if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
                              /*  if (RemoteCamera.isSendExitTakephoto) {
                                    return;
                                } else {
                                    Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent);
                                }*/
                                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                if (state) {  // 是否打开了相机
//                                    Intent intentCamera = new Intent(BTNotificationApplication.getInstance(), CameraActivity.class);
//                                    startActivity(intentCamera);
//                                }

                                    if(!CameraActivity.isPhoneExitTakephoto){  // 手机端主动退出
//                                    Intent intentCamera = new Intent(BTNotificationApplication.getInstance(), CameraActivity.class);     // todo  --- 打开 拍照
//                                    startActivity(intentCamera);

                                        Intent intentCamera = new Intent(getApplicationContext(), CameraActivity.class);     // todo  --- 打开 拍照              BTNotificationApplication.getInstance()    getApplicationContext()  sContext   getBaseContext
                                        intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intentCamera);
                                    }else{
                                        CameraActivity.isPhoneExitTakephoto = false;
                                    }


                                } else {
                                    Intent intent = new Intent("0x46");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent);
                                }
                                break;

                            case BleConstants.CAMERA_TAKE: // 拍照
//                            Intent intent2 = new Intent("0x47");   // 发送远程拍照 --- 对应的广播
//                            sendBroadcast(intent2);

                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
                                    sendBroadcast(new Intent(ACTION_REMOTE_CAMERA));
                                }
                                else if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1"))
                                {
                                    boolean b = (boolean) SharedPreUtil.getParam(this, SharedPreUtil.USER, SharedPreUtil.TB_CAMERA_NOTIFY, true);
                                    if(b)
                                    {
                                        Intent intent2 = new Intent("0x47");   // 发送远程拍照 --- 对应的广播
                                        sendBroadcast(intent2);
                                    }
                                }
                                else {
                                    Intent intent2 = new Intent("0x47");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent2);
                                }


                                break;

                            case BleConstants.CAMERA_CLOSE: // 退出拍照
//                            Intent intent3 = new Intent("0x48");   // 发送远程拍照 --- 对应的广播
//                            sendBroadcast(intent3);
//                            isDeviceSendExitCommand = true;

//                            sendBroadcast(new Intent(ACTION_REMOTE_CAMERA_EXIT));

                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WATCH).equals("2")) {
                                    sendBroadcast(new Intent(ACTION_REMOTE_CAMERA_EXIT));
                                }else {
                                    Intent intent3 = new Intent("0x48");   // 发送远程拍照 --- 对应的广播
                                    sendBroadcast(intent3);
                                    isDeviceSendExitCommand = true;
                                }
                                break;

                            case BleConstants.KEY_WIFI_PASSWORD:  // wifi 需要密码  ---- 还未用到

                                break;

                            case BleConstants.KEY_WIFI_LINK:  // 已保存的 wifi
                                break;

                            case BleConstants.KEY_WIFI_NOPASSWORD:  //  wifi 无需密码

                                break;

                            case BleConstants.KEY_WIFI_LIST:  // -- 手表WiFi     // BleConstants.SYN_WIFI TODO --- 手表返回错误 （0x19 --- 25）   KEY_WIFI_LIST  ---- 手表端返回WiFi列表
                                String resbytes = new String(bytes);
                                Log.e(TAG, "bytes.toString----" + resbytes);

                                byte[] newByte = new byte[bytes.length - 5];
                                System.arraycopy(bytes, 5, newByte, 0, bytes.length - 5);
                                String resnewbytes = new String(newByte);
                                Log.e(TAG, "newbytes.toString----" + resnewbytes);

                                Intent intent = new Intent();
                                intent.setAction(MainService.ACTION_WIFIINFO);  // TODO --- 收到 WiFi 数据后 发广播 更新 WiFi 列表
                                intent.putExtra("wifidata", resnewbytes);
                                sendBroadcast(intent);

                                break;

                            case BleConstants.DIAL_RETURN: // 发送绑定设备后，手表返回  表盘数据返回

                                byte[] newByte2 = new byte[bytes.length - 5];   // 去掉前面5byte 第6byte开始为 手表实际返回数据
                                System.arraycopy(bytes, 5, newByte2, 0, bytes.length - 5);
                                String resnewbytes2 = new String(newByte2);
                                Log.e(TAG, "newbytes.toString----" + resnewbytes2);

                                String ss = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL);
                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL).equals(resnewbytes2)) {
                                    return;
                                } else {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    Log.e("readMessagereadcontant", resnewbytes2);
                                    Intent intentNew = new Intent();
                                    intentNew.setAction(MainService.ACTION_CLOCK_SKIN_MODEL_CHANGE);

                                    SharedPreUtil.savePre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.CLOCK_SKIN_MODEL, resnewbytes2);
                                    // intent.putExtra("type", readMessagereadcontant);
                                    sendBroadcast(intentNew);
                                }
                                break;
                            case (byte)0x50:
                                byte[] bytes1 = new byte[bytes.length - 5];
                                System.arraycopy(bytes,5,bytes1,0,bytes.length - 5);
                                try {
                                    String watchPushData = new String(bytes1,"utf-8");
                                    Log.e(TAG,"watchPushData = " + watchPushData);
                                    SharedPreUtil.setParam(sContext,SharedPreUtil.USER,SharedPreUtil.CLOCK_SKIN_MODEL,watchPushData);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case BleConstants.KEY_INPUTASSIT_SEND:        //协助输入
                                EventBus.getDefault().post("assistInput_success");
                                break;
                        }
                        if (bytes[2] == BleConstants.ELECTRIC_RETURN) {
                            int battary = bytes[5];
                            Log.i(TAG, "Device power： " + battary);
                            int battaryType = bytes[6];
                            Log.i(TAG, "Device status: " + battaryType);
                        }
                    } else if (byte1 == BleConstants.FIND_COMMAND) {                        //TODO -- 查找命令   0x05
//                    if (bytes[2] == BleConstants.FIND_PHONE) {   // -------  key
//                        showDialog();        //todo ----     Find more bags
//                    }
                        if (bytes[2] == BleConstants.FIND_DEVICE) { // 找手表  ---- 手表端回复命令
                            Intent intent = new Intent();
                            intent.setAction(MainService.ACTION_GETWATCH);   // 发广播，销毁加载框
                            sendBroadcast(intent);
                        }
                    } else if (byte1 == BleConstants.REMIND_COMMAND) {                      //TODO -- 提醒命令   0x06

                    } else if (byte1 == BleConstants.RUN_MODE_COMMADN) {                     //TODO -- 运动模式命令   0x07   多包
                        if(bytes[2] == BleConstants.RUN_BASE_RETURN){
                            GpsPointDetailData gpsPointDetailData = new GpsPointDetailData();
                            String year = String.format(Locale.ENGLISH,"20" + "%02d", bytes[5]);
                            String month = String.format(Locale.ENGLISH,"%02d", bytes[6]);
                            String day = String.format(Locale.ENGLISH,"%02d", bytes[7]);
                            String hour = String.format(Locale.ENGLISH,"%02d", bytes[8]);
                            String minute = String.format(Locale.ENGLISH,"%02d", bytes[9]);
                            String second = String.format(Locale.ENGLISH,"%02d", bytes[10]);
                            int mode = bytes[11];
                            int time = Utils.getInt(bytes,12);
                            float distance = Utils.byte2float(bytes,16);
                            int step = Utils.getInt(bytes,20);
                            float calorie = Utils.byte2float(bytes,24);
                            int pauseTime = (((bytes[28] << 8) & 0xff00) | (bytes[29] & 0xff));
                            int pauseNumber = (((bytes[30] << 8) & 0xff00) | (bytes[31] & 0xff));
                            int maxWidth = bytes[32] & 0xff;
                            int avgWidth = bytes[33] & 0xff;
                            int minWidth = bytes[34] & 0xff;
                            int heartNumber = (((bytes[35] << 8) & 0xff00) | (bytes[36] & 0xff));
                            gpsPointDetailData.setMac(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC));
                            gpsPointDetailData.setMid(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MID));
                            gpsPointDetailData.setCalorie(calorie + "");
                            gpsPointDetailData.setSpeed("0");
                            gpsPointDetailData.setAve_step_width(avgWidth + "");
                            gpsPointDetailData.setMax_step_width(maxWidth + "");
                            gpsPointDetailData.setMin_step_width(minWidth + "");
                            gpsPointDetailData.setSportType(mode + "");
                            gpsPointDetailData.setMile(distance);
                            gpsPointDetailData.setDate(year + "-" + month + "-" + day + " " + hour + ":" + minute);
                            gpsPointDetailData.setDeviceType("2");
                            gpsPointDetailData.setPauseNumber(pauseNumber + "");
                            gpsPointDetailData.setPauseTime(pauseTime + "");
                            gpsPointDetailData.setSportTime(String.format(Locale.ENGLISH, "%1$02d:%2$02d:%3$02d", time / 60 / 60, time / 60 % 60, time % 60));
                            gpsPointDetailData.setAltitude("0");
                            gpsPointDetailData.setmCurrentSpeed("0");
                            gpsPointDetailData.setHeartRate("0");
                            gpsPointDetailData.setStep(step + "");
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                            try {
                                gpsPointDetailData.setsTime(time + "");
                                gpsPointDetailData.setTimeMillis(simpleDateFormat.parse(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second).getTime() /1000 + "");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(heartNumber > 0) {
                                String heartBuffer = "";
                                String frequencyBuffer = "";
                                String speedBuffer = "";
                                for (int i = 0; i < heartNumber; i++) {
                                    int heart = bytes[i * 7 + 37] & 0xFF;
                                    float frequency = (((bytes[(i * 7) + 1 + 37] << 8) & 0xff00) | (bytes[(i * 7) + 2 + 37] & 0xff));
                                    float speed = Utils.byte2float(bytes,(i * 7) + 3 + 37);
                                    Log.e(TAG,"heart = " + heart + " ; frequency = " + frequency + " ; speed = " + speed);
                                    if(heart > 200 || frequency < 0 || heart < 0 || speed < 0){
                                        continue;
                                    }
                                    heartBuffer += (heart + "&");
                                    frequencyBuffer += (frequency + "&");
                                    speedBuffer += (speed + "&");
                                }
                                if(TextUtils.isEmpty(heartBuffer)){
                                    gpsPointDetailData.setArrheartRate("0");
                                }else{
                                    gpsPointDetailData.setArrheartRate(heartBuffer);
                                }
                                if(TextUtils.isEmpty(frequencyBuffer)){
                                    gpsPointDetailData.setArrcadence("0");
                                }else{
                                    gpsPointDetailData.setArrcadence(frequencyBuffer);
                                }
                                if(TextUtils.isEmpty(speedBuffer)){
                                    gpsPointDetailData.setArrspeed("0");
                                }else{
                                    gpsPointDetailData.setArrspeed(speedBuffer);
                                }
                            }else{
                                gpsPointDetailData.setArrheartRate("0");
                                gpsPointDetailData.setArrcadence("0");
                                gpsPointDetailData.setArrspeed("0");
                            }
                            int paceNumber = (((bytes[heartNumber * 7 + 37] << 8) & 0xff00) | (bytes[heartNumber * 7 + 38] & 0xff));
                            if(paceNumber > 0){
                                String paceBuffer = "";
                                for (int i = 0; i < paceNumber; i++) {
                                    int pace = (((bytes[(i * 2) + heartNumber * 7 + 39] << 8) & 0xff00) | (bytes[(i * 2) + heartNumber * 7 + 1 + 39] & 0xff));
                                    if(pace > 0) {
                                        paceBuffer += (String.format(Locale.ENGLISH,"%1$02d'%2$02d''", pace / 60, pace % 60) + "&");
                                    }
                                    Log.e(TAG,"paceBuffer = " + paceBuffer);
                                }
                                if(TextUtils.isEmpty(paceBuffer)){
                                    gpsPointDetailData.setArrTotalSpeed("0");
                                }else{
                                    gpsPointDetailData.setArrTotalSpeed(paceBuffer);
                                }
                            }else{
                                gpsPointDetailData.setArrTotalSpeed("0");
                            }
                            int latlngNumber = (((bytes[paceNumber * 2 + heartNumber * 7 + 39] << 8) & 0xff00) | (bytes[paceNumber * 2 + heartNumber * 7 + 40] & 0xff));
                            if(latlngNumber > 0){
                                String latBuffer = "";
                                String lngBuffer = "";
                                String altitudeBuffer = "";
                                for (int i = 0; i < latlngNumber; i++) {
                                    double lng = (double)Utils.getInt(bytes,i * 10 + paceNumber * 2 + heartNumber * 7 + 41) / 1000000;
                                    double lat = (double)Utils.getInt(bytes,i * 10 + paceNumber * 2 + heartNumber * 7 + 4 + 41) / 1000000;
                                    float altitude = ((bytes[i * 10 + paceNumber * 2 + heartNumber * 7 + 4 + 4 + 41] << 8) & 0xff00) | (bytes[i * 10 + 4 + 4 + 1 + 41] & 0xff);
                                    Log.e(TAG,"lng = " + lng + " ; lat = " + lat + " ; altitude = " + altitude);
                                    if(lng == -1  && lat == -1){
                                        continue;
                                    }
                                    lngBuffer += (lng + "&");
                                    latBuffer += (lat + "&");
                                    altitudeBuffer += (altitude + "&");
                                }
                                if(TextUtils.isEmpty(latBuffer)){
                                    gpsPointDetailData.setArrLat("0");
                                }else{
                                    gpsPointDetailData.setArrLat(latBuffer);
                                }
                                if(TextUtils.isEmpty(lngBuffer)){
                                    gpsPointDetailData.setArrLng("0");
                                }else{
                                    gpsPointDetailData.setArrLng(lngBuffer);
                                }
                                if(TextUtils.isEmpty(altitudeBuffer)){
                                    gpsPointDetailData.setArraltitude("0");
                                }else{
                                    gpsPointDetailData.setArraltitude(altitudeBuffer);
                                }
                            }else{
                                gpsPointDetailData.setArrLat("0");
                                gpsPointDetailData.setArrLng("0");
                                gpsPointDetailData.setArraltitude("0");
                            }
                            Log.e(TAG,"year = " + year + " ; month = " + month + " ; day = " + day + " ; hour = " + hour + " ; minute = " + minute + " ; second = " + second + " ; mode = " + mode + " ; time = " + time + " ; distance = " + distance + " ; step = " + step +
                                    " ; calorie = " + calorie + " ; pauseTime = " + pauseTime + " ; pauseNumber = " + pauseNumber + " ; maxWidth = " + maxWidth + " ; avgWidth = " + avgWidth + " ; minWidth = " + minWidth);
                            gpsList.add(gpsPointDetailData);
                            saveSpoetData(gpsList);
                            gpsList.clear();
                        }

                    } else if (byte1 == BleConstants.SLEEP_COMMAND) {                        //TODO -- 睡眠命令     0x08

                    } else if (byte1 == BleConstants.HEART_COMMAND) {                           //TODO -- 心率命令   0x09

                    } else if (byte1 == BleConstants.SYN_COMMAND) {                           //TODO -- 同步命令      0x0A
                        if (bytes[2] == BleConstants.BRACELET_RUN_DATA_RETURN) {               //TODO 手环运动数据返回  ---历史  (0xA3)
                            // 0A00A3006411081D00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004B4000004B400000AC000000B2000000CEA000014180000197500001F1400001F48
                            // 0A00A30000
                            if(!BTNotificationApplication.isSyncEnd) {
                                if(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                    needReceDataNumber = 1;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                    intent.putExtra("step", "1");
                                    sContext.sendBroadcast(intent);
                                }else{        //(手环)
                                    getSyncDataNumInService++;  //todo ---- getSyncDataNumInService 值对应设备端有几天的数据

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }
                                    }//////////////////////////////////////////////////////////////////

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                }
                            }

                            int l2ValueLength = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            if (l2ValueLength == 0) {
                                return;
                            }

                            if (l2ValueLength == 100) {   //旧协议
//                            if(Locale.getDefault().getLanguage().equalsIgnoreCase("ar")){ //todo ---  阿拉伯语
//                                String ttd = String.format(Locale.ENGLISH,"20" + "%02d", bytes[5] & 0xff);  // 2018
//                                String tt = String.format(Locale.getDefault(),"20" + "%02d", bytes[5] & 0xff);  // 20١٨
//                                String year2 = String.format("20" + "%02d", bytes[5] & 0xff);   // 20١٨
//                                String ddd  = "554";
//                            }else {
//                                String year = String.format("20" + "%02d", bytes[5] & 0xff);   // 2018
//                                String dd  = "554";
//                            }

                                String year = String.format(Locale.ENGLISH,"20" + "%02d", bytes[5] & 0xff);
                                Log.i(TAG, "run year =" + year);
                                String mouth = String.format(Locale.ENGLISH,"%02d", bytes[6] & 0xff);
                                Log.i(TAG, "run mouth =" + mouth);
                                String day = String.format(Locale.ENGLISH,"%02d", bytes[7] & 0xff);
                                Log.i(TAG, "run day =" + day);
                                String hour = String.format(Locale.ENGLISH,"%02d", bytes[8] & 0xff);
                                Log.i(TAG, "run hour =" + hour);
                                int[] oneDayStep = new int[24];   //对应1-24点之间的时间段 步数增量值
                                int j = 0;
                                for (int i = 9; i < 105; i++) {   //
                                    int pp = i % 4;
                                    if (pp == 0) { // 16 --- 20   //
                                        byte[] bb = new byte[4];
                                        bb[3] = bytes[i];   //
                                        bb[2] = bytes[i - 1];
                                        bb[1] = bytes[i - 2];  //
                                        bb[0] = bytes[i - 3];
                                        int mindex = ++j;
                                        int dddd = NumberBytes.byteArrayToInt(bb);
                                        oneDayStep[mindex - 1] = NumberBytes.byteArrayToInt(bb);
                                    }
                                }

//                        int runCount =  oneDayStep[23]; // 步数  --- 一天的总步数值
                                for (int p = 0; p < 24; p++) {
                                    Log.i(TAG, "oneDayStep--" + p + "----" + oneDayStep[p]);
                                }

                                List<StepData> dataList = new ArrayList<>();

                                int hasStepTemp = 0;
                                for (int i = 0; i < oneDayStep.length; i++) {
                                    int goalSum = oneDayStep[i];
                                    int realStep = 0;  // 时间段实际步数值
                                    StepData stepData = new StepData();
                                    String mcurTime = year + "-" + mouth + "-" + day + " " + String.format(Locale.ENGLISH,"%02d", i) + ":" + "00:00";  // 20١٨-٠٧-٢٨ ٠٠:00:00
                                    Date date = StringUtils.parseStrToDate(mcurTime, StringUtils.SIMPLE_DATE_FORMAT);
                                    if (date != null) {
                                        stepData.setTime(date.getTime() / 1000 + "");
                                        if (hasStepTemp == 0 || SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {  //智能表传入的是增量数据
                                            stepData.setCounts(goalSum + "");
                                            realStep = goalSum;
                                        } else {

                                            int zhengliang = goalSum - hasStepTemp;
                                            if (zhengliang < 0) {
                                                zhengliang = 0;
                                                stepData.setCounts(zhengliang + "");
                                                realStep = zhengliang;
                                            } else {
                                                stepData.setCounts(zhengliang + "");
                                                realStep = zhengliang;
                                            }
                                        }
                                        Log.i(TAG, "oneDayStep--Actual step increment ---- " + realStep);

                                        if (hasStepTemp != goalSum && goalSum > hasStepTemp) {   // hasStepTemp != goalSum
                                            hasStepTemp = goalSum;
                                        }

                                        int userWeightI = 60;
                                        String userWeight = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.WEIGHT, "60");
                                        if (StringUtils.isEmpty(userWeight) || userWeight.equals("0")) {
                                            userWeightI = 60;
                                        } else {
                                            userWeightI = Integer.valueOf(userWeight);
                                        }

                                        int userHeightI = 170;
                                        String userHeight = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.HEIGHT, "170");
                                        if (StringUtils.isEmpty(userHeight) || userHeight.equals("0")) {
                                            userHeightI = 170;
                                        } else {
                                            userHeightI = Integer.valueOf(userHeight);
                                        }

                                        double calorie = 0.00;
                                        if (realStep > 0) {
                                            calorie = Double.valueOf(String.format(Locale.ENGLISH, "%1$.3f", realStep * (((float) (userWeightI) - 15) * 0.000693 + 0.005895))); //TODO  BLE手环给的计算公式
                                            //calorie = Double.valueOf(String.format(Locale.ENGLISH,"%1$.2f", ((float)(userWeightI) * (float) ((realStep * 0.7) / 1000.0) * 1.036)));  // 卡路里     0,00    Locale.ENGLISH
                                        }
                                        String distance = String.format(Locale.ENGLISH, "%.3f", (realStep * (0.415 * (float) userHeightI) / 100000));  //TODO BLE手环给的计算公式
                                        //String distance = String.format(Locale.ENGLISH,"%.2f", (realStep * 0.7) / 1000.0);     // 运动距离
//                                String distance = String.format("%.2f", (realStep * 0.7) / 1000.0);     // 运动距离
                                        stepData.setCalorie(calorie + "");
                                        stepData.setDistance(distance + "");
                                        dataList.add(stepData);
                                    }
                                    Log.e(TAG, "goalSum = " + goalSum + " ; hasStepTemp = " + hasStepTemp + " ; time = " + mcurTime + " ;  binTime = " + stepData.getTime() + " ; goal = " + realStep);
                                    Log.i(TAG, "run goal = " + realStep + "  time = " + i);
                                }

                                BTdataWrite(dataList);
                            }else {      //新协议(388字节)
                                String year = String.format(Locale.ENGLISH,"20" + "%02d", bytes[5] & 0xff);
                                Log.i(TAG, "run year =" + year);
                                String mouth = String.format(Locale.ENGLISH,"%02d", bytes[6] & 0xff);
                                Log.i(TAG, "run mouth =" + mouth);
                                String day = String.format(Locale.ENGLISH,"%02d", bytes[7] & 0xff);
                                Log.i(TAG, "run day =" + day);
                                String hour = String.format(Locale.ENGLISH,"%02d", bytes[8] & 0xff);
                                Log.i(TAG, "run hour =" + hour);
                                List<StepData> dataList = new ArrayList<>();
                                int j = 0;
                                for (int i = 9; i < bytes.length; i+=16) {
                                    int dayStep = Utils.getInt(bytes,i);
                                    int runStep = Utils.getInt(bytes,i+4);
                                    float calorie = Utils.byte2float(bytes,i+8);
                                    float distance = Utils.byte2float(bytes,i+12);
                                    Log.i(TAG,"oneDayStep--Actual step increment ---- " + dayStep + "; oneRunStep--Actual running increment ---- " + runStep + "; calorie = " + calorie + "; distance = " + distance);
                                    StepData stepData = new StepData();
                                    String mcurTime = year + "-" + mouth + "-" + day + " " + String.format(Locale.ENGLISH,"%02d", j) + ":" + "00:00";
                                    Date date = StringUtils.parseStrToDate(mcurTime, StringUtils.SIMPLE_DATE_FORMAT);
                                    if (date != null) {
                                        stepData.setTime(date.getTime() / 1000 + "");
                                        if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {  //智能表传入的是增量数据
                                            stepData.setCounts(dayStep + "");
                                            stepData.setCalorie(calorie + "");
                                            stepData.setDistance(distance + "");
                                        } else {
                                            stepData.setCounts(dayStep - hasStepTemp + "");
                                            stepData.setDistance(calorie - hasCalorieTemp + "");
                                            stepData.setCalorie(distance - hasDistanceTemp + "");
                                        }
                                        Log.i(TAG, "oneDayStep--Actual step increment ---- " + stepData.getCounts() + "; oneDayCalorie--Actual calorie increment ---- " + stepData.getCalorie() + "; oneDayDistance--Actual distance increment ---- " + stepData.getDistance());

                                        if (hasStepTemp != dayStep && dayStep > hasStepTemp) {   // hasStepTemp != goalSum
                                            hasStepTemp = dayStep;
                                            hasCalorieTemp = calorie;
                                            hasDistanceTemp = distance;
                                        }

                                        dataList.add(stepData);
                                        j++;
                                    }
                                /*Log.e(TAG, "goalSum = " + goalSum + " ; hasStepTemp = " + hasStepTemp + " ; time = " + mcurTime + " ;  binTime = " + stepData.getTime() + " ; goal = " + realStep);
                                Log.i(TAG, "run goal = " + realStep + "  time = " + i);*/
                                }
                                hasStepTemp = 0;
                                hasRunTemp = 0;
                                hasDistanceTemp = 0;
                                hasCalorieTemp = 0;
                                BTdataWrite(dataList);
                            }
                        } else if (bytes[2] == BleConstants.BRACELET_SLEEP_DATA_RETURN) {             //TODO 手环睡眠数据返回  ---历史  (0xA2)
//                        String resModebyteslx2 = UtilsLX.bytesToHexString(bytes);   // 0A00A2000C110610011600021700000200
                            //         3     5       8     11
                            // 0A00 A2 00 4B 110617 011637 02172101173402173901001400002D01003800010901011A02012401020900020E01022400031201031602032401033102041801042602043701050802050C01051700062F    23号下午到
                            // 0A00A2003C11061901173A02003A01010902010E01012A02013401021202021801022A02032501033302042C010438020521010530020629010639020702000713

                            if(!BTNotificationApplication.isSyncEnd) {
                                if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(智能表)
                                    if(needReceDataNumber == 1){
                                        needReceDataNumber = 2;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // 发数据同步成功的广播
                                        intent.putExtra("step", "2");
                                        sContext.sendBroadcast(intent);
                                    }
                                    if(BTNotificationApplication.needSendDataType == needReceDataNumber){
                                        receiveCountDownTimer.cancel();
                                        receiveCountDownTimer.start();
                                    }


                                } else {
                                    getSyncDataNumInService++;

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送1广播");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "发送5广播");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送1广播");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送2广播");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "发送5广播");
                                        }
                                    }//////////////////////////////////////////////////////////////////

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                }
                            }

                            byte[] sleep = new byte[3];
                            byte[] sleepNext = new byte[3];
                            int l2Length = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));   // 75    --- 60
                            String year = String.format(Locale.ENGLISH,"20" + "%02d", (bytes[5] & 0xff));   // 第5位 年
                            Log.e(TAG, "sleep year =" + year);
                            String month = String.format(Locale.ENGLISH,"%02d", bytes[6] & 0xff);
                            Log.e(TAG, "sleep mouth =" + month);
                            String day = String.format(Locale.ENGLISH,"%02d", bytes[7] & 0xff);
                            Log.e(TAG, "sleep day =" + day);

                            String sleepBeginDay = year + "-" + month + "-" + day;
                            String sleepEndDay = "";

                            int sleepLength = (l2Length - 3) / 3;   // 所有的睡眠数据的组数    ---- 17  应该为 19组
                            List<SleepData> sleepList = new ArrayList<>();
                            boolean hasOtherDay = false;  // 是否跨天
                            for (int i = 0; i < sleepLength; i++) {  // 3
                                System.arraycopy(bytes, (i * 3) + 8, sleep, 0, 3);
                                if ((i + 1) == sleepLength) {
                                    break;
                                }
                                System.arraycopy(bytes, ((i + 1) * 3) + 8, sleepNext, 0, 3);
                                int sleepBeginMode = sleep[0];  // 睡眠模式
                                int sleepBeginHour = sleep[1];  // 睡眠的开始 小时  10进制
                                int sleepBeginMinute = sleep[2]; // 睡眠的开始 分钟
                                Log.e(TAG, "sleepBeginMode =" + sleepBeginMode);
                                Log.e(TAG, "sleepBeginHour = " + sleepBeginHour);
                                Log.e(TAG, "sleepBeginMinute = " + sleepBeginMinute);
                                int sleepEngMode = sleepNext[0];  // 睡眠模式
                                int sleepEndHour = sleepNext[1];   // 睡眠的开始 小时   10进制
                                int sleepEndMinute = sleepNext[2];  // 睡眠的开始 分钟
                                Log.e(TAG, "sleepEngMode =" + sleepEngMode);
                                Log.e(TAG, "sleepEndHour = " + sleepEndHour);
                                Log.e(TAG, "sleepEndMinute = " + sleepEndMinute);
                                long deepSleepTime = 0;   //深睡
                                long lightSleepTime = 0;   //浅睡
                                long notSleepTime = 0; //未睡眠
                                SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                DBHelper db = DBHelper.getInstance(sContext);
                                //         3     5       8     11
                                // 0A00 A2 00 4B 110617 011637 02172101173402173901001400002D01003800010901011A02012401020900020E01022400031201031602032401033102041801042602043701050802050C01051700062F
//                            01 1637    --- 小时分钟
                                String sleepBeginTime = String.format(Locale.ENGLISH,"%02d", sleepBeginHour) + ":" + String.format(Locale.ENGLISH,"%02d", sleepBeginMinute);   //开始时间   小时:分钟
                                String sleepEndTime = String.format(Locale.ENGLISH,"%02d", sleepEndHour) + ":" + String.format(Locale.ENGLISH,"%02d", sleepEndMinute);   //结束时间
                                Log.e(TAG, "sleepBeginTime = " + sleepBeginTime);
                                Log.e(TAG, "sleepEndTime = " + sleepEndTime);
                                SleepData sleepData = new SleepData();

                                if (hasOtherDay) {
                                    sleepBeginDay = sleepEndDay;
                                } else {
                                    if (sleepBeginHour > sleepEndHour) {
                                        hasOtherDay = true;
                                        Date date = new Date();
                                        try {
                                            Date newDay = new DateTime(format.parse(sleepBeginDay)).plusDays(1).toDate();  // 日期
                                            sleepEndDay = dfs.format(newDay).split(" ")[0];  // 结束的日期 加1
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    } else { //开始时间一直小于结束时间（没有跨天的睡眠数据）
                                        if (i == 0) { // 取当天睡眠数据的第一条
                                            if (sleepEndHour <= 12) {   // 全为第2天 0点以后的数据
                                                try {
                                                    Date newDay = new DateTime(format.parse(sleepBeginDay)).plusDays(1).toDate();  // 日期
                                                    sleepEndDay = dfs.format(newDay).split(" ")[0];  // 结束的日期 加1
                                                    sleepBeginDay = sleepEndDay;
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {  // 当天的24点之前有睡眠数据
                                                sleepEndDay = sleepBeginDay;
                                            }
                                        }
                                    }
                                }

                                Log.e(TAG, "sleepEndDay = " + sleepEndDay);
                                Log.e(TAG, "sleepBeginDay = " + sleepBeginDay); //
                                try {
                                    Date begin = dfs.parse(sleepBeginDay + " " + sleepBeginTime);   // "yyyy-MM-dd HH:mm
                                    Date end = dfs.parse(sleepEndDay + " " + sleepEndTime);        // "yyyy-MM-dd HH:mm
                                    long between = (end.getTime() - begin.getTime()) / 1000;//除以1000是为了转换成秒 (睡眠开始时间和结束时间之间的 总秒数)
                                    if (sleepBeginMode == 0) {
                                        notSleepTime = between / 60;    // 0 : 未睡
                                        continue;
                                    } else if (sleepBeginMode == 1) {   // 1: 浅睡 ：分钟数
                                        lightSleepTime = between / 60;
                                    } else {                            // 2: 深睡 ：分钟数
                                        deepSleepTime = between / 60;
                                    }
                                    sleepData.setMac(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC));
                                    sleepData.setMid(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MID));
                                    sleepData.setDeepsleep(deepSleepTime + "");   // 深睡时间  --- 分钟数
                                    sleepData.setLightsleep(lightSleepTime + ""); // 浅睡时间  --- 分钟数
                                    sleepData.setDate(sleepBeginDay);   // 睡眠的日期  -- yyyy-MM-dd
                                    sleepData.setSleepmillisecond((deepSleepTime + lightSleepTime) * 60 * 1000 + "");  // 睡眠总时间的毫秒数
                                    sleepData.setStarttimes(sleepBeginDay + " " + sleepBeginTime + ":00");
                                    sleepData.setEndTime(sleepEndDay + " " + sleepEndTime + ":00");
                                    sleepData.setAutosleep(deepSleepTime + lightSleepTime + ":00");
                                    sleepData.setSleeptype(sleepBeginMode + "");
                                    sleepList.add(sleepData);
                                    Log.e(TAG, "sleepList----" + i + "------" + deepSleepTime + "---" + lightSleepTime + "---" + sleepBeginDay + " " + sleepBeginTime + ":00" + "---" + sleepEndDay + " " + sleepEndTime + ":00");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            String tempDate = "";
                            StringBuffer sbData = new StringBuffer();

                            if (sleepList.size() > 0) {
                                for (int i = 0; i < sleepList.size(); i++) {
                                    if (!sleepList.get(i).getDate().equals(tempDate)) {
                                        tempDate = sleepList.get(i).getDate();
                                        sbData.append(tempDate + "#");
                                    }
                                }
                            }

                            String mData = sbData.toString();
                            if (!StringUtils.isEmpty(mData) && mData.contains("#")) {
                                String[] allData = mData.split("#");
                                if (allData.length == 2) {
                                    Query query1 = db.getSleepDao().queryBuilder().where(SleepDataDao.Properties.Mac.eq(SharedPreUtil.readPre(getApplicationContext(), SharedPreUtil.USER, SharedPreUtil.MAC)))
                                            .where(SleepDataDao.Properties.Date.eq(allData[0])).build();
                                    List<SleepData> list1 = query1.list();

                                    for (int j = 0; j < list1.size(); j++) {
                                        for (int i = 0; i < sleepList.size(); i++) {
                                            if (list1.get(j).getStarttimes().equals(sleepList.get(i).getStarttimes())) {  //todo --- Query whether the sleep start time in the database is equal to the start of sleep passed by the device. If they are equal, it means passing, deleting, not passing, not deleting.
                                                db.deleteSleepData(list1.get(j).getId());
                                            }
                                        }
                                    }

                                    Query query2 = db.getSleepDao().queryBuilder().where(SleepDataDao.Properties.Mac.eq(SharedPreUtil.readPre(getApplicationContext(), SharedPreUtil.USER, SharedPreUtil.MAC)))
                                            .where(SleepDataDao.Properties.Date.eq(allData[1])).build();
                                    List<SleepData> list2 = query2.list();
                                    for (int j = 0; j < list2.size(); j++) {
                                        for (int i = 0; i < sleepList.size(); i++) {
                                            if (list2.get(j).getStarttimes().equals(sleepList.get(i).getStarttimes())) {  //todo --- Query whether the sleep start time in the database is equal to the start of sleep passed by the device. If they are equal, it means passing, deleting, not passing, not deleting.
                                                db.deleteSleepData(list2.get(j).getId());
                                            }
                                        }
                                    }
                                } else {
                                    Query query1 = db.getSleepDao().queryBuilder().where(SleepDataDao.Properties.Mac.eq(SharedPreUtil.readPre(getApplicationContext(), SharedPreUtil.USER, SharedPreUtil.MAC)))
                                            .where(SleepDataDao.Properties.Date.eq(allData[0])).build();
                                    List<SleepData> list1 = query1.list();
                                    for (int j = 0; j < list1.size(); j++) {
                                        for (int i = 0; i < sleepList.size(); i++) {
                                            if (list1.get(j).getStarttimes().equals(sleepList.get(i).getStarttimes())) {  //todo --- Query whether the sleep start time in the database is equal to the start of sleep passed by the device. If they are equal, it means passing, deleting, not passing, not deleting.
                                                db.deleteSleepData(list1.get(j).getId());
                                            }
                                        }
                                    }
                                }
                            }
                            if(sleepList.size() > 0) {
                                SharedPreUtil.savePre(sContext, SharedPreUtil.SLEEP, SharedPreUtil.readPre(this, SharedPreUtil.USER, SharedPreUtil.MAC), sleepList.get(sleepList.size() - 1).getEndTime());
                            }
                            for (int i = 0; i < sleepList.size(); i++) {
                                db.saveSleepData(sleepList.get(i));      // Resave sleep data for the corresponding date
                            }
                        } else if (bytes[2] == BleConstants.BRACELET_HEART_DATA_RETURN) {            //TODO Hand ring heart rate data return  ----history(0xA4)

                            if(!BTNotificationApplication.isSyncEnd) {
                                if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(Smart watch)
                                    if (needReceDataNumber == 1) {
                                        needReceDataNumber = 2;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                        intent.putExtra("step", "2");
                                        sContext.sendBroadcast(intent);
                                    }else if(needReceDataNumber == 2){
                                        needReceDataNumber = 3;
                                        Intent intent = new Intent();
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                        intent.putExtra("step", "3");
                                        sContext.sendBroadcast(intent);
                                    }
                                    if(BTNotificationApplication.needSendDataType == needReceDataNumber) {
                                        receiveCountDownTimer.cancel();
                                        receiveCountDownTimer.start();
                                    }
                                } else {
                                    getSyncDataNumInService++;

                                    Log.e("liuxiaodata", "The data entry that needs to be received is----" + BTNotificationApplication.needReceiveNum);
                                    if (BTNotificationApplication.needReceiveNum == 21) {  // 1/5,2/5
                                        if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 5) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 13) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 16) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 20) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize4));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);

                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }

                                    } else if (BTNotificationApplication.needReceiveNum == 6) {
                                        if (getSyncDataNumInService == 1) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize1));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization     ACTION_SYNFINSH_STEP
                                            intent.putExtra("step", "1");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 1 broadcast");
                                        } else if (getSyncDataNumInService == 2) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize2));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "2");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 2 broadcasts");
                                        } else if (getSyncDataNumInService == 3) {
//                                    HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize3));

                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "3");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 3 broadcasts");
                                        } else if (getSyncDataNumInService == 4) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "4");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 4 broadcasts");
                                        } else if (getSyncDataNumInService == 5) {
                                            Intent intent = new Intent();      // add 0414
                                            intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                            intent.putExtra("step", "5");
                                            sContext.sendBroadcast(intent);
                                            Log.e("liuxiaodata", "Send 5 broadcasts");
                                        }
                                    }

                                    Log.e("liuxiaodata", "The number of data received has been--" + getSyncDataNumInService);
                                    if (getSyncDataNumInService == BTNotificationApplication.needReceiveNum) {  //   BTNotificationApplication.bleSyncDataDays = 7;    HomeFragment.getHistoryDataDays
//                                Intent intent = new Intent();      // add 0414
//                                intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
//                                sContext.sendBroadcast(intent);

                                        Intent intent = new Intent();      // add 0414
                                        intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                        intent.putExtra("step", "6");
                                        sContext.sendBroadcast(intent);
                                        Log.e("liuxiaodata", "Send 6 broadcasts");

//                                HelperFragment.loadingDialog.setText(getString(R.string.userdata_synchronize5));

                                        getSyncDataNumInService = 0;
                                        BTNotificationApplication.isSyncEnd = true;

                                        String curMacaddress = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.MAC);

                                        String isFirstSync = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.ISFIRSTSYNCDATA, SharedPreUtil.SYNCED);
                                        if (StringUtils.isEmpty(isFirstSync) || isFirstSync.substring(0, 1).equals("0")) {      // TODO--- I have not taken 7 days of data.
                                            SharedPreUtil.savePre(BTNotificationApplication.getInstance(), SharedPreUtil.ISFIRSTSYNCDATA, SharedPreUtil.SYNCED, "1#" + curMacaddress);  // todo The synchronization is complete --- Synchronize 7 days of data for the first time, set SYNCED after 7 days of data
                                        }
                                    } else { //todo --- The number of corresponding received data has not been reached
                                        mHandler.sendEmptyMessageDelayed(HEART_DATA_FAILOVER, 20000); //TODO --- Delayed 30 seconds to send, broadcast
                                    }
                                }
                            }

                            List<HearData> hearDataList = new ArrayList<>();
                            byte[] heart = new byte[7]; // ????? 7    byte[] heart = new byte[10]
                            int l2Length = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            int runCount = l2Length / 7;
                            SharedPreUtil.savePre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.DEFAULT_HEART_RATE, "1");  // Display the identity of the heart rate page

                            Query query = null;
                            List<HearData> list = null;
                            if(bytes.length >= 8) {
                                String year = String.format(Locale.ENGLISH,"20" + "%02d", bytes[5]);
                                String mouth = String.format(Locale.ENGLISH,"%02d", bytes[6]);
                                String day = String.format(Locale.ENGLISH,"%02d", bytes[7]);
                                String beginDate = year + "-" + mouth + "-" + day;
                                query = db.getHearDao().queryBuilder()
                                        .where(HearDataDao.Properties.Mac.eq(SharedPreUtil.readPre(getApplicationContext(), SharedPreUtil.USER, SharedPreUtil.MAC)))
                                        .where(HearDataDao.Properties.Date.eq(beginDate)).build();
                            }
                            if(null != query) {
                                list = query.list();
                            }
                            Log.e(TAG, "heart l2Length =" + l2Length);
                            for (int i = 0; i < runCount; i++) {
                                HearData hearData = new HearData();
                                System.arraycopy(bytes, (i * 7) + 5, heart, 0, 7);
                                String year = String.format(Locale.ENGLISH,"20" + "%02d", heart[0]);
                                Log.e(TAG, "heart year =" + year);
                                String mouth = String.format(Locale.ENGLISH,"%02d", heart[1]);
                                Log.e(TAG, "heart mouth =" + mouth);
                                String day = String.format(Locale.ENGLISH,"%02d", heart[2]);
                                Log.e(TAG, "heart day =" + day);
                                String hour = String.format(Locale.ENGLISH,"%02d", heart[3]);
                                Log.e(TAG, "heart hour =" + hour);
                                String minute = String.format(Locale.ENGLISH,"%02d", heart[4]);
                                Log.e(TAG, "heart minute =" + minute);
                                String second = String.format(Locale.ENGLISH,"%02d", heart[5]);
                                Log.e(TAG, "heart second =" + second);
                                int hearts = heart[6] & 0xff;
                                Log.e(TAG, "heart hearts =" + hearts);
                                if(hearts <= 0){
                                    continue;
                                }
                                String beginTime = year + "-" + mouth + "-" + day + " " + hour + ":" + minute + ":" + second;
                                Date date = null;
                                if(StringUtils.isEmpty(beginTime)){
                                    continue;
                                }
                                boolean isFlag = false;  //Determine if there is the same data
                                try {
                                    date = new SimpleDateFormat(Utils.YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH).parse(beginTime);
                                    if (null == date) {
                                        break;
                                    }
                                    if(heartAllList.size() > 0) {
                                        for (int j = 0; j < heartAllList.size(); j++) {
                                            if((date.getTime()/1000 + "").equals(heartAllList.get(j).getBinTime())){
                                                isFlag = true;
                                                break;
                                            }
                                        }
                                    }else if(null != list && list.size() > 0){
                                        for (int j = 0; j < list.size(); j++) {
                                            if((date.getTime()/1000 + "").equals(list.get(j).getTimes())){
                                                isFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if(!isFlag) {
                                    if(null == date){
                                        try {
                                            date = new SimpleDateFormat(Utils.YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH).parse(beginTime);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (null == date) {
                                        break;
                                    }
                                    hearData.setBinTime(date.getTime() / 1000 + "");    // 1502680129
                                    hearData.setHeartbeat(hearts + "");      // 107
                                    hearData.setHigt_hata(hearts + "");
                                    hearData.setLow_hata(hearts + "");
                                    hearData.setAvg_hata(hearts + "");// Average heart rate
                                    hearDataList.add(hearData);
                                }
                            }
                            Log.e("UPDTA", "3");
                            heartAllList.addAll(hearDataList);
                            heartdataWrite(hearDataList,false);
                        } else if (bytes[2] == BleConstants.BRACELREALRUN) {                      //TODO Hand ring real-time step data return (0xAC)
                            int run = Utils.getInt(bytes, 5);
                            Log.e(TAG, "bracel run =" + run);
                            float calorie = Utils.getFloat(bytes, 9);  // 16.079199
                            Log.e(TAG, "bracel calorie =" + calorie);
                            float distance = Utils.getFloat(bytes, 13);  // 0.27472
                            Log.e(TAG, "bracel distance =" + distance);
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);//设置日期格式
                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.RUN, run + "");
                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.CALORIE, calorie + "");
                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.DISTANCE, distance + "");

//                        String dd =  String.format(Locale.ENGLISH, df.format(new Date()) + "");
                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.WATCHTIME,  String.format(Locale.ENGLISH,  df.format(new Date()) + ""));
//                        String distance = String.format(Locale.ENGLISH, "%.2f", (allStep * 0.7) / 1000.0);   String.format(Locale.ENGLISH, "%1$02d-%2$02d-%3$02d", mai, sec, yunshu) df.format(new Date()) + "");
//                        SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.WATCHTIME, df.format(new Date()) + "");
                            //todo ---- Add judgment When the number of run steps is 0, the description is 0 to clear the data or the firmware is upgraded --- The number of steps in the synchronization history should also be cleared.
                            if(run == 0){
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.RUN, "0");
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.CALORIE,  "0");
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.DISTANCE, "0");
//                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.WATCHTIME, df.format(new Date()) + "");  //存手环实时时间

                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.SYNRUN, "0");
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.SYNCALORIE,  "0");
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.SYNDISTANCE, "0");
                                SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.SYNDATASIZE,  "0");
//                            SharedPreUtil.savePre(sContext, SharedPreUtil.BLEWATCHDATA, SharedPreUtil.WATCHSYNCTIME, df.format(new Date()) + "");  //存手环同步时间
                            }

                            if(BTNotificationApplication.isSyncEnd) {  //todo --- Synchronous data is completed before sending a synchronous real-time step broadcast
                                Intent intent = new Intent();
                                intent.setAction(MainService.ACTION_SYNFINSH_SUCCESS);    //todo ----BLE Broadcast of real-time steps, successful broadcast of data synchronization       ACTION_SYNFINSH
                                sendBroadcast(intent);
                            }

//                        Intent intent = new Intent();
//                        intent.setAction(MainService.ACTION_SYNFINSH_SUCCESS);    //todo ----BLE Broadcast of real-time steps, successful broadcast of data synchronization      ACTION_SYNFINSH
//                        sendBroadcast(intent);
                        } else if (bytes[2] == BleConstants.BRACELREALHEART) {
                            //TODO Bracelet real-time heart rate data return (0xAB)

                            int heart = bytes[5] & 0xff;
                            Log.e(TAG,"Real-time heart rate ： " + heart);
                            if(heart > 0){
                                SendHate(heart);
                            }
                            //判断当前手环类型 cf006
                        /*if(SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.MACNAME).contains("006")){
                            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                            String str = new SimpleDateFormat("HH:mm").format(curDate);
                            if(issavexinlv==false){
                                issavexinlv=true;
                                if(null==xinlvgmatime){xinlvgmatime= str;SendHate(heart);}
                            }else{
                                if(null!=xinlvgmatime&&!xinlvgmatime.equals(str)){xinlvgmatime= str;SendHate(heart);}
                            }
                        }else{
                            SendHate(heart);
                        }*/


                        } else if (bytes[2] == BleConstants.BRACELREALSPORT) {                         //TODO Bracelet motion mode data return (0xA5)
                            int l2ValueLength = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            if (l2ValueLength == 0) {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(MainService.ACTION_SYNFINSH_SPORTS); // todo  --- When there is no motion mode data, send a broadcast, destroy the loaded sync box
                                getApplicationContext().sendBroadcast(broadcastIntent);
                                return;
                            }
                            if(l2ValueLength % 16 == 0) {
                                if(!isGPS) {
                                    byte[] sports = new byte[16];   //Motion mode array
                                    int sportCount = (l2ValueLength) / 16;   //Number of sports mode data sets
                                    List<GpsPointDetailData> listGps = new ArrayList<>();
                                    for (int i = 0; i < sportCount; i++) {
                                        GpsPointDetailData gpsPointDetailData = new GpsPointDetailData();
                                        System.arraycopy(bytes, (i * 16) + 5, sports, 0, 16);
                                        String year = String.format(Locale.ENGLISH,"20" + "%02d", sports[0]);
                                        Log.e(TAG, "sport year = " + year);
                                        String month = String.format(Locale.ENGLISH,"%02d", sports[1]);
                                        Log.e(TAG, "sport month = " + month);
                                        String day = String.format(Locale.ENGLISH,"%02d", sports[2]);
                                        Log.e(TAG, "sport day = " + day);
                                        String startHour = String.format(Locale.ENGLISH,"%02d", sports[3]);
                                        Log.e(TAG, "sport startHour = " + startHour);
                                        String startMin = String.format(Locale.ENGLISH,"%02d", sports[4]);
                                        Log.e(TAG, "sport startMin = " + startMin);
                                        String endHour = String.format(Locale.ENGLISH,"%02d", sports[5]);
                                        Log.e(TAG, "sport endHour = " + endHour);
                                        String endMin = String.format(Locale.ENGLISH,"%02d", sports[6]);
                                        Log.e(TAG, "sport endMin = " + endMin);
                                        int sportType = sports[7];
                                        Log.e(TAG, "sport sportType = " + sportType);
                                        int step = Utils.getInt(sports, 8);
                                        Log.e(TAG, "sport step = " + step);
                                        float calories = Utils.byte2float(sports, 12);
                                        float calorie = Math.round(calories * 100) / 100;
                                        Log.e(TAG, "sport calorie = " + calorie);
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Utils.YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH);
                                        SimpleDateFormat sportSimpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

                                        String startTime = "";
                                        String endTime = "";
                                        int sHourI = Integer.valueOf(startHour);
                                        int eHourI = Integer.valueOf(endHour);

                                        if (eHourI < sHourI) {
                                            startTime = year + "-" + month + "-" + day + " " + startHour + ":" + startMin + ":00";  // 2017-07-28 21:04   --- 2017-07-28 21:04:00   -----   2017-11-08 20:04:00
//                                endTime = year + "-" + month + "-" + day + " " + endHour + ":" + endMin + ":00";
                                            try {
                                                Date startDate = simpleDateFormat.parse(startTime);
                                                Calendar calendar = Calendar.getInstance();   // Current day of the first day  2017-06-28
                                                calendar.setTime(startDate);
//                                    String  mcurDate = getDateFormat.format(calendar.getTime());  //  TODO---- 当前天的日期   --- 2017-11-08
                                                calendar.add(Calendar.DAY_OF_MONTH, 1);  //Set to 1 day after
                                                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                                String lastOneDay = sdf2.format(calendar.getTime());//todo 后一天的日期  --- 2017-11-09       2017-09-29    ----- Save this date locally. When the current date of the current homepage is 2017-09-29, set the 7-day sync flag to 0.
                                                Log.e(TAG, "The date after the current date is 1 day ---- " + lastOneDay);

                                                endTime = lastOneDay + " " + endHour + ":" + endMin + ":00";    // 2017-11-09 09:26:00
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            startTime = year + "-" + month + "-" + day + " " + startHour + ":" + startMin + ":00";  // 2017-07-28 21:04   --- 2017-07-28 21:04:00
                                            endTime = year + "-" + month + "-" + day + " " + endHour + ":" + endMin + ":00";
                                        }
//                            String startTime = year + "-" + month + "-" + day + " " + startHour + ":" + startMin + ":00";  // 2017-07-28 21:04   --- 2017-07-28 21:04:00
//                            String endTime = year + "-" + month + "-" + day + " " + endHour + ":" + endMin + ":00";      // 2017-07-28 08:46:00
                                        try {
                                            Date startDate = simpleDateFormat.parse(startTime);
                                            Date endDate = simpleDateFormat.parse(endTime);
                                            long sportTime = 0;
                                        /*if (endDate.getTime() > startDate.getTime()) { //TODO --- Sports mode data across days, date is the date of the previous day
                                            sportTime = endDate.getTime() - startDate.getTime();  //todo --- Seconds
                                        } else {
                                            sportTime = startDate.getTime() - endDate.getTime();  //todo --- Seconds
                                        }*/
                                            sportTime = Math.abs(endDate.getTime() - startDate.getTime());


                                            long sportMiaos = sportTime / 1000;

                                            int day1 = (int) (sportTime / (24 * 60 * 60 * 1000));
//                                int hour = (int) (sportTime / (60 * 60 * 1000) - day1 * 24);
//                                int min = (int) ((sportTime / (60 * 1000)) - day1 * 24 * 60 - hour * 60);
//                                int s = (int) (sportTime / 1000 - day1 * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

                                            int hour = (int) (sportTime / (60 * 60 * 1000));   // 13
                                            int min = (int) ((sportTime / (60 * 1000)) - hour * 60);  // 22    ---- 802分钟
                                            int s = (int) (sportTime / 1000 - hour * 60 * 60 - min * 60);

                                            Date sportDate = new Date();
                                            sportDate.setTime(sportTime);
                                            gpsPointDetailData.setMac(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC)); //mac地址
                                            gpsPointDetailData.setMid(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MID)); //用户mid
                                            gpsPointDetailData.setCalorie(calorie + "");   //卡路里
                                            gpsPointDetailData.setSpeed("0");
                                            gpsPointDetailData.setAve_step_width("0");
                                            gpsPointDetailData.setAltitude("0");
                                            gpsPointDetailData.setArraltitude("0");
                                            gpsPointDetailData.setArrLat("0");
                                            gpsPointDetailData.setArrheartRate("0");
                                            gpsPointDetailData.setArrLng("0");
                                            gpsPointDetailData.setArrspeed("0");
                                            gpsPointDetailData.setArrTotalSpeed("0");
                                            String hour1 = hour == 0 ? "00" : String.valueOf(hour);
                                            String min1 = min == 0 ? "00" : String.valueOf(min);
                                            String ss = s == 0 ? "00" : String.valueOf(s);
                                            String s1 = hour1 + ":" + min1 + ":" + ss;
                                            gpsPointDetailData.setSportTime(s1);

//                                long ddd = startDate.getTime();   // 1498721520000  --- 1498721940000 ---
                                            gpsPointDetailData.setTimeMillis(startDate.getTime() / 1000 + "");     // Motion data start time --- This field must be set, otherwise, the motion mode data list is garbled.
                                            gpsPointDetailData.setSportType(sportType + "");

                                            String code = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.FIRMEWAREINFO, SharedPreUtil.FIRMEWARECODE); //
                                            if("473".equals(code) || "193".equals(code) || "199".equals(code) || "496".equals(code)) {// todo  --- AB227-X2+ Running mode distance algorithm changed to 90CM, serial number 193, 473
//                                            gpsPointDetailData.setMile(step * 0.9);
                                                int userHeightI = 170;
                                                String userHeight = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.HEIGHT, "170");
                                                if (StringUtils.isEmpty(userHeight) || userHeight.equals("0")) {
                                                    userHeightI = 170;
                                                } else {
                                                    userHeightI = Integer.valueOf(userHeight);
                                                }

                                                int sex = 1;
                                                if (SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.SEX).equals("")) {
                                                    sex = 1;//默认为男
                                                } else {
                                                    int mspSex = Integer.parseInt(SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.SEX));
                                                    if(mspSex == 0){
                                                        sex = 1;
                                                    }else {
                                                        sex = 0;
                                                    }
                                                }

                                                if(sportType == 1){  // todo --- Walking
                                                    if(sex == 1){ // 男
                                                        gpsPointDetailData.setMile(step * 0.320*userHeightI/100);  // 0.415
                                                    }else { // 女
                                                        gpsPointDetailData.setMile(step * 0.313*userHeightI/100);  // 0.413
                                                    }
                                                }else if(sportType == 2){  // todo --- Run
                                                    if(sex == 1){ // 男
                                                        gpsPointDetailData.setMile(step * 0.415*userHeightI/100);  // 0.516
                                                    }else { // 女
                                                        gpsPointDetailData.setMile(step * 0.413*userHeightI/100);    // 0.5
                                                    }
                                                }else if(sportType == 4){   // todo --- Mountain climbing
                                                    if(sex == 1){ // 男
                                                        gpsPointDetailData.setMile(step * 0.320*userHeightI/100); // 0.415
                                                    }else { // 女
                                                        gpsPointDetailData.setMile(step * 0.313*userHeightI/100); // 0.413
                                                    }
                                                }
//                                            gpsPointDetailData.setMile(step * 0.516*170/100);
                                            }else{
                                                gpsPointDetailData.setMile(step * 0.7);
                                            }

                                            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                       /* int userHeightI = 170;
                                        String userHeight = SharedPreUtil.readPre(BTNotificationApplication.getInstance(), SharedPreUtil.USER, SharedPreUtil.HEIGHT, "170");
                                        if (StringUtils.isEmpty(userHeight) || userHeight.equals("0")) {
                                            userHeightI = 170;
                                        } else {
                                            userHeightI = Integer.valueOf(userHeight);
                                        }

                                        String distance = String.format(Locale.ENGLISH, "%.3f", (realStep * (0.415 * (float) userHeightI) / 100000));  //TODO Calculation formula given by BLE bracelet*/
                                            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//                                        gpsPointDetailData.setMile(step * 0.7);  // todo  --- AB227-X2+ 跑步模式距离算法改为90CM,序列号193,473
                                            gpsPointDetailData.setArrcadence("0");
                                            gpsPointDetailData.setDate(startTime.substring(0, 16));  //2017-06-29 15:39:00
                                            gpsPointDetailData.setDeviceType("2");
                                            gpsPointDetailData.setHeartRate("0");
                                            gpsPointDetailData.setMin_step_width("0");
                                            gpsPointDetailData.setPauseNumber("0");
                                            gpsPointDetailData.setPauseTime("0");
                                            gpsPointDetailData.setsTime(sportMiaos + "");
                                            gpsPointDetailData.setMax_step_width("0");
                                            gpsPointDetailData.setmCurrentSpeed("0");
                                            gpsPointDetailData.setStep("0");
                                            listGps.add(gpsPointDetailData);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    saveSpoetData(listGps);
                                }else{
                                    byte[] b = new byte[4];
                                    int sportLength = l2ValueLength / 16;
                                    if(sportLength > 0 ) {
                                        for (int j = 0; j < sportLength; j++) {
                                            String gpsYear = String.format(Locale.ENGLISH,"20" + "%02d", bytes[j * 16 + 5]);
                                            String gpsMonth = String.format(Locale.ENGLISH,"%02d", bytes[j * 16 + 6]);
                                            String gpsDay = String.format(Locale.ENGLISH,"%02d", bytes[j * 16 + 7]);
                                            String gpsHour = String.format(Locale.ENGLISH,"%02d", bytes[j * 16 + 8]);
                                            String gpsMinute = String.format(Locale.ENGLISH,"%02d", bytes[j * 16 + 9]);
                                            String gpsSecond = String.format(Locale.ENGLISH,"%02d", bytes[j * 16 + 10]);
                                            System.arraycopy(bytes, j * 16 + 13, b, 0, 4);
                                            double lat = (double) NumberBytes.byteArrayToInt(b) / 1000000;
                                            latSb.append(lat + "&");
                                            gpsLatList.add(lat);
                                            System.arraycopy(bytes, j * 16 + 17, b, 0, 4);
                                            double lng = (double) NumberBytes.byteArrayToInt(b) / 1000000;
                                            lngSb.append(lng + "&");
                                            gpsLngList.add(lat);
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                            try {
                                                gpsTimeList.add(simpleDateFormat.parse(gpsYear + "-" + gpsMonth + "-" + gpsDay + " " + gpsHour + ":" + gpsMinute + ":" + gpsSecond).getTime() / 1000);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            gpsIndex++;
                                            if (gpsIndex == gpsNumber) {
                                                if(gpsPointDetailData != null){
                                                    gpsPointDetailData.setArrLng(lngSb.toString());
                                                    gpsPointDetailData.setArrLat(latSb.toString());

                                                    float distance2 = 0;

                                                    for (int i = 0; i < gpsTimeList.size(); i++) {
                                                        if(i >= gpsTimeList.size() -1){
                                                            break;
                                                        }
                                                        gpsTime += gpsTimeList.get(i + 1) - gpsTimeList.get(i);
                                                        double radLat1 = gpsLatList.get(i)*GPS_PI/180.0;  // 纬度
                                                        double radLat2 = gpsLatList.get(i+1)*GPS_PI/180.0;  // 纬度
                                                        double radLng1=  gpsLngList.get(i)*GPS_PI /180.0;    // 经度
                                                        double radLng2=  gpsLngList.get(i+1)*GPS_PI /180.0;    // 经度
                                                        double a = Math.abs(radLat1 - radLat2);
                                                        double c = Math.abs(radLng1 - radLng2);
                                                        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(c / 2), 2)));
                                                        s=s*6378137.0;
                                                        distance2 += s;
                                                        if(distance2 > 1000){

                                                            if(Locale.getDefault().getLanguage().equalsIgnoreCase("ar")){ //todo ---  阿拉伯语
                                                                speedSb.append(String.format(Locale.ENGLISH,"%1$02s'%2$02''",gpsTime/60,gpsTime%60) + "&");
                                                            }else {
                                                                speedSb.append(String.format(Locale.ENGLISH,"%1$02d'%2$02d''",gpsTime/60,gpsTime%60) + "&");
                                                            }
//                                                        speedSb.append(String.format("%1$02d'%2$02d''",gpsTime/60,gpsTime%60) + "&");
                                                            gpsTime = 0;
                                                            distance2 = distance2 % 1000;
                                                        }else{
                                                            if(i == gpsTimeList.size() - 2){
                                                                speedSb.append(String.format(Locale.ENGLISH,"%1$02d'%2$02d''",gpsTime/60,gpsTime%60) + "&");
                                                            }
                                                        }

                                                    }
                                                    if(!TextUtils.isEmpty(speedSb.toString())) {
                                                        gpsPointDetailData.setArrTotalSpeed(speedSb.toString());
                                                    }else{
                                                        gpsPointDetailData.setArrTotalSpeed("0");
                                                    }
                                                    gpsList.add(gpsPointDetailData);
                                                    if(!isReceiveSport) {
                                                        saveSpoetData(gpsList);
                                                    }
                                                    gpsList.clear();
                                                    latSb.delete(0,latSb.length());
                                                    lngSb.delete(0,lngSb.length());
                                                    speedSb.delete(0,speedSb.length());
                                                    gpsLatList.clear();
                                                    gpsLngList.clear();
                                                    gpsTimeList.clear();
                                                    gpsNumber = 0;
                                                    gpsIndex = 0;
                                                    isGPS = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }else {
                                int type = bytes[5] & 0xff;
                                int year = Integer.parseInt(String.format(Locale.ENGLISH,"20" + "%02d", bytes[6]));
                                String month = String.format(Locale.ENGLISH,"%02d", bytes[7]);
                                String day = String.format(Locale.ENGLISH,"%02d", bytes[8]);
                                String hour = String.format(Locale.ENGLISH,"%02d", bytes[9]);
                                String minute = String.format(Locale.ENGLISH,"%02d", bytes[10]);
                                String second = String.format(Locale.ENGLISH,"%02d", bytes[11]);
                                int sportTime = Utils.getInt(bytes, 12);
                                int sportDistance = Utils.getInt(bytes, 16);
                                int sportCalorie = Utils.getInt(bytes, 20);
                                int sportStep = Utils.getInt(bytes, 24);
                                int maxHeart = bytes[28] & 0xff;
                                int avgHeart = bytes[29] & 0xff;
                                int minHeart = bytes[30] & 0xff;
                                int maxFrequency = ((bytes[31] << 8) & 0xff00 | bytes[32] & 0xff);
                                int avgFrequency = ((bytes[33] << 8) & 0xff00 | bytes[34] & 0xff);
                                int minFrequency = ((bytes[35] << 8) & 0xff00 | bytes[36] & 0xff);
                                int maxPace = ((bytes[37] << 8) & 0xff00 | bytes[38] & 0xff);
                                int avgPace = ((bytes[39] << 8) & 0xff00 | bytes[40] & 0xff);
                                int minPace = ((bytes[41] << 8) & 0xff00 | bytes[42] & 0xff);
                                gpsNumber = Utils.getInt(bytes, 43);
                                gpsPointDetailData = new GpsPointDetailData();
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                gpsPointDetailData.setMac(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC));
                                gpsPointDetailData.setMid(SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MID));
                                gpsPointDetailData.setmCurrentSpeed(avgPace + "");
                                try {
                                    gpsPointDetailData.setTimeMillis(format.parse(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second).getTime() / 1000 + "");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                gpsPointDetailData.setAltitude("0");
                                gpsPointDetailData.setArraltitude("0");
                                gpsPointDetailData.setArrcadence(minFrequency + "&" + avgFrequency + "&" + maxFrequency);
                                gpsPointDetailData.setArrheartRate(minHeart + "&" + avgHeart + "&" + maxHeart);
                                gpsPointDetailData.setArrTotalSpeed(String.format(Locale.ENGLISH,"%1$02d'%2$02d''",minPace/60,minPace%60) + "&"
                                        + String.format(Locale.ENGLISH,"%1$02d'%2$02d''",avgPace/60,avgPace%60) + "&" + String.format(Locale.ENGLISH,"%1$02d'%2$02d''",maxPace/60,maxPace%60));
                                gpsPointDetailData.setAve_step_width("0");
                                gpsPointDetailData.setMax_step_width("0");
                                gpsPointDetailData.setMin_step_width("0");
                                gpsPointDetailData.setDeviceType("2");
                                gpsPointDetailData.setPauseTime("0");
                                gpsPointDetailData.setPauseNumber("0");
                                gpsPointDetailData.setSportType(type + "");
                                gpsPointDetailData.setDate(year + "-" + month + "-" + day + " " + hour + ":" + minute);
                                gpsPointDetailData.setMile(sportDistance);
                                gpsPointDetailData.setHeartRate(avgHeart + "");
                                gpsPointDetailData.setsTime(sportTime + "");
                                gpsPointDetailData.setSportTime(String.format(Locale.ENGLISH, "%1$02d:%2$02d:%3$02d", sportTime / 60 / 60, sportTime / 60 % 60, sportTime % 60));
                                gpsPointDetailData.setCalorie((double)sportCalorie/1000 + "");
                                gpsPointDetailData.setSpeed((sportDistance/(double)sportTime) + "");
                                gpsPointDetailData.setArrspeed("0");
                                gpsPointDetailData.setStep(sportStep + "");
                                if(gpsNumber <= 0){
                                    gpsPointDetailData.setArrLat("0");
                                    gpsPointDetailData.setArrLng("0");
                                    gpsList.add(gpsPointDetailData);
                                    if(!isReceiveSport){
                                        saveSpoetData(gpsList);
                                    }
                                    gpsIndex = 0;
                                    gpsNumber = 0;
                                    isGPS = false;
                                    gpsList.clear();
                                }else{
                                    isGPS = true;
                                }
                            }
//                        Utils.saveSpoetData(listGps, null, sContext);
                        } else if (bytes[2] == BleConstants.BLOOD_OXYGEN_HIS) {                         //TODO -- Blood oxygen data return    0xAE

                        } else if (bytes[2] == BleConstants.BLOOD_PRESSURE_HIS) {                 //TODO -- Historical blood pressure data returned    0xAD
                            if (SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.WATCH).equals("1")) {    //(Smart watch)
                                Log.e(TAG,"needSendDataType = " + needSendDataType + " ; needReceDataNumber = " + needReceDataNumber);
                                if (needReceDataNumber == 1) {
                                    needReceDataNumber = 2;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                    intent.putExtra("step", "2");
                                    sContext.sendBroadcast(intent);
                                } else if (needReceDataNumber == 2) {
                                    needReceDataNumber = 3;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                    intent.putExtra("step", "3");
                                    sContext.sendBroadcast(intent);
                                } else if(needReceDataNumber == 3){
                                    needReceDataNumber = 4;
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_SYNFINSH);    // Successfully broadcast data synchronization
                                    intent.putExtra("step", "4");
                                    sContext.sendBroadcast(intent);
                                }
                                if (BTNotificationApplication.needSendDataType == needReceDataNumber) {
                                    receiveCountDownTimer.cancel();
                                    receiveCountDownTimer.start();
                                }
                            }

                            int l2ValueLength = (((bytes[3] << 8) & 0xff00) | (bytes[4] & 0xff));
                            if (l2ValueLength == 0) {
                                return;
                            }
                            int pressureCount = l2ValueLength / 8;
                            if(pressureCount > 0) {
                                String myaddress = SharedPreUtil.readPre(sContext, SharedPreUtil.USER, SharedPreUtil.MAC);
                                if(!TextUtils.isEmpty(myaddress)) {
                                    for (int j = 0; j < pressureCount; j++) {
                                        Bloodpressure bloodpressure = new Bloodpressure();
                                        count++;
                                        bloodpressure.setData(String.format(Locale.ENGLISH,"20" + "%02d",bytes[(j * 8) + 5])
                                                + "-" + String.format(Locale.ENGLISH,"%02d",bytes[(j * 8) + 6]) + "-" + String.format(Locale.ENGLISH,"%02d",bytes[(j * 8) + 7]));
                                        bloodpressure.setHour(String.format(Locale.ENGLISH,"%02d",bytes[(j * 8) + 8]) + ":" + String.format(Locale.ENGLISH,"%02d",bytes[(j * 8) + 9])
                                                + ":" + String.format(Locale.ENGLISH,"%02d",bytes[(j * 8) + 10]));
                                        bloodpressure.setConunt(count + "");
                                        bloodpressure.setMac(myaddress);
                                        bloodpressure.setHeightBlood((bytes[(j * 8) + 11] & 0xff) + "");
                                        bloodpressure.setMinBlood((bytes[(j * 8) + 12] & 0xff) + "");
                                        Log.e(TAG,"Blood date = " + bloodpressure.getData() + " " + bloodpressure.getHour());
                                        Log.e(TAG,"MinBlood = " + bloodpressure.getMinBlood() + " ;  MaxBlood = " + bloodpressure.getHeightBlood());
                                        if (BbloodpressureList != null) {
                                            BbloodpressureList.add(bloodpressure);
                                        }
                                    }
                                    if (BbloodpressureList.size() > 0 && BbloodpressureList != null) {
                                        saveBloodpressure(BbloodpressureList);
                                        BbloodpressureList.clear();
                                    }
                                }
                            }


                        } else if (bytes[2] == BleConstants.BLOOD_PRESSURE) {                 //TODO --  Real-time blood pressure data return    0xAD
                            int bp_max = bytes[5] & 0xFF;
                            int bp_min = bytes[6] & 0xFF;
                            //Determine the current bracelet type cf006

                            if(BTNotificationApplication.isSyncEnd) {  //todo --- 同Synchronous data is completed before sending a synchronous real-time step broadcast
                                SendXieya(bp_min,bp_max);   //todo   ---  BLE real-time blood pressure
                            }

                        } else if (bytes[2] == BleConstants.BLOOD_OXYGEN) {                 //TODO --  Real-time blood oxygen data return    0xAD
                            int oxygen = bytes[5] & 0xFF;             //血氧值
                            Log.e(TAG, "实时血氧 ： " + oxygen);  //

                            if(BTNotificationApplication.isSyncEnd) {  //todo --- Synchronous data is completed before sending a synchronous real-time step broadcast
                                SenDXieyang(oxygen);      //todo   ---  BLE real-time blood oxygenation
                            }
                        }
                    } else if (byte1 == BleConstants.CALIBRATION_COMMAND) {                       //TODO -- Calibration command    0x0B

                    } else if (byte1 == BleConstants.FACTORY_COMMAND) {                             //TODO -- Factory order   0x0C

                    }else if (byte1 == BleConstants.PUSH_DATA_TO_PHONE_COMMAND) {                        //TODO -- Find command   0x0D   Push data to mobile phone (Connected phone related)
                        //    0A00A300 64110909000000004C00000059000000A6000000B0000000E1000000F3000001360000028E00000C1000000EDA0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                        //    0A00A300 641109080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000063300000C2600000E3E00000F1D
                        switch (byte3) {   // -------  key                                      BA300 0080 04F000A0D00010003000101      0A00 AD00 08 1109080101000000
                            case BleConstants.GESTURE_PUSH_COMMAND: // 手势智控推送    ----    0D0001 00  03 00 0000    0D0001 00  0300 0101
                                Log.e("phone", "智控开关发命令了");  // TAG     05005000 0100
                                //todo  --- 当开关为开时，将抬手亮屏的开关打开 ----发广播
                                int gesNum = bytes[6];
                                if(gesNum == 1){
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_GESTURE_ON);    // Successfully broadcast data synchronization
                                    sendBroadcast(intent);
                                }else{
                                    Intent intent = new Intent();
                                    intent.setAction(MainService.ACTION_GESTURE_OFF);    // Successfully broadcast data synchronization
                                    sendBroadcast(intent);
                                }

                                break;

//                        case BleConstants.REJECT_DIAL_COMMAND: // 拒接电话
//                            Log.e("phone", "拒接电话了");  // TAG
//                            break;
//
//                        case BleConstants.ANSWER_DIAL_COMMAND: // 接电话
//                            Log.e("phone", "接电话了");
//                            break;

                        }
                    }else if (byte1 == (byte) 0x10){              //TODO   ---Clock movement calibration command
                        switch (byte3){
                            case (byte)0x02:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.REFUSE_CALIBRATION));
                                break;
                            case (byte)0x03:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.REFUSE_CALIBRATION));
                                break;
                            case (byte)0x04:
                                EventBus.getDefault().post(new MessageEvent(CalibrationActivity.CONFIRM_CALIBRATION));
                                break;
                            case (byte)0x05:
                                if(bytes.length > 5){
                                    int code = bytes[5];
                                    if(code == 0){
                                        EventBus.getDefault().post(new MessageEvent(CalibrationActivity.SEND_CALIBRATION));
                                    }else{
                                        EventBus.getDefault().post(new MessageEvent(CalibrationActivity.CANCEL_CALIBRATION));
                                    }
                                }

                                break;
                        }
                    }else if (byte1 == BleConstants.COMMAND_WEATHER_INDEX) {//TODO -- Dial push    0x0E Multi-package
                        if (bytes[2] == BleConstants.DIAL_PUSH) {   // Dial push       0E 00 E2 00 06  00 00 00 00 00 00   ------     0E 00 E2 00 02 01 00
                            int packageNum = WatchPushActivityNew.fileByte.length/256;    // 0E00E20006 00 0100 0000 00    ------
                            int lastpackageNum = WatchPushActivityNew.fileByte.length%256;
                       /* int packageNum = WatchPushActivityNew.fileByte.length/256;    // 0E00E20006 00 0100 0000 00    ------
                        int lastpackageNum = WatchPushActivityNew.fileByte.length%256;
                        boolean isEnd = false;
//                        index = 0;
                        boolean isHasPianYi = false;
                        int ipianYi = 0;

//                        indexHpy = 0;
                        int packageNumHpy = 0;    // 0E00E20006 00 0100 0000 00    ------
                        int lastpackageNumHpy = 0;*/
                            if(bytes[6] == 0 && bytes.length>7){  // TODO -- No dial   --- According to the length of the returned Byte array plus judgment
                                byte[] value = new byte[262];
                                value[0] = (byte)0x01; // Fixed 1 byte
                                value[1] = (byte)00; // 4-byte offset address
                                value[2] = (byte)00;
                                value[3] = (byte)00;
                                value[4] = (byte)00;
                                value[5] = (byte)00; // Fixed 1 byte
                                // Object src : The original array int srcPos : the start of the metadata Object dest : the target array int destPos : the start of the target array int length : the length to be copied
                                System.arraycopy(WatchPushActivityNew.fileByte, 0, value, 6, 256);   // Start the firmware package bit 8 copy 4 bit to mFileImgHdr.uid
                                L2Send.sendPushDialPicData(value);
                            }else if(bytes[6] == 1&& bytes.length>7){  // TODO -- The dial has not been pushed     0E 00 E2 00 01 01
                                isHasPianYi = true;

                                byte[] valueLast = new byte[4]; //  todo  Push offset address    0E 00 E2 00 06  00 00   00 00 00 00
                                valueLast[0] = bytes[7];
                                valueLast[1] = bytes[8];
                                valueLast[2] = bytes[9];
                                valueLast[3] = bytes[10]; // 保存偏移地址
                                ipianYi = NumberBytes.byteArrayToInt(valueLast);   // todo --- Image data that has been sent

                                packageNumHpy = (WatchPushActivityNew.fileByte.length - ipianYi)/256;    // 0E00E20006 00 0100 0000 00    ------
                                lastpackageNumHpy = (WatchPushActivityNew.fileByte.length - ipianYi)%256;

                                byte[] value = new byte[262];
                                value[0] = (byte)0x01; // Fixed 1 byte
                                value[1] = bytes[7]; // 4-byte offset address
                                value[2] = bytes[8];
                                value[3] = bytes[9];        //     int dddd = NumberBytes.byteArrayToInt(bb);
                                value[4] = bytes[10];
                                value[5] = (byte)00; // Fixed 1 byte
                                // Object src : 原The original array int srcPos : the start of the metadata Object dest : the target array int destPos : the start of the target array int length : the length to be copied
//                            System.arraycopy(WatchPushActivityNew.fileByte, 0, value, 6, 256);   // 将 固件包 的 起始 第8位 copy 4位 到  mFileImgHdr.uid

                                if(WatchPushActivityNew.fileByte.length - ipianYi >= 256){
                                    System.arraycopy(WatchPushActivityNew.fileByte,ipianYi, value, 6, 256);
                                }else {
                                    System.arraycopy(WatchPushActivityNew.fileByte,ipianYi, value, 6, WatchPushActivityNew.fileByte.length - ipianYi);
                                }
//                            System.arraycopy(WatchPushActivityNew.fileByte,ipianYi, value, 6, 256);  // todo ---- Also need to consider whether it is enough 256 bytes
                                L2Send.sendPushDialPicData(value);
                            }else if(bytes[6] == 2 && bytes.length>7){  // TODO -- The dial has been pushed
                                Toast.makeText(BTNotificationApplication.getInstance(),getString(R.string.dialpush_ed),Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setAction(MainService.ACTION_PUSHPIC_FINISH);   //TODO  ---  Broadcast，
                                sendBroadcast(intent);
                            }

                            if(bytes[5] == 1 && bytes.length == 7){   // 0E 00 E2 00 02 0100  ---- Send upgrade package should be 6 bytes (2nd command)
//                            int indexHpy = 0;
                                if(isHasPianYi){ // There is offset, there is a dial that has not been pushed  ----    ipianYi Offset
                                    indexHpy++;
//                                int packageNumHpy = (WatchPushActivityNew.fileByte.length - ipianYi)/256;    // 0E00E20006 00 0100 0000 00    ------
//                                int lastpackageNumHpy = (WatchPushActivityNew.fileByte.length - ipianYi)%256;

                                    if(isEnd){
                                        indexHpy = 0;
                                        byte[] value = new byte[1];
                                        value[0] = (byte)0x02; // Fixed 1 byte
                                        L2Send.sendPushDialPicData(value);
                                        isHasPianYi = false;
                                        return;
                                    }

                                    if(indexHpy + 1 <= packageNumHpy){ //indexHpy +1 <= packageNumHpy  最后一个完整包       0E 00 E2 00 02 02 00  ----  0E 00 E2 00 02 02 01 （最后一条命令）
                                        isEnd = false;
                                        int pianyiAdd = indexHpy*256;
                                        byte[] value = new byte[262];
                                        value[0] = (byte)0x01; // 固定1字节

//                                    value[1] = (byte)(pianyiAdd & 0xff); // 4字节偏移地址
//                                    value[2] = (byte)(pianyiAdd >> 8);
//                                    value[3] = (byte)(pianyiAdd >> 16);
//                                    value[4] = (byte)(pianyiAdd >> 24);
                                        value[4] = (byte)(pianyiAdd & 0xff); // 4-byte offset address
                                        value[3] = (byte)(pianyiAdd >> 8);
                                        value[2] = (byte)(pianyiAdd >> 16);
                                        value[1] = (byte)(pianyiAdd >> 24);

                                        value[5] = (byte)00; // Fixed 1 byte
                                        // Object src : 原数组 int srcPos : 元数据的起始  Object dest : 目标数组  int destPos : 目标数组起始  int length  : 要copy的长度
                                        System.arraycopy(WatchPushActivityNew.fileByte, pianyiAdd, value, 6, 256);   // 将 固件包 的 起始 第8位 copy 4位 到  mFileImgHdr.uid
                                        L2Send.sendPushDialPicData(value);
                                    }else {
                                        int pianyiAdd = packageNumHpy*256;
                                        byte[] value = new byte[6+ lastpackageNumHpy];
                                        value[0] = (byte)0x01; // 固定1字节

//                                    value[1] = (byte)(pianyiAdd & 0xff); // 4字节偏移地址
//                                    value[2] = (byte)(pianyiAdd >> 8);
//                                    value[3] = (byte)(pianyiAdd >> 16);
//                                    value[4] = (byte)(pianyiAdd >> 24);
                                        value[4] = (byte)(pianyiAdd & 0xff); // 4-byte offset address
                                        value[3] = (byte)(pianyiAdd >> 8);
                                        value[2] = (byte)(pianyiAdd >> 16);
                                        value[1] = (byte)(pianyiAdd >> 24);

                                        value[5] = (byte)00; // Fixed 1 byte
                                        // Object src : 原数组 int srcPos : 元数据的起始  Object dest : 目标数组  int destPos : 目标数组起始  int length  : 要copy的长度
                                        System.arraycopy(WatchPushActivityNew.fileByte, pianyiAdd, value, 6, lastpackageNum);   // 将 固件包 的 起始 第8位 copy 4位 到  mFileImgHdr.uid
                                        L2Send.sendPushDialPicData(value);
                                        isEnd = true;
                                    }

//                                if(isEnd){
//                                    indexHpy = 0;
//                                    byte[] value = new byte[1];
//                                    value[0] = (byte)0x02; // 固定1字节
//                                    L2Send.sendPushDialPicData(value);
//                                }

                                }else{
                                    index++;

                                    if(isEnd){
                                        index = 0;
                                        byte[] value = new byte[1];
                                        value[0] = (byte)0x02; // Fixed 1 byte
                                        L2Send.sendPushDialPicData(value);
                                        return;
                                    }

                                    if(index +1 <= packageNum){ // The last complete package       0E 00 E2 00 02 02 00  ----  0E 00 E2 00 02 02 01 （Last command）
                                        isEnd = false;
                                        int pianyiAdd = index*256;
                                        byte[] value = new byte[262];
                                        value[0] = (byte)0x01; // 固定1字节

//                                    value[1] = (byte)(pianyiAdd & 0xff); // 4字节偏移地址
//                                    value[2] = (byte)(pianyiAdd >> 8);
//                                    value[3] = (byte)(pianyiAdd >> 16);
//                                    value[4] = (byte)(pianyiAdd >> 24);
                                        value[4] = (byte)(pianyiAdd & 0xff); // 4-byte offset address
                                        value[3] = (byte)(pianyiAdd >> 8);
                                        value[2] = (byte)(pianyiAdd >> 16);
                                        value[1] = (byte)(pianyiAdd >> 24);

                                        value[5] = (byte)00; // Fixed 1 byte
                                        // Object src : 原数组 int srcPos : 元数据的起始  Object dest : 目标数组  int destPos : 目标数组起始  int length  : 要copy的长度
                                        System.arraycopy(WatchPushActivityNew.fileByte, pianyiAdd, value, 6, 256);   // 将 固件包 的 起始 第8位 copy 4位 到  mFileImgHdr.uid
                                        L2Send.sendPushDialPicData(value);
                                    }else {
                                        int pianyiAdd = packageNum*256;
                                        byte[] value = new byte[6+ lastpackageNum];
                                        value[0] = (byte)0x01; // Fixed 1 byte

//                                    value[1] = (byte)(pianyiAdd & 0xff); // 4字节偏移地址
//                                    value[2] = (byte)(pianyiAdd >> 8);
//                                    value[3] = (byte)(pianyiAdd >> 16);
//                                    value[4] = (byte)(pianyiAdd >> 24);
                                        value[4] = (byte)(pianyiAdd & 0xff); // 4-byte offset address
                                        value[3] = (byte)(pianyiAdd >> 8);
                                        value[2] = (byte)(pianyiAdd >> 16);
                                        value[1] = (byte)(pianyiAdd >> 24);


                                        value[5] = (byte)00; // Fixed 1 byte
                                        // Object src : 原数组 int srcPos : 元数据的起始  Object dest : 目标数组  int destPos : 目标数组起始  int length  : 要copy的长度
                                        System.arraycopy(WatchPushActivityNew.fileByte, pianyiAdd, value, 6, lastpackageNum);   // 将 固件包 的 起始 第8位 copy 4位 到  mFileImgHdr.uid
                                        L2Send.sendPushDialPicData(value);
                                        isEnd = true;
                                    }

//                                if(isEnd){
//                                    index = 0;
//                                    byte[] value = new byte[1];
//                                    value[0] = (byte)0x02; // 固定1字节
//                                    L2Send.sendPushDialPicData(value);
//                                }
                                }
                            }else if(bytes[5] == 2  && bytes.length == 7){      //TODO -- 此处应该为7个字节  0E 00 E2 00 02 02  00
                                ipianYi = 0;
                                packageNumHpy = 0;    // 0E00E20006 00 0100 0000 00    ------
                                lastpackageNumHpy = 0;
                                isEnd = false;
                                if(bytes[6] == 1){
                                    Toast.makeText(BTNotificationApplication.getInstance(),getString(R.string.dialpush_success),Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(BTNotificationApplication.getInstance(),getString(R.string.dialpush_fail),Toast.LENGTH_SHORT).show();
                                }

                                Intent intent = new Intent();
                                intent.setAction(MainService.ACTION_PUSHPIC_FINISH);   //TODO  ---  发广播，
                                sendBroadcast(intent);
                            }
                        }
                    }
                }
            }
        }

        public LocationListener(String provider) {
            UtilityFunctions.showLogErrorStatic( "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            UtilityFunctions.showLogErrorStatic("onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            UtilityFunctions.showLogErrorStatic("onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            UtilityFunctions.showLogErrorStatic("onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            UtilityFunctions.showLogErrorStatic( "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    //Location work end
    private DatabaseHelper db;
    // creating bluetooth connection listerns here

    // Binding service starts here
    // Binder given to clients
   // private final IBinder mBinder = new LocalBinder();

    // Just For testing purpose you can remove this after testing
    // Random number generator
    private final Random mGenerator = new Random();


    /** method for clients */
    private IConnectListener iConnectListener = new IConnectListener() {
        @Override
        public void onConnectState(int i) {
          if(i==2){
               //showNotification("Connecting");
            }else if(i == 4)
            SavedData.setConnectStatus(false);    //showNotification("Disconnected");*/
        }

        @Override
        public void onConnectDevice(BluetoothDevice bluetoothDevice) {
            //showNotification("Connected");
            SavedData.setConnectStatus(true);
            KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_settime_pack());
            sendBluetoothCommand();
        }

        @Override
        public void onScanDevice(BluetoothLeDevice bluetoothLeDevice) {
            KCTBluetoothManager.getInstance().connect(bluetoothLeDevice.getDevice(),1);
            //stopScan();
        }

        @Override
        public void onCommand_d2a(byte[] bytes) {
            String arra = "";
            for(int i=0;i<bytes.length;i++){
                arra = arra + bytes[i]+ "  ";
            }
            utilityFunctions.showLogError("bytes length "+bytes.length + " data "+arra);
            KCTBluetoothCommand.getInstance().d2a_command_Parse(getApplicationContext(),bytes,iReceiveListener);

            if(bytes.length==16){
                //for blood pressure here
                if(bytes[15]==0 && bytes[13] >0){
                    String array = "["+String.valueOf( bytes[13])+","+String.valueOf(bytes[14])+"]";
                    //bytes[13]//Shrinkage bp
                    //bytes[14]//diastalic bp
                    utilityFunctions.showLogError(array);
                    parseDataHere(BLOOD_PRESSURE_OXYGEN,array.toString());
                }
            }else if(bytes.length==14 ){
                if(bytes[13]!=0 && bytes[0]==-70 && bytes[13] >0 && bytes[10] == -78){
                    String array = String.valueOf( bytes[13]);
                    //bytes[13]//Shrinkage bp
                    //bytes[14]//diastalic bp
                    utilityFunctions.showLogError(array.toString());
                    parseDataHere(OXYGEN,array.toString());
                }
            }else if(bytes.length==14 ){
                if(bytes[13]!=0 && bytes[0]==-70 && bytes[13] >0 && bytes[10] == -85){
                    String array = String.valueOf( bytes[13]);
                    //bytes[13]//Shrinkage bp
                    //bytes[14]//diastalic bp
                    utilityFunctions.showLogError(array.toString());
                    parseDataHere(HEART_RATE_REAL,array.toString());
                }
            }
        }
    };


    /*coverting parsed data here */

    private IReceiveListener iReceiveListener =  new IReceiveListener() {
        @Override
        public void onReceive(int item_type, boolean b, Object... objects) {
            String data = "ItemType "+item_type+ " boolean "+b+" \n";
            utilityFunctions.showLogError("onReceive int "+item_type+" boolean "+b+ " Objects "+objects[0].toString());

            for(int i=0;i<objects.length;i++){
                utilityFunctions.showLogError("Receive "+objects[i].toString());
            }
            parseDataHere(item_type,objects[0].toString());
        }
    };


    private void parseDataHere(int item_type, String s) {
        try {
            int stepcount = 0;
            float distancetot = 0.0f,caltot=0.0f;
            if(item_type == PEDOMETER_ALL) {
                JSONArray jsonArray = new JSONArray(s);
                String unique_key = "";

                for (int j = 0; j < jsonArray.length(); j++) {
                    unique_key = "";
                    JSONObject jsonObject = jsonArray.getJSONObject(j);

                    step = jsonObject.getString("step");
                    day = jsonObject.getString("day");
                    month = jsonObject.getString("month");
                    hour = jsonObject.getString("hour");
                    year = jsonObject.getString("year");
                    calorie = jsonObject.getString("calorie");
                    distance = jsonObject.getString("distance");
                    unique_key = item_type+"_"+year+"_"+month+"_"+day+"_"+hour;
                    stepcount = Integer.valueOf(step)>=stepcount?Integer.valueOf(step):stepcount;
                    distancetot = Float.valueOf(distance)>=distancetot?Float.valueOf(distance):distancetot;
                    caltot = Float.valueOf(calorie)>=caltot?Float.valueOf(calorie):caltot;
                    utilityFunctions.showLogError("distance  : "+distance);
                    // inserting a row
                    utilityFunctions.showLogError("isInserting "+jsonObject.toString());
                    long isInserted = db.insertNote(jsonObject.toString(),unique_key,item_type);
                    utilityFunctions.showLogError("isInserted "+isInserted);
                }
                SavedData.setDistance( distancetot);
                SavedData.setCalorie( caltot);
                SavedData.setStep(stepcount);
            }else if(item_type == HEART_RATE_ALL){
                JSONArray jsonArray = new JSONArray(s);
                String unique_key = "";

                for (int j = 0; j < jsonArray.length(); j++) {
                    unique_key = "";
                    JSONObject jsonObject = jsonArray.getJSONObject(j);


                    day = jsonObject.getString("day");
                    month = jsonObject.getString("month");
                    hour = jsonObject.getString("hour");
                    minute = jsonObject.getString("minute");
                    second= jsonObject.getString("second");
                    year = jsonObject.getString("year");
                    heart = jsonObject.getString("heart");
                    unique_key = item_type+"_"+year+"_"+month+"_"+day+"_"+hour+"_"+minute+"_"+second;

                    // inserting a row
                    utilityFunctions.showLogError("isInserting "+jsonObject.toString());
                    long isInserted = db.insertNote(jsonObject.toString(),unique_key,item_type);
                    utilityFunctions.showLogError("isInserted "+isInserted);
                }
            }else if(item_type == BLOOD_PRESSURE_OXYGEN){

                String unique_key = "";
                    unique_key = item_type+"_"+utilityFunctions.getCurrentTime();

                    // inserting a row
                    utilityFunctions.showLogError("isInserting "+s);
                    long isInserted = db.insertNote(s,unique_key,item_type);
                    utilityFunctions.showLogError("isInserted "+isInserted);

            }else if(item_type == OXYGEN){

                String unique_key = "";
                unique_key = item_type+"_"+utilityFunctions.getCurrentTime();

                // inserting a row
                utilityFunctions.showLogError("isInserting "+s);
                long isInserted = db.insertNote(s,unique_key,item_type);
                utilityFunctions.showLogError("isInserted "+isInserted);

            }else if(item_type == HEART_RATE_REAL){

                String unique_key = "";
                unique_key = item_type+"_"+utilityFunctions.getCurrentTime();

                // inserting a row
                utilityFunctions.showLogError("isInserting "+s);
                long isInserted = db.insertNote(s,unique_key,item_type);
                utilityFunctions.showLogError("isInserted "+isInserted);

            }


        } catch (JSONException e) {
            e.printStackTrace();
            utilityFunctions.showLogError("data error = "+e.getMessage());
        }finally {
            sendDataToServerHere();
        }
    }

    private void sendDataToServerHere() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.URL.SYNC_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                        utilityFunctions.showLogError( "Response :" + response);
                        try {
                            JSONObject jsonObject= new JSONObject(response);
                                String status = jsonObject.getJSONObject("responseBody").getString("status");
                                if(status .equalsIgnoreCase("success")){
                                    clearDataFromDb();
                                    //showNotification("Sync Success");
                                    sendMessageToActivity(ctx);
                                }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //showNotification("Sync Error");
                utilityFunctions.showLogError(error.toString());
                sendMessageToActivity(ctx);
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = getDataFromdb();
                return str.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("userid", SavedData.getuser_id());




                return params;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    private void clearDataFromDb() {
        db.deleteAll();
    }

    private String getDataFromdb() {
        List<WatchData> list = db.getAllNotes();
        //utilityFunctions.showToast(list.get(0).getResponse());
        JSONObject jsonObject ,jsonReqPacket;
        JSONArray jsonArray = new JSONArray();
        String type = "";
        jsonReqPacket=new JSONObject();
        try {
            jsonReqPacket.put("mobile", SavedData.getUserNumber());
            jsonReqPacket.put("device_id", SavedData.getDeviceId());
            jsonReqPacket.put("location", "{\"latitiude\":\""+mLastLocation.getLatitude()+"\",\"longitude\":\""+mLastLocation.getLongitude()+"\"}");

            for (int i = 0; i < list.size(); i++) {
                jsonObject = new JSONObject();

                if (list.get(i).getType() == PEDOMETER_ALL)
                    type = "PEDOMETER";
                else if(list.get(i).getType() == HEART_RATE_ALL)
                    type = "HEART_RATE";
                else if(list.get(i).getType() == BLOOD_PRESSURE_OXYGEN)
                    type = "BLOOD_PRESSURE_OXYGEN";
                else if(list.get(i).getType() == OXYGEN)
                    type = "OXYGEN";
                else if(list.get(i).getType() == HEART_RATE_REAL)
                    type = "HEART_RATE_REAL";

                jsonObject.put("type", type);
                jsonObject.put("response", list.get(i).getResponse());
                jsonObject.put("unique_id", list.get(i).getUnique_id());
                jsonObject.put("timestamp", list.get(i).getTime_stamp());
                jsonArray.put(jsonObject);

            }
            jsonReqPacket.put("data", jsonArray);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        //utilityFunctions.showToast(jsonArray.toString());
        utilityFunctions.showLogError("request packet Size ->  "+jsonArray.length());
        utilityFunctions.showLogDebug("request packet ->  "+jsonReqPacket.toString());

        return jsonReqPacket.toString();
    }
    private void startScan(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            SavedData.setConnectStatus(false);
            utilityFunctions.showLogError("Device do not support bluetooth device");
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                showNotification("Please enable bluetooth");
                SavedData.setConnectStatus(false);
                utilityFunctions.showLogError("Please enable your bluetooth device");
                return;
            }
        }

        // checking how many times service started here
        SavedData.setService_STARTED_COUNT();
        int connectState = KCTBluetoothManager.getInstance().getConnectState();
        utilityFunctions.showLogError("Connect State : "+connectState);
        if(connectState !=3) {
            SavedData.setConnectStatus(false);
            KCTBluetoothManager.getInstance().scanDevice(true);
            utilityFunctions.showLogError("Starting Scan");
        }else if(connectState == 3){
            utilityFunctions.showLogError("Device already connected ");
            SavedData.setConnectStatus(true);
            sendBluetoothCommand();
        }else{
            utilityFunctions.showLogError("Some Error Occuured");
            SavedData.setConnectStatus(false);
        }
    }

    private void sendBluetoothCommand() {
        // cheking other data here

        HashMap<String,Object> map = new HashMap<>();

        map.put("Enable",true);
        map.put("Starthour",01);
        map.put("Startmin",00);
        map.put("Endhour",23);
        map.put("Endmin",59);
        map.put("The interval",10);

       // KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.BLE_COMMAND_a2d_sendMTKCurrentAllRun_pack());
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_settime_pack());
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_setAutoHeartData_pack(map));
        UtilityFunctions.showLogErrorStatic("Command Sent");
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synData_pack(Constant.PEDOMETER,utilityFunctions.getCurrentTime()));
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synData_pack(Constant.HEART_RATE,utilityFunctions.getCurrentTime()));
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synData_pack(Constant.SLEEP,utilityFunctions.getCurrentTime()));

        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synData_pack(BLOOD_PRESSURE_OXYGEN,utilityFunctions.getCurrentTime()));

        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synRealData_pack(Constant.HEART_RATE));
        KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_synRealData_pack(BLOOD_PRESSURE_OXYGEN));
        //KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().BLE_COMMAND_a2d_getFirmwareData_pack());
       // KCTBluetoothManager.getInstance().sendCommand_a2d(BLEBluetoothManager.getInstance().Blesend);
    }
    private void stopScan(){
        KCTBluetoothManager.getInstance().scanDevice(false);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ExceptionHandler.register(this, "http://uvcabs.esy.es/crash_logs_watch/server.php?version=" + UtilityFunctions.getDeviceName()+"-"+Build.VERSION.SDK_INT);
        startForeground(12345678, getNotification("Service Started"));

        utilityFunctions =  new UtilityFunctions(getApplicationContext());
        KCTBluetoothManager.getInstance().registerListener(iConnectListener);
        db = new DatabaseHelper(getApplicationContext());
        queue = Volley.newRequestQueue(this);
        ctx = getApplicationContext();


        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            utilityFunctions.showLogError("fail to request location update, ignore"+ ex);
        } catch (IllegalArgumentException ex) {
            utilityFunctions.showLogError("network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            startScan();
            SavedData.setServiceStatus(true);
        }else if (intent.getAction().equals(Constant.ACTION.STARTFOREGROUND_ACTION)) {
            utilityFunctions.showLogError( "Received Start Foreground Intent ");
            utilityFunctions.showLogError("Service Started!");
            startScan();
            SavedData.setServiceStatus(true);

        } else if (intent.getAction().equals(
                Constant.ACTION.STOPFOREGROUND_ACTION)) {
            utilityFunctions.showLogError("Service STOPFOREGROUND_ACTION!");
            stopForeground(true);
            stopSelf();
            onDestroy();
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        utilityFunctions.showLogError( "On Destroy");
        KCTBluetoothManager.getInstance().unregisterListener(iConnectListener);
        KCTBluetoothManager.getInstance().disConnect_a2d();

        SavedData.setServiceStatus(false);
        SavedData.setConnectStatus(false);
        Intent broadcastIntent = new Intent("com.fpmd.wearhealth.Service");
        sendBroadcast(broadcastIntent);

        for (int i = 0; i < mLocationListeners.length; i++) {
            mLocationManager.removeUpdates(mLocationListeners[i]);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private Notification getNotification(String msg) {

        NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        return builder.build();
    }

    private void showNotification(String msg) {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.setAction(Constant.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);



        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);



        int notifyID = 1;
        String CHANNEL_ID = "channel_01";// The id of the channel.
        CharSequence name = "My Channel";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification notification=null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            utilityFunctions.showLogError("Inside Notification");
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notification = new Notification.Builder(ForegroundService.this)
                    .setContentTitle("WatchStatus")
                    .setContentText(msg)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId(CHANNEL_ID)
                    .build();
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);

// Issue the notification.
            if(!serviceIsRunningInForeground(ForegroundService.this)) {
                mNotificationManager.notify(notifyID, notification);
            }
        }else {
// Create a notification and set the notification channel.


            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Scanning")
                    .setTicker("Checking background service")
                    .setContentText(msg)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

        }
        startForeground(Constant.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }
    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                //utilityFunctions.showLogError("Class Name "+getClass().getName() + " service name "+service.service.getClassName());
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
    private  void sendMessageToActivity(Context ctx) {
        SavedData.setLastSyncTime(utilityFunctions.getCurrentTime());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.fpmd.wearhealth.Service");


        ctx.sendBroadcast(intent);


    }
    public class LocationServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }
}
