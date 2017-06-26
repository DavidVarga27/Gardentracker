package sk.upjs.ics.android.gardentracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import sk.upjs.ics.android.gardentracker.provider.Contract;

public class Uvod extends AppCompatActivity {
    //TODO: zmenit stringy
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);
        //display the logo during 3 seconds,
        new CountDownTimer(3000,1000){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                //set the new Content of your activity
                Uvod.this.setContentView(R.layout.uvod);

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                setButtons();
            }
        }.start();


        triggerNotification(2);



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

        Log.d("s","s");
        Log.d("s2","s2");
        Log.d("s","s");
    }

    public PendingIntent getNotificationContentIntent() {
        int REQUEST_CODE = 0;
        int NO_FLAGS = 0;

        Intent intent = new Intent(getApplicationContext(), GardenMaintenance.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, NO_FLAGS);
        return contentIntent;
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setButtons(){
        ((Button) findViewById(R.id.gardenMaintenanceButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Uvod.this,GardenMaintenance.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.photoDiaryButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Uvod.this,PhotoDiaryGallery.class);
                startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.weatherButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                Cursor cursor = getContentResolver().query(Contract.Weather.CONTENT_URI, null, null, null, null);
                if (cursor.getCount() == 0) {
                    intent = new Intent(Uvod.this, SearchCity.class);
                    intent.putExtra("db","insert");
                    startActivity(intent);
                }
                else {
                    intent = new Intent(Uvod.this, Weather.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uvod, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
