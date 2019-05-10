package com.fpmd.wearhealth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kct.bluetooth.KCTBluetoothManager;
import com.kct.bluetooth.bean.BluetoothLeDevice;
import com.kct.bluetooth.callback.IConnectListener;
import com.kct.command.BLEBluetoothManager;
import com.kct.command.IReceiveListener;
import com.kct.command.KCTBluetoothCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import com.fpmd.wearhealth.Constants.Constant;
import com.fpmd.wearhealth.Service.ForegroundService;
import com.fpmd.wearhealth.Utility.SavedData;
import com.fpmd.wearhealth.Utility.UtilityFunctions;
import com.fpmd.wearhealth.database.DatabaseHelper;
import com.fpmd.wearhealth.modal.WatchData;

import static com.fpmd.wearhealth.Constants.Constant.PEDOMETER_ALL;

public class HomeActivityBkp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,IConnectListener ,IReceiveListener {

    Button add_device_button,connect_device_button,disconnect_device_button,sync_time_device_button,data_recieved_from_device_button,testing_device_button,reset_button,current_time_button,get_data_from_db
            ,start_foreground_service,stop_foreground_service;
    TextView editText_data;
    BLEBluetoothManager bleBluetoothManager;
    KCTBluetoothManager kctBluetoothManager; // For the searching and finding the device through this manger class
    UtilityFunctions utilityFunctions;
    KCTBluetoothCommand kctBluetoothCommand;
    BluetoothDevice bluetoothDevice;

    private DatabaseHelper db;

