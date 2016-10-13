package com.bahrani.weathercool;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public CurrentWeather mCurrentWeather;
    private TextView mTemperatureLabel;
    private TextView mTimeLabel;
    private  TextView mHumidityValue;
    private  TextView mPrecipValue;
    private  TextView mSummaryLabel;
    private  ImageView mIconImageView;
    private ImageView refreshImageVIew;
    private ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTemperatureLabel = (TextView)findViewById(R.id.temperatureLabel);
        mTimeLabel = (TextView) findViewById(R.id.timeLabel);
        mHumidityValue = (TextView) findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) findViewById(R.id.summaryLabel);
        mIconImageView = (ImageView) findViewById(R.id.iconImageView);
        refreshImageVIew = (ImageView) findViewById(R.id.refreshImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.refreshProgressBar);

        mProgressBar.setVisibility(View.INVISIBLE);
        final double latitude= 37.8267 ;
        final double longitude = -122.4233;


        refreshImageVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getForcast(latitude, longitude);
            }
        });


        getForcast(latitude, longitude);

    }

    private void getForcast(double latitude, double longitude) {
        String apiKey = "ea0b57cc900f300b60b20a9224b10d5a";
        String forcastUrl = "https://api.darksky.net/forecast/"+ apiKey +"/"+latitude+","+longitude;
        // String forcastURL = "https://api.darksky.net/forecast/ea0b57cc900f300b60b20a9224b10d5a/37.8267,-122.4233";

        if(isNetworkAvailble() ) {
            toggleRefresh();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forcastUrl).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           toggleRefresh();
                       }
                   });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG,jsonData );
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exeption Cought", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception cought", e);
                    }
                }
            });
        }else {//End if
            Toast.makeText(this, R.string.network_unavailalble_message,Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            refreshImageVIew.setVisibility(View.INVISIBLE);
        }else {
            mProgressBar.setVisibility(View.INVISIBLE);
            refreshImageVIew.setVisibility(View.VISIBLE);

        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime()+ " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);


    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forcast = new JSONObject(jsonData);
        String timezone = forcast.getString("timezone");
        Log.i(TAG, "From joson: "+ timezone);
        JSONObject currently = forcast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setMtime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimezone(timezone);
        Log.d(TAG, currentWeather.getFormattedTime());



        return currentWeather;
    }

    private boolean isNetworkAvailble() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvaliable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvaliable = true;


        }
        return isAvaliable;
    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "Error_dialog");
    }
}
