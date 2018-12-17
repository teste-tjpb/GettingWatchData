package vikas.gettingwatchdata;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
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
import android.widget.FrameLayout;
import android.widget.TextView;


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
import vikas.gettingwatchdata.fragments.HomeFragment;
import vikas.gettingwatchdata.modal.WatchData;

import static vikas.gettingwatchdata.Constants.Constant.HEART_RATE_ALL;
import static vikas.gettingwatchdata.Constants.Constant.PEDOMETER_ALL;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    UtilityFunctions utilityFunctions;
    //Buttons
    Button buttonStartStopService;
    TextView device_id,text_steps,text_calorie,text_distance,text_connected,text_sync_time;
    TextView textViewMe,textViewAnalysis,textViewHome,textViewReport,textViewMore;
    CardView serviceCard;

    FrameLayout container;

    private DatabaseHelper db;
    AlarmManager am ;
    PendingIntent servicePendingIntent ;
    Intent serviceIntent;

    private void reciveMessage() {
        registerReceiver(mMessageReceiver, new IntentFilter("vikas.gettingwatchdata.Service"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            Log.e("i m in Broadcast recive", "i m in Broadcast recive");
           setTextValues();
        }
    };
    private void setTextValues(){
        text_steps.setText(SavedData.getStep()+"");
        text_calorie.setText(SavedData.getCalorie()+"");
        text_distance.setText(SavedData.getDistance()+"");
        text_connected.setText("Watch Connected Status \n\n"+SavedData.getConnectStatus());
        text_sync_time.setText("Last Sync  \n\n"+SavedData.getLastSyncTime());
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        setTextValues();
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
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);



       // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
          //      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initilizebuttons();
        initFooter();
        initFrameLayout();
        setclickListener();
        device_id.setText("Device Id : "+SavedData.getDeviceId());
        //utilityFunctions.showToast(SavedData.getDeviceId());
        //utilityFunctions.showToast(SavedData.getUserNumber());

        // init alarm and intent here
        Context ctx = getApplicationContext();
/** this gives us the time for the first trigger.  */
        am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        serviceIntent = new Intent(ctx, ForegroundService.class);
        serviceIntent.setAction(Constant.ACTION.STARTFOREGROUND_ACTION);
// make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
        servicePendingIntent =
                PendingIntent.getService(ctx,
                        ForegroundService.SERVICE_ID, // integer constant used to identify the service
                        serviceIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        //end

        //working with fragments

        settingDefaultFragment(new HomeFragment());
        //end
    }

    private void settingDefaultFragment(Fragment destFragment) {
        // First get FragmentManager object.
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.container, destFragment);

        // Commit the Fragment replace action.
        fragmentTransaction.commit();
    }

    private void initFrameLayout() {
        container = findViewById(R.id.container);
    }

    private void initFooter() {
        textViewMe = findViewById(R.id.textViewMe);
        textViewAnalysis = findViewById(R.id.textViewAnalysis);
        textViewHome = findViewById(R.id.textViewHome);
        textViewReport = findViewById(R.id.textViewReport);
        textViewMore = findViewById(R.id.textViewMore);
    }
    private void changeFooterColor(TextView textView){
        textViewMe.setTextColor(getResources().getColor(R.color.textColor,null));
        textViewAnalysis.setTextColor(getResources().getColor(R.color.textColor,null));
        textViewHome.setTextColor(getResources().getColor(R.color.textColor,null));
        textViewReport.setTextColor(getResources().getColor(R.color.textColor,null));
        textViewMore.setTextColor(getResources().getColor(R.color.textColor,null));
        //setting drawable tint color
        ColorStateList colorStateList = getResources().getColorStateList(R.color.drawableTintClosed,null);
        textViewMe.setCompoundDrawableTintList(colorStateList);
        textViewAnalysis.setCompoundDrawableTintList(colorStateList);
        textViewHome.setCompoundDrawableTintList(colorStateList);
        textViewReport.setCompoundDrawableTintList(colorStateList);
        textViewMore.setCompoundDrawableTintList(colorStateList);

        textView.setTextColor(getResources().getColor(R.color.textColorSelected,null));
        ColorStateList colorStateListSelected = getResources().getColorStateList(R.color.drawableTintSelected,null);

        textView.setCompoundDrawableTintList(colorStateListSelected);
    }


    private void setclickListener() {
        textViewMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView= (TextView) view;
                changeFooterColor(textView);
            }
        });

        textViewAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView= (TextView) view;
                changeFooterColor(textView);
            }
        });

        textViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView= (TextView) view;
                changeFooterColor(textView);
            }
        });

        textViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView= (TextView) view;
                changeFooterColor(textView);
            }
        });

        textViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView= (TextView) view;
                changeFooterColor(textView);
            }
        });

        serviceCard.setOnClickListener(new View.OnClickListener() {
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


    }

    private void updateUI() {
        if(SavedData.isServiceRunning())
            buttonStartStopService.setText(getResources().getString(R.string.button_start));
        else
            buttonStartStopService.setText(getResources().getString(R.string.button_start));
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


        Calendar cal = Calendar.getInstance();
        long interval = 1000 * 60 * 5; // 5 minutes in milliseconds


        // FLAG to avoid creating a second service if there's already one running
// there are other options like setInexactRepeating, check the docs
        am.setRepeating(
                AlarmManager.RTC_WAKEUP,//type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
                cal.getTimeInMillis(),
                interval,
                servicePendingIntent
        );
        utilityFunctions.showToast("Please Wait While we fetch the status");
        this.getApplication().bindService(serviceIntent      , serviceConnection, Context.BIND_AUTO_CREATE);

        updateUI();

    }
    private void stopForegroundService() {
        Intent service = new Intent(this, ForegroundService.class);
        service.setAction(Constant.ACTION.STOPFOREGROUND_ACTION);
        ForegroundService.IS_SERVICE_RUNNING = false;
        startService(service);
        am.cancel(servicePendingIntent);
        SavedData.setServiceStatus(false);
        updateUI();
    }



    private void initilizebuttons() {
        serviceCard = findViewById(R.id.serviceCard);
        utilityFunctions = new UtilityFunctions(this);
        buttonStartStopService = findViewById(R.id.buttonStartStopService);
        text_calorie  = findViewById(R.id.text_calorie);
        text_steps  = findViewById(R.id.text_steps);
        text_distance  = findViewById(R.id.text_distance);
        text_connected  = findViewById(R.id.text_connected);
        text_sync_time = findViewById(R.id.text_sync_time);
        device_id = findViewById(R.id.device_id);
        db = new DatabaseHelper(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    public ForegroundService foregroundService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if (name.endsWith("ForegroundService")) {
                foregroundService = ((ForegroundService.LocationServiceBinder) service).getService();
                utilityFunctions.showToast(" service connected");
                SavedData.setServiceStatus(true);
                updateUI();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            utilityFunctions.showToast(className.getClassName()+" service disconnected");
            if (className.getClassName().equals("ForegroundService")) {
                foregroundService = null;
                SavedData.setServiceStatus(false);
                updateUI();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }


}
