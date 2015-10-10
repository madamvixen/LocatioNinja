package com.mobileapp.rutgers.locationinja;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Malabika on 10/8/2015.
 */
public class MyLocationGetter implements LocationListener {

    Context context;
    Location myLocation;
    LocationManager myLocManager;

    static final LatLng BUSCH_CAMPUS_CENTER = new LatLng(40.523128,-74.458797);
    static final LatLng HIGH_POINT_SOLUTIONS_STADIUM = new LatLng(40.513817,-74.464844);
    static final LatLng ELECTRICAL_ENG_BUILDING = new LatLng(40.521663,-74.460665);
    static final LatLng RUTGERS_STUDENT_CENTER = new LatLng(40.502661,-74.451771);
    static final LatLng OLD_QUEENS = new LatLng(40.498720,-74.446229);


    public MyLocationGetter(Context context)
    {
        this.context = context;
        myLocation = getLocation();
    }

    private Location getLocation() {

        myLocManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

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