    private static View view;
    private final Handler handler = new Handler();


   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        utilityFunctions.showToast(SavedData.getDeviceId());
        utilityFunctions.showToast(SavedData.getUserNumber());
    }*/

    private void setclickListener() {
        add_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevice();
            }
        });
        connect_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectDevice();
            }
        });
        disconnect_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectDevice();
            }
        });
        sync_time_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncTimeWithDevice();
            }
        });
        data_recieved_from_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchDataDemo();
            }
        });
        testing_device_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testingFunction();
            }
        });
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData();
            }
        });
        current_time_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utilityFunctions.showToast(utilityFunctions.getCurrentTime());
            }
        });
        get_data_from_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromdb();
            }
        });
        start_foreground_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyForegroundService();
            }
        });
        stop_foreground_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopForegroundService();
            }
        });
    }

    private void startMyForegroundService() {


      /*  Intent service = new Intent(this, ForegroundService.class);
        if (!ForegroundService.IS_SERVICE_RUNNING) {
            service.setAction(Constant.ACTION.STARTFOREGROUND_ACTION);
            ForegroundService.IS_SERVICE_RUNNING = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            }else
            startService(service);
        } else {

        }*/

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

    }
    private void stopForegroundService() {
        Intent service = new Intent(this, ForegroundService.class);
        service.setAction(Constant.ACTION.STOPFOREGROUND_ACTION);
        ForegroundService.IS_SERVICE_RUNNING = false;
        startService(service);
    }
    private void getDataFromdb() {
        List<WatchData> list = db.getAllNotes();
        //utilityFunctions.showToast(list.get(0).getResponse());
        JSONObject jsonObject ;
        JSONArray jsonArray = new JSONArray();
        String type = "";
        for(int i=0;i<list.size();i++){
            jsonObject = new JSONObject();
            if(list.get(i).getType() == PEDOMETER_ALL)
                type = "PEDOMETER";
            try {
                jsonObject.put("device_id",SavedData.getDeviceId());
                jsonObject.put("mobile",SavedData.getUserNumber());
                jsonObject.put("type",type);
                jsonObject.put("response",list.get(i).getResponse());
                jsonObject.put("unique_id",list.get(i).getUnique_id());
                jsonObject.put("timestamp",list.get(i).getTime_stamp());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        utilityFunctions.showToast(jsonArray.toString());
        utilityFunctions.showLogError(jsonArray.toString());
    }

    private void resetData() {
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_sendFactoryReset_pack());
    }

    private void testingFunction() {
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_sendMTKHeart_pack());
       // kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_sendMTKPressure_pack());
        //kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_sendMTKOxyen_pack());
    }

    private void fetchDataDemo() {
        // cheking other data here

        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_synData_pack(Constant.PEDOMETER,utilityFunctions.getCurrentTime()));
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_synData_pack(Constant.HEART_RATE,utilityFunctions.getCurrentTime()));
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_synData_pack(Constant.SLEEP,utilityFunctions.getCurrentTime()));
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_synData_pack(Constant.BLOOD_PRESSURE_OXYGEN,utilityFunctions.getCurrentTime()));
    }

    private void syncTimeWithDevice() {
        kctBluetoothManager.sendCommand_a2d(bleBluetoothManager.BLE_COMMAND_a2d_settime_pack());
    }

    private void disconnectDevice() {
        kctBluetoothManager.disConnect_a2d();
    }

    private void connectDevice() {
        if(kctBluetoothManager.getConnectState() == 3){
            utilityFunctions.showToast("Device is already connected");
        }else{
            if(bluetoothDevice==null)
                utilityFunctions.showToast("No device Found please scan again");
            else
                kctBluetoothManager.connect(bluetoothDevice,1);
        }


    }

    private void scanDevice(){
        kctBluetoothManager.scanDevice(true);
    }

    private void initilizebuttons() {
        utilityFunctions = new UtilityFunctions(this);
        add_device_button = (Button)findViewById(R.id.add_device_button);
        connect_device_button= (Button)findViewById(R.id.connect_device_button);
        disconnect_device_button= (Button)findViewById(R.id.disconnect_device_button);
        sync_time_device_button= (Button)findViewById(R.id.sync_time_device_button);
        data_recieved_from_device_button = (Button)findViewById(R.id.data_recieved_from_device_button);
        testing_device_button =  (Button)findViewById(R.id.testing_device_button);
        current_time_button =  (Button)findViewById(R.id.current_time_button);
        get_data_from_db =  (Button)findViewById(R.id.get_data_from_db);
        editText_data = (TextView) findViewById(R.id.editText_data);
        reset_button =   (Button)findViewById(R.id.reset_button);
        start_foreground_service=   (Button)findViewById(R.id.start_foreground_service);
        stop_foreground_service=   (Button)findViewById(R.id.stop_foreground_service);
        //bleBluetoothManager = BLEBluetoothManager.getInstance();
        //kctBluetoothManager =  KCTBluetoothManager.getInstance();
        //kctBluetoothManager.init(this,true);
        //kctBluetoothCommand = KCTBluetoothCommand.getInstance();
        //kctBluetoothManager.registerListener(this);
        //fetchDataDemo();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectState(int i) {
        utilityFunctions.showLogError("connect state "+i);
        if(i==2){
            utilityFunctions.showToast(bluetoothDevice.getName()+" Connecting");
        }
    }

    @Override
    public void onConnectDevice(BluetoothDevice bluetoothDevice) {
        utilityFunctions.showLogError("connectdevice vikas "+bluetoothDevice.getName());
        utilityFunctions.showToast(bluetoothDevice.getName()+" Connected");
    }

    @Override
    public void onScanDevice(BluetoothLeDevice bluetoothLeDevice) {

        bluetoothDevice = bluetoothLeDevice.getDevice();
        utilityFunctions.showLogError("scan device "+bluetoothLeDevice.getDevice());
        if(this.bluetoothDevice != null)
        utilityFunctions.showToast("Device Found "+bluetoothLeDevice.getDevice().getName()+" Connect The device now");
    }

    @Override
    public void onCommand_d2a(byte[] bytes) {
        utilityFunctions.showLogError("onCommand_d2a"+bytes.toString());
        for(int i =0 ;i<bytes.length;i++){
            utilityFunctions.showLogError("data "+i+" "+bytes[i]);
        }

        kctBluetoothCommand.d2a_command_Parse(this,bytes,this);
        kctBluetoothCommand.d2a_MTK_command(bytes,this);

    }

    @Override
    public void onReceive(int item_type, boolean b, Object... objects) {
        String data = "ItemType "+item_type+ " boolean "+b+" \n";
        utilityFunctions.showLogError("onReceive int "+item_type+" boolean "+b+ " Objects "+objects[0].toString());
        for(int i =0 ;i<objects.length;i++){
            utilityFunctions.showLogError("data "+i+" "+objects[i].toString());
            data += "data "+i+" "+objects[i].toString();

        }


        editText_data.setText(data);

        parseDataHere(item_type,objects[0].toString());
    }
private String step,month,hour,day,year,calorie,distance;
    private void parseDataHere(int item_type, String s) {
        try {

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

                    // inserting a row
                    utilityFunctions.showLogError("isInserting "+jsonObject.toString());
                   long isInserted = db.insertNote(jsonObject.toString(),unique_key,item_type);
                   utilityFunctions.showLogError("isInserted "+isInserted);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            utilityFunctions.showLogError("data error = "+e.getMessage());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        utilityFunctions.showLogError("onPointerCaptureChanged "+hasCapture);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        kctBluetoothManager.unregisterListener(this);
    }
}
