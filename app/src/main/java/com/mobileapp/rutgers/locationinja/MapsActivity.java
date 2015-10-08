package com.mobileapp.rutgers.locationinja;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

//    final static int GET_LOCATION = 1;
    private GoogleMap mMap;

    TextView longitudeTV;
    TextView latitudeTV;
    TextView addressTV;


    LocationManager locManager;
    Location myLoc;

    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = new Intent(this, LaunchActivity.class);
        if(stopService(intent))
        {
            Log.e("LocatioNinja","LaunchActivity stopped");
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        longitudeTV = (TextView) findViewById(R.id.longitudeText);
        latitudeTV = (TextView) findViewById(R.id.latitudeText);
        addressTV = (TextView) findViewById(R.id.addressTextView);

        if(Geocoder.isPresent()) {
            Log.e("LocatioNinja","Geocoder Present");
            geocoder = new Geocoder(MapsActivity.this);
        }
        else
            Log.e("LocatioNinja","Geocoder Not Present");

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent _intent)
//    {
//        Bundle bundle;
//        if(requestCode == 1)
//        {
//            if(resultCode == RESULT_OK)
//            {
//                bundle = _intent.getExtras();
//                myLoc = (Location)bundle.get("LOCATION");
//
//                longitudeTV.setText(String.valueOf(myLoc.getLongitude()));
//                latitudeTV.setText(String.valueOf(myLoc.getLatitude()));
//            }
//        }
//    }

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
        mMap.setMyLocationEnabled(true);

        //Check if location is enabled -- my checking if network or Gps are available


        myLoc = mMap.getMyLocation();

        if(myLoc != null)
        {
            Log.e("LocatioNinja", "Location from Map is null");
        }


        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locManager!=null)
            Log.e("LocatioNinja", "Got Location Manager");

        if (checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED)
            if (checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                Log.e("LocatioNinja", "No permissions");
                return;
            }

            Criteria criteria = new Criteria();
            Log.e("locationNinja", "Checking for location");
            myLoc = locManager.getLastKnownLocation(locManager.getBestProvider(criteria, true));

        //test message
        Log.e("LocatioNinja", "Get Location");

        if (myLoc != null) {
            Log.e("LocatioNinja", "my Location not null-- On Map ready");

            myPresentLoc = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myPresentLoc).title("I am Here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc, 13));

            longitudeTV.setText(String.valueOf(myLoc.getLongitude()));
            latitudeTV.setText(String.valueOf(myLoc.getLatitude()));


            List<Address> getAddresses = null;
            if(geocoder!=null)
            {
                Log.e("LocatioNinja", "Geocoder present, getting the address");
                try {

                    getAddresses = geocoder.getFromLocation(myLoc.getLatitude(), myLoc.getLongitude(),10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(getAddresses!=null) {
                for (int i = 0; i < getAddresses.size(); i++) {
                    Log.e("LocationNinja", getAddresses.get(i).toString());
                    addressTV.setText(getAddresses.get(0).getAddressLine(0) + "\n" + getAddresses.get(0).getAddressLine(1)+ "\n" + getAddresses.get(0).getAddressLine(2) );
                }
            }

        }
        else {
            //onLocationChanged(myLoc);
            Log.e("LocatioNinja", "Location is Null");
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
//
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
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Log.e("LocatioNinja", "Checking permissions");
                return;
            }

        myLoc= locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(!mapupd)
            updateMarker(myLoc);



//        longitudeTV.setText(String.valueOf(myLoc.getLongitude()));
//        latitudeTV.setText(String.valueOf(myLoc.getLatitude()));

    }

    public void updateMarker(Location location)
    {
        if (location != null && !mapupd) {
            Log.e("LocatioNinja", "my Location not null");

            myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myPresentLoc).title("I am Here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc,14));
            mapupd = true;

            longitudeTV.setText(String.valueOf(myLoc.getLongitude()));
            latitudeTV.setText(String.valueOf(myLoc.getLatitude()));
        }
        else {
            //onLocationChanged(myLoc);
            Log.e("LocatioNinja", "Location is Null");

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
}

