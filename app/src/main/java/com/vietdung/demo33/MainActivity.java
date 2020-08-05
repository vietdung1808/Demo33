package com.vietdung.demo33;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "loidemo33";
    private LatLng NASU_LATLNG = new LatLng(19.336425, 105.312462);
    private Location NASU_LOCATION = new Location(LocationManager.GPS_PROVIDER);
    private static final int REQUEST_CODE_LOCATION = 111;
    Button btnCheckIn;
    TextView tvDistance, tvLocation;
    GoogleMap map;

    TelephonyManager telephonyManager;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Location currentLocation;


    LocationCallback locationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
//                double distance = getDistance(location.getLatitude(), location.getLongitude(),
//                        NASU_LOCATION.latitude, NASU_LOCATION.longitude);
                float distance = location.distanceTo(NASU_LOCATION);
                tvLocation.setText(Html.fromHtml(
                        "<b>Latitude: </b>" + location.getLatitude()
                                + "<br><b>Longitude: </b>" + location.getLongitude()));
                tvDistance.setText(String.format("%2f Mét", distance));

                tvDistance.append("\n" + telephonyManager.getDeviceId());


                map.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                        .snippet("Vị trí hiện tại")).showInfoWindow();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(NASU_LATLNG);
                builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                LatLngBounds bounds = builder.build();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
            }
            stopLocationUpdates();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        NASU_LOCATION.setLatitude(NASU_LATLNG.latitude);
        NASU_LOCATION.setLongitude(NASU_LATLNG.longitude);

        btnCheckIn = findViewById(R.id.buttonCheckIn);
        tvDistance = findViewById(R.id.textViewDistance);
        tvLocation = findViewById(R.id.textViewLocation);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);


        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                    getLastLocation();

                    map.clear();
                    addLocation();
                    checkSettingAndStartLocationUpdates();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_LOCATION);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            getLastLocation();
            checkSettingAndStartLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            double distance = getDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                    NASU_LATLNG.latitude, NASU_LATLNG.longitude);
                            tvLocation.setText(Html.fromHtml(
                                    "<b>Latitude: </b>" + currentLocation.getLatitude()
                                            + "<br><b>Longitude: </b>" + currentLocation.getLongitude()));
                            tvDistance.setText(String.format("%2f Mét", distance));
                        } else {
                            tvLocation.setText("No data");
                        }
                    }
                });
    }

    private double getDistance(double lat1, double long1, double lat2, double long2) {
        double longDiff = long1 - long2;

        double distance = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(longDiff));

        distance = Math.acos(distance);
        distance = rag2deg(distance);

        //distance in miles
        distance = distance * 60 * 1.1515;

        //distance in met
        distance = distance * 1.609344 * 1000;

        return distance;
    }

    private double rag2deg(double distance) {
        return (distance * 180.0 / Math.PI);
    }

    private double deg2rad(double lat1) {
        return (lat1 * Math.PI / 180.0);
    }

    private void checkSettingAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();

            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(NASU_LATLNG, 18));
    }

    private void addLocation() {
        map.addMarker(new MarkerOptions()
                .position(NASU_LATLNG)
                .title("NASU")
                .snippet("Cổng chính nhà máy")).showInfoWindow();

        map.addCircle(new CircleOptions()
                .center(NASU_LATLNG)
                .radius(100)
                .strokeWidth(0f)
                .fillColor(0x550000FF));
    }

}