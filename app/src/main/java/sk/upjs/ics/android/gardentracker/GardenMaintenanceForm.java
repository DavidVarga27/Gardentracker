package sk.upjs.ics.android.gardentracker;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class GardenMaintenanceForm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_maintenance_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((Button) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((Button) findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
                String description = ((EditText) findViewById(R.id.descriptionEditText)).getText().toString();

                DatePicker datePicker = (DatePicker) findViewById(R.id.startdatePicker);
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                long startDate = calendar.getTimeInMillis();
                String interval = ((EditText) findViewById(R.id.intervalEditText)).getText().toString();
                if (name.length() == 0 || description.length() == 0 || interval.length() == 0) {

                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.fill_form_warning_toast),Toast.LENGTH_LONG).show();

                } else {
                    long intervalInMilliseconds = (Long.parseLong(interval)) * 24 * 3600 * 1000;

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Contract.Maintenance.NAME, name);
                    contentValues.put(Contract.Maintenance.DESCRIPTION, description);
                    contentValues.put(Contract.Maintenance.LAST_CHECK, startDate);
                    contentValues.put(Contract.Maintenance.NEXT_CHECK, startDate + intervalInMilliseconds);
                    contentValues.put(Contract.Maintenance.INTERVAL_IN_DAYS, Integer.parseInt(interval));


                    AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                        @Override
                        protected void onInsertComplete(int token, Object cookie, Uri uri) {
                            Toast.makeText(GardenMaintenanceForm.this, getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    };
                    queryHandler.startInsert(0, Defaults.NO_COOKIE, Contract.Maintenance.CONTENT_URI, contentValues);

                }
            }
        });

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("name", ((EditText) findViewById(R.id.nameEditText)).getText().toString());
        savedInstanceState.putString("description", ((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
        DatePicker datePicker = (DatePicker) findViewById(R.id.startdatePicker);
        savedInstanceState.putInt("year", datePicker.getYear());
        savedInstanceState.putInt("month", datePicker.getMonth());
        savedInstanceState.putInt("day", datePicker.getDayOfMonth());
        savedInstanceState.putString("interval", ((EditText) findViewById(R.id.intervalEditText)).getText().toString());//string intervalu

   }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        DatePicker datePicker = (DatePicker) findViewById(R.id.startdatePicker);
        datePicker.updateDate(savedInstanceState.getInt("year"), savedInstanceState.getInt("month"), savedInstanceState.getInt("day"));
        ((EditText) findViewById(R.id.nameEditText)).setText(savedInstanceState.getString("name"));
        ((EditText) findViewById(R.id.descriptionEditText)).setText(savedInstanceState.getString("description"));
        ((EditText) findViewById(R.id.intervalEditText)).setText(savedInstanceState.getString("interval"));
    }
}
