package com.mobileapp.rutgers.locationinja;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

//    final static int GET_LOCATION = 1;
    private static GoogleMap mMap;

    static TextView longitudeTV;
    static TextView latitudeTV;
    static TextView addressTV;
    Button BtnCheckIn;
    Button BtnViewAllLocations;
    Button BtnClearEntries;
    CheckBox BtnViewHeatMap;
    ZoomControls ControlMapZoom;

    DatabaseHandler dbHandler;
    HMDatabaseHandler hmdbHandler;
    List<checkedInLocation> myLocations;
    MyLocationGetter myLocationGetter;

    LocationManager locManager;
    static Location myLastlocation;

    static Geocoder geocoder;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        Log.e("LocaioNinja", "The thread id is: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));

        //Building the API client - for Location Services API
        this.buildGoogleApiClient();

        //Start background task on obtaining locations for heatmaps
//        new updateLocationTask().execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        longitudeTV = (TextView) findViewById(R.id.longitudeText);
        latitudeTV = (TextView) findViewById(R.id.latitudeText);
        addressTV = (TextView) findViewById(R.id.addressTextView);
        BtnCheckIn = (Button) findViewById(R.id.CheckInButton);
        BtnViewAllLocations = (Button) findViewById(R.id.buttonViewAllLocations);
        BtnClearEntries = (Button) findViewById(R.id.buttonClear);
        BtnViewHeatMap = (CheckBox) findViewById(R.id.showHeatMapButton);
        ControlMapZoom = (ZoomControls) findViewById(R.id.mapZoomControls);

        dbHandler = new DatabaseHandler(getApplicationContext(), null, null, 1);

        myLocations = dbHandler.getAllMyLocations();
        showCheckIns(myLocations);

        hmdbHandler = new HMDatabaseHandler(getApplicationContext(), null, null, 1);

        if (Geocoder.isPresent()) {
            geocoder = new Geocoder(MapsActivity.this);
        }

        BtnViewAllLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<checkedInLocation> myLocations = dbHandler.getAllMyLocations();
                    showCheckIns(myLocations);
            }
        });

        BtnClearEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.clearDatabase();
            }
        });

    }

    private void showCheckIns(List<checkedInLocation> _myLocations) {

        AlertDialog.Builder _alertDialog = new AlertDialog.Builder(MapsActivity.this);
        _alertDialog.setTitle("CHECK-INS");

        if(_myLocations.isEmpty())
        {
            Log.e("LOCATIONINJA", "LIST IS EMPTY");

                    _alertDialog.setMessage("Sorry! You have no check-ins!");
            _alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        else
        {
            String _placenames[] = new String[dbHandler.getLocationsCount()];

            for (int index = 0; index < dbHandler.getLocationsCount(); index++) {
                Log.e("LOCATIONINJA", String.valueOf(_myLocations.get(index).getId()) + ", " + _myLocations.get(index).get_nameofplace() +
                        " , " + _myLocations.get(index).get_latitude() + " , " + _myLocations.get(index).get_longitude());
                _placenames[index] = _myLocations.get(index).get_nameofplace();
//            _myData.add(_myLocations.get(index).get_nameofplace());
            }

            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.locationlist, null);
            ListView lv = (ListView) convertView.findViewById(R.id.lview);
            (convertView.findViewById(R.id.etext)).setVisibility(View.INVISIBLE);

            if (_placenames.length != 0) {
                Log.e("LocatioNinja", "inside the non-empty checkins ");
                ArrayAdapter<String> _myData = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _placenames);
                lv.setAdapter(_myData);
            }

            _alertDialog.setView(convertView);

            _alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        _alertDialog.show();
    }

    static ArrayList<String> Distances = new ArrayList<>();
    public static class _bReceiver extends BroadcastReceiver{
        Bundle extras;
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()=="UPDATELOCATION") {
                Log.e("LOCATIONINJA", "Received broadcast");
                extras = intent.getExtras();
                Distances = intent.getStringArrayListExtra("distances");
                myLastlocation = (Location) extras.get("newlocation");
                updateMarker(myLastlocation);

                //update the Database to generate heatmap
                Timer _timer = new Timer("check in");
                final HMDatabaseHandler dbh = new HMDatabaseHandler(context, null, null, 1);

                _timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        checkedInLocation _checkedInLocation = new checkedInLocation(dbh.getLocationsCount(), "LocatioNinja-autocheckin",
                                String.valueOf(myLastlocation.getLatitude()), String.valueOf(myLastlocation.getLongitude()));
                        dbh.createHMLocation(_checkedInLocation);
                    }
                }, 10000, 300000);
            }
        }
    }

    private boolean isGPSEnabled(){
        locManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isNetworkEnabled(){
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
        if(!mGoogleApiClient.isConnected() && isGPSEnabled() && isNetworkEnabled()) {
            mGoogleApiClient.connect();
        }
        else if(!isGPSEnabled()&& !isNetworkEnabled()) {
             BtnCheckIn.setEnabled(false);
        }

        BtnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add the location to the SQLite Database
                showCheckingInAlert();
            }
        });
    }

    EditText tv;
    private void showCheckingInAlert() {
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.locationlist, null);
        tv = (EditText) convertView.findViewById(R.id.etext);

        AlertDialog.Builder _alertDialog = new AlertDialog.Builder(MapsActivity.this)
                .setTitle("CHECKING IN")
                .setView(convertView)
                .setMessage("Enter a tag for your check-in here -");

        _alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedInLocation _checkedInLocation = new checkedInLocation(dbHandler.getLocationsCount(), tv.getText().toString(),
                        String.valueOf(myLastlocation.getLatitude()), String.valueOf(myLastlocation.getLongitude()));
                dbHandler.createLocation(_checkedInLocation);
                hmdbHandler.createHMLocation(_checkedInLocation);
                dialog.dismiss();
            }
        });

        _alertDialog.show();
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

    static LatLng myPresentLoc;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true); //gives the blue-dot indicating the present location of the user

        mMap.addMarker(new MarkerOptions().position(MyLocationGetter.BUSCH_CAMPUS_CENTER).title("Busch Campus Center"));
        mMap.addMarker(new MarkerOptions().position(MyLocationGetter.RUTGERS_STUDENT_CENTER).title("Rutgers student center"));
        mMap.addMarker(new MarkerOptions().position(MyLocationGetter.OLD_QUEENS).title("Old Queens"));
        mMap.addMarker(new MarkerOptions().position(MyLocationGetter.HIGH_POINT_SOLUTIONS_STADIUM).title("High Point Solutions Stadium"));
        mMap.addMarker(new MarkerOptions().position(MyLocationGetter.ELECTRICAL_ENG_BUILDING).title("Electrical Eng Building"));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String _name = marker.getTitle();
                int index = 0;
                switch (_name) {
                    case "Busch Campus Center":
                        index = 0;
                        break;
                    case "Rutgers Student Center":
                        index = 1;
                        break;
                    case "Old Queens":
                        index = 2;
                        break;
                    case "High Point Solution Stadium":
                        index = 3;
                        break;
                    case "Electrical Eng Building":
                        index = 4;
                        break;
                }

                marker.setSnippet("you are " + Distances.get(index) + " miles away from here!");
                marker.showInfoWindow();
                return false;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //check if location enabled
                if (isGPSEnabled() && isNetworkEnabled()) {
                    BtnCheckIn.setEnabled(true);
                }
                return false;
            }
        });

        //implementing Zoom in-out controls
        ControlMapZoom.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        });

        ControlMapZoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });

        //Show heatMap
        BtnViewHeatMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BtnViewHeatMap.isChecked())
                    addHeatMap();
            }
        });
    }

    private void addHeatMap() {
        List<checkedInLocation> _manualLocations = dbHandler.getAllMyLocations();
        List<checkedInLocation> myLocations = hmdbHandler.getAllMyHMLocations();
        myLocations.addAll(_manualLocations);

        Collection<LatLng> myCoordinates = new ArrayList<>();

        for(int i = 0; i<myLocations.size();i++) {
            myCoordinates.add(new LatLng(Double.parseDouble(myLocations.get(i).get_latitude()), Double.parseDouble(myLocations.get(i).get_longitude())));
        }

//      Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder().data(myCoordinates).build();
//      Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

    }

    static boolean mapupd = false;
    public static void updateMarker(Location location)
    {
        if (location != null) {
            Log.e("LocatioNinja", "my Location not null");

            myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            if(!mapupd) {
//                mMap.addMarker(new MarkerOptions().position(myPresentLoc).title("I am Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc, 13));
                mapupd = true;
            }
            longitudeTV.setText(String.valueOf(location.getLongitude()));
            latitudeTV.setText(String.valueOf(location.getLatitude()));
            updateAddress(location);
        }
    }

    private static void updateAddress(Location location)
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

    @Override
    public void onConnected(Bundle bundle) {

        Log.e("LocatioNinja", "Inside getConnected method");
        myLocationGetter = new MyLocationGetter(MapsActivity.this, mGoogleApiClient);
        Intent intent = new Intent();
        intent.setAction("GETLOCATION");
        myLocationGetter.onBind(intent);
        myLocationGetter.onHandleIntent(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Services are unavailable right now: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
        mGoogleApiClient.reconnect();
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
        if(requestCode == 2) {
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
        dbHandler.close();
    }

}

