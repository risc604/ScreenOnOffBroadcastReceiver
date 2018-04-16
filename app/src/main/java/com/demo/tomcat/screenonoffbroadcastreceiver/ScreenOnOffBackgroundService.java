package com.demo.tomcat.screenonoffbroadcastreceiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

//import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.*;
//import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.ACTION_ALARM_CANCEL;
//import static com.demo.tomcat.screenonoffbroadcastreceiver.MainActivity.ACTION_ALARM_SET;

public class ScreenOnOffBackgroundService extends Service
{
    private static final String TAG = ScreenOnOffBackgroundService.class.getSimpleName();
    public static final String ACTION_ALARM_SET = "com.demo.tomcat.ACTION_ALARM_SET";
    public static final String ACTION_ALARM_CANCEL = "com.demo.tomcat.ACTION_ALARM_CANCEL";


    AlarmManager alarmManager = null;
    PendingIntent alarmPI = null;
    NotificationManager manager = null;
    private final long USER_PEROID = 20*1000;
    private int alarmCount = 0;


    final LocalReceiver ScreenOnOffReceiver = new LocalReceiver();
    public class LocalReceiver extends BroadcastReceiver
    {
        public LocalReceiver() {
            super();
            Log.w(TAG, " LocalReceiver(), constructor.");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "onReceive(), action: " + action);

            if (Intent.ACTION_SCREEN_OFF.equalsIgnoreCase(action))
            {
                Log.d(TAG, "Screen is turn Off, alarmCount: " + alarmCount);
                cancelAlarm();
                alarmCount = 0;
                setAlarm();
            }
            else if (Intent.ACTION_SCREEN_ON.equalsIgnoreCase(action))
            {
                Log.d(TAG, "Screen is turn On.");
                //cancelAlarm();
                sendBroadcast(new Intent(ACTION_ALARM_CANCEL));
            }
            else if (ACTION_ALARM_SET.equalsIgnoreCase(action))
            {
                Log.d(TAG, " alarmCount: " + alarmCount);
                if (alarmCount < 5) {
                    notificationDialog("BackGround running");
                    setAlarm();
                }
                else {
                    sendBroadcast(new Intent(ACTION_ALARM_CANCEL));
                    alarmCount = 0;
                }
            }
            else if (ACTION_ALARM_CANCEL.equalsIgnoreCase(action))
            {
                //Log.d(TAG, "ACTION_ALARM_CANCEL.");
                cancelAlarm();
            }

        }
    }

    public ScreenOnOffBackgroundService() {
        Log.w(TAG, " ScreenOnOffBackgroundService(), constructor.");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, " onCreate(),");

        int iii=0;

        //while (iii < 10) {
        //    iii++;
        //    Intent intent =
                    registerReceiver(ScreenOnOffReceiver, makeIntentFilter());
            Log.i(TAG, " registerReceiver, " ); //+ iii + ", Intent: " + intent);
       //     if (intent != null) {
       //         iii = 100;
       //        break;
       //     }
       // }

            //Log.i(TAG, " registerReceiver Intent: " + intent);
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
        Log.w(TAG, " onBind(),");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, " onDestroy(),");
        super.onDestroy();
        if (ScreenOnOffReceiver != null)
        {
            unregisterReceiver(ScreenOnOffReceiver);
            Log.e(TAG, "Service onDestroy: screenOnOffReceiver is unregistered.");
        }

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

    private void setAlarm()
    {
        Log.w(TAG, " setAlarm(), ");
        long triggerTime = System.currentTimeMillis() + USER_PEROID;
        Intent  aIntent = new Intent(ACTION_ALARM_SET);
        alarmPI = PendingIntent.getBroadcast(this, 0,
                aIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            if (Build.VERSION.SDK_INT < 23)
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, alarmPI);
            }
            else
            {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, alarmPI);
            }
            alarmCount++;
        }
        else
        {
            String errorMsg = "Error !! alarmManager is null.";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm()
    {
        if (alarmManager != null)
        {
            Log.e(TAG, " cancelAlarm(), ");
            alarmManager.cancel(alarmPI);
            alarmPI = null;
            alarmManager = null;

            //alarmCount = 0;    //debug
        }
    }

    public void notificationDialog(String noteMSG)
    {
        Log.i(TAG, "notificationDialog(), noteMSG: " + noteMSG );

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent  mainIntent = new Intent();
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mainIntent.setAction(Intent.ACTION_MAIN);
        //mainIntent.addCategory(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        Log.w(TAG, "notificationDialog(), mainIntent: " + mainIntent);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                //mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainIntent,0);
        //PendingIntent contentIntent = PendingIntent.getActivities(this, 100,
        //        new Intent[]{mainIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bmpIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher_background);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(contentIntent)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(noteMSG)
                .setWhen(System.currentTimeMillis())
                //.setColor(Color.parseColor("#ff0000ff"))
                .setColor(Color.parseColor("#0046ae"))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(bmpIcon)
                //.setSound(Uri.parse("android.resource://" + getPackageName() +
                //        "/raw/kwahmah_02_alarm1"))
                .setVibrate(new long[]{0, 5000, 60 * 1000 * 60, 5000})
                .setPriority(Notification.PRIORITY_MAX);
        Log.w(TAG, "2 Notification title: " + getResources().getString(R.string.app_name) +
                ", " + noteMSG );



        builder.setSound(Uri.parse("android.resource://" + getPackageName() +
                "/raw/kwahmah_02_alarm1"));

        Notification notification = builder.build();
        //notification = builder.build();
        //notification.flags |= Notification.FLAG_INSISTENT;  //sound continue ...
        //notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;  //sound one shut
        notification.flags |= Notification.FLAG_AUTO_CANCEL;  //sound one shut
        //notification.flags |= Notification.FLAG_NO_CLEAR;  //sound one shut
        manager.notify(10, notification);
    }



}
