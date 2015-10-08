package com.mobileapp.rutgers.locationinja;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Malabika on 10/8/2015.
 */
public class MyLocationGetter implements LocationListener {

    Context context;
    Location myLocation;

    public MyLocationGetter(Context context)
    {
        this.context = context;
        myLocation = getLocation();
    }

    private Location getLocation() {




        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
