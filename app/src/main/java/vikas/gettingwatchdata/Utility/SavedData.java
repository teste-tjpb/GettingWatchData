package vikas.gettingwatchdata.Utility;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import vikas.gettingwatchdata.Constants.Constant;
import vikas.gettingwatchdata.app.Myapplication;

/**
 * Created by vikasaggarwal on 15/04/18.
 */

public class SavedData {


    static SharedPreferences prefs;

    public static int getService_STARTED_COUNT() {
        return getInstance().getInt(Constant.SHARED_PREFERENCE_ID.SERVICE_COUNT, 0);

    }

    public static void   setService_STARTED_COUNT() {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putInt(Constant.SHARED_PREFERENCE_ID.SERVICE_COUNT, (getService_STARTED_COUNT()+1));
        editor.apply();
    }

    public static String getUserNumber() {
        return getInstance().getString(Constant.SHARED_PREFERENCE_ID.USER_NUMBER,"");

    }

    public static void   setUserNumber(String mobile) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(Constant.SHARED_PREFERENCE_ID.USER_NUMBER,mobile );
        editor.apply();
    }

    public static String getDeviceId() {
        return getInstance().getString(Constant.SHARED_PREFERENCE_ID.DEVICE_ID,"");

    }

    public static void   setDeviceId(String id) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(Constant.SHARED_PREFERENCE_ID.DEVICE_ID,id );
        editor.apply();
    }
    public static SharedPreferences getInstance() {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(Myapplication.getInstance());
        }
        return prefs;
    }

    public static Boolean isServiceRunning() {
        return getInstance().getBoolean(Constant.SHARED_PREFERENCE_ID.SERVICE_RUNNING,false);

    }

    public static void   setServiceStatus(Boolean status) {
        UtilityFunctions.showLogErrorStatic("Service Setting Here "+status);
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putBoolean(Constant.SHARED_PREFERENCE_ID.SERVICE_RUNNING,status );
        editor.apply();
    }

    public static void   setDistance(float distance) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putFloat(Constant.SHARED_PREFERENCE_ID.DISTANCE,distance );
        editor.apply();
    }
    public static void   setCalorie(float cal) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putFloat(Constant.SHARED_PREFERENCE_ID.CALORIE,cal );
        editor.apply();
    }
    public static void   setStep(int step) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putInt(Constant.SHARED_PREFERENCE_ID.STEP,step );
        editor.apply();
    }
    public static void   setLastSyncTime(String time) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(Constant.SHARED_PREFERENCE_ID.SYNC_TIME,time );
        editor.apply();
    }
    public static void   setConnectStatus(Boolean status) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putBoolean(Constant.SHARED_PREFERENCE_ID.CONNECT_STATUS,status );
        editor.apply();
    }

    public static String getLastSyncTime() {
        return getInstance().getString(Constant.SHARED_PREFERENCE_ID.SYNC_TIME,"");

    }
    public static Float getDistance() {
        return getInstance().getFloat(Constant.SHARED_PREFERENCE_ID.DISTANCE,0);

    }
    public static Float getCalorie() {
        return getInstance().getFloat(Constant.SHARED_PREFERENCE_ID.CALORIE,0);

    }
    public static int getStep() {
        return getInstance().getInt(Constant.SHARED_PREFERENCE_ID.STEP,0);

    }
    public static boolean getConnectStatus() {
        return getInstance().getBoolean(Constant.SHARED_PREFERENCE_ID.CONNECT_STATUS,false);

    }



}
