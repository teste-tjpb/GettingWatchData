package vikas.gettingwatchdata.app;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kct.bluetooth.KCTBluetoothManager;

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
