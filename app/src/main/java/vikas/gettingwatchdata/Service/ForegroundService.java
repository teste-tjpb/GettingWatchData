package vikas.gettingwatchdata.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kct.bluetooth.KCTBluetoothManager;
import com.kct.bluetooth.bean.BluetoothLeDevice;
import com.kct.bluetooth.callback.IConnectListener;
import com.kct.command.BLEBluetoothManager;
import com.kct.command.IReceiveListener;
import com.kct.command.KCTBluetoothCommand;
import com.nullwire.trace.ExceptionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import vikas.gettingwatchdata.Constants.Constant;
import vikas.gettingwatchdata.HomeActivity;
import vikas.gettingwatchdata.R;
import vikas.gettingwatchdata.Utility.SavedData;
import vikas.gettingwatchdata.Utility.UtilityFunctions;
import vikas.gettingwatchdata.database.DatabaseHelper;
import vikas.gettingwatchdata.modal.WatchData;

import static vikas.gettingwatchdata.Constants.Constant.BLOOD_PRESSURE_OXYGEN;
import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE;
import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE_ALL;
import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE_REAL;
import static vikas.gettingwatchdata.Constants.Constant.OXYGEN;
import static vikas.gettingwatchdata.Constants.Constant.PEDOMETER_ALL;

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
        Intent broadcastIntent = new Intent("vikas.gettingwatchdata.Service");
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
        intent.setAction("vikas.gettingwatchdata.Service");


        ctx.sendBroadcast(intent);


    }
    public class LocationServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }
}
