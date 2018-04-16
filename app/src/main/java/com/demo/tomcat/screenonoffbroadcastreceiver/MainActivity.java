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

    TextView    tvMessage;


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy(), ");
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
        setTitle(" Keep BroadcastReceiver Running After App Exit.");

        Intent backgroundService = new Intent(getApplicationContext(),
                                    ScreenOnOffBackgroundService.class);
        startService(backgroundService);
        Log.w(TAG, "Activity onCreate");

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

