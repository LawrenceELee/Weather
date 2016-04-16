package com.example.lawrence.weather;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

// This is the controller layer for the app.
// It has most of the logic for downloading JSON data from API.
// ...
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // stores Weather objects, 1 object for each "day" in the JSON data.
    private List<Weather> weatherList = new ArrayList<>();

    // ArrayAdapter for binding Weather objects to a ListView
    private WeatherArrayAdapter weatherArrayAdapter;

    // ListView container widget
    private ListView weatherListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AppCompatActivity must programmatically supply it's own tool/action bars
        // to be compatible for earlier versions of Android.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create ArrayAdapter to bind weatherList to the weatherListView
        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        // configure FAB to hide keyboard and initiate web service request
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from locationEditText and create web service URL
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());

                // hide keyboard and initiate a GetWeatherTask to download
                // weather data from OpenWeatherMap.org in a separate thread
                if (url != null) {
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    // note: AsyncTasks instances can only be execute() once.
                    // we have to create new instances for every execute().
                    getLocalWeatherTask.execute(url);
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // programmatically hide keyboard after user touches floating button
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    // create url to API (endpoint to send GET request).
    private URL createURL(String city) {
        // hard code for testing so you don't have to type input everytime
        //city = "new%20york"; // %20 is url encoded space char

        String baseUrl = APISettings.apiBase;
        String units = APISettings.apiUnits;
        String apiKey = APISettings.apiKey;

        try {
            // create URL for specified city and imperial units (Fahrenheit)
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") + units + apiKey;
            //String urlString = baseUrl + city + units + apiKey;

            Log.d(TAG, "city is: " + city);
            // test if Snackbar works for Android 4.4 (it does).
            //Snackbar.make(findViewById(R.id.coordinatorLayout), "city is: " + city, Snackbar.LENGTH_LONG).show();

            // urlString should resemble "http://api.openweathermap.org/data/2.5/forecast/daily?q=newyork&units=imperial&cnt=16&APPID=XXXXX"
            // where XXXXX is your specific api key.
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get weather data and save data to a local HTML file
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        // takes a generics of url, void, and JSONObject
        // url is used by doInBackground()
        // void is used by onProgressUpdate(), which we aren't using
        // jsonobject is used by onPostExecute()

        @Override
        protected JSONObject doInBackground(URL... params) {
            // we use variable num of params, but we only used the 1st param (params[0])
            HttpURLConnection connection = null;

            // try doing a HTTP connection
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                // if HTTP response code is 200 then OK
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    BufferedReader reader;
                    try{
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        // problem reading JSON data
                        Snackbar.make(
                                findViewById(R.id.coordinatorLayout),
                                R.string.read_error,
                                Snackbar.LENGTH_LONG
                        ).show();

                        e.printStackTrace();
                    }

                    Log.d(TAG, "JSON data: " + builder.toString());
                    return new JSONObject(builder.toString());

                } else {
                    // problem with HTTP transaction
                    Snackbar.make(
                            findViewById(R.id.coordinatorLayout),
                            R.string.connect_error,
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            } catch (Exception e) {
                // some sort of connection error (e.g. no wifi or data connection, firewall blocked, etc).
                Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.connect_error,
                        Snackbar.LENGTH_LONG
                ).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return null;
        }

        // this will be called by the main UI thread when needed.
        // process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject weather) {
            // repopulate weatherList with updated data
            convertJSONtoArrayList(weather);

            // notify adapter that we updated weather object and rebind to ListView
            weatherArrayAdapter.notifyDataSetChanged();

            // scroll back to the top of the list
            weatherListView.smoothScrollToPosition(0);
        }
    }

    // method to transform/"mung" and bind JSON data to Weather objects
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear(); // clear old weather data

        try {
            // get forecast's "list" JSONArray which contains 16 days of weather.
            JSONArray list = forecast.getJSONArray("list");

            // convert each element of list to a Weather object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject data = list.getJSONObject(i);

                // get the data's temperatures ("temp") JSONObject
                JSONObject temperatures = data.getJSONObject("temp");

                // get data's "weather" JSONObject for the description and icon
                JSONObject weather = data.getJSONArray("weather").getJSONObject(0);

                // add new Weather object to weatherList
                weatherList.add(
                        new Weather(
                                data.getLong("dt"), // date/time timestamp
                                temperatures.getDouble("min"), // minimum temperature
                                temperatures.getDouble("max"), // maximum temperature
                                data.getDouble("humidity"), // percent humidity
                                weather.getString("description"), // weather conditions
                                weather.getString("icon") // icon name
                        )
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
