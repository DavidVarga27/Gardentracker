package sk.upjs.ics.android.gardentracker;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class GardenMaintenanceFormDetail extends AppCompatActivity {
    //TODO: zmenit stringy
    private EditText nameEditText,descriptionEditText,intervalEditText;
    private DatePicker datePicker;
    private Button changeSaveButton;
    private int position;
    private int id;
    private boolean editable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);// vypne zobrazovanie klavesnice
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_maintenance_form);

        fillOutForm(savedInstanceState);

    }

    private void fillOutForm(Bundle savedInstanceState){
        changeSaveButton = (Button) findViewById(R.id.saveButton);
        ((Button) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        changeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getResources().getString(R.string.save).equals(changeSaveButton.getText().toString())){
                    String name = nameEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    long startDate = calendar.getTimeInMillis();
                    String interval = intervalEditText.getText().toString();
                    int intervalInMilliseconds = Integer.parseInt(interval)*24*3600*1000;


                    if(name.length()==0 || description.length() == 0 || interval.length() == 0){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.fill_form_warning_toast),Toast.LENGTH_LONG).show();
                    }else {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Contract.Maintenance.NAME, name);
                        contentValues.put(Contract.Maintenance.DESCRIPTION, description);
                        contentValues.put(Contract.Maintenance.LAST_CHECK, startDate);
                        contentValues.put(Contract.Maintenance.NEXT_CHECK, startDate+intervalInMilliseconds);
                        contentValues.put(Contract.Maintenance.INTERVAL_IN_DAYS, Integer.parseInt(interval));


                        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {

                            @Override
                            protected void onUpdateComplete(int token, Object cookie, int result) {
                                Toast.makeText(GardenMaintenanceFormDetail.this, "Updated!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        };
                        Uri selectedMaintenanceUri = ContentUris.withAppendedId(Contract.Maintenance.CONTENT_URI, id);
                        queryHandler.startUpdate(0,Defaults.NO_COOKIE,selectedMaintenanceUri,contentValues,null,null);
                    }


                }else{
                    changeSaveButton.setText(getResources().getString(R.string.save));
                    nameEditText.setEnabled(true);
                    descriptionEditText.setEnabled(true);
                    intervalEditText.setEnabled(true);
                    datePicker.setEnabled(true);
                }


            }
        });

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        intervalEditText = (EditText) findViewById(R.id.intervalEditText);
        datePicker = (DatePicker) findViewById(R.id.startdatePicker);

        if (savedInstanceState == null) {
            String name, description;
            long interval,date;
            Intent intent = getIntent();
            position = intent.getIntExtra("position", 0);

            Cursor cursor = getContentResolver().query(Contract.Maintenance.CONTENT_URI, null, null, null, null);
            cursor.moveToPosition(position);
            id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));

            name = cursor.getString(cursor.getColumnIndex(Contract.Maintenance.NAME));
            description = cursor.getString(cursor.getColumnIndex(Contract.Maintenance.DESCRIPTION));
            interval = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance.INTERVAL_IN_DAYS));
            date = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.LAST_CHECK));
            changeSaveButton = (Button) findViewById(R.id.saveButton);
            changeSaveButton.setText("CHANGE");

            Log.d("Zavolalo", "databadzu");

            //TODO: skontrolovat ci to je ok podla novotneho predstav



            nameEditText.setText(name);
            descriptionEditText.setText(description);
            intervalEditText.setText(String.valueOf(interval));

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);

            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            nameEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);
            intervalEditText.setEnabled(false);
            datePicker.setEnabled(false);
        }
    }

    private boolean isEditable(){
        return changeSaveButton.getText().toString()== getResources().getString(R.string.save);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("name",((EditText) findViewById(R.id.nameEditText)).getText().toString());
        savedInstanceState.putString("description",((EditText) findViewById(R.id.descriptionEditText)).getText().toString());
        DatePicker datePicker = (DatePicker) findViewById(R.id.startdatePicker);
        savedInstanceState.putInt("year",datePicker.getYear());
        savedInstanceState.putInt("month",datePicker.getMonth());
        savedInstanceState.putInt("day",datePicker.getDayOfMonth());
        savedInstanceState.putString("interval",((EditText) findViewById(R.id.intervalEditText)).getText().toString());//string intervalu
        savedInstanceState.putInt("id",id);
        Log.d("save",String.valueOf(isEditable()));
        savedInstanceState.putBoolean("editable",isEditable());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        DatePicker datePicker = (DatePicker) findViewById(R.id.startdatePicker);
        datePicker.updateDate(savedInstanceState.getInt("year"),savedInstanceState.getInt("month"),savedInstanceState.getInt("day"));
        ((EditText) findViewById(R.id.nameEditText)).setText(savedInstanceState.getString("name"));
        ((EditText) findViewById(R.id.descriptionEditText)).setText(savedInstanceState.getString("description"));
        ((EditText) findViewById(R.id.intervalEditText)).setText(savedInstanceState.getString("interval"));
        id = (savedInstanceState.getInt("id"));
        boolean isEditable = savedInstanceState.getBoolean("editable");
        Log.d("restore",String.valueOf(isEditable));
        nameEditText.setEnabled(isEditable);
        descriptionEditText.setEnabled(isEditable);
        intervalEditText.setEnabled(isEditable);
        datePicker.setEnabled(isEditable);
        if (isEditable)
            changeSaveButton.setText(getResources().getString(R.string.save));
        else
            changeSaveButton.setText(getResources().getString(R.string.change));
    }
}
