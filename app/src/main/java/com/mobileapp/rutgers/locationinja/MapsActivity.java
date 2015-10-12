package com.mobileapp.rutgers.locationinja;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MapsActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

//    final static int GET_LOCATION = 1;
    private GoogleMap mMap;

    TextView longitudeTV;
    TextView latitudeTV;
    TextView addressTV;
    Button BtnCheckIn;
    Button BtnViewAllLocations;
    Button BtnClearEntries;

    boolean locationEnabled;
    Thread checkGPSStatus;

    DatabaseHandler dbHandler;


    LocationManager locManager;
    Location myLoc;
    Location myLastlocation;

    Geocoder geocoder;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Building the API client - for Location Services API
        this.buildGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        longitudeTV = (TextView) findViewById(R.id.longitudeText);
        latitudeTV = (TextView) findViewById(R.id.latitudeText);
        addressTV = (TextView) findViewById(R.id.addressTextView);
        BtnCheckIn = (Button) findViewById(R.id.CheckInButton);
        BtnViewAllLocations = (Button) findViewById(R.id.buttonViewAllLocations);
        BtnClearEntries = (Button) findViewById(R.id.buttonClear);

        dbHandler = new DatabaseHandler(getApplicationContext(), null, null, 1);

        if (Geocoder.isPresent()) {
            geocoder = new Geocoder(MapsActivity.this);
        }

        BtnViewAllLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<checkedInLocation> myLocations = dbHandler.getAllMyLocations();
                if(myLocations.isEmpty())
                {
                    Log.e("LOCATIONINJA","LIST IS EMPTY");
                }
                else {
                    for (int index = 0; index < dbHandler.getLocationsCount(); index++) {
                        Log.e("LOCATIONINJA", String.valueOf(myLocations.get(index).getId())+ ", "+ myLocations.get(index).get_nameofplace() +
                                " , " + myLocations.get(index).get_latitude() + " , " + myLocations.get(index).get_longitude());
                    }
                }
            }
        });

        BtnClearEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.clearDatabase();
            }
        });


//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(!isGPSEnabled()&&!isNetworkEnabled()){
//                    showSettingsAlert();
//                }
//            }
//        });
    }

    private boolean isGPSEnabled()
    {
        locManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isNetworkEnabled()
    {
        locManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
        return locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    protected synchronized void buildGoogleApiClient() {
        Log.e("LocationNinja", "Inside: Building google API Client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //TODO: Check if location enabled - else don't go ahead with application

        if (!isGPSEnabled() && !isNetworkEnabled()) {
            Log.e("LocatioNinja", "Showing alert dialog for enabling location");
            showSettingsAlert();
        }
        else
            mGoogleApiClient.connect();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!mGoogleApiClient.isConnected() && isGPSEnabled() && isNetworkEnabled())
        {
            mGoogleApiClient.connect();
        }
        else
            BtnCheckIn.setEnabled(false);

        BtnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckingInAlert();
                //Add the location to the SQLite Database
//                checkedInLocation _checkedInLocation = new checkedInLocation(dbHandler.getLocationsCount(), "- ", String.valueOf(myLastlocation.getLatitude()),String.valueOf(myLastlocation.getLongitude()));
//                dbHandler.createLocation(_checkedInLocation);
            }
        });
    }

    private void showCheckingInAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this)
                .setTitle("CHECKING IN")
                .setMessage("Your present location will be checked in")
                .setCancelable(false);


        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedInLocation _checkedInLocation = new checkedInLocation(dbHandler.getLocationsCount(), "- ", String.valueOf(myLastlocation.getLatitude()), String.valueOf(myLastlocation.getLongitude()));
                dbHandler.createLocation(_checkedInLocation);
            }
        });

        alertDialog.show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    LatLng myPresentLoc;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true); //gives the blue-dot indicating the present location of the user

        //TODO:Check if location is enabled -- my checking if network or Gps are available

//        myLoc = mMap.getMyLocation(); //Deprecated - doesn't return anything

//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc, 20));
//            }
//        });
    }

    boolean mapupd = false;

    @Override
    public void onLocationChanged(Location location) {
        myLoc = location;
        Toast.makeText(MapsActivity.this,"Location Changed: "+myLoc.getLatitude() +" , " + myLoc.getLongitude(), Toast.LENGTH_LONG).show();

        if (checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED)
            if (checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {

                return;
            }

        myLoc= locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(!mapupd)
            updateMarker(myLoc);
    }

    public void updateMarker(Location location)
    {
        if (location != null && !mapupd) {
            Log.e("LocatioNinja", "my Location not null");

            myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myPresentLoc).title("I am Here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc,15));
            mapupd = true;

            longitudeTV.setText(String.valueOf(location.getLongitude()));
            latitudeTV.setText(String.valueOf(location.getLatitude()));

            updateAddress(location);
        }
        else{
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateAddress(Location location)
    {

//            ---------------------------------------------------------------------------------------------------------------------------------------
//            Using GeoCoder for obtaining the address given the location;
//            geoCoder created in onCreate() method
        if(location!=null) {
            List<Address> getAddresses = null;
            if (geocoder != null) {
                Log.e("LocatioNinja", "Geocoder present, getting the address");
                try {

                    getAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (getAddresses != null) {
                for (int i = 0; i < getAddresses.size(); i++) {
                    Log.e("LocationNinja", getAddresses.get(i).toString());
                    String addString = getAddresses.get(0).getAddressLine(0) + "\n" + getAddresses.get(0).getAddressLine(1) + "\n" +
                            getAddresses.get(0).getAddressLine(2);
                    addressTV.setText(addString);
                }
            }
//            ---------------------------------------------------------------------------------------------------------------------------------------
        }
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


    @Override
    public void onConnected(Bundle bundle) {

        Log.e("LocatioNinja", "Inside getConnected method");
        myLastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(myLastlocation!=null)
        {
            Toast.makeText(this, "Latitude: " +myLastlocation.getLatitude() +" , " + "Longitude: "+myLastlocation.getLongitude(), Toast.LENGTH_SHORT).show();
            updateMarker(myLastlocation);
        }
//        else{
//            //request location updates
//
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Services are unavailable right now: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
        mGoogleApiClient.reconnect();
//        if(mGoogleApiClient.isConnected())
//            this.onConnected(); //Get the bundle-- save the last known location in a bundle
    }

    public void showSettingsAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("GPS SETTINGS")
                                            .setMessage("GPS not enabled; Enable it now in the settings menu.");


        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MapsActivity.this.startActivityForResult(intent, 2);
            }
        });

        alertDialog.setNegativeButton("Change settings later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MapsActivity.this, "Oopsie! ALL FUNCTIONALITIES WILL NOT BE AVAILABLE!", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent _intent)
    {
        if(requestCode == 2)
        {
                if(isGPSEnabled()&&isNetworkEnabled()) {

                    BtnCheckIn.setEnabled(true);
                    mGoogleApiClient.connect();

                }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

}

