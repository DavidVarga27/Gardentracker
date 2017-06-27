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
    public static final String WORKER_THREAD_NAME = "NotificationService";

    public NotificationService() {
        super(WORKER_THREAD_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getName(), "Zaplo notification service");
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

        if (  notificationTimeToday<=System.currentTimeMillis() && System.currentTimeMillis()<=notificationTimeToday+5*60*1000) {
            int numOfTask = 0;
            Date date = new Date(System.currentTimeMillis());
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            long startTime = date.getTime();
            long endTime = startTime + 24*3600*1000;

            cursor = getContentResolver().query(Contract.Maintenance.CONTENT_URI,null,null,null,null);

            while (cursor.moveToNext()) {
                long nextCheck = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.NEXT_CHECK));
                if (nextCheck >= startTime && nextCheck < endTime)
                    numOfTask ++;
            }
            if(numOfTask>0){
                triggerNotification(numOfTask);
            }

        }
    }

    @Override
    public void onDestroy() {
        Log.i(getClass().getName(), "Vyplo notification service");
        super.onDestroy();
    }

    private void triggerNotification(int taskCount) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.work_for_today))
                .setContentText(getResources().getQuantityString(R.plurals.notification_text_tasks,taskCount,taskCount))
                .setContentIntent(getNotificationContentIntent())
                .setTicker(getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.spade)
                .setSound(alarmSound)
                .getNotification();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(getResources().getString(R.string.app_name), 0, notification);
    }

    public PendingIntent getNotificationContentIntent() {
        int REQUEST_CODE = 0;
        int NO_FLAGS = 0;

        Intent intent = new Intent(getApplicationContext(), GardenMaintenance.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, NO_FLAGS);
        return contentIntent;
    }

}