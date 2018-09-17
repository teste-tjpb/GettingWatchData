/**
 * Created by vikasaggarwal on 10/04/18.
 */

import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;

import vikas.gettingwatchdata.Service.ForegroundService;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(ServiceBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, ForegroundService.class));;
    }
}