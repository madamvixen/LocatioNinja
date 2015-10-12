package com.mobileapp.rutgers.locationinja;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Malabika on 10/8/2015.
 */
public class MyLocationGetter extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    Context context;
    Location myLocation;
    LocationManager myLocManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    static final LatLng BUSCH_CAMPUS_CENTER = new LatLng(40.523128,-74.458797);
    static final LatLng HIGH_POINT_SOLUTIONS_STADIUM = new LatLng(40.513817,-74.464844);
    static final LatLng ELECTRICAL_ENG_BUILDING = new LatLng(40.521663,-74.460665);
    static final LatLng RUTGERS_STUDENT_CENTER = new LatLng(40.502661,-74.451771);
    static final LatLng OLD_QUEENS = new LatLng(40.498720,-74.446229);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyLocationGetter(String name) {
        super(name);
    }


    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(intent.getAction()=="") {

            startLocationUpdates();
        }
    }

    public class locationUpdateTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute()
        {
            myLocManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(myLocManager == null)
            {
                Toast.makeText(context, "Location Manager is null", Toast.LENGTH_SHORT).show();
            }
            createLocationRequest();
        }


        @Override
        protected Void doInBackground(Void... params) {
            //request for location updates in the background.
//             if (mRequestingLocationUpdates) {
//                    startLocationUpdates();
//                }
             return null;
        }
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
    }



    @Override
    public void onLocationChanged(Location location) {

    }


}