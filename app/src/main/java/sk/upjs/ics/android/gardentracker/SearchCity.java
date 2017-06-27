package sk.upjs.ics.android.gardentracker;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class SearchCity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);

        final String dbOperation = getIntent().getStringExtra("db");

        ((Button)findViewById(R.id.searchButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = ((EditText)findViewById(R.id.cityEditText)).getText().toString();
                if (city.length() == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_city_warning_toast),Toast.LENGTH_SHORT).show();
                }
                else {


                    try {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);


                        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&APPID=89d15296f24595504c7abd4eae948aa7&q=" + city);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        if (connection.getResponseCode() == 200) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Weather.CITY, city);

                            switch (dbOperation) {
                                case "insert": {
                                    AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                        @Override
                                        protected void onInsertComplete(int token, Object cookie, Uri uri) {
                                            finish();
                                            Intent intent = new Intent(SearchCity.this, Weather.class);
                                            startActivity(intent);
                                        }
                                    };
                                    queryHandler.startInsert(0, Defaults.NO_COOKIE, Contract.Weather.CONTENT_URI, contentValues);
                                    break;
                                }
                                case "update": {
                                    Cursor cursor = getContentResolver().query(Contract.Weather.CONTENT_URI, null, null, null, null);
                                    cursor.moveToFirst();
                                    int id = cursor.getInt(cursor.getColumnIndex(Contract.Weather._ID));

                                    AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {

                                        @Override
                                        protected void onUpdateComplete(int token, Object cookie, int result) {
                                            finish();
                                            Intent intent = new Intent(SearchCity.this, Weather.class);
                                            startActivity(intent);
                                        }
                                    };
                                    Uri selectedUri = ContentUris.withAppendedId(Contract.Weather.CONTENT_URI, id);
                                    queryHandler.startUpdate(0, Defaults.NO_COOKIE,selectedUri,contentValues,null,null);


                                    break;
                                }
                            }

                        }
                        else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.city_not_found), Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
