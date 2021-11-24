package com.siddhi.fleet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startService(new Intent(context, TrackingService.class));
        } else {
            context.startService(new Intent(context, TrackingService.class));
        }
    }
}
