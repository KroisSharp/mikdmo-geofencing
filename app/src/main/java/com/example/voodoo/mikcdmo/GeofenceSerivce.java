package com.example.voodoo.mikcdmo;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceSerivce extends IntentService {


    public static final String TAG = "geofenceSerivce";

    public GeofenceSerivce() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()){
            Log.d(TAG, "onHandleIntent: event error");
        }
        else {
            int transittion = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();

            if (transittion == geofence.GEOFENCE_TRANSITION_ENTER){
                Log.d(TAG, "onHandleIntent: enter geofence - " + requestId); //notifikationer
            }else if(transittion == geofence.GEOFENCE_TRANSITION_EXIT){
                Log.d(TAG, "onHandleIntent: exit geofence - " + requestId); //notifikationer
            }
        }
    }
}
