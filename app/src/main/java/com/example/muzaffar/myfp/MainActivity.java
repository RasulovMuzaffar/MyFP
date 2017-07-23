package com.example.muzaffar.myfp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {

    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    Button sendPosotions;

    TextView statusSending;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    Location l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        statusSending = (TextView) findViewById(R.id.statusSending);

        sendPosotions = (Button) findViewById(R.id.btnSendPositions);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sendPosotions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendPositions(l);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000 * 10, 1, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000 * 10, 1, locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        l = location;
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, speed = %3$.4f, time = %4$tF %4$tT",
                location.getLatitude(), location.getLongitude(), location.getSpeed(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public void onClickSendPositions(Location location) {
        String loko = "loko=5644";
        String lat = "lat="+location.getLatitude();
        String lon = "lon="+location.getLongitude();
        String time = "time="+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println(lat);
        System.out.println(lon);
        System.out.println(time);
        String url = "http://46.48.58.66/test?"+loko+"&"+lat+"&"+lon+"&"+time;
        OkHttpClient client = new OkHttpClient();

        client.newCall(
                new Request.Builder()
                        .url(url)
                        .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call c, IOException e) {
                        System.err.println("call failed");
                    }

                    @Override
                    public void onResponse(Call c, Response resp)
                            throws IOException {
                        System.out.println(resp.body().string());
                    }
                });
        statusSending.setText("" + tvLocationGPS.getText());
    }


}
