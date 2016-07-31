package com.franksapps.flickrapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by frankbrosnan on 12/07/2016.
 */
public class PollService extends IntentService {

    private static final String TAG="PollService";

    private static final int POOL_INTERVAL = 1000*60*1; //5 minutes.

    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static final String ACTION_SHOW_NOTIFICATION = "com.franksapps.flickrapp.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE = "com.franksapps.flickrapp.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    public PollService(){
        super(TAG);
    }

    @Override
    @SuppressWarnings("depreciation")
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable)
            return;
        Log.i(TAG,"Receieved an Intent"+intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickrFetcher.PREF_SEARCH_QUERY,null);
        String lastResultId = prefs.getString(FlickrFetcher.PREF_LAST_RESULT_ID,null);

        ArrayList<GalleryItem> items;
        if (query != null){
            items = new FlickrFetcher().search(query);
        } else {
            items = new FlickrFetcher().fetchItems();
        }

        if (items.size() == 0){
            return;
        }

        String resultId = items.get(0).getId();

        if (!resultId.equals(lastResultId)){
            Log.i(TAG,"Got a new result: "+resultId);

            //create a notification !
            Resources resources = getResources();
            Intent i = new Intent(this,PhotoGalleryActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            //notificationManager.notify(0,notification);
            //end create a notification !
            showBackgroundNotification(0,notification);

            //broadcast intent
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION),PERM_PRIVATE);
            //broadcast intent

        } else {
            Log.i(TAG,"Got an old result: "+resultId);
        }





        prefs.edit()
             .putString(FlickrFetcher.PREF_LAST_RESULT_ID,resultId)
             .commit();

    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE,requestCode);
        i.putExtra(NOTIFICATION,notification);
        sendOrderedBroadcast(i,PERM_PRIVATE,null,null,
                Activity.RESULT_OK, null,null);

    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pi = PendingIntent.getService(context,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                                      POOL_INTERVAL,pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                         .edit()
                         .putBoolean(PollService.PREF_IS_ALARM_ON,isOn)
                         .commit();

    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pi = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_NO_CREATE);
        return (pi != null);

    }

}
