package com.demo.tomcat.screenonoffbroadcastreceiver;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.*;
import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.ACTION_ALARM_CANCEL;
import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.ACTION_ALARM_SET;

public class ScreenOnOffBackgroundService extends Service
{
    private static final String TAG = ScreenOnOffBackgroundService.class.getSimpleName();

    MainActivity.LocalReceiver screenOnOffReceiver = null;

    public ScreenOnOffBackgroundService() {
        Log.w(TAG, " ScreenOnOffBackgroundService(), constructor.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //screenOnOffReceiver = new MainActivity.LocalReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.w(TAG, " onStartCommand(),");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //---------------- user function ---------------------//
    private IntentFilter makeIntentFilter()
    {
        Log.w(TAG, " makeIntentFilter(), ");
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(ACTION_ALARM_SET);
        filter.addAction(ACTION_ALARM_CANCEL);
        filter.setPriority(100);
        return filter;
    }

}
