package com.franksapps.flickrapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by frankbrosnan on 15/07/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG="NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"receieved result "+ getResultCode());
        if (getResultCode() != Activity.RESULT_OK){
            return;
        }
        //Everything OK !
        int requestCode = intent.getIntExtra("REQUEST_CODE",0);
        Notification notification = intent.getParcelableExtra("NOTIFICATION");
        NotificationManager notificationManager = (NotificationManager)
                                                   context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode,notification);
    }
}
