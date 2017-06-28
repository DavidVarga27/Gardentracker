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

    private EditText nameEditText,descriptionEditText,intervalEditText;
    private DatePicker datePicker;
    private Button changeSaveButton;
    private int position;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);// vypne zobrazovanie klavesnice
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_maintenance_form);

        fillOutForm(savedInstanceState);

    }

    private void fillOutForm(Bundle savedInstanceState){
        ((Button) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //inicializacia changeSaveButtona aj s updateom aj s udalostami okolo kliknutia
        changeSaveButton = (Button) findViewById(R.id.saveButton);
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

                        //updatovanie
                        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {

                            @Override
                            protected void onUpdateComplete(int token, Object cookie, int result) {
                                Toast.makeText(GardenMaintenanceFormDetail.this, getResources().getString(R.string.updated), Toast.LENGTH_LONG).show();
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
            Intent intent = getIntent();
            position = intent.getIntExtra("position", 0);

            //z databazy nacita udaje do formu
            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    super.onQueryComplete(token, cookie, cursor);

                    cursor.moveToPosition(position);
                    id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));

                    String name = cursor.getString(cursor.getColumnIndex(Contract.Maintenance.NAME));
                    String description = cursor.getString(cursor.getColumnIndex(Contract.Maintenance.DESCRIPTION));
                    long interval = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance.INTERVAL_IN_DAYS));
                    long date = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.LAST_CHECK));

                    nameEditText.setText(name);
                    descriptionEditText.setText(description);
                    intervalEditText.setText(String.valueOf(interval));

                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(date);

                    datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                }
            };
            queryHandler.startQuery(0, Defaults.NO_COOKIE,Contract.Maintenance.CONTENT_URI,null,null,null,null);

            changeSaveButton = (Button) findViewById(R.id.saveButton);
            changeSaveButton.setText(getResources().getString(R.string.change));
            nameEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);
            intervalEditText.setEnabled(false);
            datePicker.setEnabled(false);
        }
    }

    //ze ci je formular v editovatelnom stave, pouziva sa to v savedInstanceState
    private boolean isEditable(){
        return getResources().getString(R.string.save).equals(changeSaveButton.getText().toString());
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
