package com.fpmd.wearhealth; /**
 * Created by vikasaggarwal on 10/04/18.
 */

import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;

import com.fpmd.wearhealth.Service.ForegroundService;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(ServiceBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!! ok");
        context.startService(new Intent(context, ForegroundService.class));;
    }
}
