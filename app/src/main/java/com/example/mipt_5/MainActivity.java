package com.example.mipt_5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout rlHome;
    private ProgressBar pbLoader;
    private TextView tvCityName, tvTemperature, tvCondition;
    private TextInputEditText tiEditCityName;
    private ImageView ivIcon, ivBlack, ivSearch;
    private RecyclerView rvWeather;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    private String cityName;
    private String lastCityName = "Vilnius";
    private TextInputLayout tilCityName;

    // Used to silence the warning about touch listener
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // To have full screen layout
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                             WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        rlHome = findViewById(R.id.rlHome);
        pbLoader = findViewById(R.id.pbLoader);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tiEditCityName = findViewById(R.id.tiEditCityName);
        ivIcon = findViewById(R.id.ivIcon);
        ivBlack = findViewById(R.id.ivBlack);
        ivSearch = findViewById(R.id.ivSearch);
        rvWeather = findViewById(R.id.rvWeather);
        tilCityName = findViewById(R.id.tilCityName);

        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        rvWeather.setAdapter(weatherRVAdapter);

        // Getting system location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Checking if user gave permission to location service
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                                                     Manifest.permission.ACCESS_COARSE_LOCATION},
                                                                                     PERMISSION_CODE);
            Log.d("[ PERMISSIONS ]", "Requested permissions");
        }

        Log.d("[ PERMISSIONS ]", "Got permissions");

        // Getting last known location from device
        // Couldn't get to work on emulator
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            Log.e("[ LOCATION ]", "Last known location was null");
        }

        // Couldn't get getLastKnownLocation to work, so Hardcoded Vilnius as default location
        // cityName = getCityName(location.getLatitude(), location.getLongitude());
        cityName = "Vilnius";
        getWeatherInfo(cityName);

        // Check for input when clicking search icon
        ivSearch.setOnClickListener(view -> {
            String city = Objects.requireNonNull(tiEditCityName.getText()).toString();
            if (city.isEmpty()) {
                Log.d("[ ivSearch ]", "Input was empty");
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d("[ ivSearch ]", "Got input");
                getWeatherInfo(city);
            }
        });

        // If clicked on textInput and want to make keyboard disappear - press anywhere else on screen
        rlHome.setOnTouchListener((v, event) -> {
            Log.d("[ TouchListener ]", "Clicked on screen");
            InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
            return true;
        });

    }

    // Informational function helper for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                Log.d("[ PERMISSION ]", "Request code: " + requestCode + " granted!");
            }
            else {
                Toast.makeText(MainActivity.this, "Please provide permissions", Toast.LENGTH_SHORT).show();
                Log.d("[ PERMISSION ]", "Permissions were not provided");
                finish();
            }
        }
    }

    // Function the gets city name from coordinates (not used, because location service doesn't work)
    // But I'll leave it here
    private String getCityName(double latitude, double longitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);

            for(Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    }
                    else {
                        Log.d("[NOT FOUND]", "City Not Found");
                        Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityName;
    }

    // Main function which gets data from API
    // Didn't have to use ASYNC, because Volley is async it self
    // Every Volley request which takes a listener for example, success and fail listeners, is asynchronous and by default I want to say that almost all are asynchronous.
    // For more information you can read more in Volley's documentation.
    // https://stackoverflow.com/questions/32438167/is-volley-service-calls-are-asynchronous
    // Volley is asynchronous which means you can participate on your own time without having to coordinate schedules.
    // https://www.volleyapp.com/#:~:text=Volley%20is%20asynchronous%20which%20means,without%20having%20to%20coordinate%20schedules
    @SuppressWarnings("SpellCheckingInspection")
    private void getWeatherInfo(String cityName) {
        tiEditCityName.setText("");
        // Building long string (cityName can change with user input)
        String url = "http://api.weatherapi.com/v1/forecast.json?key=28be60798e28475387a121902210512&q="
                + cityName
                + "&days=1&aqi=yes&alerts=yes";
        // Saving last name on screen to replace it if new city entered is non-existant
        lastCityName = tvCityName.getText().toString();
        // Setting up async call
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        // Making Async call
        // Making home visible
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            pbLoader.setVisibility(View.GONE);
            rlHome.setVisibility(View.VISIBLE);
            weatherRVModelArrayList.clear();

            try {
                // Our data is in JSON format, so we use getJSONObject
                String temperature = response.getJSONObject("current").getString("temp_c");
                tvTemperature.setText(String.format("%s%s", temperature, getString(R.string.Celsius)));
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                // Loads images
                Picasso.get().load("http:".concat(conditionIcon)).into(ivIcon);
                tvCondition.setText(condition);
                // Sets background images
                if (isDay == 1) {
                    //  morning
                    // Setting some text to dark so it is more visable
                    tvTemperature.setTextColor(ContextCompat.getColor(this, R.color.black_shade_1));
                    tiEditCityName.setTextColor(ContextCompat.getColor(this, R.color.black_shade_1));
                    tvCityName.setTextColor(ContextCompat.getColor(this, R.color.black_shade_1));
                    tilCityName.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black_shade_1)));
                    Picasso.get().load("https://images.unsplash.com/photo-1512508497406-d4c5505afbca?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80").into(ivBlack);
                }
                else {
                    // night
                    // Setting some text to white so it is more visable
                    tvTemperature.setTextColor(ContextCompat.getColor(this, R.color.white));
                    tiEditCityName.setTextColor(ContextCompat.getColor(this, R.color.white));
                    tiEditCityName.setHintTextColor(ContextCompat.getColor(this, R.color.white));
                    tilCityName.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                    Picasso.get().load("https://images.unsplash.com/photo-1505322022379-7c3353ee6291?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8bmlnaHR8ZW58MHx8MHx8&auto=format&fit=crop&w=500&q=60").into(ivBlack);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecastDay = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecastDay.getJSONArray("hour");

                // Looping through hourly forecast and putting it in the RecyclerView
                for (int i = 0; i < hourArray.length(); i++) {
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModelArrayList.add(new WeatherRVModel(time, temper, img, wind));
                }
                // Refreshing every time the data is changed
                // It's here because changing cities we need to change whole data
                weatherRVAdapter.notifyDataSetChanged();
                tvCityName.setText(cityName);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            tvCityName.setText(lastCityName);
            Log.e("[ JSON Request ]", "City doesn't exist");
            Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

}