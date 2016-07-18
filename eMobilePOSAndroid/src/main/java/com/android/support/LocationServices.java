package com.android.support;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by guarionex on 7/18/16.
 */
public class LocationServices {

    private Context context;
    public GoogleApiClient mGoogleApiClient;
    private Location lastLocation;

    public LocationServices(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        this.context = context;
        initGoogleApi(connectionCallbacks, onConnectionFailedListener);
    }

    private void initGoogleApi(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(onConnectionFailedListener)
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .build();
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }
}
