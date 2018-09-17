package vikas.gettingwatchdata;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import java.util.Calendar;
import java.util.List;

import vikas.gettingwatchdata.Constants.Constant;
import vikas.gettingwatchdata.Service.ForegroundService;
import vikas.gettingwatchdata.Utility.SavedData;
import vikas.gettingwatchdata.Utility.UtilityFunctions;
import vikas.gettingwatchdata.database.DatabaseHelper;
import vikas.gettingwatchdata.modal.WatchData;

import static vikas.gettingwatchdata.Constants.Constant.BLOOD_PRESSURE_OXYGEN;
import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE_ALL;
import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE_REAL;
import static vikas.gettingwatchdata.Constants.Constant.OXYGEN;
import static vikas.gettingwatchdata.Constants.Constant.PEDOMETER_ALL;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    UtilityFunctions utilityFunctions;
    //Buttons
    Button buttonStartStopService,buttonStopService,getDbData,clearDb;
    TextView device_id,text_steps,text_calorie,text_distance,text_connected,text_sync_time;


    private DatabaseHelper db;


    private void reciveMessage() {
        registerReceiver(mMessageReceiver, new IntentFilter("vikas.gettingwatchdata.Service"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            Log.e("i m in Broadcast recive", "i m in Broadcast recive");
            text_steps.setText("No Of Steps Covered : \n\n"+SavedData.getStep());
            text_calorie.setText("Total Calorie Burnt : \n\n"+SavedData.getCalorie());
            text_distance.setText("Total Distance Covered : \n\n"+SavedData.getDistance());
            text_connected.setText("Watch Connected Staus \n\n"+SavedData.getConnectStatus());
            text_sync_time.setText("Last Sync : \n\n"+SavedData.getLastSyncTime());
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        text_steps.setText("No Of Steps Covered : \n\n"+SavedData.getStep());
        text_calorie.setText("Total Calorie Burnt : \n\n"+SavedData.getCalorie());
        text_distance.setText("Total Distance Covered : \n\n"+SavedData.getDistance());
        text_connected.setText("Watch Connected Staus \n\n"+SavedData.getConnectStatus());
        text_sync_time.setText("Last Sync : \n\n"+SavedData.getLastSyncTime());
        reciveMessage();
        if(SavedData.isServiceRunning()){
            startMyForegroundService();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExceptionHandler.register(this, "http://uvcabs.esy.es/crash_logs_watch/server.php?version=" + UtilityFunctions.getDeviceName()+"-"+Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here any message will be displayed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initilizebuttons();
        setclickListener();
        device_id.setText("Device Id : "+SavedData.getDeviceId());
        //utilityFunctions.showToast(SavedData.getDeviceId());
        //utilityFunctions.showToast(SavedData.getUserNumber());
    }

    private void getDataFromdb() {
        List<WatchData> list = db.getAllNotes();
        //utilityFunctions.showToast(list.get(0).getResponse());
        JSONObject jsonObject ,jsonReqPacket;
        JSONArray jsonArray = new JSONArray();
        String type = "";
        jsonReqPacket=new JSONObject();
        try {
            jsonReqPacket.put("mobile", SavedData.getUserNumber());
            jsonReqPacket.put("device_id", SavedData.getDeviceId());

            for (int i = 0; i < list.size(); i++) {
                jsonObject = new JSONObject();

                if (list.get(i).getType() == PEDOMETER_ALL)
                    type = "PEDOMETER";
                else if(list.get(i).getType() == HEART_RATE_ALL){
                    utilityFunctions.showLogError("Heart Rate also found");
                }


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
        utilityFunctions.showToast(jsonArray.toString());
        utilityFunctions.showLogError(jsonReqPacket.toString());
    }
    private void setclickListener() {
        buttonStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SavedData.isServiceRunning()) {
                    stopForegroundService();
                    SavedData.setServiceStatus(false);
                }
                else {
                    startMyForegroundService();
                    SavedData.setServiceStatus(true);
                }



            }
        });
        buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopForegroundService();
            }
        });
        getDbData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromdb();
            }
        });
        clearDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearDataFromDb();
            }
        });
    }

    private void updateUI() {
        if(SavedData.isServiceRunning())
            buttonStartStopService.setText("Stop Service");
        else
            buttonStartStopService.setText("Start Service V5");
    }

    private void clearDataFromDb() {
        db.deleteAll();
    }
    private void startMyForegroundService() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            utilityFunctions.showToast("Device do not support bluetooth device");
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)

                utilityFunctions.showToast("Please enable your bluetooth device");
                return;
            }
        }


        ForegroundService.IS_SERVICE_RUNNING = true;

        Context ctx = getApplicationContext();
/** this gives us the time for the first trigger.  */
        Calendar cal = Calendar.getInstance();
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long interval = 1000 * 60 * 5; // 5 minutes in milliseconds
        Intent serviceIntent = new Intent(ctx, ForegroundService.class);
        serviceIntent.setAction(Constant.ACTION.STARTFOREGROUND_ACTION);
// make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
        PendingIntent servicePendingIntent =
                PendingIntent.getService(ctx,
                        ForegroundService.SERVICE_ID, // integer constant used to identify the service
                        serviceIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        // FLAG to avoid creating a second service if there's already one running
// there are other options like setInexactRepeating, check the docs
        am.setRepeating(
                AlarmManager.RTC_WAKEUP,//type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
                cal.getTimeInMillis(),
                interval,
                servicePendingIntent
        );
        utilityFunctions.showToast("Please Wait While we fetch the status");
        updateUI();

    }
    private void stopForegroundService() {
        Intent service = new Intent(this, ForegroundService.class);
        service.setAction(Constant.ACTION.STOPFOREGROUND_ACTION);
        ForegroundService.IS_SERVICE_RUNNING = false;
        startService(service);
        Context ctx = getApplicationContext();
/** this gives us the time for the first trigger.  */


        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(ctx, ForegroundService.class);
        serviceIntent.setAction(Constant.ACTION.STARTFOREGROUND_ACTION);
        PendingIntent servicePendingIntent =
                PendingIntent.getService(ctx,
                        ForegroundService.SERVICE_ID, // integer constant used to identify the service
                        serviceIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(servicePendingIntent);
        updateUI();
    }



    private void initilizebuttons() {

        utilityFunctions = new UtilityFunctions(this);
        buttonStartStopService = (Button)findViewById(R.id.buttonStartStopService);
        buttonStopService = (Button)findViewById(R.id.buttonStopService);
        getDbData= (Button)findViewById(R.id.getDbData);

        text_calorie  = (TextView)findViewById(R.id.text_calorie);
        text_steps  = (TextView)findViewById(R.id.text_steps);
        text_distance  = (TextView)findViewById(R.id.text_distance);
        text_connected  = (TextView)findViewById(R.id.text_connected);
        text_sync_time = (TextView)findViewById(R.id.text_sync_time);
        clearDb= (Button)findViewById(R.id.clearDb);
        device_id = (TextView)findViewById(R.id.device_id);
        db = new DatabaseHelper(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        }
         else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mMessageReceiver);
    }


}
