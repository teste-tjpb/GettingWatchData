package vikas.gettingwatchdata;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nullwire.trace.ExceptionHandler;
import com.skyfishjy.library.RippleBackground;

import java.util.Arrays;

import vikas.gettingwatchdata.Utility.SavedData;
import vikas.gettingwatchdata.Utility.UtilityFunctions;

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
          final  static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=1263;

          boolean isPermissionGiven = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ExceptionHandler.register(this, "http://uvcabs.esy.es/crash_logs_watch/server.php?version=" + UtilityFunctions.getDeviceName()+"-"+Build.VERSION.SDK_INT);        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            UtilityFunctions.showLogErrorStatic("Permisioin not given check");
            isPermissionGiven = false;
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            isPermissionGiven = false;
            UtilityFunctions.showLogErrorStatic("Permisioin not given");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Please enable Location service to detect your watch and location accurately")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            isPermissionGiven = true;
            // Permission has already been granted
            UtilityFunctions.showLogErrorStatic("Permisioin ALready given");

        }
        new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over

                // Start your app main activity
                Intent i = null;

                if(SavedData.getUserNumber() =="") {

                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }else{
                    i = new Intent(SplashActivity.this, HomeActivity.class);
                }
                if(isPermissionGiven) {
                    rippleBackground.stopRippleAnimation();
                    startActivity(i);
                    // close this activity
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        UtilityFunctions.showLogErrorStatic("Permission Something"+ Arrays.toString(permissions)+Arrays.toString(grantResults)+" "+requestCode);
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    UtilityFunctions.showLogErrorStatic("Permission given");
                    isPermissionGiven = true;
                    Intent i = null;

                    if(SavedData.getUserNumber() =="") {

                        i = new Intent(SplashActivity.this, LoginActivity.class);
                    }else{
                        i = new Intent(SplashActivity.this, HomeActivity.class);
                    }
                    startActivity(i);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    UtilityFunctions.showLogErrorStatic("Permission denied");
                    isPermissionGiven = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
