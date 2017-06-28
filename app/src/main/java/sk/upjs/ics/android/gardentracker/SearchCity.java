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
import android.view.Menu;
import android.view.MenuItem;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String dbOperation = getIntent().getStringExtra("db");

        ((Button)findViewById(R.id.searchButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String city = ((EditText)findViewById(R.id.cityEditText)).getText().toString();
                if (city.length() == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_city_warning_toast),Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        //docasne riesenie, ze sa povoli robit networkconnection v hlavnom vlakne
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);

//TODO: treba dat do async tasku
                        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&APPID=89d15296f24595504c7abd4eae948aa7&q=" + city);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        if (connection.getResponseCode() == 200) {
                            connection.disconnect();

                            switch (dbOperation) {
                                case "insert": {//toto prichadza z Uvodu, ked v tabulke Weather nie su data
                                    //vlozim mesto uplne prvy krat do tabulky
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(Contract.Weather.CITY, city);
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
                                case "update": {//toto prichadza z Weather

                                    //prepise sa meno mesta
                                    AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                        @Override
                                        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                            super.onQueryComplete(token, cookie, cursor);

                                            cursor.moveToFirst();
                                            int id = cursor.getInt(cursor.getColumnIndex(Contract.Weather._ID));
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put(Contract.Weather.CITY, city);

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
                                        }
                                    };
                                    queryHandler.startQuery(0, Defaults.NO_COOKIE,Contract.Weather.CONTENT_URI,null,null,null,null);
                                    break;
                                }
                            }
                        }

                        else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.city_not_found), Toast.LENGTH_SHORT).show();
                        }
                        connection.disconnect();

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
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
            Intent intent = new Intent(SearchCity.this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
