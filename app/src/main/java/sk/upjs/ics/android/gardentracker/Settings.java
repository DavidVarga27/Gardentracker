package sk.upjs.ics.android.gardentracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TimePicker;

import sk.upjs.ics.android.gardentracker.provider.Contract;

public class Settings extends AppCompatActivity {

    TimePicker notificationTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTime();

    }

    private void setTime() {
        notificationTimePicker = (TimePicker)findViewById(R.id.notificationTimePicker);
        notificationTimePicker.setEnabled(false);
        notificationTimePicker.setIs24HourView(true);

        Cursor cursor = getContentResolver().query(Contract.Settings.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        int settings_minutes = cursor.getInt(cursor.getColumnIndex(Contract.Settings.NOTIFICATION_TIME));
        int hour = settings_minutes / 60;
        int minutes = settings_minutes % 60;
        notificationTimePicker.setCurrentHour(hour);
        notificationTimePicker.setCurrentMinute(minutes);

    }

}