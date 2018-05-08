package com.example.voodoo.mikcdmo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button StartLocationMontoring, StartGeofenceMonitoring, StopGeofenceMonitoring;
    public static final String TAG = "MainActivity";
    public static final String GEOFENCE_ID = "mygeoid";

    GoogleApiClient googleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StartLocationMontoring = findViewById(R.id.Btn_StartLocationMon);
        StartLocationMontoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartLocation();
            }
        });


        StartGeofenceMonitoring = findViewById(R.id.Btn_StartGeofencing);
        StartGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatGeofence();
            }
        });

        StopGeofenceMonitoring = findViewById(R.id.Btn_StopGeofecning);
        StopGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopGeofence();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Conencted to GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Connection suspened");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: connection failed");
                    }
                })
                .build();


        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1234); //1234 er request code
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();

        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response != ConnectionResult.SUCCESS) {

            Log.d(TAG, "onResume: no google play");
        } else {
            Log.d(TAG, "onResume: no action req");
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();
        googleApiClient.disconnect();
    }


    private void StartLocation() {
        Log.d(TAG, "StartLocation: start location called");
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)//mili sek
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Log.d(TAG, "StartLocation: error failed check");
                return;
            }


            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "location updated lat/long" + location.getLongitude() + " " + location.getLatitude());
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, "StartLocation:" + e);
        }
    }

    private void StatGeofence() {
        Log.d(TAG, "StatGeofence: called");
        try {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(GEOFENCE_ID) //butik id
                    .setCircularRegion(33, -84, 100)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();
 
            Intent intent = new Intent(this, GeofenceSerivce.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (!googleApiClient.isConnected()) {
                Log.d(TAG, "googleapi not ocnnected");
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    Log.d(TAG, "StatGeofence: called check permission");
                    return;
                }
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    Log.d(TAG, "added geofence ok");
                                } else {
                                    Log.d(TAG, "error failed to add geofence" + status);
                                }
                            }
                        });
            }


        } catch (Exception e) {
            Log.d(TAG, "StatGeofence: " + e);
        }
    }

    private void StopGeofence() {
        Log.d(TAG, "StopGeofence: stopping geofence");
        ArrayList<String> geofenceIds = new ArrayList<String>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
    }


}
