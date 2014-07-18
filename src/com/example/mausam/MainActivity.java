package com.example.mausam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity
{
    /* JSON node names. */
    private static final String TAG_CITIES = "list";
    private static final String TAG_NAME = "name";
    private static final String TAG_MAIN = "main";
    private static final String TAG_TEMPERATURE = "temp";
    private static final String TAG_PRESSURE = "pressure";
    private static final String TAG_HUMIDITY = "humidity";

    JSONArray cities = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /* Generate the URL to fetch weather data depending on the current location. */
        String url = getWeatherURL();

        if (url.length() != 0)
        {
            /* Display the weather data. */
            printWeatherData(url);
        }
        else
        {
            TableLayout table = (TableLayout) findViewById(R.id.tableLayout);

            TableRow tableRow = (TableRow) table.findViewById(R.id.tableRowCurrentCity);

            TextView textViewCityName = (TextView) tableRow.findViewById(R.id.textViewCityName);
            textViewCityName.setText(R.string.failed);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private String getWeatherURL()
    {
        /* Get the current location and format the weather data URL accordingly. */
        MyLocation loc = new MyLocation(this);

        String url = "";

        if (loc.canGetLocation())
        {
            url = "http://api.openweathermap.org/data/2.1/find/city?lat=" +
                  loc.getLatitude() +
                  "&lon=" +
                  loc.getLongitude() +
                  "&cnt=10";
        	
            /* Temporary hack to set location to Mumbai. */
            url = "http://api.openweathermap.org/data/2.1/find/city?lat=18.96&lon=72.82&cnt=10";
        }
        else
        {
            loc.showSettingsAlert();
        }

        return url;
    }

    private void printWeatherData(String url)
    {
        /* Instantiate and launch an AsyncTask to fetch the weather data in a separate thread. */
        GetWeatherData task = new GetWeatherData();

        task.execute(url);
    }

    private void printWeather(TableRow tableRow, int jsonObjNum)
    {
        try
        {
            /* Extract all necessary information from the given JSON object. */
            JSONObject city = cities.getJSONObject(jsonObjNum);
            String name     = city.getString(TAG_NAME);

            JSONObject main    = city.getJSONObject(TAG_MAIN);
            String temperature = main.getString(TAG_TEMPERATURE);
            String pressure    = main.getString(TAG_PRESSURE);
            String humidity    = main.getString(TAG_HUMIDITY);

            /* Get references of the required text views. */
            TextView textViewCityName    = (TextView) tableRow.findViewById(R.id.textViewCityName);
            TextView textViewTemperature = (TextView) tableRow.findViewById(R.id.textViewTemperature);
            TextView textViewPressure    = (TextView) tableRow.findViewById(R.id.textViewPressure);
            TextView textViewHumidity    = (TextView) tableRow.findViewById(R.id.textViewHumidity);

            /* Set the texts in the text views. */
            textViewCityName.setText   (name);
            textViewTemperature.setText(temperature + " F");
            textViewPressure.setText   (pressure    + " hPa");
            textViewHumidity.setText   (humidity    + "%");
        }
        catch (JSONException e)
        {
            Log.v("JSONException", e.toString());
            e.printStackTrace();
        }
        catch (RuntimeException e)
        {
            Log.v("RuntimeException", e.toString());
            e.printStackTrace();
        }
    }

    private class GetWeatherData extends AsyncTask <String, Integer, String>
    {
        /* Execute the HTTP request (in a separate thread). */
        @Override
            protected String doInBackground(String... urls)
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet                 http = new HttpGet(urls[0]);

                try
                {
                    HttpResponse httpResponse = httpClient.execute(http);

                    if (httpResponse.getEntity() != null)
                    {
                        return EntityUtils.toString(httpResponse.getEntity());
                    }
                }
                catch (ClientProtocolException e)
                {
                    Log.v("ClientProtocolException", e.toString());
                    e.printStackTrace();
                }
                catch (UnsupportedEncodingException e)
                {
                    Log.v("UnsupportedEncodingException", e.toString());
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    Log.v("IOException", e.toString());
                    e.printStackTrace();
                }

                return "";
            }

        /* Parse the HTTP response (same as UI thread). */
        protected void onPostExecute(String httpString)
        {
            JSONObject jObj;

            try
            {
                jObj = new JSONObject(httpString);

                cities = jObj.getJSONArray(TAG_CITIES);

                TableLayout table = (TableLayout) findViewById(R.id.tableLayout);

                /* Show details of the current city. */
                TableRow tableRow = (TableRow) table.findViewById(R.id.tableRowCurrentCity);

                TextView textViewLblTemperature = (TextView) tableRow.findViewById(R.id.textViewLblTemperature);
                textViewLblTemperature.setText(R.string.temperature);

                TextView textViewLblPressure = (TextView) tableRow.findViewById(R.id.textViewLblPressure);
                textViewLblPressure.setText(R.string.pressure);

                TextView textViewLblHumidity = (TextView) tableRow.findViewById(R.id.textViewLblHumidity);
                textViewLblHumidity.setText(R.string.humidity);

                printWeather(tableRow, 0);

                /* Show the "Nearby cities" label. */
                tableRow = (TableRow) table.findViewById(R.id.tableRowNearbyCities);
                TextView textViewNearbyCities = (TextView) tableRow.findViewById(R.id.textViewNearbyCities);
                textViewNearbyCities.setText(R.string.nearby_cities);

                /* The total number of cities that can be displayed is one less than
                 * the number of rows because of the "Nearby cities" label. */
                int numCitiesPossible = table.getChildCount() - 1;

                /* Show details of the nearby cities. */
                for(int i = 1; (i < cities.length() && i < numCitiesPossible); i++)
                {
                    tableRow = (TableRow) table.getChildAt(i+1);
                    printWeather(tableRow, i);
                }
            }
            catch (JSONException e)
            {
                Log.v("JSONException", e.toString());
                e.printStackTrace();
            }
            catch (RuntimeException e)
            {
                Log.v("RuntimeException", e.toString());
                e.printStackTrace();
            }
        }
    }
}

