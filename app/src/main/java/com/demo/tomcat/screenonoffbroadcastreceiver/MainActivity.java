package com.demo.tomcat.screenonoffbroadcastreceiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

// https://www.dev2qa.com/android-keep-broadcast-receiver-running-after-application-exit/

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACTION_ALARM_SET = "com.demo.tomcat.ACTION_ALARM_SET";
    public static final String ACTION_ALARM_CANCEL = "com.demo.tomcat.ACTION_ALARM_CANCEL";

    TextView    tvMessage;

    AlarmManager alarmManager = null;
    PendingIntent alarmPI = null;
    NotificationManager manager = null;
    private final long USER_PEROID = 20*1000;
    private int alarmCount = 0;

    private LocalReceiver ScreenOnOffReceiver = new LocalReceiver();
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logFileCreated();
        Log.w(TAG, "onCreate(), ");

        if (Build.VERSION.SDK_INT > 22)
            marshmallowPermission();

        initView();
        initControl();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop(), ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult()...");

        int grantTotal = 0;
        Log.w(TAG, "permissions: " + Arrays.toString(permissions) +
                ", grantResults: " + Arrays.toString(grantResults));

        for (int i = 0; i < grantResults.length; i++) {
            grantTotal += grantResults[i];
        }
        Log.w(TAG, "grantTotal: " + grantTotal);

        if (grantTotal < 0) {
            marshmallowPermission();
        } else {
            switch (requestCode) {
                case 1:
                    if ((grantResults.length > 0) &&
                            (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(getApplicationContext(), "Permission OK",
                                Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Log.d(TAG, "onRequestPermissionsResult(), Permission denied.");
                        Toast.makeText(getApplicationContext(), "Permission denied",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //------------------- User function --------------------//
    private void initView()
    {
        Log.w(TAG, " initView(), ");
        tvMessage = findViewById(R.id.textView);
    }

    private void initControl()
    {
        Log.w(TAG, " initControl(), ");

        Intent intent = registerReceiver(ScreenOnOffReceiver, makeIntentFilter());
        Log.i(TAG, " registerReceiver Intent: " + intent);
    }

    private boolean marshmallowPermission()
    {
        Log.i(TAG, "marshmallowPermission() ...");

        int storagePermission = ContextCompat.checkSelfPermission(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE);
        int sysPermissionState = PackageManager.PERMISSION_GRANTED;
        if (storagePermission != sysPermissionState)
        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN))
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{   Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },  1);
                ////Toast.makeText(this, "Please give App those permission To Run ...",
                /// Toast.LENGTH_LONG).show();
                Log.d(TAG, " Error !! PERMISSION_DENIED ");
                return false;
            }
            else
                return true;
        }
        else
        {
            Log.d(TAG, " PERMISSION_GRANTED ");
            return true;
        }
    }

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

        Intent  mainIntent = MainActivity.this.getIntent();
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


    public void logFileCreated()
    {
        try
        {
            //final String logFilePath = "/storage/emulated/0/Download/"+"Log_mt24.txt";
            final String logFilePath =  Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Download/ScreenOnOff.txt";
            final String cmds00 = "logcat -d -f ";
            //final String cmds01 = "logcat *:e *:i | grep \"(" + mPID + ")\"";

            //String mPID = String.valueOf(android.os.Process.myPid());
            //String cmds01 = "logcat *:e *:i | grep \"(" + mPID + ")\"";

            File f = new File(logFilePath);
            if (f.exists() && !f.isDirectory())
            {
                if (!f.delete())
                {
                    Log.w(TAG, "FAIL !! file delete NOT ok.");
                }
            }

            java.lang.Process process = Runtime.getRuntime().exec(cmds00 + logFilePath);
            Log.w(TAG, "logFileCreated(), process: " + process.toString() +
                    ", path: " + logFilePath);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }




}

