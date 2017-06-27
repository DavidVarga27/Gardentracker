package sk.upjs.ics.android.gardentracker;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class Settings extends AppCompatActivity {

    TimePicker notificationTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
fillOutTimePicker();


    }

    private void fillOutTimePicker(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTime();
        final Button changeSaveButton = (Button) findViewById(R.id.changeSaveButton);
        changeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getString(R.string.change).equals(changeSaveButton.getText().toString())){
                    notificationTimePicker.setEnabled(true);
                    changeSaveButton.setText(getResources().getString(R.string.save));
                }else{
                    notificationTimePicker.setEnabled(false);
                    int minutesOfNotification=notificationTimePicker.getCurrentHour()*60+notificationTimePicker.getCurrentMinute();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Contract.Settings.NOTIFICATION_TIME,minutesOfNotification);

                    Cursor cursor = getContentResolver().query(Contract.Settings.CONTENT_URI, null, null, null, null);
                    cursor.moveToFirst();
                    int id = cursor.getInt(cursor.getColumnIndex(Contract.Settings._ID));

                    AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {

                        @Override
                        protected void onUpdateComplete(int token, Object cookie, int result) {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.updated),Toast.LENGTH_SHORT).show();
                        }
                    };
                    Uri selectedUri = ContentUris.withAppendedId(Contract.Settings.CONTENT_URI, id);
                    queryHandler.startUpdate(0, Defaults.NO_COOKIE,selectedUri,contentValues,null,null);

                    changeSaveButton.setText(getResources().getString(R.string.change));
                }
            }
        });


    }



    private void setTime() {
        notificationTimePicker = (TimePicker)findViewById(R.id.notificationTimePicker);
        notificationTimePicker.setIs24HourView(true);

        Cursor cursor = getContentResolver().query(Contract.Settings.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        int settingsMinutes = cursor.getInt(cursor.getColumnIndex(Contract.Settings.NOTIFICATION_TIME));
        int hour = settingsMinutes / 60;
        int minutes = settingsMinutes % 60;
        notificationTimePicker.setCurrentHour(hour);
        notificationTimePicker.setCurrentMinute(minutes);
        notificationTimePicker.setEnabled(false);
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
            Intent intent = new Intent(Settings.this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}