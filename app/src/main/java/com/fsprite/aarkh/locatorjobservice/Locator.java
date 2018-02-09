package com.fsprite.aarkh.locatorjobservice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class Locator extends LocationCallback implements OnSuccessListener<Location> {

    private static final String LOG_TAG = "LocatorJobServiceLogs";

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mProviderClient;
    private ArrayList<Location> mLocationList;
    private Location mLastLocation;
    private Context mContext;

    private Locator() {} // we need context for getting provider. That's why Locator can be created only with context in argument

    public Locator ( Context context ) {
        mContext = context;
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //default request. For custom request use setCustomLocationRequest()
        mProviderClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationList = new ArrayList<>();
    }

    public void setCustomLocationRequest( LocationRequest request ) {
        mLocationRequest = request;
    }

    @Nullable
    public Location getLocation() {
        Location location = null;
        // you calculations here. Usually you want to return mLastLocation as most accurate location possible
        return location;
    }

    /*
     * getting last location is usually enough, but for some reason it often returns null.
     * Due to this reason you should always call requestLocationUpdates() before getLastLocation()
     * Also
     * getLastLocation() return asynchronous class Task, not Location, as every sane person
     * would expect it to return, that's why we catch location in listener
     */
    @Nullable
    public void requestLastKnownLocation() {
        try {
            checkPermission();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mProviderClient.getLastLocation().addOnSuccessListener(this);
    }

    @Override
    public void onSuccess( Location location ) {
        if ( location == null ) {
            Log.d( LOG_TAG, "Last know location is null. Location is turned off in" +
                    " the device settings, or the device never recorded it's location" );
            return;
        }
        Log.d( LOG_TAG, "LastKnownLocation get non null Location object" +
                ", lat = " + location.getLatitude() +
                ", long = " + location.getLongitude() );
        mLastLocation = location;
    }

    /*
     * Keep in mind, that you SHOULD call FusedLocationProviderClient.requestLocationUpdates in main thread.
     */
    public void requestLocationUpdates () {
        try {
            checkPermission();
            mProviderClient.requestLocationUpdates( mLocationRequest, this, null );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        for ( Location location : locationResult.getLocations() ) {
            Log.d( LOG_TAG, "getting requested location" +
                    ", lat = " + location.getLatitude() +
                    ", long = " + location.getLongitude() );
            mLocationList.add( location );
        }
    }

    public void removeLocationUpdates () {
        mProviderClient.removeLocationUpdates( this );
    }

    private void checkPermission () throws IllegalAccessException {
        int fineLocationPermission = mContext.checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION );
        int coarseLocationPermission = mContext.checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
        if ( fineLocationPermission != PackageManager.PERMISSION_GRANTED || coarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            throw new IllegalAccessException( "permission denied by user" );
        }
    }

    /*
     * use this method, when you need more details about the the current state of the relevant location settings
     */
    public LocationSettingsStates getCurrentLocationSettings () {
        LocationSettingsRequest.Builder Builder = new LocationSettingsRequest.Builder().addLocationRequest( mLocationRequest );
        SettingsClient client = LocationServices.getSettingsClient( mContext );
        Task<LocationSettingsResponse> responseTask = client.checkLocationSettings( Builder.build() );
        return responseTask.getResult().getLocationSettingsStates();
    }

}
