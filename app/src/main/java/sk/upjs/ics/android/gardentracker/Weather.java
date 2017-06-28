package sk.upjs.ics.android.gardentracker;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class Weather extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Weather.this,SearchCity.class);
                intent.putExtra("db","update");
                startActivity(intent);
                finish();
            }
        });

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);

                cursor.moveToFirst();
                String city = cursor.getString(cursor.getColumnIndex(Contract.Weather.CITY));

                WeatherTask rft = new WeatherTask();
                rft.execute(city);
            }
        };
        queryHandler.startQuery(0, Defaults.NO_COOKIE,Contract.Weather.CONTENT_URI,null,null,null,null);
    }

    //na vstupe je String, co je v podstate meno mesta....stranka kde sa robilo pocasie http://it4kt.cnl.sk/c/smart/2015/lecture.04.html
    public class WeatherTask extends AsyncTask<String, Void, JSONObject> {
        private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric&q=%s&APPID=89d15296f24595504c7abd4eae948aa7";
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.progress = new ProgressDialog(Weather.this);
            this.progress.setMessage(getResources().getString(R.string.searching));
            this.progress.show();
        }

        @Override
        protected JSONObject doInBackground(String... cities) {
            for (String city : cities) {
                try {
                    URL url = new URL(String.format(WEATHER_URL, city));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return null;
                    }
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line + '\n');
                    }
                    connection.disconnect();
                    return new JSONObject(stringBuilder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            this.progress.dismiss();

            if (json == null) {
                Toast.makeText(getApplicationContext(), "Connection error, weather data is null", Toast.LENGTH_SHORT).show();
                return;
            }else{

                //rozpytvavanie JSON-a zo stranky
                try {
                    JSONArray weather = json.getJSONArray("weather");
                    JSONObject weatherObject = weather.getJSONObject(0);
                    String description = weatherObject.getString("description");
                    String icon = "w" + weatherObject.getString("icon");

                    weatherObject = json.getJSONObject("main");
                    int currentTemperature = weatherObject.getInt("temp");
                    int maxTemperature = weatherObject.getInt("temp_max");
                    int minTemperature = weatherObject.getInt("temp_min");
                    int humidity = weatherObject.getInt("humidity");

                    weatherObject = json.getJSONObject("wind");
                    int windSpeed = weatherObject.getInt("speed");
                    int windDirection;
                    try {
                        windDirection = weatherObject.getInt("deg");
                    } catch (Exception e) {
                        windDirection = -1;
                    }

                    weatherObject = json.getJSONObject("sys");
                    long sunset = weatherObject.getLong("sunset");
                    long sunrise = weatherObject.getLong("sunrise");

                    String cityAndCountry = String.format(getResources().getString(R.string.city_country),json.getString("name"),weatherObject.getString("country"));
                    String mainWeather = String.format(getResources().getString(R.string.weather_main),currentTemperature,description);
                    String maxTemp = String.format(getResources().getString(R.string.max_temp),maxTemperature);
                    String minTemp = String.format(getResources().getString(R.string.min_temp),minTemperature);
                    String wind = String.format(getResources().getString(R.string.wind_speed_direction),windSpeed,degreesToDirection(windDirection));
                    String humidityS = String.format(getResources().getString(R.string.humidity),humidity);
                    String sunsetS = String.format(getResources().getString(R.string.sunset),longToTime(sunset));
                    String sunriseS = String.format(getResources().getString(R.string.sunrise),longToTime(sunrise));



                    ((TextView) findViewById(R.id.cityTextView)).setText(cityAndCountry);
                    ((TextView) findViewById(R.id.currentTemperatureTextView)).setText(mainWeather);
                    ((ImageView) findViewById(R.id.weatherImageView)).setImageResource(getResources().getIdentifier(icon,"drawable",getPackageName()));
                    ((TextView) findViewById(R.id.maxTemperatureTextView)).setText(maxTemp);
                    ((TextView) findViewById(R.id.minTemperatureTextView)).setText(minTemp);
                    ((TextView) findViewById(R.id.windTextView)).setText(wind);
                    ((TextView) findViewById(R.id.humidityTextView)).setText(humidityS);
                    ((TextView) findViewById(R.id.sunriseTextView)).setText(sunriseS);
                    ((TextView) findViewById(R.id.sunsetTexView)).setText(sunsetS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    //prepocitanie stupnov smeru vetra na Nazvy smeru
    private String degreesToDirection(int deg){
        if (deg == -1) {
            return "N/A";
        } else {
            double val = Math.floor((deg / 22.5) + 0.5);

            String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
            int indexOfDirection = (int) (val % 16);
            return directions[indexOfDirection];
        }
    }

    //cas v sekundach prerobeny na String
    private String longToTime(long time){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time*1000);

        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(date);
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
            Intent intent = new Intent(Weather.this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

