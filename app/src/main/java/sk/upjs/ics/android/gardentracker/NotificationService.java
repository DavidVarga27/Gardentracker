package sk.upjs.ics.android.gardentracker;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import sk.upjs.ics.android.gardentracker.provider.Contract;


public class NotificationService extends IntentService {
    public static final String WORKER_THREAD_NAME = "PresenceService";

    public NotificationService() {
        super(WORKER_THREAD_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getName(), "Presence service created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.d("onhandleINTENT","TU SI");

        Cursor cursor = getContentResolver().query(Contract.Settings.CONTENT_URI,null,null,null,null);
        cursor.moveToFirst();
        long notificationTime = cursor.getInt(cursor.getColumnIndex(Contract.Settings.NOTIFICATION_TIME));
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(year,month,day,0,0,0);
        long notificationTimeToday = calendar.getTimeInMillis()+notificationTime*1000*60;//ze kedy dneska ma byt notifikacia

        if (  notificationTimeToday<=System.currentTimeMillis() && System.currentTimeMillis()<=notificationTimeToday+60*1000) {
          /*  int num_of_task = 0;
            Date date = new Date(System.currentTimeMillis());
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            long start_time = date.getTime();
            long end_time = start_time + 24*3600*1000;

            cursor = getContentResolver().query(Contract.Maintenance.CONTENT_URI,null,null,null,null);

            while (cursor.moveToNext()) {
                long next_check = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.NEXT_CHECK));
                if (next_check >= start_time && next_check < end_time)
                    num_of_task ++;
            }*/
            triggerNotification(1);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(getClass().getName(), "Notification service destroyed");
        super.onDestroy();
    }

    private void triggerNotification(int taskCount) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Garden maintenance")
                .setContentText(String.format("Dnes musis spravit %d ulohy.",taskCount))
                .setContentIntent(getNotificationContentIntent())
                .setTicker("to co sa zobrazi hned na zaciatku")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.spade)
                .setSound(alarmSound)
                .getNotification();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("Presentr", 0, notification);
    }

    public PendingIntent getNotificationContentIntent() {
        int REQUEST_CODE = 0;
        int NO_FLAGS = 0;

        Intent intent = new Intent(getApplicationContext(), GardenMaintenance.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, NO_FLAGS);
        return contentIntent;
    }

}