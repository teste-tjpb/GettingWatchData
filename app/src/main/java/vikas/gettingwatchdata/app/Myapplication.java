package vikas.gettingwatchdata.app;

import android.app.Application;
import android.os.Environment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kct.bluetooth.KCTBluetoothManager;

import java.io.File;
import java.io.IOException;

import vikas.gettingwatchdata.Utility.UtilityFunctions;

/**
 * Created by vikasaggarwal on 08/04/18.
 */

public class Myapplication extends Application {
    private static Myapplication mInstance;
    private RequestQueue mRequestQueue;
    @Override
    public void onCreate() {
        super.onCreate();
        KCTBluetoothManager.getInstance().init(this,true);
        mInstance = this;


    }

    /* Checks if external storage is available for read and write */

    public boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();

        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {

            return true;

        }

        return false;

    }



    /* Checks if external storage is available to at least read */

    public boolean isExternalStorageReadable() {

        String state = Environment.getExternalStorageState();

        if ( Environment.MEDIA_MOUNTED.equals( state ) ||

                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {

            return true;

        }

        return false;

    }

    public static synchronized Myapplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
}
