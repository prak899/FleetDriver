package com.siddhi.fleet;

import android.app.Application;

import com.instabug.apm.APM;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new Instabug.Builder(this, "1e7be21ff226ea24a95d79ba5d9a69fb")
                .setInvocationEvents(
                        InstabugInvocationEvent.SHAKE,
                        InstabugInvocationEvent.SCREENSHOT)
                .build();
        APM.setEnabled(true);


    }
}
