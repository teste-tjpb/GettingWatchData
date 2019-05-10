package com.fpmd.wearhealth;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nullwire.trace.ExceptionHandler;

import com.fpmd.wearhealth.Utility.SavedData;
import com.fpmd.wearhealth.Utility.UtilityFunctions;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText txtMobile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExceptionHandler.register(this, "http://uvcabs.esy.es/crash_logs_watch/server.php?version=" + UtilityFunctions.getDeviceName()+"-"+Build.VERSION.SDK_INT);        setContentView(R.layout.activity_login);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        txtMobile = (EditText) findViewById(R.id.txtMobile);

        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SavedData.setDeviceId(android_id);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = txtMobile.getText().toString().trim();
                if(mobile.isEmpty()) {
                    txtMobile.setError("Mobile Number required");

                }else if(mobile.length()!=10){
                    txtMobile.setError("Mobile Number Invalid");
                }else {
                    SavedData.setUserNumber(mobile);
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }
            }
        });
    }
}
